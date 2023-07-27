package com.dksys.biz.user.mail.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MailMapper {
	
	Map<String, String> selectUserInfo(Map<String, String> paramMap);

	int selectMailCount(Map<String, String> paramMap);

	List<Map<String, String>> selectMailList(Map<String, String> paramMap);

	int insertMail(Map<String, String> paramMap);
	
	int updateMailError(Map<String, String> paramMap);
	
	int deleteMail(Map<String, String> paramMap);
	
}