CREATE DATABASE HRMS;

USE HRMS;

-- 1. TẠO CÁC BẢNG DỮ LIỆU
-- ==============================================
-- Bảng Quản lý Vai Trò (Roles)
CREATE TABLE Roles (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(64) NOT NULL UNIQUE,
    INDEX (role_name)
);

-- Bảng Quản lý Phòng Ban (Departments)
CREATE TABLE Departments (
    department_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(128) NOT NULL UNIQUE,
    status VARCHAR(10) NOT NULL DEFAULT 'Active',
    INDEX (department_name)
);


-- Bảng Quản lý Người Dùng (Users)
CREATE TABLE Users (
    username VARCHAR(64) PRIMARY KEY,
    password VARCHAR(256) NOT NULL, 
    employee_name VARCHAR(128) NOT NULL,
    email VARCHAR(128) NOT NULL UNIQUE,
    department_id BIGINT,
    role_name VARCHAR(64) NOT NULL,
    is_supervisor BOOLEAN DEFAULT FALSE,
    status VARCHAR(64) NOT NULL,
    FOREIGN KEY (department_id) REFERENCES Departments(department_id),
    FOREIGN KEY (role_name) REFERENCES Roles(role_name),
    UNIQUE (username),
    INDEX (department_id),
    INDEX (role_name),
    INDEX (status)
);

-- Bảng Yêu Cầu (Requests)
CREATE TABLE Requests (
    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    department_id BIGINT NOT NULL,
    request_type VARCHAR(64) NOT NULL,
    request_reason TEXT DEFAULT NULL, -- KHÔNG BẮT BUỘC NẾU ĐƯỢC DUYỆT
    request_status VARCHAR(64) NOT NULL,
    approver_username VARCHAR(64) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    approved_at TIMESTAMP NULL DEFAULT NULL, -- Chi cap nhat khi duoc duyet
    rejection_reason TEXT DEFAULT NULL, -- Ly do rejected
    FOREIGN KEY (username) REFERENCES Users(username),
    FOREIGN KEY (approver_username) REFERENCES Users(username),
    INDEX (username),
    INDEX (request_status),
    INDEX (start_time),
    INDEX (end_time),
    INDEX (approver_username)
);

-- Bảng Quản lý Phòng Họp (Meeting_Rooms)
CREATE TABLE Meeting_Rooms (
    room_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_name VARCHAR(128) NOT NULL,
    location VARCHAR(256) NOT NULL,
    capacity INT NOT NULL,
    INDEX (room_name),
    INDEX (location)
);

-- Bảng Đặt Phòng Họp (Bookings)
CREATE TABLE Bookings (
    booking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    room_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    attendees VARCHAR(255) NOT NULL,
    content VARCHAR(255) NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status VARCHAR(64) DEFAULT 'Confirmed',
    booking_type VARCHAR(32) NOT NULL, -- Loại đặt lịch
    weekdays VARCHAR(32) NULL, -- Chứa danh sách thứ nếu là 'WEEKLY' (ví dụ: 'Mo,We,Fr')
    FOREIGN KEY (username) REFERENCES Users(username),
    FOREIGN KEY (room_id) REFERENCES Meeting_Rooms(room_id),
    INDEX (username),
    INDEX (room_id),
    INDEX (start_time),
    INDEX (end_time),
    INDEX (status),
    INDEX (booking_type),
    INDEX (weekdays),
    -- Ràng buộc logic
    CHECK (start_time < end_time), -- Ngày hợp lệ
    CHECK (
        (booking_type = 'WEEKLY' AND weekdays IS NOT NULL) OR
        (booking_type IN ('ONLY', 'DAILY') AND weekdays IS NULL)
    ) -- Nếu là weekly thì weekdays phải có giá trị, ngược lại phải là NULL
);

-- Bảng Thông Báo (Notifications)
CREATE TABLE Notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    sender VARCHAR(64) NOT NULL,
    receiver VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    INDEX (sender),
    INDEX (receiver),
    INDEX (created_at),
    INDEX (is_read)
);

-- 2. CHÈN DỮ LIỆU MẪU
-- ==============================================
-- Chèn dữ liệu vào bảng Roles
INSERT INTO Roles (role_name) VALUES
('EMPLOYEE'),
('SUPERVISOR'),
('ADMIN');

-- Chèn dữ liệu vào bảng Departments
INSERT INTO Departments (department_name) VALUES
('HR'),
('Finance'),
('IT');

-- Chèn dữ liệu vào bảng Users với mật khẩu đã mã hóa và tên nhân viên
INSERT INTO Users (username, password, email, role_name, department_id, is_supervisor, status, employee_name) VALUES
('ntdu', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'ntdu@cmcglobal.vn', 'EMPLOYEE', 1, FALSE, 'Active', 'Trung Du Nguyen'),
('pmhao', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'pmhao@cmcglobal.vn', 'SUPERVISOR', 1, TRUE, 'Active', 'Minh Hao Pham'),
('bkkhanh', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'bkkhanh@cmcglobal.vn', 'ADMIN', 2, TRUE, 'Active', 'Khac Khanh Bui'),
('pnminh', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'pnminh@cmcglobal.vn', 'EMPLOYEE', 3, FALSE, 'Inactive', 'Nhat Minh Pham'),
('nhtien1', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'nhtien1@cmcglobal.vn', 'SUPERVISOR', 2, FALSE, 'Active', 'Huu Tien Pham');

-- Chèn dữ liệu vào bảng Requests
INSERT INTO Requests (username, department_id, request_type, request_reason, request_status, approver_username, start_time, end_time, rejection_reason) VALUES
('ntdu', 1, 'PAID_LEAVE', 'Nghỉ phép cá nhân', 'REJECTED', 'pmhao', '2025-02-28 09:00:00', '2025-02-28 17:00:00','Không đủ ngày nghỉ phép'),
('pmhao', 1, 'UNPAID_LEAVE', 'Nghỉ phép đột xuất', 'APPROVED', 'bkkhanh', '2025-02-27 09:00:00', '2025-02-27 17:00:00',NULL),
('nhtien1', 2, 'UNPAID_LEAVE', 'Làm việc từ xa do bệnh', 'REJECTED', 'pmhao', '2025-02-26 09:00:00', '2025-02-26 17:00:00','Chưa có giấy xác nhận'),
('bkkhanh', 2, 'PAID_LEAVE', 'Xin nghỉ phép do việc gia đình', 'REJECTED', 'bkkhanh', '2025-02-25 09:00:00', '2025-02-25 17:00:00', 'Nhân sự không đủ để thay thế'),
('pnminh', 3, 'PAID_LEAVE', 'Làm việc tại nhà', 'APPROVED', 'pmhao', '2025-02-24 09:00:00', '2025-02-24 17:00:00', NULL);

-- Chèn dữ liệu vào bảng Meeting_Rooms
INSERT INTO Meeting_Rooms (room_name, location, capacity) VALUES
('Sky Room', 'Floor 1', 10),
('Star Room', 'Floor 2', 15),
('Admin Room', 'Floor 3', 20),
('Ocean Room', 'Floor 4', 12),
('Sun Room', 'Floor 5', 8),
('Moon Room', 'Floor 6', 18);

-- Chèn dữ liệu vào bảng Bookings
INSERT INTO Bookings (username, room_id, title, attendees, content, start_time, end_time, status, booking_type, weekdays) VALUES
-- ONLY: Cuộc họp một lần
('ntdu', 1, 'DKR1_Training 1 (Draft)', 'ntdu, pmhao, pnminh, bkkhanh, nhtien1', 'Buổi đào tạo nội bộ', '2025-03-01 10:00:00', '2025-03-01 12:00:00', 'Cancelled', 'ONLY', NULL),
('ntdu', 2, 'Finance Meeting', 'pmhao, bkkhanh', 'Thảo luận ngân sách', '2025-04-11 14:00:00', '2025-04-11 16:00:00', 'Confirmed', 'ONLY', NULL),
('bkkhanh', 3, 'IT Strategy Session', 'bkkhanh, ntdu', 'Chiến lược IT năm 2025', '2025-03-03 09:00:00', '2025-03-03 11:00:00', 'Cancelled', 'ONLY', NULL),
('pnminh', 1, 'HR Policy Review', 'nhtien1, pmhao', 'Xem xét chính sách nhân sự', '2025-03-04 13:00:00', '2025-03-04 15:00:00', 'Requested', 'ONLY', NULL),
('pmhao', 1, 'Customer Feedback Session', 'pmhao, bkkhanh', 'Lắng nghe ý kiến khách hàng', '2025-04-20 15:00:00', '2025-04-20 16:00:00', 'Confirmed', 'ONLY', NULL),
('ntdu', 4, 'Ocean Retro', 'ntdu, pmhao', 'Buổi họp tổng kết quý trước', '2025-03-29 14:00:00', '2025-03-29 15:00:00', 'Cancelled', 'ONLY', NULL),
('ntdu', 4, 'Ocean Brainstorm', 'ntdu, pmhao, htpham', 'Buổi brainstorm chiến lược cho quý mới', '2025-04-10 10:00:00', '2025-04-10 11:30:00', 'Confirmed', 'ONLY', NULL),
('pmhao', 5, 'Sunlight Planning', 'pmhao, bkkhanh', 'Lên kế hoạch dự án ánh sáng', '2025-03-25 10:00:00', '2025-03-25 11:00:00', 'Cancelled', 'ONLY', NULL),
('pmhao', 5, 'Sunlight Review', 'pmhao, bkkhanh', 'Xem xét tiến độ dự án ánh sáng', '2025-04-18 14:00:00', '2025-04-18 15:00:00', 'Confirmed', 'ONLY', NULL),
('bkkhanh', 6, 'Moon Project Kickoff', 'bkkhanh, ntdu', 'Bắt đầu dự án Moonlight', '2025-04-15 15:00:00', '2025-04-15 16:00:00', 'Confirmed', 'ONLY', NULL),
('bkkhanh', 6, 'Moon Pre-Kickoff', 'bkkhanh, ntdu', 'Chuẩn bị khởi động dự án Moon', '2025-03-20 15:00:00', '2025-03-20 16:00:00', 'Cancelled', 'ONLY', NULL),
-- DAILY: Họp từ ngày 15/04/2025 đến 17/04/2025
('ntdu', 1, 'Daily QA Sync', 'ntdu, nhtien1', 'Đồng bộ QA hàng ngày', '2025-04-15 10:30:00', '2025-04-15 11:00:00', 'Confirmed', 'DAILY', NULL),
('ntdu', 1, 'Daily QA Sync', 'ntdu, nhtien1', 'Đồng bộ QA hàng ngày', '2025-04-16 10:30:00', '2025-04-16 11:00:00', 'Confirmed', 'DAILY', NULL),
('ntdu', 1, 'Daily QA Sync', 'ntdu, nhtien1', 'Đồng bộ QA hàng ngày', '2025-04-17 10:30:00', '2025-04-17 11:00:00', 'Confirmed', 'DAILY', NULL),
-- Daily từ ngày 06/03/2025 đến 08/03/2025
('ntdu', 1, 'Daily Scrum Meeting', 'ntdu, pmhao, nhtien1', 'Họp Scrum hằng ngày', '2025-03-06 09:00:00', '2025-03-06 09:30:00', 'Cancelled', 'DAILY', NULL),
('ntdu', 1, 'Daily Scrum Meeting', 'ntdu, pmhao, nhtien1', 'Họp Scrum hằng ngày', '2025-03-07 09:00:00', '2025-03-07 09:30:00', 'Cancelled', 'DAILY', NULL),
('ntdu', 1, 'Daily Scrum Meeting', 'ntdu, pmhao, nhtien1', 'Họp Scrum hằng ngày', '2025-03-08 09:00:00', '2025-03-08 09:30:00', 'Cancelled', 'DAILY', NULL),
-- Daily từ ngày 11/04/2025 đến 12/04/2025
('pmhao', 2, 'Daily Standup', 'pmhao, bkkhanh', 'Cập nhật công việc hằng ngày', '2025-04-11 09:00:00', '2025-04-11 09:30:00', 'Confirmed', 'DAILY', NULL),
('pmhao', 2, 'Daily Standup', 'pmhao, bkkhanh', 'Cập nhật công việc hằng ngày', '2025-04-12 09:00:00', '2025-04-12 09:30:00', 'Confirmed', 'DAILY', NULL),
-- Daily từ ngày 10/04/2025 đến 11/04/2025
('ntdu', 4, 'Daily Ocean Sync', 'ntdu,nhtien1', 'Họp cập nhật tiến độ', '2025-04-10 09:00:00', '2025-04-10 09:30:00', 'Cancelled', 'DAILY', NULL),
('ntdu', 4, 'Daily Ocean Sync', 'ntdu, nhtien1', 'Họp cập nhật tiến độ', '2025-04-11 09:00:00', '2025-04-11 09:30:00', 'Cancelled', 'DAILY', NULL),
-- Daily từ ngày 21/04/2025 đến 22/04/2025
('ntdu', 4, 'Daily Ocean Sync', 'ntdu, nhtien1', 'Họp cập nhật tiến độ', '2025-04-21 09:00:00', '2025-04-21 09:30:00', 'Confirmed', 'DAILY', NULL),
('ntdu', 4, 'Daily Ocean Sync', 'ntdu, nhtien1', 'Họp cập nhật tiến độ', '2025-04-22 09:00:00', '2025-04-22 09:30:00', 'Confirmed', 'DAILY', NULL),
-- Daily từ ngày 12/04/2025 đến 13/04/2025
('pmhao', 5, 'Morning Sun Sync', 'pmhao, bkkhanh', 'Daily Morning Catch-up', '2025-04-12 08:30:00', '2025-04-12 09:00:00', 'Cancelled', 'DAILY', NULL),
('pmhao', 5, 'Morning Sun Sync', 'pmhao, bkkhanh', 'Daily Morning Catch-up', '2025-04-13 08:30:00', '2025-04-13 09:00:00', 'Cancelled', 'DAILY', NULL),
-- Daily từ ngày 23/04/2025 đến 24/04/2025
('pmhao', 5, 'Morning Sun Sync', 'pmhao, bkkhanh', 'Daily Morning Catch-up', '2025-04-23 08:30:00', '2025-04-23 09:00:00', 'Confirmed', 'DAILY', NULL),
('pmhao', 5, 'Morning Sun Sync', 'pmhao, bkkhanh', 'Daily Morning Catch-up', '2025-04-24 08:30:00', '2025-04-24 09:00:00', 'Confirmed', 'DAILY', NULL),
-- Weekly từ ngày 11/04/2025 đến ngày 20/04/2025 (Fr, Mo, Tu, Fr) đặt ngày 11, 14, 15, 18
('bkkhanh', 3, 'Weekly IT Sync', 'bkkhanh, ntdu', 'Đồng bộ công việc IT hằng tuần', '2025-04-11 10:00:00', '2025-04-11 11:00:00', 'Confirmed', 'WEEKLY', 'Mo,Tu,Fr'),
('bkkhanh', 3, 'Weekly IT Sync', 'bkkhanh, ntdu', 'Đồng bộ công việc IT hằng tuần', '2025-04-14 10:00:00', '2025-04-14 11:00:00', 'Confirmed', 'WEEKLY', 'Mo,Tu,Fr'),
('bkkhanh', 3, 'Weekly IT Sync', 'bkkhanh, ntdu', 'Đồng bộ công việc IT hằng tuần', '2025-04-15 10:00:00', '2025-04-15 11:00:00', 'Confirmed', 'WEEKLY', 'Mo,Tu,Fr'),
('bkkhanh', 3, 'Weekly IT Sync', 'bkkhanh, ntdu', 'Đồng bộ công việc IT hằng tuần', '2025-04-18 10:00:00', '2025-04-18 11:00:00', 'Confirmed', 'WEEKLY', 'Mo,Tu,Fr'),
-- Weekly từ ngày 01/03/2025 đến ngày 04/03/2025 (Mo, Tu) đặt ngày 03, 04
('pnminh', 1, 'Weekly HR Meeting', 'nhtien1, pmhao', 'Họp phòng nhân sự hằng tuần', '2025-03-03 14:00:00', '2025-03-03 15:00:00', 'Cancelled', 'WEEKLY', 'Mo,Tu'),
('pnminh', 1, 'Weekly HR Meeting', 'nhtien1, pmhao', 'Họp phòng nhân sự hằng tuần', '2025-03-04 14:00:00', '2025-03-04 15:00:00', 'Cancelled', 'WEEKLY', 'Mo,Tu'),
-- Weekly từ ngày 11/04/2025 đến ngày 17/04/2025 (Fr, Mo, We, Th) đặt ngày 11, 14, 16, 17
('nhtien1', 2, 'Engineering Weekly', 'nhtien1, ntdu', 'Cập nhật tiến độ kỹ thuật', '2025-04-11 08:30:00', '2025-04-11 09:30:00', 'Confirmed', 'WEEKLY', 'Mo,We,Th,Fr'),
('nhtien1', 2, 'Engineering Weekly', 'nhtien1, ntdu', 'Cập nhật tiến độ kỹ thuật', '2025-04-14 08:30:00', '2025-04-14 09:30:00', 'Confirmed', 'WEEKLY', 'Mo,We,Th,Fr'),
('nhtien1', 2, 'Engineering Weekly', 'nhtien1, ntdu', 'Cập nhật tiến độ kỹ thuật', '2025-04-16 08:30:00', '2025-04-16 09:30:00', 'Confirmed', 'WEEKLY', 'Mo,We,Th,Fr'),
('nhtien1', 2, 'Engineering Weekly', 'nhtien1, ntdu', 'Cập nhật tiến độ kỹ thuật', '2025-04-17 08:30:00', '2025-04-17 09:30:00', 'Confirmed', 'WEEKLY', 'Mo,We,Th,Fr'),
-- WEEKLY: Họp mỗi thứ Ba và thứ Năm từ ngày 22/04 đến 29/04
('pnminh', 2, 'Dev Team Weekly', 'pnminh, nhtien1', 'Lập kế hoạch sprint', '2025-04-22 14:00:00', '2025-04-22 15:00:00', 'Confirmed', 'WEEKLY', 'Tu,Th'),
('pnminh', 2, 'Dev Team Weekly', 'pnminh, nhtien1', 'Lập kế hoạch sprint', '2025-04-24 14:00:00', '2025-04-24 15:00:00', 'Confirmed', 'WEEKLY', 'Tu,Th'),
('pnminh', 2, 'Dev Team Weekly', 'pnminh, nhtien1', 'Lập kế hoạch sprint', '2025-04-29 14:00:00', '2025-04-29 15:00:00', 'Confirmed', 'WEEKLY', 'Tu,Th'),
-- WEEKLY: Họp mỗi thứ Ba và thứ Năm từ ngày 09/04 đến 18/04
('bkkhanh', 6, 'Moonlight Weekly', 'bkkhanh, ntdu', 'Họp cập nhật tiến độ Moon', '2025-04-09 14:00:00', '2025-04-09 15:00:00', 'Cancelled', 'WEEKLY', 'We,Fr'),
('bkkhanh', 6, 'Moonlight Weekly', 'bkkhanh, ntdu', 'Họp cập nhật tiến độ Moon', '2025-04-11 14:00:00', '2025-04-11 15:00:00', 'Cancelled', 'WEEKLY', 'We,Fr'),
('bkkhanh', 6, 'Moonlight Weekly', 'bkkhanh, ntdu', 'Họp cập nhật tiến độ Moon', '2025-04-16 14:00:00', '2025-04-16 15:00:00', 'Confirmed', 'WEEKLY', 'We,Fr'),
('bkkhanh', 6, 'Moonlight Weekly', 'bkkhanh, ntdu', 'Họp cập nhật tiến độ Moon', '2025-04-18 14:00:00', '2025-04-18 15:00:00', 'Confirmed', 'WEEKLY', 'We,Fr');

-- Chèn dữ liệu mẫu vào bảng Notifications
INSERT INTO Notifications (title, content, sender, receiver, created_at, is_read)
VALUES 
('Phê duyệt đơn nghỉ phép', 'Đơn xin nghỉ phép của bạn đã được phê duyệt từ ngày 25/04 đến 27/04', 'bkkhanh', 'pmhao', DATE_SUB(NOW(), INTERVAL 20 MINUTE), false),
('Lịch họp phòng ban', 'Cuộc họp phòng ban sẽ diễn ra vào 14:00 ngày 22/04 tại phòng họp A', 'bkkhanh', 'ntdu', DATE_SUB(NOW(), INTERVAL 2 HOUR), false),
('Thông báo cập nhật hệ thống', 'Hệ thống sẽ bảo trì vào tối nay từ 22:00-23:00', 'bkkhanh', 'ntdu', DATE_SUB(NOW(), INTERVAL 1 DAY), true),
('Thông báo lương tháng 4', 'Lương tháng 4 đã được chuyển vào tài khoản của bạn', 'bkkhanh', 'ntdu', DATE_SUB(NOW(), INTERVAL 5 HOUR), false),
('Đánh giá hiệu suất', 'Đánh giá hiệu suất quý I/2025 sẽ diễn ra từ 01/05-10/05', 'bkkhanh', 'ntdu', DATE_SUB(NOW(), INTERVAL 2 DAY), true),

('Yêu cầu phê duyệt', 'Có một yêu cầu mới cần bạn phê duyệt từ nhân viên Nguyễn Văn A', 'ntdu', 'pmhao', DATE_SUB(NOW(), INTERVAL 10 MINUTE), false),
('Lịch phỏng vấn ứng viên', 'Bạn có lịch phỏng vấn ứng viên vào 9:00 sáng mai', 'ntdu', 'bkkhanh', DATE_SUB(NOW(), INTERVAL 3 HOUR), false),
('Kết quả đánh giá Q1', 'Kết quả đánh giá hiệu suất Q1 đã sẵn sàng để xem xét', 'ntdu', 'bkkhanh', DATE_SUB(NOW(), INTERVAL 8 HOUR), true),
('Thông báo họp Ban Giám đốc', 'Cuộc họp Ban Giám đốc sẽ diễn ra vào 8:00 ngày mai tại phòng họp VIP', 'ntdu', 'bkkhanh', DATE_SUB(NOW(), INTERVAL 7 HOUR), false),
('Yêu cầu duyệt ngân sách', 'Có yêu cầu duyệt ngân sách dự án mới từ phòng Kinh doanh', 'ntdu', 'bkkhanh', DATE_SUB(NOW(), INTERVAL 2 DAY), false);

select * from requests;
select * from users;
select * from bookings;