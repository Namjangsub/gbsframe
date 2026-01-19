package com.dksys.biz.user.pm.pm20;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.pm.pm20.service.PM20Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/pm/pm20")
public class PM20Ctr {

    @Autowired
	MessageUtils messageUtils;
	
	@Autowired
	PM20Svc pm20Svc;

    PM20Ctr(MessageUtils messageUtils) {
        this.messageUtils = messageUtils;
    }

	// 임팀장회의록 현황 조회
	@PostMapping(value = "/selectList_pm20")
	public String selectList_pm20(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		
		List<Map<String, String>> result = pm20Svc.selectList_pm20(paramMap);
		model.addAttribute("result", result);
		
		return "jsonView";
	}

	// 프로젝트별 매입원가현황 헤더 정보 조회
	@PostMapping(value = "/select_pm20m_Info")
	public String select_pm20m_Info(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = pm20Svc.select_pm20m_Info(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// 프로젝트별 그리드 자식 트리 전체 리스트 조회 *(안건리스트, 참석자리스트, 파일리스트)
    @PostMapping("/selectAgendaList")
    public String selectAgendaList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = pm20Svc.selectAgendaList(paramMap);
    	model.addAttribute("result", result);
		paramMap.put("fileTrgtKey", result.get(0).get("fileTrgtKey"));
		// 참석자 조회
		List<Map<String, String>> d02List = pm20Svc.select_pm20_d02_List(paramMap);
		model.addAttribute("d02List", d02List);
    	return "jsonView";
    }

	// 주제별 안건 제목 등록 및 수정
	@PostMapping(value = "/insert_update_agenda_title")
	public String insert_update_agenda_title(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (pm20Svc.insert_update_agenda_title(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	// 안건내용 등록 및 수정
	@PostMapping(value = "/insert_update_agenda")
	public String insert_update_agenda(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (pm20Svc.insert_update_agenda(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	// 참석자 update
	@PostMapping("/pm20_d02_update")
	public String pm20_d02_update(@RequestBody Map<String,Object> paramMap, ModelMap model) throws Exception {
		try {
			if (pm20Svc.pm20_d02_update(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		} catch(Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	// 참석자 삭제
	@PostMapping(value = "/pm20_d02_delete")
	public String pm20_d02_delete(@RequestBody Map<String, Object> paramMap, ModelMap model) throws Exception {
		try {
			if (pm20Svc.pm20_d02_delete(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	// 참석자 선택 삭제
	@PostMapping(value = "/pm20_d02_delete_selected")
	public String pm20_d02_delete_selected(@RequestBody Map<String, Object> paramMap, ModelMap model) throws Exception {
		try {
			if (pm20Svc.pm20_d02_delete_selected(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	// 안건 삭제
	@PostMapping(value = "/delete_agenda")
	public String delete_agenda(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (pm20Svc.delete_agenda(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	// 날짜 삭제(해당 날짜의 안건 내용/참석자)
	@PostMapping(value = "/delete_agenda_date")
	public String delete_agenda_date(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (pm20Svc.delete_agenda_date(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	// 안건 순서 변경
	@PostMapping(value = "/update_agenda_order")
	public String update_agenda_order(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (pm20Svc.update_agenda_order(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	// 안건 추가를 위한 순서 밀기
	@PostMapping(value = "/shift_agenda_order")
	public String shift_agenda_order(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (pm20Svc.shift_agenda_order(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	// 안건 드래그 이동
	@PostMapping(value = "/move_agenda_order")
	public String move_agenda_order(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (pm20Svc.move_agenda_order(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	// 파일업로드
	@PostMapping("/agUploadFile")
	public String agUploadFile(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			if (pm20Svc.agUploadFile(paramMap, mRequest) >= 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getMessage());
		}
		
		return "jsonView";
	}

	
}
