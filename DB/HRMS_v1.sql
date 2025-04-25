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
    role_id BIGINT NOT NULL,
    FOREIGN KEY (role_id) REFERENCES Roles(role_id),
    INDEX (department_name),
    INDEX (role_id)
);

-- Bảng Quản lý Người Dùng (Users)
CREATE TABLE Users (
    username VARCHAR(64) PRIMARY KEY,
    password VARCHAR(256) NOT NULL, 
    department_id BIGINT,
    role VARCHAR(64) NOT NULL,
    is_supervisor BOOLEAN DEFAULT FALSE,
    status VARCHAR(64) NOT NULL,
    FOREIGN KEY (department_id) REFERENCES Departments(department_id),
    UNIQUE (username),
    INDEX (department_id),
    INDEX (role),
    INDEX (status)
);

-- Bảng Yêu Cầu (Requests)
CREATE TABLE Requests (
    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    department_id BIGINT NOT NULL,
    request_type VARCHAR(64) NOT NULL,
    request_reason TEXT NOT NULL,
    request_status VARCHAR(64) NOT NULL,
    approver_username VARCHAR(64) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    approved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (username) REFERENCES Users(username),
    FOREIGN KEY (approver_username) REFERENCES Users(username),
    INDEX (username),
    INDEX (request_status),
    INDEX (start_time),
    INDEX (end_time),
    INDEX (approver_username)
);
INSERT INTO Requests (username, department_id, request_type, request_reason, request_status, approver_username, start_time, end_time) VALUES
('Trung Du Nguyen', 7, 'PAID_LEAVE', 'Nghỉ phép cá nhân', 'REJECTED', 'Minh Hao Pham', '2025-02-28 09:00:00', '2025-02-28 17:00:00'),
('Minh Hao Pham', 6, 'UNPAID_LEAVE', 'Nghỉ phép đột xuất', 'APPROVED', 'Khac Khanh Bui', '2025-02-27 09:00:00', '2025-02-27 17:00:00'),
('Trung Du Nguyen', 5, 'UNPAID_LEAVE', 'Làm việc từ xa do bệnh', 'REJECTED', 'Minh Hao Pham', '2025-02-26 09:00:00', '2025-02-26 17:00:00'),
('Khac Khanh Bui', 4, 'PAID_LEAVE', 'Xin nghỉ phép do việc gia đình', 'REJECTED', 'Khac Khanh Bui', '2025-02-25 09:00:00', '2025-02-25 17:00:00'),
('Nhat Minh Pham', 3, 'PAID_LEAVE', 'Làm việc tại nhà', 'APPROVED', 'Minh Hao Pham', '2025-02-24 09:00:00', '2025-02-24 17:00:00');
-- request_type phải là PAID_LEAVE,UNPAID_LEAVE và request_type là REJECTED,APPROVED
select * from Requests;
DELETE FROM Requests;
SET SQL_SAFE_UPDATES = 0;
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
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status VARCHAR(64) DEFAULT 'Requested',
    FOREIGN KEY (username) REFERENCES Users(username),
    FOREIGN KEY (room_id) REFERENCES Meeting_Rooms(room_id),
    INDEX (username),
    INDEX (room_id),
    INDEX (start_time),
    INDEX (end_time),
    INDEX (status)
);

-- 2. CHÈN DỮ LIỆU MẪU
-- ==============================================
-- Chèn dữ liệu vào bảng Roles
INSERT INTO Roles (role_name) VALUES
('employee'),
('supervisor'),
('admin');

-- Chèn dữ liệu vào bảng Departments
INSERT INTO Departments (department_name, role_id) VALUES
('HR', 1),
('Finance', 2),
('IT', 3);

-- Chèn dữ liệu vào bảng Users với mật khẩu đã mã hóa
INSERT INTO Users (username, password, role, department_id, is_supervisor, status) VALUES -- 123456
('Trung Du Nguyen', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 1, 1, FALSE, 'Active'),
('Minh Hao Pham', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 2, 1, TRUE, 'Active'),
('Khac Khanh Bui', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 3, 2, TRUE, 'Active'),
('Nhat Minh Pham', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 1, 3, FALSE, 'Inactive'),
('Huu Tien Pham', '$2a$10$Cx7mPooZBiruz8YjaOkhTu1dlfMlHN9T5IFM8wnOp.KQTd5xzEL4q', 2, 2, FALSE, 'Active');

-- Chèn dữ liệu vào bảng Meeting_Rooms
INSERT INTO Meeting_Rooms (room_name, location, capacity) VALUES
('Sky Room', 'Floor 1', 10),
('Star Room', 'Floor 2', 15),
('Admin Room', 'Floor 3', 20);

-- Chèn dữ liệu vào bảng Bookings
INSERT INTO Bookings (username, room_id, start_time, end_time, status) VALUES
('Trung Du Nguyen', 1, '2025-03-01 10:00:00', '2025-03-01 12:00:00', 'Requested'),
('Minh Hao Pham', 2, '2025-03-02 14:00:00', '2025-03-02 16:00:00', 'Confirmed'),
('Khac Khanh Bui', 3, '2025-03-03 09:00:00', '2025-03-03 11:00:00', 'Cancelled'),
('Nhat Minh Pham', 1, '2025-03-04 13:00:00', '2025-03-04 15:00:00', 'Requested'),
('Huu Tien Pham', 2, '2025-03-05 08:00:00', '2025-03-05 10:00:00', 'Confirmed');
SELECT 
    r.request_id AS requestId, 
    r.username AS username, 
    r.department_id AS departmentId, 
    r.request_type AS requestType, 
    r.request_reason AS requestReason, 
    r.request_status AS requestStatus, 
    r.approver_username AS approverUsername, 
    r.start_time AS startTime, 
    r.end_time AS endTime, 
    r.created_at AS createdAt, 
    r.updated_at AS updatedAt, 
    r.approved_at AS approvedAt 
FROM 
    Requests r 
LEFT JOIN 
    Departments d ON r.department_id = d.department_id 
LEFT JOIN 
    Users u ON r.username = u.username 
LEFT JOIN 
    Roles ro ON d.role_id = ro.role_id 
ORDER BY 
    r.request_id ASC;