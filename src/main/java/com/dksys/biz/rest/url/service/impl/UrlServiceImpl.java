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
    public Map<String, String> generateShortUrl(Map<String, String> paramMap){
    	String longUrl = paramMap.get("longUrl");
//        if(!urlTypeValidation.valid(longUrl)){ //정상적인 접속이 안될경우 오류 발생으로 임시 삭제 2023.10.12 13:23분
//            throw new IllegalArgumentException("잘못된 URL 타입입니다.");
//        }

//        longUrl = longUrl.replace("https://","").replace("http://",""); //전부제거
        longUrl = longUrl.replaceFirst("^(https?://)", ""); //한번만 제거
        String strId = urlMapper.getUrlIdByLongUrl(longUrl);
        int id = 0;
        if (strId == null || strId.equals("")) {
        	/******************************************************
        	 * 비밀코드 생성 및 메일 자료에 첨부해서 이후 사용자가 자료 접근시 체크 사용
        	 * 비밀코드의 길이는 5자리로 생성함 (필요시 길이 조절가능)
        	 * URL최초 등록시 한번 생성, URL 생명주기 3주정도, 지정 필요--> DB스케쥴로 관리
        	 * -------> DB procedure 작성 연계 -----------------------
        	 */
        	String chkCode = base62Util.createCode(); // 비밀코드 생성
        	/******************************************************/
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

    	Map<String, String> returnMap = new HashMap<>();
    	returnMap.put("shortUrl", paramMap.get("hostAddress") + "/s/"+shortUrl);
    	returnMap.put("chkCode", urlMapper.getChkCodeById(id));
    	
        return returnMap;
    }

    @Override
    public String getLongUrlByshortUrl(String shortUrl) {
        int id = base62Util.decoding(shortUrl);
    	urlMapper.addUrlCount(id);
        return urlMapper.getLongUrlById(id);
    }
    
    @Override
    public String shortUrlPromissChkCode(String longUrl) {
    	longUrl = longUrl.replaceFirst("^(https?://)", ""); // http://, https:// 한번만 제거
        String strId = urlMapper.getUrlIdByLongUrl(longUrl);
    	return urlMapper.getChkCodeById(Integer.parseInt(strId));
    }

}