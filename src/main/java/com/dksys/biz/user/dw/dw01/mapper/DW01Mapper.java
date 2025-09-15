package com.dksys.biz.user.dw.dw01.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DW01Mapper {
    Map<String, Object> findDoc(Map<String, Object> param);         // salesCd, docNo
    Map<String, Object> findVersionByPath(Map<String, Object> param);         // salesCd, docNo
    int insertDoc(Map<String, Object> param);                         // selectKey -> verId
    int updateDocHead(Map<String, Object> param);
    Map<String, Object> findLastVersion(@Param("docId") Long docId);  // selectKey -> docId

    int insertVersion(Map<String, Object> param);                   // fileName, ext, verNo, userId
    int updateVersionPath(Map<String, Object> params);

    int insertAudit(Map<String, Object> param);                      // 감사기록
    int bumpActiveVer(Map<String, Object> params);
    Integer getActiveVerForUpdate(@Param("docId") Long docId);

    int insertHistory(Map<String, Object> param);                    // 감사기록
    int markDocDeleted(Map<String, Object> param);
    int markDocActevated(Map<String, Object> param);
    
}
