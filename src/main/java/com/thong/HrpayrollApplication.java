package com.thong;

import com.thong.config.CloudinaryProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.TimeZone;

@SpringBootApplication
@EnableConfigurationProperties(CloudinaryProperties.class)
public class HrpayrollApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Phnom_Penh"));
		SpringApplication.run(HrpayrollApplication.class, args);
	}

}
