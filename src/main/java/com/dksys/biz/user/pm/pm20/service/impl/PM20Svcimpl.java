package com.dksys.biz.user.pm.pm20.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.user.pm.pm20.mapper.PM20Mapper;
import com.dksys.biz.user.pm.pm20.service.PM20Svc;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class PM20Svcimpl implements PM20Svc {

	@Autowired
	PM20Mapper pm20Mapper;

	@Autowired
	CM08Svc cm08Svc;

	@Autowired
	CM15Svc cm15Svc;

	@Override
	public List<Map<String, String>> selectAgendaList(Map<String, String> paramMap) {
		Gson gson = new Gson();

		// 회의록 리스트 조회
		List<Map<String, String>> agendaList = pm20Mapper.selectAgendaList(paramMap);

		// 결과를 담을 새로운 리스트에 회의록 복사
		List<Map<String, String>> resultList = new ArrayList<>(agendaList);

		// ----------------------------
		// (fileTrgtKey, agendaNo, agendaDate) 유니크 조합 수집
		// ----------------------------
		Set<String> keyPairs = new LinkedHashSet<>();
		if (agendaList != null) {
			for (Map<String, String> row : agendaList) {
				String fileTrgtKey = row.get("fileTrgtKey");
				String agendaNo = row.get("agendaNo");
				String agendaDate = row.get("agendaDate");
				if (fileTrgtKey != null && !fileTrgtKey.isEmpty()
						&& agendaNo != null && !agendaNo.isEmpty()
						&& agendaDate != null && !agendaDate.isEmpty()) {
					keyPairs.add(fileTrgtKey + "|" + agendaNo + "|" + agendaDate);
				}
			}
		}

		// (fileTrgtKey, agendaNo, agendaDate)별 파일 조회
		List<Map<String, String>> allFiles = new ArrayList<>();
		for (String pair : keyPairs) {
			String[] parts = pair.split("\\|", -1);
			String fileTrgtKeyBase = parts[0];
			String agendaNo = parts[1];
			String agendaDate = parts[2];

			// 조합 규칙: (fileTrgtKey_agendaNo_agendaDate)
			String fileTrgtKey = fileTrgtKeyBase + "_" + agendaNo + "_" + agendaDate;

			Map<String, String> fileMap = new HashMap<>(paramMap);
			fileMap.put("comonCd", "FITR05");
			fileMap.put("coCd", paramMap.get("coCd"));
			fileMap.put("fileTrgtTyp", "PM2001M01");
			fileMap.put("fileTrgtKey", fileTrgtKey);
			fileMap.put("jobType", "fileList");
			fileMap.put("userId", paramMap.get("userId"));

			List<Map<String, String>> files = cm08Svc.selectFileList(fileMap);
			if (files != null && !files.isEmpty()) {
				for (Map<String, String> f : files) {
					// 프론트 매핑용 메타
					f.put("fileTrgtKey", fileTrgtKeyBase);
					f.put("agendaNo", agendaNo);
					f.put("agendaDate", agendaDate);
				}
				allFiles.addAll(files);
			}
		}

		// 파일 전용 row 추가
		if (!allFiles.isEmpty()) {
			Map<String, String> fileRow = new HashMap<>();
			fileRow.put("rowType", "FILE");
			fileRow.put("files", gson.toJson(allFiles));
			resultList.add(fileRow);
		}

		return resultList;
	}

	@Override
	public List<Map<String, String>> select_pm20_d02_List(Map<String, String> paramMap) {
		return pm20Mapper.select_pm20_d02_List(paramMap);
	}

	@Override
	public int insert_update_agenda_title(Map<String, String> paramMap) throws Exception {
		String fileTrgtKey = "";
		if (!paramMap.containsKey("fileTrgtKey") || paramMap.get("fileTrgtKey").isEmpty()) {
			String yy = String.format("%02d", LocalDate.now().getYear() % 100);
			paramMap.put("yy", yy);
			String maxSeq = pm20Mapper.selectMaxFileTrgtKeySeq(paramMap);
			fileTrgtKey = yy + maxSeq;

			paramMap.put("fileTrgtKey", fileTrgtKey);
		}
		pm20Mapper.pm20_main_update(paramMap); // 회의주제 마스터 테이블 insert/update

		return pm20Mapper.insert_update_agenda_title(paramMap);
	}

	@Override
	public int insert_update_agenda(Map<String, String> paramMap) throws Exception {
		int result = 0;

		// fileTrgtKey 생성 (신규인 경우)
		if (!paramMap.containsKey("fileTrgtKey") || paramMap.get("fileTrgtKey") == null
				|| paramMap.get("fileTrgtKey").isEmpty()) {
			String yy = String.format("%02d", LocalDate.now().getYear() % 100);
			paramMap.put("yy", yy);
			String maxSeq = pm20Mapper.selectMaxFileTrgtKeySeq(paramMap);
			paramMap.put("fileTrgtKey", yy + maxSeq);
		}

		result += pm20Mapper.pm20_main_update(paramMap); // 회의주제 마스터 테이블 insert/update
		result += pm20Mapper.insert_update_agenda_title(paramMap); // 안건 제목도 함께 저장 (TB_PM20D01)
		if (paramMap.containsKey("agendaNo")) {
			result += pm20Mapper.pm20_d3_insert_update(paramMap); // 회의주제 상세 테이블 insert/update
		}
		return result;
	}

	@Override
	public List<Map<String, String>> select_agenda_no_by_date(Map<String, String> paramMap) {
		return pm20Mapper.select_agenda_no_by_date(paramMap);
	}

	@Override
	public int pm20_update_status(Map<String, String> paramMap) throws Exception {
		return pm20Mapper.pm20_update_status(paramMap);
	}

	@Override
	public int pm20_update_agenda_date(Map<String, String> paramMap) throws Exception {
		int result = 0;

		// 1) 파일 키 변경 대상 agendaNo 목록 확보
		List<Map<String, String>> agendaList = pm20Mapper.select_agenda_no_by_date(paramMap);
		String fileTrgtKey = paramMap.get("fileTrgtKey");
		String oldDate = paramMap.get("oldDate");
		String newDate = paramMap.get("newDate");

		// 2) 안건 내용/참석자 날짜 변경
		result += pm20Mapper.pm20_d03_update_date(paramMap);
		result += pm20Mapper.pm20_d02_update_date(paramMap);

		// 3) 첨부파일 FILE_TRGT_KEY 변경
		if (agendaList != null && fileTrgtKey != null && oldDate != null && newDate != null) {
			for (Map<String, String> row : agendaList) {
				String agendaNo = row.get("agendaNo");
				if (agendaNo == null || agendaNo.isEmpty())
					continue;
				Map<String, String> p = new HashMap<>();
				p.put("oldFileTrgtKey", fileTrgtKey + "_" + agendaNo + "_" + oldDate);
				p.put("newFileTrgtKey", fileTrgtKey + "_" + agendaNo + "_" + newDate);
				pm20Mapper.pm20_update_file_trgt_key(p);
			}
		}
		return result;
	}

	@Override
	public int delete_agenda(Map<String, String> paramMap) throws Exception {
		int result = 0;
		result += pm20Mapper.pm20_d03_delete_by_agenda(paramMap);
		result += pm20Mapper.pm20_d01_delete(paramMap);

		// 메인삭제 (안건이 하나도 없으면 메인삭제 쿼리)
		result += pm20Mapper.pm20_m01_delete_main(paramMap);

		return result;
	}

	@Override
	public int delete_agenda_date(Map<String, String> paramMap) throws Exception {
		int result = 0;
		result += pm20Mapper.pm20_d03_delete_by_date(paramMap);
		result += pm20Mapper.pm20_d02_delete_by_date(paramMap);
		return result;
	}

	@Override
	public int pm20_d02_update(Map<String, Object> paramMap) throws Exception {
		int result = 0;
		List<Map<String, Object>> attedList = (List<Map<String, Object>>) paramMap.get("attendList");

		for (Map<String, Object> dtl : attedList) {
			dtl.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
			dtl.put("agendaNo", paramMap.get("agendaNo"));
			dtl.put("agendaVer", paramMap.get("agendaVer"));
			dtl.put("agendaDate", paramMap.get("agendaDate"));
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			result += pm20Mapper.pm20_d02_update(dtl);
		}
		return result;
	}

	@Override
	public int pm20_d02_delete(Map<String, Object> paramMap) throws Exception {
		int result = 0;
		List<String> ids = (List<String>) paramMap.get("attendIds");
		for (String id : ids) {
			Map<String, Object> p = new HashMap<>();
			p.put("mnDate", paramMap.get("mnDate"));
			p.put("userId", id);
			result += pm20Mapper.pm20_d02_delete(p);
		}
		return result;
	}

	@Override
	public int pm20_d02_delete_selected(Map<String, Object> paramMap) throws Exception {
		int result = 0;
		List<String> ids = (List<String>) paramMap.get("attendIds");
		if (ids == null)
			return 0;
		for (String id : ids) {
			Map<String, Object> p = new HashMap<>();
			p.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
			p.put("agendaNo", paramMap.get("agendaNo"));
			p.put("agendaVer", paramMap.get("agendaVer"));
			p.put("agendaDate", paramMap.get("agendaDate"));
			p.put("userId", id);
			result += pm20Mapper.pm20_d02_delete_selected(p);
		}
		return result;
	}

	@Override
	public int update_agenda_order(Map<String, String> paramMap) throws Exception {
		int result = 0;
		result += pm20Mapper.pm20_swap_agenda_no_d03(paramMap);
		result += pm20Mapper.pm20_swap_agenda_no_d02(paramMap);
		result += pm20Mapper.pm20_swap_agenda_no_d01(paramMap);
		// 파일 FILE_TRGT_KEY swap (3단계: from->TEMP, to->from, TEMP->to)
		pm20Mapper.pm20_swap_file_trgt_key(paramMap);
		pm20Mapper.pm20_swap_file_trgt_key_step2(paramMap);
		pm20Mapper.pm20_swap_file_trgt_key_step3(paramMap);
		return result;
	}

	@Override
	public int shift_agenda_order(Map<String, String> paramMap) throws Exception {
		int result = 0;
		// 파일 먼저 shift (큰 번호부터 처리해야 중복 방지)
		pm20Mapper.pm20_shift_file_trgt_key(paramMap);
		result += pm20Mapper.pm20_shift_agenda_no_d03(paramMap);
		result += pm20Mapper.pm20_shift_agenda_no_d02(paramMap);
		result += pm20Mapper.pm20_shift_agenda_no_d01(paramMap);
		return result;
	}

	@Override
	public int move_agenda_order(Map<String, String> paramMap) throws Exception {
		int result = 0;
		result += pm20Mapper.pm20_move_agenda_no_d03(paramMap);
		result += pm20Mapper.pm20_move_agenda_no_d02(paramMap);
		result += pm20Mapper.pm20_move_agenda_no_d01(paramMap);
		return result;
	}

	@Override
	public int agUploadFile(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;

		// 삭제할 키부터 삭제
		String deleteJson = paramMap.get("deleteFileArr");
		if (deleteJson != null && !deleteJson.isEmpty()) {
			List<Map<String, String>> deleteList = new Gson().fromJson(
					deleteJson,
					new TypeToken<List<Map<String, String>>>() {
					}.getType());
			for (Map<String, String> entry : deleteList) {
				String fileKey = entry.get("fileKey");
				cm08Svc.deleteFile(fileKey);
			}
		}

		// 업로드할 파일이 있으면 업로드
		List<MultipartFile> fileList = mRequest.getFiles("files");
		if (!fileList.isEmpty()) {
			// "FITR05"은 공통코드에서 사진,미디어 첨부 디렉토리임
			paramMap.put("comonCd", "FITR05");
			paramMap.put("coCd", paramMap.get("coCd"));
			paramMap.put("jobType", "fileUp");
			paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
			paramMap.put("fileTrgtKey", mRequest.getParameter("fileTrgtKey"));
			cm15Svc.selectFileAuthCheck(paramMap);

			// 업로드
			result = cm08Svc.uploadFile(paramMap, mRequest);
		}

		return result;
	}
}
