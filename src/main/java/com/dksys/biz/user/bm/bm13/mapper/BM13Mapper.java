package com.dksys.biz.user.bm.bm13.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface BM13Mapper {
	
	//결재선리스트
	int selectApprovalListCount(Map<String, String> paramMap);

	List<Map<String, String>> selectApprovalList(Map<String, String> paramMap);

	List<Map<String, String>> selectApprovalExcelList(Map<String, String> paramMap);

	String selectMaxAppNo(Map<String, String> paramMap);	
	
	String selectMaxAppSeq(Map<String, String> paramMap);	
	
	//결재선 등록, 수정, 삭제
	int insertApproval(Map<String, String> paramMap);
	
	int updateApproval(Map<String, String> paramMap);
	
	int deleteApproval(Map<String, String> paramMap);
	
	int deleteMainGdApproval(Map<String, String> paramMap);
	
	List<Map<String, String>> selectRsltsMemberList(Map<String, String> paramMap);

}
