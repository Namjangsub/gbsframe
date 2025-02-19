package com.dksys.biz.user.cr.cr50.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CR50Mapper {

    List<Map<String, String>> selectPFUAreaItemList(Map<String, String> paramMap);

    List<Map<String, String>> selectPFUAreaRetriveList(Map<String, String> paramMap);

    List<Map<String, String>> selectPFUChangedList(Map<String, String> paramMap);

    Map<String, String> selectSalesCdInfo(Map<String, String> paramMap);

    Map<String, String> selectPfuInfo(Map<String, String> paramMap);

    List<Map<String, String>> selectPfuInfoSalesCdList(Map<String, String> paramMap);

//  Map<String, String> selectPfuClobInfo(Map<String, String> paramMap);
    List<Map<String, String>> selectStdPfuClobInfo(Map<String, String> paramMap);

    String selectSystemCreateDttm(Map<String, String> paramMap);

    int insertPfu(Map<String, String> paramMap);

    int updatePfu(Map<String, String> paramMap);

    int deletePfuNo(Map<String, String> paramMap);


    int insertPfuArea(Map<String, String> paramMap);

    int updatePfuArea(Map<String, String> paramMap);

    int deletePfuArea(Map<String, String> paramMap);

    int deletePfuAreaAll(Map<String, String> paramMap);

    String selectPfuDeleteCheck(Map<String, String> paramMap);


    int selectPfuListCount(Map<String, String> paramMap);
 
    List<Map<String, String>> selectPfuList(Map<String, String> paramMap);

    // PFU복사부분 시작
    int copy_cr50_master(Map<String, String> dtl);

    int copy_cr50_detail(Map<String, String> dtl);

    int copy_cr50_salescd(Map<String, String> dtl);

    int delete_cr50(Map<String, String> dtl);

    int selectPfuCopyTargetListCount(Map<String, String> paramMap);

    List<Map<String, String>> selectPfuCopyTargetList(Map<String, String> paramMap);
    // PFU복사 끝

    int selectPfuIsThereListCount(Map<String, String> paramMap);

    List<Map<String, String>> selectTagetSalesCodeList(Map<String, String> paramMap);

    int insertPfuSalesCd(Map<String, String> detailMap);

    int updatePfuSalesCd(Map<String, String> paramMap);

    int deletePfuSalesCd(Map<String, String> paramMap);

    int deletePfuSalesCdAll(Map<String, String> paramMap);

    List<Map<String, String>> selectPfuReferenceTargetList(Map<String, String> paramMap);

    int selectPfuReferenceTargetListCount(Map<String, String> paramMap);

    List<Map<String, String>> selectIssueReferenceList(Map<String, String> paramMap);

    List<Map<String, String>> selectImprovementReferenceList(Map<String, String> paramMap);

    int insertPfuMasterHistory(Map<String, String> paramMap);

    int insertPfuDetailHistory(Map<String, String> paramMap);

    int insertPfuSalesCdHistory(Map<String, String> paramMap);

    int updatePfuMasterVersionUp(Map<String, String> paramMap);

}










