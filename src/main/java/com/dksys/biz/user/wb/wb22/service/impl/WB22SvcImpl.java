package com.dksys.biz.user.wb.wb22.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.rest.url.Base62Util;
import com.dksys.biz.rest.url.mapper.UrlMapper;
import com.dksys.biz.user.dw.dw02.mapper.DW02Mapper;
import com.dksys.biz.user.qm.qm01.mapper.QM01Mapper;
import com.dksys.biz.user.wb.wb21.mapper.WB21Mapper;
import com.dksys.biz.user.wb.wb22.mapper.WB22Mapper;
import com.dksys.biz.user.wb.wb22.service.WB22Svc;
import com.dksys.biz.util.ExceptionThrower;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB22SvcImpl implements WB22Svc {

	@Autowired
	WB22Mapper wb22Mapper;

	@Autowired
	WB22Svc wb22Svc;

	@Autowired
	CM08Svc cm08Svc;

	@Autowired
	CM15Svc cm15Svc;

	@Autowired
	QM01Mapper QM01Mapper;

	@Autowired
	DW02Mapper dw02Mapper;

	@Autowired
    WB21Mapper wb21Mapper;

	@Autowired
    private UrlMapper urlMapper;

	@Autowired
    private Base62Util base62Util;

	@Autowired
	ExceptionThrower thrower;

	@Override
	public int selectWbsSjListCount(Map<String, String> paramMap) {
		;
		return wb22Mapper.selectWbsSjListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsSjList(Map<String, String> paramMap) {
		return wb22Mapper.selectWbsSjList(paramMap);
	}

	@Override
	public Map<String, String> selectSjInfo(Map<String, String> paramMap) {
		return wb22Mapper.selectSjInfo(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWBS1Level(Map<String, String> paramMap) {
		return wb22Mapper.selectWBS1Level(paramMap);
	}

	@Override
	public int wbsLevel1Insert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

		HashMap<String, String> param = new HashMap<>();
		param.put("coCd", paramMap.get("coCd"));
		param.put("year", paramMap.get("year"));
		param.put("ordrsNo", paramMap.get("salesCd").substring(0, 5));
		// 해당하는 수주번호에 과제확정이면서 일정이 미확정인애들 + TARGET(C/U)
		List<Map<String, String>> salesCdList = wb22Mapper.salesCdList(param);

		int result = 0;
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			int i = 0;
			for (Map<String, String> sharngMap : sharngArr) {
				String wbsPlansDt = sharngMap.get("wbsPlansDt");
				String wbsPlaneDt = sharngMap.get("wbsPlaneDt");
				String daycnt = sharngMap.get("daycnt");
				String creatId = sharngMap.get("creatId");
				String creatPgm = sharngMap.get("creatPgm");
				sharngMap.put("coCd", paramMap.get("coCd"));

				if (!sharngMap.containsKey("fileTrgtKey")) {
					String wbsPlanNo = wb22Mapper.selectMaxWbsPlanNo(paramMap);
					sharngMap.put("wbsPlanNo", wbsPlanNo);

					String fileTrgtKey = wb22Mapper.selectWbsSeqNext(paramMap);
					sharngMap.put("fileTrgtKey", fileTrgtKey);

					sharngMap.put("seq", String.valueOf(i + 1));

					result += wb22Mapper.wbsLevel1Insert(sharngMap);
				} else {
					result += wb22Mapper.wbsLevel1Update(sharngMap);

					// level2 Task정보 변경 처리
					sharngMap.put("wbsPlanCodeKind", sharngMap.get("wbsPlanCodeId"));
					String chkValue = sharngMap.get("wbsPlanMngId");
					if (chkValue == null || chkValue.equals("")) {
						// level1 담당자와 일정이 없으면 TASK등록된 일정 일괄 삭제처리
						result += wb22Mapper.deleteWbsPlanlist(sharngMap);
					} else {
						// level1 담당자로 TASK 담당자 일괄 수정
						result += wb22Mapper.wbsLevel2MngIdUpdate(sharngMap);
						// 실적이 있으면 실적테이블의 id도 수정
						result += wb22Mapper.wbsRsltsMngIdUpdate(sharngMap);
					}
				}
				i++;
				// 프로젝트 전체일경우
				if ("Y".equals(sharngMap.get("allPjtYn"))) {
					for (Map<String, String> salesRow : salesCdList) {
						String salesCd = salesRow.get("salesCd");
						String target  = salesRow.get("target");					// 등록대상 : C , 수정대상 : U
						String closeYn = salesRow.get("closeYn");					// 계획확정 여부
						String wb22VerNo = salesRow.get("wb22VerNo");					// 계획확정 여부
						if ("N".equals(closeYn) && !salesCd.equals(sharngMap.get("salesCd"))) {	// 미확정일정이면서 salesCd가 다를경우에 대한 insert or update 작업
							// salesCd 기준으로 반복이므로, 여기서 salesCd 세팅
							sharngMap.put("salesCd", salesCd);
							sharngMap.put("ordrsNo", salesRow.get("ordrsNo"));

							if ("C".equals(target) && !paramMap.get("salesCd").equals(salesCd)) {	// 아직 일정이 생성안된경우 각 salesCd를 돌면서 insert 처음 들어온 salesCd는 제외 (위에서 이미 insert 작업했음)
								String wbsPlanNo = wb22Mapper.selectMaxWbsPlanNo(param);
								sharngMap.put("wbsPlanNo", wbsPlanNo);

								String fileTrgtKey = wb22Mapper.selectWbsSeqNext(param);
								sharngMap.put("fileTrgtKey", fileTrgtKey);

								sharngMap.put("seq", String.valueOf(i + 1));
								// 신규 버전은 1로 시작
								sharngMap.put("verNo", "1");
								sharngMap.put("creatId",  paramMap.get("userId"));
								sharngMap.put("creatPgm", paramMap.get("pgmId"));

								result += wb22Mapper.wbsLevel1Insert(sharngMap);
							} else if ("U".equals(target)) { // UPDATE 대상
								Map<String, String> updateTargetRow = wb22Mapper.selectWbsPlanTarget(sharngMap);
								if (updateTargetRow != null && !updateTargetRow.isEmpty()) {
									sharngMap.put("fileTrgtKey", updateTargetRow.get("fileTrgtKey"));
									result += wb22Mapper.wbsLevel1Update(sharngMap);
								} else {
									String wbsPlanNo = wb22Mapper.selectMaxWbsPlanNo(param);
									sharngMap.put("wbsPlanNo", wbsPlanNo);

									String fileTrgtKey = wb22Mapper.selectWbsSeqNext(param);
									sharngMap.put("fileTrgtKey", fileTrgtKey);

									sharngMap.put("seq", String.valueOf(i + 1));
									sharngMap.put("verNo", wb22VerNo);
									sharngMap.put("creatId",  paramMap.get("userId"));
									sharngMap.put("creatPgm", paramMap.get("pgmId"));

									result += wb22Mapper.wbsLevel1Insert(sharngMap);
								}
							} else {
								throw new RuntimeException("생성오류!! 전산실에 문의해주세요.");
							}
						}
					}
				}
				// Task계획 포함일경우
				if ("Y".equals(sharngMap.get("taskPlanYn"))) {
					Map<String, String> copyParam = new HashMap<>();
					copyParam.put("coCd", paramMap.get("coCd"));
					copyParam.put("salesCd", paramMap.get("salesCd"));
					copyParam.put("wbsPlanCodeKind", sharngMap.get("wbsPlanCodeId"));  // ★ Level2 쿼리는 부모 Level1 의 codeId 기준
					List<Map<String, String>> copyWBS2Level = wb22Mapper.selectWBS2Level(copyParam);

					// 복사 원본이 없으면 패스
					if (copyWBS2Level == null || copyWBS2Level.isEmpty()) {
						continue;
					}

					// 여기서 부터 수정하면됨 잘보거라!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! 
					for (Map<String, String> salesRow : salesCdList) {
						String wbsPlanCodeId    = sharngMap.get("wbsPlanCodeId");
						String coCd         = sharngMap.get("coCd");
						String salesCd      = salesRow.get("salesCd"); 	
						String closeYn 		= salesRow.get("closeYn");					// 계획확정 여부
						//  && !salesCd.equals(paramMap.get("salesCd"))
						if ("N".equals(closeYn)) {
							Map<String, String> param2 = new HashMap<>();
							param2.put("coCd", coCd);
							param2.put("salesCd", salesCd);
							param2.put("wbsPlanCodeKind", wbsPlanCodeId);  // ★ Level2 쿼리는 부모 Level1 의 codeId 기준
							List<Map<String, String>> selectWBS2Level = wb22Mapper.selectWBS2Level(param2);
							if (selectWBS2Level != null && selectWBS2Level.size() > 0) {
								for (Map<String, String> level2Map : selectWBS2Level) {
									HashMap<String, String> taskParam = new HashMap<>(level2Map);
									taskParam.put("wbsPlansDt", wbsPlansDt);
									taskParam.put("wbsPlaneDt", wbsPlaneDt);
									taskParam.put("daycnt", daycnt);
									taskParam.put("creatId", creatId);
									taskParam.put("creatPgm", creatPgm);
									wb22Mapper.wbsLevel2Update(taskParam);
								}
							} else{
								// task새롭게 insert 작업
								int k = 0;
								for (Map<String, String> tempMap : copyWBS2Level) {
									HashMap<String, String> taskParam = new HashMap<>();
									String wbsPlanNo = wb22Mapper.selectMaxWbsPlanNo(paramMap);
									String newFileTrgtKey = wb22Mapper.selectWbsSeqNext(paramMap);
									taskParam.put("fileTrgtKey", newFileTrgtKey);
									taskParam.put("coCd", paramMap.get("coCd"));
									taskParam.put("wbsPlanNo", wbsPlanNo);
									taskParam.put("wbsPlanCodeKind", sharngMap.get("wbsPlanCodeId"));
									taskParam.put("wbsPlanCodeNm", tempMap.get("wbsPlanCodeNm"));
									taskParam.put("salesCd", salesCd);
									taskParam.put("verNo", "1");  // 신규 버전은 1로 시작
									taskParam.put("wbsPlanMngId", sharngMap.get("wbsPlanMngId"));
									taskParam.put("wbsPlansDt", sharngMap.get("wbsPlansDt"));
									taskParam.put("wbsPlaneDt", sharngMap.get("wbsPlaneDt"));
									taskParam.put("daycnt", sharngMap.get("daycnt"));
									taskParam.put("wbsPlanStsCodeId", sharngMap.get("wbsPlanStsCodeId"));

									int wbsPlanCodeId2 = wb22Mapper.selectMaxWbsCode(sharngMap);
									String codeId = "";
									if (wbsPlanCodeId2 < 10) {
										codeId = "0" + String.valueOf(wbsPlanCodeId2);
									} else {
										codeId = String.valueOf(wbsPlanCodeId2);
									}
									taskParam.put("wbsPlanCodeId", sharngMap.get("wbsPlanCodeId") + codeId);
									taskParam.put("creatId", paramMap.get("userId"));
									taskParam.put("creatPgm", paramMap.get("pgmId"));
									taskParam.put("seq", String.valueOf(k + 1));

									result = wb22Mapper.wbsLevel2Insert(taskParam);
									k++;
								}
							}
						}
					}
				}
				// 실적삭제할 경우
				if ("Y".equals(sharngMap.get("rsltDete"))) {

					 // 1) 실적이 있는 SALES_CD 목록 조회 (wbsRsltsChkExist 재사용)
					Map<String, String> rsltParam = new HashMap<>();
					rsltParam.put("coCd", paramMap.get("coCd"));
					rsltParam.put("ordrsNo", paramMap.get("salesCd").substring(0, 5));
					rsltParam.put("wbsPlanCodeKind", sharngMap.get("wbsPlanCodeId"));
					List<Map<String, String>> rsltExistList = wb22Mapper.wbsRsltsChkExist(rsltParam);
					// SALES_CD만 Set으로 뽑기
					Set<String> rsltSalesCdSet = new HashSet<>();
					for (Map<String, String> row : rsltExistList) {
						rsltSalesCdSet.add(row.get("salesCd"));
					}

					for (Map<String, String> salesRow : salesCdList) {
						String wbsPlanCodeId    = sharngMap.get("wbsPlanCodeId");
						String coCd         = sharngMap.get("coCd");
						String salesCd      = salesRow.get("salesCd"); 	
						String closeYn 		= salesRow.get("closeYn");		// 계획확정 여부
						String wb22VerNo 	= salesRow.get("wb22VerNo");	// 계획버전

						// 계획 확정(Y)이면 건드리면 안되니 기존대로 필터링
						if (!"N".equals(closeYn)) {
							continue;
						}

						// 실적 없는 SALES_CD는 스킵
						if (!rsltSalesCdSet.contains(salesCd)) {
							continue;
						}

						Map<String, String> param3 = new HashMap<>();
						param3.put("coCd", coCd);
						param3.put("salesCd", salesRow.get("salesCd"));
						param3.put("wbsPlanCodeKind", wbsPlanCodeId);
						List<Map<String, String>> targetWBS2Level = wb22Mapper.selectWBS2Level(param3);
						for (Map<String, String> trgRow : targetWBS2Level) {
							if (trgRow.get("rsltsFileTrgtKey") != null) {
								wb22Mapper.wbsRsltsDelete(trgRow);	// task 실적삭제

								//---------------------------------------------------------------
								//첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
								//   필수값 :  jobType, userId, comonCd
								//---------------------------------------------------------------
								HashMap<String, String> fileParam = new HashMap<>();
								fileParam.put("userId", paramMap.get("userId"));
								HashMap<String, String> authParam = new HashMap<>();
								authParam.putAll(paramMap);
								authParam.put("fileTrgtTyp", "WB2201P02");

								// selectTrgtWbsRsltList 에서 FILE_TRGT_KEY 하나씩 꺼내서 처리
								String fileTrgtKey = trgRow.get("rsltsFileTrgtKey");
								if (fileTrgtKey == null || fileTrgtKey.isEmpty()) {
									continue;
								}
								
								// 현재 타겟키 세팅
								authParam.put("fileTrgtKey", fileTrgtKey);

								// 해당 fileTrgtKey 에 매핑된 파일 전체 조회
								List<Map<String, String>> deleteFileList = cm08Svc.selectFileListAll(authParam);

								// 권한체크 + 삭제
								for (Map<String, String> deleteFile : deleteFileList) {
									// 삭제할 파일 하나씩 점검 필요(전체 목록에서 삭제 선택시 필요함)
									// 접근 권한 없으면 Exception 발생
									fileParam.put("comonCd", deleteFile.get("comonCd"));  //삭제할 파일이 보관된 저장 위치 정보
									fileParam.put("coCd", authParam.get("coCd"));
									fileParam.put("jobType", "fileDelete");
									cm15Svc.selectFileAuthCheck(fileParam);

									//---------------------------------------------------------------
									// 첨부 화일 처리  실제 삭제
									//---------------------------------------------------------------
									cm08Svc.deleteFile(deleteFile.get("fileKey"));
								}
								//---------------------------------------------------------------
								//첨부 화일 처리  끝
								//---------------------------------------------------------------
							}
							wb22Mapper.wbsLevel2Delete(trgRow);	// task 계획삭제
						}
						// task새롭게 insert 작업
						HashMap<String, String> insertMap = new HashMap<>();
						insertMap.put("userId", paramMap.get("userId"));
						insertMap.put("wbsPlanCodeId", wbsPlanCodeId);
						List<Map<String, String>> selectWbsTaskTempletList = wb22Mapper.selectWbsTaskTempletList(insertMap);
						if (selectWbsTaskTempletList != null && selectWbsTaskTempletList.size() > 0) {
							int k = 0;
							for (Map<String, String> tempMap : selectWbsTaskTempletList) {
								HashMap<String, String> taskParam2 = new HashMap<>();
								String wbsPlanNo = wb22Mapper.selectMaxWbsPlanNo(paramMap);
								String newFileTrgtKey = wb22Mapper.selectWbsSeqNext(paramMap);
								taskParam2.put("fileTrgtKey", newFileTrgtKey);
								taskParam2.put("coCd", paramMap.get("coCd"));
								taskParam2.put("wbsPlanNo", wbsPlanNo);
								taskParam2.put("wbsPlanCodeKind", sharngMap.get("wbsPlanCodeId"));
								taskParam2.put("wbsPlanCodeNm", tempMap.get("codeNm"));
								taskParam2.put("salesCd", salesCd);
								taskParam2.put("verNo", wb22VerNo);
								taskParam2.put("wbsPlanMngId", sharngMap.get("wbsPlanMngId"));
								taskParam2.put("wbsPlansDt", sharngMap.get("wbsPlansDt"));
								taskParam2.put("wbsPlaneDt", sharngMap.get("wbsPlaneDt"));
								taskParam2.put("daycnt", sharngMap.get("daycnt"));
								taskParam2.put("wbsPlanStsCodeId", sharngMap.get("wbsPlanStsCodeId"));
								taskParam2.put("creatId", paramMap.get("userId"));
								taskParam2.put("creatPgm", "WB2201P01");
								taskParam2.put("verUpReason", sharngMap.get("verUpReason"));

								int wbsPlanCodeId3 = wb22Mapper.selectMaxWbsCode(sharngMap);
								String codeId = "";
								if (wbsPlanCodeId3 < 10) {
									codeId = "0" + String.valueOf(wbsPlanCodeId3);
								} else {
									codeId = String.valueOf(wbsPlanCodeId3);
								}
								taskParam2.put("wbsPlanCodeId", sharngMap.get("wbsPlanCodeId") + codeId);
								taskParam2.put("seq", String.valueOf(k + 1));
								
								result = wb22Mapper.wbsLevel2Insert(taskParam2);
							}
							k++;
						}
						
					}
				}
			}
		}
		return result;
	}

	@Override
	public int wbsLevel1Update(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			for (Map<String, String> sharngMap : sharngArr) {
				result = wb22Mapper.wbsLevel1Update(sharngMap);
			}
		}
		return result;
	}

	@Override
	public List<Map<String, String>> selectWBS2Level(Map<String, String> paramMap) {
		return wb22Mapper.selectWBS2Level(paramMap);
	}

	@Override
	public int wbsLevel2Insert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gson = new Gson();

		int result = 0;

		// Task 계획 저장시 해당 과제가 확정(Y)이면서 Task등록하는 일정이있어야하며 해당 일정도 Y인지 Check
		int wbsLevel2InsertChk = wb22Mapper.wbsLevel2InsertChk(paramMap);
		if (wbsLevel2InsertChk == 0) {
			throw new RuntimeException("Task 계획을 저장할 수 없습니다. 일정확인 후 다시 시도해주세요.");
		}

		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();

		List<Map<String, String>> deletArr = gson.fromJson(paramMap.get("deleteRowArr"), stringList);
		if (deletArr != null && deletArr.size() > 0) {
			for (Map<String, String> sharngMap : deletArr) {
				
			}
		}

		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			int i = 0;
			for (Map<String, String> sharngMap : sharngArr) {
				// System.out.println(sharngMap.get("fileTrgtKey"));
				if (sharngMap.get("fileTrgtKey").length() > 0) {
					sharngMap.put("seq", String.valueOf(i + 1));
					result = wb22Mapper.wbsLevel2Update(sharngMap);
				} else {
					sharngMap.put("coCd", paramMap.get("coCd"));
					String wbsPlanNo = wb22Mapper.selectMaxWbsPlanNo(paramMap);
					sharngMap.put("wbsPlanNo", wbsPlanNo);
					sharngMap.put("wbsPlanCodeKind", paramMap.get("wbsPlanCodeKind"));
					int wbsPlanCodeId = wb22Mapper.selectMaxWbsCode(paramMap);

					String codeId = "";
					if (wbsPlanCodeId < 10) {
						codeId = "0" + String.valueOf(wbsPlanCodeId);
					} else {
						codeId = String.valueOf(wbsPlanCodeId);
					}
					sharngMap.put("wbsPlanCodeId", paramMap.get("wbsPlanCodeKind") + codeId);

					sharngMap.put("seq", String.valueOf(i + 1));
					String fileTrgtKey = wb22Mapper.selectWbsSeqNext(paramMap);
					sharngMap.put("fileTrgtKey", fileTrgtKey);
					result = wb22Mapper.wbsLevel2Insert(sharngMap);
				}
				i++;
			}
		}

		Type stringList1 = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> deleteRowArr = gson.fromJson(paramMap.get("deleteRowArr"), stringList1);
		if (deleteRowArr != null && deleteRowArr.size() > 0) {
			for (Map<String, String> sharngMap : deleteRowArr) {
				// 실적먼저 지워져야함
				wb22Mapper.wbsRsltsDelete(sharngMap);
				result = wb22Mapper.wbsLevel2Delete(sharngMap);
			}
		}
		// 해당 계획에 대한 모든 실적과 Task 계획이 삭제되었으면 도면관리 대장에도 삭제
		int wbsRsltsLevel2Count = wb22Mapper.selectWbsRsltsLevel2Count(paramMap);
		if (wbsRsltsLevel2Count ==  0 && "WBSCODE03".equals(paramMap.get("wbsPlanCodeKind"))) {
			paramMap.put("dwgNo", paramMap.get("salesCd"));
			result += dw02Mapper.deleteDrawDocItem(paramMap);
		}

		return result;
	}

	@Override
	public int wbsVerUpInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		// Ver.up Check : 해당 버전에 ClOSE_YN이 Y인지 체크
		List<Map<String, String>> chkList = wb22Mapper.wbsVerUpInsertChk(paramMap);

		// 조회 결과가 없으면 이미 CLOSE_YN이 'N'인 상태이므로 실행 중지
		if (chkList == null || chkList.isEmpty()) {
			return 0;
		}

		int result = 0;
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			int i = 0;
			for (Map<String, String> sharngMap : sharngArr) {
				sharngMap.put("seq", String.valueOf(i + 1));
				wb22Mapper.wbsVerUpInsert(sharngMap);
				result++;
				i++;
			}
			wb22Mapper.wbsVerUpUpdate(paramMap);
		}
		return result;
	}

	@Override
	public List<Map<String, String>> selectVerNoNext(Map<String, String> paramMap) {
		return wb22Mapper.selectVerNoNext(paramMap);
	}

	@Override
	public int wbsLevel1confirm(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			for (Map<String, String> sharngMap : sharngArr) {
				result = wb22Mapper.wbsLevel1confirm(sharngMap);
				result += wb22Mapper.wbsLevel1confirmAll(sharngMap); // 전체 Y 설정
				sharngMap.put("wbsPlanCodeKind", sharngMap.get("wbsPlanCodeId"));
				String chkValue = sharngMap.get("wbsPlanMngId");
				if (chkValue == null || chkValue.equals("")) {
					// level1 담당자와 일정이 없으면 TASK등록된 일정 일괄 삭제처리
					result += wb22Mapper.deleteWbsPlanlist(sharngMap);
				} else {
					// level1 담당자로 TASK 담당자 일괄 수정
					result += wb22Mapper.wbsLevel2MngIdUpdate(sharngMap);
				}
			}
		}
		return result;
	}

	@Override
	public int wbsLevel2confirm(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			for (Map<String, String> sharngMap : sharngArr) {
				result += wb22Mapper.wbsLevel2confirm(sharngMap);
			}
		}
		return result;
	}

	@Override
	public List<Map<String, String>> selectRsltsSharngList(Map<String, String> paramMap) {
		return wb22Mapper.selectRsltsSharngList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectRsltsApprovalList(Map<String, String> paramMap) {
		return wb22Mapper.selectRsltsApprovalList(paramMap);
	}

	@Override
	public int wbsRsltsInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int fileTrgtKey = wb22Mapper.selectWbsRstlsSeqNext(paramMap);
		paramMap.put("rsltsFileTrgtKey", Integer.toString(fileTrgtKey));

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();

		int result = wb22Mapper.wbsRsltsInsert(paramMap);

		// WBS Task실적 등록시 해당 Task의 WBS_PLAN_CODE_KIND가 'WBSCODE03'(설계완료)이고 실적등록하는 SALES_CD가 도면관리 대장에 없으면 자동 등록처리
		int wbsPlanCodeKindCount = wb22Mapper.selectWbsPlanCodeKindCount(paramMap);
		if (wbsPlanCodeKindCount == 1 && "WBSCODE03".equals(paramMap.get("wbsPlanCodeKind2_P"))) {

			Map<String, String> salesCdMap = new HashMap<>();
			salesCdMap.put("salesCd", paramMap.get("salesCd2_P"));
			Map<String, Object> selectDrawDocItemInfo = dw02Mapper.selectDrawDocItemInfo(salesCdMap);

			Map<String, String> paramMap2 = new HashMap<>();
			paramMap2.put("prdtGrp", (String) selectDrawDocItemInfo.get("prdtGrp"));
			paramMap2.put("equipNm", (String) selectDrawDocItemInfo.get("equipNm"));
			paramMap2.put("clntCd",  (String) selectDrawDocItemInfo.get("clntCd"));
			paramMap2.put("clntNm",  (String) selectDrawDocItemInfo.get("clntNm"));

			paramMap2.put("dwgNo", paramMap.get("salesCd2_P"));
			paramMap2.put("salesCd", paramMap.get("salesCd2_P"));
			paramMap2.put("dsgnEmpNm", paramMap.get("userNm"));
			paramMap2.put("startDt", paramMap.get("wbsRsltssDt"));
			paramMap2.put("remark", paramMap.get("wbsRsltsCnts"));
			paramMap2.put("userId", paramMap.get("userId"));
			paramMap2.put("pgmId", paramMap.get("pgmId"));

			result += dw02Mapper.insertDrawItem(paramMap2);
		}

		Gson gson = new Gson();

		// String pgParam1 = "{\"actionType\":\""+ "T" +"\",";
		// pgParam1 += "\"fileTrgtKey\":\""+ fileTrgtKey +"\",";
		// pgParam1 += "\"coCd\":\""+ paramMap.get("coCd") +"\",";
		// pgParam1 += "\"lvl\":\""+ paramMap.get("lvl") +"\",";
		// pgParam1 += "\"idx\":\""+ paramMap.get("idx") +"\",";
		// pgParam1 += "\"salesCd\":\""+ paramMap.get("salesCd") +"\",";
		// pgParam1 += "\"ordrsNo\":\""+ paramMap.get("ordrsNo") +"\",";
		// pgParam1 += "\"codeKind\":\""+ paramMap.get("codeKind") +"\",";
		// pgParam1 += "\"codeId\":\""+ paramMap.get("codeId") +"\"}";

		String pgParam = "{\"fileTrgtKey\":\"" + fileTrgtKey + "\"}";

//		String todoTitle1 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + "(" + paramMap.get("salesCd2_P") + ") " + paramMap.get("wbsPlanCodeNm2_P") + " 실적 공유";
//		String todoTitle1 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + paramMap.get("salesCd2_P") + " 실적 공유";
		String todoTitle1 = "[WBS실적_등록]" + paramMap.get("salesCd2_P") + paramMap.get("wbsPlanCodeNm2_P");

		// String todoTitle = "TASK명 : " + paramMap.get("wbsPlanCodeNm2_P") + ", SALES CODE : " + paramMap.get("salesCd2_P") + " 실적일자 : " +
		// paramMap.get("wbsRsltssDt") + " ~ " + paramMap.get("wbsRsltseDt");

		Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
		if (sharngArr != null && sharngArr.size() > 0) {
			int i = 0;
			for (Map<String, String> sharngMap : sharngArr) {
				sharngMap.put("reqNo", paramMap.get("wbsRsltsNo"));
				sharngMap.put("fileTrgtKey", paramMap.get("rsltsFileTrgtKey"));
				sharngMap.put("pgmId", paramMap.get("pgmId"));
				sharngMap.put("userId", paramMap.get("userId"));
				sharngMap.put("sanCtnSn", Integer.toString(i + 1));
				sharngMap.put("pgParam", pgParam);
				sharngMap.put("todoTitle", todoTitle1);
				QM01Mapper.insertWbsSharngList(sharngMap);
				i++;
			}
		}

//		String todoTitle2 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + "(" + paramMap.get("salesCd2_P") + ") " + paramMap.get("wbsPlanCodeNm2_P") + " 실적 결재";
//		String todoTitle2 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + paramMap.get("salesCd2_P") + " 실적 결재";
		String todoTitle2 = "[WBS실적_등록]" + paramMap.get("salesCd2_P") + paramMap.get("wbsPlanCodeNm2_P");

		// 결재
		Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
		if (approvalArr != null && approvalArr.size() > 0) {
			int i = 0;
			for (Map<String, String> approvalMap : approvalArr) {
//	            try {
				approvalMap.put("reqNo", paramMap.get("wbsRsltsNo"));
				approvalMap.put("fileTrgtKey", paramMap.get("rsltsFileTrgtKey"));
				approvalMap.put("pgmId", paramMap.get("pgmId"));
				approvalMap.put("userId", paramMap.get("userId"));
				approvalMap.put("sanCtnSn", Integer.toString(i + 1));
				approvalMap.put("pgParam", pgParam);
				approvalMap.put("todoTitle", todoTitle2);
				QM01Mapper.insertWbsApprovalList(approvalMap);
				i++;
//	            } catch (Exception e) {
//	                System.out.println("error2"+e.getMessage());
//	            }
			}
		}

		// ---------------------------------------------------------------
		// 첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
		// 필수값 : jobType, userId, comonCd
		// ---------------------------------------------------------------
		List<Map<String, String>> uploadFileList = gsonDtl.fromJson(paramMap.get("uploadFileArr"), dtlMap);
		if (uploadFileList.size() > 0) {
			// 접근 권한 없으면 Exception 발생
			paramMap.put("jobType", "fileUp");
			cm15Svc.selectFileAuthCheck(paramMap);
		}
		// ---------------------------------------------------------------
		// 첨부 화일 권한체크 끝
		// ---------------------------------------------------------------
		// ---------------------------------------------------------------
		// 첨부 화일 처리 시작 (처음 등록시에는 화일 삭제할게 없음)
		// ---------------------------------------------------------------
		if (uploadFileList.size() > 0) {
			paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
			paramMap.put("fileTrgtKey", paramMap.get("rsltsFileTrgtKey"));
			cm08Svc.uploadFile(paramMap, mRequest);
		}
		// ---------------------------------------------------------------
		// 첨부 화일 처리 끝
		// ---------------------------------------------------------------

		return result;
	}

	@Override
	public int wbsRsltsUpdate(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();

		// ---------------------------------------------------------------
		// 첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
		// 필수값 : jobType, userId, comonCd
		// ---------------------------------------------------------------
		HashMap<String, String> param = new HashMap<>();
		param.put("userId", paramMap.get("userId"));
		param.put("coCd", paramMap.get("coCd"));
		param.put("comonCd", paramMap.get("comonCd")); // 프로트엔드에 넘어온 화일 저장 위치 정보

		List<Map<String, String>> uploadFileList = gsonDtl.fromJson(paramMap.get("uploadFileArr"), dtlMap);
		if (uploadFileList.size() > 0) {
			// 접근 권한 없으면 Exception 발생 (jobType, userId, comonCd 3개 필수값 필요)
			param.put("jobType", "fileUp");
			cm15Svc.selectFileAuthCheck(param);
		}
		String[] deleteFileArr = gsonDtl.fromJson(paramMap.get("deleteFileArr"), String[].class);
		List<String> deleteFileList = Arrays.asList(deleteFileArr);
		for (String fileKey : deleteFileList) { // 삭제할 파일 하나씩 점검 필요(전체 목록에서 삭제 선택시 필요함)
			Map<String, String> fileInfo = cm08Svc.selectFileInfo(fileKey);
			// 접근 권한 없으면 Exception 발생
			param.put("comonCd", fileInfo.get("comonCd")); // 삭제할 파일이 보관된 저장 위치 정보
			param.put("jobType", "fileDelete");
			cm15Svc.selectFileAuthCheck(param);
		}
		// ---------------------------------------------------------------
		// 첨부 화일 권한체크 끝
		// ---------------------------------------------------------------

		int result = wb22Mapper.wbsRsltsUpdate(paramMap);
		Gson gson = new Gson();

		String pgParam = "{\"fileTrgtKey\":\"" + paramMap.get("rsltsFileTrgtKey") + "\"}";

//		String todoTitle1 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + "(" + paramMap.get("salesCd2_P") + ") " + paramMap.get("wbsPlanCodeNm2_P") + " 실적 공유";
//		String todoTitle1 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + paramMap.get("salesCd2_P") + " 실적 공유";
		String todoTitle1 = "[WBS실적_변경]" + paramMap.get("salesCd2_P") + paramMap.get("wbsPlanCodeNm2_P");

		paramMap.put("reqNo", paramMap.get("wbsRsltsNo"));
		paramMap.put("salesCd", paramMap.get("salesCd2_P"));

		QM01Mapper.deleteWbsSharngList(paramMap);
		if (Integer.parseInt(paramMap.get("approvalYnCnt")) == 0) {
//			QM01Mapper.deleteWbsSharngList(paramMap);

			Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {
			}.getType();
			List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
			if (sharngArr != null && sharngArr.size() > 0) {
				int i = 0;
				for (Map<String, String> sharngMap : sharngArr) {
					try {
						sharngMap.put("reqNo", paramMap.get("wbsRsltsNo"));
						sharngMap.put("fileTrgtKey", paramMap.get("rsltsFileTrgtKey"));
						sharngMap.put("pgmId", paramMap.get("pgmId"));
						sharngMap.put("userId", paramMap.get("userId"));
						sharngMap.put("sanCtnSn", Integer.toString(i + 1));
						sharngMap.put("pgParam", pgParam);
						sharngMap.put("todoTitle", todoTitle1);
						QM01Mapper.insertWbsSharngList(sharngMap);
						i++;
					} catch (Exception e) {
						System.out.println("error2" + e.getMessage());
					}
				}
			}

//			String todoTitle2 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + "(" + paramMap.get("salesCd2_P") + ") " + paramMap.get("wbsPlanCodeNm2_P") + " 실적 결재";
			String todoTitle2 = "[WBS실적_변경]" + paramMap.get("salesCd2_P") + paramMap.get("wbsPlanCodeNm2_P");

			// 결재
			Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {
			}.getType();
			List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
			if (approvalArr != null && approvalArr.size() > 0) {
				int i = 0;
				for (Map<String, String> approvalMap : approvalArr) {
					try {
						approvalMap.put("reqNo", paramMap.get("wbsRsltsNo"));
						approvalMap.put("fileTrgtKey", paramMap.get("rsltsFileTrgtKey"));
						approvalMap.put("pgmId", paramMap.get("pgmId"));
						approvalMap.put("userId", paramMap.get("userId"));
						approvalMap.put("sanCtnSn", Integer.toString(i + 1));
						approvalMap.put("pgParam", pgParam);
						approvalMap.put("todoTitle", todoTitle2);
						QM01Mapper.insertWbsApprovalList(approvalMap);
						i++;
					} catch (Exception e) {
						System.out.println("error2" + e.getMessage());
					}
				}
			}

		}

		// List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(paramMap);
		// if (sharngChk.size() == 0) {
		// QM01Mapper.deleteWbsSharngList(paramMap);
		// }

		// List<Map<String, String>> approvalgChk = QM01Mapper.deleteWbsApprovalListChk(paramMap);
		// if (approvalgChk.size() > 0) {
		// QM01Mapper.deleteWbsApprovalList(paramMap);
		// }

		// ---------------------------------------------------------------
		// 첨부 화일 처리 시작
		// ---------------------------------------------------------------
		if (uploadFileList.size() > 0) {
			paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
			paramMap.put("fileTrgtKey", paramMap.get("rsltsFileTrgtKey"));
			cm08Svc.uploadFile(paramMap, mRequest);
		}

		for (String fileKey : deleteFileList) {
			cm08Svc.deleteFile(fileKey);
		}
		// ---------------------------------------------------------------
		// 첨부 화일 처리 끝
		// ---------------------------------------------------------------

		return result;
	}

	@Override
	public int wbsRsltsconfirm(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;
//		try {
		result = wb22Mapper.wbsRsltsconfirm(paramMap);

		// 실적 확정시 모든 실적 확정 상태이면 해당 과제의 REL_DT 업데이트 처리
		int closeYnChk = wb22Mapper.wbsRsltsCloseChk(paramMap);
		if (closeYnChk == 1 && "WBSCODE03".equals(paramMap.get("wbsPlanCodeKind"))) {
			result += dw02Mapper.updateRelDtNew(paramMap);
		} else {
			if ("N".equals(paramMap.get("flag"))) {
				result += dw02Mapper.updateRelDtInit(paramMap);
			}
		}
//		} catch (Exception e) {
//			System.out.println("error2" + e.getMessage());
//		}
		return result;
	}

	@Override
	public List<Map<String, String>> selectTodoRsltsView(Map<String, String> paramMap) {
		return wb22Mapper.selectTodoRsltsView(paramMap);
	}

	@Override
	public List<Map<String, String>> selectIncompleteJob(Map<String, String> paramMap) {
		return wb22Mapper.selectIncompleteJob(paramMap);
	}

	@Override
	public void callCopyWbsPlan(Map<String, String> paramMap) {
		wb22Mapper.callCopyWbsPlan(paramMap);
	}

	@Override
	public int selectWbsTaskTempletCount(Map<String, String> paramMap) {
		return wb22Mapper.selectWbsTaskTempletCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsTaskTempletList(Map<String, String> paramMap) {
		return wb22Mapper.selectWbsTaskTempletList(paramMap);
	}

	@Override
	public int saveWbsTaskTempletList(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		int result = 0;
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();

		List<Map<String, String>> delArr = gson.fromJson(paramMap.get("taskDeleteRowArr"), stringList);
		if (delArr != null && delArr.size() > 0) {
			for (Map<String, String> sharngMap : delArr) {
				result = wb22Mapper.wbsTaskTempletDelete(sharngMap);
			}
		}

		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			for (Map<String, String> sharngMap : sharngArr) {
				// 상위 코드
				sharngMap.put("codeKind", paramMap.get("wbsPlanCodeId"));
				// codeId 값이 없으면 insert
				if (sharngMap.get("codeId").isEmpty()) {
					String wbsPlanNo = wb22Mapper.selectNewWbsTaskTempletCd(sharngMap);
					sharngMap.put("codeId", wbsPlanNo);

					result = wb22Mapper.wbsTaskTempletInsert(sharngMap);
				} else {
					result = wb22Mapper.wbsTaskTempletUpdate(sharngMap);
				}

			}
		}

		return result;
	}

	// 유저별 템플릿 serviceImplement
	// ------------------------------------------------------------------------------------------------------
	@Override
	public int selectWbsUserTaskTempletCount(Map<String, String> paramMap) {
		return wb22Mapper.selectWbsUserTaskTempletCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsUserTaskTempletList(Map<String, String> paramMap) {
		return wb22Mapper.selectWbsUserTaskTempletList(paramMap);
	}

	@Override
	public int saveWbsUserTaskTempletList(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		int result = 0;
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();

		result = wb22Mapper.wbsUserTaskTempletDelete(paramMap);

		result = 0;
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			for (Map<String, String> sharngMap : sharngArr) {
				// 상위 코드
				sharngMap.put("codeKind", paramMap.get("codeKind"));
				sharngMap.put("codeDesc", paramMap.get("codeDesc"));
				sharngMap.put("userId", paramMap.get("userId"));
				// codeId 값이 없으면 insert
				if (sharngMap.get("codeId").isEmpty()) {
					result = wb22Mapper.wbsUserTaskTempletInsert(sharngMap);
				} else {
					result = wb22Mapper.wbsUserTaskTempletUpdate(sharngMap);
				}

			}
		}

		return result;
	}

	// ------------------------------------------------------------------------------------------------------

	@Override
	public List<Map<String, String>> selectHistWBS1Level(Map<String, String> paramMap) {
		return wb22Mapper.selectHistWBS1Level(paramMap);
	}

	// 계획일괄복사부분
	@Override
	public int ModalwbsPlanconfirmListCount(Map<String, String> paramMap) {
		return wb22Mapper.ModalwbsPlanconfirmListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> ModalwbsPlanconfirmList(Map<String, String> paramMap) {
		return wb22Mapper.ModalwbsPlanconfirmList(paramMap);
	}

	@Override
	public int confirm_copy(Map<String, String> paramMap) {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		HashMap<String, String> param = new HashMap<>();
		param.put("userId", paramMap.get("userId"));

		// 데이터처리 시작
		int result = 0;

		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("rowList"), dtlMap);

		String userId = "";
		String sYear = "";
		userId = paramMap.get("userId");
		sYear = paramMap.get("year");

		if (dtlParam.size() > 0) {

			for (Map<String, String> dtl : dtlParam) {

				String dtaChk = dtl.get("dtaChk").toString();
				dtl.put("creatId", userId);
				dtl.put("year", sYear);

				if ("U".equals(dtaChk)) {

					List<Map<String, String>> chkCount = wb22Mapper.selectWbcPlan(dtl);

					if (chkCount.size() == 0) {
						// insert
						List<Map<String, String>> chkList = wb22Mapper.selectWbcPlan(paramMap);

						for (Map<String, String> chkMap : chkList) {

							dtl.put("wbsPlanCodeId", chkMap.get("wbsPlanCodeId"));
							dtl.put("verNo", "1");
							dtl.put("sortNo", chkMap.get("seq"));
							dtl.put("wbsPlanMngId", chkMap.get("wbsPlanMngId"));
							dtl.put("wbsPlansDt", chkMap.get("wbsPlansDt"));
							dtl.put("wbsPlaneDt", chkMap.get("wbsPlaneDt"));
							dtl.put("daycnt", chkMap.get("daycnt"));
							dtl.put("wbsPlanStsCodeId", chkMap.get("wbsPlanStsCodeId"));
							dtl.put("creatPgm", chkMap.get("creatPgm"));

							String wbsPlanNo = wb22Mapper.selectMaxWbsPlanNo(dtl);
							dtl.put("wbsPlanNo", wbsPlanNo);

							String fileTrgtKey = wb22Mapper.selectWbsSeqNext(dtl);
							dtl.put("fileTrgtKey", fileTrgtKey);

							result = wb22Mapper.wbsLevel1Insert(dtl);
						}
					} else {
						// update
						List<Map<String, String>> chkList = wb22Mapper.selectWbcPlan(paramMap);

						if (chkList.size() == 0) { // 데이터가 없을시 대상데이터를 null 셋팅한다.
							dtl.put("wbsPlanCodeId", "");
							dtl.put("wbsPlanMngId", "");
							dtl.put("wbsPlansDt", "");
							dtl.put("wbsPlaneDt", "");
							dtl.put("daycnt", "");
							dtl.put("wbsPlanStsCodeId", "");
							dtl.put("creatPgm", "WB2201M01");

							result = wb22Mapper.updateWbcPlan(dtl);
						} else {
							for (Map<String, String> chkMap : chkList) {

								dtl.put("wbsPlanCodeId", chkMap.get("wbsPlanCodeId"));
								dtl.put("wbsPlanMngId", chkMap.get("wbsPlanMngId"));
								dtl.put("wbsPlansDt", chkMap.get("wbsPlansDt"));
								dtl.put("wbsPlaneDt", chkMap.get("wbsPlaneDt"));
								dtl.put("daycnt", chkMap.get("daycnt"));
								dtl.put("wbsPlanStsCodeId", chkMap.get("wbsPlanStsCodeId"));
								dtl.put("creatPgm", chkMap.get("creatPgm"));

								result = wb22Mapper.updateWbcPlan(dtl);
							}
						}
					}

				}
			}

		}
		// 데이터 처리 끝
		return result;
	}

	@Override
	public List<Map<String, String>> selectWbcPlanTodoList(Map<String, String> paramMap) {
		return wb22Mapper.selectWbcPlanTodoList(paramMap);
	}

	@Override
	public int wbcPlanTodoInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();

		int result = 0;

		Gson gson = new Gson();

		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			int i = 0;
			for (Map<String, String> sharngMap : sharngArr) {
				String pgParam = "{\"coCd\":\"" + sharngMap.get("coCd") + "\",";
				pgParam += "\"salesCd\":\"" + sharngMap.get("salesCd") + "\",";
				pgParam += "\"planVerNo\":\"" + sharngMap.get("verNo") + "\",";
				pgParam += "\"histYn\":\"" + sharngMap.get("histYn") + "\"}";
				sharngMap.put("sanCtnSn", Integer.toString(i + 1));
				sharngMap.put("pgParam", pgParam);
				result = QM01Mapper.insertWbsSharngList(sharngMap);
				i++;
			}
		}
		return result;
	}

	// 일괄확정부분
	@Override
	public int Modalwb22noconfirmListCount(Map<String, String> paramMap) {
		return wb22Mapper.Modalwb22noconfirmListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> Modalwb22noconfirmList(Map<String, String> paramMap) {
		return wb22Mapper.Modalwb22noconfirmList(paramMap);
	}

	// 일괄확정
	@Override
	public int confirm_wb22(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		HashMap<String, String> param = new HashMap<>();
		param.put("userId", paramMap.get("userId"));
		param.put("comonCd", paramMap.get("comonCd")); // 프로트엔드에 넘어온 화일 저장 위치 정보

		// 데이터처리 시작
		int result = 0;

		// 상세수정
		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
		for (Map<String, String> dtl : dtlParam) {
			// 반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
			// dtl.put("userId", paramMap.get("userId"));
			// dtl.put("pgmId", paramMap.get("pgmId"));

			String dataChk = dtl.get("dataChk").toString();
			// "dataChk" 값을 확인하여 "I"인 경우 세부정보를 삽입
			if ("U".equals(dataChk)) {
				// 데이터 처리
				result = wb22Mapper.confirm_wb22(dtl);
			}
		}
		// 데이터 처리 끝
		return result;
	}
	// 일괄확정부분 끝

	@Override
	public List<Map<String, String>> selectWbcPlanUpdteTodoList(Map<String, String> paramMap) {
		return wb22Mapper.selectWbcPlanUpdteTodoList(paramMap);
	}

	@Override
	public Map<String, String> wbsResultLastVerNoSearch(Map<String, String> paramMap) {
		return wb22Mapper.wbsResultLastVerNoSearch(paramMap);
	}

	// wbs계획 관리 변경사항 저장
	@Override
	public int updateWbsChanges(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;
		
		// Gson을 사용하여 JSON 데이터를 파싱
		Gson gson = new Gson();
		Type listType = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();

		// JSON 형식의 "changedData" 추출
		List<Map<String, String>> changedDataList = gson.fromJson(paramMap.get("changedData"), listType);

		// 데이터가 존재할 경우 처리
		if (changedDataList != null && !changedDataList.isEmpty()) {
			for (Map<String, String> changedData : changedDataList) {
				// 필요시 추가 처리 (예: coCd, salesCd와 같은 추가 필드 처리)
				changedData.put("coCd", paramMap.get("coCd"));
				changedData.put("salesCd", paramMap.get("salesCd"));

				result += wb22Mapper.updateWbsChanges(changedData);
			}
		}
		
		return result;
	}

	@Override
	public Map<String, String> generateShortUrl(Map<String, String> paramMap) {

		String longUrl = paramMap.get("longUrl");
        String strId = urlMapper.getUrlIdByLongUrl(longUrl);
        int id = 0;
        if (strId == null || strId.equals("")) {
        	String chkCode = "QRGeration"; // 비밀코드는 고정 문자열 -> DB에 저장용도임
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
    	returnMap.put("shortUrl", paramMap.get("hostAddress") + "/s/" + shortUrl);
    	returnMap.put("chkCode", urlMapper.getChkCodeById(id));
    	
        return returnMap;
	}
	


	@Override
	public int wbsLevel1GantUpdate(Map<String, String> paramMap) {
		return wb22Mapper.wbsLevel1GantUpdate(paramMap);
	}
	
	@Override
	public int wbsLevel2GantInsert(Map<String, String> paramMap) {
		String fileTrgtKey = wb22Mapper.selectWbsSeqNext(paramMap);
		paramMap.put("fileTrgtKey", fileTrgtKey);

		String wbsPlanNo = wb22Mapper.selectMaxWbsPlanNo(paramMap);
		paramMap.put("wbsPlanNo", wbsPlanNo);
		
		int wbsPlanCodeId = wb22Mapper.selectMaxWbsCode(paramMap);

		String codeId = "";
		if (wbsPlanCodeId < 10) {
			codeId = "0" + String.valueOf(wbsPlanCodeId);
		} else {
			codeId = String.valueOf(wbsPlanCodeId);
		}
		paramMap.put("wbsPlanCodeId", paramMap.get("wbsPlanCodeKind") + codeId);
		
		return wb22Mapper.wbsLevel2Insert(paramMap);
	}
	
	@Override
	public int wbsRsltsGantInsert(Map<String, String> paramMap) {
		int fileTrgtKey = wb22Mapper.selectWbsRstlsSeqNext(paramMap);
		paramMap.put("rsltsFileTrgtKey", Integer.toString(fileTrgtKey));

		return wb22Mapper.wbsRsltsInsert(paramMap);
	}
	
	//간트에서 일정 삭제 요청 처리
	@Override
	public int wbsRsltsGantDelete(Map<String, String> paramMap) {
		String cat = paramMap.get("cat");
		if (cat == null || cat.equals("PLAN")) {
			if (wb22Mapper.wbsRsltsGantDeleteChk(paramMap) > 0 ) {	// 실적자료가 존재하면 삭제 불가능
				return 0;
			} else {
				return wb22Mapper.wbsLevel2Delete(paramMap);	// 담당자 계획 삭제
			}
		} else if (cat == null || cat.equals("DO")) {
			return wb22Mapper.wbsRsltsGantDelete(paramMap);		// 담당자 실적 삭제
		} else {
			return 0;
		}
	}

	@Override
	public int updateWbsPlan(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		// 미확정 필터링 로직 제거: 확정/미확정 상관없이 버전업 처리
		// List<Map<String, String>> chkList = wb22Mapper.wbsVerUpInsertChk(paramMap);
		// 조회 결과가 없으면 이미 CLOSE_YN이 'N'인 상태이므로 실행 중지 -> 미확정으로 들어온 경우 방지
		// if (chkList == null || chkList.isEmpty()) {
		// 	return 0;
		// }

		int wbsIssueExistChk = wb22Mapper.wbsIssueExistChk(paramMap);
		if (wbsIssueExistChk > 0 && "delete".equals(paramMap.get("changeType"))) {
			throw new RuntimeException("이미 문제가 등록되어 있어 삭제할 수 없습니다.");
		}

		int result = 0;

		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			int i = 0;
			// 최초 일정 변경시 insert 작업
			for (Map<String, String> sharngMap : sharngArr) {
				if (!sharngMap.containsKey("fileTrgtKey")) {
					String wbsPlanNo = wb22Mapper.selectMaxWbsPlanNo(paramMap);
					sharngMap.put("wbsPlanNo", wbsPlanNo);

					String fileTrgtKey = wb22Mapper.selectWbsSeqNext(paramMap);
					sharngMap.put("fileTrgtKey", fileTrgtKey);

					sharngMap.put("seq", String.valueOf(i + 1));
					sharngMap.put("verNo", "1");

					result += wb22Mapper.wbsLevel1Insert(sharngMap);
				} 
				// else {
				// 	sharngMap.put("seq", String.valueOf(i + 1));
				// 	wb22Mapper.wbsVerUpInsert(sharngMap);		// Histoty Insert
				// }
				result++;
				i++;
			}
			// wb22Mapper.wbsVerUpUpdate(paramMap);	// verNo Update
			// if (paramMap.get("flag") != null && "Y".equals(paramMap.get("flag"))) {
			// 	wb22Mapper.wbsLevel1confirmAll(paramMap); // 전체 Y 설정
			// }
		}

		// 내용삭제만 실적 및 파일삭제처리
		String changeType = paramMap.get("changeType");
		if ("delete".equals(changeType)) {
			// 일정 WBS실적 변경
			HashMap<String, String> dtlMap = new HashMap<>();
			dtlMap.put("coCd", paramMap.get("coCd"));
			dtlMap.put("salesCd", paramMap.get("salesCd"));
			dtlMap.put("wbsPlanCodeId", paramMap.get("wbsPlanCodeId"));
			dtlMap.put("wbsPlanMngId", "");
			dtlMap.put("wbsPlansDt", "");
			dtlMap.put("wbsPlaneDt", "");
			dtlMap.put("daycnt", "0");
			dtlMap.put("wbsPlanStsCodeId", "WBSPLANSTS10");
			dtlMap.put("creatId", paramMap.get("creatId"));
			dtlMap.put("creatPgm", paramMap.get("creatPgm"));
			result += wb22Mapper.updateWbcPlan(dtlMap);

			// TASK 계획삭제
			result += wb22Mapper.deleteWbsTask(paramMap);

			List<Map<String, String>> selectTrgtWbsRsltList = wb22Mapper.selectTrgtWbsRsltList(paramMap);
			//---------------------------------------------------------------
			//첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
			//   필수값 :  jobType, userId, comonCd
			//---------------------------------------------------------------
			HashMap<String, String> param = new HashMap<>();
			param.put("userId", paramMap.get("userId"));

			HashMap<String, String> param2 = new HashMap<>();
			param2.putAll(paramMap);
			param2.put("fileTrgtTyp", "WB2201P02");

			// selectTrgtWbsRsltList 에서 FILE_TRGT_KEY 하나씩 꺼내서 처리
			if (selectTrgtWbsRsltList != null) {
				for (Map<String, String> trgt : selectTrgtWbsRsltList) {
					String fileTrgtKey = trgt.get("fileTrgtKey");
					if (fileTrgtKey == null || fileTrgtKey.isEmpty()) {
						continue;
					}

					// 현재 타겟키 세팅
					param2.put("fileTrgtKey", fileTrgtKey);

					// 해당 fileTrgtKey 에 매핑된 파일 전체 조회
					List<Map<String, String>> deleteFileList = cm08Svc.selectFileListAll(param2);

					// 권한체크 + 삭제
					for (Map<String, String> deleteFile : deleteFileList) {
						// 삭제할 파일 하나씩 점검 필요(전체 목록에서 삭제 선택시 필요함)
						// 접근 권한 없으면 Exception 발생
						param.put("comonCd", deleteFile.get("comonCd"));  //삭제할 파일이 보관된 저장 위치 정보
						param.put("coCd", param2.get("coCd"));
						param.put("jobType", "fileDelete");
						cm15Svc.selectFileAuthCheck(param);

						//---------------------------------------------------------------
						// 첨부 화일 처리  실제 삭제
						//---------------------------------------------------------------
						cm08Svc.deleteFile(deleteFile.get("fileKey"));
					}
				}
			}
			//---------------------------------------------------------------
			//첨부 화일 처리  끝
			//---------------------------------------------------------------

			// 실적삭제
			result += wb22Mapper.deleteWbsRslt(paramMap);
		} else if ("mngId".equals(changeType) || "date".equals(changeType)) {
			// 담당자/일자 변경: 변경된 데이터 저장
			if (sharngArr != null && sharngArr.size() > 0) {
				for (Map<String, String> sharngMap : sharngArr) {
					// 변경된 데이터 업데이트
					result += wb22Mapper.wbsLevel1Update(sharngMap);
					// 실적 담당자 업데이트
					if (sharngMap.get("wbsPlanCodeId").equals(paramMap.get("wbsPlanCodeId"))) {
						sharngMap.put("wbsPlanCodeKind", paramMap.get("wbsPlanCodeId"));
						// level1 담당자로 TASK 담당자 일괄 수정
						result += wb22Mapper.wbsLevel2MngIdUpdate(sharngMap);
						// 실적이 있으면 실적테이블의 id도 수정
						result += wb22Mapper.wbsRsltsMngIdUpdate(sharngMap);
					}
				}
			}
		}

		return result;
	}

	@Override
	public List<Map<String, String>> wbsRsltsChkExist(Map<String, String> paramMap) {
		return wb22Mapper.wbsRsltsChkExist(paramMap);
	}


	@Override
	public int wb22OrdrsNoVersionUp(Map<String, String> paramMap, MultipartHttpServletRequest mRequest)throws Exception {
		// List<Map<String, String>> salesCdList = wb22Mapper.salesCdList(paramMap);

		int result = 0;
		Gson gsonDtl = new Gson();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
		if (dtlParam != null && !dtlParam.isEmpty()) {
			for (Map<String, String> dtl : dtlParam) {
				if ("Y".equals(dtl.get("closeYn"))) {
					Map<String, String> param = new HashMap<>();
					param.put("userId", paramMap.get("userId"));
					param.put("coCd", dtl.get("coCd"));
					param.put("salesCd", dtl.get("salesCd"));
					param.put("beginDt", String.valueOf(dtl.get("beginDt")));
					param.put("acptncDt", String.valueOf(dtl.get("acptncDt")));
					param.put("reqreDaycnt", String.valueOf(dtl.get("reqreDaycnt")));
					List<Map<String, String>> selectWBS1Level = wb22Mapper.selectWBS1Level(param);
					if (selectWBS1Level != null && selectWBS1Level.size() > 0) {
						int i = 0;
						for (Map<String, String> level1Map : selectWBS1Level) {
							level1Map.put("seq", String.valueOf(i + 1));
							wb22Mapper.wbsVerUpInsert(level1Map);
							result++;
							i++;
						}
						Map<String, String> param2 = new HashMap<>();
						param2.put("newVerNo", String.valueOf(Integer.parseInt(dtl.get("planVerNo")) + 1));
						param2.put("verUpReason", paramMap.get("verUpReason"));
						param2.put("coCd", dtl.get("coCd"));
						param2.put("salesCd", dtl.get("salesCd"));
						param2.put("verNo", dtl.get("planVerNo"));

						wb22Mapper.wbsVerUpUpdate(param2);
					}
				}
			}
		}
		return result;
	}
}