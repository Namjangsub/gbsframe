package com.dksys.biz.rest.url.service.impl;

import com.dksys.biz.rest.url.Base62Util;
import com.dksys.biz.rest.url.UrlTypeValidation;
import com.dksys.biz.rest.url.mapper.UrlMapper;
import com.dksys.biz.rest.url.service.UrlService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class UrlServiceImpl implements UrlService{
	
    @Autowired
    private UrlMapper urlMapper;

    @Autowired
    private UrlTypeValidation urlTypeValidation;

    @Autowired
    private Base62Util base62Util;

    @Override
    public List<Map<String, Object>> getUrls() {
        return urlMapper.getUrls();
    }

    @Override
    public String generateShortUrl(Map<String, String> paramMap){
    	String longUrl = paramMap.get("longUrl");
        if(!urlTypeValidation.valid(longUrl)){
            throw new IllegalArgumentException("잘못된 URL 타입입니다.");
        }

//        longUrl = longUrl.replace("https://","").replace("http://",""); //전부제거
        longUrl = longUrl.replaceFirst("^(https?://)", ""); //한번만 제거
        String strId = urlMapper.getUrlIdByLongUrl(longUrl);
        int id = 0;
        if (strId == null || strId.equals("")) {
        	String chkCode = base62Util.createCode(); // 비밀코드 생성
//        	Map<String, String> paramMap = new HashMap<>();
        	paramMap.put("longUrl", longUrl);
            paramMap.put("chkCode", chkCode);
        	id = urlMapper.insertLongUrl(paramMap);

        	Object paramValue = paramMap.get("id");
        	if (paramValue != null && paramValue instanceof Integer) {
        		id = ((Integer) paramValue).intValue();
        	}

        	if (id > 0) {
//        		id = Integer.parseInt(paramMap.get("id"));
        	} else {
        		throw new IllegalArgumentException("등록중 오류가 발생 했습니다.");
        	}
        	
        } else {
        	id = Integer.parseInt(strId);
        }
        String shortUrl = base62Util.encoding(id);

        return "http://localhost/s/"+shortUrl;
    }

    @Override
    public String getLongUrlByshortUrl(String shortUrl) {
        int id = base62Util.decoding(shortUrl);
    	urlMapper.addUrlCount(id);
        return urlMapper.getLongUrlById(id);
    }
    
    @Override
    public String shortUrlPromissChkCode(String longUrl) {
    	longUrl = longUrl.replaceFirst("^(https?://)", ""); //한번만 제거
        String strId = urlMapper.getUrlIdByLongUrl(longUrl);
    	return urlMapper.getChkCodeById(Integer.parseInt(strId));
    }

}