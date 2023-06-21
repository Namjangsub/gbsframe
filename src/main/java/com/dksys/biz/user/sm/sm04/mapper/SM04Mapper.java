package com.dksys.biz.user.sm.sm04.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface SM04Mapper {


    String selectMaxEstNo(Map<String, String> paramMap);

    String selectEstNo();

    int selectIoCount(Map<String, String> param);

    int selectIoDetailCount(Map<String, String> param);

    List<Map<String, Object>> selectIoList(Map<String, String> param);
    
    List<Map<String, Object>> selectIoDetail(Map<String, String> param);

    List<Map<String, Object>> selectStInfo(Map<String, String> param);

    List<Map<String, Object>> selectWhCd(Map<String, String> paramMap);

    List<Map<String, Object>> selectEstDetail(Map<String, String> paramMap);

    int insertIo(Map<String, String> paramMap);
    
    int select_sm04_SeqNext(Map<String, String> paramMap);

    int insertEstDetail(Map<String, Object> detailMap);

    int updateEst(Map<String, String> paramMap);

    int updateEstConfirm(Map<String, String> paramMap);

    int deleteEstDetail(Map<String, String> paramMap);

    int deleteAllEstDetails(Map<String, String> paramMap);


    String selectMaxEstDeg(Map<String, String> paramMap);

    String selectMaxFileTrgtKey();


}
