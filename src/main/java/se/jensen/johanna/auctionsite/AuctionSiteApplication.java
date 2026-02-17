package se.jensen.johanna.auctionsite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@EnableRetry
@SpringBootApplication
public class AuctionSiteApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AuctionSiteApplication.class);
        // Sätt porten här om du vill
        app.setDefaultProperties(java.util.Collections.singletonMap("server.port", "8080"));
        app.run(args);


    }
}
