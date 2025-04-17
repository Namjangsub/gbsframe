package com.dksys.biz.openaitest.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.dksys.biz.openaitest.service.ChatService;
import com.dksys.biz.openaitest.service.OllamaService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/bot")
public class CustomBotCtr {
	private Logger logger = LoggerFactory.getLogger(CustomBotCtr.class);

	// 기본 모델 이름을 application.properties 또는 기본값("ollamachat")에서 주입
	@Value("${ollama.model:phi4:latest}")
	private String ollamaModel;

	// OpenApi사용 유무 application.properties 에서 주입
	@Value("${openai.enabled}")
	private Boolean openaiExec;

	// ollama사용 유무 application.properties 에서 주입
	@Value("${ollama.enabled}")
	private Boolean ollamaExec;
	
	@Autowired
	private RestTemplate template;

	@Autowired
	ChatService chatService;

	@Autowired
	OllamaService ollamaService;

//	@PostMapping("/chat")
//	public String chat(@RequestParam(name = "prompt") String prompt) {
//		ChatGPTRequest request = new ChatGPTRequest(apimodel, prompt);
//		ChatGPTResponse chatGPTResponse = template.postForObject(apiURL, request, ChatGPTResponse.class);
//		return chatGPTResponse.getChoices().get(0).getMessage().getContent();
//	}

	@PostMapping("/chatRtv")
	public String chatRtv(@RequestBody Map<String, String> param, ModelMap model) {
		String aiType = param.get("aiType");
		String chatTempString = "";
		if ("GPT".equals(aiType)) {
			if (openaiExec) {
				chatTempString = chatService.queryOpenApi(param);
			} else {
				chatTempString = param.get("originMsg");
			}
		} else {
			if (ollamaExec) {
				chatTempString = ollamaService.queryOllama(param);
			} else {
				chatTempString = param.get("originMsg");
			}
		}
		model.addAttribute("chatgpt", chatTempString);
		return "jsonView";
	}

}