document.addEventListener("DOMContentLoaded", () => {
    const section = document.querySelector("section"),
        overlay = document.querySelector(".overlay"),
        modalTitle = document.getElementById("modal-title"),
        modalMessage = document.getElementById("modal-message"),
        closeBtn = document.querySelector(".close-btn");

    function showNotification(message) {
        modalTitle.textContent = "Thông báo";
        modalMessage.textContent = message;
        section.classList.add("active");
    }

    // Kiểm tra và gán sự kiện cho overlay
    if (overlay) {
        overlay.addEventListener("click", () => section.classList.remove("active"));
    } else {
        console.warn("Không tìm thấy phần tử overlay.");
    }

    // Kiểm tra và gán sự kiện cho closeBtn
    if (closeBtn) {
        closeBtn.addEventListener("click", () => section.classList.remove("active"));
    } else {
        console.warn("Không tìm thấy phần tử closeBtn.");
    }

    let timeout;

    function resetTimer() {
        clearTimeout(timeout);
        const expireTime = Date.now() + 30 * 60 * 1000; // 30 phút
        localStorage.setItem('expireTime', expireTime);

        timeout = setTimeout(() => {
            showNotification("Phiên làm việc đã hết hạn. Bạn sẽ được chuyển đến trang đăng nhập.");
            setTimeout(() => {
                window.location.href = "/users/login"; // Đường dẫn logout
            }, 5000); // 5 giây trước khi chuyển hướng
        }, 30 * 60 * 1000); // 30 phút
    }

    function checkTimeout() {
        const expireTime = localStorage.getItem('expireTime');
        if (expireTime && Date.now() > expireTime) {
            showNotification("Phiên làm việc đã hết hạn. Bạn sẽ được chuyển đến trang đăng nhập.");
            setTimeout(() => {
                window.location.href = "/users/login"; // Đường dẫn logout
            }, 5000); // 5 giây trước khi chuyển hướng
        } else {
            resetTimer();
        }
    }

    window.onload = checkTimeout;
    document.onmousemove = resetTimer;
    document.onkeydown = resetTimer;
});