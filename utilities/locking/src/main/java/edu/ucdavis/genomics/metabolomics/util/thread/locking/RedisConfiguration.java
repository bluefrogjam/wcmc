package edu.ucdavis.genomics.metabolomics.util.thread.locking;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("sjp.locking.redis")
public class RedisConfiguration {

    @Bean
    public RedissonClient redissonClient(){
        return Redisson.create();
    }
}
