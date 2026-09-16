package com.thecodecompanyinc.social_media_service.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.thecodecompanyinc.social_media_service.entity.Role;
import com.thecodecompanyinc.social_media_service.entity.User;

/**
 * NOTE: ALL THOSE TESTS ARE AI-GENERATED AND REVIEWED MANUALLY
 *
 * <p>Unit tests for UserRepository Uses real database (H2 in-memory or Testcontainers) Tests JPA
 * queries and database interactions
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("UserRepository Unit Tests")
class UserRepositoryTest {

    @Autowired private UserRepository userRepository;

    @Autowired private TestEntityManager entityManager;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        // Clean database
        userRepository.deleteAll();

        Date now = new Date(0);

        // Setup test data
        user1 =
                User.builder()
                        .email("user1@gmail.com")
                        .username("user1")
                        .phoneNumber("+201010149602")
                        .passwordHash("user1passwordhash")
                        .join_date(new Timestamp(now.getTime()))
                        .firstName("user")
                        .lastName("1")
                        .imageUrl("")
                        .role(Role.ADMIN)
                        .code(111111)
                        .emailVerified(true)
                        .build();

        user2 =
                User.builder()
                        .email("user2@gmail.com")
                        .username("user2")
                        .phoneNumber("+201110149602")
                        .passwordHash("user2passwordhash")
                        .join_date(new Timestamp(now.getTime()))
                        .firstName("user")
                        .lastName("2")
                        .imageUrl("")
                        .role(Role.CLIENT)
                        .code(111111)
                        .emailVerified(true)
                        .build();
    }

    @Test
    @DisplayName("Should save user successfully")
    void shouldSaveUser() {
        // Act
        User saved = userRepository.save(user1);

        // Assert
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("user1@gmail.com");
        assertThat(saved.getPhoneNumber()).isEqualTo("+201010149602");
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        // Arrange
        entityManager.persist(user1);
        entityManager.flush();

        // Act
        Optional<User> found = userRepository.findByEmail("user1@gmail.com");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getPhoneNumber()).isEqualTo("+201010149602");
    }

    @Test
    @DisplayName("Should return empty when user email not found")
    void shouldReturnEmptyWhenEmailNotFound() {
        // Act
        Optional<User> found = userRepository.findByEmail("non-existent");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find all users")
    void shouldFindAllUsers() {
        // Arrange
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        // Act
        List<User> users = userRepository.findAll();

        // Assert
        assertThat(users).hasSize(2);
        assertThat(users)
                .extracting(User::getPhoneNumber)
                .containsExactlyInAnyOrder("+201010149602", "+201110149602");
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUser() {
        // Arrange
        User saved = entityManager.persist(user1);
        entityManager.flush();

        // Act
        saved.setFirstName("New First Name");
        saved.setLastName("New Last Name");
        saved.setImageUrl("New Image Url");
        User updated = userRepository.save(saved);

        // Assert
        assertThat(updated.getFirstName()).isEqualTo("New First Name");
        assertThat(updated.getLastName()).isEqualTo("New Last Name");
        assertThat(updated.getImageUrl()).isEqualTo("New Image Url");
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUser() {
        // Arrange
        User saved = entityManager.persist(user1);
        entityManager.flush();
        Long userId = saved.getId();

        // Act
        userRepository.deleteById(userId);

        // Assert
        Optional<User> found = userRepository.findById(userId);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check if user exists by ID")
    void shouldCheckIfUserExists() {
        // Arrange
        User saved = entityManager.persist(user1);
        entityManager.flush();

        // Act
        boolean exists = userRepository.existsById(saved.getId());
        boolean notExists = userRepository.existsById(999L);

        // Assert
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should count users")
    void shouldCountUsers() {
        // Arrange
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        // Act
        long count = userRepository.count();

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should apply last-write-wins behavior for sequential updates")
    void shouldHandleConcurrentUpdates() {
        // Arrange
        User saved = entityManager.persist(user1);
        entityManager.flush();
        entityManager.clear();

        // Act - Simulate two concurrent updates
        User user1Copy = userRepository.findById(saved.getId()).get();
        User user2Copy = userRepository.findById(saved.getId()).get();

        user1Copy.setImageUrl("Image Url 1");
        userRepository.save(user1Copy);

        user2Copy.setImageUrl("Image Url 2");
        userRepository.save(user2Copy);

        // Assert - Last write wins
        User result = userRepository.findById(saved.getId()).get();
        assertThat(result.getImageUrl()).isEqualTo("Image Url 2");
    }
}

