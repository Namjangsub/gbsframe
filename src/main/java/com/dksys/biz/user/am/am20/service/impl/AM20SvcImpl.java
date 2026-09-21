package com.dksys.biz.user.am.am20.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.am.am20.mapper.AM20Mapper;
import com.dksys.biz.user.am.am20.service.AM20Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class AM20SvcImpl implements AM20Svc {

    @Autowired
    private AM20Mapper am20Mapper;

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

        int firstIndex = (pageNo - 1) * recordCnt + 1;
        int lastIndex = pageNo * recordCnt;

        paramMap.put("firstIndex", firstIndex);
        paramMap.put("lastIndex", lastIndex);
    }

    @Override
    public int selectInboxCount(Map<String, Object> paramMap) {
        return am20Mapper.selectInboxCount(paramMap);
    }

    @Override
    public List<Map<String, Object>> selectInboxList(Map<String, Object> paramMap) {
        preparePaging(paramMap);
        return am20Mapper.selectInboxList(paramMap);
    }

    @Override
    public int selectProgressCount(Map<String, Object> paramMap) {
        return am20Mapper.selectProgressCount(paramMap);
    }

    @Override
    public List<Map<String, Object>> selectProgressList(Map<String, Object> paramMap) {
        preparePaging(paramMap);
        return am20Mapper.selectProgressList(paramMap);
    }

    @Override
    public int selectDraftedCount(Map<String, Object> paramMap) {
        return am20Mapper.selectDraftedCount(paramMap);
    }

    @Override
    public List<Map<String, Object>> selectDraftedList(Map<String, Object> paramMap) {
        preparePaging(paramMap);
        return am20Mapper.selectDraftedList(paramMap);
    }

    @Override
    public int selectCompletedCount(Map<String, Object> paramMap) {
        return am20Mapper.selectCompletedCount(paramMap);
    }

    @Override
    public List<Map<String, Object>> selectCompletedList(Map<String, Object> paramMap) {
        preparePaging(paramMap);
        return am20Mapper.selectCompletedList(paramMap);
    }

    @Override
    public int selectRejectedCount(Map<String, Object> paramMap) {
        return am20Mapper.selectRejectedCount(paramMap);
    }

    @Override
    public List<Map<String, Object>> selectRejectedList(Map<String, Object> paramMap) {
        preparePaging(paramMap);
        return am20Mapper.selectRejectedList(paramMap);
    }

    @Override
    public int selectReferencedCount(Map<String, Object> paramMap) {
        return am20Mapper.selectReferencedCount(paramMap);
    }

    @Override
    public List<Map<String, Object>> selectReferencedList(Map<String, Object> paramMap) {
        preparePaging(paramMap);
        return am20Mapper.selectReferencedList(paramMap);
    }

    @Override
    public int selectTempCount(Map<String, Object> paramMap) {
        return am20Mapper.selectTempCount(paramMap);
    }

    @Override
    public List<Map<String, Object>> selectTempList(Map<String, Object> paramMap) {
        preparePaging(paramMap);
        return am20Mapper.selectTempList(paramMap);
    }
}
