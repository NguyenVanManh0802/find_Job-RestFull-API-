package vn.manh.findJob.service;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import vn.manh.findJob.repository.JobRepository;
import vn.manh.findJob.repository.SubscriberRepository;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final MailSender mailSender;
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final JobRepository jobRepository;
    private final SubscriberRepository subscriberRepository;
//    public void sendSimpleEmail(String to, String subject, String text){
//        log.info("Sending simple email to: {}", to);
//        try {
//            SimpleMailMessage message = new SimpleMailMessage();
//         //   message.setFrom(); // Lấy từ config hoặc cố định
//            message.setTo(to);
//            message.setSubject(subject);
//            message.setText(text);
//
//            mailSender.send(message);
//            log.info("Simple email sent successfully.");
//        } catch (Exception e) {
//            log.error("Failed to send simple email", e);
//        }
//    }

    public void sendEmailSync(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        // Prepare message using a Spring helper
        MimeMessage mimeMessage =javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content, isHtml);
            this.javaMailSender.send(mimeMessage);
        } catch (MailException | MessagingException e) {
            System.out.println("ERROR SEND EMAIL: " + e);
        }
    }


    public void sendEmailFromTemplateSync(String to, String subject, String templateName,String userName,Object value) {
        Context context = new Context();
        context.setVariable("name",userName);
        context.setVariable("jobs",value);
        String content = this.templateEngine.process(templateName, context);
        this.sendEmailSync(to, subject, content, false, true);
    }

    // Hàm gửi email xác thực
    public void sendVerificationEmail(String name, String toEmail, String token) {
        String subject = "Hoàn tất đăng ký tài khoản FindJob";

        // Link này trỏ về Frontend hoặc Backend.
        // Ví dụ trỏ về Backend API để test:
        //String verificationLink = "http://localhost:8080/api/v1/auth/verify?token=" + token;

        // Nếu làm chuẩn Frontend React:
        String verificationLink = "http://localhost:3000/verify?token=" + token;

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("link", verificationLink);

        // Lưu ý: Phải có file "verify-account.html" trong thư mục templates
        String content = this.templateEngine.process("verify-account", context);

        this.sendEmailSync(toEmail, subject, content, false, true);
    }

//send reset password
    public void sendResetPasswordEmail(String to, String token) {
        String subject = "Yêu cầu đặt lại mật khẩu - FindJob";

        // Link trỏ về trang Reset Password của Frontend (ReactJS)
        // Token JWT được đính kèm vào tham số ?token=...
        String url = "http://localhost:3000/reset-password?token=" + token;

        // Tạo nội dung HTML đơn giản (Inline CSS để đảm bảo hiển thị tốt trên Gmail)
        String content = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e0e0e0; border-radius: 5px; max-width: 600px; margin: 0 auto;'>"
                + "<h2 style='color: #1677ff;'>Xin chào,</h2>"
                + "<p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản FindJob của bạn.</p>"
                + "<p>Vui lòng nhấn vào nút bên dưới để thiết lập mật khẩu mới (Link này sẽ hết hạn sau 15 phút):</p>"
                + "<div style='text-align: center; margin: 30px 0;'>"
                + "<a href='" + url + "' style='background-color: #1677ff; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold; font-size: 16px;'>Đặt lại mật khẩu</a>"
                + "</div>"
                + "<p style='color: #666; font-size: 13px;'>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này. Tài khoản của bạn vẫn an toàn.</p>"
                + "<hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;' />"
                + "<p style='text-align: center; color: #999; font-size: 12px;'>FindJob Support Team</p>"
                + "</div>";

        // Gọi hàm gửi mail đồng bộ (hoặc bất đồng bộ tùy cấu hình của bạn)
        // sendEmailSync(người nhận, tiêu đề, nội dung, isMultipart, isHtml)
        this.sendEmailSync(to, subject, content, false, true);
    }

}