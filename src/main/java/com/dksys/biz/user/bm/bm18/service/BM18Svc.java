package com.dksys.biz.user.bm.bm18.service;

import java.util.List;
import java.util.Map;


public interface BM18Svc {

	//알림톡수신 카운트
	int selectReceptionMessageListCount(Map<String, String> paramMap);
	
	//알림톡수신 조회
	List<Map<String, String>> selectReceptionMessageList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectMaxMessageId(Map<String, String> paramMap);

	//알림톡 messageId 채번(KKO+연도2자리+시퀀스6자리). 서버 큐 발송이 결재요청 직접 발송과 같은 형식의 messageId 를 쓰기 위함
	String selectKakaoSeqNext(Map<String, String> paramMap);
	
	List<Map<String, String>> selectMaxMessageIdTodo(Map<String, String> paramMap);
	
    List<Map<String, String>> selectMaxMessageIdTodoCompleted(Map<String, String> paramMap);

	List<Map<String, String>> selectMaxMessageIdTodoNew(Map<String, String> paramMap);
	
	List<Map<String, String>> selectMaxMessageIdTodo_kakao(Map<String, String> paramMap);

	//알림톡 로그저장
	int insertKakaoMessage(Map<String, String> paramMap) throws Exception;	
	
	List<Map<String, String>> selectPmMaxMessageIdTodo(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanMaxMessageIdTodo(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanUpdateMaxMessageIdTodo(Map<String, String> paramMap);

}