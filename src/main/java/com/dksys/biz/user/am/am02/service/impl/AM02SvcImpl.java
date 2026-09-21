package com.dksys.biz.user.am.am02.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.Reader;
import java.sql.Clob;
import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.am.am02.mapper.AM02Mapper;
import com.dksys.biz.user.am.am02.service.AM02Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class AM02SvcImpl implements AM02Svc {

    @Autowired
    private AM02Mapper am02Mapper;

    private void preparePaging(Map<String, Object> paramMap) {
        int pageNo = 1;
        int recordCnt = 20;

        if (paramMap.get("pageNo") != null) {
            try {
                pageNo = Integer.parseInt(String.valueOf(paramMap.get("pageNo")));
            } catch (NumberFormatException ignored) {}
        }
        if (paramMap.get("recordCnt") != null) {
            try {
                recordCnt = Integer.parseInt(String.valueOf(paramMap.get("recordCnt")));
            } catch (NumberFormatException ignored) {}
        }

        paramMap.put("firstIndex", (pageNo - 1) * recordCnt + 1);
        paramMap.put("lastIndex", pageNo * recordCnt);
    }

    @Override
    public int selectFormCount(Map<String, Object> paramMap) {
        return am02Mapper.selectFormCount(paramMap);
    }

    @Override
    public List<Map<String, Object>> selectFormList(Map<String, Object> paramMap) {
        preparePaging(paramMap);
        return am02Mapper.selectFormList(paramMap);
    }

    @Override
    public Map<String, Object> selectFormDetail(Map<String, Object> paramMap) {
        Map<String, Object> result = am02Mapper.selectFormDetail(paramMap);
        if (result != null) {
            normalizeClob(result, "formTmplHtml");
            normalizeClob(result, "formSchemaJson");
            normalizeClob(result, "dfltLineJson");
        }
        return result;
    }

    private void normalizeClob(Map<String, Object> result, String field) {
        Object value = result.get(field);
        if (!(value instanceof Clob) && (value == null || !value.getClass().getName().toLowerCase().contains("clob"))) return;
        try {
            StringBuilder text = new StringBuilder();
            char[] buffer = new char[4096];
            Method method = value instanceof Clob
                    ? Clob.class.getMethod("getCharacterStream")
                    : value.getClass().getMethod("getCharacterStream");
            try (Reader reader = (Reader) method.invoke(value)) {
                int read;
                while ((read = reader.read(buffer)) != -1) text.append(buffer, 0, read);
            }
            result.put(field, text.toString());
        } catch (Exception e) {
            throw new IllegalStateException("결재양식 본문 변환 실패: " + field, e);
        }
    }

    @Override
    public Map<String, Object> saveForm(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        String formCd = (String) paramMap.get("formCd");
        boolean isNew = "Y".equals(paramMap.get("isNew"));

        if (isNew) {
            paramMap.put("currVer", 1);
            paramMap.put("formVer", 1);
            am02Mapper.insertForm(paramMap);
            am02Mapper.insertFormVersion(paramMap);
            resultMap.put("resultMessage", "양식이 성공적으로 등록되었습니다.");
        } else {
            // 버전 업 여부 확인
            boolean isNewVer = "Y".equals(paramMap.get("isNewVer"));
            if (isNewVer) {
                int currVer = Integer.parseInt(String.valueOf(paramMap.get("currVer"))) + 1;
                paramMap.put("currVer", currVer);
                paramMap.put("formVer", currVer);
                am02Mapper.updateForm(paramMap);
                am02Mapper.insertFormVersion(paramMap);
                resultMap.put("resultMessage", "양식 신규 버전(v" + currVer + ")이 개정 등록되었습니다.");
            } else {
                am02Mapper.updateForm(paramMap);
                resultMap.put("resultMessage", "양식 정보가 수정되었습니다.");
            }
        }

        resultMap.put("resultCode", "200");
        resultMap.put("formCd", formCd);
        return resultMap;
    }
}
