package com.thecodecompanyinc.social_media_service.service.mail;

public interface MailSenderService {
  void sendEmail(String toEmail, String subject, String body);
}
