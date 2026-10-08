/**
 * ========================================================================================
 * [vacationRules.js] PM07 휴가신청 유형 판정 공용 함수
 * ----------------------------------------------------------------------------------------
 * PC(PM0701P01.html) / 모바일(PM0701P01_M.html) 화면이 각각 복붙해 쓰던 판정 로직을 공용화함 (2026-10).
 * DOM 을 직접 읽지 않고 (휴가유형 코드, 휴가유형명) 인자만으로 판정한다.
 * 이 로직을 각 화면에 다시 복붙하지 말 것.
 * ========================================================================================
 */

var VACATION_HALF_DAY_TYPE_CDS = ["PM07TYPE02", "PM07TYPE08", "PM07TYPE12"];

/**
 * 반차류(반차, 포상휴가반차, 대체휴가반차) 여부
 */
function isHalfDayVacationType(vacTypeCd, vacTypeNm) {
	return VACATION_HALF_DAY_TYPE_CDS.indexOf(vacTypeCd || "") >= 0
		|| (!!vacTypeNm && vacTypeNm.indexOf("반차") >= 0);
}

/**
 * From-Till 기간선택 가능 유형 여부 (연차, 교육훈련, 경조휴가, 포상휴가, 하계휴가, 대체휴가, 병가)
 * 유형명이 비어 있으면 true (유형 미선택 상태는 기간선택 허용)
 */
function isMultiDayVacationType(vacTypeCd, vacTypeNm) {
	if (!vacTypeNm) return true;
	if (vacTypeCd === "PM07TYPE14") return false;
	var nm = $.trim(vacTypeNm);
	if (nm.indexOf("반차") >= 0 || nm.indexOf("조퇴") >= 0 || nm.indexOf("외출") >= 0 || nm.indexOf("지각") >= 0) {
		return false;
	}
	return nm.indexOf("연차") >= 0
		|| nm.indexOf("교육") >= 0
		|| nm.indexOf("훈련") >= 0
		|| nm.indexOf("경조") >= 0
		|| nm.indexOf("포상") >= 0
		|| nm.indexOf("하계") >= 0
		|| nm.indexOf("대체") >= 0
		|| nm.indexOf("병가") >= 0;
}

/**
 * 사유 필수입력 제외 유형 여부 (연차, 반차, 조퇴, 외출, 하계휴가, 대체휴가, 병가, 지각)
 */
function isVacationRmkOptional(vacTypeCd, vacTypeNm) {
	if (!vacTypeNm) return false;
	if (vacTypeCd === "PM07TYPE14") return true;
	var nm = $.trim(vacTypeNm);
	return nm.indexOf("연차") >= 0
		|| nm.indexOf("반차") >= 0
		|| nm.indexOf("조퇴") >= 0
		|| nm.indexOf("외출") >= 0
		|| nm.indexOf("하계") >= 0
		|| nm.indexOf("대체") >= 0
		|| nm.indexOf("병가") >= 0
		|| nm.indexOf("지각") >= 0;
}

/**
 * 차감일수 - 화면 표시 전용.
 * 최종 차감일수는 서버(PM07SvcImpl.evaluateVacationAndDeductDays)가 저장 직전에 재산정하며
 * 프론트에서 보낸 deductDays 값은 사용하지 않는다. 서버에 없는 판정을 여기에 추가하지 말 것.
 */
function getVacationDeductDaysForDisplay(vacTypeCd, vacTypeNm, vacDays) {
	if (!vacTypeNm) return 0;
	var isNonDeduct = vacTypeNm.indexOf("교육") >= 0 || vacTypeNm.indexOf("훈련") >= 0 || vacTypeNm.indexOf("병가") >= 0
			|| vacTypeNm.indexOf("조퇴") >= 0 || vacTypeNm.indexOf("외출") >= 0 || vacTypeNm.indexOf("재택") >= 0 || vacTypeNm.indexOf("지각") >= 0;

	if (isHalfDayVacationType(vacTypeCd, vacTypeNm)) {
		return 0.5;
	} else if (isNonDeduct) {
		return 0;
	}
	return vacDays || 0;
}

/**
 * 저장 전송용 휴가유형명(vacTypeNm) 조립.
 * 코드명의 첫 "(" 이후 부연설명을 제거하고, 반차류는 " (오전)" / " (오후)" 를 붙인다.
 * ampmText 는 선택된 구분 option 의 표시 텍스트.
 */
function buildVacationTypeNm(vacTypeCd, rawVacTypeNm, ampmCd, ampmText) {
	var cleanVacNm = $.trim(String(rawVacTypeNm || "").replace(/\s*\(.*$/, ""));
	var isHalf = (cleanVacNm.indexOf("반차") !== -1) || VACATION_HALF_DAY_TYPE_CDS.indexOf(vacTypeCd || "") >= 0;
	if (isHalf && ampmCd && ampmText && ampmText !== "선택하세요") {
		var ampmLabel = (ampmText.indexOf("오전") !== -1 || ampmCd === "PM07AMPM01" || ampmCd === "AM") ? "오전" : "오후";
		cleanVacNm += " (" + ampmLabel + ")";
	}
	return cleanVacNm;
}

/**
 * 기본 결재선의 SPECRTS17(근태결재라인) 결재자 결재구분.
 * cyh 는 협조(COOP), 그 외는 결재(APPR).
 */
function getVacationSpecrtsLineType(userId) {
	return (String(userId || "").toLowerCase() === "cyh") ? "COOP" : "APPR";
}
