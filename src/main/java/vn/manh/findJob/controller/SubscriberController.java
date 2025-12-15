package vn.manh.findJob.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.manh.findJob.domain.Subscriber;

import vn.manh.findJob.dto.ResponseData;
import vn.manh.findJob.dto.Subscribe.ReqSubscribeDTO;
import vn.manh.findJob.service.SubscriberService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/subscribers")
public class SubscriberController {

    private final SubscriberService subscriberService;

    @PostMapping("/subscribe")
    public ResponseEntity<ResponseData<Subscriber>> subscribeToCompany(@RequestBody @Valid ReqSubscribeDTO req) {
        Subscriber subscriber = this.subscriberService.handleSubscribe(req);

        ResponseData<Subscriber> res = new ResponseData<>(
                HttpStatus.OK.value(),
                "Cập nhật theo dõi thành công", // Đổi message
                subscriber
        );
        return ResponseEntity.ok(res);
    }
}