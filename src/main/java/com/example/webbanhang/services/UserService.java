package com.example.webbanhang.services;

import com.example.webbanhang.components.JwtTokenUtils;
import com.example.webbanhang.components.LocalizationUtils;
import com.example.webbanhang.dtos.UpdateUserDTO;
import com.example.webbanhang.dtos.UserDTO;
import com.example.webbanhang.exceptions.DataNotFoundException;
import com.example.webbanhang.exceptions.InvalidPasswordException;
import com.example.webbanhang.exceptions.PremissionDenyException;
import com.example.webbanhang.models.Role;
import com.example.webbanhang.models.Token;
import com.example.webbanhang.models.User;
import com.example.webbanhang.repositories.RoleRepository;
import com.example.webbanhang.repositories.UserRepository;
import com.example.webbanhang.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{
    @Autowired
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtils jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final LocalizationUtils localizationUtils;
//    @Override
//    public User getUserDetailsFromRefreshToken(String refreshToken) throws Exception {
//        Token existingToken = tokenRepository.findByRefreshToken(refreshToken);
//        return getUserDetailsFromToken(existingToken.getToken());
//    }

    @Override
    public Page<User> findAll(String keyword, Pageable pageable) throws Exception {
        return userRepository.findAll(keyword, pageable);
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) throws InvalidPasswordException, DataNotFoundException {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        String encodedPassword = passwordEncoder.encode(newPassword);
        existingUser.setPassword(encodedPassword);
        userRepository.save(existingUser);
        //reset password => clear token
    }

    @Override
    @Transactional
    public void blockOrEnable(Long userId, Boolean active) throws DataNotFoundException {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        existingUser.setActive(active);
        userRepository.save(existingUser);
    }
    @Override
    @Transactional
    public User createUser(UserDTO userDTO) throws Exception {
        //register user
        String phoneNumber = userDTO.getPhoneNumber();
        // Kiểm tra xem số điện thoại đã tồn tại hay chưa
        if(userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DataIntegrityViolationException("Phone number already exists");
        }
        Role role =roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new DataNotFoundException(
                        localizationUtils.getLocalizedMessage(MessageKeys.ROLE_DOES_NOT_EXISTS)));
        if(role.getName().toUpperCase().equals(Role.ADMIN)) {
            throw new PremissionDenyException("You cannot register an admin account");
        }
        //convert from userDTO => user
        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(userDTO.getPhoneNumber())
                .password(userDTO.getPassword())
                .address(userDTO.getAddress())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId(userDTO.getFacebookAccountId())
                .googleAccountId(String.valueOf(userDTO.getGoogleAccountId()))
                .active(true)
                .build();

        newUser.setRole(role);

        // Kiểm tra nếu có accountId, không yêu cầu password
        if (userDTO.getFacebookAccountId() == 0 && userDTO.getGoogleAccountId() == 0) {
            String password = userDTO.getPassword();
            String encodedPassword = passwordEncoder.encode(password);
            newUser.setPassword(encodedPassword);
        }
        return userRepository.save(newUser);
    }

    public User findByGoogleAccountId(String googleAccountId) {
        // Tìm người dùng theo googleAccountId
        return userRepository.findByGoogleAccountId(googleAccountId).orElse(null);
    }


    public void createGoogleUser(String googleAccountId, String fullName, String email) {
        // Tạo người dùng mới nếu chưa tồn tại
        User newUser = new User();
        newUser.setGoogleAccountId(googleAccountId);
        newUser.setFullName(fullName);
        newUser.setPhoneNumber(email);  // Bạn có thể dùng email hoặc thêm một trường phoneNumber riêng biệt
        newUser.setActive(true);  // Đặt người dùng thành active
        newUser.setPassword("google_login_password");  // Tạo mật khẩu mặc định
        newUser.setRole(new Role());  // Đặt vai trò người dùng mặc định, có thể thay đổi sau


        // Lưu người dùng vào cơ sở dữ liệu
        userRepository.save(newUser);
    }
    public User registerOrGetGoogleUser(String googleAccountId, String fullName, String email) {
        // Kiểm tra xem người dùng đã tồn tại trong cơ sở dữ liệu chưa
        Optional<User> existingUser = userRepository.findByGoogleAccountId(googleAccountId);

        if (existingUser.isPresent()) {
            // Nếu tài khoản đã tồn tại, trả về người dùng hiện có
            return existingUser.get();
        } else {
            // Nếu tài khoản chưa tồn tại, tạo tài khoản mới
            User newUser = new User();
            newUser.setGoogleAccountId(googleAccountId);
            newUser.setFullName(fullName);
            newUser.setPhoneNumber(email);  // Có thể sử dụng email như phoneNumber, hoặc bạn có thể tạo một trường riêng cho email
            newUser.setActive(true);
            newUser.setPassword(new BCryptPasswordEncoder().encode("google_login_password"));  // Mật khẩu mặc định
            newUser.setRole(new Role());  // Thiết lập quyền mặc định cho người dùng mới
            // Lưu người dùng vào cơ sở dữ liệu
            return userRepository.save(newUser);
        }
    }
    @Override
    public String login(
            String phoneNumber,
            String password,
            Long roleId
    ) throws Exception {
        Optional<User> optionalUser = userRepository.findByPhoneNumber(phoneNumber);
        if(optionalUser.isEmpty()) {
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.WRONG_PHONE_PASSWORD));
        }
        //return optionalUser.get();//muốn trả JWT token ?
        User existingUser = optionalUser.get();
        //check password
        if (existingUser.getFacebookAccountId() == 0
                && Objects.equals(existingUser.getGoogleAccountId(), "")) {
            if(!passwordEncoder.matches(password, existingUser.getPassword())) {
                throw new BadCredentialsException(localizationUtils.getLocalizedMessage(MessageKeys.WRONG_PHONE_PASSWORD));
            }
        }
        Optional<Role> optionalRole = roleRepository.findById(roleId);
        if(optionalRole.isEmpty() || !roleId.equals(existingUser.getRole().getId())) {
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.ROLE_DOES_NOT_EXISTS));
        }
        if(!optionalUser.get().isActive()) {
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.USER_IS_LOCKED));
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                phoneNumber, password,
                existingUser.getAuthorities()
        );

        //authenticate with Java Spring security
        authenticationManager.authenticate(authenticationToken);
        return jwtTokenUtil.generateToken(existingUser);
    }
    @Transactional
    @Override
    public User updateUser(Long userId, UpdateUserDTO updatedUserDTO) throws Exception {
        // Find the existing user by userId
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        // Check if the phone number is being changed and if it already exists for another user
        String newPhoneNumber = updatedUserDTO.getPhoneNumber();
        if (!existingUser.getPhoneNumber().equals(newPhoneNumber) &&
                userRepository.existsByPhoneNumber(newPhoneNumber)) {
            throw new DataIntegrityViolationException("Phone number already exists");
        }

        // Update user information based on the DTO
        if (updatedUserDTO.getFullName() != null) {
            existingUser.setFullName(updatedUserDTO.getFullName());
        }
        if (newPhoneNumber != null) {
            existingUser.setPhoneNumber(newPhoneNumber);
        }
        if (updatedUserDTO.getAddress() != null) {
            existingUser.setAddress(updatedUserDTO.getAddress());
        }
        if (updatedUserDTO.getDateOfBirth() != null) {
            existingUser.setDateOfBirth(updatedUserDTO.getDateOfBirth());
        }
        if (updatedUserDTO.getFacebookAccountId() > 0) {
            existingUser.setFacebookAccountId(updatedUserDTO.getFacebookAccountId());
        }
        if (updatedUserDTO.getGoogleAccountId() > 0) {
            existingUser.setGoogleAccountId(String.valueOf(updatedUserDTO.getGoogleAccountId()));
        }

        // Update the password if it is provided in the DTO
        if (updatedUserDTO.getPassword() != null
                && !updatedUserDTO.getPassword().isEmpty()) {
            if(!updatedUserDTO.getPassword().equals(updatedUserDTO.getRetypePassword())) {
                throw new DataNotFoundException("Password and retype password not the same");
            }
            String newPassword = updatedUserDTO.getPassword();
            String encodedPassword = passwordEncoder.encode(newPassword);
            existingUser.setPassword(encodedPassword);
        }
        //existingUser.setRole(updatedRole);
        // Save the updated user
        return userRepository.save(existingUser);
    }

    @Override
    public User getUserDetailsFromToken(String token) throws Exception {
        if(jwtTokenUtil.isTokenExpired(token)) {
            throw new Exception("Token is expired");
        }
        String phoneNumber = jwtTokenUtil.extractPhoneNumber(token);
        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);

        if (user.isPresent()) {
            return user.get();
        } else {
            throw new Exception("User not found");
        }
    }
}

