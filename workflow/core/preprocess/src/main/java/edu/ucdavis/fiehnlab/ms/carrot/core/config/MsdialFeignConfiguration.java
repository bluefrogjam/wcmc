//package edu.ucdavis.fiehnlab.ms.carrot.core.config;
//
//import feign.Logger;
//import feign.Request;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.cloud.netflix.feign.EnableFeignClients;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * Created by diego on 2/10/2017.
// */
//@Configuration
//@EnableFeignClients
//@ConfigurationProperties(prefix = "msdial")
//public class MsdialFeignConfiguration {
//
//	int connectTimeoutMillis;
//
//	int readTimeoutMillis;
//
//	@Bean
//	Logger.Level logger() {
//		return Logger.Level.FULL;
//	}
//
//	@Bean
//	Request.Options options() {
//		return new Request.Options(connectTimeoutMillis, readTimeoutMillis);
//	}
//}
