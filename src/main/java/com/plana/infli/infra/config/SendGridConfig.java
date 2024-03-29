package com.plana.infli.infra.config;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendGridConfig {

    @Value("${SENDGRID_API_KEY}")
    private String key;

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(key);
    }
}
