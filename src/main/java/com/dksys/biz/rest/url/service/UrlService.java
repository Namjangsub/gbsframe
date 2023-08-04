package com.dksys.biz.rest.url.service;

import java.util.List;
import java.util.Map;

public interface UrlService {

    List<Map<String, Object>> getUrls();

	String getLongUrlByshortUrl(String shortUrl);

	String generateShortUrl(Map<String, String> paramMap);
	
	String shortUrlPromissChkCode(String shortUrl);
}