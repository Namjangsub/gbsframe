package com.dksys.biz;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class FileDownloadController {

    // 파일이 저장된 디렉토리 경로
    private static final String FILE_DIRECTORY = "D:\\gunyang\\";

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) throws IOException {
        // 파일 경로 생성
        Path filePath = Paths.get(FILE_DIRECTORY + fileName);
        // 파일을 Resource 객체로 로드
        Resource resource = new UrlResource(filePath.toUri());

        // 다운로드를 위한 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);

        // 파일을 ResponseEntity로 반환하여 다운로드
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}