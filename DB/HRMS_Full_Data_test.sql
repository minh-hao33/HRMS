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
('IT'),
('Marketing'),
('Operations'),
('Research & Development');

-- Cập nhật lại danh sách Users với phân bổ đều phòng ban (department_id từ 1 đến 6)
INSERT INTO Users (username, password, email, role_name, department_id, is_supervisor, status, employee_name) VALUES
-- Admin
('bkkhanh', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'bkkhanh@cmcglobal.vn', 'ADMIN', 1, TRUE, 'Active', 'Khac Khanh Bui'),
('ntdu', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'ntdu@cmcglobal.vn', 'ADMIN', 2, FALSE, 'Active', 'Trung Du Nguyen'),
-- Supervisors
('nvhoang', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'nvhoang@cmcglobal.vn', 'SUPERVISOR', 3, TRUE, 'Active', 'Van Hoang Nguyen'),
('pttung', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'pttung@cmcglobal.vn', 'SUPERVISOR', 4, TRUE, 'Active', 'Thanh Tung Pham'),
('nhhoa', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'nhhoa@cmcglobal.vn', 'SUPERVISOR', 5, TRUE, 'Active', 'Hong Hoa Nguyen'),
('nhtien1', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'nhtien1@cmcglobal.vn', 'SUPERVISOR', 6, FALSE, 'Active', 'Huu Tien Pham'),
('lqbao', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'lqbao@cmcglobal.vn', 'SUPERVISOR', 1, TRUE, 'Active', 'Quoc Bao Le'),
('tvlinh', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'tvlinh@cmcglobal.vn', 'SUPERVISOR', 2, TRUE, 'Active', 'Viet Linh Tran'),
('pmhao', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'pmhao@cmcglobal.vn', 'SUPERVISOR', 2, TRUE, 'Active', 'Minh Hao Pham'),
-- Employees
('lthanh', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'lthanh@cmcglobal.vn', 'EMPLOYEE', 3, FALSE, 'Active', 'Thanh Le'),
('khanhvu', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'khanhvu@cmcglobal.vn', 'EMPLOYEE', 4, FALSE, 'Active', 'Khanh Vu'),
('ndthao', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'ndthao@cmcglobal.vn', 'EMPLOYEE', 5, FALSE, 'Inactive', 'Dieu Thao Nguyen'),
('hxminh', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'hxminh@cmcglobal.vn', 'EMPLOYEE', 6, FALSE, 'Active', 'Xuan Minh Hoang'),
('tdnam', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'tdnam@cmcglobal.vn', 'EMPLOYEE', 1, TRUE, 'Active', 'Duc Nam Tran'),
('pnminh', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'pnminh@cmcglobal.vn', 'EMPLOYEE', 3, FALSE, 'Inactive', 'Nhat Minh Pham'),
('nvhung', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'nvhung@cmcglobal.vn', 'EMPLOYEE', 4, FALSE, 'Active', 'Van Hung Nguyen'),
('ltnga', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'ltnga@cmcglobal.vn', 'EMPLOYEE', 5, FALSE, 'Active', 'Thanh Nga Le'),
('htbao', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'htbao@cmcglobal.vn', 'EMPLOYEE', 6, FALSE, 'Active', 'Tien Bao Ho'),
('dtquynh', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'dtquynh@cmcglobal.vn', 'EMPLOYEE', 1, FALSE, 'Inactive', 'Thu Quynh Do'),
('dnphuc', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 'dnphuc@cmcglobal.vn', 'EMPLOYEE', 2, FALSE, 'Active', 'Ngoc Phuc Do');

-- Chèn dữ liệu vào bảng Requests
INSERT INTO Requests (username, department_id, request_type, request_reason, request_status, approver_username, start_time, end_time, rejection_reason) VALUES
('ntdu', 2, 'PAID_LEAVE', 'Nghỉ phép cá nhân', 'REJECTED', 'pmhao', '2025-02-28 09:00:00', '2025-02-28 17:00:00','Không đủ ngày nghỉ phép'),
('pmhao', 2, 'UNPAID_LEAVE', 'Nghỉ phép đột xuất', 'APPROVED', 'bkkhanh', '2025-02-27 09:00:00', '2025-02-27 17:00:00',NULL),
('nhtien1', 6, 'UNPAID_LEAVE', 'Làm việc từ xa do bệnh', 'REJECTED', 'pmhao', '2025-02-26 09:00:00', '2025-02-26 17:00:00','Chưa có giấy xác nhận'),
('bkkhanh', 1, 'PAID_LEAVE', 'Xin nghỉ phép do việc gia đình', 'REJECTED', 'bkkhanh', '2025-02-25 09:00:00', '2025-02-25 17:00:00', 'Nhân sự không đủ để thay thế'),
('pnminh', 3, 'PAID_LEAVE', 'Làm việc tại nhà', 'APPROVED', 'pmhao', '2025-02-24 09:00:00', '2025-02-24 17:00:00', NULL),
('nvhung', 4, 'WFH', 'Làm việc tại nhà do có việc riêng', 'APPROVED', 'lqbao', '2025-01-15 08:00:00', '2025-01-15 17:00:00', NULL),
('ltnga', 5, 'PAID_LEAVE', 'Nghỉ phép du lịch', 'PENDING', 'tvlinh', '2025-03-05 09:00:00', '2025-03-06 17:00:00', NULL),
('htbao', 6, 'OVERTIME', 'Làm thêm giờ để hoàn thành dự án', 'APPROVED', 'pmhao', '2025-02-20 18:00:00', '2025-02-20 21:00:00', NULL),
('dtquynh', 1, 'PAID_LEAVE', 'Nghỉ phép do sức khỏe', 'REJECTED', 'nhtien1', '2025-01-25 09:00:00', '2025-01-25 17:00:00', 'Thiếu thông tin bác sĩ'),
('dnphuc', 2, 'UNPAID_LEAVE', 'Xin nghỉ giải quyết việc cá nhân', 'APPROVED', 'bkkhanh', '2025-03-10 09:00:00', '2025-03-10 17:00:00', NULL),
('ntdu', 1, 'WFH', 'Làm việc từ xa do lịch trình di chuyển', 'APPROVED', 'pmhao', '2025-01-18 08:00:00', '2025-01-18 17:00:00', NULL),
('pnminh', 3, 'UNPAID_LEAVE', 'Nghỉ đột xuất', 'PENDING', 'tvlinh', '2025-02-05 09:00:00', '2025-02-05 17:00:00', NULL),
('nvhung', 4, 'OVERTIME', 'Hỗ trợ hoàn thành deadline', 'APPROVED', 'lqbao', '2025-02-22 18:00:00', '2025-02-22 20:00:00', NULL),
('ltnga', 5, 'WFH', 'Làm việc từ xa để chăm sóc gia đình', 'REJECTED', 'nhtien1', '2025-01-30 08:00:00', '2025-01-30 17:00:00', 'Không phù hợp với tiến độ dự án'),
('htbao', 1, 'PAID_LEAVE', 'Nghỉ phép thăm gia đình', 'APPROVED', 'pmhao', '2025-03-12 09:00:00', '2025-03-13 17:00:00', NULL),
('dtquynh', 1, 'UNPAID_LEAVE', 'Nghỉ để xử lý công việc cá nhân', 'PENDING', 'lqbao', '2025-02-14 09:00:00', '2025-02-14 17:00:00', NULL),
('dnphuc', 2, 'PAID_LEAVE', 'Nghỉ phép đi khám bệnh', 'APPROVED', 'bkkhanh', '2025-01-20 09:00:00', '2025-01-20 17:00:00', NULL),
('pmhao', 2, 'OVERTIME', 'Làm thêm giờ triển khai dự án', 'APPROVED', 'bkkhanh', '2025-01-22 18:00:00', '2025-01-22 21:00:00', NULL),
('nhtien1', 6, 'WFH', 'Làm việc từ xa do bệnh nhẹ', 'APPROVED', 'bkkhanh', '2025-03-02 08:00:00', '2025-03-02 17:00:00', NULL),
('lqbao', 1, 'PAID_LEAVE', 'Nghỉ phép tham gia hội thảo', 'PENDING', 'bkkhanh', '2025-03-15 09:00:00', '2025-03-15 17:00:00', NULL),
('tvlinh', 1, 'UNPAID_LEAVE', 'Nghỉ để giải quyết công việc cá nhân', 'REJECTED', 'bkkhanh', '2025-01-27 09:00:00', '2025-01-27 17:00:00', 'Thiếu nhân sự hỗ trợ dự án'),
('bkkhanh', 1, 'WFH', 'Làm việc từ xa để xử lý tài liệu', 'APPROVED', 'bkkhanh', '2025-03-01 08:00:00', '2025-03-01 17:00:00', NULL),
('ntdu', 2, 'SICK_LEAVE', 'Nghỉ ốm để điều trị bệnh', 'APPROVED', 'pmhao', '2025-03-10 09:00:00', '2025-03-10 17:00:00', NULL),
('pmhao', 2, 'MATERNITY_LEAVE', 'Nghỉ thai sản để chăm sóc con', 'PENDING', 'bkkhanh', '2025-04-01 09:00:00', '2025-04-01 17:00:00', NULL),
('nhtien1', 6, 'PATERNITY_LEAVE', 'Nghỉ thai sản chăm sóc vợ sau sinh', 'APPROVED', 'pmhao', '2025-03-20 09:00:00', '2025-03-20 17:00:00', NULL),
('bkkhanh', 1, 'COMPENSATORY_LEAVE', 'Nghỉ bù do làm thêm giờ tuần trước', 'PENDING', 'bkkhanh', '2025-03-15 09:00:00', '2025-03-15 17:00:00', NULL),
('pnminh', 3, 'STUDY_LEAVE', 'Nghỉ học để tham gia khóa đào tạo', 'APPROVED', 'pmhao', '2025-02-10 09:00:00', '2025-02-10 17:00:00', NULL),
('nvhung', 4, 'PERSONAL_LEAVE', 'Nghỉ việc riêng để giải quyết công việc gia đình', 'REJECTED', 'lqbao', '2025-02-12 09:00:00', '2025-02-12 17:00:00', 'Không đủ nhân sự thay thế'),
('ltnga', 5, 'BEREAVEMENT_LEAVE', 'Nghỉ phép do tang lễ của người thân', 'APPROVED', 'nhtien1', '2025-02-28 09:00:00', '2025-02-28 17:00:00', NULL),
('htbao', 6, 'PREGNANCY_EXAM_LEAVE', 'Nghỉ phép để khám thai định kỳ', 'APPROVED', 'pmhao', '2025-03-05 09:00:00', '2025-03-05 17:00:00', NULL),
('dtquynh', 1, 'CONTRACEPTIVE_MEASURE_LEAVE', 'Nghỉ phép thực hiện biện pháp tránh thai', 'PENDING', 'bkkhanh', '2025-03-08 09:00:00', '2025-03-08 17:00:00', NULL),
('dnphuc', 2, 'MISCARRIAGE_LEAVE', 'Nghỉ phép do sảy thai', 'APPROVED', 'pmhao', '2025-03-10 09:00:00', '2025-03-10 17:00:00', NULL),
('ntdu', 1, 'FAMILY_WEDDING_FUNERAL_LEAVE', 'Nghỉ phép không hưởng lương do người thân kết hôn', 'REJECTED', 'nhtien1', '2025-02-15 09:00:00', '2025-02-15 17:00:00', 'Không đủ số ngày phép không lương'),
('pnminh', 3, 'EMPLOYEE_WEDDING_LEAVE', 'Nghỉ phép do kết hôn', 'APPROVED', 'pmhao', '2025-03-03 09:00:00', '2025-03-03 17:00:00', NULL),
('nvhung', 4, 'CHILD_WEDDING_LEAVE', 'Nghỉ phép để tham dự đám cưới của con', 'APPROVED', 'lqbao', '2025-03-20 09:00:00', '2025-03-20 17:00:00', NULL),
('ltnga', 5, 'FAMILY_DEATH_LEAVE', 'Nghỉ phép do người thân qua đời', 'APPROVED', 'nhtien1', '2025-03-12 09:00:00', '2025-03-12 17:00:00', NULL),
('htbao', 6, 'PAID_LEAVE', 'Nghỉ phép có lương để đi du lịch', 'PENDING', 'pmhao', '2025-04-01 09:00:00', '2025-04-01 17:00:00', NULL);


-- Chèn dữ liệu vào bảng Meeting_Rooms
INSERT INTO Meeting_Rooms (room_name, location, capacity) VALUES
('Sky Room', 'Floor 1', 10),
('Star Room', 'Floor 2', 15),
('Admin Room', 'Floor 3', 20),
('Ocean Room', 'Floor 4', 12),
('Sun Room', 'Floor 5', 8),
('Moon Room', 'Floor 6', 18),
('Cloud Room', 'Floor 7', 24),
('Galaxy Room', 'Floor 8', 28),
('Wind Room', 'Floor 9', 22),
('Rain Room', 'Floor 10', 26),
('Fire Room', 'Floor 11', 30),
('Earth Room', 'Floor 12', 27);

INSERT INTO Bookings (username, room_id, title, attendees, content, start_time, end_time, status, booking_type, weekdays) VALUES
-- ONLY: Cuộc họp một lần
('lthanh', 1, 'DKR1_Training 1 (Draft)', 'ntdu, pmhao, pnminh, bkkhanh, nhtien1', 'Buổi đào tạo nội bộ', '2025-03-01 10:00:00', '2025-03-01 12:00:00', 'Cancelled', 'ONLY', NULL),
('khanhvu', 2, 'Finance Meeting', 'pmhao, bkkhanh', 'Thảo luận ngân sách', '2025-04-26 14:00:00', '2025-04-26 16:00:00', 'Confirmed', 'ONLY', NULL),
('ndthao', 3, 'IT Strategy Session', 'bkkhanh, ntdu', 'Chiến lược IT năm 2025', '2025-03-03 09:00:00', '2025-03-03 11:00:00', 'Cancelled', 'ONLY', NULL),
('hxminh', 1, 'HR Policy Review', 'nhtien1, pmhao', 'Xem xét chính sách nhân sự', '2025-03-04 13:00:00', '2025-03-04 15:00:00', 'Cancelled', 'ONLY', NULL),
('tdnam', 2, 'Customer Feedback Session', 'pmhao, bkkhanh', 'Lắng nghe ý kiến khách hàng', '2025-04-20 15:00:00', '2025-04-20 16:00:00', 'Cancelled', 'ONLY', NULL),
('pnminh', 3, 'Ocean Retro', 'ntdu, pmhao', 'Buổi họp tổng kết quý trước', '2025-03-29 14:00:00', '2025-03-29 15:00:00', 'Cancelled', 'ONLY', NULL),
('nvhung', 4, 'Ocean Brainstorm', 'ntdu, pmhao, htpham', 'Buổi brainstorm chiến lược cho quý mới', '2025-04-26 10:00:00', '2025-04-26 11:30:00', 'Confirmed', 'ONLY', NULL),
('ltnga', 5, 'Sunlight Planning', 'pmhao, bkkhanh', 'Lên kế hoạch dự án ánh sáng', '2025-03-25 10:00:00', '2025-03-25 11:00:00', 'Cancelled', 'ONLY', NULL),
('htbao', 5, 'Sunlight Review', 'pmhao, bkkhanh', 'Xem xét tiến độ dự án ánh sáng', '2025-04-27 14:00:00', '2025-04-27 15:00:00', 'Confirmed', 'ONLY', NULL),
('dnphuc', 6, 'Moon Project Kickoff', 'bkkhanh, ntdu', 'Bắt đầu dự án Moonlight', '2025-04-27 15:00:00', '2025-04-27 16:00:00', 'Confirmed', 'ONLY', NULL),
('dtquynh', 6, 'Moon Pre-Kickoff', 'bkkhanh, ntdu', 'Chuẩn bị khởi động dự án Moon', '2025-03-20 15:00:00', '2025-03-20 16:00:00', 'Cancelled', 'ONLY', NULL),
('tvlinh', 1, 'Sky Room Strategy Meeting', 'ntdu, pmhao, bkkhanh', 'Thảo luận chiến lược cho dự án', '2025-05-01 09:00:00', '2025-05-01 10:30:00', 'Confirmed', 'ONLY', NULL),
('lthanh', 2, 'Star Room Budget Planning', 'pmhao, bkkhanh, ntdu', 'Lên kế hoạch ngân sách cho năm 2025', '2025-05-02 11:00:00', '2025-05-02 12:30:00', 'Confirmed', 'ONLY', NULL),
('khanhvu', 3, 'Admin Room Quarterly Review', 'bkkhanh, pnminh, nhtien1', 'Đánh giá tiến độ công việc quý I', '2025-05-03 14:00:00', '2025-05-03 16:00:00', 'Confirmed', 'ONLY', NULL),
('ndthao', 4, 'Ocean Room Team Sync', 'pnminh, pmhao, nhtien1', 'Cập nhật tiến độ dự án và các vấn đề cần giải quyết', '2025-05-04 13:00:00', '2025-05-04 14:30:00', 'Confirmed', 'ONLY', NULL),
('hxminh', 5, 'Sun Room Innovation Session', 'ntdu, bkkhanh, pmhao', 'Brainstorm ý tưởng sáng tạo cho sản phẩm mới', '2025-05-05 10:00:00', '2025-05-05 11:30:00', 'Confirmed', 'ONLY', NULL),
('tdnam', 6, 'Moon Room Project Kickoff', 'pmhao, ntdu, pnminh', 'Khởi động dự án Moonlight', '2025-05-06 15:00:00', '2025-05-06 16:30:00', 'Confirmed', 'ONLY', NULL),
('pnminh', 7, 'Cloud Room Marketing Strategy', 'bkkhanh, pmhao, ntdu', 'Lập kế hoạch chiến lược tiếp thị', '2025-05-07 09:00:00', '2025-05-07 10:30:00', 'Cancelled', 'ONLY', NULL),
('nvhung', 8, 'Galaxy Room Tech Talk', 'ntdu, pnminh, bkkhanh', 'Hướng dẫn công nghệ cho đội phát triển', '2025-05-08 13:00:00', '2025-05-08 14:30:00', 'Confirmed', 'ONLY', NULL),
('ltnga', 9, 'Wind Room Client Meeting', 'pmhao, bkkhanh, pnminh', 'Cuộc họp với khách hàng để trình bày báo cáo tiến độ', '2025-05-09 11:00:00', '2025-05-09 12:30:00', 'Confirmed', 'ONLY', NULL),
('htbao', 10, 'Rain Room Staff Meeting', 'ntdu, bkkhanh', 'Cuộc họp toàn bộ nhân viên để cập nhật thông tin công ty', '2025-05-10 14:00:00', '2025-05-10 15:30:00', 'Cancelled', 'ONLY', NULL),
('dtquynh', 11, 'Fire Room Strategy Planning', 'bkkhanh, ntdu, pmhao', 'Lập kế hoạch chiến lược cho quý 2', '2025-05-11 16:00:00', '2025-05-11 17:30:00', 'Confirmed', 'ONLY', NULL),
('dnphuc', 12, 'Earth Room Annual Review', 'pnminh, nhtien1, pmhao', 'Đánh giá tổng kết năm 2024', '2025-05-12 09:00:00', '2025-05-12 10:30:00', 'Confirmed', 'ONLY', NULL),

-- DAILY: Họp từ ngày 15/04/2025 đến 17/04/2025
('lthanh', 1, 'Daily QA Sync', 'lthanh, nvhoang, pnminh', 'Đồng bộ QA hàng ngày', '2025-04-15 10:30:00', '2025-04-15 11:00:00', 'Cancelled', 'DAILY', NULL),
('lthanh', 1, 'Daily QA Sync', 'lthanh, nvhoang, pnminh', 'Đồng bộ QA hàng ngày', '2025-04-16 10:30:00', '2025-04-16 11:00:00', 'Cancelled', 'DAILY', NULL),
('lthanh', 1, 'Daily QA Sync', 'lthanh, nvhoang, pnminh', 'Đồng bộ QA hàng ngày', '2025-04-17 10:30:00', '2025-04-17 11:00:00', 'Cancelled', 'DAILY', NULL),
-- Daily từ ngày 06/03/2025 đến 08/03/2025
('khanhvu', 8, 'Daily Scrum Meeting', 'khanhvu, pttung, nvhung, htbao', 'Họp Scrum hằng ngày', '2025-03-06 09:00:00', '2025-03-06 09:30:00', 'Cancelled', 'DAILY', NULL),
('khanhvu', 8, 'Daily Scrum Meeting', 'khanhvu, pttung, nvhung, htbao', 'Họp Scrum hằng ngày', '2025-03-07 09:00:00', '2025-03-07 09:30:00', 'Cancelled', 'DAILY', NULL),
('khanhvu', 8, 'Daily Scrum Meeting', 'khanhvu, pttung, nvhung, htbao', 'Họp Scrum hằng ngày', '2025-03-08 09:00:00', '2025-03-08 09:30:00', 'Cancelled', 'DAILY', NULL),
-- Daily từ ngày 29/04/2025 đến 30/04/2025
('pnminh', 2, 'Daily Standup', 'pnminh, nvhoang', 'Cập nhật công việc hằng ngày', '2025-04-29 09:00:00', '2025-04-29 09:30:00', 'Confirmed', 'DAILY', NULL),
('pnminh', 2, 'Daily Standup', 'pnminh, nvhoang', 'Cập nhật công việc hằng ngày', '2025-04-30 09:00:00', '2025-04-30 09:30:00', 'Confirmed', 'DAILY', NULL),
-- Daily từ ngày 10/04/2025 đến 11/04/2025
('ndthao', 4, 'Daily Ocean Sync', 'ntdu, pnminh, htbao', 'Họp cập nhật tiến độ', '2025-04-10 10:00:00', '2025-04-10 10:30:00', 'Confirmed', 'DAILY', NULL),
('ndthao', 4, 'Daily Ocean Sync', 'ntdu, pnminh, htbao', 'Họp cập nhật tiến độ', '2025-04-11 10:00:00', '2025-04-11 10:30:00', 'Confirmed', 'DAILY', NULL),
-- Daily từ ngày 28/04/2025 đến 29/04/2025
('hxminh', 4, 'Daily Ocean Sync', 'hxminh, pnminh', 'Họp cập nhật tiến độ', '2025-04-28 09:30:00', '2025-04-28 10:30:00', 'Confirmed', 'DAILY', NULL),
('hxminh', 4, 'Daily Ocean Sync', 'hxminh, pnminh', 'Họp cập nhật tiến độ', '2025-04-29 09:30:00', '2025-04-29 10:30:00', 'Confirmed', 'DAILY', NULL),
-- Daily từ ngày 12/04/2025 đến 13/04/2025
('tdnam', 5, 'Morning Sun Sync', 'pmhao, pnminh, ltnga', 'Daily Morning Catch-up', '2025-04-12 08:30:00', '2025-04-12 09:00:00', 'Cancelled', 'DAILY', NULL),
('tdnam', 5, 'Morning Sun Sync', 'pmhao, pnminh, ltnga', 'Daily Morning Catch-up', '2025-04-13 08:30:00', '2025-04-13 09:00:00', 'Cancelled', 'DAILY', NULL),
-- Daily từ ngày 23/04/2025 đến 24/04/2025
('nvhung', 5, 'Morning Sun Sync', 'pmhao, bkkhanh', 'Daily Morning Catch-up', '2025-04-23 08:30:00', '2025-04-23 09:00:00', 'Confirmed', 'DAILY', NULL),
('nvhung', 5, 'Morning Sun Sync', 'pmhao, bkkhanh', 'Daily Morning Catch-up', '2025-04-24 08:30:00', '2025-04-24 09:00:00', 'Confirmed', 'DAILY', NULL),
-- Daily từ ngày 27/04/2025 đến 28/04/2025
('ltnga', 6, 'Morning Sync', 'bkkhanh, pnminh', 'Cập nhật công việc buổi sáng', '2025-04-27 08:00:00', '2025-04-27 09:00:00', 'Confirmed', 'DAILY', NULL),
('ltnga', 6, 'Morning Sync', 'bkkhanh, pnminh', 'Cập nhật công việc buổi sáng', '2025-04-28 08:00:00', '2025-04-28 09:00:00', 'Confirmed', 'DAILY', NULL),
-- Daily từ ngày 01/05/2025 đến 02/05/2025
('ltnga', 9, 'Daily Review', 'nhtien1, nvhung, htbao', 'Đánh giá công việc hàng ngày', '2025-05-01 09:30:00', '2025-05-01 10:00:00', 'Confirmed', 'DAILY', NULL),
('ltnga', 9, 'Daily Review', 'nhtien1, nvhung', 'Đánh giá công việc hàng ngày', '2025-05-02 09:30:00', '2025-05-02 10:00:00', 'Confirmed', 'DAILY', NULL),
-- Daily từ ngày 18/04/2025 đến 19/04/2025
('ltnga', 11, 'End of Day Sync', 'ltnga, htbao', 'Tổng kết cuối ngày', '2025-05-07 16:30:00', '2025-05-07 17:00:00', 'Confirmed', 'DAILY', NULL),
('ltnga', 11, 'End of Day Sync', 'ltnga, htbao', 'Tổng kết cuối ngày', '2025-05-08 16:30:00', '2025-05-08 17:00:00', 'Confirmed', 'DAILY', NULL),
('ltnga', 11, 'End of Day Sync', 'ltnga, htbao', 'Tổng kết cuối ngày', '2025-05-09 16:30:00', '2025-05-09 17:00:00', 'Confirmed', 'DAILY', NULL),
-- Daily từ ngày 28/04/2025 đến 29/04/2025
('htbao', 12, 'Team Update', 'htbao, dtquynh', 'Cập nhật nhóm hàng ngày', '2025-04-28 11:00:00', '2025-04-28 12:00:00', 'Confirmed', 'DAILY', NULL),
('htbao', 12, 'Team Update', 'htbao, dtquynh', 'Cập nhật nhóm hàng ngày', '2025-04-29 11:00:00', '2025-04-29 12:00:00', 'Confirmed', 'DAILY', NULL),
-- Daily từ ngày 22/04/2025 đến 23/04/2025
('dtquynh', 3, 'Team Catch-Up', 'dtquynh, dnphuc', 'Họp nhóm để bắt kịp tiến độ', '2025-04-22 10:00:00', '2025-04-22 10:30:00', 'Cancelled', 'DAILY', NULL),
('dtquynh', 3, 'Team Catch-Up', 'dtquynh, dnphuc', 'Họp nhóm để bắt kịp tiến độ', '2025-04-23 10:00:00', '2025-04-23 10:30:00', 'Cancelled', 'DAILY', NULL),
-- Daily từ ngày 02/05/2025 đến 03/05/2025
('dnphuc', 2, 'Weekly Sync', 'lqbao, tvlinh', 'Họp tuần để thống nhất công việc', '2025-05-02 10:30:00', '2025-05-02 11:30:00', 'Confirmed', 'DAILY', NULL),
('dnphuc', 2, 'Weekly Sync', 'lqbao, tvlinh', 'Họp tuần để thống nhất công việc', '2025-05-03 10:30:00', '2025-05-03 11:30:00', 'Confirmed', 'DAILY', NULL),


-- Weekly từ ngày 29/05/2025 đến ngày 07/05/2025 (We, Th, Fr) đặt ngày 30, 01, 02, 07
('lthanh', 3, 'Weekly IT Sync', 'lthanh, ntdu, khanhvu, bkkhanh', 'Đồng bộ công việc IT hằng tuần', '2025-04-30 10:00:00', '2025-04-30 11:00:00', 'Confirmed', 'WEEKLY', 'We,Th,Fr'),
('lthanh', 3, 'Weekly IT Sync', 'lthanh, ntdu, khanhvu, bkkhanh', 'Đồng bộ công việc IT hằng tuần', '2025-05-01 10:00:00', '2025-05-01 11:00:00', 'Confirmed', 'WEEKLY', 'We,Th,Fr'),
('lthanh', 3, 'Weekly IT Sync', 'lthanh, ntdu, khanhvu, bkkhanh', 'Đồng bộ công việc IT hằng tuần', '2025-05-02 10:00:00', '2025-05-02 11:00:00', 'Confirmed', 'WEEKLY', 'We,Th,Fr'),
('lthanh', 3, 'Weekly IT Sync', 'lthanh, ntdu, khanhvu, bkkhanh', 'Đồng bộ công việc IT hằng tuần', '2025-05-07 10:00:00', '2025-05-07 11:00:00', 'Confirmed', 'WEEKLY', 'We,Th,Fr'),
-- Weekly từ ngày 11/04/2025 đến ngày 17/04/2025 (Fr, Mo, We, Th) đặt ngày 11, 14, 16, 17
('khanhvu', 2, 'Engineering Weekly', 'nhtien1, khanhvu, lthanh', 'Cập nhật tiến độ kỹ thuật', '2025-04-11 08:30:00', '2025-04-11 09:30:00', 'Confirmed', 'WEEKLY', 'Mo,We,Th,Fr'),
('khanhvu', 2, 'Engineering Weekly', 'nhtien1, khanhvu, lthanh', 'Cập nhật tiến độ kỹ thuật', '2025-04-14 08:30:00', '2025-04-14 09:30:00', 'Confirmed', 'WEEKLY', 'Mo,We,Th,Fr'),
('khanhvu', 2, 'Engineering Weekly', 'nhtien1, khanhvu, lthanh', 'Cập nhật tiến độ kỹ thuật', '2025-04-16 08:30:00', '2025-04-16 09:30:00', 'Confirmed', 'WEEKLY', 'Mo,We,Th,Fr'),
('khanhvu', 2, 'Engineering Weekly', 'nhtien1, khanhvu, lthanh', 'Cập nhật tiến độ kỹ thuật', '2025-04-17 08:30:00', '2025-04-17 09:30:00', 'Confirmed', 'WEEKLY', 'Mo,We,Th,Fr'),
-- WEEKLY: Họp mỗi thứ Ba và thứ Năm từ ngày 09/04 đến 18/04
('ndthao', 6, 'Moonlight Weekly', 'bkkhanh, ntdu', 'Họp cập nhật tiến độ Moon', '2025-04-09 14:00:00', '2025-04-09 15:00:00', 'Cancelled', 'WEEKLY', 'We,Fr'),
('ndthao', 6, 'Moonlight Weekly', 'bkkhanh, ntdu', 'Họp cập nhật tiến độ Moon', '2025-04-11 14:00:00', '2025-04-11 15:00:00', 'Cancelled', 'WEEKLY', 'We,Fr'),
('ndthao', 6, 'Moonlight Weekly', 'bkkhanh, ntdu', 'Họp cập nhật tiến độ Moon', '2025-04-16 14:00:00', '2025-04-16 15:00:00', 'Confirmed', 'WEEKLY', 'We,Fr'),
('ndthao', 6, 'Moonlight Weekly', 'bkkhanh, ntdu', 'Họp cập nhật tiến độ Moon', '2025-04-18 14:00:00', '2025-04-18 15:00:00', 'Confirmed', 'WEEKLY', 'We,Fr'),
-- WEEKLY: Họp mỗi thứ Ba và thứ Năm từ ngày 09/04 đến 18/04
('tdnam', 9, 'Moonlight Weekly', 'tdnam, bkkhanh, ntdu', 'Họp cập nhật tiến độ Moon', '2025-04-09 14:00:00', '2025-04-09 15:00:00', 'Cancelled', 'WEEKLY', 'We,Fr'),
('tdnam', 9, 'Moonlight Weekly', 'tdnam, bkkhanh, ntdu', 'Họp cập nhật tiến độ Moon', '2025-04-11 14:00:00', '2025-04-11 15:00:00', 'Cancelled', 'WEEKLY', 'We,Fr'),
('tdnam', 9, 'Moonlight Weekly', 'tdnam, bkkhanh, ntdu', 'Họp cập nhật tiến độ Moon', '2025-04-16 14:00:00', '2025-04-16 15:00:00', 'Confirmed', 'WEEKLY', 'We,Fr'),
('tdnam', 9, 'Moonlight Weekly', 'tdnam, bkkhanh, ntdu', 'Họp cập nhật tiến độ Moon', '2025-04-18 14:00:00', '2025-04-18 15:00:00', 'Confirmed', 'WEEKLY', 'We,Fr'),
-- WEEKLY: Họp mỗi thứ Ba và thứ Năm từ ngày 09/04 đến 18/04
('hxminh', 12, 'Moonlight Weekly', 'khanhvu, bkkhanh, ntdu', 'Họp cập nhật tiến độ Moon', '2025-04-09 14:00:00', '2025-04-09 15:00:00', 'Cancelled', 'WEEKLY', 'We,Fr'),
('hxminh', 12, 'Moonlight Weekly', 'khanhvu, bkkhanh, ntdu', 'Họp cập nhật tiến độ Moon', '2025-04-11 14:00:00', '2025-04-11 15:00:00', 'Cancelled', 'WEEKLY', 'We,Fr'),
('hxminh', 12, 'Moonlight Weekly', 'khanhvu, bkkhanh, ntdu', 'Họp cập nhật tiến độ Moon', '2025-04-16 14:00:00', '2025-04-16 15:00:00', 'Confirmed', 'WEEKLY', 'We,Fr'),
('hxminh', 12, 'Moonlight Weekly', 'khanhvu, bkkhanh, ntdu', 'Họp cập nhật tiến độ Moon', '2025-04-18 14:00:00', '2025-04-18 15:00:00', 'Confirmed', 'WEEKLY', 'We,Fr'),
-- WEEKLY: Họp mỗi thứ Ba và thứ Năm từ ngày 22/04 đến 29/04
('pnminh', 2, 'Dev Team Weekly', 'pnminh, nhtien1', 'Lập kế hoạch sprint', '2025-04-22 16:00:00', '2025-04-22 17:00:00', 'Confirmed', 'WEEKLY', 'Tu,Th'),
('pnminh', 2, 'Dev Team Weekly', 'pnminh, nhtien1', 'Lập kế hoạch sprint', '2025-04-24 16:00:00', '2025-04-24 17:00:00', 'Confirmed', 'WEEKLY', 'Tu,Th'),
('pnminh', 2, 'Dev Team Weekly', 'pnminh, nhtien1', 'Lập kế hoạch sprint', '2025-04-29 16:00:00', '2025-04-29 17:00:00', 'Confirmed', 'WEEKLY', 'Tu,Th'),
-- WEEKLY: Họp mỗi thứ Ba và thứ Năm từ ngày 22/04 đến 29/04
('nvhung', 7, 'Dev Team Weekly', 'pnminh, nhtien1, nvhung, ntdu', 'Lập kế hoạch sprint', '2025-04-22 14:00:00', '2025-04-22 15:00:00', 'Confirmed', 'WEEKLY', 'Tu,Th'),
('nvhung', 7, 'Dev Team Weekly', 'pnminh, nhtien1, nvhung, ntdu', 'Lập kế hoạch sprint', '2025-04-24 14:00:00', '2025-04-24 15:00:00', 'Confirmed', 'WEEKLY', 'Tu,Th'),
('nvhung', 7, 'Dev Team Weekly', 'pnminh, nhtien1, nvhung, ntdu', 'Lập kế hoạch sprint', '2025-04-29 14:00:00', '2025-04-29 15:00:00', 'Confirmed', 'WEEKLY', 'Tu,Th'),
-- WEEKLY: Họp mỗi thứ Ba và thứ Năm từ ngày 10/05 đến 18/05
('pnminh', 8, 'Dev Team Weekly', 'pnminh, htbao', 'Lập kế hoạch sprint', '2025-05-10 14:00:00', '2025-05-10 15:00:00', 'Confirmed', 'WEEKLY', 'Tu,Th'),
('pnminh', 8, 'Dev Team Weekly', 'pnminh, htbao', 'Lập kế hoạch sprint', '2025-05-12 14:00:00', '2025-05-12 15:00:00', 'Confirmed', 'WEEKLY', 'Tu,Th'),
('pnminh', 8, 'Dev Team Weekly', 'pnminh, htbao', 'Lập kế hoạch sprint', '2025-05-17 14:00:00', '2025-05-17 15:00:00', 'Confirmed', 'WEEKLY', 'Tu,Th'),
-- WEEKLY: Họp mỗi thứ Ba và thứ Năm từ ngày 22/04 đến 29/04
('pnminh', 4, 'Dev Team Weekly', 'pnminh, dtquynh', 'Lập kế hoạch sprint', '2025-04-22 14:00:00', '2025-04-22 15:00:00', 'Confirmed', 'WEEKLY', 'Tu,Th'),
('pnminh', 4, 'Dev Team Weekly', 'pnminh, dtquynh', 'Lập kế hoạch sprint', '2025-04-24 14:00:00', '2025-04-24 15:00:00', 'Confirmed', 'WEEKLY', 'Tu,Th'),
('pnminh', 4, 'Dev Team Weekly', 'pnminh, dtquynh', 'Lập kế hoạch sprint', '2025-04-29 14:00:00', '2025-04-29 15:00:00', 'Confirmed', 'WEEKLY', 'Tu,Th'),
-- Weekly từ ngày 01/03/2025 đến ngày 04/03/2025 (Mo, Tu) đặt ngày 03, 04
('pnminh', 5, 'Weekly HR Meeting', 'pnminh, nhtien1, pmhao', 'Họp phòng nhân sự hằng tuần', '2025-03-03 14:00:00', '2025-03-03 15:00:00', 'Cancelled', 'WEEKLY', 'Mo,Tu'),
('pnminh', 5, 'Weekly HR Meeting', 'pnminh, nhtien1, pmhao', 'Họp phòng nhân sự hằng tuần', '2025-03-04 15:00:00', '2025-03-04 16:00:00', 'Cancelled', 'WEEKLY', 'Mo,Tu'),
-- Weekly từ ngày 01/03/2025 đến ngày 04/03/2025 (Mo, Tu) đặt ngày 03, 04
('ltnga', 6, 'Weekly HR Meeting', 'ltnga, nhtien1, pmhao', 'Họp phòng nhân sự hằng tuần', '2025-03-03 14:00:00', '2025-03-03 15:00:00', 'Cancelled', 'WEEKLY', 'Mo,Tu'),
('ltnga', 6, 'Weekly HR Meeting', 'ltnga, nhtien1, pmhao', 'Họp phòng nhân sự hằng tuần', '2025-03-04 14:00:00', '2025-03-04 15:00:00', 'Cancelled', 'WEEKLY', 'Mo,Tu'),
-- Weekly từ ngày 01/03/2025 đến ngày 04/03/2025 (Mo, Tu) đặt ngày 03, 04
('htbao', 10, 'Weekly HR Meeting', 'htbao, nhtien1, pmhao', 'Họp phòng nhân sự hằng tuần', '2025-03-03 14:00:00', '2025-03-03 15:00:00', 'Cancelled', 'WEEKLY', 'Mo,Tu'),
('htbao', 10, 'Weekly HR Meeting', 'htbao, nhtien1, pmhao', 'Họp phòng nhân sự hằng tuần', '2025-03-04 14:00:00', '2025-03-04 15:00:00', 'Cancelled', 'WEEKLY', 'Mo,Tu'),
-- Weekly từ ngày 01/03/2025 đến ngày 04/03/2025 (Mo, Tu) đặt ngày 03, 04
('dtquynh', 1, 'Weekly HR Meeting', 'nhtien1, pmhao, dtquynh', 'Họp phòng nhân sự hằng tuần', '2025-03-03 11:00:00', '2025-03-03 12:00:00', 'Cancelled', 'WEEKLY', 'Mo,Tu'),
('dtquynh', 1, 'Weekly HR Meeting', 'nhtien1, pmhao, dtquynh', 'Họp phòng nhân sự hằng tuần', '2025-03-04 11:00:00', '2025-03-04 12:00:00', 'Cancelled', 'WEEKLY', 'Mo,Tu'),
-- Weekly từ ngày 01/03/2025 đến ngày 04/03/2025 (Mo, Tu) đặt ngày 03, 04
('dtquynh', 2, 'Weekly HR Meeting', 'dnphuc, dtquynh, nhtien1, pmhao', 'Họp phòng nhân sự hằng tuần', '2025-03-03 08:00:00', '2025-03-03 09:00:00', 'Cancelled', 'WEEKLY', 'Mo,Tu'),
('dtquynh', 3, 'Weekly HR Meeting', 'dnphuc, dtquynh, nhtien1, pmhao', 'Họp phòng nhân sự hằng tuần', '2025-03-04 08:00:00', '2025-03-04 09:00:00', 'Cancelled', 'WEEKLY', 'Mo,Tu'),
-- Weekly từ ngày 01/03/2025 đến ngày 04/03/2025 (Mo, Tu) đặt ngày 03, 04
('dnphuc', 5, 'Weekly HR Meeting', 'nhtien1, pmhao, dnphuc', 'Họp phòng nhân sự hằng tuần', '2025-03-03 14:00:00', '2025-03-03 15:00:00', 'Cancelled', 'WEEKLY', 'Mo,Tu'),
('dnphuc', 5, 'Weekly HR Meeting', 'nhtien1, pmhao, dnphuc', 'Họp phòng nhân sự hằng tuần', '2025-03-04 14:00:00', '2025-03-04 15:00:00', 'Cancelled', 'WEEKLY', 'Mo,Tu');

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