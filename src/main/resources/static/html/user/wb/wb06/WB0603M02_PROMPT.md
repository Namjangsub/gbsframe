# WB0603M02.html Full-File Generation Prompt

You are generating a full HTML file named `WB0603M02.html` for a Gantt-style WBS screen.
Output a complete HTML document (with `<head>`, `<style>`, `<body>`, and `<script>`) and do not omit any required sections.
Keep code readable and performant: deduplicate logic with helpers, use clear function names, and avoid repeated DOM queries.

Goal
- Render a WBS Gantt view by machine, grouped by salesCd, with sticky left and right panes, interactive bars, summary rows, save/export/print, and a modal to show changes.

Dependencies (include as `<link>`/`<script>`)
- Bootstrap CSS/JS + datepicker
- jQuery + jQuery UI
- ax5 (grid/mask/modal/toast/menu/calendar/picker/select/combobox)
- ExcelJS (`exceljs.min.js`)
- html2canvas (cdn)
- `global.js`, `fileTree.js`, `workingDayCalc.js`, `korean-lunar-calendar.min.js`, `jquery.blockUI.js`
- Standard site css: `gnb.css`, `main.css`, `sub.css`, `common.css`
- Use `"Noto Sans KR", Arial, "dotum", sans-serif` for body and `.table_input.type04`

Layout
- Topbar with:
  - Title (“실행계획 > 기계별 WBS현황”)
  - Buttons: SEARCH, RESET, SAVE CHANGES, EXPORT EXCEL, PRINT, HELP
  - Search fields with `data-search` attributes (coCd, salesCd, date range, etc.)
- Main page area:
  - `.page > .wbs-card > .wbs-scroll > #wbs` container
  - Left sticky columns: 기계, 설비명, salesCd
  - Right sticky column: 비고 (Remarks)
  - Timeline center: month header + day header (workdays only), grid, bars
- Custom modal with backdrop (`#modal`) for showing JSON changes; center it and use high z-index; buttons centered (복사/확인/닫기).

Data & State
- Use constants: `VIEW_START`, `VIEW_END`, `CELL_W` (px per day)
- `STATE` object: `viewStart`, `viewEnd`, `today`, `rows` (data from backend)
- Use DEPTS list:
  - S01 영업 (Blue `#0072B2`)
  - S02 연구소 (Purple `#CC79A7`)
  - S03 구매 (Orange `#E69F00`)
  - S04 생산 (Green `#009E73`)
  - S50 S.PM (Sky `#56B4E9`)
- Remove combo dept classes (S12/S23/S34/S41) and map combined names to base depts.
- Machine label: show customer + machine in two lines: `clntNm<br>clntPjtNm` in `.c-clntPjtNm`.

Key Helpers (must exist)
- `parseYMD`, `ymd`, `timestampToYmd`, `daysBetween` (business days only)
- `buildSalesCdGroups(rows)`: group by salesCd, keep order
- `buildSummarySegmentsFromRows(rows)`: merge intervals by dept; accept numeric timestamps
- `buildRenderRows(rows)`: return `{renderRows, spans}` with per-machine summary row (“전체집계”)
- `buildMergeSpans(renderRows)`: for cell merge of same machine
- `updateSummaryRowForMachine(machine)`: refresh summary row bars on change
- `formatMachineLabel(clntNm, clntPjtNm)`
- `getDeptShortLabel(deptNm)`: 영/연/구/생/스

Rendering Requirements
- One data row per salesCd containing all bars for that salesCd.
- Summary row “전체집계” after each machine’s group; grey background for left, timeline-canvas, and remarks.
- Show TODAY vertical line + “TODAY” badge in header if within range.
- Merge machine cell (`.c-clntPjtNm`) across same machine rows (visual merge).
- Remarks column is sticky right; editable on click (textarea).
- Bars:
  - Default opacity ~0.6; on hover opacity 1 and z-index up.
  - Draggable left/right; resizable by left/right handles (day unit).
  - Double-click bar opens:
    - `openSecondModal("/static/html/user/wb/wb22/WB2201P01.html", 1200, 900, "WBS 계획등록", { coCd: $('#coCd_S').val(), salesCd, histYn: "N" }, () => searchData());`
  - Summary-row bars are not interactive and do not open modal.
- When a bar changes, update row dates and mark as changed; also refresh summary row for that machine.

Save Changes
- Maintain `CHANGED_ROWS` map.
- SAVE CHANGES opens modal with JSON array of changed rows.
- If no changes, show toast “변경된 내용이 없습니다.”

Search & Data Loading
- `searchData()`: collect inputs with `[data-search]`, call `postAjax("/user/wb/wb26/select_wb06_List", formData, ...)`
- Normalize `deptNm`/`deptCd` (alias map), ensure dates in `yyyy-mm-dd`
- Update `STATE.rows` and `renderWbs()`

Excel Export (ExcelJS)
- Row 1: title from `#topSubMenu`
- Row 2: legend image (colored chips) + search condition text (legend as image only; no legend text)
- Row 3-4: header (month row + day row), include “비고” column on right
- Data rows start at row 5, matching on-screen order (include summary rows)
- Gantt cells filled by dept color; put short label in each cell, overlap with “/”
- Gantt cell font size = 6
- Summary rows: grey fill across all columns

Print
- Use html2canvas to capture `.page`
- Temporarily adjust `CELL_W` to fit page width if needed, then restore

UX details
- `.c-eqpNm`, `.c-salesCd` font-weight normal
- `.c-clntPjtNm` uses bold and two-line label
- `.timeline-day` font size scales with zoom (min 2px, max 14px)
- Right remarks border visible
- Sticky left and right panes stay fixed

Output
- Provide the full HTML file only.
- Keep functions grouped with clear section comments.
- Avoid duplicate helper implementations: render and export must share helpers.



IDE에서 만든 프롬프트

당신은 Spring Boot 정적 리소스에서 동작하는 HTML5 + jQuery 기반의 WBS 타임라인 페이지를 구현하는 프론트엔드 개발자다.
대상 화면: “실행계획 > 기계별 WBS현황 (HTML5 + jQuery)”.
기술 스택: jQuery, jQuery UI(draggable/resizable), Bootstrap, ExcelJS, html2canvas, moment, datepicker, ax5 유틸, 기존 global.js 함수(postAjax, openSecondModal, inputValidation, mainDefaultLoad, setCommonSelect, authChk 등)을 그대로 사용.

목표
- 기계(클라이언트 PJT)별 WBS 현황을 가로 타임라인으로 표시
- 평일(월~금)만 타임라인에 표시(주말 제외)
- 바(부서별 일정)를 드래그/리사이즈로 변경하고 변경 사항을 수집
- 검색 조건 기반으로 백엔드에서 데이터를 조회하고 화면을 재렌더링
- 엑셀 내보내기와 인쇄(스크린샷) 기능 제공

UI/레이아웃 요구사항
1) 상단 검색 영역(기존 table_input type04 스타일 유지)
- 회사(coCd_S), 수주일자(strtDt_S~endDt_S), 고객사PJT(clntPjt_S), 수주구분(ordrsDiv_S),
  SalesCode(salesCd_S), 고객사(ordrsClntNm_S/ordrsClntCd_S), PM계획상태(scdStatus_S)
- datepicker 적용, 검색조건 변경 시 자동 조회
- 상단 버튼: 도움말, 초기화, 검색
2) 상단 레전드 + 줌 슬라이더 + 툴바
- 부서 레전드 칩(색상 매핑)
- 줌 슬라이더(4~30 px/day), 실시간 리렌더
- “SAVE CHANGES”, “EXPORT EXCEL”, “PRINT” 버튼
3) WBS 본문
- 좌측 고정 컬럼(기계/설비명/salesCd), 중앙 타임라인, 우측 고정 Remarks
- 헤더 row는 sticky
- rows 구성:
  - salesCd 그룹 row: 동일 salesCd의 여러 부서 바를 한 줄에 렌더
  - summary row: 동일 clntPjtNm(기계) 그룹별 전체 집계 바
  - total row: 전체 진행 요약(segments 기반)
- 동일 clntPjtNm 연속 row는 좌측 “기계” 셀을 세로 병합처럼 표시
- TODAY 라인(붉은 점선) + 헤더 배지

데이터 구조/로직
- STATE.rows는 백엔드 리스트를 변환한 배열
- 각 row는 기본 필드( coCd, salesCd, deptCd, deptNm, wbsPlansDtFm, wbsPlaneDtFm, clntPjtNm, eqpNm, remarks )
- 같은 salesCd끼리 묶어서 renderRows 생성, 각 row에는 rows[] 배열 포함
- summary row는 같은 clntPjtNm 기준으로 부서별 기간을 병합하여 생성
- daysBetween/addDays는 “영업일(월~금) 기준”으로 계산
- 타임라인 헤더는 월/일을 평일만 표시하며, width = 평일수 * CELL_W
- VIEW_START/VIEW_END는 데이터 최소~최대 날짜에서 ±7일 여유로 계산

부서 색상 매핑
- S01: 영업(#0072B2), S02: 연구소(#CC79A7), S03: 구매(#E69F00),
  S04: 생산(#009E73), S50: S.PM(#56B4E9)
- combo dept(S12/S23/S34/S41)는 각각 S01/S02/S03/S04로 매핑
- deptNm이 “영/연/구/생”이면 정식명으로 치환

상호작용
- 바 드래그: 좌우 이동(1일 단위), 시작일 변경
- 바 리사이즈: 기간 변경(1일 단위)
- 변경된 row는 row-dirty로 표시 + CHANGED_ROWS Map에 수집
- Remarks 셀 클릭 시 inline 편집(Enter 저장, Esc 취소, blur 저장)
- 바 더블클릭 시 WBS 계획등록 모달 오픈
  - openSecondModal("/static/html/user/wb/wb22/WB2201P01.html", …)

저장/엑셀/인쇄
- SAVE CHANGES: 변경 내역을 JSON으로 모달에 표시(백엔드 전송은 이후)
- EXPORT EXCEL:
  - ExcelJS 사용, 평일 컬럼만 생성
  - 상단 2행에 레전드 이미지 + 검색조건 메타 텍스트 삽입
  - 부서별 색상 셀 채움
- PRINT:
  - html2canvas로 .page 영역 캡처
  - 타임라인 전체가 보이도록 CELL_W를 임시 조정 후 캡처, 캡처 후 원복

백엔드 연동
- 조회 API: POST `/user/wb/wb26/select_wb06_List`
  - 요청 파라미터: data-search 속성 기준으로 수집
  - input_calendar는 날짜에서 ‘-’ 제거 후 전송
- 리스트 응답은 각 row에 대해 deptNm/deptCd 매핑 보정 후 STATE.rows에 반영

필수 함수/도구 사용
- mainDefaultLoad, setCommonSelect, authChk, postAjax, openSecondModal, inputValidation
- jQuery UI draggable/resizable
- moment, datepicker

출력물
- 단일 HTML 파일(WB0603M02.html)로 구현
- 기존 스타일/레이아웃을 유지하면서 기능 완전 구현
- 코드에는 필요한 최소 주석만 추가
