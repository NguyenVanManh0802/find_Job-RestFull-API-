package vn.manh.findJob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync  //bất đồng bộ cho việc gửi email
public class FindJobApplication {

	public static void main(String[] args) {
		SpringApplication.run(FindJobApplication.class, args);
	}

}
