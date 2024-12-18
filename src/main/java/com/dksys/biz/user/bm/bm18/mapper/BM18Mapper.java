package com.dksys.biz.user.bm.bm18.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BM18Mapper {
	
	//알림톡수신 카운트
	int selectReceptionMessageListCount(Map<String, String> paramMap);

	//알림톡수신 조회
	List<Map<String, String>> selectReceptionMessageList(Map<String, String> paramMap);

	//수신번호 채번	
	List<Map<String, String>> selectMaxMessageId(Map<String, String> paramMap);		
	
	//todo 알림톡
	List<Map<String, String>> selectMaxMessageIdTodo(Map<String, String> paramMap);

    List<Map<String, String>> selectMaxMessageIdTodoCompleted(Map<String, String> paramMap);
	
	//todo 알림톡New 남장섭 일부 최적화
	List<Map<String, String>> selectMaxMessageIdTodoNew(Map<String, String> paramMap);

	//todo 알림톡
	List<Map<String, String>> selectMaxMessageIdTodo_kakao(Map<String, String> paramMap);
	
	String selectKakaoSeqNext(Map<String, String> paramMap);	
	
	//알림톡 로그등록
	int insertKakaoMessage(Map<String, String> paramMap);	
	
	List<Map<String, String>> selectPmMaxMessageIdTodo(Map<String, String> paramMap);	
	
	List<Map<String, String>> selectWbsPlanMaxMessageIdTodo(Map<String, String> paramMap);	
	
    List<Map<String, String>> selectWbsPlanUpdateMaxMessageIdTodo(Map<String, String> paramMap);
}

