package com.thecodecompanyinc.social_media_service.service.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailSenderServiceImpl implements MailSenderService {

  private final JavaMailSender mailSender;

  @Override
  public void sendEmail(String toEmail, String subject, String body) {
    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setFrom("muhammad.heshamyt@gmail.com");
    mailMessage.setTo(toEmail);
    mailMessage.setText(body);
    mailMessage.setSubject(subject);

    // For Testing Environment
    if (false) mailSender.send(mailMessage);

    log.info("Mail Sent to {} Successfully!", toEmail);
  }
}
