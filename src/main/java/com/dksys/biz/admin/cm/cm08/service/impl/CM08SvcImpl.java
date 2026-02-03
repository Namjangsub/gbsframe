package com.dksys.biz.admin.cm.cm08.service.impl;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm08.mapper.CM08Mapper;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.util.DateUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
/****************************************************************************************
 * 2026.02.03
 * 파일 업로드 시 파일 개수 리턴
 * 
 * “파일 1개 저장됐는데 2번째에서 실패하면 1번째가 롤백”은 2가지 레이어로 처리됩니다.
 * 
 * 1) DB 롤백(1번째 insertFile 취소)
 * - 롤백 “로직”은 코드에 delete로 직접 쓰여있는 게 아니라, Spring 트랜잭션 AOP가 합니다.
 * - 해당 메서드들에 붙인 @Transactional(rollbackFor = Exception.class) 때문에, 2번째에서 예외가 발생해 메서드 밖으로 던져지면
 *   트랜잭션이 rollback 되면서 1번째 cm08Mapper.insertFile(...)도 같이 취소됩니다.
 * - 대상 메서드: src/main/java/com/dksys/biz/admin/cm/cm08/service/impl/CM08SvcImpl.java의
 *   - uploadFile(String, String, MultipartHttpServletRequest)
 *   - uploadFile(Map<String,String>, MultipartHttpServletRequest)
 *   - uploadTreeFile(String, Map, MultipartHttpServletRequest)
 *   - copyTreeFile(...)
 *   - fileUpload(...)
 * 
 * 2) 파일시스템 롤백(1번째로 써진 파일 삭제)
 * - 각 업로드/복사 메서드에서 성공적으로 써진 파일을 written 리스트에 넣고,
 * - 이후 실패 시 catch (Exception e)에서 written에 들어있는 파일들을 best-effort로 delete() 하는 부분이 “파일 1개 저장 롤백(정리)” 로직입니다.
 * - 위치: 같은 파일 CM08SvcImpl.java 각 메서드의 catch (Exception e) { for (File f : written) { ... f.delete(); } ... throw ... }
 * 
 * 참고: 트랜잭션 롤백이 실제로 동작하려면 “프록시를 통한 호출”이어야 하는데, fileUpload(...)는 cm08Svc.uploadFile(...)로
 *       호출해서(자기 자신 주입 프록시) 이 조건을 만족합니다. 다른 서비스에서 CM08Svc를 주입받아 호출하는 경우도 보통 프록시
 *       경유라 롤백됩니다.
 * 
 ****************************************************************************************/
@Service
public class CM08SvcImpl implements CM08Svc {
    private static final Logger logger = LoggerFactory.getLogger(CM08SvcImpl.class);

    private static final class FileStorageException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private FileStorageException(String message) {
            super(message);
        }

        private FileStorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @Autowired
    CM08Mapper cm08Mapper;

	@Autowired
    CM08Svc cm08Svc;

	@Autowired
    CM15Svc cm15Svc;

    @Value("${file.uploadDir}")
    private String uploadDir;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int uploadFile(String fileTrgtTyp, String fileTrgtKey, MultipartHttpServletRequest mRequest) {
        List<MultipartFile> fileList =  new ArrayList<>();
        //PM0101M01_M ==> 작업일보에서 출장경비용 첨부파일인경우, 그외 해당 화면의 프로그램명으로 들어옴
        if ("PM0101M01_M".equals(fileTrgtTyp)) {
        	// 작업일보번호 : DM20240402-3615-1  마지막 일련번호 추출
        	String str = fileTrgtKey;
        	String lastPart = "rptTripFiles";
            int lastIndex = str.lastIndexOf('-'); // 마지막 '-'의 위치
            if (lastIndex != -1 && lastIndex < str.length() - 1) { // 마지막 '-' 문자뒤에 있나?
                lastPart += str.substring(lastIndex + 1); 
            }
        	
        	//rptTripFiles1, rptTripFiles2, rptTripFiles3으로 담겨져 넘어옴
//        	fileList = mRequest.getFiles("rptTripFiles1");
        	fileList = mRequest.getFiles(lastPart);
        } else if ("PM5001P01_M".equals(fileTrgtTyp)) {
			String str = fileTrgtKey;
			String lastPart = "bfuFiles";
			int lastIndex = str.lastIndexOf('-'); // 마지막 '-'의 위치
			if (lastIndex != -1 && lastIndex < str.length() - 1) { // 마지막 '-' 문자뒤에 있나?
				lastPart += str.substring(lastIndex + 1); 
			}
			fileList = mRequest.getFiles(lastPart);
		} else {
            fileList = mRequest.getFiles("files");
        }
        
        if (fileList != null && !fileList.isEmpty()) {
        	//저장할 파일이 있다면 여기
        } else { //첨부 파이이 없으므로 작업 종료함.
        	return 0;
        }
        String safeTrgtTyp = requireSafePathSegment(fileTrgtTyp, "fileTrgtTyp");

        String year = DateUtil.getCurrentYyyy();
        String month = DateUtil.getCurrentMm();
        Path baseDir = resolveBaseUploadDir();
        Path dir = baseDir.resolve(Paths.get(safeTrgtTyp, year, month)).normalize();
        if (!dir.startsWith(baseDir)) {
            throw new FileStorageException("Invalid upload path");
        }
        String dirForDb = dir.toString() + File.separator;

        int savedCount = 0;
        List<File> written = new ArrayList<>();
        try {
            Files.createDirectories(dir);

            for (MultipartFile mf : fileList) {
                if (mf == null || mf.isEmpty()) {
                    continue;
                }

                String originFileName = mf.getOriginalFilename();
                String safeFileName = sanitizeFilename(originFileName);
                String fileType = extractFileExtension(safeFileName);

                HashMap<String, String> param = new HashMap<String, String>();
                param.put("fileSize", String.valueOf(mf.getSize()));
                param.put("fileType", fileType);
                param.put("fileName", safeFileName);
                param.put("filePath", dirForDb);
                param.put("fileTrgtTyp", safeTrgtTyp);
                param.put("fileTrgtKey", fileTrgtKey);
                param.put("userId", mRequest.getParameter("userId"));
                param.put("pgmId", safeTrgtTyp);
                param.put("coCd", mRequest.getParameter("coCd"));
                param.put("comonCd", mRequest.getParameter("comonCd"));

                if ("TB_BM02M01".equals(safeTrgtTyp)) {
                    param.put("clntCd", fileTrgtKey);
                    param.put("comonCd", "FITR9903"); //거래처 첨부 디렉토리
                } else {
                    Map<String, String> chk = fetchAllowedDataMap(param);
                    param.put("coCd",    nz(chk,"coCd")    == null ? "" : nz(chk,"coCd"));
                    param.put("clntCd",  nz(chk,"clntCd")  == null ? "" : nz(chk,"clntCd"));
                    param.put("prjctCd", nz(chk,"prjctCd") == null ? "" : nz(chk,"prjctCd"));
                    param.put("ordrsNo", nz(chk,"ordrsNo") == null ? "" : nz(chk,"ordrsNo"));
                    param.put("itemCd",  nz(chk,"itemDiv") == null ? "" : nz(chk,"itemDiv"));
                    param.put("prdtCd",  nz(chk,"prdtCd")  == null ? "" : nz(chk,"prdtCd"));
                    param.put("salesCd", nz(chk,"salesCd") == null ? "" : nz(chk,"salesCd"));
                }

                cm08Mapper.insertFile(param);
                String saveFile = param.get("fileKey") + "_" + safeFileName;
                File target = new File(dirForDb, saveFile);
                mf.transferTo(target);
                written.add(target);
                savedCount++;
            }
        } catch (Exception e) {
            for (File f : written) {
                try {
                    if (f != null && f.exists()) {
                        f.delete();
                    }
                } catch (Exception ignore) {
                    // best effort
                }
            }
            logger.error("File upload failed (typ={}, key={})", safeTrgtTyp, fileTrgtKey, e);
            throw (e instanceof RuntimeException) ? (RuntimeException) e : new FileStorageException("File upload failed", e);
        }
        return savedCount;
    }

    //CLNT_CD,PRDT_CD,ITEM_CD,SALES_CD,PRJCT_CD,CO_CD,COMON_CD-->paramMap 으로 처리
    //화일관리 추가 구현
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int uploadFile(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		String fileTrgtTyp = String.valueOf(paramMap.get("fileTrgtTyp"));
	    	String fileTrgtKey = String.valueOf(paramMap.get("fileTrgtKey"));
	        String safeTrgtTyp = requireSafePathSegment(fileTrgtTyp, "fileTrgtTyp");
      	
      	List<MultipartFile> fileList = mRequest.getFiles("files");
	    	String year = DateUtil.getCurrentYyyy();
	    	String month = DateUtil.getCurrentMm();
	        Path baseDir = resolveBaseUploadDir();
	        Path dir = baseDir.resolve(Paths.get(safeTrgtTyp, year, month)).normalize();
	        if (!dir.startsWith(baseDir)) {
	            throw new FileStorageException("Invalid upload path");
	        }
	        String dirForDb = dir.toString() + File.separator;

	        int savedCount = 0;
	        List<File> written = new ArrayList<>();
	        try {
	            Files.createDirectories(dir);
	            for (MultipartFile mf : fileList) {
	                if (mf == null || mf.isEmpty()) {
	                    continue;
	                }

	                String originFileName = mf.getOriginalFilename();
	                String safeFileName = sanitizeFilename(originFileName);
	                String fileType = extractFileExtension(safeFileName);

	                HashMap<String, String> param = new HashMap<String, String>(paramMap);
	                param.put("fileSize", String.valueOf(mf.getSize()));
	                param.put("fileType", fileType);
	                param.put("fileName", safeFileName);
	                param.put("filePath", dirForDb);
	                param.put("fileTrgtTyp", safeTrgtTyp);
	                param.put("fileTrgtKey", fileTrgtKey);
	                param.put("pgmId", safeTrgtTyp);

	                param.put("coCd",   nz(paramMap,"coCd"));
	                param.put("userId", nz(paramMap,"userId"));
	                param.put("clntCd", nz(paramMap,"clntCd"));
	                param.put("prdtCd", nz(paramMap,"prdtCd"));
	                param.put("itemCd", nz(paramMap,"itemCd"));
	                param.put("salesCd", nz(paramMap,"salesCd"));
	                param.put("prjctCd", nz(paramMap,"prjctCd"));
	                param.put("ordrsNo", nz(paramMap,"ordrsNo"));

	                Map<String, String> chk = fetchAllowedDataMap(param);
	                param.put("coCd",    nz(chk,"coCd")    == null ? "" : nz(chk,"coCd"));
	                param.put("clntCd",  nz(chk,"clntCd")  == null ? "" : nz(chk,"clntCd"));
	                param.put("prjctCd", nz(chk,"prjctCd") == null ? "" : nz(chk,"prjctCd"));
	                param.put("ordrsNo", nz(chk,"ordrsNo") == null ? "" : nz(chk,"ordrsNo"));
	                param.put("itemCd", nz(chk,"itemDiv") == null ? "" : nz(chk,"itemDiv"));
	                param.put("prdtCd",  nz(chk,"prdtCd")  == null ? "" : nz(chk,"prdtCd"));
	                param.put("salesCd", nz(chk,"salesCd") == null ? "" : nz(chk,"salesCd"));

	                cm08Mapper.insertFile(param);
	                String saveFile = param.get("fileKey") + "_" + safeFileName;
	                File target = new File(dirForDb, saveFile);
	                mf.transferTo(target);
	                written.add(target);
	                savedCount++;
	                paramMap.put("fileKey", param.get("fileKey"));
	            }
	        } catch (Exception e) {
	            for (File f : written) {
	                try {
	                    if (f != null && f.exists()) {
	                        f.delete();
	                    }
	                } catch (Exception ignore) {
	                    // best effort
	                }
	            }
	            logger.error("File upload failed (typ={}, key={})", safeTrgtTyp, fileTrgtKey, e);
	            throw (e instanceof RuntimeException) ? (RuntimeException) e : new FileStorageException("File upload failed", e);
	        }
	        return savedCount;
    }
    @Override
    public  int uploadTreeFile(String fileTrgtTyp,String fileTrgtKey, MultipartHttpServletRequest mRequest) {
        return 1;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public  int uploadTreeFile(String fileTrgtTyp,Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
        List<MultipartFile> fileList = mRequest.getFiles("files");
        String safeTrgtTyp = requireSafePathSegment(fileTrgtTyp, "fileTrgtTyp");
        String year = DateUtil.getCurrentYyyy();
        String month = DateUtil.getCurrentMm();
        Path baseDir = resolveBaseUploadDir();
        Path dir = baseDir.resolve(Paths.get(safeTrgtTyp, year, month)).normalize();
        if (!dir.startsWith(baseDir)) {
            throw new FileStorageException("Invalid upload path");
        }
        String dirForDb = dir.toString() + File.separator;

        int savedCount = 0;
        List<File> written = new ArrayList<>();
        try {
            Files.createDirectories(dir);
            for (int i = 0; i < fileList.size(); i++) {
                MultipartFile mf = fileList.get(i);
                if (mf == null || mf.isEmpty()) {
                    continue;
                }

                String originFileName = mf.getOriginalFilename();
                String safeFileName = sanitizeFilename(originFileName);
                String fileType = extractFileExtension(safeFileName);

                HashMap<String, String> param = new HashMap<String, String>();
                param.put("fileSize", String.valueOf(mf.getSize()));
                param.put("fileType", fileType);
                param.put("fileName", safeFileName);
                param.put("filePath", dirForDb);
                param.put("fileTrgtTyp", safeTrgtTyp);
                param.put("userId", mRequest.getParameter("userId"));
                param.put("pgmId", safeTrgtTyp);
                param.put("coCd", mRequest.getParameter("coCd"));
                String comonCdKey = "comonCd_" + i;
                String comonCd = mRequest.getParameter(comonCdKey);
                param.put("comonCd", comonCd);
                param.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
                String userId = mRequest.getParameter("userId");
                param.put("creatId", userId);
                param.put("creatPgm", safeTrgtTyp);

                cm08Mapper.insertFile(param);
                String saveFile = param.get("fileKey") + "_" + safeFileName;
                File target = new File(dirForDb, saveFile);
                mf.transferTo(target);
                written.add(target);
                savedCount++;
            }
        } catch (Exception e) {
            for (File f : written) {
                try {
                    if (f != null && f.exists()) {
                        f.delete();
                    }
                } catch (Exception ignore) {
                    // best effort
                }
            }
            logger.error("Tree file upload failed (typ={})", safeTrgtTyp, e);
            throw (e instanceof RuntimeException) ? (RuntimeException) e : new FileStorageException("Tree file upload failed", e);
        }
        return savedCount;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int copyTreeFile(String fileTrgtTyp, Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
        String safeTrgtTyp = requireSafePathSegment(fileTrgtTyp, "fileTrgtTyp");
        String year = DateUtil.getCurrentYyyy();
        String month = DateUtil.getCurrentMm();

        Path baseDir = resolveBaseUploadDir();
        Path destDir = baseDir.resolve(Paths.get(safeTrgtTyp, year, month)).normalize();
        if (!destDir.startsWith(baseDir)) {
            throw new FileStorageException("Invalid upload path");
        }
        String destDirForDb = destDir.toString() + File.separator;

        int i = 0;
        int copiedCount = 0;
        List<File> written = new ArrayList<>();
        try {
            Files.createDirectories(destDir);
            while (true) {
            String comonCdKey = "comonCd_" + i;
            String filePathKey = "filePath_" + i;
            String fileNameKey = "fileName_" + i;
            String fileKey = "fileKey_" + i;

            String comonCd = mRequest.getParameter(comonCdKey);
            String filePath = mRequest.getParameter(filePathKey);
            String fileName = mRequest.getParameter(fileNameKey);
            String filePKey = mRequest.getParameter(fileKey);

            if (comonCd == null || filePath == null || fileName == null) {
                break; // 해당 키로부터 정보를 얻지 못한 경우, 더 이상의 처리를 멈춥니다.
            }

            String safeSourceFileKey = requireNumericId(filePKey, "fileKey");
            String safeFileName = sanitizeFilename(fileName);

	            Path sourceDir = Paths.get(filePath.trim()).toAbsolutePath().normalize();
	            if (!sourceDir.startsWith(baseDir)) {
	                throw new FileStorageException("Invalid source path");
	            }

            Path sourcePath = sourceDir.resolve(safeSourceFileKey + "_" + safeFileName).normalize();
            if (!sourcePath.startsWith(sourceDir)) {
                throw new FileStorageException("Invalid source file path");
            }

            File sourceFile = sourcePath.toFile();
            if (!sourceFile.exists() || !sourceFile.isFile()) {
                throw new FileStorageException("Source file not found");
            }

            // 복사된 파일에 대한 정보를 DB에 저장
            HashMap<String, String> param = new HashMap<>();
            param.put("fileSize", String.valueOf(sourceFile.length()));
            param.put("fileType", extractFileExtension(safeFileName));
            param.put("fileName", safeFileName);
            param.put("filePath", destDirForDb);
            param.put("fileTrgtTyp", safeTrgtTyp);
            param.put("userId", mRequest.getParameter("userId"));
            param.put("pgmId", safeTrgtTyp);
            param.put("coCd", mRequest.getParameter("coCd"));
            param.put("comonCd", comonCd);
            param.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
            String userId = mRequest.getParameter("userId");
            param.put("creatId", userId);
            param.put("creatPgm", safeTrgtTyp);
            cm08Mapper.insertFile(param);
            String nowFileKey = param.get("fileKey");

            String saveFile = nowFileKey + "_" + safeFileName;
            File targetFile = new File(destDirForDb, saveFile);
            Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            written.add(targetFile);
            copiedCount++;
            i++;
        }
        } catch (Exception e) {
            for (File f : written) {
                try {
                    if (f != null && f.exists()) {
                        f.delete();
                    }
                } catch (Exception ignore) {
                    // best effort
                }
            }
            logger.error("copyTreeFile failed (typ={})", safeTrgtTyp, e);
            throw (e instanceof RuntimeException) ? (RuntimeException) e : new FileStorageException("copyTreeFile failed", e);
        }

        return copiedCount;
    }
    @Override
    public List<Map<String, String>> selectFileList(Map<String, String> paramMap) {
        return cm08Mapper.selectFileList(paramMap);
    }
	@Override
	public int  selectTreeFileCount(Map<String, String> paramMap) {
		return cm08Mapper.selectTreeFileCount(paramMap);
	}
    @Override
    public List<Map<String, String>> selectTreeFileList(Map<String, String> paramMap) {
        return cm08Mapper.selectTreeFileList(paramMap);
    }
    @Override
    public List<Map<String, String>> selectTreeFileModule(Map<String, String> paramMap) {
        System.out.println(paramMap+"트리Log");
        return cm08Mapper.selectTreeFileModule(paramMap);
    }
    @Override
    public void setDisposition(HttpServletRequest request, HttpServletResponse response, String fileName) {
        try {
            String browser = getBrowser(request);
            String dispositionPrefix = "attachment; filename=";
            String encodedFilename = null;
            if (browser.equals("MSIE")) {
                encodedFilename = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            } else if (browser.equals("Trident")) { // IE11 문자열 깨짐 방지
                encodedFilename = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            } else if (browser.equals("Firefox")) {
                encodedFilename = "\"" + new String(fileName.getBytes("UTF-8"), "8859_1") + "\"";
            } else if (browser.equals("Opera")) {
                encodedFilename = "\"" + new String(fileName.getBytes("UTF-8"), "8859_1") + "\"";
            } else if (browser.equals("Chrome")) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < fileName.length(); i++) {
                    char c = fileName.charAt(i);
                    if (c > '~') {
                        sb.append(URLEncoder.encode("" + c, "UTF-8"));
                    } else {
                        sb.append(c);
                    }
                }
                encodedFilename = "\"" + sb.toString() + "\"";
            } else {
                throw new IOException("Not supported browser");
            }
            response.setHeader("Content-Disposition", dispositionPrefix + encodedFilename);
            if ("Opera".equals(browser)) {
                response.setContentType("application/octet-stream;charset=UTF-8");
            }
            response.setHeader("Content-Transfer-Encoding", "binary");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getBrowser(HttpServletRequest request) {
        String header = request.getHeader("User-Agent");
        if (header.indexOf("MSIE") > -1) {
            return "MSIE";
        } else if (header.indexOf("Trident") > -1) { // IE11 문자열 깨짐 방지
            return "Trident";
        } else if (header.indexOf("Chrome") > -1) {
            return "Chrome";
        } else if (header.indexOf("Opera") > -1) {
            return "Opera";
        }
        return "Firefox";
    }
    @Override
    public Map<String, String> selectFileInfo(String fileKey) {
        return cm08Mapper.selectFileInfo(fileKey);
    }
    @Override
    public int deleteFile(String fileKey) {
        Map<String, String> fileInfo = selectFileInfo(fileKey);
        String filePath = fileInfo.get("filePath") + fileKey + "_" + fileInfo.get("fileName");
        int result = cm08Mapper.deleteFile(fileKey);
        try {
            File f = new File(filePath);
            f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    @Override
    public String excelDownload(List<Map<String, String>> list, String fileName) {
        if (list.size() == 0) return "noData";
        try (XSSFWorkbook xWorkbook = new XSSFWorkbook()) {
            XSSFCellStyle numberCellStyle = xWorkbook.createCellStyle();
            XSSFDataFormat numberDataFormat = xWorkbook.createDataFormat();
            numberCellStyle.setDataFormat(numberDataFormat.getFormat("#,##0"));
            XSSFSheet sheet = xWorkbook.createSheet("sheet1");
            Row row = null;
            int j = 0;
            //컬럼 생성
            row = sheet.createRow(0);
            Map<String, String> map = list.get(0);
            //컬럼 생성
            for (String key : map.keySet()) {
                Cell cell = row.createCell(j);
                cell.setCellValue(key);
                j++;
            }
            //행 데이터 생성
            for (int i = 0; i < list.size(); i++) {
                j = 0;
                map = list.get(i);
                row = sheet.createRow(i + 1);
                for (String key : map.keySet()) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(map.get(key));
                    j++;
                }
            }
            FileOutputStream fileOut = null;
            File f = new File(uploadDir, fileName);
//        	if(!f.isDirectory()) f.mkdirs();
            fileOut = new FileOutputStream(f);
            xWorkbook.write(fileOut);
            f.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }
	@Override
	public int selectConfirmCount(Map<String, String> paramMap) {
		return cm08Mapper.selectConfirmCount(paramMap);
	}
	@Override
	public int moveFile(Map<String, String> paramMap) {
		// update
		int result = cm08Mapper.moveFile(paramMap);
		return result;
	}
	@Override
	public int deleteFileCall(Map<String, String> paramMap) {
		String fileKey = paramMap.get("fileKey");
        Map<String, String> fileInfo = selectFileInfo(fileKey);
        String filePath = fileInfo.get("filePath") + fileKey + "_" + fileInfo.get("fileName");
        int result = cm08Mapper.deleteFile(fileKey);
        try {
            File f = new File(filePath);
            f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;		
	}
	
    @Override
    public Map<String, String> selectFileInfoUser(Map<String, String> paramMap) {
        return cm08Mapper.selectFileInfoUser(paramMap);
    }

	@Override
	public List<Map<String, String>> selectFileListAll(Map<String, String> paramMap) {
		return cm08Mapper.selectFileListAll(paramMap);
	}
    
	// 유틸: null/빈/문자열 "null"/"undefined" → null
	private static String nz(Map<String, String> m, String k) {
	    if (m == null) return null;
	    String v = m.get(k);
	    if (v == null) return null;
	    v = v.trim();
	    if (v.isEmpty()) return null;
	    if ("null".equalsIgnoreCase(v) || "undefined".equalsIgnoreCase(v)) return null;
	    return v;
	}


    private static Map<String, String> outMap() {
        // 순서 보장
        return new LinkedHashMap<String, String>(8);
    }

    private static boolean anyEqualsIgnoreCase(String s, String... arr) {
        if (s == null) return false;
        for (String a : arr) {
            if (s.equalsIgnoreCase(a)) return true;
        }
        return false;
    }

    /** 공통 아웃맵 채우기 (null -> "") */
    private static void fillOut(Map<String, String> out,
                                String coCd, String clntCd, String prjctCd,
                                String ordrsNo, String itemDiv, String prdtCd, String salesCd) {
        out.put("coCd",   coCd   == null ? "" : coCd);
        out.put("clntCd", clntCd == null ? "" : clntCd);
        out.put("prjctCd",prjctCd== null ? "" : prjctCd);
        out.put("ordrsNo",ordrsNo== null ? "" : ordrsNo);
        out.put("itemDiv",itemDiv== null ? "" : itemDiv);
        out.put("prdtCd", prdtCd == null ? "" : prdtCd);
        out.put("salesCd",salesCd== null ? "" : salesCd);
    }
    
    // fileTrgtTyp: SALES_CD | ORDRS_NO | CLNT_PJT | CLNT_CD | M.CO_CD
	/*******************************************************************************
	 * 문서등록업무명	        : 발생업무코드	     |coCd clntCd prjctCd salesCd prdtCd itemCd
	 * ----------------------------------------------------------------------------
     * 자재마스터				: BM0501P01			O					
     * 메뉴얼					: TB_BM99P01        O					
     * 공지사항				: TB_CM09M01        O					
     * 자료실					: TB_CM14M01        O					
     * 임팀장 주간회의 자료		: PM1002M01         O					
     * 업무요청관리				: CM1601M01         O					
     * 거래처 관리				: TB_BM02M01        O	O				
     * 프로젝트 이슈관리			: BM1601P02     	O	O	O			
     * 견적서관리				: CR0101M01         O	O	O			
     * 수주 등록				: CR0202P01         O	O	O			
     * 수금 등록				: CR0501P01         O	O	O			
     * 매출계산서 등록			: CR0801P01         O	O	O	
     * 개선 제안서 등록			: IM0101P01         O	O	O	O	O	O		
     * 물류진행요청 등록			: CR1001P01     	O	O	O	O	O	O
     * PFU관리				: CR5001P01         O	O	O	O	O	O
     * 작업일보관리				: PM0101M01         O	O	O	O	O	O
     * 작업일보관리				: PM0101M01_M       O	O	O	O	O	O
     * 작업일보관리				: PM0101P01         O	O	O	O	O	O
     * 출장자 사진 Upload작업	: PM5001M01         O	O	O	O	O	O
     * 출장자 사진 Upload작업	: PM5001P01_M       O	O	O	O	O	O
     * 요인별 발주 및 출장요청		: QM0101P01     	O	O	O	O	O	O
     * 요인별 발주 및 출장결과		: QM0101P03     	O	O	O	O	O	O
     * 매입 발주 등록			: SM0201P01         O	O	O	O	O	O
     * 매입 입고				: SM0301P01         O	O	O	O	O	O
     * 구매비용 등록			: SM1001P01         O	O	O	O	O	O
     * 외주관리 등록			: SM1101P01         O	O	O	O	O	O
     * 과제 관리 등록			: WB2101P01         O	O	O	O	O	O
     * TASK 실적 등록			: WB2201P02         O	O	O	O	O	O
     * WBS 계획 문제			: WB2401P01         O	O	O	O	O	O
     * WBS 실적 문제			: WB2401P11         O	O	O	O	O	O
	 ********************************************************************************/
    private Map<String, String> fetchAllowedDataMap (Map<String, String> paramMap) {

        if (paramMap == null) return Collections.emptyMap();
        

        // 1) typ 정규화 (없으면 빈맵 반환)
        String typ = nz(paramMap, "fileTrgtTyp");
        if (typ == null) return Collections.emptyMap();
        typ = typ.toUpperCase();
        
        
        // 즉시 반환 그룹 1: coCd만 관리 → coCd 외 나머지 공란
        if (anyEqualsIgnoreCase(typ,
                "BM0501P01","TB_BM99P01","TB_CM09M01","TB_CM14M01","PM1002M01","CM1601M01")) {
            Map<String, String> out = outMap();
            fillOut(out,
                    nz(paramMap,"coCd"), null, null,
                    null, null, null, null);
            return out;
        }


        // 즉시 반환 그룹 2: 거래처 관리
        if ("TB_BM02M01".equalsIgnoreCase(typ)) {
            Map<String, String> out = outMap();
            fillOut(out,
                    nz(paramMap,"coCd"), nz(paramMap,"clntCd"), null,
                    null, null, null, null);
            return out;
        }


        // 즉시 반환 그룹 3: 거래처/프로젝트코드 관련
        if (anyEqualsIgnoreCase(typ, "BM1601P01", "BM1601P02","CR0202P01","CR0501P01", "CR0701P01", "CR0801P01")) {
            Map<String, String> out = outMap();
            fillOut(out,
                    nz(paramMap,"coCd"), nz(paramMap,"clntCd"), nz(paramMap,"prjctCd"),
                    nz(paramMap,"ordrsNo"), null, null, null);
            return out;
        }
        

        // 이외의 경우: DB 조회
        /******************************************************************************************
         * 1. 각 업무별 테이블에 있는 값을 기준으로설정하기
         *******************************************************************************************/
        Map<String, String> m = cm08Mapper.selectMByTarget(paramMap);
        if (m == null) {
            return Collections.emptyMap();
        }
        m.put("FILE_TRGT_TYP",typ); //String typ = nz(paramMap, "fileTrgtTyp");

        // 타입 결정 우선순위: salesCd > ordrsNo > clntCd > coCd
        String salesCd = nz(m,"salesCd"); if (salesCd == null) salesCd = nz(paramMap,"salesCd");
        String ordrsNo = nz(m,"ordrsNo"); if (ordrsNo == null) ordrsNo = nz(paramMap,"ordrsNo");
        String clntCd  = nz(m,"clntCd");  if (clntCd  == null) clntCd  = nz(paramMap,"clntCd");
        String coCd    = nz(m,"coCd");    if (coCd    == null) coCd    = nz(paramMap,"coCd");
        
        String type;
        if (salesCd != null)       type = "SALES_CD";
        else if (ordrsNo != null)  type = "ORDRS_NO";
        else if (clntCd  != null)  type = "CLNT_CD";
        else if (coCd    != null)  type = "CO_CD";
        else return Collections.emptyMap();
        	

        Map<String, String> out = outMap();
        Map<String, String> rm;

        /******************************************************************************************
         * 2. 추출가능한 자료를 기준으로 파일 저장 테이블 설정하기 
         *    m --> camelMap 으로 생성되므로 값변경시 UPPER_SNAKE_CASE(모든글자 대문자, 단어사이는 _(underScore)로 구분
         *    m.put("SALES_CD", salesCd)
         *******************************************************************************************/
        if ("SALES_CD".equals(type)) {
            m.put("type", "SALES_CD");
            m.put("SALES_CD", salesCd);
            rm = cm08Mapper.selectMByRetriveValue(m); // 전부 허용
            if (rm == null) rm = Collections.<String,String>emptyMap();
            fillOut(out,
                    nz(rm,"coCd"), nz(rm,"clntCd"), nz(rm,"prjctCd"),
                    nz(rm,"ordrsNo"), nz(rm,"itemDiv"), nz(rm,"prdtCd"), nz(rm,"salesCd"));
            return out;

        } else if ("ORDRS_NO".equals(type)) {
            m.put("type", "ORDRS_NO");
            m.put("ORDRS_NO", ordrsNo);
            rm = cm08Mapper.selectMByRetriveValue(m); // ordrsNo, clntPjt, clntCd, coCd
            if (rm == null) rm = Collections.<String,String>emptyMap();
            fillOut(out,
                    nz(rm,"coCd"), nz(rm,"clntCd"), nz(rm,"prjctCd"),
                    nz(rm,"ordrsNo"), null, null, null); // itemDiv/prdtCd/salesCd는 공란
            return out;

        } else if ("CLNT_CD".equals(type)) {
            fillOut(out,
            		coCd, clntCd, null,
                    null, null, null, null);
            return out;

        }
//        } else if ("CO_CD".equals(type)) {
            // coCd만
            fillOut(out,
                    coCd, null, null,
                    null, null, null, null);
            return out;

    }


    @Override
    public long nextDlHistId() {
        return cm08Mapper.nextDlHistId();
    }


    @Override
    public int insertDnldStart(Map<String, Object> hist) {
        return cm08Mapper.insertDnldStart(hist);
    }
    
    
    @Override
    public int updateDnldEnd(Map<String, Object> end) {
        return cm08Mapper.updateDnldEnd(end);
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int fileUpload(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type mapList = new TypeToken<ArrayList<Map<String, String>>>() { }.getType();
		int result = 0;
		 //---------------------------------------------------------------
        //첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
        //   필수값 :  jobType, userId, comonCd
        //---------------------------------------------------------------
        HashMap<String, String> param = new HashMap<>();
        param.put("userId", paramMap.get("userId"));
        param.put("comonCd", paramMap.get("comonCd"));  //프로트엔드에 넘어온 화일 저장 위치 정보
        param.put("coCd", paramMap.get("coCd"));

        List<Map<String, String>> uploadFileList = gsonDtl.fromJson(paramMap.get("uploadFileArr"), mapList);
        if (uploadFileList.size() > 0) {
            //접근 권한 없으면 Exception 발생 (jobType, userId, comonCd 3개 필수값 필요)
        	param.put("jobType", "fileUp");
            cm15Svc.selectFileAuthCheck(param);
        }
		String[] deleteFileArr = gsonDtl.fromJson(paramMap.get("deleteFileArr"), String[].class);
		List<String> deleteFileList = Arrays.asList(deleteFileArr);
	    for(String fileKey : deleteFileList) {  // 삭제할 파일 하나씩 점검 필요(전체 목록에서 삭제 선택시 필요함)
			    Map<String, String> fileInfo = cm08Svc.selectFileInfo(fileKey);
				//접근 권한 없으면 Exception 발생
			    param.put("comonCd", fileInfo.get("comonCd"));  //삭제할 파일이 보관된 저장 위치 정보
			    param.put("jobType", "fileDelete");
				cm15Svc.selectFileAuthCheck(param);
		}
        
        //---------------------------------------------------------------
        //첨부 화일 권한체크  끝
        //---------------------------------------------------------------

		//---------------------------------------------------------------
        //첨부 화일 처리 시작
        //---------------------------------------------------------------
        if (uploadFileList.size() > 0) {
          	param.put("fileTrgtTyp", paramMap.get("pgmId"));
          	param.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
			result += cm08Svc.uploadFile(param, mRequest);
        }

        for(String fileKey : deleteFileList) {
            result += cm08Svc.deleteFile(fileKey);
        }
        //---------------------------------------------------------------
        //첨부 화일 처리  끝
        //---------------------------------------------------------------

		return result;
	}

	private Path resolveBaseUploadDir() {
	    String base = (uploadDir == null) ? null : uploadDir.trim();
	    if (base == null || base.isEmpty()) {
	        throw new FileStorageException("Upload base directory is not configured");
	    }
	    return Paths.get(base).toAbsolutePath().normalize();
	}

	private static String requireSafePathSegment(String v, String field) {
	    if (v == null) {
	        throw new FileStorageException(field + " is required");
	    }
	    String s = v.trim();
	    if (s.isEmpty()) {
	        throw new FileStorageException(field + " is required");
	    }
	    // allow: letters/digits/_/- only (blocks traversal and path separators)
	    if (!s.matches("[A-Za-z0-9_-]+")) {
	        throw new FileStorageException("Invalid " + field);
	    }
	    return s;
	}

	private static String requireNumericId(String v, String field) {
	    if (v == null) {
	        throw new FileStorageException(field + " is required");
	    }
	    String s = v.trim();
	    if (!s.matches("\\d+")) {
	        throw new FileStorageException("Invalid " + field);
	    }
	    return s;
	}

	private static String sanitizeFilename(String name) {
	    String s = (name == null) ? "" : name;
	    // strip any client-supplied path
	    s = s.replace('\\', '/');
	    int slash = s.lastIndexOf('/');
	    if (slash >= 0) {
	        s = s.substring(slash + 1);
	    }
	    s = s.trim();
	    if (s.isEmpty()) {
	        s = "file";
	    }
	    // remove control chars
	    s = s.replaceAll("[\\x00-\\x1F\\x7F]", "");
	    // replace Windows-illegal characters
	    s = s.replaceAll("[\\\\/:*?\"<>|]", "_");
	    // avoid trailing dot/space on Windows
	    while (s.endsWith(" ") || s.endsWith(".")) {
	        s = s.substring(0, s.length() - 1);
	    }
	    if (s.isEmpty() || ".".equals(s) || "..".equals(s)) {
	        s = "file";
	    }
	    // cap length to avoid filesystem limits
	    if (s.length() > 180) {
	        s = s.substring(0, 180);
	    }
	    return s;
	}

	private static String extractFileExtension(String fileName) {
	    if (fileName == null) return "bin";
	    int dot = fileName.lastIndexOf('.');
	    if (dot <= 0 || dot == fileName.length() - 1) {
	        return "bin";
	    }
	    String ext = fileName.substring(dot + 1).trim();
	    if (ext.isEmpty()) return "bin";
	    // keep DB stable and avoid weird characters
	    if (!ext.matches("[A-Za-z0-9]+")) return "bin";
	    return ext.toLowerCase();
	}
    
}
