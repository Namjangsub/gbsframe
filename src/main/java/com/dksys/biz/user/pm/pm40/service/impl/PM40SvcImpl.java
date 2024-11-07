package com.dksys.biz.user.pm.pm40.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.pm.pm40.mapper.PM40Mapper;
import com.dksys.biz.user.pm.pm40.service.PM40Svc;
import com.dksys.biz.user.qm.qm01.mapper.QM01Mapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;



@Service
@Transactional(rollbackFor = Exception.class)
public class PM40SvcImpl implements PM40Svc{

	
	 @Autowired
	  PM40Mapper pm40Mapper;
	
	// @Autowired
	 // QM01Mapper QM01Mapper;
	 
	 
	 	
	 
		@Override
		public int selectMainGridListCount(Map<String, String> paramMap) {
			return pm40Mapper.selectMainGridListCount(paramMap);
		}
		
		@Override
		public List<Map<String, String>> selectMainGridList(Map<String, String> paramMap) {
			return pm40Mapper.selectMainGridList(paramMap);
		}
		
		
		
		
		@Override
		public Map<String, String> insert_pm40(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {	 
			Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
			Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
			int result = 0;
			//해당월의 고찰데이터가 있는지 없는지 확인
			
			Map rtnMap = new HashMap();
			
			int gochalIn = pm40Mapper.select_gochal_count(paramMap);

			if (gochalIn == 0) {
				//int fileTrgtKey = pm402Mapper.select_qm02_SeqNext(paramMap);
				//paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));

				String newMNGM_NO = pm40Mapper.select_pm40_Next_MNGM_NO(paramMap);
				paramMap.put("workNo", newMNGM_NO);
				paramMap.put("reqNo", newMNGM_NO);
				
				result = pm40Mapper.insert_pm40(paramMap);
				rtnMap.put("result", String.valueOf(result)); //문자열로 변환하여 rtnMap에 "result"키로 저장
				rtnMap.put("workNo", newMNGM_NO);// rtnMap에 "workNo"키로 저장
				Gson gson = new Gson();	
				
				List<Map<String, String>> sharngChk = pm40Mapper.deleteWbsSharngListChk(paramMap); 
				if (sharngChk.size() > 0) {
					pm40Mapper.deleteWbsSharngList(paramMap); 
				}
				
				//공유
				String pgParam1 = "{\"actionType\":\""+ "T" +"\",";
				//pgParam1 += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\","; 
				pgParam1 += "\"coCd\":\""+ paramMap.get("coCd") +"\","; 
				pgParam1 += "\"workYm\":\""+ paramMap.get("workYm") +"\",";
				//if (!"".equals(paramMap.get("workRptNo"))) {	//issNo가 존재하면 문제건, 없으면 정상건
				pgParam1 += "\"userId\":\""+ paramMap.get("userId") +"\",";
				//}
				pgParam1 += "\"gubun\":\""+ "개인" +"\",";
				pgParam1 += "\"workNo\":\""+ paramMap.get("workNo") +"\"}";
				
				
				//결재
				String pgParam2 = "{\"actionType\":\""+ "S" +"\",";
				//pgParam2 += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\","; 
				pgParam2 += "\"coCd\":\""+ paramMap.get("coCd") +"\","; 
				pgParam2 += "\"workYm\":\""+ paramMap.get("workYm") +"\",";
				//if (!"".equals(paramMap.get("workRptNo"))) {	//issNo가 존재하면 문제건, 없으면 정상건
				pgParam2 += "\"userId\":\""+ paramMap.get("userId") +"\",";
				//}
				pgParam2 += "\"gubun\":\""+ "개인" +"\",";
				pgParam2 += "\"workNo\":\""+ paramMap.get("workNo") +"\"}";
				
				//공유-결재
				Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
				List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("approvalArr"), stringList2);
				if (sharngArr != null && sharngArr.size() > 0 ) {
					int iSharng = 1;
					int iApproval = 1;
			        for (Map<String, String> sharngMap : sharngArr) {

		        	    sharngMap.put("reqNo", paramMap.get("reqNo"));
		        	    sharngMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
		        	    sharngMap.put("pgmId", paramMap.get("pgmId"));
		        	    sharngMap.put("userId", paramMap.get("userId"));
		        	    
			        	if ("공유".equals(sharngMap.get("gb"))) {
			            	    sharngMap.put("sanCtnSn",Integer.toString(iSharng));
			            	    sharngMap.put("pgParam", pgParam1);
			            	    pm40Mapper.insertWbsSharngList(sharngMap);       		
			                	iSharng++;
			        	} else {
			        		sharngMap.put("sanCtnSn",Integer.toString(iApproval));
			        		sharngMap.put("pgParam", pgParam2);
			        		pm40Mapper.insertWbsApprovalList(sharngMap);       		
			                	iApproval++;
			        	}
			        }
				}
				
				
				
				//마스터입력
				

			}else if (gochalIn == 1) {
				result = 7;
				rtnMap.put("result", String.valueOf(result));
				
			}
		return rtnMap;
		}
		
		@Override
		public int update_pm40(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {	 
			Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
			Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
		
			
			int result = pm40Mapper.update_pm40(paramMap);
			paramMap.put("reqNo",paramMap.get("workNo"));
			Gson gson = new Gson();	
			
			List<Map<String, String>> sharngChk = pm40Mapper.deleteWbsSharngListChk(paramMap); 
			if (sharngChk.size() > 0) {
				pm40Mapper.deleteWbsSharngList(paramMap); 
			}
			
			//공유
			String pgParam1 = "{\"actionType\":\""+ "T" +"\",";
			//pgParam1 += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\","; 
			pgParam1 += "\"coCd\":\""+ paramMap.get("coCd") +"\","; 
			pgParam1 += "\"workYm\":\""+ paramMap.get("workYm") +"\",";
			//if (!"".equals(paramMap.get("workRptNo"))) {	//issNo가 존재하면 문제건, 없으면 정상건
			pgParam1 += "\"userId\":\""+ paramMap.get("userId") +"\",";
			//}
			pgParam1 += "\"gubun\":\""+ "팀" +"\",";
			pgParam1 += "\"workNo\":\""+ paramMap.get("workNo") +"\"}";
			
			
			//결재
			String pgParam2 = "{\"actionType\":\""+ "S" +"\",";
			//pgParam2 += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\",";
			pgParam2 += "\"gubun\":\""+ "팀" +"\",";
			pgParam2 += "\"coCd\":\""+ paramMap.get("coCd") +"\","; 
			pgParam2 += "\"workYm\":\""+ paramMap.get("workYm") +"\",";
			//if (!"".equals(paramMap.get("workRptNo"))) {	//issNo가 존재하면 문제건, 없으면 정상건
			pgParam2 += "\"userId\":\""+ paramMap.get("userId") +"\",";
			//}
			pgParam2 += "\"gubun\":\""+ "팀" +"\",";
			pgParam2 += "\"workNo\":\""+ paramMap.get("workNo") +"\"}";
			
			//공유-결재
			Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
			List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("approvalArr"), stringList2);
			if (sharngArr != null && sharngArr.size() > 0 ) {
				int iSharng = 1;
				int iApproval = 1;
		        for (Map<String, String> sharngMap : sharngArr) {

	        	    sharngMap.put("reqNo", paramMap.get("reqNo"));
	        	    sharngMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
	        	    sharngMap.put("pgmId", paramMap.get("pgmId"));
	        	    sharngMap.put("userId", paramMap.get("userId"));
	        	    
		        	if ("공유".equals(sharngMap.get("gb"))) {
		            	    sharngMap.put("sanCtnSn",Integer.toString(iSharng));
		            	    sharngMap.put("pgParam", pgParam1);
		            	    pm40Mapper.insertWbsSharngList(sharngMap);       		
		                	iSharng++;
		        	} else {
		        		sharngMap.put("sanCtnSn",Integer.toString(iApproval));
		        		sharngMap.put("pgParam", pgParam2);
		        		pm40Mapper.insertWbsApprovalList(sharngMap);       		
		                	iApproval++;
		        	}
		        }
			}
			
			
		return result;
		}
		
		@Override
		public int delete_pm40(Map<String, String> paramMap) throws Exception {	 
		int result = 0;
		result = pm40Mapper.delete_pm40(paramMap);
		
		List<Map<String, String>> sharngChk = pm40Mapper.deleteWbsSharngListChk(paramMap); 
		if (sharngChk.size() > 0) {
			pm40Mapper.deleteWbsSharngList(paramMap); 
		 }
		
		return result;
		}

		@Override
		public List<Map<String, String>> selectYearWorkMainList(Map<String, String> paramMap) {
			// TODO Auto-generated method stub
			return pm40Mapper.selectYearWorkMainList(paramMap);
		}

		@Override
		public String select_pm40_Next_MNGM_NO(Map<String, String> paramMap) {
			
			
			// TODO Auto-generated method stub
			return pm40Mapper.select_pm40_Next_MNGM_NO(paramMap);
		}

		@Override
		public int select_gochal_count(Map<String, String> paramMap) {
			// TODO Auto-generated method stub
			return pm40Mapper.select_gochal_count(paramMap);
		}

		@Override
		public Map<String, String> select_pm40_Info(Map<String, String> paramMap) {
			// TODO Auto-generated method stub
			return pm40Mapper.select_pm40_Info(paramMap);
		}
		
		@Override
		public List<Map<String, String>> selectSignResUserlst(Map<String, String> paramMap) {
			return pm40Mapper.selectSignResUserlst(paramMap);
		}

		@Override
		public List<Map<String, String>> selectWorkPrtList(Map<String, String> paramMap) {
			// TODO Auto-generated method stub
			
			String[] roleArray = paramMap.get("userName").split(",");
			String test = "";
			for (int i = 0; i < roleArray.length; i++) {
				if ((roleArray.length -1) == i ) {
					test += "'" + roleArray[i] +"' AS user"+ (i+1);
				}
				else {
					test += "'" + roleArray[i] +"' AS user"+ (i+1) +  ",";
				}
			}

			paramMap.put("test", test);
			
			List<Map<String,String>> result = pm40Mapper.selectWorkPrtList(paramMap);
			System.out.println(result.size()+"resultSize");
			
			double totalSumSum = result.stream()
				    .mapToDouble(row -> Double.parseDouble(row.get("totalSum")))
				    .sum();

				double accumulatedPercentage = 0.0;
				for (int i = 0; i < result.size(); i++) {
				    double totalSum = Double.parseDouble(result.get(i).get("totalSum"));
				    double percentage = (totalSum / totalSumSum) * 100;

				    // 마지막 행에서 합계를 조정하여 100.00이 되도록 처리
				    if (i == result.size() - 1) {
				        percentage = 100.0 - accumulatedPercentage;
				    } else {
				        accumulatedPercentage += Math.round(percentage * 100.0) / 100.0; // 누적합을 반올림하여 누적
				    }

				    result.get(i).put("tot_pct", String.format("%.2f", percentage));
			}
			 
			return result;
		}
		
		
		public Map<String, String> insert_pm40_p02(Map<String, String> paramMap){
			Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
			Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
			Map rtnMap = new HashMap();
			
			
			//데이터 처리 시작
			int result = 0;
			//해당월의 고찰데이터가 있는지 없는지 확인
			int resultIn = pm40Mapper.select_result_count(paramMap);
					
			if (resultIn == 0) {
				

				String newMNGM_NO = pm40Mapper.select_pm40_Next_MNGM_NO(paramMap);
				paramMap.put("workNo", newMNGM_NO);
				paramMap.put("reqNo", newMNGM_NO);
				
				result = pm40Mapper.insert_pm40_p02(paramMap);
				rtnMap.put("result", String.valueOf(result)); //문자열로 변환하여 rtnMap에 "result"키로 저장
				rtnMap.put("workNo", newMNGM_NO);// rtnMap에 "reqNo"키로 저장
				
				
				
				Gson gson = new Gson();	
				
				List<Map<String, String>> sharngChk = pm40Mapper.deleteWbsSharngListChk(paramMap); 
				if (sharngChk.size() > 0) {
					pm40Mapper.deleteWbsSharngList(paramMap); 
				}
				
				//공유
				String pgParam1 = "{\"actionType\":\""+ "T" +"\",";
				//pgParam1 += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\","; 
				pgParam1 += "\"coCd\":\""+ paramMap.get("coCd") +"\","; 
				pgParam1 += "\"workYm\":\""+ paramMap.get("workYm") +"\",";
				//if (!"".equals(paramMap.get("workRptNo"))) {	//issNo가 존재하면 문제건, 없으면 정상건
				pgParam1 += "\"userId\":\""+ paramMap.get("userId") +"\",";
				//}
				pgParam1 += "\"gubun\":\""+ "팀" +"\",";
				pgParam1 += "\"workNo\":\""+ paramMap.get("workNo") +"\"}";
				
				
				//결재
				String pgParam2 = "{\"actionType\":\""+ "S" +"\",";
				//pgParam2 += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\",";
				pgParam2 += "\"gubun\":\""+ "팀" +"\",";
				pgParam2 += "\"coCd\":\""+ paramMap.get("coCd") +"\","; 
				pgParam2 += "\"workYm\":\""+ paramMap.get("workYm") +"\",";
				//if (!"".equals(paramMap.get("workRptNo"))) {	//issNo가 존재하면 문제건, 없으면 정상건
				pgParam2 += "\"userId\":\""+ paramMap.get("userId") +"\",";
				//}
				pgParam2 += "\"gubun\":\""+ "팀" +"\",";
				pgParam2 += "\"workNo\":\""+ paramMap.get("workNo") +"\"}";
				
				//공유-결재
				Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
				List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("approvalArr"), stringList2);
				if (sharngArr != null && sharngArr.size() > 0 ) {
					int iSharng = 1;
					int iApproval = 1;
			        for (Map<String, String> sharngMap : sharngArr) {

		        	    sharngMap.put("reqNo", paramMap.get("reqNo"));
		        	    sharngMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
		        	    sharngMap.put("pgmId", paramMap.get("pgmId"));
		        	    sharngMap.put("userId", paramMap.get("userId"));
		        	    
			        	if ("공유".equals(sharngMap.get("gb"))) {
			            	    sharngMap.put("sanCtnSn",Integer.toString(iSharng));
			            	    sharngMap.put("pgParam", pgParam1);
			            	    pm40Mapper.insertWbsSharngList(sharngMap);       		
			                	iSharng++;
			        	} else {
			        		sharngMap.put("sanCtnSn",Integer.toString(iApproval));
			        		sharngMap.put("pgParam", pgParam2);
			        			pm40Mapper.insertWbsApprovalList(sharngMap);       		
			                	iApproval++;
			        	}
			        }
				}
				
				
				
			}
			else if (resultIn == 1) {
				result = 7;
				rtnMap.put("result", String.valueOf(result));
			}
			
			
			
			return rtnMap;
		}
		
		// 그리드 카운트
		@Override
		public int select_result_count(Map<String, String> paramMap) {
			return pm40Mapper.select_result_count(paramMap);
		}
}
