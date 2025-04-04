package com.dksys.biz.user.wb.wb21.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WB21Mapper {
	
	List<Map<String, String>> selectMaxSjNo(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSalesCodeCheck(Map<String, String> paramMap);
	
	List<Map<String, String>> selectCodeList(Map<String, String> paramMap);
	
	int selectSjListCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSjList(Map<String, String> paramMap);

	// 과제일괄확정부분
	int ModalsjnoconfirmListCount(Map<String, String> paramMap);
	
	List<Map<String, String>> ModalsjnoconfirmList(Map<String, String> paramMap);

	int confirm_wb21(Map<String, String> paramMap);
	// 과제일괄확정부분 끝

	// 과제복사부분
	int ModalsjnocopyListCount(Map<String, String> paramMap);
	
	List<Map<String, String>> ModalsjnocopyList(Map<String, String> paramMap);

	int copy_wb21(Map<String, String> paramMap);
	// 과제복사부분 끝
	
	List<Map<String, String>> deleteSjListChk(Map<String, String> paramMap);
	
	int deleteSjList(Map<String, String> paramMap);
	
	int selectSjSeqNext(Map<String, String> paramMap);
	
	int insertSjDtlList(Map<String, String> paramMap);
	
    int sjInsert(Map<String, String> paramMap);
    
    int sjUpdate(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSjDtlList(Map<String, String> paramMap);
	
	Map<String, String> selectSjInfo(Map<String, String> paramMap);
	
	int sjConfirmY(Map<String, String> paramMap);
	
	int sjCloseY(Map<String, String> paramMap);
	
    int sjConfirmN(Map<String, String> paramMap);
	
	int sjCloseN(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSjVerNoNext(Map<String, String> paramMap);
	
	//DATA DELETE
	int deleteSjNo(Map<String, String> paramMap);

	//Project 과제 체크
	Map<String, String> selectWbChk(Map<String, String> paramMap);
	
	//외주 제작업체 수정
    int planMkerCdChange(Map<String, String> paramMap);

	int delete_wb21(Map<String, String> paramMap);

    int updateSalesCdManual(Map<String, String> paramMap);

    int deleteSalesCdManual(Map<String, String> paramMap);

	// 출고일 변경 Chk
	List<Map<String, String>> deDtChangChk(Map<String, String> paramMap);
}