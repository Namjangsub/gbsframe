package com.dksys.biz.user.wb.wb02.mapper;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface WB02Mapper {
   int deleteWbsRsltslist(Map<String, String> paramMap);
       
   int deleteWbsSharngListSub(Map<String, String> paramMap);
	
   int deleteWbsApprovalListSub(Map<String, String> paramMap);

   int updateWbsRsltsCloseYn(Map<String, String> paramMap);

   List<Map<String, String>> selectRsltsMemberList(Map<String, String> paramMap);

   List<Map<String, String>> selectWbsRsltsDetailList(Map<String, String> paramMap);

   List<Map<String, String>> selectRsltsSharngList(Map<String, String> paramMap);

   List<Map<String, String>> selectRsltsApprovalList(Map<String, String> paramMap);

   Map<String, String> selectWbsRsltsInfo(Map<String, String> paramMap);

   int selectWbsRstlsSeqNext(Map<String, String> paramMap);

   int wbsRsltsMasterInsert(Map<String, String> paramMap);

   int wbsRsltsDetailInsert(Map<String, String> paramMap);

   int insertWbsSharngList(Map<String, String> paramMap);

   int insertWbsApprovalList(Map<String, String> paramMap);

   int wbsPlanStsCodeUpdate(Map<String, String> paramMap);

   int wbsRsltsMasterUpdate(Map<String, String> paramMap);

   List<Map<String, String>> selectApprovalList(Map<String, String> paramMap);

   int deleteWbsRsltsDetailSub(Map<String, String> paramMap);

   List<Map<String, String>> deleteWbsRsltsDetailChk(Map<String, String> paramMap);

   List<Map<String, String>> deleteWbsSharngListChk(Map<String, String> paramMap);

   List<Map<String, String>> deleteWbsApprovalListChk(Map<String, String> paramMap);

   int deleteWbsRsltsDetail(Map<String, String> paramMap);

   int deleteWbsSharngList(Map<String, String> paramMap);
 
   int deleteWbsApprovalList(Map<String, String> paramMap);

}
