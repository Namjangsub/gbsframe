package com.dksys.biz.user.bm.bm18.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.util.ExceptionThrower;
import com.dksys.biz.user.bm.bm18.mapper.BM18Mapper;
import com.dksys.biz.user.bm.bm18.service.BM18Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class BM18SvcImpl implements BM18Svc {

	@Autowired
	BM18Mapper bm18Mapper;
	
	@Autowired
	BM18Svc bm18Svc;

	@Autowired
	ExceptionThrower thrower;

	//알림톡수신 카운트
	@Override
	public int selectReceptionMessageListCount(Map<String, String> paramMap) {
		return bm18Mapper.selectReceptionMessageListCount(paramMap);
	}

	//알림톡수신 조회
	@Override
	public List<Map<String, String>> selectReceptionMessageList(Map<String, String> paramMap) {
		return bm18Mapper.selectReceptionMessageList(paramMap);
	}
	
	//수신번호 채번	
	public List<Map<String, String>> selectMaxMessageId(Map<String, String> paramMap) {
			
		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
		list = bm18Mapper.selectMaxMessageId(paramMap);
		return list;
	}	
	
	//todo 수신번호 메시지 내용 채번	
	public List<Map<String, String>> selectMaxMessageIdTodo(Map<String, String> paramMap) {

		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
		list = bm18Mapper.selectMaxMessageIdTodo(paramMap);
		return list;
	}
	
	//todo 수신번호 메시지 내용 채번 NEW	
	public List<Map<String, String>> selectMaxMessageIdTodoNew(Map<String, String> paramMap) {

		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
		list = bm18Mapper.selectMaxMessageIdTodoNew(paramMap);
		return list;
	}
	
	//todo 수신번호 메시지 내용 채번	
	public List<Map<String, String>> selectMaxMessageIdTodo_kakao(Map<String, String> paramMap) {
		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
		list = bm18Mapper.selectMaxMessageIdTodo_kakao(paramMap);
		return list;
	}
	
	@Override
	public int insertKakaoMessage(Map<String, String> paramMap) throws Exception {

		int result = 0;		
	    result = bm18Mapper.insertKakaoMessage(paramMap);	

		return result;
	}	
	
	@Override
	public List<Map<String, String>> selectPmMaxMessageIdTodo(Map<String, String> paramMap) {
		return bm18Mapper.selectPmMaxMessageIdTodo(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectWbsPlanMaxMessageIdTodo(Map<String, String> paramMap) {
		return bm18Mapper.selectWbsPlanMaxMessageIdTodo(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectWbsPlanUpdateMaxMessageIdTodo(Map<String, String> paramMap) {
		return bm18Mapper.selectWbsPlanUpdateMaxMessageIdTodo(paramMap);
	}
}