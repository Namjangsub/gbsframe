package com.dksys.biz.integration.pdm;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.bm.bm05.mapper.BM05Mapper;
import com.dksys.biz.user.bm.bm14.mapper.BM14Mapper;

@RestController
public class PdmLookupCtr {

    public static final String MATERIAL_SEARCH_PATH = "/api/pdm/materials/search";
    public static final String PURCHASE_BOM_STATUS_PATH = "/api/pdm/purchase-bom/status";

    private static final Logger logger = LoggerFactory.getLogger(PdmLookupCtr.class);
    private final BM05Mapper bm05Mapper;
    private final BM14Mapper bm14Mapper;
    public PdmLookupCtr(BM05Mapper bm05Mapper, BM14Mapper bm14Mapper) {
        this.bm05Mapper = bm05Mapper;
        this.bm14Mapper = bm14Mapper;
    }

    @PostMapping(path = MATERIAL_SEARCH_PATH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchMaterials(@RequestBody Map<String, String> request) {
        String searchValue = text(request, "searchValue");
        if (searchValue.length() > 200 || searchValue.replaceAll("[\\s%_\\-]", "").isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "검색어(searchValue)를 200자 이내로 입력해 주세요.");
        }

        int pageNo;
        int recordCnt;
        try {
            pageNo = pageParam(request, "pageNo", 1);
            recordCnt = pageParam(request, "recordCnt", 100);
        } catch (NumberFormatException exception) {
            return error(HttpStatus.BAD_REQUEST, "페이지 번호와 조회 건수는 정수로 입력해 주세요.");
        }
        if (pageNo < 1 || recordCnt < 1 || recordCnt > 100 || (long) pageNo * recordCnt > Integer.MAX_VALUE) {
            return error(HttpStatus.BAD_REQUEST, "페이지 번호 또는 조회 건수가 허용 범위를 벗어났습니다.");
        }

        Map<String, String> params = new HashMap<>();
        params.put("searchValue", searchValue);
        params.put("matrClntDiv", text(request, "matrClntDiv"));
        params.put("pageNo", String.valueOf(pageNo));
        params.put("recordCnt", String.valueOf(recordCnt));

        PaginationInfo paginationInfo = new PaginationInfo(params, bm05Mapper.MatModal_selectCount(params));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("MatModalList", bm05Mapper.MatModal_selectList(params));
        result.put("paginationInfo", paginationInfo);
        return ResponseEntity.ok(result);
    }

    @PostMapping(path = PURCHASE_BOM_STATUS_PATH, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPurchaseBomStatus(@RequestBody Map<String, String> request) {
        String coCd = text(request, "coCd");
        String salesCd = text(request, "salesCd");
        String dsgnNo = text(request, "dsgnNo");
        if (coCd.isEmpty() || salesCd.isEmpty() || salesCd.length() > 200 || dsgnNo.isEmpty() || dsgnNo.length() > 200) {
            return error(HttpStatus.BAD_REQUEST, "회사코드(coCd), Sales Code(salesCd), 도면번호(dsgnNo)를 확인해 주세요.");
        }
        Map<String, String> params = new HashMap<>();
        params.put("coCd", coCd);
        params.put("salesCd", salesCd);
        params.put("dsgnNo", dsgnNo);

        List<String> lowerKeys = bm14Mapper.selectPdmDesignBomKeys(params);
        if (lowerKeys.size() > 1) {
            return error(HttpStatus.CONFLICT, "일치하는 설계 BOM 항목이 여러 건입니다.");
        }
        boolean designBomExists = !lowerKeys.isEmpty();
        boolean purchaseBomRegistered = false;
        if (designBomExists) {
            params.put("lowerKey", lowerKeys.get(0));
            purchaseBomRegistered = bm14Mapper.selectPchsBomCheck(params) > 0;
        }
        Map<String, Boolean> result = new LinkedHashMap<>();
        result.put("designBomExists", designBomExists);
        result.put("purchaseBomRegistered", purchaseBomRegistered);
        return ResponseEntity.ok(result);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> invalidJson() {
        return error(HttpStatus.BAD_REQUEST, "JSON 요청 본문을 확인해 주세요.");
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> lookupFailure(DataAccessException exception) {
        logger.error("PDM 조회 중 DB 오류", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "ERP 조회 중 오류가 발생했습니다.");
    }

    private static String text(Map<String, String> request, String key) {
        String value = request.get(key);
        return value == null ? "" : value.trim();
    }

    private static int pageParam(Map<String, String> request, String key, int defaultValue) {
        String value = text(request, key);
        return value.isEmpty() ? defaultValue : Integer.parseInt(value);
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Collections.singletonMap("error", message));
    }
}
