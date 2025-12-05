package com.dksys.biz.auth;

import com.dksys.biz.auth.vo.VersionInfo;
import com.dksys.biz.auth.service.AppUpdateService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@RestController
@RequestMapping("/api/app")
public class AppUpdateController {

    private final AppUpdateService appUpdateService;

    public AppUpdateController(AppUpdateService appUpdateService) {
        this.appUpdateService = appUpdateService;
    }

    /**
     * Flutter / AppUpdateManager 에서 사용하는 체크 URL
     * checkUrl: https://gbs.gunyangitt.co.kr/api/app/versionCheck
     */
    @GetMapping("/versionCheck")
    public ResponseEntity<?> versionCheck(HttpServletRequest request) {

        // JWT + mTLS 검증은 Filter에서 끝났다고 가정
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UNAUTHORIZED");
        }

        String baseUrl = getBaseUrl(request);

        try {
            VersionInfo info = appUpdateService.getLatestVersion(baseUrl);
            // 여기서는 clientVersionCode 비교 안 함
            // AppUpdateManager 에서 latestCode vs currentCode 로 판단

            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Version check failed");
        }
    }

    /**
     * 필요시 /version 이라는 이름으로도 제공 (웹/기타 클라이언트용)
     *  - AppUpdateManager 는 /versionCheck 를 사용
     */
    @GetMapping("/version")
    public ResponseEntity<?> getAppVersion(HttpServletRequest request) {
        return versionCheck(request);
    }

    /**
     * APK 스트리밍 다운로드 API
     * - AppUpdateService.getLatestVersion() 에서 생성한 apkUrl과 연결
     *   예: /api/app/apk?versionCode=10002
     */
    @GetMapping("/apk")
    public ResponseEntity<?> downloadApk(
            HttpServletRequest request,
            @RequestParam("versionCode") int versionCode) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UNAUTHORIZED");
        }

        try {
            File apkFile = appUpdateService.resolveApkFileByVersionCode(versionCode);
            if (!apkFile.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("APK not found");
            }

            Resource resource = new FileSystemResource(apkFile);
            String encodedName = URLEncoder.encode(apkFile.getName(), StandardCharsets.UTF_8.name())
                    .replaceAll("\\+", "%20");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.android.package-archive"));
            headers.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName);

            return new ResponseEntity<Resource>(resource, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("APK download failed");
        }
    }


    // ================= 내부 유틸 =================

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null || scheme.isEmpty()) {
            scheme = request.getScheme();
        }
        String host = request.getHeader("X-Forwarded-Host");
        if (host == null || host.isEmpty()) {
            host = request.getServerName() + ":" + request.getServerPort();
        }
        return scheme + "://" + host;
    }
}
