package com.thecodecompanyinc.social_media_service.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.thecodecompanyinc.social_media_service.service.mail.MailSenderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/** NOTE: ALL THOSE TESTS ARE AI-GENERATED AND REVIEWED MANUALLY */
@ExtendWith(MockitoExtension.class)
@DisplayName("MailSenderService Unit Tests")
class MailSenderServiceTest {

    @Mock private JavaMailSender mailSender;

    @InjectMocks private MailSenderServiceImpl mailSenderService;

    @Test
    @DisplayName("Should call sendEmail without throwing")
    void shouldCallSendEmailWithoutThrowing() {
        // Act - sendEmail currently has mail sending disabled (if false)
        mailSenderService.sendEmail("test@example.com", "Test Subject", "Test Body");

        // Assert - mailSender.send should NOT be called due to `if (false)` guard
        verify(mailSender, never()).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should handle different email parameters")
    void shouldHandleDifferentEmailParameters() {
        // Act - Should not throw for any valid parameters
        mailSenderService.sendEmail("user@domain.com", "Welcome", "Hello World");
        mailSenderService.sendEmail("another@test.com", "Verification Code", "123456");

        // Assert - No exceptions and no mail sent
        verify(mailSender, never()).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }
}
