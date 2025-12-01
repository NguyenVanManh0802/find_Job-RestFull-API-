package vn.manh.findJob.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.manh.findJob.service.EmailService;
import vn.manh.findJob.service.SubscriberService;

@RestController
@RequestMapping("/api/v1/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final SubscriberService subscriberSer;
    // API để test gửi email
    @GetMapping()
    public ResponseEntity<String> sendTestEmail(@RequestParam String email) {
         //Gửi email text đơn giản
//         emailService.sendSimpleEmail(
//             email,
//             "Test Email từ JobHunter",
//             "Đây là email test."
//         );
        // Gửi email HTML (thực tế hơn)
//        String htmlBody = "<h1>Chào mừng bạn đến với JobHunter!</h1>"
//                + "<p>Cảm ơn bạn đã đăng ký.</p>"
//                + "<a href='http://localhost:3000'>Truy cập trang web</a>";
//
//        emailService.sendEmailFromTemplateSync(
//                email,
//                "test send email",
//                "job"
//
//
//        );

        this.subscriberSer.sendSubscribersEmailJobs();
        return ResponseEntity.ok("Email đã được gửi đi (bất đồng bộ)!");
    }

}