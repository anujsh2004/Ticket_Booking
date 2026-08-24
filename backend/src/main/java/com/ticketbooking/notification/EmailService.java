package com.ticketbooking.notification;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final QrCodeService qrCodeService;

    @Value("${app.email.provider:mock}")
    private String emailProvider;

    @Value("${app.email.from:noreply@ticketbooking.com}")
    private String emailFrom;

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String frontendUrl;

    @Async
    public void sendBookingConfirmationEmail(
            String toEmail,
            String customerName,
            String bookingReference,
            String eventTitle,
            String venueName,
            String showTime,
            List<String> seatLabels,
            BigDecimal totalAmount
    ) {
        log.info("Sending booking confirmation email to: {} for booking: {}", toEmail, bookingReference);

        if ("mock".equalsIgnoreCase(emailProvider)) {
            log.info("============== [MOCK EMAIL: BOOKING CONFIRMED] ==============");
            log.info("To: {}", toEmail);
            log.info("Subject: Your Ticket is Confirmed — {}", bookingReference);
            log.info("Dear {}, your booking for '{}' at '{}' on {} is confirmed!", customerName, eventTitle, venueName, showTime);
            log.info("Seats: {}", String.join(", ", seatLabels));
            log.info("Total Paid: ${}", totalAmount);
            log.info("Booking Reference: {}", bookingReference);
            log.info("=============================================================");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailFrom);
            helper.setTo(toEmail);
            helper.setSubject("Your Ticket is Confirmed — " + bookingReference);

            String htmlBody = String.format("""
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                    <h2 style="color: #4F46E5;">Booking Confirmation</h2>
                    <p>Dear <strong>%s</strong>,</p>
                    <p>Your booking for <strong>%s</strong> is confirmed!</p>
                    <div style="background-color: #F3F4F6; padding: 15px; border-radius: 6px; margin: 20px 0;">
                        <p><strong>Booking Reference:</strong> %s</p>
                        <p><strong>Venue:</strong> %s</p>
                        <p><strong>Show Time:</strong> %s</p>
                        <p><strong>Seats:</strong> %s</p>
                        <p><strong>Total Amount:</strong> $%s</p>
                    </div>
                    <div style="text-align: center; margin: 25px 0;">
                        <p><strong>Scan QR Code at Entry:</strong></p>
                        <img src="cid:qrCodeImage" alt="Ticket QR Code" style="width: 200px; height: 200px;" />
                    </div>
                    <p style="color: #6B7280; font-size: 12px; text-align: center;">Thank you for booking with Ticket Booking System.</p>
                </div>
            """, customerName, eventTitle, bookingReference, venueName, showTime, String.join(", ", seatLabels), totalAmount);

            helper.setText(htmlBody, true);

            byte[] qrBytes = qrCodeService.generateQrCodeBytes(bookingReference, 250, 250);
            helper.addInline("qrCodeImage", new ByteArrayResource(qrBytes), "image/png");

            mailSender.send(message);
            log.info("Booking confirmation email successfully dispatched to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send real confirmation email to {}. Fallback to log.", toEmail, e);
        }
    }

    @Async
    public void sendWaitlistOfferEmail(
            String toEmail,
            String customerName,
            String eventTitle,
            String seatLabel,
            String categoryName,
            Long offerId,
            int validityMinutes
    ) {
        String claimUrl = frontendUrl + "/waitlist/claim/" + offerId;
        log.info("Sending waitlist offer email to: {} for event: {}", toEmail, eventTitle);

        if ("mock".equalsIgnoreCase(emailProvider)) {
            log.info("============== [MOCK EMAIL: WAITLIST OFFER] ==============");
            log.info("To: {}", toEmail);
            log.info("Subject: A seat is available for {}!", eventTitle);
            log.info("Dear {}, a {} seat ({}) has opened up for '{}'!", customerName, categoryName, seatLabel, eventTitle);
            log.info("You have {} minutes to complete your booking.", validityMinutes);
            log.info("Claim URL: {}", claimUrl);
            log.info("==========================================================");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(emailFrom);
            helper.setTo(toEmail);
            helper.setSubject("Seat Available! Complete your booking for " + eventTitle);

            String htmlBody = String.format("""
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                    <h2 style="color: #10B981;">Good News! A Seat is Available</h2>
                    <p>Dear <strong>%s</strong>,</p>
                    <p>A seat has just become available for <strong>%s</strong> in your requested category (<strong>%s</strong> - Seat %s).</p>
                    <p>You have <strong>%d minutes</strong> to claim this seat before it is offered to the next person in the waitlist.</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #4F46E5; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold;">
                            Complete Booking Now
                        </a>
                    </div>
                </div>
            """, customerName, eventTitle, categoryName, seatLabel, validityMinutes, claimUrl);

            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send waitlist offer email to {}", toEmail, e);
        }
    }
}
