/**
 * ========================================================================================
 * [vacationColorConfig.js] 글로벌 SaaS 표준(Google Calendar / Notion / Linear) 베스트 프랙티스
 * ----------------------------------------------------------------------------------------
 * 눈에 전혀 부담을 주지 않는 은은한 파스텔 틴트(Soft Pastel Tint) 배경 + 명확한 다크 텍스트 + 좌측 3px 보더
 * ========================================================================================
 */

var VACATION_TYPE_STYLE_MAP = {
	"PM07TYPE01": { cd: "PM07TYPE01", nm: "연차",         bg: "#fce8e6", text: "#c5221f", border: "#e57373", pattern: "", cls: "card-PM07TYPE01" }, // 연차: 소프트 핑크 틴트 + 다크 크림슨 Red
	"PM07TYPE02": { cd: "PM07TYPE02", nm: "반차",         bg: "#fef3c7", text: "#92400e", border: "#f59e0b", pattern: "", cls: "card-PM07TYPE02" }, // 반차: 소프트 크림 옐로우 + 다크 엠버
	"PM07TYPE03": { cd: "PM07TYPE03", nm: "조퇴",         bg: "#e0f2fe", text: "#0369a1", border: "#38bdf8", pattern: "", cls: "card-PM07TYPE03" }, // 조퇴: 은은한 파스텔 블루 + 다크 세룰리안
	"PM07TYPE04": { cd: "PM07TYPE04", nm: "외출",         bg: "#f3e8ff", text: "#6b21a8", border: "#c084fc", pattern: "", cls: "card-PM07TYPE04" }, // 외출: 소프트 라벤더 틴트 + 다크 퍼플
	"PM07TYPE05": { cd: "PM07TYPE05", nm: "교육훈련",     bg: "#dcfce7", text: "#15803d", border: "#4ade80", pattern: "", cls: "card-PM07TYPE05" }, // 교육훈련: 차분한 민트 틴트 + 다크 포레스트
	"PM07TYPE06": { cd: "PM07TYPE06", nm: "경조휴가",     bg: "#f1f5f9", text: "#334155", border: "#94a3b8", pattern: "", cls: "card-PM07TYPE06" }, // 경조휴가: 소프트 슬레이트 + 다크 차콜
	"PM07TYPE07": { cd: "PM07TYPE07", nm: "포상휴가",     bg: "#ffedd5", text: "#c2410c", border: "#fb923c", pattern: "", cls: "card-PM07TYPE07" }, // 포상휴가: 부드러운 피치 틴트 + 다크 오렌지
	"PM07TYPE08": { cd: "PM07TYPE08", nm: "포상휴가반차", bg: "#fef9c3", text: "#854d0e", border: "#facc15", pattern: "repeating-linear-gradient(135deg, rgba(133,77,14,0.12) 0 4px, transparent 4px 10px)", cls: "card-PM07TYPE08" }, // 포상휴가반차: 머스터드 틴트 + 줄무늬
	"PM07TYPE09": { cd: "PM07TYPE09", nm: "하계휴가",     bg: "#e0f2fe", text: "#0284c7", border: "#38bdf8", pattern: "radial-gradient(rgba(2,132,199,0.25) 22%, transparent 23%) 0 0 / 10px 10px", cls: "card-PM07TYPE09" }, // 하계휴가: 소프트 오션 틴트 + 점무늬
	"PM07TYPE10": { cd: "PM07TYPE10", nm: "재택근무",     bg: "#ccfbf1", text: "#0f766e", border: "#2dd4bf", pattern: "", cls: "card-PM07TYPE10" }, // 재택근무: 은은한 민트 세이지 + 다크 세이지
	"PM07TYPE11": { cd: "PM07TYPE11", nm: "대체휴가",     bg: "#fee2e2", text: "#991b1b", border: "#f87171", pattern: "repeating-linear-gradient(135deg, rgba(153,27,27,0.15) 0 4px, transparent 4px 10px)", cls: "card-PM07TYPE11" }, // 대체휴가: 버건디 틴트 + 줄무늬
	"PM07TYPE12": { cd: "PM07TYPE12", nm: "대체휴가반차", bg: "#ffe4e6", text: "#9f1239", border: "#fb7185", pattern: "", cls: "card-PM07TYPE12" }, // 대체휴가반차: 부드러운 페일 코랄 틴트 + 다크 코랄
	"PM07TYPE13": { cd: "PM07TYPE13", nm: "병가무급",     bg: "#f3f4f6", text: "#374151", border: "#9ca3af", pattern: "", cls: "card-PM07TYPE13" }  // 병가무급: 은은한 쿨그레이 + 다크 그레이
};

/**
 * 휴가 유형 코드 및 휴가명에 해당하는 공통 스타일 정보 반환
 */
function getVacationStyle(vacTypeCd, vacTypeNm) {
	if (vacTypeCd && VACATION_TYPE_STYLE_MAP[vacTypeCd]) {
		return VACATION_TYPE_STYLE_MAP[vacTypeCd];
	}
	var name = vacTypeNm || "";
	if (name.indexOf("포상") >= 0 && name.indexOf("반차") >= 0) return VACATION_TYPE_STYLE_MAP["PM07TYPE08"];
	if (name.indexOf("대체") >= 0 && name.indexOf("반차") >= 0) return VACATION_TYPE_STYLE_MAP["PM07TYPE12"];
	if (name.indexOf("반차") >= 0 || name.indexOf("오전") >= 0 || name.indexOf("오후") >= 0) return VACATION_TYPE_STYLE_MAP["PM07TYPE02"];
	if (name.indexOf("조퇴") >= 0) return VACATION_TYPE_STYLE_MAP["PM07TYPE03"];
	if (name.indexOf("외출") >= 0) return VACATION_TYPE_STYLE_MAP["PM07TYPE04"];
	if (name.indexOf("교육") >= 0 || name.indexOf("훈련") >= 0) return VACATION_TYPE_STYLE_MAP["PM07TYPE05"];
	if (name.indexOf("경조") >= 0) return VACATION_TYPE_STYLE_MAP["PM07TYPE06"];
	if (name.indexOf("포상") >= 0) return VACATION_TYPE_STYLE_MAP["PM07TYPE07"];
	if (name.indexOf("하계") >= 0) return VACATION_TYPE_STYLE_MAP["PM07TYPE09"];
	if (name.indexOf("재택") >= 0) return VACATION_TYPE_STYLE_MAP["PM07TYPE10"];
	if (name.indexOf("대체") >= 0) return VACATION_TYPE_STYLE_MAP["PM07TYPE11"];
	if (name.indexOf("병가") >= 0) return VACATION_TYPE_STYLE_MAP["PM07TYPE13"];
	return VACATION_TYPE_STYLE_MAP["PM07TYPE01"]; // 기본 연차
}
