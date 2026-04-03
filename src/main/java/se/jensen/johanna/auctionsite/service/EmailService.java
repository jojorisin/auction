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
import se.jensen.johanna.auctionsite.dto.EmailRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;

  @Async
  public void sendEmail(EmailRequest request) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      setEmailContent(helper, request);
      helper.setTo(request.email());
      mailSender.send(message);
    } catch (MessagingException | MailException e) {
      log.error("Failed to send email to {}: {}", request.email(), e.getMessage());
    }
  }

  public void setEmailContent(MimeMessageHelper helper, EmailRequest request)
      throws MessagingException {
    switch (request.type()) {
      case WINNER:
        helper.setSubject(String.format("Congratulations! You won %s.", request.title()));
        helper.setText(
            String.format(
                "Congratulations! You won auction %s. Find payment and transport options under My Wins",
                request.title()
            )
        );
        break;
      case OUTBID:
        helper.setSubject(String.format("You were outbid on %s.", request.title()));
        helper.setText(
            String.format(
                "You were outbid on auction %s. Place a higher bid to compete.",
                request.title()
            )
        );
        break;
      case LOST:
        helper.setSubject(String.format("You lost %s.", request.title()));
        helper.setText(
            String.format("You lost auction %s.", request.title()));
        break;
      case AUCTION_REMINDER:
        helper.setSubject(String.format("Auction %s ends soon.", request.title()));
        helper.setText(
            String.format(
                "Auction %s ends soon, dont miss out!",
                request.title()
            )
        );
        break;
      case ITEM_SOLD:
        helper.setSubject(String.format("Item %s sold.", request.title()));
        helper.setText(
            String.format("Item %s you have bidded on has unfortunately sold.", request.title()));
        break;
      case ITEM_NOT_SOLD:
        helper.setSubject(String.format("Item %s not sold.", request.title()));
        helper.setText(String.format(
            "Item %s has unfortunately not sold.",
            request.title()
        ));
        break;
      default:
        log.warn("Unknown email type: {}", request.type());
    }
  }
}
