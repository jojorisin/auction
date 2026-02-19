package se.jensen.johanna.auctionsite.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import se.jensen.johanna.auctionsite.dto.EmailTypeDTO;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    @Async
    public void sendEmail(EmailTypeDTO emailDTO) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            switch (emailDTO.status()) {
                case WON:
                    helper.setSubject("Congratulations! You won");
                    helper.setText("Congratulations! You won auction " + emailDTO.title() + " " + emailDTO.imageUrl() + ". Find payment and transport options under My Wins");
                    break;
                case OUTBID:
                    helper.setSubject("You were outbid.");
                    helper.setText("You were outbid on auction " + emailDTO.title() + " " + emailDTO.imageUrl());
                    break;
                default:
                    log.warn("Unknown email status: {}", emailDTO.status());
                    break;
            }

            helper.setTo(emailDTO.email());
            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            log.warn("Failes to send email to {}: {}", emailDTO.email(), e.getMessage());
        }
    }
}
