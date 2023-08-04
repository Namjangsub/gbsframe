package com.dksys.biz.rest.url.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface UrlMapper {
	
    List<Map<String, Object>> getUrls();

    String getUrlIdByLongUrl(String longUrl);

    int insertLongUrl(Map<String, String> paramMap);

    String getLongUrlById(int id);
    
    int addUrlCount(int id);
    
    int updateChkCode(Map<String, String> paramMap);
    
    String getChkCodeById(int id);
    
}