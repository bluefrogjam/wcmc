package edu.ucdavis.fiehnlab.ms.carrot.core.preprocess;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;

import java.io.File;

@Configuration
@EnableIntegration
@IntegrationComponentScan
public class PreprocessContext {
	@Value(value = "input_path")
	String INBOUND_PATH;

	@Bean
	@InboundChannelAdapter(value = "rawFileIn", poller = @Poller(fixedDelay="1000"))
	public MessageSource<?> rawFileReader() {
		FileReadingMessageSource source = new FileReadingMessageSource();
		source.setDirectory(new File(INBOUND_PATH));
		return source;
	}


}
