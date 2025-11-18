package com.dksys.biz.admin.cm.cm08;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/admin/cm/cm08")
public class CM08Ctr {
	
	@Autowired
	MessageUtils messageUtils;
	
	@Autowired
	CM08Svc cm08Svc;
 	
	@Autowired
	CM15Svc cm15Svc;
	
	@PostMapping(value="/fileDownInfo")
	public String fileDownInfo(@RequestBody Map<String, String> param, ModelMap model) throws Exception {
		Map<String, String> fileInfo = cm08Svc.selectFileInfo(param.get("fileKey"));
		model.addAttribute("fileInfo", fileInfo);
		return "jsonView";
	}
	
	@PostMapping(value="/fileDownInfoUser")
	public String fileDownInfoUser(@RequestBody Map<String, String> param, ModelMap model) throws Exception {
    	//-----------------------------------------------
    	//parameter 정보
    	//  fileKey : 이동하고자 하는 파일 일련번호
    	//  userId  : 이동 작업을 실행하는 사용자 ID
    	//-----------------------------------------------		
	   	//해당 파일이 보관된 디렉토리 다운로드 권한이 있어야 작업 가능    		
    	Map<String, String> tempParam = new HashMap<String, String>();
    	
    	//해당 디렉토리 다운로드 권한이 있어야 작업 가능 (사용자별 접근권한 체크로 임의의 값이 오면 해당 폴더 권한이 있어야 조회됨    	
    	tempParam.putAll(param);
    	tempParam.put("jobType", "fileDown");
		Map<String, String> fileInfo = cm08Svc.selectFileInfoUser(tempParam);

    	//
    	if (null != fileInfo.get("fileName")) {
    		model.addAttribute("resultCode", 200);
    		model.addAttribute("resultMessage", messageUtils.getMessage("excute"));
    		model.addAttribute("fileInfo", fileInfo);
    		model.addAttribute("fileInfo", fileInfo);
    	}else {
			model.addAttribute("resultCode", 500);
    		model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
    	}
    	
		return "jsonView";
	}
	
	@GetMapping(value="/fileDownload")
	public void fileDownload(@RequestParam String filePath, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		BufferedInputStream bis = null;
		ServletOutputStream sos = null;

		try{
			File file = new File(filePath);
			response.setContentType("application/octet-stream; charset=UTF-8");
			response.setContentLength((int)file.length());
			int idx = filePath.split("\\\\").length-1;
			String fileName = filePath.split("\\\\")[idx];
			fileName = fileName.substring(fileName.indexOf("_")+1);
			cm08Svc.setDisposition(request, response, fileName);

			OutputStream out = response.getOutputStream();
	        FileInputStream fis = null;
	         
	        try {
	            fis = new FileInputStream(file);
	            FileCopyUtils.copy(fis, out);
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            if (fis != null) { try { fis.close(); } catch (Exception e2) {}}
	            if (out != null) { try { out.close(); } catch (Exception e2) {}}
	        }
	        out.flush();
	        
		} catch(IOException e){
			e.printStackTrace();
		} finally{
			if(bis != null)
				bis.close();
			if(sos != null)
				sos.close();
		}
	}
    
	
	@GetMapping(value="/fileDownloadAuth")
	public void fileDownloadAuth(@RequestParam Map<String, String> param, HttpServletRequest request, HttpServletResponse response) throws Exception {

	    String source = request.getHeader("X-GBS-Source");
	    if (!"pdfjs-viewer".equals(source)) {
	        System.out.println("주소창/링크 등 기타 요청일 가능성");
	        String json = "{\"error\":\"정상적인 접근이 아닙니다.\"}";

	        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
	        response.setContentType("application/json;charset=UTF-8");
	        response.getWriter().write(json);
	        response.getWriter().flush();
	        return;  // 이후 파일 다운로드 로직 실행하지 않도록 종료
	    }
        
		//-----------------------------------------------
    	//parameter 정보
    	//  fileKey : 이동하고자 하는 파일 일련번호
    	//  userId  : 이동 작업을 실행하는 사용자 ID
    	//-----------------------------------------------		
	   	//해당 디렉토리 다운로드 권한이 있어야 작업 가능     	
    	Map<String, String> tempParam = new HashMap<String, String>();
    	tempParam.putAll(param);
    	tempParam.put("jobType", "fileDown");
//    	String userId = request.getParameter("userId");
		Map<String, String> fileInfo = cm08Svc.selectFileInfoUser(tempParam);
    	//
    	if (null != fileInfo.get("fileName"))  {
//    		String filePath = request.getParameter("filePath");
    		String filePath = fileInfo.get("filePath") + fileInfo.get("fileKey")+ '_' + fileInfo.get("fileName");
    		
    		BufferedInputStream bis = null;
    		ServletOutputStream sos = null;
    		
    		try{
    			File file = new File(filePath);
    			response.setContentType("application/octet-stream; charset=UTF-8");
    			response.setContentLength((int)file.length());
    			int idx = filePath.split("\\\\").length-1;
    			String fileName = filePath.split("\\\\")[idx];
    			fileName = fileName.substring(fileName.indexOf("_")+1);
    			cm08Svc.setDisposition(request, response, fileName);
    			
    			OutputStream out = response.getOutputStream();
    			FileInputStream fis = null;
    			
    			try {
    				fis = new FileInputStream(file);
    				FileCopyUtils.copy(fis, out);
    			} catch (Exception e) {
    				e.printStackTrace();
    			} finally {
    				if (fis != null) { try { fis.close(); } catch (Exception e2) {}}
    				if (out != null) { try { out.close(); } catch (Exception e2) {}}
    			}
    			out.flush();
    			
    		} catch(IOException e){
    			e.printStackTrace();
    		} finally{
    			if(bis != null)
    				bis.close();
    			if(sos != null)
    				sos.close();
    		}
    	} 		
	}

    @GetMapping(value="/fileDownloadAuth2")
    public void fileDownloadAuth2(@RequestParam Map<String, String> param, HttpServletRequest request, HttpServletResponse response) throws Exception {

	    String source = request.getHeader("X-GBS-Source");
	    if (!"pdfjs-viewer".equals(source)) {
	        System.out.println("주소창/링크 등 기타 요청일 가능성");
	        String json = "{\"error\":\"정상적인 접근이 아닙니다.\"}";

	        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
	        response.setContentType("application/json;charset=UTF-8");
	        response.getWriter().write(json);
	        response.getWriter().flush();
	        return;  // 이후 파일 다운로드 로직 실행하지 않도록 종료
	    }
        
        //-----------------------------------------------
        //parameter 정보
        //  fileKey : 이동하고자 하는 파일 일련번호
        //  userId  : 이동 작업을 실행하는 사용자 ID
        //-----------------------------------------------       
        //해당 디렉토리 다운로드 권한이 있어야 작업 가능        
        Map<String, String> tempParam = new HashMap<String, String>();
        tempParam.putAll(param);
        tempParam.put("jobType", "fileDown");
//      String userId = request.getParameter("userId");
        Map<String, String> fileInfo = cm08Svc.selectFileInfoUser(tempParam);
        //
        if (fileInfo == null || fileInfo.get("fileName") == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write("권한이 없거나 파일 정보를 찾을 수 없습니다.");
            return;
        }
        
        if (null != fileInfo.get("fileName"))  {
            String filePath = fileInfo.get("filePath") + fileInfo.get("fileKey")+ '_' + fileInfo.get("fileName");
            File file = new File(filePath);
            

            if (!file.exists() || !file.isFile()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write("파일이 존재하지 않습니다.");
                return;
            }

            BufferedInputStream bis = null;
            ServletOutputStream sos = null;
            
            try{
				String fileName = file.getName().substring(file.getName().indexOf("_") + 1); // 실제 파일명 추출
				String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(); // 확장자 추출

				// Content-Type 및 Content-Disposition 설정
				String contentType = "application/octet-stream";
				String dispositionType = "attachment";

				if (Arrays.asList("pdf", "jpg", "jpeg", "png", "heic").contains(extension)) {
			        if ("pdf".equals(extension)) contentType = "application/pdf";
			        if ("jpg".equals(extension) || "jpeg".equals(extension)) contentType = "image/jpeg";
			        if ("png".equals(extension)) contentType = "image/png";
			        if ("heic".equals(extension)) contentType = "image/heic";
			        dispositionType = "inline";
			    } // 필요한 경우 다른 이미지 포맷 추가 가능

				response.setContentType(contentType + "; charset=UTF-8");
				response.setContentLengthLong(file.length());
				response.setHeader("Content-Disposition",
						dispositionType + "; filename=\"" + java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20") + "\"");
                
                OutputStream out = response.getOutputStream();
                FileInputStream fis = null;
                
                try {
                    fis = new FileInputStream(file);
                    FileCopyUtils.copy(fis, out);
                    out.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                    // 파일 복사 중 오류 발생 시, 응답을 재설정하고 오류 메시지를 본문에 출력
                    response.reset(); // 이미 버퍼에 쓰여진 데이터 및 헤더 초기화
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.setContentType("text/plain; charset=UTF-8");
                    response.getWriter().write("파일 전송 중 오류 발생: " + e.getMessage());
                } finally {
                    if (fis != null) { try { fis.close(); } catch (Exception e2) {}}
                    if (out != null) { try { out.close(); } catch (Exception e2) {}}
                }
                
            } catch(IOException e){
                e.printStackTrace();
                // 파일 복사 중 오류 발생 시, 응답을 재설정하고 오류 메시지를 본문에 출력
                response.reset(); // 이미 버퍼에 쓰여진 데이터 및 헤더 초기화
                response.setContentType("text/plain; charset=UTF-8");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("파일 전송 중 오류 발생: " + e.getMessage());
            } finally{
                if (bis != null) { try { bis.close(); } catch (Exception e) { } }
                if (sos != null) { try { sos.close(); } catch (Exception e) { } }
            }
        }       
    }
    
	@GetMapping(value="/excelDownload")
	public void excelDownload(@RequestParam String fileName, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		BufferedInputStream bis = null;
		ServletOutputStream sos = null;
		String filePath = "C:\\upload\\";
		try{
			File file = new File(filePath+fileName);
			response.setContentType("application/octet-stream; charset=UTF-8");
			response.setContentLength((int)file.length());
			cm08Svc.setDisposition(request, response, fileName);

			OutputStream out = response.getOutputStream();
	        FileInputStream fis = null;
	         
	        try {
	            fis = new FileInputStream(file);
	            FileCopyUtils.copy(fis, out);
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            if (fis != null) { try { fis.close(); } catch (Exception e2) {}}
	            if (out != null) { try { out.close(); } catch (Exception e2) {}}
	        }
	        out.flush();
	        
		} catch(IOException e){
			e.printStackTrace();
		} finally{
			if(bis != null)
				bis.close();
			if(sos != null)
				sos.close();
		}
	}


    //카테고리별 파일정보 리스트 조회
    @PostMapping("/selectTreeFileList")
    public String selectTreeFileList(@RequestBody Map<String, String> param, ModelMap model) {
    	int totalCnt = cm08Svc.selectTreeFileCount(param);
		PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
    	model.addAttribute("paginationInfo", paginationInfo);
    	
    	List<Map<String, String>> fileList = cm08Svc.selectTreeFileList(param);
    	model.addAttribute("fileList", fileList);
        return "jsonView";
    }
	@PostMapping("/selectTreeFileModule")
	public String selectTreeFileModule(@RequestBody Map<String, String> param, ModelMap model) {
		int totalCnt = cm08Svc.selectTreeFileCount(param);
		PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);

		List<Map<String, String>> fileList = cm08Svc.selectTreeFileModule(param);
		model.addAttribute("fileList", fileList);
		return "jsonView";
	}



	
	@PostMapping(value = "/selectConfirmCount")
	public String selectConfirmCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int result = cm08Svc.selectConfirmCount(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/moveFile")
	public String moveFile(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	//-----------------------------------------------
    	//parameter 정보
    	//  fileKey : 이동하고자 하는 파일 일련번호
    	//  userId  : 이동 작업을 실행하는 사용자 ID
    	//  comonCd : 이동하고자 하는 타겟 디렉토리정보가 담겨져 있음 (To Dir), from Dir은 fileKey의 Data정보에서 확인
    	//-----------------------------------------------

    	//이동하고자 하는 타겟 디렉토리 권한 체크
    	Map<String, String> authInfo = cm15Svc.selectFileAuthInfo(paramMap);
    	if ("Y".equals(authInfo.get("fileUpdate")))  { //"Y" = 이동 작업 권한 있음
			//해당 디렉토리 자료이동 권한이 있어야 작업 가능    	
	    	Map<String, String> tempParam = new HashMap<String, String>();
	    	tempParam.putAll(paramMap);
	    	tempParam.put("jobType", "fileUpdate");
	    	try {
	    		//이동하고자 하는 타겟 디렉토리
	    		Map<String, String> fileInfo = cm08Svc.selectFileInfoUser(tempParam);
	        	if (null != fileInfo.get("fileName"))  {
					cm08Svc.moveFile(paramMap);
			    	model.addAttribute("resultCode", 200);
			    	model.addAttribute("resultMessage", messageUtils.getMessage("move"));
	        	} else {
		    		model.addAttribute("resultCode", 500);
		    		model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
	        	}		    	
			}catch(Exception e) {
				model.addAttribute("resultCode", 900);
	    		model.addAttribute("resultMessage", messageUtils.getMessage("noFileUpdate"));
			}
    	} else {
    		model.addAttribute("resultCode", 900);
    		model.addAttribute("resultMessage", messageUtils.getMessage("noFileUpdate"));
    	}		
    	
    	return "jsonView";
    }
		
    @DeleteMapping(value = "/deleteFileCall")
    public String deleteFileCall(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
	    	int result = cm08Svc.deleteFileCall(paramMap);
	    	model.addAttribute("deleteCount", result);
	    	model.addAttribute("resultCode", 200);
	    	model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
		}catch(Exception e) {
			model.addAttribute("resultCode", 500);
    		model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
		}
    	return "jsonView";
    }
    
    @DeleteMapping(value = "/deleteFileCallAuth")
    public String deleteFileCallAuth(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
    	//-----------------------------------------------
    	//parameter 정보
    	//  fileKey : 이동하고자 하는 파일 일련번호
    	//  userId  : 이동 작업을 실행하는 사용자 ID
    	//-----------------------------------------------		
	   	//해당 디렉토리 삭제 권한이 있어야 작업 가능    	
    	Map<String, String> tempParam = new HashMap<String, String>();
    	tempParam.putAll(paramMap);
    	tempParam.put("jobType", "fileDelete");
    	try {
    		Map<String, String> fileInfo = cm08Svc.selectFileInfoUser(tempParam);
        	if (null != fileInfo.get("fileName"))  {
	    		int result = cm08Svc.deleteFileCall(paramMap);
	    		model.addAttribute("deleteCount", result);
	    		model.addAttribute("resultCode", 200);
	    		model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
        	} else {
	    		model.addAttribute("resultCode", 500);
	    		model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
        	}
    	}catch(Exception e) {
    		model.addAttribute("resultCode", 900);
    		model.addAttribute("resultMessage", messageUtils.getMessage("noFileDelete"));
    	}
    	return "jsonView";
    }
}