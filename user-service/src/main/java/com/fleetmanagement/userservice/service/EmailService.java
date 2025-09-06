package com.fleetmanagement.userservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.email.verification.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmailVerification(String toEmail, String fullName, String verificationToken) {
        try {
            String subject = "Fleet Management - Email Verification";
            String verificationUrl = baseUrl + "/verify-email?token=" + verificationToken;

            String htmlContent = buildVerificationEmailContent(fullName, verificationUrl);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            logger.info("Email verification sent to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send email verification to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendPasswordReset(String toEmail, String fullName, String resetToken) {
        try {
            String subject = "Fleet Management - Password Reset";
            String resetUrl = baseUrl + "/reset-password?token=" + resetToken;

            String htmlContent = buildPasswordResetEmailContent(fullName, resetUrl);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            logger.info("Password reset email sent to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildVerificationEmailContent(String fullName, String verificationUrl) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 10px;">
                    <h2 style="color: #333; text-align: center;">Email Verification Required</h2>
                    <p>Dear %s,</p>
                    <p>Welcome to Fleet Management System! Please verify your email address by clicking the button below:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #007bff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;">
                            Verify Email Address
                        </a>
                    </div>
                    <p>If the button doesn't work, copy and paste this link into your browser:</p>
                    <p style="word-break: break-all; color: #666;">%s</p>
                    <p>This verification link will expire in 24 hours.</p>
                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #eee;">
                    <p style="font-size: 12px; color: #666; text-align: center;">
                        If you didn't create an account, please ignore this email.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(fullName, verificationUrl, verificationUrl);
    }

    private String buildPasswordResetEmailContent(String fullName, String resetUrl) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 10px;">
                    <h2 style="color: #333; text-align: center;">Password Reset Request</h2>
                    <p>Dear %s,</p>
                    <p>You have requested to reset your password. Click the button below to reset it:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #dc3545; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;">
                            Reset Password
                        </a>
                    </div>
                    <p>If the button doesn't work, copy and paste this link into your browser:</p>
                    <p style="word-break: break-all; color: #666;">%s</p>
                    <p>This reset link will expire in 1 hour.</p>
                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #eee;">
                    <p style="font-size: 12px; color: #666; text-align: center;">
                        If you didn't request this password reset, please ignore this email.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(fullName, resetUrl, resetUrl);
    }
}
