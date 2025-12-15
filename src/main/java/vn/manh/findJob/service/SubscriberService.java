package vn.manh.findJob.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.manh.findJob.domain.Company;
import vn.manh.findJob.domain.Subscriber;
import vn.manh.findJob.domain.User;
import vn.manh.findJob.dto.Subscribe.ReqSubscribeDTO;
import vn.manh.findJob.exception.ResourceNotFoundException;
import vn.manh.findJob.repository.CompanyRepository;
import vn.manh.findJob.repository.SubscriberRepository;
import vn.manh.findJob.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class SubscriberService {

    private final SubscriberRepository subscriberRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public Subscriber handleSubscribe(ReqSubscribeDTO reqSubscribeDTO) {
        // 1. Check đăng nhập
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get() : "";
        if (email.equals("")) {
            throw new ResourceNotFoundException("Vui lòng đăng nhập để thực hiện chức năng này.");
        }

        User currentUser = this.userRepository.findByEmail(email);
        if (currentUser == null) {
            throw new ResourceNotFoundException("Người dùng không tồn tại.");
        }

        Company company = this.companyRepository.findById(reqSubscribeDTO.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Công ty không tồn tại"));

        Subscriber subscriber = this.subscriberRepository.findByEmail(email);

        if (subscriber == null) {
            // Trường hợp 1: Chưa từng theo dõi ai -> Tạo mới Subscriber
            subscriber = new Subscriber();
            subscriber.setEmail(email);
            subscriber.setName(currentUser.getName());
            List<Company> companies = new ArrayList<>();
            companies.add(company);
            subscriber.setCompanies(companies);
        } else {
            // Trường hợp 2: Đã có Subscriber -> Check list Company
            List<Company> companies = subscriber.getCompanies();

            // Kiểm tra xem đã theo dõi công ty này chưa
            boolean isExists = companies.stream().anyMatch(c -> c.getId() == company.getId());

            if (isExists) {
                // --- LOGIC HỦY THEO DÕI ---
                // Nếu đã có -> Xóa khỏi danh sách
                companies.removeIf(c -> c.getId() == company.getId());
            } else {
                // --- LOGIC THEO DÕI ---
                // Nếu chưa có -> Thêm vào danh sách
                companies.add(company);
            }

            subscriber.setCompanies(companies);
        }

        return this.subscriberRepository.save(subscriber);
    }
}