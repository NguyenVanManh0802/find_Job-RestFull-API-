package vn.manh.findJob.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    /**
     * Hàm gửi email cơ bản (Core)
     */
    public void sendEmailSync(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content, isHtml);
            this.javaMailSender.send(mimeMessage);
            log.info("Email sent successfully to: {}", to);
        } catch (MailException | MessagingException e) {
            log.error("ERROR SEND EMAIL to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Hàm tiện ích xử lý Template Thymeleaf
     * Giúp code gọn gàng, không lặp lại logic tạo Context
     */
    @Async
    public void sendEmailFromTemplate(String to, String subject, String templateName, Map<String, Object> variables) {
        Context context = new Context();
        // Nạp tất cả biến vào context
        context.setVariables(variables);

        // Render HTML từ template
        String content = this.templateEngine.process(templateName, context);

        // Gửi mail
        this.sendEmailSync(to, subject, content, false, true);
    }

    // ========================================================================
    // CÁC HÀM NGHIỆP VỤ (Sử dụng hàm tiện ích ở trên)
    // ========================================================================
    @Async
    // 1. Gửi email xác thực tài khoản
    public void sendVerificationEmail(String name, String toEmail, String token) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", name);
        variables.put("link", "http://localhost:3000/verify?token=" + token);

        this.sendEmailFromTemplate(toEmail, "Hoàn tất đăng ký tài khoản FindJob", "verify-account", variables);
    }
    @Async
    // 2. Gửi email Reset Password
    public void sendResetPasswordEmail(String to, String token) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("link", "http://localhost:3000/reset-password?token=" + token);

        this.sendEmailFromTemplate(to, "Yêu cầu đặt lại mật khẩu - FindJob", "email-reset-password", variables);
    }
    @Async
    // 3. Gửi cho Ứng viên sau khi nộp CV thành công
    public void sendEmailToCandidateAfterApply(String receiverEmail, String jobName, String userName) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", userName);
        variables.put("jobName", jobName);

        this.sendEmailFromTemplate(receiverEmail, "Xác nhận ứng tuyển thành công - FindJob", "email-apply-success", variables);
    }
    @Async
    // 4. Gửi thông báo cho HR khi có ứng viên mới
    public void sendEmailToHRAfterApply(String hrEmail, String jobName, String candidateName, String resumeLink) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("jobName", jobName);
        variables.put("candidateName", candidateName);
        variables.put("resumeLink", resumeLink);

        this.sendEmailFromTemplate(hrEmail, "[FindJob] Ứng viên mới cho: " + jobName, "email-new-applicant-hr", variables);
    }
    @Async
    // 5. Gửi thư mời phỏng vấn
    public void sendEmailInterview(String email, String candidateName, String jobName) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("candidateName", candidateName);
        variables.put("jobName", jobName);

        this.sendEmailFromTemplate(email, "[FindJob] Thư mời phỏng vấn - " + jobName, "email-interview-invite", variables);
    }
    @Async
    // 6. Gửi thư từ chối
    public void sendEmailReject(String email, String candidateName, String jobName) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("candidateName", candidateName);
        variables.put("jobName", jobName);

        this.sendEmailFromTemplate(email, "[FindJob] Thông báo kết quả ứng tuyển - " + jobName, "email-job-reject", variables);
    }

    @Async
    public void sendEmailNewJobAlert(String receiverEmail, String userName, String jobName, String companyName, String jobLink, double salary) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", userName);
        variables.put("jobName", jobName);
        variables.put("companyName", companyName);
        variables.put("jobLink", jobLink);
        // Format lương cho đẹp (hoặc xử lý ở FE/Template, ở đây demo đơn giản)
        variables.put("salary", String.format("%,.0f đ", salary));

        this.sendEmailFromTemplate(receiverEmail, "🔥 Cơ hội việc làm mới từ " + companyName, "email-new-job-alert", variables);
    }
}