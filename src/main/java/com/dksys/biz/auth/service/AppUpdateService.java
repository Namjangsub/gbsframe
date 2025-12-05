package com.dksys.biz.auth.service;

import com.dksys.biz.auth.vo.VersionInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AppUpdateService {

    // APK 디렉토리 (application.yml / properties 에서 설정 가능)
    @Value("${gbs.gunyang.appupdate.apk-dir:D:/gunyang/appupdate/apk-dir}")
    private String apkDir;  // 예: d:/gunyang/appupdate/apk/GBS_ERP

    // 파일명 패턴: GBS_ERP_1.0.2.apk
    private static final Pattern APK_NAME_PATTERN =
            Pattern.compile("^GBS_ERP_(\\d+)\\.(\\d+)\\.(\\d+)\\.apk$", Pattern.CASE_INSENSITIVE);


    /**
     * 최신 버전 정보 조회
     * - apkDir 아래 GBS_ERP_*.apk 스캔
     * - 파일명에서 버전 파싱 후 가장 큰 버전 선택
     */
    public VersionInfo getLatestVersion(String baseUrl) {
        File dir = new File(apkDir);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalStateException("APK directory not found: " + apkDir);
        }

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            throw new IllegalStateException("No APK files in directory: " + apkDir);
        }

        File latestFile = null;
        int latestVersionCode = -1;
        String latestVersionName = null;

        for (File file : files) {
            if (!file.isFile()) continue;

            String name = file.getName();
            Matcher m = APK_NAME_PATTERN.matcher(name);
            if (!m.matches()) {
                // 규칙에 맞지 않는 파일은 무시
                continue;
            }

            int major = Integer.parseInt(m.group(1));
            int minor = Integer.parseInt(m.group(2));
            int patch = Integer.parseInt(m.group(3));

            int vCode = toVersionCode(major, minor, patch);
            String vName = major + "." + minor + "." + patch;

            if (vCode > latestVersionCode) {
                latestVersionCode = vCode;
                latestVersionName = vName;
                latestFile = file;
            }
        }

        if (latestFile == null) {
            throw new IllegalStateException("No valid GBS_ERP_*.apk in directory: " + apkDir);
        }

        String apkName = latestFile.getName();
        String apkUrl = baseUrl + "/api/app/apk?versionCode=" + latestVersionCode;

        VersionInfo info = new VersionInfo();
        info.setLatestVersionCode(latestVersionCode);
        info.setLatestVersionName(latestVersionName);
        info.setApkName(apkName);
        info.setApkUrl(apkUrl);
        info.setForceUpdate(false);
        info.setMessage("최신 버전이 있습니다.");

        return info;
    }

    /**
     * versionCode 기준으로 실제 APK 파일 찾기
     * - 같은 규칙으로 GBS_ERP_*.apk에서 versionCode 계산 후 매칭
     */
    public File resolveApkFileByVersionCode(int versionCode) {
        File dir = new File(apkDir);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalStateException("APK directory not found: " + apkDir);
        }

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            throw new IllegalStateException("No APK files in directory: " + apkDir);
        }

        for (File file : files) {
            if (!file.isFile()) continue;

            String name = file.getName();
            Matcher m = APK_NAME_PATTERN.matcher(name);
            if (!m.matches()) continue;

            int major = Integer.parseInt(m.group(1));
            int minor = Integer.parseInt(m.group(2));
            int patch = Integer.parseInt(m.group(3));

            int vCode = toVersionCode(major, minor, patch);
            if (vCode == versionCode) {
                return file;
            }
        }

        throw new IllegalStateException("APK not found for versionCode=" + versionCode + " in " + apkDir);
    }

    /**
     * 버전코드 계산 규칙
     *  - 예: 1.0.2 -> 10002, 1.1.0 -> 10100
     */
    private int toVersionCode(int major, int minor, int patch) {
    	return Integer.parseInt(major + String.valueOf(minor) + String.valueOf(patch));
    }
}
