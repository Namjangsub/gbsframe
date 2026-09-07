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

// ─ 반차 유형 판정 (오전반차 'AM' | 오후반차 'PM' | 반차아님 null) ─
// [코드 최우선 원칙]: 데이터가 코드로 저장된 것은 코드를 기준으로 비교 판정하고 코드가 없는 경우에 한해서 텍스트 베이스로 활용한다.
// 단, 실제 출퇴근 타각 시각(수정일시 최우선)이 명백한 오전/오후 근무 패턴을 나타낼 경우 실데이터를 최우선 보호한다.
function getHalfVacationType(row) {
	if (!row) return null;

	// 1. [코드 최우선] 휴가유형코드(vacTypeCd) 기준 반차 여부 판정
	var vacCd = String(row.vacTypeCd || '').trim().toUpperCase();
	var isHalf = (vacCd === 'PM07TYPE02' || vacCd === 'PM07TYPE08' || vacCd === 'PM07TYPE12');

	// 코드가 없는 경우에 한해 일수(0.5) 및 텍스트 베이스로 활용
	if (!isHalf) {
		isHalf = (String(row.vacationTxt || '').trim() === '0.5' || parseFloat(row.vacationTxt) === 0.5);
	}
	if (!isHalf) {
		var txt = (String(row.spclMtr || '') + ' ' + String(row.rmk || '') + ' ' + String(row.vacTypeNm || '') + ' ' + String(row.ampmNm || ''));
		if (txt.indexOf('반차') !== -1) {
			isHalf = true;
		}
	}
	if (!isHalf) return null;

	// 2. [최우선] 실제 출퇴근 시각 기준 정밀 판정 (IN_DTTM/OUT_DTTM 최우선, IN_TM/OUT_TM 차선)
	var inStr = row.inDttm ? String(row.inDttm).trim() : (row.inTm ? String(row.inTm).trim() : '');
	var outStr = row.outDttm ? String(row.outDttm).trim() : (row.outTm ? String(row.outTm).trim() : '');
	var inHm = inStr ? (inStr.indexOf(' ') !== -1 ? inStr.split(' ')[1] : inStr) : '';
	var outHm = outStr ? (outStr.indexOf(' ') !== -1 ? outStr.split(' ')[1] : outStr) : '';
	inHm = inHm.replace(/[^0-9:]/g, '');
	outHm = outHm.replace(/[^0-9:]/g, '');

	// 2-1. 출근이 오전(11:30 이전)이고 퇴근이 15:30 이하인 경우 -> 명백한 오전 근무 후 오후 반차(PM)
	// (예: 06:46 출근 ~ 12:50 퇴근 -> 신청서에 오전으로 기안되었더라도 실제 출퇴근은 100% 오후 반차임)
	if (inHm && inHm < '11:30' && outHm && outHm <= '15:30') {
		return 'PM';
	}
	// 2-2. 출근 자체가 11:30 이후(오후)에 이루어진 경우 -> 명백한 오전 휴가 후 오후 출근(AM)
	if (inHm && inHm >= '11:30') {
		return 'AM';
	}
	// 2-3. 출근 기록만 있고 11:30 이전 출근인 경우 -> 오전 출근했으므로 오후 반차(PM)
	if (inHm && inHm < '11:30' && !outHm) {
		return 'PM';
	}
	// 2-4. 출근 기록이 없고 퇴근 기록만 15:30 이하인 경우 -> 오후 반차(PM)
	if (!inHm && outHm && outHm <= '15:30') {
		return 'PM';
	}
	// 2-5. 출근 기록이 없고 퇴근 기록만 15:30 이후인 경우 -> 오후 근무로 추정되는 오전 반차(AM)
	if (!inHm && outHm && outHm > '15:30') {
		return 'AM';
	}

	// 3. [코드 우선] 출퇴근 시각으로 판별할 수 없는 경우: 결재 공통코드(ampmCd) 기준 판정
	var ampmCd = String(row.ampmCd || '').toUpperCase().trim();
	if (ampmCd === 'PM07AMPM01' || ampmCd === 'PM07AMPM10' || ampmCd === 'AM') {
		return 'AM'; // 오전 반차 (코드 기준)
	}
	if (ampmCd === 'PM07AMPM02' || ampmCd === 'PM07AMPM20' || ampmCd === 'PM') {
		return 'PM'; // 오후 반차 (코드 기준)
	}

	// 4. [텍스트 베이스] 코드가 없는 경우에 한해서 텍스트 기준 판정
	var ampmNm = String(row.ampmNm || '').trim();
	if (ampmNm === '오전') return 'AM';
	if (ampmNm === '오후') return 'PM';

	var txtFallback = (String(row.spclMtr || '') + ' ' + String(row.rmk || '') + ' ' + String(row.vacTypeNm || ''));
	if (txtFallback.indexOf('오전') !== -1) return 'AM';
	if (txtFallback.indexOf('오후') !== -1) return 'PM';

	// 5. 기본값: 오후 반차 (오전 근무 후 오후 반차 퇴근이 현업 대다수)
	return 'PM';
}

// ─ 출근/퇴근 판정(inJdgNm, outJdgNm) 및 지각/조퇴(lateTxt, earlyLeaveTxt) 단일 표준 판정 헬퍼 (Single Source of Truth) ─
// PM3001M01.html, PM3002M01.html 및 공용 재계산에서 100% 동일한 판정 룰을 적용한다.
function applyAttendanceJudgment(row) {
	if (!row) return;

	var wt = String(row.workTypeNm || '').trim();
	var halfType = (typeof getHalfVacationType === 'function') ? getHalfVacationType(row) : null;
	var isHalf = (halfType !== null) || (typeof isHalfVacationRow === 'function' && isHalfVacationRow(row));
	var isVacation = (wt === '휴가') && !isHalf; // 반차는 전일휴가가 아니므로 평일 출퇴근 판정(else 분기)으로 진행
	var isHoliday = (String(row.holidayYn || '').trim() === '휴일') || (wt === '휴일');
	var hasSubstWork = !!(row.workApplNo || String(row.substWorkYn || '').trim() === '1');

	var effectiveInStr = row.inDttm ? String(row.inDttm).trim() : (row.inTm ? String(row.inTm).trim() : '');
	var effectiveOutStr = row.outDttm ? String(row.outDttm).trim() : (row.outTm ? String(row.outTm).trim() : '');

	if (hasSubstWork) {
		// 휴일대체근무 신청서가 연동된 행: 출퇴근 시각 유무와 상관없이 무조건 휴일출근 / 휴일퇴근 판정 기본 적용
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
	} else if (isVacation) {
		// 근무형태가 휴가인 행: 휴일과 마찬가지로 출/퇴근 판정 '휴가' 처리 및 지각/조퇴 비움
		row.inJdgNm = '휴가';
		row.outJdgNm = '휴가';
		row.lateTxt = '';
		row.earlyLeaveTxt = '';
	} else if (isHoliday) {
		// 순수 휴일인 행: 출퇴근 기록이 있으면 휴일출근/휴일퇴근 판정, 없으면 '휴일' / '휴일'
		if (!effectiveInStr && !effectiveOutStr) {
			row.inJdgNm = '휴일';
			row.outJdgNm = '휴일';
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
		// 평일 출근/퇴근 판정 (수정출근/퇴근일시 최우선, 반차 정상판정 적용)
		var inTimeNorm = effectiveInStr ? normalizeHHmm(effectiveInStr) : '';
		var outTimeNorm = effectiveOutStr ? normalizeHHmm(effectiveOutStr) : '';
		var halfType = getHalfVacationType(row);

		// 익일 퇴근(outDt > workDt) 여부 정밀 확인 — 24시 넘겨 익일 새벽 퇴근한 건이 00:44 < 17:30 에 걸려 조퇴로 잘못 판정되는 것을 차단한다.
		var isNextDayOut = false;
		var wd = row.workDt ? String(row.workDt).replace(/[^0-9]/g, '') : '';
		var dtFormatted = (wd.length === 8) ? (wd.substr(0,4)+'-'+wd.substr(4,2)+'-'+wd.substr(6,2)) : '';
		if (effectiveOutStr && dtFormatted && effectiveOutStr.indexOf('-') !== -1) {
			var outDtPart = effectiveOutStr.split(' ')[0];
			if (outDtPart > dtFormatted) {
				isNextDayOut = true;
			}
		}

		if (!inTimeNorm) {
			row.inJdgNm = '';
			row.lateTxt = '';
		} else if (halfType === 'AM') {
			// 오전 반차: 오전 휴가이므로 출근은 정상출근 처리, 지각 제외
			row.inJdgNm = '정상출근';
			row.lateTxt = '';
		} else {
			row.inJdgNm = (inTimeNorm > '08:30') ? '지각' : '정상출근';
			row.lateTxt = (inTimeNorm > '08:30') ? '1' : '';
		}

		if (!outTimeNorm) {
			row.outJdgNm = '';
			row.earlyLeaveTxt = '';
		} else {
			var otMins = parseHHmmToMinutes(row.otTm);
			var ngtMins = parseHHmmToMinutes(row.nghtTm);
			var totalOtMins = otMins + ngtMins;
			var hasNight = (ngtMins > 0);

			// 익일 퇴근(isNextDayOut)이 아니며 당일 퇴근 시각이 17:30 미만일 때만 조퇴!
			// 단, 오후 반차(PM)인 경우 정상퇴근 처리!
			var isEarlyLeave = (!isNextDayOut && outTimeNorm < '17:30' && halfType !== 'PM');

			if (hasNight) {
				row.outJdgNm = '연장/야간근무';
			} else if (totalOtMins >= 30) {
				row.outJdgNm = '연장근무';
			} else if (isEarlyLeave) {
				row.outJdgNm = '조퇴';
			} else {
				row.outJdgNm = '정상퇴근';
			}
			row.earlyLeaveTxt = isEarlyLeave ? '1' : '';
		}
	}
}

// ─ 행에 출장/휴가/휴일대체근무 중 하나라도 연결돼 있는지 판정하는 단일 기준 ─
function rowHasApplicationLinkage(row, matchedSubstWork) {
	if (!row) return false;
	return !!(row.vacApplNo || row.tripApplNo || row.tripReplyNo || row.workApplNo || matchedSubstWork);
}

// ─ 사내근무 유형 판정: 시급직(평일/휴일) & 연봉직(평일) -> 사내1, 연봉직(휴일) -> 사내2 ─
function getInhouseWorkTypeByOrg(row) {
	if (!row) return '사내1';
	var orgDiv = String(row.orgDivNm || '').trim();
	var salesArea = String(row.salesArea || row.salesAreaCd || '').trim();
	var isHourly = (orgDiv === '시급직' || salesArea === 'SALESAREA60' || salesArea === '시급직(생산)' || salesArea === '시급직(현장)');
	var isHoliday = (String(row.holidayYn || '').trim() === '휴일');

	// 연봉직(휴일) -> 사내2
	if (!isHourly && isHoliday) {
		return '사내2';
	}
	// 시급직(평일,휴일) & 연봉직(평일) -> 사내1
	return '사내1';
}

// ─ 조퇴, 외출, 지각 (반차 포함) 발생 행 여부 판정 헬퍼 ─
// 최종 시간 판단: IN_DTTM, OUT_DTTM 최우선, null인 경우 IN_TM, OUT_TM 적용
function isLateEarlyOutgoRow(row, matchedVacation) {
	if (!row) return false;
	var halfType = (typeof getHalfVacationType === 'function') ? getHalfVacationType(row) : null;
	if (!halfType && matchedVacation) {
		var vCd = String(matchedVacation.vacTypeCd || '').trim().toUpperCase();
		var isHalfMatched = (vCd === 'PM07TYPE02' || vCd === 'PM07TYPE08' || vCd === 'PM07TYPE12' || Number(matchedVacation.workHour) === 4);
		if (!isHalfMatched) {
			var vNm = String(matchedVacation.vacTypeNm || matchedVacation.pureVacTypeNm || '');
			isHalfMatched = (vNm.indexOf('반차') !== -1);
		}
		if (isHalfMatched) {
			var aCode = String(matchedVacation.ampmCd || '').toUpperCase();
			if (aCode === 'PM07AMPM01' || aCode === 'PM07AMPM10' || aCode === 'AM') halfType = 'AM';
			else if (aCode === 'PM07AMPM02' || aCode === 'PM07AMPM20' || aCode === 'PM') halfType = 'PM';
			else {
				var vNmFallback = String(matchedVacation.vacTypeNm || matchedVacation.pureVacTypeNm || '');
				if (vNmFallback.indexOf('오전') !== -1) halfType = 'AM';
				else halfType = 'PM';
			}
		}
	}
	// 1. 지각 (오전 반차는 지각 제외)
	var inTime = (typeof extractHHmm === 'function') ? extractHHmm(row.inDttm || row.inTm) : (row.inDttm || row.inTm || '');
	var isLate = false;
	if (halfType !== 'AM') {
		isLate = (String(row.lateTxt || '').trim() === '1') || (String(row.inJdgNm || '').trim() === '지각');
		if (!isLate && inTime && inTime.length >= 5) {
			var inHm = inTime.substr(inTime.length - 5, 5);
			if (inHm > '08:30') isLate = true;
		}
	}
	// 2. 조퇴 (오후 반차는 조퇴 제외)
	var isEarly = false;
	var outTime = (typeof extractHHmm === 'function') ? extractHHmm(row.outDttm || row.outTm) : (row.outDttm || row.outTm || '');
	if (halfType !== 'PM') {
		isEarly = (String(row.earlyLeaveTxt || '').trim() === '1') || (String(row.outJdgNm || '').trim() === '조퇴');
		if (!isEarly && outTime && outTime.length >= 5) {
			var outHm = outTime.substr(outTime.length - 5, 5);
			if (outHm < '17:30' && outHm > '00:00') isEarly = true;
		}
	}
	// 3. 외출
	var isOuting = (String(row.outgoYn || '').trim().toUpperCase() === 'Y') ||
	               (matchedVacation && String(matchedVacation.vacTypeCd || '').trim() === 'PM07TYPE04') ||
	               (String(row.rmk || '').indexOf('외출') !== -1) ||
	               (String(row.spclMtr || '').indexOf('외출') !== -1);
	// 4. 반차 (4시간 근무 + 4시간 휴가 결합으로 조퇴/지각과 같은 사내 근무 범주)
	var isHalfVac = (halfType !== null);

	return isLate || isEarly || isOuting || isHalfVac;
}

// ─ 근무형태(workTypeNm) 자동 판정 결과 산출 함수 (단일 표준 룰 엔진) ─
function determineWorkTypeNm(row, matchedTrip, matchedVacation, matchedSubstWork) {
	if (!row) return '';
	// 이미 '제외'(근태제외자)로 판정된 경우 '제외' 상태를 보존 (휴일로 덮어쓰지 않음)
	if (String(row.workTypeNm || '').trim() === '제외') {
		return '제외';
	}

	// 1. 출장 (설치시운전, 설치장애, A/S(무상), 기타출장)
	if (matchedTrip) {
		var tripTc = String(matchedTrip.tripTypeCds || matchedTrip.tripTypeCd || '').trim().toUpperCase();
		if (tripTc === 'PM51TYPE30') return '설치시운전';
		else if (tripTc === 'PM51TYPE40') return '설치장애';
		else if (tripTc === 'PM51TYPE60') return 'A/S(무상)';
		else if (tripTc.indexOf('PM51TYPE') !== -1) return '기타출장';
		else {
			// 코드가 없는 경우에 한해 텍스트 베이스 활용
			var tripNm = String(matchedTrip.tripTypeNm || matchedTrip.tripType || '').trim();
			if (tripNm.indexOf('설치시운전') !== -1) return '설치시운전';
			if (tripNm.indexOf('설치장애') !== -1) return '설치장애';
			if (tripNm.indexOf('A/S') !== -1 || tripNm.indexOf('무상') !== -1) return 'A/S(무상)';
			return '기타출장';
		}
	}

	// 2. 전일 휴가 (연차, 포상, 대체휴가 등 1일 전일 휴가 - 반차/외출은 출근 근무로 처리)
	if (matchedVacation && !isLateEarlyOutgoRow(row, matchedVacation)) {
		return '휴가';
	}

	var isHoliday = String(row.holidayYn || '').trim() === '휴일';
	// 근태내역(출근/퇴근 타각 시각 또는 총 근무시간) 존재 여부 정밀 확인
	var inTimeStr = String(row.inDttm || row.inTm || '').trim();
	var outTimeStr = String(row.outDttm || row.outTm || '').trim();
	var totWorkVal = Number(row.totWorkTm || 0);
	var hasWorkTime = (inTimeStr !== '' || outTimeStr !== '' || totWorkVal > 0);

	// 3. 휴일인 경우:
	//    - 근태내역이 있거나 휴일대체근무 신청서가 있을 때: 사내1(시급직), 사내2(연봉직) 판정
	//    - 근태내역이 없으면: 기본 '휴일'
	if (isHoliday) {
		if (hasWorkTime || matchedSubstWork) {
			return getInhouseWorkTypeByOrg(row); // 시급직: 사내1, 연봉직: 사내2
		}
		return '휴일';
	}

	// 4. 평일인 경우:
	//    - 조퇴, 외출, 지각, 반차 또는 일반 근무: 평일 사내1 자동 반영
	return getInhouseWorkTypeByOrg(row); // 평일: 사내1
}

// ─ 변환작업(convertPastedData) 등에서 판정 결과를 row.workTypeNm에 직접 반영 ─
function applyWorkTypeNmDecision(row, matchedTrip, matchedVacation, matchedSubstWork) {
	row.workTypeNm = determineWorkTypeNm(row, matchedTrip, matchedVacation, matchedSubstWork);
	return row;
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

	// 1. 출장 연동 유형명 (수주번호 ordrsNo가 있으면 "수주번호-유형명")
	if (row.tripApplNo || row.tripReplyNo) {
		var tripTypeStr = row.workTypeNm || '기타출장';
		if (row.ordrsNo) {
			tripTypeStr = row.ordrsNo + '-' + tripTypeStr;
		}
		rmkParts.push(tripTypeStr);
	}

	// 2. 휴가 연동 (출장과 휴가가 동시에 존재하면 2가지 모두 비고에 표시)
	if (row.vacApplNo) {
		var vacRmk = '';
		var halfType = (typeof getHalfVacationType === 'function') ? getHalfVacationType(row) : null;
		if (halfType === 'AM') {
			vacRmk = '반차(오전)';
		} else if (halfType === 'PM') {
			vacRmk = '반차(오후)';
		} else if (row.spclMtr && (row.spclMtr.indexOf('휴가') !== -1 || row.spclMtr.indexOf('반차') !== -1)) {
			var spParts = row.spclMtr.split(' / ');
			for (var p = 0; p < spParts.length; p++) {
				if (spParts[p].indexOf('휴가') !== -1 || spParts[p].indexOf('반차') !== -1) {
					vacRmk = spParts[p];
					break;
				}
			}
		}
		if (!vacRmk) {
			vacRmk = (row.workTypeNm === '휴가') ? '휴가' : (row.vacTypeNm || '휴가');
		}
		rmkParts.push(vacRmk);
	}

	// 3. 지각/조퇴 시각 정보 (recalcRowAttendanceObject에서 판정된 lateEarlyRmk)
	if (row.lateEarlyRmk) {
		rmkParts.push(row.lateEarlyRmk);
	}

	// 4. 대체근무
	if (String(row.substWorkYn || '').trim() === '1') {
		rmkParts.push('대체근무');
	}

	// 5. 지각/조퇴 자동등록 수동버튼 메시지 보존
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

	// 1. [코드 최우선] 출장 유형 코드(tripTypeCds) 우선 판정
	var tc = String(row.tripTypeCds || row.tripTypeCd || '').trim().toUpperCase();
	if (tc === 'PM51TYPE30') return 'install';
	if (tc === 'PM51TYPE40') return 'fault';
	if (tc === 'PM51TYPE60') return 'asFree';
	if (tc.indexOf('PM51TYPE') !== -1) return 'etcTrip';

	// 2. [텍스트 fallback] 코드가 없거나 매핑되지 않은 경우 근무형태 텍스트 기준 판정
	if (wt === '사내1' || wt === '사내2' || wt === '사내3') return 'inhouse';
	if (wt === '설치시운전') return 'install';
	if (wt === '설치장애') return 'fault';
	if (wt === 'A/S(무상)') return 'asFree';
	if (wt === '기타출장') return 'etcTrip';

	if (row.tripApplNo || row.tripReplyNo) return 'etcTrip';
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

	// 근무형태가 '휴일' 또는 '전일 휴가'이면서 실제 출퇴근 타각이나 대체근무 신청이 없는 경우 판정 및 근무시간 리셋
	var wtTrim = String(row.workTypeNm || '').trim();
	var effInCheck = row.inDttm ? String(row.inDttm).trim() : (row.inTm ? String(row.inTm).trim() : '');
	var effOutCheck = row.outDttm ? String(row.outDttm).trim() : (row.outTm ? String(row.outTm).trim() : '');
	var hasActualWorkTime = !!(effInCheck || effOutCheck);
	var hasSubstWorkCheck = !!(row.workApplNo || String(row.substWorkYn || '').trim() === '1');
	var isHalfVac = (typeof isHalfVacationRow === 'function') && isHalfVacationRow(row);

	if ((wtTrim === '휴일' && !hasActualWorkTime && !hasSubstWorkCheck) || (wtTrim === '휴가' && !isHalfVac && !hasActualWorkTime)) {
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
	// 수정출근일시 ~ 수정퇴근일시 자동 설정 (단, 반차가 결합된 경우 반차 근무시간과 합치되도록 정밀 산출)
	// [우선순위 원칙]: 담당자가 최종 수정한 값(isUserEditedInDttm/isUserEditedOutDttm) > DB에서 불러온 저장값(savedInDttm/savedOutDttm) > 자동 설정 기준
	if ((isTripType || isTrip) && dtFormatted) {
		var halfType = (typeof getHalfVacationType === 'function') ? getHalfVacationType(row) : null;
		var defaultIn = dtFormatted + (halfType === 'AM' ? ' 13:30' : ' 08:30');
		var defaultOut = dtFormatted + (halfType === 'PM' ? ' 12:30' : ' 17:30');

		if (!row.isUserEditedInDttm) {
			if (row.savedInDttm !== undefined && row.savedInDttm !== null && String(row.savedInDttm).trim() !== '') {
				row.inDttm = row.savedInDttm;
			} else if (!row.inDttm) {
				row.inDttm = defaultIn;
			}
		}

		if (!row.isUserEditedOutDttm) {
			if (row.savedOutDttm !== undefined && row.savedOutDttm !== null && String(row.savedOutDttm).trim() !== '') {
				row.outDttm = row.savedOutDttm;
			} else if (!row.outDttm) {
				row.outDttm = defaultOut;
			}
		}
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

	// 3. 근태 판정 (출장건 및 일반건 단일 표준 판정 함수 호출 — Single Source of Truth)
	applyAttendanceJudgment(row);

	var inTimeNorm = (inM && inM.isValid()) ? inM.format('HH:mm') : '';
	var outTimeNorm = (outM && outM.isValid()) ? outM.format('HH:mm') : '';

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
// [코드 최우선 원칙]: 휴가유형코드(vacTypeCd) 우선 판정, 코드가 없는 경우에 한해 일수 및 텍스트 fallback
function isHalfVacationRow(row) {
	if (!row) return false;
	// 1. [코드 최우선] 휴가유형코드(vacTypeCd) 기준 판정
	var vacCd = String(row.vacTypeCd || '').trim().toUpperCase();
	if (vacCd === 'PM07TYPE02' || vacCd === 'PM07TYPE08' || vacCd === 'PM07TYPE12') return true;

	// 2. [텍스트/수치 fallback] 코드가 없는 경우에 한해 일수(0.5) 및 비고 텍스트 기준
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
	var isHalf = isHalfVacationRow(row) || (pureNm.indexOf('반차') !== -1) || (String(row.vacationTxt || '').trim() === '0.5');

	// 반차가 아닌 일반 휴가는 원본 명칭 그대로 반환
	if (!isHalf) {
		return pureNm;
	}

	// 반차 오전/오후 구분: getHalfVacationType 단일 기준(Single Source of Truth) 적용
	var halfType = (typeof getHalfVacationType === 'function') ? getHalfVacationType(row) : null;
	var ampm = (halfType === 'AM') ? '오전' : ((halfType === 'PM') ? '오후' : '');

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
	var inTime = extractHHmm(row.inDttm || row.inTm);
	var outTime = extractHHmm(row.outDttm || row.outTm);

	// 1. 휴가 정보 판정
	var vacCredit = getVacationCreditHours(row);
	var isHalf = isHalfVacationRow(row);
	var vacText = '';
	if (vacCredit > 0 || isHalf || row.vacApplNo || row.vacTypeNm) {
		vacText = formatVacationRemark(row, '');
	}

	// 반차 유형(오전/오후) 판정
	var halfType = (typeof getHalfVacationType === 'function') ? getHalfVacationType(row) : null;
	var isAmHalf = (halfType === 'AM') || (isHalf && vacText.indexOf('오전') !== -1);
	var isPmHalf = (halfType === 'PM') || (isHalf && vacText.indexOf('오후') !== -1);

	// 2. 지각 판정 (오전 반차는 지각 제외)
	var isLate = false;
	if (isAmHalf) {
		isLate = false;
	} else if (String(row.lateTxt || '').trim() === '1' || String(row.inJdgNm || '').trim() === '지각') {
		isLate = true;
	} else if (row.lateTxt !== undefined && row.lateTxt !== null && String(row.lateTxt).trim() === '' && row.inJdgNm) {
		isLate = false;
	} else if (inTime && inTime > '08:30') {
		isLate = true;
	}
	if (isLate) {
		accidentItems.push(inTime ? ('지각' + inTime) : '지각');
	}

	// 3. 조퇴 판정 (오후 반차는 조퇴 제외, 익일 퇴근/야간/연장 검사)
	var isEarly = false;
	if (isPmHalf) {
		isEarly = false;
	} else if (String(row.earlyLeaveTxt || '').trim() === '1' || String(row.outJdgNm || '').trim() === '조퇴') {
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

		if (!isNextDayOut && !hasNight && !hasOt && outTime && outTime < '17:30' && outTime > '00:00') {
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
