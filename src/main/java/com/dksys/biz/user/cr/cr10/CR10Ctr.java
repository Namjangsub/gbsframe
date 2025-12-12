package com.dksys.biz.user.cr.cr10;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.cr.cr10.mapper.CR10Mapper;
import com.dksys.biz.user.cr.cr10.service.CR10Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/cr/cr10")
public class CR10Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
  CR10Svc cr10Svc;

  @Autowired
  CR10Mapper cr10Mapper;

    //Paging 조회
	@PostMapping(value = "/selectLgistReqPageList")
	public String selectLgistReqPageList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr10Svc.selectLgistReqPageCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = cr10Svc.selectLgistReqPageList(paramMap);
		model.addAttribute("resultList", result);
		return "jsonView";
	}

	@PostMapping(value = "/selectLgistReqList")
	public String selectLgistReqList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = cr10Svc.selectLgistReqList(paramMap);
		model.addAttribute("resultList", result);
		return "jsonView";
	}

	@PostMapping(value = "/selectSelesCdList")
	public String selectSelesCdList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = cr10Svc.selectSelesCdList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	@PostMapping(value = "/selectSelesCdViewList")
	public String selectSelesCdViewList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = cr10Svc.selectSelesCdViewList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	@PostMapping(value = "/selectLgistMastInfo")
		public String selectLgistMastInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = cr10Svc.selectLgistMastInfo(paramMap);
		model.addAttribute("result", result);
		List<Map<String, String>> resultList = cr10Svc.selectLgistAppList(paramMap);
		model.addAttribute("resultAppList", resultList);
		
		paramMap.put("lgistNo", result.get("lgistNo"));
		List<Map<String, String>> lgistItemList = cr10Svc.selectLgistItemList(paramMap);
		model.addAttribute("lgistItemList", lgistItemList);
		return "jsonView";
	}

	
	/*
	 * 클래스가 @Controller 이경우에는 
	 * 메서드가 ResponseEntity<byte[]>를 반환하면(바이너리 응답) 
	 * 반드시 @ResponseBody(또는 @RestController)가 필요
	 * @ResponseBody가 없으면 Spring이 이걸 “뷰 이름/뷰 렌더링”으로 처리
	 * 결과적으로 404/뷰 처리 실패처럼 보일 수 있슴
	 * jsonView 패턴과 혼재된 프로젝트에서 자주 발생
	 */
	@ResponseBody
	@GetMapping("/itemListImage")
	public ResponseEntity<byte[]> getItemImage(
	        @RequestParam("lgistNo") String lgistNo,
	        @RequestParam("lgistNoSeq") int lgistNoSeq
	) throws Exception {

	    Map<String, Object> p = new HashMap<>();
	    p.put("lgistNo", lgistNo);
	    p.put("lgistNoSeq", lgistNoSeq);

	    Map<String, Object> r = cr10Mapper.selectLgistItemListImage(p);
	    if (r == null) return ResponseEntity.notFound().build();

	    Object imgObj  = r.get("lgistItemImg");
	    if (imgObj == null) imgObj = r.get("lgistitemimg"); // 키 소문자 대비
	    Object mimeObj = r.get("lgistItemImgMime");
	    if (mimeObj == null) mimeObj = r.get("lgistitemimgmime");

	    if (imgObj == null) return ResponseEntity.notFound().build();

	    byte[] imgBytes;
	    if (imgObj instanceof byte[]) {
	        imgBytes = (byte[]) imgObj;
	    } else if (imgObj instanceof java.sql.Blob) {
	        imgBytes = blobToBytes((java.sql.Blob) imgObj);
	    } else {
	        throw new IllegalStateException("IMG type not supported: " + imgObj.getClass());
	    }

	    if (imgBytes == null || imgBytes.length == 0) return ResponseEntity.notFound().build();

	    String mime = (mimeObj == null) ? "application/octet-stream" : String.valueOf(mimeObj).trim();
	    if (mime.isEmpty()) mime = "application/octet-stream";

	    return ResponseEntity.ok()
	            .contentType(org.springframework.http.MediaType.parseMediaType(mime))
	            .header("Cache-Control", "no-cache")
	            .body(imgBytes);
	}

	private static byte[] blobToBytes(java.sql.Blob blob) throws Exception {
	    try (java.io.InputStream in = blob.getBinaryStream();
	         java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
	        byte[] buf = new byte[8192];
	        int n;
	        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
	        return out.toByteArray();
	    }
	}
	
	
    // 사진 정보 조회
    // [post body json : Map 방식]
    // [경로 지정 : http://localhost:7000/saveImage]
    // [body json 데이터 : {"idx":"1", "image":"data:image/png;base64,iVBORw ...."}]
    // [input : 서비스에서 설정한 파라미터 개수와 같아야합니다]
    // [output : 모델에서 설정한 return 타입으로 결과를 반환합니다]
    @PostMapping("/checkLgistItemImage")
    public String checkLgistItemImage(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	Map<String, Object> result = cr10Svc.checkLgistItemImage(paramMap);
    	
        model.addAttribute("result",result);
        return "jsonView";
    }

	@PostMapping(value = "/selectLgistAppCount")
	public String selectLgistAppCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
		String result = cr10Svc.selectLgistAppCount(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/selectLgistAppList")
	public String selectLgistAppList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = cr10Svc.selectLgistAppList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}


	@PostMapping(value = "/insertLgistMast")
	public String insertLgistMast(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			Map<String, String> rtnResult = cr10Svc.insertLgistMast(paramMap, mRequest);
			
			String lgistNo =  rtnResult.get("lgistNo").toString();
			int result =  Integer.parseInt(rtnResult.get("result"));
			
			if (result !=0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
				model.addAttribute("lgistNo", lgistNo);
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

	@PostMapping(value = "/updateLgistMast")
	public String updateLgistMast(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  	try {
			if (cr10Svc.updateLgistMast(paramMap, mRequest) != 0 ) {
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


	@PostMapping(value = "/updateLgistlistImage")
	public String updateLgistlistImage(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  	try {
	  		int result = cr10Svc.updateLgistlistImage(paramMap, mRequest);
			if (result == 9999 ) {
				model.addAttribute("resultCode", 9999);
				model.addAttribute("resultMessage", "출하 설비 LIST에 처리할 자료가 없습니다.");
			} else if (result != 0 ) {
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
	
	
	@PutMapping(value = "/deleteLgistMast")
	public String deleteLgistMast(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
	  	try {
			if (cr10Svc.deleteLgistMast(paramMap) != 0 ) {
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

	@PostMapping(value = "/updateTodoAppConfirm")
	public String updateTodoAppConfirm(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  	try {
			if (cr10Svc.updateTodoAppConfirm(paramMap) != 0 ) {
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

}