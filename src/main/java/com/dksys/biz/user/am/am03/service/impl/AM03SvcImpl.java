package com.dksys.biz.user.am.am03.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.am.am03.mapper.AM03Mapper;
import com.dksys.biz.user.am.am03.service.AM03Svc;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class AM03SvcImpl implements AM03Svc {

    private final Logger logger = LoggerFactory.getLogger(AM03SvcImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AM03Mapper am03Mapper;

    @Override
    public List<Map<String, Object>> selectLinePresetList(Map<String, Object> paramMap) {
        return am03Mapper.selectLinePresetList(paramMap);
    }

    @Override
    public List<Map<String, Object>> selectLinePresetDetail(Map<String, Object> paramMap) {
        return am03Mapper.selectLinePresetDetail(paramMap);
    }

    @Override
    public Map<String, Object> saveLinePreset(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();

        am03Mapper.insertLinePreset(paramMap);
        String linePresetId = (String) paramMap.get("linePresetId");

        List<Map<String, Object>> lineList = parseLineList(paramMap.get("lineList"));
        if (lineList != null && !lineList.isEmpty()) {
            int seq = 1;
            for (Map<String, Object> item : lineList) {
                item.put("linePresetId", linePresetId);
                item.put("lineSeq", seq++);
                item.put("userId", paramMap.get("userId"));
                item.put("pgmId", paramMap.get("pgmId"));
                am03Mapper.insertLinePresetDetail(item);
            }
        }

        resultMap.put("resultCode", "200");
        resultMap.put("resultMessage", "개인 결재선이 저장되었습니다.");
        resultMap.put("linePresetId", linePresetId);
        return resultMap;
    }

    @Override
    public Map<String, Object> deleteLinePreset(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        am03Mapper.deleteLinePresetDetail(paramMap);
        int deleted = am03Mapper.deleteLinePreset(paramMap);
        if (deleted == 0) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "삭제할 결재선이 없거나 삭제 권한이 없습니다.");
            return resultMap;
        }
        resultMap.put("resultCode", "200");
        resultMap.put("resultMessage", "개인 결재선이 삭제되었습니다.");
        return resultMap;
    }

    @Override
    public List<Map<String, Object>> selectDelegateList(Map<String, Object> paramMap) {
        return am03Mapper.selectDelegateList(paramMap);
    }

    @Override
    public Map<String, Object> saveDelegate(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        am03Mapper.insertDelegate(paramMap);
        resultMap.put("resultCode", "200");
        resultMap.put("resultMessage", "대결(위임) 설정이 저장되었습니다.");
        return resultMap;
    }

    @Override
    public Map<String, Object> updateDelegateStatus(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        am03Mapper.updateDelegate(paramMap);
        resultMap.put("resultCode", "200");
        resultMap.put("resultMessage", "대결 상태가 변경되었습니다.");
        return resultMap;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseLineList(Object raw) {
        if (raw == null) return null;
        if (raw instanceof List) {
            return (List<Map<String, Object>>) raw;
        }
        if (raw instanceof String) {
            String str = ((String) raw).trim();
            if (str.isEmpty()) return null;
            try {
                return objectMapper.readValue(str, new TypeReference<List<Map<String, Object>>>() {});
            } catch (Exception e) {
                logger.error("결재선 JSON 파싱 오류: {}", str, e);
            }
        }
        return null;
    }
}
