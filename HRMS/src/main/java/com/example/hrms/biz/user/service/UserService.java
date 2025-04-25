package com.example.hrms.biz.user.service;

import com.example.hrms.biz.user.model.User;
import com.example.hrms.biz.user.model.criteria.UserCriteria;
import com.example.hrms.biz.user.model.dto.UserDTO;
import com.example.hrms.enumation.RoleEnum;
import com.example.hrms.biz.user.repository.UserMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserByUsername(String username) {
        return userMapper.getUserByUsername(username);
    }

    public List<User> getAllUsers() {
        return userMapper.getAllUsers();
    }

    @Transactional
    public int insertUser(User user) {
        // Mã hóa password trước khi lưu
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userMapper.insertUser(user);
    }

    @Transactional
    public int updateUser(User user) {
        return userMapper.updateUser(user);
    }
    @Transactional
    public int deleteUser(String username) {
        return userMapper.deleteUser(username);
    }

    public int count(UserCriteria criteria) {
        return userMapper.count(criteria);
    }

    public List<UserDTO.Resp> list(UserCriteria criteria) {
        return userMapper.searchUsers(criteria.getDepartmentIds(), criteria.getRoles())
                .stream()
                .map(this::convertToUserResp)
                .collect(Collectors.toList());
    }


    public boolean isUsernameDuplicated(String username) {
        return checkUsernameExists(username) > 0;
    }

    public RoleEnum getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role) // bỏ "ROLE_"
                .findFirst()
                .map(role -> {
                    try {
                        return RoleEnum.valueOf(role.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .orElse(null);
    }


    private UserDTO.Resp convertToUserResp(User user) {
        UserDTO.Resp resp = new UserDTO.Resp();
        resp.setUsername(user.getUsername());
        resp.setEmployeeName(user.getEmployeeName());
        resp.setEmail(user.getEmail());
        resp.setDepartmentId(user.getDepartmentId());
        resp.setDepartmentName(user.getDepartmentName());
        resp.setRoleName(user.getRoleName());
        resp.setStatus(user.getStatus());
        return resp;
    }


    public int checkUsernameExists(String username) {
        return userMapper.checkUsernameExists(username);
    }

    public List<UserDTO.Resp> findAllUsersInDepartment(String supervisorUsername) {
        try {
            // Lấy department ID của supervisor
            String departmentId = userMapper.getDepartmentIdBySupervisor(supervisorUsername);

            // Lấy tất cả người dùng trong phòng ban đó
            List<User> users = userMapper.getUsersByDepartment(departmentId);

            // Trả về danh sách người dùng đã chuyển đổi
            return users.stream().map(this::convertToUserResp).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Could not fetch users, please try again later.");
        }
    }
}