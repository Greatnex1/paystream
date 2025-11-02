package com.interswitch.bulktransaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class BulkTransactionApplication {
	;

	public static void main(String[] args) {
		SpringApplication.run(BulkTransactionApplication.class, args);
		log.info("==>>BULK TRANSACTION PROCESSING SERVICE started");
	}
}
