package vn.manh.findJob.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.manh.findJob.domain.Job;
import vn.manh.findJob.domain.Skill;
import vn.manh.findJob.domain.Subscriber;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.dto.email.ResEmailJob;
import vn.manh.findJob.exception.ResourceAlreadyExistsException;
import vn.manh.findJob.exception.ResourceNotFoundException;
import vn.manh.findJob.repository.JobRepository;
import vn.manh.findJob.repository.SkillRepository;
import vn.manh.findJob.repository.SubscriberRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubscriberService {

    private final SubscriberRepository subscriberRepository;
    private final SkillRepository skillRepository; // Cần thiết để xử lý logic skills
    private final JobRepository jobRepository;
    private final EmailService emailService;

    // Hàm private helper để tìm subscriber
    private Subscriber findSubscriberById(long id) {
        return subscriberRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Subscriber not found with id: {}", id);
                    return new ResourceNotFoundException("Subscriber not found with id: " + id);
                });
    }

    public Subscriber createSubscriber(Subscriber subscriber) {
        log.info("Creating new subscriber with email: {}", subscriber.getEmail());

        // 1. Kiểm tra Email tồn tại
        if (this.subscriberRepository.existsByEmail(subscriber.getEmail())) {
            throw new ResourceNotFoundException("Email " + subscriber.getEmail() + " đã tồn tại.");
        }

        // 2. Kiểm tra và lấy các Skills (Tương tự JobService)
        if (subscriber.getSkills() != null) {
            List<Long> skillIds = subscriber.getSkills()
                    .stream()
                    .map(Skill::getId)
                    .collect(Collectors.toList());

            // Dùng findAllById là phương thức chuẩn của JpaRepository
            List<Skill> dbSkills = this.skillRepository.findAllById(skillIds);

            // Kiểm tra xem có ID nào không hợp lệ không
            if (dbSkills.size() != skillIds.size()) {
                throw new IllegalArgumentException("Một hoặc nhiều Skill ID không hợp lệ.");
            }

            subscriber.setSkills(dbSkills); // Gán lại danh sách skill đã được quản lý bởi JPA
        }

        // 3. Lưu
        Subscriber newSubscriber = subscriberRepository.save(subscriber);
        log.info("Subscriber đã được lưu thành công, email = {} ", newSubscriber.getEmail());
        return newSubscriber;
    }

    public ResultPaginationDTO getAllSubscribers(Specification<Subscriber> specification, Pageable pageable) {
        Page<Subscriber> pageSubscriber = subscriberRepository.findAll(specification, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1); // Hiển thị trang bắt đầu từ 1
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageSubscriber.getTotalPages());
        meta.setTotal(pageSubscriber.getTotalElements());
        rs.setMeta(meta);

        // Trả về List<Subscriber> trực tiếp theo yêu cầu
        rs.setResult(pageSubscriber.getContent());
        return rs;
    }

    public Subscriber getSubscriberById(long id) {
        log.info("Fetching subscriber with id={}", id);
        // Dùng hàm private helper
        return this.findSubscriberById(id);
    }

    public Subscriber updateSubscriber(long id, Subscriber subscriber) {
        log.info("Updating subscriber with id={}", id);
        Subscriber existingSubscriber = this.findSubscriberById(id);

        // Kiểm tra email nếu email bị thay đổi
        if (!existingSubscriber.getEmail().equals(subscriber.getEmail())) {
            if (this.subscriberRepository.existsByEmail(subscriber.getEmail())) {
                throw new ResourceAlreadyExistsException("Email " + subscriber.getEmail() + " đã tồn tại.");
            }
            existingSubscriber.setEmail(subscriber.getEmail());
        }

        // Cập nhật các trường khác
        existingSubscriber.setName(subscriber.getName());

        // Cập nhật Skills (Tương tự JobService)
        if (subscriber.getSkills() != null) {
            List<Long> skillIds = subscriber.getSkills()
                    .stream()
                    .map(Skill::getId)
                    .collect(Collectors.toList());

            List<Skill> dbSkills = this.skillRepository.findAllById(skillIds);


            existingSubscriber.setSkills(dbSkills);
        } else {
            existingSubscriber.setSkills(null); // Xóa hết skills nếu truyền vào null
        }

        Subscriber updatedSubscriber = subscriberRepository.save(existingSubscriber);
        log.info("Subscriber updated successful");
        return updatedSubscriber;
    }

    public void deleteSubscriber(long id) {
        log.info("Deleting subscriber with id ={}", id);
        // Kiểm tra tồn tại (giống JobService)
        if (!subscriberRepository.existsById(id)) {
            log.info("Subscriber not found with id={}", id);
            throw new ResourceNotFoundException("Subscriber not found with id : " + id);
        }
        subscriberRepository.deleteById(id);
        log.info("Delete subscriber successful");
    }

    public void sendSubscribersEmailJobs() {
        List<Subscriber> listSubs = this.subscriberRepository.findAll();
        if (listSubs != null && listSubs.size() > 0) {
            for (Subscriber sub : listSubs) {
                List<Skill> listSkills = sub.getSkills();
                if (listSkills != null && listSkills.size() > 0) {
                    List<Job> listJobs = this.jobRepository.findBySkillsIn(listSkills);
                    if (listJobs != null && listJobs.size() > 0) {

                         List<ResEmailJob> arr = listJobs.stream().map(
                         job -> this.convertJobToSendEmail(job)).collect(Collectors.toList());

                        this.emailService.sendEmailFromTemplateSync(
                                sub.getEmail(),
                                "Cơ hội việc làm hot đang chờ đón bạn, khám phá ngay",
                                "job",
                                sub.getName(),
                                arr
                        );
                    }
                }
            }
        }
    }
    public ResEmailJob convertJobToSendEmail(Job job) {
        ResEmailJob res = new ResEmailJob();
        res.setName(job.getName());
        res.setSalary(job.getSalary());
        res.setCompany(new ResEmailJob.CompanyEmail(job.getCompany().getName()));
        List<Skill> skills = job.getSkills();
        List<ResEmailJob.SkillEmail> s = skills.stream().map(skill -> new ResEmailJob.SkillEmail(skill.getName()))
                .collect(Collectors.toList());
        res.setSkills(s);
        return res;
    }

    @Scheduled(fixedDelay = 1000)
    public void testCron()
    {
        System.out.println("test cron ");
    }

}