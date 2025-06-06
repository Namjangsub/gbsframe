package com.dksys.biz.user.sm.sm30.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SM30Mapper {

	Map<String, String> selectPjtInfo(Map<String, String> paramMap);

	List<Map<String, String>> selectPjtInfoDetail(Map<String, String> paramMap);

	String select_sm21_SeqNext(Map<String, String> paramMap);

	List<Map<String,String>> select_sm21_chk(Map<String,String> paramMap);

	String select_sm30_SeqNext(Map<String, String> paramMap);

	int insert_sm30(Map<String, String> paramMap);

	int insert_sm30_list(Map<String, String> paramMap);

	Map<String, String> select_sm30_info(Map<String, String> paramMap);

	List<Map<String, String>> sm30_pop_grid1_selectList(Map<String, String> paramMap);

	int delete_sm30_master(Map<String, String> paramMap);

	int delete_sm30_all(Map<String, String> paramMap);

	int delete_sm30_detail(Map<String, String> paramMap);

	Map<String, String> selectApprovalUserChk(Map<String, String> paramMap);

	List<Map<String, String>> selectShareUserChk(Map<String, String> paramMap);

	int updateApprovalHold(Map<String, String> paramMap);

	int updateApprovalSm30(Map<String, String> paramMap);

	int insertSm30ApprovalList(Map<String, String> paramMap);

	int deleteShareUser(Map<String, String> paramMap);

	
}
