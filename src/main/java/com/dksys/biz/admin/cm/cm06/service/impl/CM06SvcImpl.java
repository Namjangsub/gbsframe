package com.dksys.biz.admin.cm.cm06.service.impl;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.admin.cm.cm04.mapper.CM04Mapper;
import com.dksys.biz.admin.cm.cm05.service.CM05Svc;
import com.dksys.biz.admin.cm.cm06.mapper.CM06Mapper;
import com.dksys.biz.admin.cm.cm06.service.CM06Svc;
import com.dksys.biz.config.RequestUtils;
import com.dksys.biz.user.bm.bm18.service.BM18Svc;
import com.dksys.biz.user.wb.wb20.service.WB20Svc;
import com.google.gson.Gson;

@Service
@Transactional
public class CM06SvcImpl implements CM06Svc {

    @Autowired
    CM06Mapper cm06Mapper;
    
    @Autowired
    CM04Mapper cm04Mapper;

	@Autowired
	CM06Svc cm06Svc;
	
	@Autowired
	CM05Svc cm05Svc;
	
	@Autowired
	WB20Svc wb20Svc;
	
	@Autowired
	BM18Svc bm18Svc;

    @Override
	public int selectUserCount(Map<String, String> paramMap) {
		return cm06Mapper.selectUserCount(paramMap);
	}
    
	@Override
	public List<Map<String, String>> selectUserList(Map<String, String> paramMap) {
		return cm06Mapper.selectUserList(paramMap);
	}
	
	@Override
	public Map<String, String> selectUserInfo(Map<String, String> paramMap) {
		return cm06Mapper.selectUserInfo(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectUserTree(Map<String, String> paramMap) {
		List<Map<String, String>> deptTree = cm04Mapper.selectDeptTree(paramMap);
		List<Map<String, String>> useTree = cm06Mapper.selectUserTree(paramMap);
		deptTree.addAll(useTree);
		return deptTree;
	}
	
	@Override
	public List<Map<String, String>> selectSignUserTree(Map<String, String> paramMap) {
		List<Map<String, String>> deptTree = cm04Mapper.selectDeptSignTree(paramMap);
		List<Map<String, String>> useSignTree = cm06Mapper.selectSignUserTree(paramMap);
		deptTree.addAll(useSignTree);
		return deptTree;
	}

	@Override
	public void insertUser(Map<String, String> paramMap) throws Exception{
		cm06Mapper.insertUser(paramMap);
		cm06Mapper.insertUserOauth(paramMap);

		// 이미지 업로드를 위한 로직 (타입 변경 및 필요 매개변수 삽입)
		// 이미지 Insert 및 Update에 관한 모듈로 분리
		if (paramMap.containsKey("userImg")) {
			Map<String, Object> imgParam = new HashMap<>();
			imgParam.put("userId", paramMap.get("userId"));
			imgParam.put("userImg", paramMap.get("userImg"));
			
			cm06Svc.updateUserImg(imgParam);
		}
	}
	
	@Override
	public void updateUser(Map<String, String> paramMap) throws Exception {
		cm06Mapper.updateUser(paramMap);

		// 이미지 업로드를 위한 로직 (타입 변경 및 필요 매개변수 삽입)
		// 이미지 Insert 및 Update에 관한 모듈로 분리
		if (paramMap.containsKey("userImg")) {
			Map<String, Object> imgParam = new HashMap<>();
			imgParam.put("userId", paramMap.get("userId"));
			imgParam.put("userImg", paramMap.get("userImg"));
			
			cm06Svc.updateUserImg(imgParam);
		}
	}

	@Override
	public int insertPgmHistory(Map<String, String> paramMap) {
		return cm06Mapper.insertPgmHistory(paramMap);
	}

	@Override
	public int updatePw(Map<String, String> paramMap) {
		int result = 0;
		result += cm06Mapper.updatePw(paramMap);
		result += cm06Mapper.updateTokenPw(paramMap);
		return result;
	}
    
	@Override
	public List<Map<String, String>> selectRuleCheckList(Map<String, String> paramMap) {
		return cm06Mapper.selectRuleCheckList(paramMap); 
	}

	@Override
	public Map<String, String> updatePwErrCnt(Map<String, String> paramMap) {
		cm06Mapper.updatePwErrCnt(paramMap);
		Map<String, String> usrInfo = cm06Mapper.selectUserInfo(paramMap);
		if(Integer.parseInt(usrInfo.get("passErrCnt")) >= 10) {
			cm06Mapper.updateUserN(paramMap);
			
			//계정잠김 시 전산관리자들에게 알림톡 발송
			try {
				Map<String, String> codeMap = new HashMap<>();
				codeMap.put("codeId", "SPECRTS06");
				Map<String, String> codeDetail = cm05Svc.selectCodeInfo(codeMap);
				if (codeDetail != null) {
					String codeEtc = codeDetail.get("codeEtc");
					if (codeEtc != null && !codeEtc.isEmpty() && !"null".equals(codeEtc)) {
						String[] admins = codeEtc.split(",");
						List<Map<String, String>> sharngList = new ArrayList<>();
						int sanctnSn = 1;
						String reqUserId = paramMap.get("userId");
						String todoNo = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date()) + "_" + reqUserId;
						if(todoNo.length() > 20) {
							todoNo = todoNo.substring(0, 20);
						}
						
						String mngNm = usrInfo.get("name");
						String reqNm = (mngNm != null && !mngNm.isEmpty()) ? mngNm : reqUserId;
						String displayTitle = "계정잠금 안내 (" + reqNm + ")";

						// 1. WB20M03 먼저 INSERT (빈 pgParam으로)
						for (String adminId : admins) {
							adminId = adminId.trim();
							Map<String, String> sharng = new HashMap<>();
							sharng.put("gb", "공유");
							sharng.put("todoDiv1CodeId", "TODODIV10");
							sharng.put("todoDiv2CodeId", "TODODIV1099");
							sharng.put("todoId", adminId);
							sharng.put("userId", reqUserId);
							sharng.put("coCd", "GUN");
							sharng.put("todoCoCd", "GUN");
							sharng.put("sanctnSn", String.valueOf(sanctnSn++));
							sharng.put("todoTitl", displayTitle);
							sharng.put("todoNo", todoNo);
							sharng.put("salesCd", " ");
							sharng.put("pgPath", "/user/bm/bm18/BM1801P01.html");
							sharng.put("pgParam", "{}");
							sharng.put("sanctnSttus", "N");
							sharng.put("fileTrgtKey", todoNo);
							sharng.put("reqNo", todoNo);
							sharng.put("pgmId", "LOGIN");
							sharngList.add(sharng);
						}
						Map<String, String> todoParam = new HashMap<>();
						todoParam.put("approvalArr", new Gson().toJson(sharngList));
						wb20Svc.insertTodoMaster(todoParam);
						
						// 2. selectMaxMessageIdTodo로 messageId + template + 수신자 정보 한번에 조회
						Map<String, String> kakaoTmplParam = new HashMap<>();
						kakaoTmplParam.put("coCd", "GUN");
						kakaoTmplParam.put("todoDiv1CodeId", "TODODIV10");
						kakaoTmplParam.put("todoDiv2CodeId", "TODODIV1099");
						kakaoTmplParam.put("todoNo", todoNo);
						kakaoTmplParam.put("tmplatDiv", "TMPLATDIV02");
						List<Map<String, String>> messageList = bm18Svc.selectMaxMessageIdTodo(kakaoTmplParam);
						
						if (messageList != null) {
							String sendDtFormatted = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(java.time.LocalDateTime.now());
							List<Map<String, String>> updateSharngList = new ArrayList<>();
							int updateSn = 1;
							
							for (Map<String, String> msgInfo : messageList) {
								String adminId = msgInfo.get("todoId");
								String adminName = msgInfo.get("name") != null ? msgInfo.get("name") : "";
								String mobile = msgInfo.get("telNo");
								String maxMessageId = msgInfo.get("maxMessageId");
								String messageDesc = msgInfo.get("messageDesc");
								String msgSendDt = msgInfo.get("sendDt") != null ? msgInfo.get("sendDt") : sendDtFormatted;
								
								if (mobile == null || mobile.isEmpty()) continue;
								
								// 3. 메시지 치환
								String finalMessage = messageDesc;
								if (finalMessage != null && !finalMessage.isEmpty()) {
									String mngTel = usrInfo.get("telNo");
									String reqTel = (mngTel != null && !mngTel.isEmpty()) ? mngTel : "전화번호 없음";
									finalMessage = finalMessage.replace("#{todoTitl}", displayTitle);
									finalMessage = finalMessage.replace("#{title}", displayTitle);
									finalMessage = finalMessage.replace("#{todoDiv1CodeNm}", "공유");
									finalMessage = finalMessage.replace("#{todoDiv2CodeNm}", "계정잠금 공유");
									finalMessage = finalMessage.replace("#{sanctnDiv2}", "공유");
									finalMessage = finalMessage.replace("#{sendDt}", msgSendDt);
									finalMessage = finalMessage.replace("#{ordrgMngNm}", reqNm);
									finalMessage = finalMessage.replace("#{ordrgMngTelNo}", reqTel);
									finalMessage = finalMessage.replace("#{nameTo}", adminName);
									finalMessage = finalMessage.replace("#{rcvNm}", adminName);
								}
								
								// 4. BM18 이력 저장
								Map<String, String> logParam = new HashMap<>();
								logParam.put("mssageId", maxMessageId);
								logParam.put("rcvId", adminId);
								logParam.put("rcvNm", adminName);
								logParam.put("clntCd", "1");
								logParam.put("tmplatDiv", "TMPLATDIV02");
								logParam.put("sendgStatus", "OK");
								logParam.put("title", displayTitle);
								logParam.put("mssage", finalMessage);
								logParam.put("mobile", mobile);
								logParam.put("nameTo", adminName);
								logParam.put("creatId", reqUserId);
								logParam.put("creatPgm", "LOGIN");
								bm18Svc.insertKakaoMessage(logParam);
								
								// 5. 이력 데이터 기반 pgParam 조립 → WB20M03 UPDATE (MERGE)
								Map<String, String> pgParamMap = new HashMap<>();
								pgParamMap.put("messageId", maxMessageId);
								pgParamMap.put("rcvId", adminId);
								pgParamMap.put("rcvNm", adminName);
								pgParamMap.put("clntCd", "1");
								pgParamMap.put("clntNm", "(주)건양아이티티");
								pgParamMap.put("mngNm", reqNm);
								pgParamMap.put("mngTelNo", usrInfo.get("telNo") != null ? usrInfo.get("telNo") : "");
								pgParamMap.put("tmplatDiv", "TMPLATDIV02");
								pgParamMap.put("tmplatDivNm", "To-Do 요청");
								pgParamMap.put("sendgStatus", "READY");
								pgParamMap.put("sendgStatusNm", "발송대기");
								pgParamMap.put("sendgStatusChk", "대기");
								pgParamMap.put("sendgStatusYn", "N");
								pgParamMap.put("title", displayTitle);
								pgParamMap.put("message", finalMessage);
								pgParamMap.put("mobile", mobile);
								pgParamMap.put("nameTo", adminName);
								pgParamMap.put("sendDt", sendDtFormatted);
								
								Map<String, String> updateSharng = new HashMap<>();
								updateSharng.put("gb", "공유");
								updateSharng.put("todoDiv1CodeId", "TODODIV10");
								updateSharng.put("todoDiv2CodeId", "TODODIV1099");
								updateSharng.put("todoId", adminId);
								updateSharng.put("userId", reqUserId);
								updateSharng.put("coCd", "GUN");
								updateSharng.put("todoCoCd", "GUN");
								updateSharng.put("sanctnSn", String.valueOf(updateSn++));
								updateSharng.put("todoTitl", displayTitle);
								updateSharng.put("todoNo", todoNo);
								updateSharng.put("salesCd", " ");
								updateSharng.put("pgPath", "/user/bm/bm18/BM1801P01.html");
								updateSharng.put("pgParam", new Gson().toJson(pgParamMap));
								updateSharng.put("sanctnSttus", "N");
								updateSharng.put("fileTrgtKey", todoNo);
								updateSharng.put("reqNo", todoNo);
								updateSharng.put("pgmId", "LOGIN");
								updateSharngList.add(updateSharng);
								
								// 6. 카카오 API 발송
								String sendgStatus = "ERR";
								try {
									URL url = new URL("https://talkapi.lgcns.com/request/kakao.json");
									HttpURLConnection conn = (HttpURLConnection) url.openConnection();
									conn.setRequestMethod("POST");
									conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
									conn.setRequestProperty("authToken", "e/eDfZOFCsrBqadaECQ0+g==");
									conn.setRequestProperty("serverName", "gyitt2400");
									conn.setRequestProperty("paymentType", "P");
									conn.setDoOutput(true);
									
									Map<String, String> talkBody = new HashMap<>();
									talkBody.put("service", "2310086157");
									talkBody.put("messageId", maxMessageId);
									talkBody.put("title", displayTitle);
									talkBody.put("message", finalMessage);
									talkBody.put("mobile", mobile);
									talkBody.put("template", "10003");
									
									String jsonInputString = new Gson().toJson(talkBody);
									try(OutputStream os = conn.getOutputStream()) {
										byte[] input = jsonInputString.getBytes("utf-8");
										os.write(input, 0, input.length);
									}
									int responseCode = conn.getResponseCode();
									sendgStatus = responseCode == 200 ? "OK" : "ERR" + responseCode;
								} catch(Exception kakaoEx) {
									kakaoEx.printStackTrace();
								}
								
								// 발송 결과로 pgParam 업데이트
								boolean isSuccess = "OK".equals(sendgStatus);
								pgParamMap.put("sendgStatus", sendgStatus);
								pgParamMap.put("sendgStatusNm", isSuccess ? "정상코드(SUCCESS)" : "발송실패");
								pgParamMap.put("sendgStatusChk", isSuccess ? "성공" : "실패");
								pgParamMap.put("sendgStatusYn", isSuccess ? "Y" : "N");
								updateSharng.put("pgParam", new Gson().toJson(pgParamMap));
							}
							
							// 7. WB20M03 pgParam UPDATE (MERGE로 기존 데이터 업데이트)
							Map<String, String> updateTodoParam = new HashMap<>();
							updateTodoParam.put("approvalArr", new Gson().toJson(updateSharngList));
							wb20Svc.insertTodoMaster(updateTodoParam);
						}
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return usrInfo;
	}
    
	@Override
	public List<Map<String, String>> selectUserStatusList(Map<String, String> paramMap) {
		String userAgent = RequestUtils.getUserAgent();
        String deviceType = RequestUtils.detectDeviceType(userAgent);
		String clientIp = RequestUtils.getClientIp();
		paramMap.put("userAgent", userAgent);
		paramMap.put("clientIp", clientIp);
		paramMap.put("deviceType", deviceType);
		return cm06Mapper.selectUserStatusList(paramMap); 
	}

	@Override
	public int updateUserStatus(Map<String, String> paramMap) {
		int result = 0;
		result += cm06Mapper.updateUserStatus(paramMap);
		return result;
	}
    
	@Override
	public List<Map<String, String>> selectEmployeeStatusList(Map<String, String> paramMap) {
		return cm06Mapper.selectEmployeeStatusList(paramMap); 
	}


	@Override
	public Map<String, String> checkUserIdImage(Map<String, String> paramMap) {
		return cm06Mapper.checkUserIdImage(paramMap);
	}

	// 이미지 업로드
	@Override
	public void updateUserImg(Map<String, Object> paramMap) throws Exception {
		if (paramMap.containsKey("userImg")) {
			Object userImgObj = paramMap.get("userImg");
			if (userImgObj instanceof String) {
				String userImgStr = (String) userImgObj;
				// 원하는 형식인 경우에만 바이트 배열로 변환하여 저장합니다.
				if (userImgStr != null && userImgStr.startsWith("data:application/octet-stream;base64,")) {
					// 단순히 문자열을 UTF-8 바이트 배열로 변환합니다.
					byte[] userImgBytes = userImgStr.getBytes(StandardCharsets.UTF_8);
					paramMap.put("userImg", userImgBytes);
				} else {
					paramMap.put("userImg", null);
				}
			}
		}
		cm06Mapper.updateUserImg(paramMap);
	}

	@Override
	public List<Map<String, String>> checkUserIdImageList(Map<String, String> paramMap) {
		return cm06Mapper.checkUserIdImageList(paramMap);
	}
}