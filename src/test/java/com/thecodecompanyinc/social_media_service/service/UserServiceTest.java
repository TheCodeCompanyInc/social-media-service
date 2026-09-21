package com.thecodecompanyinc.social_media_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thecodecompanyinc.social_media_service.dto.auth.GoogleLoginDto;
import com.thecodecompanyinc.social_media_service.dto.auth.UpdateUserDto;
import com.thecodecompanyinc.social_media_service.entity.Role;
import com.thecodecompanyinc.social_media_service.entity.User;
import com.thecodecompanyinc.social_media_service.exception.ResourceAlreadyExistsException;
import com.thecodecompanyinc.social_media_service.repository.UserRepository;
import com.thecodecompanyinc.social_media_service.service.jwt.JwtService;
import com.thecodecompanyinc.social_media_service.service.user.UserServiceImpl;

/** Unit tests for UserService Tests user management operations */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private String authHeader;
    private GoogleLoginDto googleLoginDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("test");
        user.setPasswordHash("hashedPassword");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhoneNumber("1234567890");
        user.setRole(Role.CLIENT);
        user.setEmailVerified(true);

        authHeader = "Bearer valid-jwt-token";

        googleLoginDto = new GoogleLoginDto();
        googleLoginDto.setEmail("google@example.com");
        googleLoginDto.setName("John Doe");
        googleLoginDto.setGivenName("John");
        googleLoginDto.setFamilyName("Doe");
    }

    /** SAVE USER TESTS */
    @Test
    @DisplayName("Should save user successfully")
    void shouldSaveUserSuccessfully() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        // Act
        User result = userService.saveUser(user);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(user.getEmail());

        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act & Assert
        assertThatThrownBy(() -> userService.saveUser(user))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("Email Already Exists!");

        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should save user with different email")
    void shouldSaveUserWithDifferentEmail() {
        // Arrange
        User newUser = new User();
        newUser.setEmail("different@example.com");
        newUser.setFirstName("Jane");

        when(userRepository.findByEmail("different@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(newUser)).thenReturn(newUser);

        // Act
        User result = userService.saveUser(newUser);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("different@example.com");
    }

    /** GOOGLE LOGIN TESTS */
    @Test
    @DisplayName("Should return existing user without creating a new one")
    void shouldReturnExistingGoogleUser() {
        // Arrange
        User existingUser = new User();
        existingUser.setEmail(googleLoginDto.getEmail());

        when(userRepository.findByEmail(googleLoginDto.getEmail()))
                .thenReturn(Optional.of(existingUser));

        // Act
        User result = userService.findOrCreateGoogleUser(googleLoginDto);

        // Assert
        assertThat(result).isEqualTo(existingUser);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should create and save new user when Google user does not exist")
    void shouldCreateNewGoogleUser() {
        // Arrange
        when(userRepository.findByEmail(googleLoginDto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        User result = userService.findOrCreateGoogleUser(googleLoginDto);

        // Assert
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo(googleLoginDto.getEmail());
        assertThat(saved.getFirstName()).isEqualTo("John");
        assertThat(saved.getLastName()).isEqualTo("Doe");
        assertThat(saved.getRole()).isEqualTo(Role.CLIENT);
        assertThat(saved.isEmailVerified()).isTrue();
        assertThat(saved.getPasswordHash()).isEqualTo("N/A");
        assertThat(saved.getPhoneNumber()).isEqualTo("N/A");
        assertThat(saved.getJoin_date()).isNotNull();
    }

    @Test
    @DisplayName("Should use full name as first name when given name is null")
    void shouldUseFullNameWhenGivenNameIsNull() {
        // Arrange
        GoogleLoginDto dto = new GoogleLoginDto();
        dto.setEmail("google@example.com");
        dto.setName("Full Name");
        dto.setGivenName(null);
        dto.setFamilyName(null);

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        userService.findOrCreateGoogleUser(dto);

        // Assert
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getFirstName()).isEqualTo("Full Name");
        assertThat(saved.getLastName()).isEqualTo("");
    }

    @Test
    @DisplayName("Should use empty string as last name when family name is null")
    void shouldUseEmptyStringWhenFamilyNameIsNull() {
        // Arrange
        GoogleLoginDto dto = new GoogleLoginDto();
        dto.setEmail("google@example.com");
        dto.setName("John");
        dto.setGivenName("John");
        dto.setFamilyName(null);

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        userService.findOrCreateGoogleUser(dto);

        // Assert
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertThat(captor.getValue().getLastName()).isEqualTo("");
    }

    /** GET USER TESTS */
    @Test
    @DisplayName("Should get user by email successfully")
    void shouldGetUserByEmail() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.getUser(user.getEmail());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(user.getEmail());

        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void shouldReturnEmptyWhenUserNotFoundByEmail() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUser("nonexistent@example.com");

        // Assert
        assertThat(result).isEmpty();

        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    /** GET USER BY JWT TESTS */
    @Test
    @DisplayName("Should get user by JWT successfully")
    void shouldGetUserByJwtSuccessfully() {
        // Arrange
        when(jwtService.validateToken("valid-jwt-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("valid-jwt-token")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        User result = userService.getUserByJwt(authHeader);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(user.getEmail());

        verify(jwtService, times(1)).validateToken("valid-jwt-token");
        verify(jwtService, times(1)).getEmailFromToken("valid-jwt-token");
        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    @DisplayName("Should throw exception with invalid token")
    void shouldThrowExceptionWithInvalidToken() {
        // Arrange
        when(jwtService.validateToken("invalid-token")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserByJwt("Bearer invalid-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token is not valid");

        verify(jwtService, times(1)).validateToken("invalid-token");
        verify(jwtService, never()).getEmailFromToken(anyString());
    }

    @Test
    @DisplayName("Should throw exception with null auth header")
    void shouldThrowExceptionWithNullAuthHeader() {
        // Act & Assert
        assertThatThrownBy(() -> userService.getUserByJwt(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token is invalid");

        verify(jwtService, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("Should throw exception with malformed auth header")
    void shouldThrowExceptionWithMalformedAuthHeader() {
        // Act & Assert
        assertThatThrownBy(() -> userService.getUserByJwt("InvalidFormat"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token is invalid");

        verify(jwtService, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("Should throw exception when user not found in database")
    void shouldThrowExceptionWhenUserNotFoundInDatabase() {
        // Arrange
        when(jwtService.validateToken("valid-jwt-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("valid-jwt-token")).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserByJwt(authHeader))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    /** GET USER BY ID TESTS */
    @Test
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserByIdSuccessfully() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository, times(1)).findById(999L);
    }

    /** GET ALL USERS TESTS */
    @Test
    @DisplayName("Should get all users successfully")
    void shouldGetAllUsersSuccessfully() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");

        List<User> users = Arrays.asList(user, user2);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getUsers();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(User::getEmail)
                .containsExactly("test@example.com", "user2@example.com");

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no users exist")
    void shouldReturnEmptyListWhenNoUsersExist() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of());

        // Act
        List<User> result = userService.getUsers();

        // Assert
        assertThat(result).isEmpty();

        verify(userRepository, times(1)).findAll();
    }

    /** UPDATE USER TESTS */
    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() throws ParseException {
        // Arrange
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setFirstName("Jane");
        updateDto.setLastName("Smith");
        updateDto.setDob("15-06-1990");

        when(jwtService.validateToken("valid-jwt-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("valid-jwt-token")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        User result = userService.updateUser(authHeader, updateDto);

        // Assert
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getDob()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception with invalid date format")
    void shouldThrowExceptionWithInvalidDateFormat() {
        // Arrange
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setFirstName("Jane");
        updateDto.setLastName("Smith");
        updateDto.setDob("invalid-date");

        when(jwtService.validateToken("valid-jwt-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("valid-jwt-token")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(authHeader, updateDto))
                .isInstanceOf(ParseException.class);
    }

    @Test
    @DisplayName("Should preserve other fields when updating user")
    void shouldPreserveOtherFieldsWhenUpdatingUser() throws ParseException {
        // Arrange
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setFirstName("Jane");
        updateDto.setLastName("Smith");
        updateDto.setDob("15-06-1990");

        String originalEmail = user.getEmail();
        String originalPhone = user.getPhoneNumber();
        Role originalRole = user.getRole();

        when(jwtService.validateToken("valid-jwt-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("valid-jwt-token")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        User result = userService.updateUser(authHeader, updateDto);

        // Assert - Other fields should remain unchanged
        assertThat(result.getEmail()).isEqualTo(originalEmail);
        assertThat(result.getPhoneNumber()).isEqualTo(originalPhone);
        assertThat(result.getRole()).isEqualTo(originalRole);
    }

    /** UPDATE USER PHOTO TESTS */
    @Test
    @DisplayName("Should update user photo successfully")
    void shouldUpdateUserPhotoSuccessfully() {
        // Arrange
        String photoUrl = "https://example.com/photo.jpg";

        when(jwtService.validateToken("valid-jwt-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("valid-jwt-token")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        userService.updateUserPhoto(authHeader, photoUrl);

        // Assert
        assertThat(user.getImageUrl()).isEqualTo(photoUrl);
    }

    @Test
    @DisplayName("Should overwrite existing photo URL")
    void shouldOverwriteExistingPhotoUrl() {
        // Arrange
        user.setImageUrl("https://example.com/old-photo.jpg");
        String newPhotoUrl = "https://example.com/new-photo.jpg";

        when(jwtService.validateToken("valid-jwt-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("valid-jwt-token")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        userService.updateUserPhoto(authHeader, newPhotoUrl);

        // Assert
        assertThat(user.getImageUrl()).isEqualTo(newPhotoUrl);
    }

    /** UPDATE FCM TOKEN TESTS */
    @Test
    @DisplayName("Should update FCM token successfully")
    void shouldUpdateFcmTokenSuccessfully() {
        // Arrange
        String fcmToken = "fcm-token-123";
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        userService.updateFcmToken(user.getEmail(), fcmToken);

        // Assert
        assertThat(user.getFirebaseToken()).isEqualTo(fcmToken);

        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    @DisplayName("Should throw exception when updating FCM token for non-existent user")
    void shouldThrowExceptionWhenUpdatingFcmTokenForNonExistentUser() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateFcmToken("nonexistent@example.com", "token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("user not found");

        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("Should overwrite existing FCM token")
    void shouldOverwriteExistingFcmToken() {
        // Arrange
        user.setFirebaseToken("old-token");
        String newToken = "new-token";
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        userService.updateFcmToken(user.getEmail(), newToken);

        // Assert
        assertThat(user.getFirebaseToken()).isEqualTo(newToken);
    }

    @Test
    @DisplayName("Should handle null FCM token")
    void shouldHandleNullFcmToken() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        userService.updateFcmToken(user.getEmail(), null);

        // Assert
        assertThat(user.getFirebaseToken()).isNull();
    }

    @Test
    @DisplayName("Should handle empty FCM token")
    void shouldHandleEmptyFcmToken() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        userService.updateFcmToken(user.getEmail(), "");

        // Assert
        assertThat(user.getFirebaseToken()).isEmpty();
    }
}
