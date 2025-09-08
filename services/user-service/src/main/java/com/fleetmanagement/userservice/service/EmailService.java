package com.fleetmanagement.userservice.service;

import com.fleetmanagement.userservice.domain.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.email.from:noreply@fleetmanagement.com}")
    private String fromEmail;

    @Value("${app.email.verification.url:http://localhost:8080/api/users/verify}")
    private String verificationBaseUrl;

    @Value("${app.email.password-reset.url:http://localhost:8080/api/users/reset-password}")
    private String passwordResetBaseUrl;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendVerificationEmail(User user) {
        try {
            String verificationUrl = verificationBaseUrl + "?token=" + user.getEmailVerificationToken();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Verify Your Email - Fleet Management System");

            String htmlContent = buildVerificationEmailContent(user.getFirstName(), verificationUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Verification email sent to: {}", user.getEmail());

        } catch (MessagingException e) {
            logger.error("Failed to send verification email to: {}", user.getEmail(), e);
        }
    }

    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            String resetUrl = passwordResetBaseUrl + "?token=" + resetToken;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Password Reset - Fleet Management System");

            String htmlContent = buildPasswordResetEmailContent(user.getFirstName(), resetUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Password reset email sent to: {}", user.getEmail());

        } catch (MessagingException e) {
            logger.error("Failed to send password reset email to: {}", user.getEmail(), e);
        }
    }

    @Async
    public void sendWelcomeEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Welcome to Fleet Management System");
            message.setText(buildWelcomeEmailContent(user.getFirstName()));

            mailSender.send(message);
            logger.info("Welcome email sent to: {}", user.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", user.getEmail(), e);
        }
    }

    private String buildVerificationEmailContent(String firstName, String verificationUrl) {
        return String.format("""
            <html>
            <body>
                <h2>Welcome to Fleet Management System!</h2>
                <p>Hello %s,</p>
                <p>Thank you for registering with Fleet Management System. Please click the link below to verify your email address:</p>
                <p><a href="%s" style="background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Verify Email</a></p>
                <p>If you didn't create this account, please ignore this email.</p>
                <p>Best regards,<br>Fleet Management Team</p>
            </body>
            </html>
            """, firstName, verificationUrl);
    }

    private String buildPasswordResetEmailContent(String firstName, String resetUrl) {
        return String.format("""
            <html>
            <body>
                <h2>Password Reset Request</h2>
                <p>Hello %s,</p>
                <p>You requested a password reset. Click the link below to reset your password:</p>
                <p><a href="%s" style="background-color: #dc3545; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Reset Password</a></p>
                <p>This link will expire in 1 hour.</p>
                <p>If you didn't request this, please ignore this email.</p>
                <p>Best regards,<br>Fleet Management Team</p>
            </body>
            </html>
            """, firstName, resetUrl);
    }

    private String buildWelcomeEmailContent(String firstName) {
        return String.format("""
            Hello %s,
            
            Welcome to Fleet Management System!
            
            Your email has been verified successfully. You can now access all features of our platform.
            
            If you have any questions, please don't hesitate to contact our support team.
            
            Best regards,
            Fleet Management Team
            """, firstName);
    }


    public void sendEmailVerification(String email, String fullName, String token) {
        // Create a user object for the existing method
        User tempUser = User.builder()
                .email(email)
                .firstName(fullName.split(" ")[0])
                .emailVerificationToken(token)
                .build();

        sendVerificationEmail(tempUser);
    }

    public void sendPasswordReset(String email, String fullName, String token) {
        // Create a user object for the existing method
        User tempUser = User.builder()
                .email(email)
                .firstName(fullName.split(" ")[0])
                .build();

        sendPasswordResetEmail(tempUser, token);
    }
}