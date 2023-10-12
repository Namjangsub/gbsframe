package com.dksys.biz.rest.url.service;

import java.util.List;
import java.util.Map;

public interface UrlService {

	public List<Map<String, Object>> getUrls();

    public String getLongUrlByshortUrl(String shortUrl);

	public Map<String, String> generateShortUrl(Map<String, String> paramMap);
	
	public String shortUrlPromissChkCode(String shortUrl);
}