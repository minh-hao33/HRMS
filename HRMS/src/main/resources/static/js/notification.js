$(document).ready(function() {
// Thông tin người dùng hiện tại từ localStorage
  let currentUser = {
    username: localStorage.getItem('Username'),
    employeeName: localStorage.getItem('EmployeeName') || 'EMPLOYEE',
    avatar: localStorage.getItem('Avatar') || (localStorage.getItem('Username') ? localStorage.getItem('Username').charAt(0).toUpperCase() : 'A'),
    role: localStorage.getItem('Role') || 'user'
  };
  
  // Kiểm tra nếu username chưa được thiết lập
  if (!currentUser.username) {
    // Thử lấy từ cookie nếu có
    currentUser.username = getCookie('Username') || 'anonymous';
    console.warn('Username not found in localStorage, using:', currentUser.username);
  }
  
  // Hàm để lấy giá trị cookie
  function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
  }
  
  // Log thông tin người dùng hiện tại để debug
  console.log('Current user loaded:', currentUser);

  // Cập nhật thông tin người dùng hiện tại trên UI
  $('#currentUser').text(currentUser.username);
  $('.user-avatar').text(currentUser.avatar);

  // API Base URL
  const API_BASE_URL = '/api/v1/notifications';

  // WebSocket connection
  let stompClient = null;
  let connected = false;

  // Thêm biến cho phân trang
  let paginationSettings = {
    page: 0,
    size: 20,
    totalItems: 0,
    totalPages: 0,
    hasMoreItems: false
  };

  // Hàm kết nối WebSocket
  function connectWebSocket() {
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

  // Sửa lại hàm xử lý WebSocket để không gọi API quá nhiều
  function onNotificationReceived(payload) {
    const message = JSON.parse(payload.body);
    console.log('Received WebSocket message:', message);

    switch(message.action) {
      case 'NEW_NOTIFICATION':
        // Thêm thông báo mới vào UI trực tiếp, không cần load lại
        handleNewNotification(message.data);
        break;

      case 'MARK_READ':
        // Cập nhật UI trực tiếp, không gọi API
        markNotificationAsReadInUI(message.data);
        break;

      case 'MARK_ALL_READ':
        // Cập nhật UI trực tiếp, không gọi API
        markAllNotificationsAsReadInUI();
        break;

      case 'DELETE_NOTIFICATION':
        // Xóa khỏi UI trực tiếp, không gọi API
        removeNotificationFromUI(message.data);
        break;
    }

    // Chỉ cập nhật số lượng thông báo, không load lại tất cả
    updateNotificationCountFromUI();
  }

  // Hàm mới để cập nhật số lượng từ UI mà không gọi API
  function updateNotificationCountFromUI() {
    const unreadCount = $('.notification-item.unread').length;
    $('#notificationCount').text(unreadCount);
    $('#notificationCount').toggle(unreadCount > 0);
  }

  // Xử lý khi nhận thông báo mới
  function handleNewNotification(notification) {
    // Hiệu ứng rung chuông
    shakeNotificationBell();

    // Thêm thông báo mới vào đầu danh sách dropdown
    prependNotificationToDropdown(notification);

    // Thêm thông báo mới vào bảng nếu đang hiển thị
    prependNotificationToTable(notification);

    // Hiển thị thông báo toast nếu muốn
    showNotificationToast(notification);

    // Phát âm thanh thông báo
    playNotificationSound();
  }

  // Hiệu ứng rung chuông thông báo
  function shakeNotificationBell() {
    $('.notification-bell').addClass('shake');

    // Xóa class shake sau khi animation hoàn thành
    setTimeout(function() {
      $('.notification-bell').removeClass('shake');
    }, 1000);
  }

  // Thêm thông báo mới vào đầu dropdown
  function prependNotificationToDropdown(notification) {
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
    if ($('#notificationList .empty-notification').length) {
      $('#notificationList').empty();
    }

    $('#notificationList').prepend(notificationItem);

    // Sau 2 giây, xóa class "new" để kết thúc hiệu ứng highlight
    setTimeout(function() {
      $(`#notificationList .notification-item[data-id="${notification.id}"]`).removeClass('new');
    }, 2000);
  }

  // Thêm thông báo mới vào bảng
  function prependNotificationToTable(notification) {
    const rowClass = notification.read ? '' : 'table-info';
    const statusBadge = notification.read
        ? '<span class="badge rounded-pill status-badge read">Đã đọc</span>'
        : '<span class="badge rounded-pill status-badge unread">Chưa đọc</span>';

    // Rút gọn nội dung nếu quá dài
    const truncatedContent = notification.content.length > 50
        ? notification.content.substring(0, 50) + '...'
        : notification.content;

    const row = `
            <tr data-id="${notification.id}" class="${rowClass}">
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
            </tr>
        `;

    // Nếu không có hàng nào hoặc có thông báo "không có thông báo", xóa và thêm dữ liệu mới
    if ($('#notificationsTable tr').length === 0 || $('#notificationsTable tr td[colspan]').length) {
      $('#notificationsTable').empty();
    }

    $('#notificationsTable').prepend(row);
  }

  // Hiển thị toast khi có thông báo mới
  function showNotificationToast(notification) {
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

    // Thêm toast vào container và hiển thị
    $('#toastContainer').append(toast);

    // Khởi tạo toast bootstrap
    const toastElement = document.getElementById(toastId);
    const toastInstance = new bootstrap.Toast(toastElement);
    toastInstance.show();

    // Xóa toast sau khi bị ẩn
    $(toastElement).on('hidden.bs.toast', function() {
      $(this).remove();
    });
  }
  $(document).on('click', '.notification-item', function(e) {
    e.preventDefault(); // Ngăn chặn chuyển hướng
    e.stopPropagation(); // Ngăn sự kiện lan ra ngoài
    
    const notificationId = $(this).data('id');
    
    if (!notificationId) {
      console.error('Missing notification ID');
      return;
    }
  
    // Đánh dấu thông báo đã đọc nếu chưa đọc
    if ($(this).hasClass('unread')) {
      $.ajax({
        url: `${API_BASE_URL}/${notificationId}/read`,
        method: 'PUT',
        success: function() {
          // Cập nhật UI
          markNotificationAsReadInUI(notificationId);
          updateNotificationCount();
        },
        error: function(error) {
          console.error('Error marking notification as read:', error);
          // Vẫn cập nhật UI
          markNotificationAsReadInUI(notificationId);
          updateNotificationCount();
        }
      });
    }
  
    // Hiển thị chi tiết thông báo trong modal
    openNotificationDetail(notificationId);
  });

  $('#viewAllNotifications').on('click', function(e) {
    e.preventDefault(); // Ngăn chặn điều hướng mặc định
    
    // Đảm bảo người dùng ở lại trang hiện tại và chỉ xem toàn bộ danh sách
    // Có thể thêm logic để hiển thị tất cả hoặc làm nổi bật tab thông báo
    
    // Nếu bạn muốn tải lại danh sách thông báo
    loadNotifications();
    
    // Đóng dropdown nếu đang mở
    $('.dropdown-menu.notification-dropdown').removeClass('show');
  });

  // Phát âm thanh thông báo
  function playNotificationSound() {
    const audio = document.getElementById('notificationSound');

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
    const notificationItem = $(`.notification-item[data-id="${notificationId}"]`);
    notificationItem.removeClass('unread');

    // Cập nhật trong bảng
    const row = $(`#notificationsTable tr[data-id="${notificationId}"]`);
    row.removeClass('table-info');
    row.find('td:nth-child(6)').html('<span class="badge rounded-pill status-badge read">Đã đọc</span>');
  }

  // Đánh dấu tất cả thông báo đã đọc trong giao diện
  function markAllNotificationsAsReadInUI() {
    // Cập nhật trong dropdown
    $('.notification-item').removeClass('unread');

    // Cập nhật trong bảng
    $('#notificationsTable tr').removeClass('table-info');
    $('#notificationsTable tr td:nth-child(6)').html('<span class="badge rounded-pill status-badge read">Đã đọc</span>');
  }

  // Xóa thông báo khỏi giao diện
  function removeNotificationFromUI(notificationId) {
    // Xóa khỏi dropdown
    $(`.notification-item[data-id="${notificationId}"]`).remove();

    // Xóa khỏi bảng
    $(`#notificationsTable tr[data-id="${notificationId}"]`).remove();

    // Kiểm tra nếu không còn thông báo nào, hiển thị thông báo "không có thông báo"
    if ($('#notificationList .notification-item').length === 0) {
      $('#notificationList').html(`
                <div class="empty-notification">
                    <i class="fas fa-bell-slash"></i>
                    <p class="empty-text">Không có thông báo nào</p>
                </div>
            `);
    }

    if ($('#notificationsTable tr').length === 0) {
      $('#notificationsTable').html(`
                <tr>
                    <td colspan="7" class="text-center py-4">
                        <i class="fas fa-bell-slash mb-2" style="font-size: 24px; color: #ccc;"></i>
                        <p class="mb-0">Không có thông báo nào</p>
                    </td>
                </tr>
            `);
    }
  }

  // Thêm biến để theo dõi request đang chạy
  let notificationRequestInProgress = false;

  function loadNotifications(page = 0) {
    // Kiểm tra nếu đã có request đang chạy
    if (notificationRequestInProgress) {
      console.log('Notification request already in progress, skipping');
      return;
    }
    
    notificationRequestInProgress = true;

    // Kiểm tra username trước khi gọi API
    if (!currentUser.username || currentUser.username === 'anonymous') {
      console.error('Cannot load notifications: Invalid username');
      loadSampleNotifications();
      notificationRequestInProgress = false;
      return;
    }

    console.log('Loading notifications for user:', currentUser.username, 'page:', page);
    
    // Lưu trang hiện tại vào settings
    paginationSettings.page = page;
    
    // Thêm tham số phân trang vào URL
    const apiUrl = `${API_BASE_URL}/user/${currentUser.username}?page=${page}&size=${paginationSettings.size}`;
    
    $.ajax({
      url: apiUrl,
      method: 'GET',
      success: function(response) {
        console.log('API response:', response);
        
        let notifications = [];
        let isFirstLoad = page === 0;
        
        try {
          // Xử lý dữ liệu phân trang nếu có
          if (response && response.data) {
            // Kiểm tra xem response có chứa thông tin phân trang không
            if (response.data.content && Array.isArray(response.data.content)) {
              // Đây là response có phân trang
              notifications = response.data.content;
              paginationSettings.totalItems = response.data.totalElements || 0;
              paginationSettings.totalPages = response.data.totalPages || 1;
              paginationSettings.hasMoreItems = page < (paginationSettings.totalPages - 1);
            } 
            // Format không phân trang
            else if (Array.isArray(response.data)) {
              notifications = response.data;
            } else if (typeof response.data === 'object') {
              notifications = [response.data]; // Nếu là object đơn
            }
          } 
          // Trường hợp API trả về mảng trực tiếp
          else if (Array.isArray(response)) {
            notifications = response;
          }
          // Định dạng cũ hơn - nếu có
          else if (response && response.retData) {
            if (Array.isArray(response.retData)) {
              notifications = response.retData;
            } else if (typeof response.retData === 'object') {
              notifications = [response.retData];
            }
          } else {
            console.warn('Unexpected API response format:', response);
            notifications = [];
          }
        } catch (error) {
          console.error('Error parsing notification response:', error);
          notifications = [];
        }
        
        // Debug log số lượng thông báo
        console.log(`Loaded ${notifications.length} notifications (page ${page}/${paginationSettings.totalPages})`);
        
        // Đảm bảo mỗi thông báo có timeAgo
        notifications.forEach(notification => {
          if (!notification.timeAgo && notification.createdAt) {
            notification.timeAgo = formatTimeAgo(new Date(notification.createdAt));
          }
        });
        
        // Nếu là trang đầu tiên, thay thế toàn bộ dữ liệu
        // Nếu không, thêm vào dữ liệu hiện có
        if (isFirstLoad) {
          renderNotifications(notifications);
          renderNotificationsTable(notifications);
        } else {
          appendNotifications(notifications);
          appendNotificationsTable(notifications);
        }
        
        updateNotificationCount();
        
        // Hiển thị hoặc ẩn nút "Xem thêm" dựa trên có dữ liệu tiếp theo không
        $('#loadMoreButton').toggle(paginationSettings.hasMoreItems);
      },
      error: function(error) {
        console.error('Error loading notifications:', error);
        if (page === 0) {
          loadSampleNotifications();
        }
      },
      complete: function() {
        // Đánh dấu là đã hoàn thành request
        notificationRequestInProgress = false;
      }
    });
  }

  // Hàm mới để thêm thông báo vào danh sách hiện có
  function appendNotifications(notifications) {
    if (!notifications || notifications.length === 0) {
      return;
    }

    const $notificationList = $('#notificationList');
    
    // Xóa thông báo "Không có thông báo nào" nếu có
    $notificationList.find('.empty-notification').remove();

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

      $notificationList.append(`
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
      `);
    });
  }

  // Hàm mới để thêm thông báo vào bảng hiện có
  function appendNotificationsTable(notifications) {
    if (!notifications || notifications.length === 0) {
      return;
    }

    const $notificationsTable = $('#notificationsTable');
    
    // Xóa thông báo "Không có thông báo nào" nếu có
    $notificationsTable.find('tr td[colspan]').parent().remove();

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

      $notificationsTable.append(`
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
      `);
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

  // Tải dữ liệu thông báo mẫu nếu API thất bại
  function loadSampleNotifications() {
    const sampleNotifications = [
      {
        id: 1,
        title: 'Phê duyệt đơn nghỉ phép',
        content: 'Đơn xin nghỉ phép của bạn đã được phê duyệt từ ngày 25/04 đến 27/04',
        sender: 'admin',
        senderName: 'Admin',
        receiver: currentUser.username,
        createdAt: new Date(new Date().getTime() - 20 * 60000), // 20 phút trước
        read: false,
        timeAgo: '20 phút trước',
        type: 'success'
      },
      {
        id: 2,
        title: 'Lịch họp phòng ban',
        content: 'Cuộc họp phòng ban sẽ diễn ra vào 14:00 ngày 22/04 tại phòng họp A',
        sender: 'admin',
        senderName: 'Admin',
        receiver: currentUser.username,
        createdAt: new Date(new Date().getTime() - 2 * 3600000), // 2 giờ trước
        read: false,
        timeAgo: '2 giờ trước',
        type: 'info'
      },
      {
        id: 3,
        title: 'Thông báo cập nhật hệ thống',
        content: 'Hệ thống sẽ bảo trì vào tối nay từ 22:00-23:00',
        sender: 'admin',
        senderName: 'Admin',
        receiver: currentUser.username,
        createdAt: new Date(new Date().getTime() - 24 * 3600000), // 1 ngày trước
        read: true,
        timeAgo: '1 ngày trước',
        type: 'warning'
      }
    ];

    renderNotifications(sampleNotifications);
    renderNotificationsTable(sampleNotifications);

    // Cập nhật số lượng thông báo chưa đọc
    const unreadCount = sampleNotifications.filter(n => !n.read).length;
    $('#notificationCount').text(unreadCount);

    if (unreadCount > 0) {
      $('#notificationCount').show();
    } else {
      $('#notificationCount').hide();
    }
  }

  let lastNotificationUpdate = 0;
  const UPDATE_INTERVAL = 30000; // 30 giây

  // Cập nhật số lượng thông báo chưa đọc
  function updateNotificationCount() {
    // Kiểm tra xem đã đủ thời gian để cập nhật chưa
    const now = Date.now();
    if (now - lastNotificationUpdate < UPDATE_INTERVAL) {
      return; // Chưa đến thời gian cập nhật, bỏ qua
    }
    
    lastNotificationUpdate = now;

    // Kiểm tra username trước khi gọi API
    if (!currentUser.username || currentUser.username === 'anonymous') {
      console.error('Cannot update notification count: Invalid username');
      // Đếm từ UI
      const unreadCount = $('.notification-item.unread').length;
      $('#notificationCount').text(unreadCount);
      $('#notificationCount').toggle(unreadCount > 0);
      return;
    }

    $.ajax({
      url: `${API_BASE_URL}/user/${currentUser.username}/count`,
      method: 'GET',
      success: function(response) {
        console.log('Count response:', response);
        
        // Chuẩn hóa cách xử lý response
        let count = 0;
        
        try {
          if (response && response.data && response.data.count !== undefined) {
            count = parseInt(response.data.count);
          } else if (response && response.count !== undefined) {
            count = parseInt(response.count);
          } else if (typeof response === 'number') {
            count = response;
          } else if (response && typeof response === 'object') {
            // Thử tìm bất kỳ thuộc tính nào có thể là count
            const possibleCountProps = ['count', 'unreadCount', 'notificationCount'];
            for (const prop of possibleCountProps) {
              if (response[prop] !== undefined) {
                count = parseInt(response[prop]);
                break;
              }
            }
          }
        } catch (error) {
          console.error('Error parsing count response:', error);
        }
        
        // Cập nhật UI
        $('#notificationCount').text(count);
        $('#notificationCount').toggle(count > 0);
        
        // Debug: So sánh với số lượng thực sự hiển thị trên UI
        const uiCount = $('.notification-item.unread').length;
        console.log(`API count: ${count}, UI count: ${uiCount}`);
        if (count !== uiCount) {
          console.warn('Count mismatch between API and UI!');
          // Làm mới danh sách thông báo nếu sự khác biệt quá lớn
          if (Math.abs(count - uiCount) > 2) {
            console.log('Significant count difference, reloading notifications');
            loadNotifications();
          }
        }
      },
      error: function(error) {
        console.error('Error updating notification count:', error);
        // Đếm từ UI
        const unreadCount = $('.notification-item.unread').length;
        $('#notificationCount').text(unreadCount);
        $('#notificationCount').toggle(unreadCount > 0);
      }
    });
  }

  // Hiển thị danh sách thông báo trong dropdown
  function renderNotifications(notifications) {
    const $notificationList = $('#notificationList');
    $notificationList.empty();

    if (!notifications || notifications.length === 0) {
      $notificationList.append(`
                <div class="empty-notification">
                    <i class="fas fa-bell-slash"></i>
                    <p class="empty-text">Không có thông báo nào</p>
                </div>
            `);
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

      $notificationList.append(`
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
            `);
    });
  }

  // Hiển thị danh sách thông báo trong bảng
  function renderNotificationsTable(notifications) {
    const $notificationsTable = $('#notificationsTable');
    $notificationsTable.empty();

    if (!notifications || notifications.length === 0) {
      $notificationsTable.append(`
                <tr>
                    <td colspan="7" class="text-center py-4">
                        <i class="fas fa-bell-slash mb-2" style="font-size: 24px; color: #ccc;"></i>
                        <p class="mb-0">Không có thông báo nào</p>
                    </td>
                </tr>
            `);
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

      $notificationsTable.append(`
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
            `);
    });
  }

  // Xử lý khi click vào thông báo trong dropdown
  $(document).on('click', '.notification-item', function() {
    const notificationId = $(this).data('id');

    // Đánh dấu thông báo đã đọc nếu chưa đọc
    if ($(this).hasClass('unread')) {
      $.ajax({
        url: `${API_BASE_URL}/${notificationId}/read`,
        method: 'PUT',
        success: function() {
          // Không cần tải lại vì sẽ nhận cập nhật qua WebSocket
        },
        error: function(error) {
          console.error('Error marking notification as read:', error);
          // Vẫn cập nhật UI dù có lỗi
          markNotificationAsReadInUI(notificationId);
        }
      });
    }

    // Hiển thị chi tiết thông báo
    openNotificationDetail(notificationId);
  });

  // Xử lý khi click vào nút xem chi tiết trong bảng
  $(document).on('click', '.view-notification', function(e) {
    e.preventDefault();
    const notificationId = $(this).data('id');
    openNotificationDetail(notificationId);
  });

  // Xử lý khi click vào nút xóa thông báo
  $(document).on('click', '.delete-notification', function(e) {
    e.preventDefault();
    e.stopPropagation();
    const notificationId = $(this).data('id');

    if (confirm('Bạn có chắc chắn muốn xóa thông báo này?')) {
      $.ajax({
        url: `${API_BASE_URL}/${notificationId}`,
        method: 'DELETE',
        success: function() {
          // Không cần xóa khỏi UI vì sẽ nhận cập nhật qua WebSocket
        },
        error: function(error) {
          console.error('Error deleting notification:', error);
          // Vẫn xóa khỏi UI dù có lỗi
          removeNotificationFromUI(notificationId);
        }
      });
    }
  });

  // Hiển thị chi tiết thông báo trong modal
  function openNotificationDetail(notificationId) {
    $.ajax({
      url: `${API_BASE_URL}/${notificationId}`,
      method: 'GET',
      success: function(response) {
        console.log('Notification detail response:', response); // Thêm log này

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
          alert('Không thể tải thông tin chi tiết thông báo. Cấu trúc dữ liệu không hợp lệ.');
          return;
        }

        // Ensure timeAgo is set
        if (!notification.timeAgo && notification.createdAt) {
          notification.timeAgo = formatTimeAgo(new Date(notification.createdAt));
        }

        // Cập nhật modal với dữ liệu thông báo
        $('#modalNotificationTitle').text(notification.title);
        $('#modalNotificationContent').text(notification.content);
        $('#modalSender').text(notification.senderName || notification.sender);
        $('#modalTime').text(notification.timeAgo || 'N/A');

        // Ẩn nút đánh dấu đã đọc nếu thông báo đã đọc
        if (notification.read) {
          $('#markReadBtn').hide();
        } else {
          $('#markReadBtn').show();
          $('#markReadBtn').data('id', notification.id);
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

        $('#modalIcon').attr('class', modalIconStyle);
        $('#modalIcon i').attr('class', iconClass);

        // Hiển thị modal
        const modal = new bootstrap.Modal(document.getElementById('notificationDetailModal'));
        modal.show();
      },
      error: function(error) {
        console.error('Error loading notification details:', error);
        alert('Không thể tải thông tin chi tiết thông báo. Vui lòng thử lại sau.');
      }
    });
  }

  // Xử lý khi click nút đánh dấu đã đọc trong modal
  $('#markReadBtn').on('click', function() {
    const notificationId = $(this).data('id');

    $.ajax({
      url: `${API_BASE_URL}/${notificationId}/read`,
      method: 'PUT',
      success: function() {
        // Ẩn nút sau khi đánh dấu đã đọc
        $('#markReadBtn').hide();
      },
      error: function(error) {
        console.error('Error marking notification as read:', error);
        alert('Đã xảy ra lỗi khi đánh dấu thông báo đã đọc. Vui lòng thử lại.');
      }
    });
  });

  // Xử lý khi click nút đánh dấu tất cả đã đọc
  $('#markAllRead').on('click', function() {
    $.ajax({
      url: `${API_BASE_URL}/user/${currentUser.username}/read-all`,
      method: 'PUT',
      success: function() {
        // Không cần cập nhật UI vì sẽ nhận cập nhật qua WebSocket
      },
      error: function(error) {
        console.error('Error marking all notifications as read:', error);
        // Vẫn cập nhật UI dù có lỗi
        markAllNotificationsAsReadInUI();
        updateNotificationCount();
      }
    });
  });

  // Xử lý khi submit form tạo thông báo mới
  $('#createNotificationForm').on('submit', function(e) {
    e.preventDefault();

    const title = $('#title').val();
    const content = $('#content').val();
    const receiver = $('#receiver').val();
    const type = $('#notificationType').val();

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

    $.ajax({
      url: API_BASE_URL,
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(requestData),
      success: function(response) {
        console.log('Notification created:', response);
        // Reset form
        $('#createNotificationForm')[0].reset();
        alert('Thông báo đã được tạo thành công');
      },
      error: function(error) {
        console.error('Error creating notification:', error);
        alert('Đã xảy ra lỗi khi tạo thông báo. Vui lòng thử lại.');
      }
    });
  });

  // Xử lý khi submit form tạo thông báo cho phòng ban
  $('#createDepartmentNotificationForm').on('submit', function(e) {
    e.preventDefault();

    const title = $('#deptTitle').val();
    const content = $('#deptContent').val();
    const departmentId = $('#department').val();
    const type = $('#deptNotificationType').val();

    if (!title || !content || !departmentId) {
      alert('Vui lòng điền đầy đủ thông tin thông báo');
      return;
    }

    // Mock data - in real implementation, you would fetch users by department
    const mockEmployees = ['user1', 'user2', 'user3'];

    // Use BulkReq format
    const requestData = {
      title: title,
      content: content,
      sender: currentUser.username,
      receivers: mockEmployees,
      type: type
    };

    $.ajax({
      url: `${API_BASE_URL}/bulk`,
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(requestData),
      success: function(response) {
        console.log('Bulk notification created:', response);
        // Reset form
        $('#createDepartmentNotificationForm')[0].reset();
        alert('Thông báo đã được gửi cho phòng ban thành công');
      },
      error: function(error) {
        console.error('Error creating department notification:', error);
        alert('Đã xảy ra lỗi khi tạo thông báo cho phòng ban. Vui lòng thử lại.');
      }
    });
  });

  // Xử lý thay đổi bộ lọc thông báo
  $('#notificationFilter').on('change', function() {
    const filter = $(this).val();

    switch(filter) {
      case 'all':
        $('#notificationsTable tr').show();
        break;
      case 'read':
        $('#notificationsTable tr').hide();
        $('#notificationsTable tr:not(.table-info)').show();
        break;
      case 'unread':
        $('#notificationsTable tr').hide();
        $('#notificationsTable tr.table-info').show();
        break;
    }
  });

  // Thêm nút "Xem thêm" vào HTML sau phần hiển thị thông báo
  $(document).ready(function() {
    // ... existing code ...
    
    // Thêm nút "Xem thêm" vào cuối dropdown và bảng thông báo
    $('#notificationList').after('<div id="loadMoreDropdown" class="text-center mt-2 mb-2"><button id="loadMoreNotifications" class="btn btn-sm btn-outline-primary">Xem thêm</button></div>');
    
    $('#notificationsTable').after('<div id="loadMoreTable" class="text-center mt-3 mb-3"><button id="loadMoreButton" class="btn btn-primary">Tải thêm thông báo</button></div>');
    
    // Xử lý sự kiện khi click vào nút "Xem thêm" trong dropdown
    $(document).on('click', '#loadMoreNotifications', function(e) {
      e.preventDefault();
      e.stopPropagation();
      
      // Tải trang tiếp theo
      loadNotifications(paginationSettings.page + 1);
    });
    
    // Xử lý sự kiện khi click vào nút "Tải thêm thông báo" trong bảng
    $(document).on('click', '#loadMoreButton', function() {
      // Tải trang tiếp theo
      loadNotifications(paginationSettings.page + 1);
    });

    // Khởi tạo với trang đầu tiên
    loadNotifications(0);
    connectWebSocket();

    // Ngắt kết nối WebSocket khi rời trang
    $(window).on('beforeunload', disconnectWebSocket);
  });

  // Cập nhật hàm khởi tạo cho file notification.html
  function initNotificationPage() {
    // Nếu đang ở trang thông báo, tải toàn bộ dữ liệu (trang đầu tiên)
    loadNotifications(0);
    
    // Hiển thị/ẩn nút "Xem thêm" dựa vào dữ liệu
    $('#loadMoreButton').toggle(paginationSettings.hasMoreItems);
  }

  // Xử lý vấn đề modal-backdrop không biến mất
  $(document).ready(function() {
    // Xử lý khi modal bị đóng
    $('.modal').on('hidden.bs.modal', function () {
      // Xóa tất cả backdrop còn sót lại
      $('.modal-backdrop').remove();
      // Loại bỏ class 'modal-open' khỏi body
      $('body').removeClass('modal-open');
      // Loại bỏ style padding-right được thêm bởi Bootstrap
      $('body').css('padding-right', '');
    });

    // Xử lý khi có lỗi modal
    $(document).on('click', '.modal-backdrop', function() {
      // Đóng tất cả các modal đang mở
      $('.modal').modal('hide');
      // Xóa tất cả backdrop
      $('.modal-backdrop').remove();
      // Loại bỏ class 'modal-open'
      $('body').removeClass('modal-open');
      // Loại bỏ style padding-right
      $('body').css('padding-right', '');
    });

    // Xóa backdrop nếu đã tồn tại khi tải trang
    if ($('.modal-backdrop').length > 0) {
      $('.modal-backdrop').remove();
      $('body').removeClass('modal-open');
      $('body').css('padding-right', '');
    }
  });
});