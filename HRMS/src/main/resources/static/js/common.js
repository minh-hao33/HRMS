/**
 * File JavaScript chung cho toàn ứng dụng
 * Chứa các hàm tiện ích và xử lý chung
 */

$(document).ready(function() {
    // Xử lý vấn đề modal-backdrop không biến mất
    fixModalBackdropIssue();

    // Đăng ký event handlers cho các tính năng chung
    setupGlobalEventHandlers();
});

/**
 * Xử lý vấn đề modal-backdrop không biến mất
 */
function fixModalBackdropIssue() {
    // Xử lý khi modal bị đóng
    $(document).on('hidden.bs.modal', '.modal', function() {
        // Kiểm tra không còn modal nào đang mở
        if (!$('.modal:visible').length) {
            // Xóa tất cả backdrop còn sót lại
            $('.modal-backdrop').remove();
            // Loại bỏ class 'modal-open' khỏi body
            $('body').removeClass('modal-open');
            // Loại bỏ style padding-right được thêm bởi Bootstrap
            $('body').css('padding-right', '');
        }
    });

    // Xóa backdrop nếu đã tồn tại khi tải trang
    if ($('.modal-backdrop').length > 0 && !$('.modal:visible').length) {
        $('.modal-backdrop').remove();
        $('body').removeClass('modal-open');
        $('body').css('padding-right', '');
    }

    // Fix cho trường hợp backdrop không click được
    $(document).on('click', '.modal-backdrop', function() {
        // Đóng modal hiện tại nếu nó có thể đóng bằng click bên ngoài
        $('.modal:visible').each(function() {
            if ($(this).data('bs.modal')._config.backdrop !== 'static') {
                $(this).modal('hide');
            }
        });
    });
}

/**
 * Đăng ký các event handlers toàn cục
 */
function setupGlobalEventHandlers() {
    // Xử lý phím ESC để đóng tất cả modals và xóa backdrops
    $(document).on('keydown', function(e) {
        if (e.key === 'Escape') {
            // Đóng tất cả modal có thể đóng bằng ESC
            $('.modal:visible').each(function() {
                if ($(this).data('bs.modal') && 
                    $(this).data('bs.modal')._config.keyboard !== false) {
                    $(this).modal('hide');
                }
            });
            
            // Dọn dẹp backdrop nếu còn sót
            if (!$('.modal:visible').length) {
                $('.modal-backdrop').remove();
                $('body').removeClass('modal-open');
                $('body').css('padding-right', '');
            }
        }
    });
}

/**
 * Mở modal với xử lý đặc biệt để tránh lỗi backdrop
 * @param {string} modalId - ID của modal cần mở
 */
function safeShowModal(modalId) {
    // Dọn dẹp backdrops trước khi mở modal mới
    if (!$('.modal:visible').length) {
        $('.modal-backdrop').remove();
        $('body').removeClass('modal-open');
        $('body').css('padding-right', '');
    }
    
    // Mở modal
    $('#' + modalId).modal('show');
}

/**
 * Đóng modal an toàn
 * @param {string} modalId - ID của modal cần đóng
 */
function safeHideModal(modalId) {
    $('#' + modalId).modal('hide');
    
    // Đảm bảo backdrop được xóa
    setTimeout(function() {
        if (!$('.modal:visible').length) {
            $('.modal-backdrop').remove();
            $('body').removeClass('modal-open');
            $('body').css('padding-right', '');
        }
    }, 300); // Đợi animation hoàn tất
} 