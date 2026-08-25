package com.helpinghand.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Sends a booking-confirmation email using JavaMail (SMTP).
 *
 * To actually send real emails, fill in SMTP_USER / SMTP_APP_PASSWORD with a
 * Gmail account and an "App Password" (Google Account > Security > App
 * Passwords). Until you do that, sendBookingConfirmation() just logs the
 * message to the console so the rest of the app keeps working during
 * development/demo — this is intentional, not an error.
 */
public class NotificationUtil {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_USER = "your-email@gmail.com";
    private static final String SMTP_APP_PASSWORD = "your-16-char-app-password";
    private static final boolean EMAIL_ENABLED = false; // flip to true once creds above are set

    public static void sendBookingConfirmation(String toEmail, String customerName,
                                                 String serviceName, String date, String time) {
        String subject = "Helping Hand — Booking Confirmed: " + serviceName;
        String body = "Hi " + customerName + ",\n\n"
                + "Your booking for \"" + serviceName + "\" is confirmed.\n"
                + "Date: " + date + "\n"
                + "Time: " + time + "\n\n"
                + "Thank you for using Helping Hand!";

        if (!EMAIL_ENABLED || toEmail == null || toEmail.isEmpty()) {
            System.out.println("[NOTIFICATION - console fallback] To: " + toEmail);
            System.out.println(body);
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
        } catch (MessagingException e) {
            // Never let a failed email break the booking flow — just log it.
            System.err.println("Failed to send booking email: " + e.getMessage());
        }
    }
}
