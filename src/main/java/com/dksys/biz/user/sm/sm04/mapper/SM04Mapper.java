package com.dksys.biz.user.sm.sm04.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SM04Mapper {
	
	
    int selectIoCount(Map<String, String> param);

    int selectIoDetailCount(Map<String, String> param);

    List<Map<String, Object>> selectIoList(Map<String, String> param);
    
    List<Map<String, Object>> selectIoDetail(Map<String, String> param);

    List<Map<String, Object>> selectStInfo(Map<String, String> param);
    
    // 창고 코드 검색
    List<Map<String, Object>> selectWhCd(Map<String, String> paramMap);

    // 수정화면 정보
    Map<String, String> select_sm04_info(Map<String, String> paramMap);

	//기본정보 & 불출정보 등록
    int insert_sm04(Map<String, String> paramMap);

	//출고창고 재고정보 등록
    int insert_sm04_Info(Map<String, String> paramMap);

	// fileTrgtKey 생성
    int select_sm04_SeqNext(Map<String, String> paramMap);
    
    int select_sm04_Seq(Map<String, String> paramMap);

    int insertEstDetail(Map<String, Object> detailMap);

    int updateEst(Map<String, String> paramMap);

    int updateEstConfirm(Map<String, String> paramMap);

    int deleteEstDetail(Map<String, String> paramMap);

    int deleteAllEstDetails(Map<String, String> paramMap);


    String selectMaxEstDeg(Map<String, String> paramMap);

    String selectMaxFileTrgtKey();
	
	
}
