package com.dksys.biz.openaitest.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OllamaService {

	private final RestTemplate restTemplate;

	// API 엔드포인트 URL (필요에 따라 application.properties에서 값을 주입할 수 있습니다.)
	@Value("${ollama.api.url:http://ai.gunyangitt.co.kr:11434/api/generate}")
	private String apiUrl;

	// 기본 모델 이름을 application.properties 또는 기본값("ollamachat")에서 주입
	@Value("${ollama.model:phi4:latest}")
	private String ollamaModel;

	public OllamaService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	/********************************************************
	 * API 호출하여 주어진 모델과 프롬프트에 대한 응답을 반환합니다.
	 *
	 * @param modelName
	 * 호출할 모델 이름
	 * @param prompt
	 * 프롬프트 텍스트
	 * @return API 응답 중 "response" 필드의 값
	 *******************************************************/
	public String queryOllama(Map<String, String> param) {
		// 요청 헤더 구성
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// 요청 본문 구성
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("model", ollamaModel);
		requestBody.put("prompt", param.get("prompt"));
		requestBody.put("stream", false);

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

		// POST 요청 전송
		ResponseEntity<Map> responseEntity = restTemplate.postForEntity(apiUrl, requestEntity, Map.class);

		if (!responseEntity.getStatusCode().is2xxSuccessful()) {
			throw new RuntimeException("API 호출 실패: " + responseEntity.getStatusCode());
		}

		// 응답 본문에서 "response" 필드 추출
		Map<String, Object> responseBody = responseEntity.getBody();
		if (responseBody != null && responseBody.containsKey("response")) {
			return responseBody.get("response").toString();
		} else {
			throw new RuntimeException("응답에 'response' 필드가 없습니다.");
		}
	}
}
