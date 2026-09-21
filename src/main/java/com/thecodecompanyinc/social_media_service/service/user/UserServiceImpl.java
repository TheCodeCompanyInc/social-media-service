package com.thecodecompanyinc.social_media_service.service.user;

import com.thecodecompanyinc.social_media_service.dto.auth.GoogleLoginDto;
import com.thecodecompanyinc.social_media_service.dto.auth.UpdateUserDto;
import com.thecodecompanyinc.social_media_service.entity.Role;
import com.thecodecompanyinc.social_media_service.entity.User;
import com.thecodecompanyinc.social_media_service.exception.ResourceAlreadyExistsException;
import com.thecodecompanyinc.social_media_service.exception.ResourceNotFoundException;
import com.thecodecompanyinc.social_media_service.repository.UserRepository;
import com.thecodecompanyinc.social_media_service.service.jwt.JwtService;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public User saveUser(User user) {
        log.info("Saving user with email: {}", user.getEmail());
        Optional<User> userOptional = userRepository.findByEmail(user.getEmail());
        if (userOptional.isPresent()) {
            log.warn("Email already exists: {}", user.getEmail());
            throw new ResourceAlreadyExistsException("Email Already Exists!");
        }

        User savedUser = userRepository.save(user);
        log.info("User saved successfully with id: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    public User findOrCreateGoogleUser(GoogleLoginDto dto) {
        log.info("Finding or creating Google user with email: {}", dto.getEmail());
        return getUser(dto.getEmail())
                .orElseGet(
                        () -> {
                            log.info("Creating new Google user with email: {}", dto.getEmail());
                            User newUser = new User();
                            newUser.setEmail(dto.getEmail());
                            newUser.setFirstName(
                                    dto.getGivenName() != null
                                            ? dto.getGivenName()
                                            : dto.getName());
                            newUser.setLastName(
                                    dto.getFamilyName() != null ? dto.getFamilyName() : "");
                            newUser.setRole(Role.CLIENT);
                            newUser.setPasswordHash("N/A");
                            newUser.setPhoneNumber("N/A");
                            newUser.setJoin_date(new Timestamp(new java.util.Date().getTime()));
                            newUser.setEmailVerified(true);
                            return saveUser(newUser);
                        });
    }

    @Override
    public Optional<User> getUser(String email) {
        log.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    public User getUserByJwt(String authHeader) {
        log.debug("Getting user by JWT token");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            final String token = authHeader.substring(7);
            if (jwtService.validateToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                log.debug("Token validated, fetching user with email: {}", email);
                return userRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () -> {
                                    log.error("User not found for email: {}", email);
                                    return new ResourceNotFoundException("User not found");
                                });
            }
            log.warn("Token validation failed");
            throw new IllegalArgumentException("Token is not valid");
        }
        log.warn("Invalid authorization header");
        throw new IllegalArgumentException("Token is invalid");
    }

    @Override
    public User getUserById(Long id) {
        log.debug("Fetching user by id: {}", id);
        return userRepository
                .findById(id)
                .orElseThrow(
                        () -> {
                            log.error("User not found with id: {}", id);
                            return new ResourceNotFoundException("User not found");
                        });
    }

    @Override
    public User getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);

        return userRepository
                .findByEmail(email)
                .orElseThrow(
                        () -> {
                            log.error("User not found with email: {}", email);
                            return new ResourceNotFoundException("User not found");
                        });
    }

    @Override
    public List<User> getUsers() {
        log.debug("Fetching all users");
        List<User> users = userRepository.findAll();
        log.debug("Found {} users", users.size());
        return users;
    }

    @Override
    public long countUsers() {
        return userRepository.count();
    }

    @Override
    public long countUsersByRole(Role role) {
        return userRepository.countByRole(role);
    }

    @Override
    @Transactional
    public User updateUser(String authHeader, UpdateUserDto updateUserDto) throws ParseException {
        log.info("Updating user profile");
        User user = getUserByJwt(authHeader);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        if (updateUserDto.getFirstName() != null) {
            user.setFirstName(updateUserDto.getFirstName());
        }
        if (updateUserDto.getLastName() != null) {
            user.setLastName(updateUserDto.getLastName());
        }
        if (updateUserDto.getDob() != null) {
            user.setDob(new Date(simpleDateFormat.parse(updateUserDto.getDob()).getTime()));
        }

        log.info("User profile updated successfully for user id: {}", user.getId());
        return user;
    }

    @Override
    @Transactional
    public void updateUserPhoto(String authHeader, String url) {
        log.info("Updating user photo");
        User user = getUserByJwt(authHeader);

        user.setImageUrl(url);
        log.info(
                "User photo updated successfully for user id: {} on thread: {}",
                user.getId(),
                Thread.currentThread().getName());
    }

    @Override
    @Transactional
    public void updateFcmToken(String email, String fcmToken) {
        log.info("Updating FCM token for user with email: {}", email);
        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () -> {
                                    log.error("User not found with email: {}", email);
                                    return new ResourceNotFoundException("user not found");
                                });
        user.setFirebaseToken(fcmToken);
        log.info("FCM token updated successfully for user id: {}", user.getId());
    }

    @Override
    @Transactional
    public User updateUserRole(Long userId, Role newRole) {
        log.info("Updating role for user id: {} to {}", userId, newRole);
        User user = getUserById(userId);
        user.setRole(newRole);
        User saved = userRepository.save(user);
        log.info("Role updated successfully for user id: {}", userId);
        return saved;
    }

    @Override
    @Transactional
    public User updateEmailVerified(Long userId, boolean verified) {
        log.info("Updating emailVerified for user id: {} to {}", userId, verified);
        User user = getUserById(userId);
        user.setEmailVerified(verified);
        User saved = userRepository.save(user);
        log.info("emailVerified updated successfully for user id: {}", userId);
        return saved;
    }
}
