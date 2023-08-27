package com.plana.infli.infra.config;

import static com.amazonaws.regions.Regions.*;
import static com.amazonaws.services.s3.AmazonS3ClientBuilder.*;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSConfig {

    @Bean
    public AmazonS3 amazonS3Client() {
        return AmazonS3Client.builder()
                .withRegion(AP_NORTHEAST_2)
                .build();
    }
}