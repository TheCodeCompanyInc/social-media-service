package com.thecodecompanyinc.social_media_service.controller;

import com.thecodecompanyinc.social_media_service.response.ProfileResponse;
import com.thecodecompanyinc.social_media_service.dto.profile.ProfileUpdateRequestDto;
import com.thecodecompanyinc.social_media_service.entity.User;
import com.thecodecompanyinc.social_media_service.response.ApiResponse;
import com.thecodecompanyinc.social_media_service.service.profile.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Profile", description = "Core Profile CRUD Operations")
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping("/{id}")
    @Operation(summary = "Get Profile by ID", description = "Retrieve a profile by its ID")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.createSuccessResponse(profileService.getProfileById(id)));
    }

    @PutMapping
    @Operation(summary = "Update Current User Profile", description = "Update the profile of the authenticated user")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid ProfileUpdateRequestDto profileUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.createSuccessResponse(profileService.updateProfile(user.getId(), profileUpdateRequestDto)));
    }
}