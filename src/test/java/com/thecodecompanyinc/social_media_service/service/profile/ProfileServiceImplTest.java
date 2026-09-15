package com.thecodecompanyinc.social_media_service.service.profile;

import com.thecodecompanyinc.social_media_service.response.ProfileResponse;
import com.thecodecompanyinc.social_media_service.dto.profile.ProfileUpdateRequestDto;
import com.thecodecompanyinc.social_media_service.entity.User;
import com.thecodecompanyinc.social_media_service.exception.ResourceNotFoundException;
import com.thecodecompanyinc.social_media_service.mapper.ProfileMapper;
import com.thecodecompanyinc.social_media_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** NOTE: ALL THOSE TESTS ARE AI-GENERATED AND REVIEWED MANUALLY */
@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private ProfileMapper profileMapper;
    @InjectMocks private ProfileServiceImpl profileService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("mahmoud")
                .firstName("Mahmoud")
                .lastName("Abdulmawlaa")
                .email("mahmoud@example.com")
                .passwordHash("hashed")
                .build();
    }

    @Test
    void getProfileById_returnsMappedProfile_whenUserExists() {
        ProfileResponse expected = new ProfileResponse();
        expected.userId = 1L;
        expected.username = "mahmoud";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileMapper.toResponse(user)).thenReturn(expected);

        ProfileResponse result = profileService.getProfileById(1L);

        assertThat(result).isEqualTo(expected);
        verify(profileMapper).toResponse(user);
    }

    @Test
    void getProfileById_throwsUserNotFoundException_whenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfileById(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(profileMapper, never()).toResponse(any());
    }

    @Test
    void updateProfile_appliesChangesAndSaves_whenUserExists() {
        ProfileUpdateRequestDto request = new ProfileUpdateRequestDto();
        request.setUsername("newname");

        ProfileResponse expected = new ProfileResponse();
        expected.userId = 1L;
        expected.username = "newname";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileMapper.toResponse(user)).thenReturn(expected);

        ProfileResponse result = profileService.updateProfile(1L, request);

        verify(profileMapper).applyUpdate(request, user);
        verify(userRepository).save(user);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void updateProfile_throwsUserNotFoundException_whenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.updateProfile(99L, new ProfileUpdateRequestDto()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any());
    }
}