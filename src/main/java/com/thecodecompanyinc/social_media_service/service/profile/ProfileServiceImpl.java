package com.thecodecompanyinc.social_media_service.service.profile;

import com.thecodecompanyinc.social_media_service.dto.profile.ProfileResponse;
import com.thecodecompanyinc.social_media_service.dto.profile.ProfileUpdateRequest;
import com.thecodecompanyinc.social_media_service.entity.User;
import com.thecodecompanyinc.social_media_service.exception.ResourceNotFoundException;
import com.thecodecompanyinc.social_media_service.mapper.ProfileMapper;
import com.thecodecompanyinc.social_media_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {
    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;

    @Override
    @Transactional
    public ProfileResponse getProfileById(Long userId) {
        User user = findUserOrThrow(userId);
        return profileMapper.toResponse(user);
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest profileUpdateRequest) {
        User user = findUserOrThrow(userId);
        profileMapper.applyUpdate(profileUpdateRequest, user);
        userRepository.save(user);
        return profileMapper.toResponse(user);
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}