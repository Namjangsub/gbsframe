package com.dksys.biz;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jodconverter.core.DocumentConverter;   // ★ 이게 핵심
import org.jodconverter.core.office.OfficeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.config.RequestUtils;

@RestController
@RequestMapping("/api/convert")
public class OfficeConvertController {

    private final DocumentConverter converter;

	@Autowired
	CM08Svc cm08Svc;
 	
	@Autowired
	CM15Svc cm15Svc;
	
    // 변환된 PDF 저장 위치
    private final Path outputDir = Paths.get("D:/office-preview"); // 서버에 맞게

    public OfficeConvertController(DocumentConverter converter) throws Exception {
        this.converter = converter;
        Files.createDirectories(outputDir);
    }

    /**
     * 1) 프런트에서 직접 파일 업로드해서 변환하는 경우
     */
    @PostMapping("/pdf")
    public ResponseEntity<?> convertToPdf(@RequestParam("file") MultipartFile file, Authentication authentication) throws Exception {
//
//        System.out.println("[/api/convert/pdf] auth = {}" +  authentication);
//        if (authentication != null) {
//        	System.out.println("principal = {}"+ authentication.getName());
//        	System.out.println("authorities = {}" + authentication.getAuthorities());
//        }
//    	
//        String originalName = file.getOriginalFilename();
//        if (originalName == null) {
//            originalName = "upload";
//        }
//
//        String ext = StringUtils.getFilenameExtension(originalName);
//        if (ext == null) {
//            return ResponseEntity.badRequest().body("no extension");
//        }
//        ext = ext.toLowerCase();
//
//        String id = UUID.randomUUID().toString().replace("-", "");
//        Path srcPath = outputDir.resolve(id + "." + ext);
//
//        // 업로드 파일을 작업폴더에 먼저 저장
//        file.transferTo(srcPath.toFile());
//
//        Path pdfPath = outputDir.resolve(id + ".pdf");
//        if ("pdf".equals(ext)) {
//        	//옵션 1) 다시 저장
////        	Files.copy(srcPath, pdfPath);
//        	// 업로드가 pdf면 우리가 쓰는 파일명으로 맞춰서 복사
////        	Files.move(srcPath, pdfPath, StandardCopyOption.REPLACE_EXISTING);
//        } else {
//        	// 3) pdf가 아니면 변환try {
//        	try {
//        	    converter.convert(srcPath.toFile())
//        	             .to(pdfPath.toFile())
//        	             .execute();
//        	} catch (OfficeException e) {
//        	    // 여기서 바로 또 convert() 하지 말고
//        	    // 프런트에 "503, 잠시 후 재요청" 보내는 게 안전
//        	    return ResponseEntity.status(503)
//        	        .body("변환 서버가 지금 바쁩니다. 3~5초 후 다시 요청해주세요.");
//        	}
//        }
//        
//        // 프런트는 이 URL을 pdf.js viewer.html?file=... 에 넘기면 됨
//        String pdfUrl = "/api/convert/pdf/" + id;
//        return ResponseEntity.ok(pdfUrl);
        return ResponseEntity.ok("Do not use PDF for submission/posting.");
    }

    /**
     * 2) 이미 서버에 저장된 파일(fileKey 등) → 변환
     */
    @GetMapping("/pdf")
    public ResponseEntity<?> convertToPdf(@RequestParam Map<String, String> param, HttpServletRequest request, Authentication authentication) throws Exception {

//        System.out.println("[/api/convert/pdf] auth = {}" +  authentication);
//        if (authentication != null) {
//        	System.out.println("principal = {}"+ authentication.getName());
//        	System.out.println("authorities = {}" + authentication.getAuthorities());
//        }

        String source = request.getHeader("X-GBS-Source");
        if (!"pdfjs-viewer".equals(source)) {
            System.out.println("주소창/링크 등 기타 요청일 가능성");
            String json = "{\"error\":\"정상적인 접근이 아닙니다.\"}";
            // 정상적인 접근이 아닌 경우: 403 + 메시지 반환
            return ResponseEntity.status(403)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(json.getBytes(StandardCharsets.UTF_8));   // byte[] 로 변환
        }
        
        //-----------------------------------------------
        //parameter 정보
        //  fileKey : 이동하고자 하는 파일 일련번호
        //  userId  : 이동 작업을 실행하는 사용자 ID
        //-----------------------------------------------       
    	// 1. 파일 정보 조회        
        Map<String, String> tempParam = new HashMap<String, String>();
        tempParam.putAll(param);
        tempParam.put("jobType", "fileDown");
        Map<String, String> fileInfo = cm08Svc.selectFileInfoUser(tempParam);
        //
        if (fileInfo == null || fileInfo.get("fileName") == null) {
            return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND)
                    .body("권한이 없거나 파일 정보를 찾을 수 없습니다.");
        }
        
        String filePath = fileInfo.get("filePath") + fileInfo.get("fileKey")+ '_' + fileInfo.get("fileName");
        File originFile = new File(filePath);
        if (!originFile.exists() || !originFile.isFile()) {
            return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND)
                                 .body("파일이 존재하지 않습니다.");
        }

        // ===== 1) 이력 START insert =====
        long dlHistId = cm08Svc.nextDlHistId();

        Map<String, Object> hist = new HashMap<>();
        hist.put("dlHistId", dlHistId);

        hist.put("fileKey", fileInfo.get("fileKey"));
        hist.put("fileName", fileInfo.get("fileName")); 
        hist.put("filePath", fileInfo.get("filePath"));

        hist.put("userId", tempParam.get("userId"));

        hist.put("deviceType", RequestUtils.detectDeviceType(RequestUtils.getUserAgent()));
        hist.put("clientIp", RequestUtils.getClientIp());
        hist.put("xffIp", RequestUtils.getClientIp());
        hist.put("userAgent", RequestUtils.cut(RequestUtils.getUserAgent(), 900));
        hist.put("referer", RequestUtils.cut(request.getHeader("Referer"), 900));

        String reqId = UUID.randomUUID().toString().replace("-", "");
        hist.put("reqId", reqId);
        hist.put("reqUri", RequestUtils.cut(request.getRequestURI(), 900));
        hist.put("httpMethod", request.getMethod());

        cm08Svc.insertDnldStart(hist);

        // 확장자
        String fileName = fileInfo.get("fileName");           // 예: spec.pptx

        int dotPos = fileName.lastIndexOf('.');
        String nameOnly;  // 확장자 뺀 파일명
        String ext;       // 확장자(소문자), 없으면 null
        if (dotPos > 0 && dotPos < fileName.length() - 1) {
            // "."이 맨 앞도 아니고, 끝도 아니면 확장자 있다고 판단
            nameOnly = fileName.substring(0, dotPos);           // report_2025
            ext      = fileName.substring(dotPos + 1).toLowerCase(); // pptx
        } else {
            // 확장자 없음
            nameOnly = fileName;
            ext = null;
        }
//        
//        // 임시 id 만들고, 원본을 작업 디렉토리로 복사
//        String id = UUID.randomUUID().toString().replace("-", "");
//        Path srcPath = outputDir.resolve(id + "." + ext);     // D:/office-preview/{id}.pptx
//        // 원본을 작업 디렉터리에 복사 (여러 번 호출해도 덮어써지게)
//        Files.copy(originFile.toPath(), srcPath, StandardCopyOption.REPLACE_EXISTING);             // 원본 → 작업폴더
//        

        // 변환 결과 경로
        String outFileName = fileInfo.get("fileKey") + '_' + nameOnly + ".pdf";
        Path pdfPath = outputDir.resolve(outFileName); 
        File targetFile = pdfPath.toFile();
        long bytesSent = 0L;
        if (!targetFile.exists()) {
            // pdf면 복사만, 아니면 변환
            if ("pdf".equalsIgnoreCase(ext)) {
            	Files.copy(originFile.toPath(), pdfPath, StandardCopyOption.REPLACE_EXISTING);
            	bytesSent = RequestUtils.toLong(fileInfo.get("fileSize"));
            } else {
            	// 너무 큰 파일 차단
            	bytesSent = originFile.length();
                if (bytesSent > 150 * 1024 * 1024) { // 150MB 이상

                    // ===== 4) 실패 END update =====
                    Map<String, Object> end = new HashMap<>();
                    end.put("dlHistId", dlHistId);
                    end.put("resultCd", "F");
                    end.put("httpStatus",413);
                    end.put("bytesSent", bytesSent);
                    end.put("errMsg", "파일이 너무 큽니다. 150MB 이하만 미리보기 가능합니다.");
                    try { cm08Svc.updateDnldEnd(end); } catch (Exception ignore) {}
                    
                    return ResponseEntity
                        .status(413)
                        .body("파일이 너무 큽니다. 150MB 이하만 미리보기 가능합니다.");
                }
                // 변환
            	try {
            		// LibreOffice 변환
            	    converter.convert(originFile)
            	             .to(pdfPath.toFile())
            	             .execute();
                } catch (org.jodconverter.core.office.OfficeException e) {

                    // ===== 4) 실패 END update =====
                    Map<String, Object> end = new HashMap<>();
                    end.put("dlHistId", dlHistId);
                    end.put("resultCd", "F");
                    end.put("httpStatus",503);
                    end.put("bytesSent", bytesSent);
                    end.put("errMsg", "변환 서버가 사용 중입니다. 잠시 후 다시 시도하세요.");
                    try { cm08Svc.updateDnldEnd(end); } catch (Exception ignore) {}
                    
                    // 오피스 프로세스 못 빌렸을 때
                    return ResponseEntity.status(503)
                            .body("변환 서버가 사용 중입니다. 잠시 후 다시 시도하세요. " + e.getMessage());
                } catch (Exception e) {

                    // ===== 4) 실패 END update =====
                    Map<String, Object> end = new HashMap<>();
                    end.put("dlHistId", dlHistId);
                    end.put("resultCd", "F");
                    end.put("httpStatus",500);
                    end.put("bytesSent", bytesSent);
                    end.put("errMsg", "문서 변환 중 오류가 발생했습니다.");
                    try { cm08Svc.updateDnldEnd(end); } catch (Exception ignore) {}
                    
                    return ResponseEntity.status(500)
                            .body("문서 변환 중 오류가 발생했습니다. " + e.getMessage());
                }
            }
        }

    	// ===== 3) 성공 END update =====
        Map<String, Object> end = new HashMap<>();
        end.put("dlHistId", dlHistId);
        end.put("resultCd", "S");
        end.put("httpStatus", 200);
		end.put("bytesSent", bytesSent);
        end.put("errMsg", null);
        cm08Svc.updateDnldEnd(end);
        
        // 프론트가 이 URL로 다시 GET 하면 pdf 내려가게
        String pdfUrl = "/api/convert/pdf/" + outFileName;
        return ResponseEntity.ok(pdfUrl);
        }

    /**
     * 3) 실제 PDF 내려주는 엔드포인트 (pdf.js가 여기로 다시 요청)
     */
    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> getPdf(@PathVariable String id, HttpServletRequest request, Authentication authentication) throws Exception {

//        System.out.println("[/api/convert/pdf] auth = {}" +  authentication);
//        if (authentication != null) {
//        	System.out.println("principal = {}"+ authentication.getName());
//        	System.out.println("authorities = {}" + authentication.getAuthorities());
//        }       
        String source = request.getHeader("X-GBS-Source");
        if (!"pdfjs-viewer".equals(source)) {
            System.out.println("주소창/링크 등 기타 요청일 가능성");
            String json = "{\"error\":\"정상적인 접근이 아닙니다.\"}";
            // 정상적인 접근이 아닌 경우: 403 + 메시지 반환
            return ResponseEntity.status(403)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(json.getBytes(StandardCharsets.UTF_8));   // byte[] 로 변환
        }
        
        Path pdfPath= outputDir.resolve(id);
        if (!Files.exists(pdfPath)) {
            return ResponseEntity.notFound().build();
        }
        byte[] bytes = Files.readAllBytes(pdfPath);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .body(bytes);
    }

}