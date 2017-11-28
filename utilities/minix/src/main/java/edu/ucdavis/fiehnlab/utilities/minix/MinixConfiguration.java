package edu.ucdavis.fiehnlab.utilities.minix;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinixConfiguration {

    @Bean
    public SXStudyFileReader studyReader() {
        return new SXStudyFileReader();
    }
}
