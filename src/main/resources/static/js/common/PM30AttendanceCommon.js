// ─────────────────────────────────────────────────────────────────────────────────────
// PM30 근태변환 공용 함수 라이브러리 (PM3001M01.html, PM3002M01.html 공용)
// ─────────────────────────────────────────────────────────────────────────────────────

// ─ 백엔드가 통합 UNION ALL로 내려주는 data.dailyApplicationList(SRC_TYPE 구분)를
// matchRowApplicationLinkage()/processRelatedApplicationsData()가 기존에 기대하던 배열 모양으로 변환한다. ─
function splitDailyApplicationList(data) {
	if (!data) return data;
	var list = data.dailyApplicationList || [];
	var tripList = [], reportTripList = [], vacationList = [], substWorkList = [];
	for (var i = 0; i < list.length; i++) {
		var r = list[i];
		if (r.srcType === 'TRIP_RPT') {
			reportTripList.push({
				empNo: r.empNo, tripReqNo: r.applNo, tripRptNo: r.rptNo,
				stDt: r.stDt, edDt: r.edDt, tripStDtm: r.stDt, tripEdDtm: r.edDt,
				tripPlace: r.tripPlace, equipNm: r.equipNm, clntPjt: r.clntPjt, clntPjtNm: r.clntPjtNm,
				salesCd: r.salesCd, ordrsNo: r.ordrsNo, tripDiv: r.tripDiv, tripDivNm: r.tripDivNm,
				tripTypeCds: r.tripTypeCds, tripTypeNm: r.tripTypeNm, pureTripTypeNm: r.tripTypeNm
			});
		} else if (r.srcType === 'TRIP_REQ') {
			tripList.push({
				empNo: r.empNo, tripReqNo: r.applNo,
				stDt: r.stDt, edDt: r.edDt, tripStDtm: r.stDt, tripEdDtm: r.edDt,
				tripPlace: r.tripPlace, equipNm: r.equipNm, clntPjt: r.clntPjt, clntPjtNm: r.clntPjtNm,
				salesCd: r.salesCd, ordrsNo: r.ordrsNo, tripDiv: r.tripDiv, tripDivNm: r.tripDivNm,
				tripTypeCds: r.tripTypeCds, tripTypeNm: r.tripTypeNm, pureTripTypeNm: r.tripTypeNm
			});
		} else if (r.srcType === 'VAC') {
			vacationList.push({
				empNo: r.empNo, vacDt: r.stDt, reqNo: r.applNo,
				vacTypeCd: r.vacTypeCd, vacTypeNm: r.vacTypeNm, ampmCd: r.ampmCd, workHour: r.workHour,
				sanctnSts: r.sts
			});
		} else if (r.srcType === 'SUBSTWORK') {
			substWorkList.push({ empNo: r.empNo, holidayDt: r.stDt, reqNo: r.applNo });
		}
	}
	data.tripList = tripList;
	data.reportTripList = reportTripList;
	data.vacationList = vacationList;
	data.substWorkList = substWorkList;
	return data;
}

// ─ "HH:mm" 시간 문자열 정규화 (예: "7:44" -> "07:44") ─
function normalizeHHmm(timeStr) {
	if (!timeStr) return '';
	var s = String(timeStr).trim();
	if (s.indexOf(' ') !== -1) {
		s = s.split(' ')[1] || s;
	}
	var parts = s.split(':');
	if (parts.length >= 2) {
		var h = parseInt(parts[0], 10);
		var m = parseInt(parts[1], 10);
		if (!isNaN(h) && !isNaN(m)) {
			return (h < 10 ? '0' : '') + h + ':' + (m < 10 ? '0' : '') + m;
		}
	}
	return s;
}

// ─ "HH:mm" 시간 문자열 -> 분(Minutes) 숫자 반환 (예: "00:30" -> 30, "01:15" -> 75) ─
function parseHHmmToMinutes(timeStr) {
	if (!timeStr) return 0;
	var s = String(timeStr).trim();
	if (s === '0' || s === '00:00') return 0;
	var parts = s.split(':');
	if (parts.length < 2) return 0;
	var h = parseInt(parts[0], 10) || 0;
	var m = parseInt(parts[1], 10) || 0;
	return h * 60 + m;
}

// ─ 분(Minutes) 숫자 -> "HH:mm" 시간 문자열 변환 (예: 90 -> "01:30") ─
function fmtMinutesToHHmm(mins) {
	mins = Math.max(0, Math.round(mins));
	var h = Math.floor(mins / 60);
	var m = mins % 60;
	return (h < 10 ? '0' : '') + h + ':' + (m < 10 ? '0' : '') + m;
}

// ─ 비고 괄호 텍스트 제거 공통 정제 ─
// PM3002M01.html과 PM3001M01.html에서 공용으로 사용하는 함수
function cleanRemarkText(txt) {
	txt = String(txt || '').trim();
	return txt.replace(/\s*\(.*?\)/g, '');
}

// ─ 단일 행의 전역 비고(rmk) 필드 조합 및 동기화 ─
function updateRowRmk(row) {
	if (!row) return;

	var rmkParts = [];

	// 1. 출장/휴가 연동 유형명 (수주번호 ordrsNo가 있으면 "수주번호-유형명")
	if (row.tripApplNo || row.tripReplyNo) {
		var tripTypeStr = row.workTypeNm || '기타출장';
		if (row.ordrsNo) {
			tripTypeStr = row.ordrsNo + '-' + tripTypeStr;
		}
		rmkParts.push(tripTypeStr);
	} else if (row.vacApplNo) {
		rmkParts.push(row.workTypeNm || '');
	}

	// 2. 지각/조퇴 시각 정보 (recalcRowAttendanceObject에서 판정된 lateEarlyRmk)
	if (row.lateEarlyRmk) {
		rmkParts.push(row.lateEarlyRmk);
	}

	// 3. 대체근무
	if (String(row.substWorkYn || '').trim() === '1') {
		rmkParts.push('대체근무');
	}

	// 4. 지각/조퇴 자동등록 수동버튼 메시지 보존
	if (row._autoRegRmk) {
		rmkParts.push(row._autoRegRmk);
	}

	row.rmk = rmkParts.join(' / ');
}

// ─ 근무형태 기반 작업 카테고리 분류 (PM3001M01, PM3002M01 공용) ─
// 반환값: 'inhouse'|'install'|'fault'|'asFree'|'etcTrip'|null
// null: 휴가/휴일/제외 등 시간 계산 대상이 아닌 날짜
function classifyWorkCategory(row) {
	if (!row) return null;
	var wt = String(row.workTypeNm || '').trim();
	if (wt === '제외' || wt === '휴가' || wt === '휴일' || wt === '정보없음') return null;
	if (wt === '사내1' || wt === '사내2' || wt === '사내3') return 'inhouse';
	if (wt === '설치시운전') return 'install';
	if (wt === '설치장애') return 'fault';
	if (wt === 'A/S(무상)') return 'asFree';
	if (wt === '기타출장') return 'etcTrip';

	var tc = String(row.tripTypeCds || '').trim();
	if (tc === 'PM51TYPE30') return 'install';
	if (tc === 'PM51TYPE40') return 'fault';
	if (tc === 'PM51TYPE60') return 'asFree';
	if (tc || row.tripApplNo || row.tripReplyNo) return 'etcTrip';
	return null;
}

// ─ 단일 행 근태 실적 정보 재계산 (수정일시 inDttm/outDttm 및 출장 08:30/17:30 기준 근태실적정보 일괄 재계산) ─
function recalcRowAttendanceObject(row) {
	if (!row) return row;

	var isTrip = !!(row.tripWorkPlace || row.tripApplNo || row.tripReplyNo);
	// 휴일대체근무 신청서가 연동된 행도 휴일 판정 분기를 탄다.
	var isHoliday = (String(row.holidayYn || '').trim() === '휴일') || (String(row.substWorkYn || '').trim() === '1') || !!(row.workApplNo && String(row.workApplNo).trim() !== '');
	var wd = row.workDt ? String(row.workDt).replace(/[^0-9]/g, '') : '';
	var dtFormatted = (wd.length === 8) ? (wd.substr(0, 4) + '-' + wd.substr(4, 2) + '-' + wd.substr(6, 2)) : '';

	// 근무형태가 '제외'인 경우 출퇴근 시각 및 모든 실적/판정/비고 공백 초기화
	if (String(row.workTypeNm || '').trim() === '제외') {
		row.inTm = '';
		row.outTm = '';
		row.inDttm = '';
		row.outDttm = '';
		row.inJdgNm = '';
		row.outJdgNm = '';
		row.spclMtr = '';
		row.otTm = '';
		row.nghtTm = '';
		row.totWorkTm = '';
		row.lateTxt = '';
		row.earlyLeaveTxt = '';
		row.vacationTxt = '';
		row.substWorkYn = '';
		row.pjEquipNm = '';
		row.tripAllwnc = '';
		row.tripWorkPlace = '';
		row.rmk = '';
		row.lunchYn = '';
		row.dinnerYn = '';
		row.nightMealYn = '';
		row.lateEarlyRmk = '';
		return row;
	}

	// 근무형태가 '휴일' 또는 '휴가'인 경우 출퇴근 판정 및 근무시간 리셋
	var wtTrim = String(row.workTypeNm || '').trim();
	if (wtTrim === '휴일' || wtTrim === '휴가') {
		var jdg = (wtTrim === '휴가') ? '휴가' : '휴일';
		row.inJdgNm = jdg;
		row.outJdgNm = jdg;
		row.lateTxt = '';
		row.earlyLeaveTxt = '';
		row.totWorkTm = '';
		row.otTm = '';
		row.nghtTm = '';
		row.lateEarlyRmk = '';
		updateRowRmk(row);
		return row;
	}

	var wt = String(row.workTypeNm || '').trim();
	var isTripType = (wt === '설치시운전' || wt === '설치장애' || wt === 'A/S(무상)' || wt === '기타출장');

	// 1. 출장 유형(설치시운전, 설치장애, A/S(무상), 기타출장) 또는 출장신청서/복명서 확인된 건:
	// 수정출근일시 08:30 ~ 수정퇴근일시 17:30으로 자동 설정
	if ((isTripType || isTrip) && dtFormatted) {
		row.inDttm = dtFormatted + ' 08:30';
		row.outDttm = dtFormatted + ' 17:30';
	}

	// 2. 수정일시(inDttm/outDttm) 및 원시시각(inTm/outTm) 유효 조합 시각 생성 (수정일시 최우선 적용)
	var effectiveInStr = row.inDttm ? String(row.inDttm).trim() : (row.inTm ? String(row.inTm).trim() : '');
	var effectiveOutStr = row.outDttm ? String(row.outDttm).trim() : (row.outTm ? String(row.outTm).trim() : '');

	if (effectiveInStr && effectiveInStr.indexOf('-') === -1 && dtFormatted) {
		effectiveInStr = dtFormatted + ' ' + normalizeHHmm(effectiveInStr);
	}
	if (effectiveOutStr && effectiveOutStr.indexOf('-') === -1 && dtFormatted) {
		effectiveOutStr = dtFormatted + ' ' + normalizeHHmm(effectiveOutStr);
	}

	var inM = effectiveInStr ? moment(effectiveInStr, 'YYYY-MM-DD HH:mm') : null;
	var outM = effectiveOutStr ? moment(effectiveOutStr, 'YYYY-MM-DD HH:mm') : null;

	if (inM && outM && inM.isValid() && outM.isValid() && outM.isAfter(inM)) {
		var rawTotalMinutes = outM.diff(inM, 'minutes');

		// 총근무시간은 휴게시간 공제 없이 전체 체류/근무 시간 그대로 산출 (예: 24:00)
		var totalMinutes = Math.max(0, rawTotalMinutes);

		// 야간근무 (22:00 ~ 익일 06:00 중첩 구간 정밀 계산)
		var nightMinutes = 0;
		var curNightStart = inM.clone().set({ hour: 22, minute: 0, second: 0, millisecond: 0 });
		if (inM.isAfter(curNightStart)) {
			curNightStart.add(1, 'days');
		}
		var prevNightStart = curNightStart.clone().subtract(1, 'days');
		var nightRanges = [prevNightStart, curNightStart, curNightStart.clone().add(1, 'days')];

		$.each(nightRanges, function(_, nStart) {
			var nEnd = nStart.clone().add(8, 'hours'); // 22:00 ~ 06:00
			var overlapStart = inM.isAfter(nStart) ? inM : nStart;
			var overlapEnd = outM.isBefore(nEnd) ? outM : nEnd;
			if (overlapEnd.isAfter(overlapStart)) {
				nightMinutes += overlapEnd.diff(overlapStart, 'minutes');
			}
		});

		// 연장근무 계산
		var otMinutes = 0;
		if (isHoliday) {
			// 휴일: 체류시간 중 기본근로+휴게(08:30~18:00=570분) 및 야간근무시간(480분)을 제외한 나머지가 휴일연장근무시간
			var workMinsExceptBase = Math.max(0, rawTotalMinutes - 570);
			otMinutes = Math.max(0, workMinsExceptBase - nightMinutes);
		} else {
			// 평일: 18:00 이후 체류 시간 중 야간근무시간을 제외한 시간
			var eveningStart = inM.clone().set({ hour: 18, minute: 0, second: 0, millisecond: 0 });
			var otStart = inM.isAfter(eveningStart) ? inM : eveningStart;
			var rawOtMins = outM.isAfter(otStart) ? outM.diff(otStart, 'minutes') : 0;
			otMinutes = Math.max(0, rawOtMins - nightMinutes);
		}

		row.totWorkTm = fmtMinutesToHHmm(totalMinutes);
		row.otTm = fmtMinutesToHHmm(otMinutes);
		row.nghtTm = fmtMinutesToHHmm(nightMinutes);
	}

	// 3. 근태 판정 (출장건 및 일반건 통합 판정)
	var inTimeNorm = (inM && inM.isValid()) ? inM.format('HH:mm') : '';
	var outTimeNorm = (outM && outM.isValid()) ? outM.format('HH:mm') : '';

	if (isHoliday) {
		if (!inTimeNorm && !outTimeNorm) {
			row.inJdgNm = '';
			row.outJdgNm = '';
			row.lateTxt = '';
			row.earlyLeaveTxt = '';
		} else {
			row.inJdgNm = '휴일출근';
			row.lateTxt = '';

			var otMins = parseHHmmToMinutes(row.otTm);
			var ngtMins = parseHHmmToMinutes(row.nghtTm);
			var totalOtMins = otMins + ngtMins;
			var hasNight = (ngtMins > 0);

			if (hasNight) {
				row.outJdgNm = '휴일연장/야간근무';
			} else if (totalOtMins >= 30) {
				row.outJdgNm = '휴일연장';
			} else {
				row.outJdgNm = '휴일퇴근';
			}
			row.earlyLeaveTxt = '';
		}
	} else {
		// 평일 출근 판정 (수정출근일시 최우선)
		if (!inTimeNorm) {
			row.inJdgNm = '';
			row.lateTxt = '';
		} else {
			row.inJdgNm = (inTimeNorm > '08:30') ? '지각' : '정상출근';
			row.lateTxt = (inTimeNorm > '08:30') ? '1' : '';
		}

		// 평일 퇴근 판정 (수정퇴근일시 및 재계산된 연장/야간근무 최우선)
		var ngtMins = parseHHmmToMinutes(row.nghtTm);
		var otMins = parseHHmmToMinutes(row.otTm);
		var hasNight = (ngtMins > 0);
		var hasOt = (otMins >= 30); // 연장근무 30분 이상일 때만 연장근무, 30분 미만은 정상퇴근

		if (!outTimeNorm) {
			row.outJdgNm = '';
			row.earlyLeaveTxt = '';
		} else if (hasNight) {
			row.outJdgNm = '연장/야간근무';
			row.earlyLeaveTxt = '';
		} else if (hasOt) {
			row.outJdgNm = '연장근무';
			row.earlyLeaveTxt = '';
		} else if (outTimeNorm < '17:30') {
			row.outJdgNm = '조퇴';
			row.earlyLeaveTxt = '1';
		} else {
			row.outJdgNm = '정상퇴근';
			row.earlyLeaveTxt = '';
		}
	}

	// 지각/조퇴 시각 비고 텍스트(lateEarlyRmk) 동기화 (예: "지각 09:10", "조퇴 16:30")
	var lateEarlyArr = [];
	if (row.lateTxt === '1') {
		lateEarlyArr.push(inTimeNorm ? ('지각 ' + inTimeNorm) : '지각');
	}
	if (row.earlyLeaveTxt === '1') {
		lateEarlyArr.push(outTimeNorm ? ('조퇴 ' + outTimeNorm) : '조퇴');
	}
	row.lateEarlyRmk = lateEarlyArr.join(' / ');

	// 휴일이면서 근무내역(출퇴근시각, 출장, 휴가, 휴일대체근무)이 없으면 근무형태는 '휴일'로 동기화.
	// rowHasApplicationLinkage()로 판정 기준을 통일 — workApplNo(PM08)를 빠뜨리면 세콤 누락으로
	// 출퇴근시각이 비어있는 휴일대체근무 행이 재계산될 때마다 '사내2'가 '휴일'로 되돌아간다.
	var isHolidayRow = String(row.holidayYn || '').trim() === '휴일';
	var hasWorkTimeRow = !!(row.inDttm || row.outDttm || row.inTm || row.outTm);
	var hasApplRow = rowHasApplicationLinkage(row);
	if (isHolidayRow && !hasWorkTimeRow && !hasApplRow && row.workTypeNm !== '제외') {
		row.workTypeNm = '휴일';
	}

	// 전역 단일 비고(rmk) 조합 및 동기화
	updateRowRmk(row);

	return row;
}

// ─────────────────────────────────────────────────────────────────────────────────────
// PM30 근태 공통 유틸리티 (단일화된 표준 규칙)
// ─────────────────────────────────────────────────────────────────────────────────────

// ─ 시각 문자열(HH:mm) 추출 헬퍼 (ex: "08:34:00" -> "08:34", "15:43" -> "15:43", "0834" -> "08:34") ─
function extractHHmm(val) {
	if (!val) return '';
	var s = String(val).trim();
	if (s.indexOf(' ') !== -1) {
		var parts = s.split(' ');
		s = parts[parts.length - 1];
	}
	var m = s.match(/(\d{2}):(\d{2})/);
	if (m) return m[1] + ':' + m[2];
	if (/^\d{4}$/.test(s)) return s.substring(0, 2) + ':' + s.substring(2, 4);
	return '';
}

// ─ 수정퇴근일시 유효성 검사 (근무일 다음날 08:30 이후 등록 불가 방어) ─
function validateOutDttm(workDt, outDttm) {
	if (!outDttm || !workDt) return { valid: true };

	var wStr = String(workDt).replace(/[^0-9]/g, ''); // ex: "20260724"
	if (wStr.length !== 8) return { valid: true };

	var oStr = String(outDttm).trim(); // ex: "2026-07-25 09:00"
	var parts = oStr.split(' ');
	if (parts.length < 2) return { valid: true };

	var oDate = parts[0].replace(/[^0-9]/g, ''); // "20260725"
	var oTime = parts[1].replace(/[^0-9:]/g, ''); // "09:00"
	if (oTime.indexOf(':') === -1 && oTime.length >= 4) {
		oTime = oTime.substring(0, 2) + ':' + oTime.substring(2, 4);
	}

	// 근무일 기준 다음날 날짜 계산
	var y = parseInt(wStr.substring(0, 4), 10);
	var m = parseInt(wStr.substring(4, 6), 10) - 1;
	var d = parseInt(wStr.substring(6, 8), 10);
	var nextDayObj = new Date(y, m, d + 1);
	var nextY = nextDayObj.getFullYear();
	var nextM = ('0' + (nextDayObj.getMonth() + 1)).slice(-2);
	var nextD = ('0' + nextDayObj.getDate()).slice(-2);
	var nextDayStr = '' + nextY + nextM + nextD; // "20260725"

	// 1. 근무일 이전 날짜인 경우 차단
	if (oDate < wStr) {
		return {
			valid: false,
			msg: '수정퇴근일시(' + oStr + ')는 근무일자(' + parts[0] + ') 이전일 수 없습니다.'
		};
	}

	// 2. 근무일 당일인 경우 (23:59까지 모두 허용)
	if (oDate === wStr) {
		return { valid: true };
	}

	// 3. 근무일 다음날인 경우 (08:30 이하만 허용, 08:30 이후 차단)
	if (oDate === nextDayStr) {
		if (oTime > '08:30') {
			return {
				valid: false,
				msg: '수정퇴근일시는 근무일 다음날 08:30 이후로 등록할 수 없습니다. (입력값: ' + oStr + ')'
			};
		}
		return { valid: true };
	}

	// 4. 다음날을 초과한 날짜 (이틀 뒤 등) 차단
	if (oDate > nextDayStr) {
		return {
			valid: false,
			msg: '수정퇴근일시는 근무일 다음날 08:30 이후로 등록할 수 없습니다. (입력값: ' + oStr + ')'
		};
	}

	return { valid: true };
}

// ─ 반차 휴가 여부 판정 ─
function isHalfVacationRow(row) {
	if (!row) return false;
	var vacTxt = String(row.vacationTxt || '').trim();
	if (vacTxt === '0.5' || parseFloat(vacTxt) === 0.5) return true;
	var rmk = String(row.rmk || '');
	var spcl = String(row.spclMtr || '');
	if (rmk.indexOf('반차') !== -1 || spcl.indexOf('반차') !== -1) return true;
	if (rmk.indexOf('(오전)') !== -1 || rmk.indexOf('(오후)') !== -1) return true;
	if (spcl.indexOf('(오전)') !== -1 || spcl.indexOf('(오후)') !== -1) return true;
	return false;
}

// ─ 휴가 인정 정상근무 시간 산출 (전일 휴가 8시간, 반차 휴가 4시간, 기타 0시간) ─
function getVacationCreditHours(row) {
	if (!row) return 0;

	var vacTxt = String(row.vacationTxt || '').trim();
	var rmk = String(row.rmk || '');
	var spcl = String(row.spclMtr || '');
	var wt = String(row.workTypeNm || '').trim();
	var combined = rmk + ' ' + spcl + ' ' + wt;

	// 1. 반차 휴가 (0.5일 -> 4시간)
	if (isHalfVacationRow(row)) {
		return 4;
	}

	// 2. 전일 8시간 휴가 (연차, 포상, 대체휴가, 공가, 병가 등 1일 휴가)
	if (vacTxt === '1.0' || vacTxt === '1' || parseFloat(vacTxt) >= 1) {
		return 8;
	}

	if (row.vacApplNo && String(row.vacApplNo).trim() !== '') {
		return 8;
	}

	if (wt === '휴가') {
		return 8;
	}

	var vacKeywords = ['연차', '대체휴가', '대체휴무', '포상', '병가', '경조', '하계휴가', '출산휴가', '육아휴직', '특별휴가', '보상휴가'];
	for (var i = 0; i < vacKeywords.length; i++) {
		if (combined.indexOf(vacKeywords[i]) !== -1) {
			return 8;
		}
	}

	return 0;
}

// ─ 휴가신청서 실제 유형을 그대로 비고에 표시 (반차인 경우만 오전/오후 부가) ─
function formatVacationRemark(row, originalRmk) {
	var vacTypeNm = String(row.vacTypeNm || '').trim();
	var rawRmk = String(originalRmk || row.rmk || row.spclMtr || '').trim();

	var baseNm = vacTypeNm || rawRmk;
	if (!baseNm) return '';

	var pureNm = baseNm.replace(/\s*\(.*?\)/g, '').trim();
	var isHalf = (pureNm.indexOf('반차') !== -1) || (String(row.vacationTxt || '').trim() === '0.5');

	// 반차가 아닌 일반 휴가는 원본 명칭 그대로 반환
	if (!isHalf) {
		return pureNm;
	}

	// 반차 오전/오후 구분
	var ampm = '';
	var combined = rawRmk + ' ' + (row.spclMtr || '') + ' ' + (row.ampmNm || '');
	var aCode = String(row.ampmCd || '').toUpperCase();

	if (combined.indexOf('오전') !== -1 || aCode.indexOf('AM') !== -1 || aCode === 'PM07AMPM01' || aCode === 'PM07AMPM10') {
		ampm = '오전';
	} else if (combined.indexOf('오후') !== -1 || aCode.indexOf('PM') !== -1 || aCode === 'PM07AMPM02' || aCode === 'PM07AMPM20') {
		ampm = '오후';
	} else {
		var inTm = String(row.inTm || row.inDttm || '').replace(/[^0-9:]/g, '');
		var outTm = String(row.outTm || row.outDttm || '').replace(/[^0-9:]/g, '');
		if (inTm.indexOf(':') !== -1) {
			var inH = parseInt(inTm.split(':')[0], 10);
			if (inH >= 12) ampm = '오전';
		}
		if (!ampm && outTm.indexOf(':') !== -1) {
			var outH = parseInt(outTm.split(':')[0], 10);
			if (outH > 0 && outH <= 14) ampm = '오후';
		}
	}

	var displayNm = pureNm;
	if (displayNm === '연차반차' || displayNm === '연차' || displayNm === '반차') {
		return ampm ? (ampm + '반차') : '반차';
	}

	return ampm ? (displayNm + '(' + ampm + ')') : displayNm;
}

// ─ 당일 발생한 모든 사고내역(지각 시각, 조퇴 시각, 외출, 휴가 등) 종합 비고 문자열 생성 ─
function buildDailyAccidentRemarks(row, isSat, isHoli, normVal) {
	if (!row) return '';
	var isExcluded = (String(row.workTypeNm || '').trim() === '제외');
	if (isExcluded) return '';

	var accidentItems = [];
	var inTime = extractHHmm(row.inTm || row.inDttm);
	var outTime = extractHHmm(row.outTm || row.outDttm);

	// 1. 휴가 정보 판정
	var vacCredit = getVacationCreditHours(row);
	var isHalf = isHalfVacationRow(row);
	var vacText = '';
	if (vacCredit > 0 || isHalf || row.vacApplNo || row.vacTypeNm) {
		vacText = formatVacationRemark(row, '');
	}

	// 2. 지각 판정
	var isAmHalf = isHalf && (vacText.indexOf('오전') !== -1);
	var isLate = false;
	if (String(row.lateTxt || '').trim() === '1' || String(row.inJdgNm || '').trim() === '지각') {
		isLate = true;
	} else if (row.lateTxt !== undefined && row.lateTxt !== null && String(row.lateTxt).trim() === '' && row.inJdgNm) {
		isLate = false;
	} else if (!isAmHalf && inTime && inTime > '08:30') {
		isLate = true;
	}
	if (isLate) {
		accidentItems.push(inTime ? ('지각' + inTime) : '지각');
	}

	// 3. 조퇴 판정 (익일 퇴근/야간/연장 검사)
	var isEarly = false;
	if (String(row.earlyLeaveTxt || '').trim() === '1' || String(row.outJdgNm || '').trim() === '조퇴') {
		isEarly = true;
	} else if (row.earlyLeaveTxt !== undefined && row.earlyLeaveTxt !== null && String(row.earlyLeaveTxt).trim() === '' && row.outJdgNm) {
		isEarly = false;
	} else {
		var isNextDayOut = false;
		var wd = row.workDt ? String(row.workDt).replace(/[^0-9]/g, '') : '';
		var dtFormatted = (wd.length === 8) ? (wd.substr(0,4)+'-'+wd.substr(4,2)+'-'+wd.substr(6,2)) : '';
		var effectiveOutStr = String(row.outDttm || row.outTm || '').trim();
		if (effectiveOutStr && dtFormatted && effectiveOutStr.indexOf('-') !== -1) {
			var outDtPart = effectiveOutStr.split(' ')[0];
			if (outDtPart > dtFormatted) {
				isNextDayOut = true;
			}
		}

		var ngtVal = parseFloat(row.clsNight) || 0;
		var otVal = parseFloat(row.clsOt) || 0;
		var hasNight = (ngtVal > 0) || (row.nghtTm && row.nghtTm !== '0' && row.nghtTm !== '00:00');
		var hasOt = (otVal > 0);
		var isPmHalf = isHalf && (vacText.indexOf('오후') !== -1);

		if (!isNextDayOut && !hasNight && !hasOt && !isPmHalf && outTime && outTime < '17:30' && outTime > '00:00') {
			isEarly = true;
		}
	}

	if (isEarly) {
		accidentItems.push(outTime ? ('조퇴' + outTime) : '조퇴');
	}

	// 4. 외출 판정
	var isOuting = (String(row.vacTypeCd || '').trim() === 'PM07TYPE04') ||
	               (String(row.rmk || '').indexOf('외출') !== -1) ||
	               (String(row.spclMtr || '').indexOf('외출') !== -1);
	if (isOuting) {
		accidentItems.push('외출');
	}

	// 5. 휴가 내역 추가
	if (vacText) {
		accidentItems.push(vacText);
	}

	// 6. 휴일근무 판정
	var isHolidayDay = isHoli || (String(row.holidayYn || '').trim() === '휴일');
	if (isHolidayDay && (normVal || 0) >= 8) {
		accidentItems.push('휴일근무');
	}

	// 7. 대체근무 판정
	var isSubst = !!(row.workApplNo || String(row.substWorkYn).trim() === '1' ||
	                 (row.rmk && row.rmk.indexOf('대체근무') !== -1) ||
	                 (row.spclMtr && row.spclMtr.indexOf('대체근무') !== -1));
	if (isSubst) {
		accidentItems.push('[대체근무]');
	}

	// 8. 기타 사용자 입력 메모 보존
	var origRmk = cleanRemarkText(row.rmk || row.spclMtr || '');
	if (origRmk) {
		var knownKeywords = ['지각', '조퇴', '외출', '연차', '반차', '오전반차', '오후반차', '대체휴가', '포상휴가', '휴일', '휴일근무', '대체근무'];
		var isCovered = false;
		for (var k = 0; k < knownKeywords.length; k++) {
			if (origRmk === knownKeywords[k] || origRmk.replace(/[\s\(\):0-9]/g, '') === knownKeywords[k]) {
				isCovered = true;
				break;
			}
		}
		if (!isCovered && accidentItems.indexOf(origRmk) === -1) {
			accidentItems.push(origRmk);
		}
	}

	var resList = [];
	for (var i = 0; i < accidentItems.length; i++) {
		var itm = accidentItems[i];
		if (itm && resList.indexOf(itm) === -1) {
			resList.push(itm);
		}
	}
	return resList.join(', ');
}
