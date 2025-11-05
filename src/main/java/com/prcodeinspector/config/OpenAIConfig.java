package com.prcodeinspector.config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class OpenAIConfig {

	@Bean
	@Primary
	public OpenAiApi openAiApi(@Value("${spring.ai.openai.api-key}") String apiKey) {
		return new OpenAiApi(apiKey);
	}

	@Bean
	@Primary
	public OpenAiChatModel chatModel(OpenAiApi openAiApi) {
		return new OpenAiChatModel(openAiApi);
	}
}