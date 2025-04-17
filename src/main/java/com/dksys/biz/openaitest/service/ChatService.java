package com.dksys.biz.openaitest.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.dksys.biz.openaitest.dto.ChatGPTRequest;
import com.dksys.biz.openaitest.dto.ChatGPTResponse;

@Service
public class ChatService {

	@Autowired
	private RestTemplate template;

	// API 엔드포인트 URL (필요에 따라 application.properties에서 값을 주입할 수 있습니다.)
	@Value("${openai.url:https://api.openai.com/v1/chat/completions}")
	private String apiURL;

	@Value("${openai.model:chatgpt-4o-latest}")
	private String apimodel;

	public ChatService(RestTemplate template) {
		this.template = template;
	}

	public String queryOpenApi(Map<String, String> param) {

		String queryMessage = param.get("originMsg") + param.get("prompt");
		ChatGPTRequest request = new ChatGPTRequest(apimodel, queryMessage);
		ChatGPTResponse chatGPTResponse = template.postForObject(apiURL, request, ChatGPTResponse.class);
		return chatGPTResponse.getChoices().get(0).getMessage().getContent();
	}
}