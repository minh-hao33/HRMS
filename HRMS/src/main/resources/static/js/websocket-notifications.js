/**
 * WebSocket Notifications Handler
 * Tích hợp cho trang chủ và trang thông báo
 */

document.addEventListener('DOMContentLoaded', function() {
  // Thông tin người dùng hiện tại từ localStorage
  let currentUser = {
    username: localStorage.getItem('Username') || 'guest',
    employeeName: localStorage.getItem('EmployeeName') || 'Guest User',
    avatar: localStorage.getItem('Avatar') || localStorage.getItem('Username')?.charAt(0).toUpperCase() || 'G',
    role: localStorage.getItem('Role') || 'guest'
  };

  // Cập nhật thông tin người dùng hiện tại trên UI
  const currentUserElement = document.getElementById('currentUser');
  const userAvatarElements = document.querySelectorAll('.user-avatar');

  if (currentUserElement) {
    currentUserElement.textContent = currentUser.username;
  }

  userAvatarElements.forEach(avatar => {
    avatar.textContent = currentUser.avatar;
  });

  // API Base URL
  const API_BASE_URL = 'http://localhost:8080/api/v1/notifications';

  // WebSocket connection
  let stompClient = null;
  let connected = false;

  // Hàm kết nối WebSocket
  function connectWebSocket() {
    if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
      console.error('SockJS or Stomp libraries not loaded');
      return;
    }

    if (!currentUser.username || currentUser.username === 'guest') {
      console.warn('Cannot connect to WebSocket: User not logged in');
      return;
    }

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    // Để tắt các log debug của Stomp
    stompClient.debug = null;

    stompClient.connect({}, function(frame) {
      console.log('Connected to WebSocket: ' + frame);
      connected = true;

      // Đăng ký nhận thông báo cá nhân
      stompClient.subscribe('/user/' + currentUser.username + '/queue/notifications', onNotificationReceived);

      // Gửi thông tin kết nối để server biết user nào đang online
      stompClient.send("/app/notification.connect", {},
          JSON.stringify({ type: "CONNECT", data: currentUser.username })
      );
    }, function(error) {
      console.error('WebSocket connection error:', error);
      connected = false;

      // Thử kết nối lại sau 5 giây nếu mất kết nối
      setTimeout(connectWebSocket, 5000);
    });
  }

  // Hàm ngắt kết nối WebSocket khi đóng trang
  function disconnectWebSocket() {
    if (stompClient !== null && connected) {
      stompClient.disconnect();
      connected = false;
    }
  }

  // Xử lý khi nhận thông báo mới qua WebSocket
  function onNotificationReceived(payload) {
    const message = JSON.parse(payload.body);
    console.log('Received WebSocket message:', message);

    switch(message.action) { // Đảm bảo sử dụng đúng tên thuộc tính
      case 'NEW_NOTIFICATION':
        // Đảm bảo timeAgo được thiết lập cho thông báo mới
        if (message.data && message.data.createdAt && !message.data.timeAgo) {
          message.data.timeAgo = formatTimeAgo(new Date(message.data.createdAt));
        }
        // Xử lý thông báo mới
        handleNewNotification(message.data);
        break;

      case 'MARK_READ':
        // Cập nhật trạng thái đã đọc cho thông báo
        markNotificationAsReadInUI(message.data);
        break;

      case 'MARK_ALL_READ':
        // Cập nhật trạng thái đã đọc cho tất cả thông báo
        markAllNotificationsAsReadInUI();
        break;

      case 'DELETE_NOTIFICATION':
        // Xóa thông báo khỏi giao diện
        removeNotificationFromUI(message.data);
        break;
    }

    // Cập nhật số lượng thông báo chưa đọc
    updateNotificationCount();
  }

  // Xử lý khi nhận thông báo mới
  function handleNewNotification(notification) {
    // Hiệu ứng rung chuông thông báo
    shakeNotificationBell();

    // Thêm thông báo mới vào dropdown
    prependNotificationToDropdown(notification);

    // Thêm thông báo mới vào bảng nếu đang hiển thị
    const notificationsTable = document.getElementById('notificationsTable');
    if (notificationsTable) {
      prependNotificationToTable(notification);
    }

    // Hiển thị thông báo toast
    showNotificationToast(notification);

    // Phát âm thanh thông báo
    playNotificationSound();
  }

  // Hiệu ứng rung chuông thông báo
  function shakeNotificationBell() {
    const bell = document.querySelector('.notification-bell');
    if (bell) {
      bell.classList.add('shake');

      // Xóa class shake sau khi animation hoàn thành
      setTimeout(function() {
        bell.classList.remove('shake');
      }, 1000);
    }
  }

  // Thêm thông báo mới vào đầu dropdown
  function prependNotificationToDropdown(notification) {
    const notificationList = document.getElementById('notificationList');
    if (!notificationList) return;

    const unreadClass = notification.read ? '' : 'unread';
    const newClass = 'new'; // Để có hiệu ứng highlight

    // Xác định icon dựa trên loại thông báo
    let iconClass = 'fas fa-bell';
    let iconStyle = 'icon-info';

    if (notification.type) {
      switch(notification.type.toLowerCase()) {
        case 'success':
          iconClass = 'fas fa-check-circle';
          iconStyle = 'icon-success';
          break;
        case 'warning':
          iconClass = 'fas fa-exclamation-triangle';
          iconStyle = 'icon-warning';
          break;
        case 'danger':
          iconClass = 'fas fa-exclamation-circle';
          iconStyle = 'icon-danger';
          break;
        default:
          iconClass = 'fas fa-bell';
          iconStyle = 'icon-info';
      }
    }

    const notificationItem = `
            <div class="notification-item ${unreadClass} ${newClass}" data-id="${notification.id}">
                <span class="notification-status"></span>
                <div class="notification-icon ${iconStyle}">
                    <i class="${iconClass}"></i>
                </div>
                <div class="notification-content">
                    <div class="notification-item-title">${notification.title}</div>
                    <div class="notification-item-text">${notification.content}</div>
                    <div class="notification-meta">
                        <div class="notification-sender">
                            <i class="fas fa-user"></i> ${notification.senderName || notification.sender}
                        </div>
                        <div class="notification-time">
                            <i class="far fa-clock"></i> ${notification.timeAgo}
                        </div>
                    </div>
                </div>
            </div>
        `;

    // Nếu danh sách trống, xóa thông báo "không có thông báo"
    if (notificationList.querySelector('.empty-notification')) {
      notificationList.innerHTML = '';
    }

    // Chèn HTML vào đầu danh sách
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = notificationItem;
    notificationList.insertBefore(tempDiv.firstChild, notificationList.firstChild);

    // Sau 2 giây, xóa class "new" để kết thúc hiệu ứng highlight
    setTimeout(function() {
      const addedItem = notificationList.querySelector(`.notification-item[data-id="${notification.id}"]`);
      if (addedItem) {
        addedItem.classList.remove('new');
      }
    }, 2000);
  }

  // Thêm thông báo mới vào bảng
  function prependNotificationToTable(notification) {
    const notificationsTable = document.getElementById('notificationsTable');
    if (!notificationsTable) return;

    const rowClass = notification.read ? '' : 'table-info';
    const statusBadge = notification.read
        ? '<span class="badge rounded-pill status-badge read">Đã đọc</span>'
        : '<span class="badge rounded-pill status-badge unread">Chưa đọc</span>';

    // Rút gọn nội dung nếu quá dài
    const truncatedContent = notification.content.length > 50
        ? notification.content.substring(0, 50) + '...'
        : notification.content;

    const row = document.createElement('tr');
    row.setAttribute('data-id', notification.id);
    row.className = rowClass;
    row.innerHTML = `
            <td>${notification.id}</td>
            <td>${notification.title}</td>
            <td>${truncatedContent}</td>
            <td>${notification.senderName || notification.sender}</td>
            <td>${notification.timeAgo}</td>
            <td>${statusBadge}</td>
            <td>
                <div class="d-flex">
                    <button class="btn btn-sm btn-info btn-action view-notification" data-id="${notification.id}" title="Xem chi tiết">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="btn btn-sm btn-danger btn-action delete-notification" data-id="${notification.id}" title="Xóa">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        `;

    // Nếu không có hàng nào hoặc có thông báo "không có thông báo", xóa và thêm dữ liệu mới
    if (notificationsTable.rows.length === 0 || notificationsTable.querySelector('td[colspan]')) {
      notificationsTable.innerHTML = '';
    }

    // Thêm vào đầu bảng
    if (notificationsTable.firstChild) {
      notificationsTable.insertBefore(row, notificationsTable.firstChild);
    } else {
      notificationsTable.appendChild(row);
    }
  }

  // Hiển thị toast khi có thông báo mới
  function showNotificationToast(notification) {
    const toastContainer = document.getElementById('toastContainer');
    if (!toastContainer) return;

    const toastId = 'toast-' + new Date().getTime();

    const toast = `
            <div class="toast" id="${toastId}" role="alert" aria-live="assertive" aria-atomic="true" data-bs-autohide="true" data-bs-delay="5000">
                <div class="toast-header">
                    <strong class="me-auto">${notification.title}</strong>
                    <small>${notification.timeAgo}</small>
                    <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
                <div class="toast-body">
                    ${notification.content}
                </div>
            </div>
        `;

    // Thêm toast vào container
    toastContainer.insertAdjacentHTML('beforeend', toast);

    // Khởi tạo toast bootstrap
    const toastElement = document.getElementById(toastId);
    if (toastElement) {
      const bsToast = new bootstrap.Toast(toastElement);
      bsToast.show();

      // Xóa toast sau khi bị ẩn
      toastElement.addEventListener('hidden.bs.toast', function() {
        this.remove();
      });
    }
  }

  // Phát âm thanh thông báo
  function playNotificationSound() {
    const audio = document.getElementById('notificationSound');
    if (!audio) return;

    // Reset âm thanh trước khi phát
    audio.pause();
    audio.currentTime = 0;

    // Phát âm thanh
    const playPromise = audio.play();

    // Xử lý lỗi phát âm thanh (thường do tương tác người dùng)
    if (playPromise !== undefined) {
      playPromise.catch(function(error) {
        console.log('Không thể phát âm thanh thông báo:', error);
      });
    }
  }

  // Cập nhật trạng thái đã đọc trong giao diện
  function markNotificationAsReadInUI(notificationId) {
    // Cập nhật trong dropdown
    const notificationItem = document.querySelector(`.notification-item[data-id="${notificationId}"]`);
    if (notificationItem) {
      notificationItem.classList.remove('unread');
    }

    // Cập nhật trong bảng
    const row = document.querySelector(`#notificationsTable tr[data-id="${notificationId}"]`);
    if (row) {
      row.classList.remove('table-info');
      const statusCell = row.querySelector('td:nth-child(6)');
      if (statusCell) {
        statusCell.innerHTML = '<span class="badge rounded-pill status-badge read">Đã đọc</span>';
      }
    }
  }

  // Đánh dấu tất cả thông báo đã đọc trong giao diện
  function markAllNotificationsAsReadInUI() {
    // Cập nhật trong dropdown
    document.querySelectorAll('.notification-item.unread').forEach(item => {
      item.classList.remove('unread');
    });

    // Cập nhật trong bảng
    const notificationsTable = document.getElementById('notificationsTable');
    if (notificationsTable) {
      notificationsTable.querySelectorAll('tr.table-info').forEach(row => {
        row.classList.remove('table-info');
      });

      notificationsTable.querySelectorAll('td:nth-child(6) .badge.unread').forEach(badge => {
        badge.classList.remove('unread');
        badge.classList.add('read');
        badge.textContent = 'Đã đọc';
      });
    }
  }

  // Xóa thông báo khỏi giao diện
  function removeNotificationFromUI(notificationId) {
    // Xóa khỏi dropdown
    const notificationItem = document.querySelector(`.notification-item[data-id="${notificationId}"]`);
    if (notificationItem) {
      notificationItem.remove();
    }

    // Xóa khỏi bảng
    const tableRow = document.querySelector(`#notificationsTable tr[data-id="${notificationId}"]`);
    if (tableRow) {
      tableRow.remove();
    }

    // Kiểm tra nếu không còn thông báo nào, hiển thị thông báo "không có thông báo"
    const notificationList = document.getElementById('notificationList');
    if (notificationList && notificationList.querySelectorAll('.notification-item').length === 0) {
      notificationList.innerHTML = `
                <div class="empty-notification">
                    <i class="fas fa-bell-slash"></i>
                    <p class="empty-text">Không có thông báo nào</p>
                </div>
            `;
    }

    const notificationsTable = document.getElementById('notificationsTable');
    if (notificationsTable && notificationsTable.rows.length === 0) {
      notificationsTable.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center py-4">
                        <i class="fas fa-bell-slash mb-2" style="font-size: 24px; color: #ccc;"></i>
                        <p class="mb-0">Không có thông báo nào</p>
                    </td>
                </tr>
            `;
    }
  }

  // Tải danh sách thông báo ban đầu
  function loadNotifications() {
    if (!currentUser.username || currentUser.username === 'guest') {
      console.warn('Cannot load notifications: User not logged in');
      return;
    }

    fetch(`${API_BASE_URL}/user/${currentUser.username}`)
    .then(response => response.json())
    .then(response => {
      console.log('API response:', response);

      // Kiểm tra cấu trúc phản hồi và trích xuất dữ liệu
      let notifications = [];
      if (response && response.data) {
        notifications = response.data;
      } else if (Array.isArray(response)) {
        // Trường hợp API trả về mảng trực tiếp
        notifications = response;
      }

      // Xử lý timeAgo cho mỗi thông báo
      notifications.forEach(notification => {
        if (!notification.timeAgo && notification.createdAt) {
          notification.timeAgo = formatTimeAgo(new Date(notification.createdAt));
        }
      });

      renderNotifications(notifications);
      updateNotificationCount();

      const notificationsTable = document.getElementById('notificationsTable');
      if (notificationsTable) {
        renderNotificationsTable(notifications);
      }
    })
    .catch(error => {
      console.error('Error loading notifications:', error);
      // Hiển thị trạng thái rỗng
      renderNotifications([]);

      const notificationsTable = document.getElementById('notificationsTable');
      if (notificationsTable) {
        renderNotificationsTable([]);
      }
    });
  }

  // Format time ago from date
  function formatTimeAgo(date) {
    if (!date || isNaN(date.getTime())) {
      console.warn('Invalid date provided to formatTimeAgo:', date);
      return 'Vừa xong';
    }

    const now = new Date();
    const diffMs = now - date;
    const diffSec = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSec / 60);
    const diffHour = Math.floor(diffMin / 60);
    const diffDay = Math.floor(diffHour / 24);

    if (diffSec < 60) {
      return 'Vừa xong';
    } else if (diffMin < 60) {
      return `${diffMin} phút trước`;
    } else if (diffHour < 24) {
      return `${diffHour} giờ trước`;
    } else if (diffDay < 30) {
      return `${diffDay} ngày trước`;
    } else {
      const day = date.getDate().toString().padStart(2, '0');
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const year = date.getFullYear();
      return `${day}/${month}/${year}`;
    }
  }

  // Cập nhật số lượng thông báo chưa đọc
  function updateNotificationCount() {
    if (!currentUser.username || currentUser.username === 'guest') {
      console.warn('Cannot update notification count: User not logged in');
      return;
    }

    fetch(`${API_BASE_URL}/user/${currentUser.username}/count`)
    .then(response => response.json())
    .then(response => {
      console.log('Count response:', response);

      // Xử lý nhiều cấu trúc phản hồi có thể có
      let count = 0;
      if (response && response.data && typeof response.data.count !== 'undefined') {
        // Cấu trúc ResultData với Map<String, Integer>
        count = response.data.count;
      } else if (response && typeof response.count !== 'undefined') {
        // Cấu trúc trực tiếp với count
        count = response.count;
      } else if (typeof response === 'number') {
        // Trường hợp API trả về số trực tiếp
        count = response;
      }

      const notificationCount = document.getElementById('notificationCount');
      if (notificationCount) {
        notificationCount.textContent = count;

        if (count > 0) {
          notificationCount.style.display = 'flex';
        } else {
          notificationCount.style.display = 'none';
        }
      }
    })
    .catch(error => {
      console.error('Error updating notification count:', error);

      // Đếm thủ công
      const unreadCount = document.querySelectorAll('.notification-item.unread').length;
      const notificationCount = document.getElementById('notificationCount');

      if (notificationCount) {
        notificationCount.textContent = unreadCount;

        if (unreadCount > 0) {
          notificationCount.style.display = 'flex';
        } else {
          notificationCount.style.display = 'none';
        }
      }
    });
  }

  // Hiển thị danh sách thông báo trong dropdown
  function renderNotifications(notifications) {
    const notificationList = document.getElementById('notificationList');
    if (!notificationList) return;

    notificationList.innerHTML = '';

    if (!notifications || notifications.length === 0) {
      notificationList.innerHTML = `
                <div class="empty-notification">
                    <i class="fas fa-bell-slash"></i>
                    <p class="empty-text">Không có thông báo nào</p>
                </div>
            `;
      return;
    }

    notifications.forEach(function(notification) {
      const unreadClass = notification.read ? '' : 'unread';

      // Xác định icon dựa trên loại thông báo
      let iconClass = 'fas fa-bell';
      let iconStyle = 'icon-info';

      if (notification.type) {
        switch(notification.type.toLowerCase()) {
          case 'success':
            iconClass = 'fas fa-check-circle';
            iconStyle = 'icon-success';
            break;
          case 'warning':
            iconClass = 'fas fa-exclamation-triangle';
            iconStyle = 'icon-warning';
            break;
          case 'danger':
            iconClass = 'fas fa-exclamation-circle';
            iconStyle = 'icon-danger';
            break;
          default:
            iconClass = 'fas fa-bell';
            iconStyle = 'icon-info';
        }
      }

      const notificationItem = `
                <div class="notification-item ${unreadClass}" data-id="${notification.id}">
                    <span class="notification-status"></span>
                    <div class="notification-icon ${iconStyle}">
                        <i class="${iconClass}"></i>
                    </div>
                    <div class="notification-content">
                        <div class="notification-item-title">${notification.title}</div>
                        <div class="notification-item-text">${notification.content}</div>
                        <div class="notification-meta">
                            <div class="notification-sender">
                                <i class="fas fa-user"></i> ${notification.senderName || notification.sender}
                            </div>
                            <div class="notification-time">
                                <i class="far fa-clock"></i> ${notification.timeAgo || formatTimeAgo(new Date(notification.createdAt))}
                            </div>
                        </div>
                    </div>
                </div>
            `;

      notificationList.insertAdjacentHTML('beforeend', notificationItem);
    });
  }

  // Hiển thị danh sách thông báo trong bảng
  function renderNotificationsTable(notifications) {
    const notificationsTable = document.getElementById('notificationsTable');
    if (!notificationsTable) return;

    notificationsTable.innerHTML = '';

    if (!notifications || notifications.length === 0) {
      notificationsTable.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center py-4">
                        <i class="fas fa-bell-slash mb-2" style="font-size: 24px; color: #ccc;"></i>
                        <p class="mb-0">Không có thông báo nào</p>
                    </td>
                </tr>
            `;
      return;
    }

    notifications.forEach(function(notification) {
      const rowClass = notification.read ? '' : 'table-info';
      const statusBadge = notification.read
          ? '<span class="badge rounded-pill status-badge read">Đã đọc</span>'
          : '<span class="badge rounded-pill status-badge unread">Chưa đọc</span>';

      // Rút gọn nội dung nếu quá dài
      const truncatedContent = notification.content.length > 50
          ? notification.content.substring(0, 50) + '...'
          : notification.content;

      // Đảm bảo timeAgo luôn có giá trị
      const timeAgo = notification.timeAgo ||
          (notification.createdAt ? formatTimeAgo(new Date(notification.createdAt)) : 'N/A');

      const row = `
                <tr data-id="${notification.id}" class="${rowClass}">
                    <td>${notification.id}</td>
                    <td>${notification.title}</td>
                    <td>${truncatedContent}</td>
                    <td>${notification.senderName || notification.sender}</td>
                    <td>${timeAgo}</td>
                    <td>${statusBadge}</td>
                    <td>
                        <div class="d-flex">
                            <button class="btn btn-sm btn-info btn-action view-notification" data-id="${notification.id}" title="Xem chi tiết">
                                <i class="fas fa-eye"></i>
                            </button>
                            <button class="btn btn-sm btn-danger btn-action delete-notification" data-id="${notification.id}" title="Xóa">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;

      notificationsTable.insertAdjacentHTML('beforeend', row);
    });
  }

  // Xử lý sự kiện click thông báo trong dropdown
  document.addEventListener('click', function(e) {
    // Kiểm tra xem click có trên notification-item không
    const notificationItem = e.target.closest('.notification-item');
    if (!notificationItem) return;

    const notificationId = notificationItem.dataset.id;
    if (!notificationId) return;

    // Đánh dấu đã đọc nếu chưa đọc
    if (notificationItem.classList.contains('unread')) {
      fetch(`${API_BASE_URL}/${notificationId}/read`, {
        method: 'PUT'
      })
      .then(response => {
        if (response.ok) {
          // WebSocket sẽ xử lý cập nhật UI
          console.log('Marked as read:', notificationId);
        }
      })
      .catch(error => {
        console.error('Error marking notification as read:', error);
        // Vẫn cập nhật UI dù có lỗi
        markNotificationAsReadInUI(notificationId);
      });
    }

    // Mở modal xem chi tiết thông báo
    openNotificationDetail(notificationId);
  });

  // Xử lý click vào nút xem chi tiết trong bảng
  document.addEventListener('click', function(e) {
    if (e.target.closest('.view-notification')) {
      e.preventDefault();
      const button = e.target.closest('.view-notification');
      const notificationId = button.dataset.id;
      openNotificationDetail(notificationId);
    }
  });

  // Xử lý click vào nút xóa thông báo
  document.addEventListener('click', function(e) {
    if (e.target.closest('.delete-notification')) {
      e.preventDefault();
      e.stopPropagation();
      const button = e.target.closest('.delete-notification');
      const notificationId = button.dataset.id;

      if (confirm('Bạn có chắc chắn muốn xóa thông báo này?')) {
        fetch(`${API_BASE_URL}/${notificationId}`, {
          method: 'DELETE'
        })
        .then(response => {
          if (response.ok) {
            // WebSocket sẽ xử lý cập nhật UI
            console.log('Deleted notification:', notificationId);
          }
        })
        .catch(error => {
          console.error('Error deleting notification:', error);
          // Vẫn xóa khỏi UI dù có lỗi
          removeNotificationFromUI(notificationId);
        });
      }
    }
  });

  // Hiển thị chi tiết thông báo trong modal
  function openNotificationDetail(notificationId) {
    const modal = document.getElementById('notificationDetailModal');
    if (!modal) return;

    fetch(`${API_BASE_URL}/${notificationId}`)
    .then(response => response.json())
    .then(response => {
      console.log('Notification detail response:', response);

      // Xử lý nhiều cấu trúc phản hồi có thể có
      let notification = null;
      if (response && response.data) {
        // Cấu trúc ResultData
        notification = response.data;
      } else {
        // Trường hợp API trả về thông báo trực tiếp
        notification = response;
      }

      if (!notification) {
        console.error('Notification not found or invalid response structure');
        alert('Không thể tải thông tin chi tiết thông báo. Vui lòng thử lại sau.');
        return;
      }

      // Ensure timeAgo is set
      if (!notification.timeAgo && notification.createdAt) {
        notification.timeAgo = formatTimeAgo(new Date(notification.createdAt));
      }

      // Cập nhật modal với dữ liệu thông báo
      document.getElementById('modalNotificationTitle').textContent = notification.title;
      document.getElementById('modalNotificationContent').textContent = notification.content;
      document.getElementById('modalSender').textContent = notification.senderName || notification.sender;
      document.getElementById('modalTime').textContent = notification.timeAgo || 'N/A';

      // Ẩn nút đánh dấu đã đọc nếu thông báo đã đọc
      const markReadBtn = document.getElementById('markReadBtn');
      if (markReadBtn) {
        if (notification.read) {
          markReadBtn.style.display = 'none';
        } else {
          markReadBtn.style.display = 'block';
          markReadBtn.dataset.id = notification.id;
        }
      }

      // Xác định icon cho modal
      let iconClass = 'fas fa-bell';
      let modalIconStyle = 'notification-icon icon-info';

      if (notification.type) {
        switch(notification.type.toLowerCase()) {
          case 'success':
            iconClass = 'fas fa-check-circle';
            modalIconStyle = 'notification-icon icon-success';
            break;
          case 'warning':
            iconClass = 'fas fa-exclamation-triangle';
            modalIconStyle = 'notification-icon icon-warning';
            break;
          case 'danger':
            iconClass = 'fas fa-exclamation-circle';
            modalIconStyle = 'notification-icon icon-danger';
            break;
        }
      }

      const modalIcon = document.getElementById('modalIcon');
      if (modalIcon) {
        modalIcon.className = modalIconStyle;
        const iconElement = modalIcon.querySelector('i');
        if (iconElement) {
          iconElement.className = iconClass;
        }
      }

      // Hiển thị modal
      const bsModal = new bootstrap.Modal(modal);
      bsModal.show();
    })
    .catch(error => {
      console.error('Error loading notification details:', error);
      alert('Không thể tải thông tin chi tiết thông báo. Vui lòng thử lại sau.');
    });
  }

  // Xử lý khi click nút đánh dấu đã đọc trong modal
  document.addEventListener('click', function(e) {
    if (e.target.id === 'markReadBtn' || e.target.closest('#markReadBtn')) {
      const button = e.target.id === 'markReadBtn' ? e.target : e.target.closest('#markReadBtn');
      const notificationId = button.dataset.id;

      fetch(`${API_BASE_URL}/${notificationId}/read`, {
        method: 'PUT'
      })
      .then(response => {
        if (response.ok) {
          // Ẩn nút sau khi đánh dấu đã đọc
          button.style.display = 'none';
        }
      })
      .catch(error => {
        console.error('Error marking notification as read:', error);
        alert('Đã xảy ra lỗi khi đánh dấu thông báo đã đọc. Vui lòng thử lại.');
      });
    }
  });

  // Xử lý khi click nút đánh dấu tất cả đã đọc
  document.addEventListener('click', function(e) {
    if (e.target.id === 'markAllRead' || e.target.closest('#markAllRead')) {
      fetch(`${API_BASE_URL}/user/${currentUser.username}/read-all`, {
        method: 'PUT'
      })
      .then(response => {
        if (response.ok) {
          console.log('Marked all notifications as read');
          // WebSocket sẽ xử lý cập nhật UI
        }
      })
      .catch(error => {
        console.error('Error marking all notifications as read:', error);
        // Vẫn cập nhật UI dù có lỗi
        markAllNotificationsAsReadInUI();
        updateNotificationCount();
      });
    }
  });

  // Xử lý khi submit form tạo thông báo mới
  const createNotificationForm = document.getElementById('createNotificationForm');
  if (createNotificationForm) {
    createNotificationForm.addEventListener('submit', function(e) {
      e.preventDefault();

      const title = document.getElementById('title').value;
      const content = document.getElementById('content').value;
      const receiver = document.getElementById('receiver').value;
      const type = document.getElementById('notificationType').value;

      if (!title || !content || !receiver) {
        alert('Vui lòng điền đầy đủ thông tin thông báo');
        return;
      }

      // Chuẩn bị dữ liệu phù hợp với NotificationDTO.Req
      const requestData = {
        title: title,
        content: content,
        sender: currentUser.username,
        receiver: receiver,
        type: type,
        isRead: false
      };

      fetch(API_BASE_URL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
      })
      .then(response => response.json())
      .then(data => {
        console.log('Notification created:', data);
        // Reset form
        createNotificationForm.reset();
        alert('Thông báo đã được tạo thành công');
      })
      .catch(error => {
        console.error('Error creating notification:', error);
        alert('Đã xảy ra lỗi khi tạo thông báo. Vui lòng thử lại.');
      });
    });
  }

  // Xử lý khi submit form tạo thông báo cho phòng ban
  const createDepartmentNotificationForm = document.getElementById('createDepartmentNotificationForm');
  if (createDepartmentNotificationForm) {
    createDepartmentNotificationForm.addEventListener('submit', function(e) {
      e.preventDefault();

      const title = document.getElementById('deptTitle').value;
      const content = document.getElementById('deptContent').value;
      const departmentId = document.getElementById('department').value;
      const type = document.getElementById('deptNotificationType').value;

      if (!title || !content || !departmentId) {
        alert('Vui lòng điền đầy đủ thông tin thông báo');
        return;
      }

      // Lấy danh sách người dùng của phòng ban (giả lập)
      fetchDepartmentUsers(departmentId)
      .then(users => {
        // Sử dụng BulkReq format
        const requestData = {
          title: title,
          content: content,
          sender: currentUser.username,
          receivers: users,
          type: type
        };

        return fetch(`${API_BASE_URL}/bulk`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(requestData)
        });
      })
      .then(response => response.json())
      .then(data => {
        console.log('Bulk notification created:', data);
        // Reset form
        createDepartmentNotificationForm.reset();
        alert('Thông báo đã được gửi cho phòng ban thành công');
      })
      .catch(error => {
        console.error('Error creating department notification:', error);
        alert('Đã xảy ra lỗi khi tạo thông báo cho phòng ban. Vui lòng thử lại.');
      });
    });
  }

  // Mô phỏng API lấy danh sách người dùng theo phòng ban
  function fetchDepartmentUsers(departmentId) {
    return new Promise((resolve, reject) => {
      // Trong thực tế, đây sẽ là một cuộc gọi API
      // Giả lập để test
      const mockUsers = {
        '1': ['user1', 'user2', 'user3'],
        '2': ['user4', 'user5'],
        '3': ['user6', 'user7', 'user8'],
        '4': ['user9', 'user10']
      };

      setTimeout(() => {
        resolve(mockUsers[departmentId] || []);
      }, 300);
    });
  }

  // Xử lý thay đổi bộ lọc thông báo
  const notificationFilter = document.getElementById('notificationFilter');
  if (notificationFilter) {
    notificationFilter.addEventListener('change', function() {
      const filter = this.value;
      const notificationsTable = document.getElementById('notificationsTable');
      if (!notificationsTable) return;

      const rows = notificationsTable.querySelectorAll('tr[data-id]');

      switch(filter) {
        case 'all':
          rows.forEach(row => {
            row.style.display = '';
          });
          break;
        case 'read':
          rows.forEach(row => {
            if (row.classList.contains('table-info')) {
              row.style.display = 'none';
            } else {
              row.style.display = '';
            }
          });
          break;
        case 'unread':
          rows.forEach(row => {
            if (row.classList.contains('table-info')) {
              row.style.display = '';
            } else {
              row.style.display = 'none';
            }
          });
          break;
      }
    });
  }

  // Xử lý dropdown thông báo
  const notificationDropdown = document.getElementById('notificationDropdown');
  if (notificationDropdown) {
    notificationDropdown.addEventListener('click', function(e) {
      e.stopPropagation();

      const dropdownMenu = this.nextElementSibling;
      if (dropdownMenu && dropdownMenu.classList.contains('dropdown-menu')) {
        // Toggle class 'show'
        if (dropdownMenu.classList.contains('show')) {
          dropdownMenu.classList.remove('show');
        } else {
          dropdownMenu.classList.add('show');
          // Tải thông báo khi mở dropdown
          loadNotifications();
        }
      }
    });

    // Đóng dropdown khi click bên ngoài
    document.addEventListener('click', function(e) {
      const dropdowns = document.querySelectorAll('.dropdown-menu.show');
      dropdowns.forEach(dropdown => {
        if (!dropdown.contains(e.target) && !notificationDropdown.contains(e.target)) {
          dropdown.classList.remove('show');
        }
      });
    });
  }

  // Tải dữ liệu ban đầu và kết nối WebSocket khi trang được tải
  loadNotifications();
  connectWebSocket();

  // Cập nhật số lượng thông báo định kỳ
  setInterval(updateNotificationCount, 60000); // Mỗi phút

  // Ngắt kết nối WebSocket khi rời trang
  window.addEventListener('beforeunload', disconnectWebSocket);
});