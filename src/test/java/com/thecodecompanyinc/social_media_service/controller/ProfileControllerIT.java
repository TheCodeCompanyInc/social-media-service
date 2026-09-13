package com.thecodecompanyinc.social_media_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thecodecompanyinc.social_media_service.dto.profile.ProfileUpdateRequest;
import com.thecodecompanyinc.social_media_service.entity.OnlineStatus;
import com.thecodecompanyinc.social_media_service.entity.Role;
import com.thecodecompanyinc.social_media_service.entity.User;
import com.thecodecompanyinc.social_media_service.repository.UserRepository;
import com.thecodecompanyinc.social_media_service.service.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.sql.Timestamp;
import java.time.Instant;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** NOTE: ALL THOSE TESTS ARE AI-GENERATED AND REVIEWED MANUALLY */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProfileControllerIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String accessToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        userRepository.deleteAll();
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .phoneNumber("+1234567890")
                .passwordHash("password")
                .firstName("Test")
                .lastName("User")
                .role(Role.CLIENT)
                .join_date(Timestamp.from(Instant.now()))
                .emailVerified(true)
                .onlineStatus(OnlineStatus.ONLINE)
                .bio("Initial bio")
                .build();
        testUser = userRepository.save(testUser);
        accessToken = "Bearer " + jwtService.generateAccessToken(testUser);
    }

    @Test
    @DisplayName("GET /api/profiles/{id} - Success")
    void getProfileById_Success() throws Exception {
        mockMvc.perform(get("/api/profiles/{id}", testUser.getId())
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.firstName", is(testUser.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(testUser.getLastName())))
                .andExpect(jsonPath("$.bio", is(testUser.getBio())));
    }

    @Test
    @DisplayName("GET /api/profiles/{id} - Not Found")
    void getProfileById_NotFound() throws Exception {
        mockMvc.perform(get("/api/profiles/{id}", 999L)
                        .header("Authorization", accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/profiles/{id} - Unauthorized")
    void getProfileById_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/profiles/{id}", testUser.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/profiles/{id} - Success")
    void updateProfile_Success() throws Exception {
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");
        updateRequest.setBio("Updated bio");

        mockMvc.perform(put("/api/profiles/{id}", testUser.getId())
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Updated")))
                .andExpect(jsonPath("$.lastName", is("Name")))
                .andExpect(jsonPath("$.bio", is("Updated bio")));
    }

    @Test
    @DisplayName("PUT /api/profiles/{id} - Validation Failure")
    void updateProfile_ValidationFailure() throws Exception {
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest();
        updateRequest.setUsername("u"); // Too short

        mockMvc.perform(put("/api/profiles/{id}", testUser.getId())
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/profiles/{id} - Unauthorized")
    void updateProfile_Unauthorized() throws Exception {
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest();
        updateRequest.setFirstName("Updated");

        mockMvc.perform(put("/api/profiles/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }
}
