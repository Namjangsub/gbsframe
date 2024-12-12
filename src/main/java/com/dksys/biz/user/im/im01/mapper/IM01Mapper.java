package com.dksys.biz.user.im.im01.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IM01Mapper {

    int selectImprovementListCount(Map<String, String> paramMap);

    List<Map<String, String>> selectImprovementList(Map<String, String> paramMap);

    Map<String, String> selectImprovement(Map<String, String> paramMap);

    int insertImprovement(Map<String, String> paramMap);

    int updateImprovement(Map<String, String> paramMap);

    int deleteImprovement(Map<String, String> paramMap);

    String selectImprovementSeqNext(Map<String, String> paramMap);

    List<Map<String, String>> selectImprovementShareUserlst(Map<String, String> paramMap);

    int updateImprvmReqIdTxt(Map<String, String> paramMap);

    int updateExecTeamIdTxt(Map<String, String> paramMap);

    int updateImprvmStsCd(Map<String, String> paramMap);

    int updateExecStsCd(Map<String, String> paramMap);

    void updateDelApprovalList1(Map<String, String> paramMap);

    void deleteDelApprovalgList1(Map<String, String> paramMap);

    void deleteImprovementNoAllList(Map<String, String> paramMap);
	  
}