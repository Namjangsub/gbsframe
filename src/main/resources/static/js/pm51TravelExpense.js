/*
 * 출장 여비(출장자 1인) 공용 요율·계산식 (출장여비규정 GIM-050-02)
 * 사용 화면: PM5101P01(출장신청서), PM5102P01(출장복명서)
 * 요율 변경 시 이 파일만 수정한다.
 * 모달이 여러 번 열려 재로드되어도 안전하도록 window 속성으로만 정의한다(최상위 const/let 금지).
 */
window.PM51_RATE = window.PM51_RATE || {
	domestic: {
		lodgingWeekday: 40000,
		lodgingWeekend: 50000,
		mealUnit: 12000,
		special: 10000
	},
	overseas: {
		lodging: { A: 120, B: 110, C: 100 },
		daily: [50, 40, 30],
		special: 15
	}
};

window.pm51RoundFx = window.pm51RoundFx || function(value) {
	var n = Number(value || 0);
	if (isNaN(n)) return 0;
	return Math.round(n * 100) / 100;
};

/*
 * 출장신청서 자동재계산(recalculatePaymentByTravelerGrid)의 출장자 1행 산식
 * row : { tripStDt('YYYY-MM-DD'|'YYYYMMDD'), nightCnt, eatCnt }
 * opts: { isDomestic: boolean, areaDiv: 'A'|'B'|'C'|'' }
 * 반환: { lodgingKrw, lodgingUsd, mealKrw, mealUsd, dailyKrw, dailyUsd }
 */
window.pm51CalcTravelerAutoExpense = window.pm51CalcTravelerAutoExpense || function(row, opts) {
	var rate = window.PM51_RATE;
	row = row || {};
	opts = opts || {};
	var nightCnt = Number(row.nightCnt || 0);
	var eatCnt = Number(row.eatCnt || 0);
	var result = { lodgingKrw: 0, lodgingUsd: 0, mealKrw: 0, mealUsd: 0, dailyKrw: 0, dailyUsd: 0 };

	if (opts.isDomestic) {
		var stDateStr = String(row.tripStDt || row.tripStDtm || '').replace(/[^0-9]/g, '').substring(0, 8);
		var start = (stDateStr.length === 8) ? moment(stDateStr, 'YYYYMMDD') : null;
		for (var night = 0; night < nightCnt; night++) {
			var nightDate = (start && start.isValid()) ? start.clone().add(night, 'days') : null;
			var isWeekend = nightDate && (nightDate.day() === 0 || nightDate.day() === 6);
			result.lodgingKrw += isWeekend ? rate.domestic.lodgingWeekend : rate.domestic.lodgingWeekday;
		}
		result.mealKrw = eatCnt * rate.domestic.mealUnit;
		result.dailyKrw = nightCnt * rate.domestic.special;
	} else {
		var areaDiv = opts.areaDiv;
		result.lodgingUsd = window.pm51RoundFx(nightCnt * (rate.overseas.lodging[areaDiv] || 0));
		result.mealUsd = window.pm51RoundFx(nightCnt * (rate.overseas.daily[{ A: 0, B: 1, C: 2 }[areaDiv]] || 0));
		result.dailyKrw = nightCnt * 15000;
	}
	return result;
};

/*
 * 출장복명서 일비 최종확정(calcActualDailyLodgingMeal)의 1인 산식
 * 회계 박수(acctNightCnt) 덮어쓰기는 호출측에서 row.nightCnt 에 반영해 넘긴다.
 * row : { nightCnt, eatCnt }
 * opts: { isDomestic: boolean, areaDiv: 'A'|'B'|'C'|'' }
 * 반환: { krw, usd }
 */
window.pm51CalcTravelerDailyFinal = window.pm51CalcTravelerDailyFinal || function(row, opts) {
	var rate = window.PM51_RATE;
	row = row || {};
	opts = opts || {};
	var nightCnt = Number(row.nightCnt || 0);
	var eatCnt = Number(row.eatCnt || 0);
	var mealKrw = 0, mealUsd = 0;
	if (opts.isDomestic) {
		mealKrw = eatCnt * rate.domestic.mealUnit;
	} else if (['A', 'B', 'C'].indexOf(opts.areaDiv) > -1) {
		mealUsd = nightCnt * rate.overseas.daily[{ A: 0, B: 1, C: 2 }[opts.areaDiv]];
	}
	var miscKrw = (isNaN(nightCnt) ? 0 : nightCnt) * (opts.isDomestic ? 10000 : 15000);
	return {
		krw: mealKrw + miscKrw,
		usd: window.pm51RoundFx(mealUsd)
	};
};

/*
 * 프로젝트별 출장조건 공용 옵션·인라인 셀렉트 렌더링 (PM5101P01 출장신청서 / PM5102P01 출장복명서 공용)
 */
window.PM51_TRIP_COND_OPTIONS = window.PM51_TRIP_COND_OPTIONS || [
	{ CD: '', NM: '선택' },
	{ CD: 'TRIPCOND01', NM: '유상' },
	{ CD: 'TRIPCOND02', NM: '무상' },
	{ CD: 'TRIPCOND99', NM: '기타' }
];

window.pm51GetTripCondLabel = window.pm51GetTripCondLabel || function(code) {
	var cd = String(code || '').trim();
	var opts = window.PM51_TRIP_COND_OPTIONS || [];
	for (var i = 0; i < opts.length; i++) {
		if (opts[i].CD === cd) {
			return cd ? opts[i].NM : '';
		}
	}
	return cd;
};

window.pm51BuildTripCondSelectHtml = window.pm51BuildTripCondSelectHtml || function(dindex, curVal, disabled, onchangeFnName) {
	var val = String(curVal || '').trim();
	var disabledAttr = disabled ? ' disabled' : '';
	var handler = onchangeFnName || 'onProjectTripCondChange';
	var opts = window.PM51_TRIP_COND_OPTIONS || [];
	var html = '<select class="project-trip-cond-sel" data-dindex="' + dindex + '"' + disabledAttr
		+ ' onclick="event.stopPropagation();" onmousedown="event.stopPropagation();" onchange="' + handler + '(this);">';
	for (var i = 0; i < opts.length; i++) {
		var selectedAttr = (opts[i].CD === val) ? ' selected' : '';
		html += '<option value="' + opts[i].CD + '"' + selectedAttr + '>' + opts[i].NM + '</option>';
	}
	html += '</select>';
	return html;
};
