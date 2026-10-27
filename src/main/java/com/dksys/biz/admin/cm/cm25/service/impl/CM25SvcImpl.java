package com.dksys.biz.admin.cm.cm25.service.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.admin.cm.cm25.mapper.CM25Mapper;
import com.dksys.biz.admin.cm.cm25.service.CM25Svc;
import com.dksys.biz.user.wb.wb20.service.WB20Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class CM25SvcImpl implements CM25Svc {

    private static final Logger LOGGER = LoggerFactory.getLogger(CM25SvcImpl.class);

    @Autowired
    CM25Mapper cm25Mapper;

    @Autowired
    WB20Svc wb20Svc;

    @Autowired
    CM15Svc cm15Svc;

    @Autowired
    CM08Svc cm08Svc;

    @Override
    public int selectExpendListCount(Map<String, String> paramMap) {
        return cm25Mapper.selectExpendListCount(paramMap);
    }

    @Override
    public List<Map<String, Object>> selectExpendList(Map<String, String> paramMap) {
        return cm25Mapper.selectExpendList(paramMap);
    }

    @Override
    public Map<String, String> selectExpendInfo(Map<String, String> paramMap) {
        return cm25Mapper.selectExpendInfo(paramMap);
    }

    @Override
    public List<Map<String, String>> selectUploadFileList(Map<String, String> paramMap) {
        paramMap.put("fileTrgtTyp", "CM2501P01");
        paramMap.put("fileTrgtKey", paramMap.get("expendNo"));
        return cm08Svc.selectFileList(paramMap);
    }

    @Override
    public Map<String, String> selectExpendPdfImageInfo(Map<String, String> paramMap) {
        return cm25Mapper.selectExpendPdfImageInfo(paramMap);
    }

    @Override
    public int insertExpend(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type listType = new TypeToken<ArrayList<Map<String, String>>>() { }.getType();
        List<Map<String, String>> uploadFileList = gson.fromJson(paramMap.get("uploadFileArr"), listType);
        List<Map<String, String>> approvalList = gson.fromJson(paramMap.get("approvalArr"), listType);
        uploadFileList = uploadFileList == null ? new ArrayList<>() : uploadFileList;
        approvalList = approvalList == null ? new ArrayList<>() : approvalList;

        if (!uploadFileList.isEmpty()) {
            paramMap.put("jobType", "fileUp");
            cm15Svc.selectFileAuthCheck(paramMap);
        }

        String expendNo = cm25Mapper.selectExpendNoNext(paramMap);
        paramMap.put("expendNo", expendNo);
        paramMap.put("expendSts", "expendSts01");
        paramMap.put("fileTrgtKey", expendNo);
        paramMap.put("todoNo", expendNo);
        paramMap.put("todoFileTrgtKey", expendNo);
        paramMap.put("reqNo", expendNo);
        paramMap.put("salesCd", expendNo);
        paramMap.put("todoCoCd", paramMap.get("coCd"));
        paramMap.put("pgmId", "CM2501P01");

        int result = cm25Mapper.insertExpend(paramMap);

        if (!uploadFileList.isEmpty()) {
            paramMap.put("fileTrgtTyp", "CM2501P01");
            paramMap.put("fileTrgtKey", expendNo);
            cm08Svc.uploadFile(paramMap, mRequest);
        }

        for (Map<String, String> approval : approvalList) {
            String actionType = "TODODIV10".equals(approval.get("todoDiv1CodeId")) ? "T" : "S";
            approval.put("coCd", paramMap.get("coCd"));
            approval.put("todoCoCd", paramMap.get("coCd"));
            approval.put("todoNo", expendNo);
            approval.put("todoFileTrgtKey", expendNo);
            approval.put("salesCd", expendNo);
            approval.put("pgPath", "admin/cm/cm25/CM2501P01.html");
            approval.put("pgmId", "CM2501P01");
            approval.put("userId", paramMap.get("userId"));
            approval.put("pgParam", "{\"actionType\":\"" + actionType + "\",\"coCd\":\""
                    + paramMap.get("coCd") + "\",\"expendNo\":\"" + expendNo + "\"}");
        }
        paramMap.put("approvalArr", gson.toJson(approvalList));
        wb20Svc.insertTodoMaster(paramMap);
        cm25Mapper.updateExpendSts(paramMap);
        syncPdfPageImages(paramMap);
        return result;
    }

    @Override
    public int updateExpend(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type listType = new TypeToken<ArrayList<Map<String, String>>>() { }.getType();
        List<Map<String, String>> uploadFileList = gson.fromJson(paramMap.get("uploadFileArr"), listType);
        List<Map<String, String>> deleteFileList = gson.fromJson(paramMap.get("deleteFileArr"), listType);
        List<Map<String, String>> approvalList = gson.fromJson(paramMap.get("approvalArr"), listType);
        uploadFileList = uploadFileList == null ? new ArrayList<>() : uploadFileList;
        deleteFileList = deleteFileList == null ? new ArrayList<>() : deleteFileList;
        approvalList = approvalList == null ? new ArrayList<>() : approvalList;

        if (cm25Mapper.selectApprovedCount(paramMap) > 0) {
            throw new Exception("발의부서 결재가 진행된 지출결의서는 수정할 수 없습니다.");
        }

        if (!uploadFileList.isEmpty()) {
            paramMap.put("jobType", "fileUp");
            cm15Svc.selectFileAuthCheck(paramMap);
        }

        String expendNo = paramMap.get("expendNo");
        paramMap.put("fileTrgtKey", expendNo);
        paramMap.put("todoNo", expendNo);
        paramMap.put("todoFileTrgtKey", expendNo);
        paramMap.put("reqNo", expendNo);
        paramMap.put("salesCd", expendNo);
        paramMap.put("todoCoCd", paramMap.get("coCd"));
        paramMap.put("pgmId", "CM2501P01");

        int result = cm25Mapper.updateExpend(paramMap);

        if (!uploadFileList.isEmpty()) {
            paramMap.put("fileTrgtTyp", "CM2501P01");
            paramMap.put("fileTrgtKey", expendNo);
            cm08Svc.uploadFile(paramMap, mRequest);
        }

        if (!deleteFileList.isEmpty()) {
            Map<String, Map<String, String>> attachedFileMap = new HashMap<>();
            paramMap.put("fileTrgtTyp", "CM2501P01");
            paramMap.put("fileTrgtKey", expendNo);
            for (Map<String, String> attachedFile : cm08Svc.selectFileList(paramMap)) {
                attachedFileMap.put(attachedFile.get("fileKey"), attachedFile);
            }

            for (Map<String, String> deleteFile : deleteFileList) {
                String fileKey = deleteFile.get("fileKey");
                Map<String, String> attachedFile = attachedFileMap.get(fileKey);
                if (attachedFile == null) {
                    throw new Exception("해당 지출결의서의 첨부파일이 아닙니다.");
                }
                Map<String, String> authParam = new HashMap<>();
                authParam.put("jobType", "fileDelete");
                authParam.put("coCd", paramMap.get("coCd"));
                authParam.put("userId", paramMap.get("userId"));
                authParam.put("comonCd", attachedFile.get("comonCd"));
                cm15Svc.selectFileAuthCheck(authParam);
                cm08Svc.deleteFile(fileKey);
            }
        }

        cm25Mapper.deleteExpendApprovalList(paramMap);
        for (Map<String, String> approval : approvalList) {
            String actionType = "TODODIV10".equals(approval.get("todoDiv1CodeId")) ? "T" : "S";
            approval.put("coCd", paramMap.get("coCd"));
            approval.put("todoCoCd", paramMap.get("coCd"));
            approval.put("todoNo", expendNo);
            approval.put("todoFileTrgtKey", expendNo);
            approval.put("salesCd", expendNo);
            approval.put("pgPath", "admin/cm/cm25/CM2501P01.html");
            approval.put("pgmId", "CM2501P01");
            approval.put("userId", paramMap.get("userId"));
            approval.put("pgParam", "{\"actionType\":\"" + actionType + "\",\"coCd\":\""
                    + paramMap.get("coCd") + "\",\"expendNo\":\"" + expendNo + "\"}");
        }
        paramMap.put("approvalArr", gson.toJson(approvalList));
        wb20Svc.insertTodoMaster(paramMap);
        cm25Mapper.updateExpendSts(paramMap);
        syncPdfPageImages(paramMap);
        return result;
    }

    @Override
    public int deleteExpend(Map<String, String> paramMap) throws Exception {
        if (cm25Mapper.selectApprovedCount(paramMap) > 0) {
            throw new Exception("발의부서 결재가 진행된 지출결의서는 삭제할 수 없습니다.");
        }

        paramMap.put("fileTrgtTyp", "CM2501P01");
        paramMap.put("fileTrgtKey", paramMap.get("expendNo"));
        List<Map<String, String>> fileList = cm08Svc.selectFileList(paramMap);
        for (Map<String, String> file : fileList) {
            Map<String, String> authParam = new HashMap<>();
            authParam.put("jobType", "fileDelete");
            authParam.put("coCd", paramMap.get("coCd"));
            authParam.put("userId", paramMap.get("userId"));
            authParam.put("comonCd", file.get("comonCd"));
            cm15Svc.selectFileAuthCheck(authParam);
        }

        List<Map<String, String>> pdfImageList = cm25Mapper.selectExpendPdfImageList(paramMap);
        cm25Mapper.deleteExpendApprovalList(paramMap);
        cm25Mapper.deleteExpendPdfImages(paramMap);
        int result = cm25Mapper.deleteExpend(paramMap);
        for (Map<String, String> file : fileList) {
            String fileKey = file.get("fileKey");
            if (fileKey != null && !fileKey.isEmpty()) {
                cm08Svc.deleteFile(fileKey);
            }
        }
        deleteGeneratedImages(pdfImageList);
        return result;
    }

    @Override
    public int updateExpendSts(Map<String, String> paramMap) {
        return cm25Mapper.updateExpendSts(paramMap);
    }

    /**
     * 현재 지출결의서에 남아 있는 PDF 첨부파일을 페이지별 JPEG로 다시 구성한다.
     * 새 이미지 생성이 모두 성공한 후에만 DB 행을 교체하여 변환 실패 시 기존 보고서 이미지를 보존한다.
     */
    private void syncPdfPageImages(Map<String, String> paramMap) throws Exception {
        paramMap.put("fileTrgtTyp", "CM2501P01");
        paramMap.put("fileTrgtKey", paramMap.get("expendNo"));

        List<Map<String, String>> oldImageList = cm25Mapper.selectExpendPdfImageList(paramMap);
        List<Map<String, String>> attachedFileList = cm08Svc.selectFileListAll(paramMap);
        List<Map<String, String>> newImageList = new ArrayList<>();
        List<Path> stagedImagePaths = new ArrayList<>();
        Map<Path, Path> stagedToFinalPaths = new LinkedHashMap<>();
        Set<Path> oldImagePaths = collectGeneratedImagePaths(oldImageList);
        Set<Path> newImagePaths = new HashSet<>();

        try {
            for (Map<String, String> attachedFile : attachedFileList) {
                if (!isPdf(attachedFile)) {
                    continue;
                }
                renderPdfPages(paramMap, attachedFile, newImageList, stagedImagePaths,
                        stagedToFinalPaths, newImagePaths);
            }

            for (Map.Entry<Path, Path> imagePath : stagedToFinalPaths.entrySet()) {
                Files.move(imagePath.getKey(), imagePath.getValue(), StandardCopyOption.REPLACE_EXISTING);
            }

            cm25Mapper.deleteExpendPdfImages(paramMap);
            for (Map<String, String> image : newImageList) {
                cm25Mapper.insertExpendPdfImage(image);
            }
        } catch (Exception e) {
            deletePathsQuietly(stagedImagePaths);
            Set<Path> newlyCreatedPaths = new HashSet<>(newImagePaths);
            newlyCreatedPaths.removeAll(oldImagePaths);
            deletePathsQuietly(new ArrayList<>(newlyCreatedPaths));
            throw e;
        }

        deleteGeneratedImages(oldImageList, newImagePaths);
    }

    private void renderPdfPages(Map<String, String> master, Map<String, String> attachedFile,
            List<Map<String, String>> imageRows, List<Path> stagedImagePaths,
            Map<Path, Path> stagedToFinalPaths, Set<Path> finalImagePaths) throws Exception {
        String fileKey = attachedFile.get("fileKey");
        String fileName = attachedFile.get("fileName");
        String filePath = attachedFile.get("filePath");
        if (isBlank(fileKey) || isBlank(fileName) || isBlank(filePath)) {
            throw new Exception("PDF 첨부파일 정보가 올바르지 않습니다.");
        }

        Path attachmentDir = Paths.get(filePath).toAbsolutePath().normalize();
        Path sourcePdf = attachmentDir.resolve(fileKey + "_" + fileName).normalize();
        if (!sourcePdf.startsWith(attachmentDir) || !Files.isRegularFile(sourcePdf)) {
            throw new Exception("PDF 첨부파일을 찾을 수 없습니다: " + fileName);
        }

        Path imageDir = attachmentDir.resolve("cm25-pdf-images").normalize();
        if (!imageDir.startsWith(attachmentDir)) {
            throw new Exception("PDF 이미지 저장경로가 올바르지 않습니다.");
        }
        Files.createDirectories(imageDir);

        try (PDDocument document = PDDocument.load(sourcePdf.toFile())) {
            PDFRenderer renderer = new PDFRenderer(document);
            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                int pageNo = pageIndex + 1;
                String imageFileName = makePdfImageFileName(fileKey, fileName, pageNo);
                Path imagePath = imageDir.resolve(imageFileName).normalize();
                if (!imagePath.startsWith(imageDir)) {
                    throw new Exception("PDF 이미지 파일경로가 올바르지 않습니다.");
                }
                Path stagedImagePath = Files.createTempFile(imageDir, ".cm25-", ".tmp");
                stagedImagePaths.add(stagedImagePath);

                BufferedImage image = renderer.renderImageWithDPI(pageIndex, 150.0f, ImageType.RGB);
                try {
                    writeJpeg(image, stagedImagePath.toFile());
                } finally {
                    image.flush();
                }
                stagedToFinalPaths.put(stagedImagePath, imagePath);
                finalImagePaths.add(imagePath);

                Map<String, String> imageRow = new HashMap<>();
                imageRow.put("coCd", master.get("coCd"));
                imageRow.put("expendNo", master.get("expendNo"));
                imageRow.put("fileKey", fileKey);
                imageRow.put("fileTrgtKey", attachedFile.get("fileTrgtKey"));
                imageRow.put("pageNo", Integer.toString(pageNo));
                imageRow.put("imageFileName", imageFileName);
                imageRow.put("imageFilePath", imageDir.toString());
                imageRow.put("imageFileSize", Long.toString(Files.size(stagedImagePath)));
                imageRow.put("userId", master.get("userId"));
                imageRow.put("pgmId", "CM2501P01");
                imageRows.add(imageRow);
            }
        } catch (Exception e) {
            throw new Exception("PDF 첨부파일 이미지 변환에 실패했습니다: " + fileName, e);
        }
    }

    private String makePdfImageFileName(String fileKey, String originalFileName, int pageNo) throws Exception {
        int extensionIndex = originalFileName.lastIndexOf('.');
        String baseName = extensionIndex > 0 ? originalFileName.substring(0, extensionIndex) : originalFileName;
        String prefix = fileKey + "_" + String.format("%02d", pageNo) + "_";
        int maxBaseNameLength = 50 - prefix.length() - ".jpg".length();
        if (maxBaseNameLength < 1) {
            throw new Exception("PDF 이미지 파일명을 생성할 수 없습니다: " + originalFileName);
        }
        if (baseName.length() > maxBaseNameLength) {
            baseName = baseName.substring(0, maxBaseNameLength);
        }
        return prefix + baseName + ".jpg";
    }

    private void writeJpeg(BufferedImage image, File target) throws Exception {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new Exception("JPEG 이미지 변환기를 찾을 수 없습니다.");
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream output = ImageIO.createImageOutputStream(target)) {
            writer.setOutput(output);
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            if (writeParam.canWriteCompressed()) {
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionQuality(0.85f);
            }
            writer.write(null, new IIOImage(image, null, null), writeParam);
        } finally {
            writer.dispose();
        }
    }

    private boolean isPdf(Map<String, String> attachedFile) {
        String fileType = attachedFile.get("fileType");
        String fileName = attachedFile.get("fileName");
        return "pdf".equalsIgnoreCase(fileType)
                || (fileName != null && fileName.toLowerCase().endsWith(".pdf"));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void deleteGeneratedImages(List<Map<String, String>> imageList) {
        deleteGeneratedImages(imageList, Collections.<Path>emptySet());
    }

    private void deleteGeneratedImages(List<Map<String, String>> imageList, Set<Path> retainedPaths) {
        for (Map<String, String> image : imageList) {
            String imageFileName = image.get("imageFileName");
            try {
                Path imagePath = getGeneratedImagePath(image);
                if (imagePath != null && !retainedPaths.contains(imagePath)) {
                    Files.deleteIfExists(imagePath);
                }
            } catch (Exception e) {
                LOGGER.warn("지출결의서 PDF 변환 이미지 삭제 실패: {}", imageFileName, e);
            }
        }
    }

    private Set<Path> collectGeneratedImagePaths(List<Map<String, String>> imageList) {
        Set<Path> paths = new HashSet<>();
        for (Map<String, String> image : imageList) {
            Path imagePath = getGeneratedImagePath(image);
            if (imagePath != null) {
                paths.add(imagePath);
            }
        }
        return paths;
    }

    private Path getGeneratedImagePath(Map<String, String> image) {
        String imageFilePath = image.get("imageFilePath");
        String imageFileName = image.get("imageFileName");
        if (isBlank(imageFilePath) || isBlank(imageFileName)) {
            return null;
        }
        Path imageDir = Paths.get(imageFilePath).toAbsolutePath().normalize();
        Path imagePath = imageDir.resolve(imageFileName).normalize();
        return imagePath.startsWith(imageDir) ? imagePath : null;
    }

    private void deletePathsQuietly(List<Path> paths) {
        for (Path path : paths) {
            try {
                Files.deleteIfExists(path);
            } catch (Exception e) {
                LOGGER.warn("지출결의서 PDF 임시 이미지 삭제 실패: {}", path, e);
            }
        }
    }

}
