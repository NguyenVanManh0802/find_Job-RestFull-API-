package vn.manh.findJob.controller;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vn.manh.findJob.domain.Subscriber;
import vn.manh.findJob.dto.ResponseData;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.service.SubscriberService;


import java.net.URI;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/subscribers")
public class SubscriberController {

    private final SubscriberService subscriberService;

    @PostMapping
    public ResponseEntity<ResponseData<Subscriber>> createSubscriber(@Valid @RequestBody Subscriber subscriber) {
        log.info("Request create Subscriber");
        Subscriber newSubscriber = subscriberService.createSubscriber(subscriber);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newSubscriber.getId())
                .toUri();

        ResponseData<Subscriber> responseData = new ResponseData<>(
                HttpStatus.CREATED.value(),
                "Subscriber created successfully",
                newSubscriber);
        return ResponseEntity.created(location).body(responseData);
    }

    @GetMapping
    public ResponseEntity<ResponseData<ResultPaginationDTO>> getAllSubscribers(
            @Filter Specification<Subscriber> specification, Pageable pageable) {

        ResultPaginationDTO result = subscriberService.getAllSubscribers(specification, pageable);
        ResponseData<ResultPaginationDTO> responseData = new ResponseData<>(
                HttpStatus.OK.value(),
                "Fetch all subscribers successfully",
                result
        );
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<Subscriber>> getSubscriberById(@PathVariable long id) {
        log.info("Get subscriber by id: {}", id);
        Subscriber subscriber = subscriberService.getSubscriberById(id);
        ResponseData<Subscriber> responseData = new ResponseData<>(
                HttpStatus.OK.value(),
                "Get subscriber by Id successful",
                subscriber
        );
        return ResponseEntity.ok(responseData);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<Subscriber>> updateSubscriber(
            @PathVariable long id, @Valid @RequestBody Subscriber subscriber) {

        Subscriber updatedSubscriber = subscriberService.updateSubscriber(id, subscriber);
        ResponseData<Subscriber> responseData = new ResponseData<>(
                HttpStatus.OK.value(),
                "Update subscriber successful",
                updatedSubscriber
        );
        return ResponseEntity.ok(responseData);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteSubscriber(@PathVariable long id) {
        log.info("Delete subscriber by id ={}", id);
        subscriberService.deleteSubscriber(id);
        ResponseData<Void> responseData = new ResponseData<>(
                HttpStatus.OK.value(),
                "Delete subscriber successful"
        );
        return ResponseEntity.ok(responseData);
    }
}