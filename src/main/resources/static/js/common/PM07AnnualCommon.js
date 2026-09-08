/**
 * ========================================================================================
 * [PM07AnnualCommon.js] 연차휴가 자동계산 공통 라이브러리
 * ----------------------------------------------------------------------------------------
 * 1. 연차 발생 기준:
 *    - 기준일자(baseDate): 조회 시점 일자 (기본값: 오늘 moment())
 *    - 대상연도(targetYy): 1월 1일 회계연도 기준 부여연도 (기본값: 기준일자의 연도)
 * 2. 산정 및 저장 규칙:
 *    - 15 만근 근무자 (2026년 기준 2024-12-31 이전 입사자):
 *      * 2026-01-01 기준 이미 만 1년이 경과하여 기본 15일 부여
 *      * 이후 만 2년마다 1개씩 추가 (최대 25일)
 *      * DB에 정상 저장됨
 *    - 15 만근 근무자가 아닌 사람 (2025-01-01 이후 입사자):
 *      * 2026-01-01 기준 만 1년이 안 된 사람은 2026-12-31일까지 최대 11개 이상 될 수 없음
 *      * 15개는 다음해(2027-01-01) 시점에 비로소 발생됨
 *      * DB에는 무조건 0개로 저장되고, 화면 로드 시 조회시점 자동 계산(최대 11개)으로 표출됨
 * ========================================================================================
 */

(function(window) {
	'use strict';

	var PM07Annual = {};

	/**
	 * 날짜 문자열 포맷 정제 (YYYYMMDD -> YYYY-MM-DD)
	 */
	PM07Annual.formatDate = function(val) {
		if (!val) return "";
		var s = String(val).replace(/[^0-9]/g, '');
		if (s.length >= 8) {
			return s.substring(0, 4) + "-" + s.substring(4, 6) + "-" + s.substring(6, 8);
		}
		return val;
	};

	/**
	 * 부여연도 1월 1일 기준 만 근속년수 계산
	 * @param {String|Date|moment} enterDtStr 입사일자
	 * @param {String|Number}      targetYy   부여연도 (예: 2026)
	 * @returns {Number} 만 근속년수
	 */
	PM07Annual.getWorkedYearsOnYearStart = function(enterDtStr, targetYy) {
		if (!enterDtStr) return 0;
		var cleanDt = String(enterDtStr).replace(/[^0-9]/g, '');
		if (cleanDt.length < 8) return 0;

		var targetYear   = targetYy ? parseInt(targetYy, 10) : moment().year();
		var yearStartMom = moment([targetYear, 0, 1]); // 부여연도 1월 1일
		var enterMom     = moment(cleanDt.substring(0, 8), "YYYYMMDD");
		if (!enterMom.isValid()) return 0;

		if (enterMom.isAfter(yearStartMom, 'day')) {
			return 0; // 미래 또는 1월 1일 이후 입사자
		}

		return yearStartMom.diff(enterMom, 'years');
	};

	/**
	 * 부여연도 1월 1일 기준 15 만근 근무자(기본 15일 발생 대상) 여부 판별
	 * - 기준: 회계연도 기준 직전 1년간(전년도 01/01 ~ 12/31)을 만근한 직원
	 * - 판정: (부여연도 - 2)년 12월 31일 이하 입사자 (즉, 전년도 1월 1일 이전 입사자)
	 *   예) 2026년 기준: 2024-12-31 이하 입사자 (2025년 입사자는 2026년 중 최대 11개 월별 누적)
	 *   예) 2027년 기준: 2025-12-31 이하 입사자 (2025년 입사자 전원 15개 정상 발생)
	 * @param {String|Date|moment} enterDtStr 입사일자
	 * @param {String|Number}      targetYy   부여연도 (예: 2026)
	 * @returns {Boolean} true: 15 만근 대상(기본 15일 이상), false: 만 1년 미만자(DB 0개, 실시간 자동계산 대상)
	 */
	PM07Annual.isOverOneYearOnYearStart = function(enterDtStr, targetYy) {
		if (!enterDtStr) return false;
		var cleanDt = String(enterDtStr).replace(/[^0-9]/g, '');
		if (cleanDt.length < 8) return false;

		var targetYear = targetYy ? parseInt(targetYy, 10) : moment().year();
		var enterMom   = moment(cleanDt.substring(0, 8), "YYYYMMDD");
		if (!enterMom.isValid()) return false;

		// 회계연도(01/01) 기준 직전 1년(전년도 1/1 ~ 12/31) 전체 만근 여부 판별:
		// 전년도 1월 1일 이전 입사(즉, targetYear - 2년 12월 31일 이하 입사)여야 15 만근 대상
		var cutoffMom = moment([targetYear - 2, 11, 31]);

		return enterMom.isSameOrBefore(cutoffMom, 'day');
	};

	/**
	 * DB 저장용 기본 발생일수 산출 (15 만근 근무자가 아니면 DB에는 0개 저장)
	 * @param {String|Date|moment} enterDtStr 입사일자
	 * @param {String|Number}      targetYy   부여연도
	 * @returns {Number} DB 기본 저장 발생일수 (15 만근 근무자: 15~25, 그 외: 0)
	 */
	PM07Annual.getDbDefaultGrantDays = function(enterDtStr, targetYy) {
		if (PM07Annual.isOverOneYearOnYearStart(enterDtStr, targetYy)) {
			var workedYears = PM07Annual.getWorkedYearsOnYearStart(enterDtStr, targetYy);
			var addDays = Math.floor((workedYears - 1) / 2);
			return Math.min(15 + addDays, 25);
		}
		return 0; // 15 만근 근무자가 아니면 DB에는 0개 저장
	};

	/**
	 * 화면 로드 시 조회시점별 조회가능개수 실시간 계산
	 * - 15 만근 근무자 (2024-12-31 이전 입사자): 15일, 이후 만 2년마다 1개씩 추가 (최대 25일)
	 * - 15 만근 근무자가 아닌 사람 (2025-01-01 이후 입사자):
	 *   2026-12-31까지는 최대 11개 이상 될 수 없음! (조회시점 기준 만 1개월마다 +1개)
	 *   15개는 2027-01-01 시점에 발생됨
	 * @param {String|Date|moment} enterDtStr  입사일자
	 * @param {String|Number}      targetYy    부여연도 (예: 2026)
	 * @param {String|Date|moment} baseDate    조회 시점 기준일자 (기본값: 오늘)
	 * @returns {Number} 발생 연차일수
	 */
	PM07Annual.calculateGrantDays = function(enterDtStr, targetYy, baseDate) {
		if (!enterDtStr) return 0;
		var cleanDt = String(enterDtStr).replace(/[^0-9]/g, '');
		if (cleanDt.length < 8) return 0;

		var baseMom = baseDate ? moment(baseDate) : moment();
		if (!baseMom.isValid()) baseMom = moment();

		var targetYear = targetYy ? parseInt(targetYy, 10) : baseMom.year();
		var enterYear  = parseInt(cleanDt.substring(0, 4), 10);
		var enterMonth = parseInt(cleanDt.substring(4, 6), 10);
		var enterDay   = parseInt(cleanDt.substring(6, 8), 10);

		var enterMom = moment(cleanDt.substring(0, 8), "YYYYMMDD");
		if (!enterMom.isValid()) return 0;

		// 미래 입사자 또는 부여연도 시작일 이전 퇴사자: 0일
		var yearStartMom = moment([targetYear, 0, 1]);
		if (enterYear > targetYear || enterMom.isAfter(baseMom, 'day') || baseMom.isBefore(yearStartMom, 'day')) {
			return 0;
		}

		// 1. 15 만근 근무자 (부여연도 2026년 기준 2024-12-31 이전 입사자):
		//    기본 15일, 이후 만 2년마다 1개씩 추가 (최대 25일)
		if (PM07Annual.isOverOneYearOnYearStart(enterDtStr, targetYear)) {
			var workedYears = PM07Annual.getWorkedYearsOnYearStart(enterDtStr, targetYear);
			var addDays = Math.floor((workedYears - 1) / 2);
			return Math.min(15 + addDays, 25);
		}

		// 2. 15 만근 근무자가 아닌 사람 (부여연도 1월 1일 기준 만 1년 미만자, 예: 2026년 기준 2025-01-01 이후 입사자):
		//    입사일 기준 만 1개월 넘을 때마다 +1개씩 (최대 11개) 발생하여 12/31까지 유지,
		//    다음해 1월 1일(부여연도 1월 1일 기준 만 1년 경과 시점)에 15개 발생
		var evalMom = baseMom.clone();
		var yearEndMom = moment([targetYear, 11, 31]);
		if (evalMom.isAfter(yearEndMom, 'day')) {
			evalMom = yearEndMom;
		}

		var passedMonths = 0;
		var checkMom = enterMom.clone();
		for (var m = 1; m <= 11; m++) {
			checkMom.add(1, 'months');
			if (checkMom.isSameOrBefore(evalMom, 'day')) {
				passedMonths++;
			} else {
				break;
			}
		}
		return Math.min(passedMonths, 11);
	};

	/**
	 * 빈 잔여현황 기본 객체 반환
	 */
	PM07Annual.getEmptyBalanceObj = function(yy, userId) {
		return {
			yy                   : yy || (window.moment ? moment().format("YYYY") : new Date().getFullYear()),
			userId               : userId || "",
			grantDays            : 0,
			usedDays             : 0,
			balanceDays          : 0,
			workSubstDays        : 0,
			workSubstUsedDays    : 0,
			workSubstBalanceDays : 0,
			summerVacDays        : 0,
			summerVacUsedDays    : 0,
			summerVacBalanceDays : 0,
			awardGrantDays       : 0,
			awardUsedDays        : 0,
			awardBalanceDays     : 0
		};
	};

	/**
	 * 특정 사원의 전체 휴가 발생/사용/잔여 현황 조회 (PM0701P02.html과 100% 동일 산정 방식)
	 * @param {Object}   params   { coCd, yy, userId, baseDate }
	 * @param {Function} callback function(balanceInfo)
	 */
	PM07Annual.getUserVacationBalance = function(params, callback) {
		var curYy   = (params && params.yy) ? params.yy : (window.moment ? moment().format("YYYY") : new Date().getFullYear());
		var userId  = (params && params.userId) ? params.userId : "";
		var coCd    = (params && params.coCd) ? params.coCd : "GUN";
		var baseMom = (params && params.baseDate) ? moment(params.baseDate) : moment();

		var emptyObj = PM07Annual.getEmptyBalanceObj(curYy, userId);
		if (!userId) {
			if (typeof callback === "function") callback(emptyObj);
			return;
		}

		var postFn = (typeof postAjax === "function") ? postAjax : (window.$ ? $.post : null);
		if (!postFn) {
			if (typeof callback === "function") callback(emptyObj);
			return;
		}

		var reqData = {
			"coCd"           : coCd,
			"yy"             : String(curYy),
			"userId"         : userId,
			"includeLeaveYn" : "Y"
		};

		postAjax("/user/pm/pm07/selectAnnualGrantList", reqData, null, function(data) {
			var list = (data && data.resultList) ? data.resultList : [];
			if (!list || list.length === 0) {
				if (typeof callback === "function") callback(emptyObj);
				return;
			}

			var item = list[0];
			var enterDt = item.enterDt;
			var leaveDt = item.leaveDt;
			var isLeave = (item.useYn === 'N' || (leaveDt && $.trim(leaveDt) !== ''));
			var empBaseMom = baseMom;
			if (isLeave && leaveDt) {
				var cleanLeaveDt = String(leaveDt).replace(/[^0-9]/g, '');
				if (cleanLeaveDt.length === 8) {
					empBaseMom = moment(cleanLeaveDt, 'YYYYMMDD');
				}
			}
			var isManual = (item.autoYn === 'N' && Number(item.grantDays || 0) > 0);
			var gDays = Number(item.grantDays || 0);

			if (!isManual && enterDt) {
				gDays = PM07Annual.calculateGrantDays(enterDt, curYy, empBaseMom);
			}

			var uDays = Number(item.usedDays || 0);
			var balDays = gDays - uDays;

			var wsDays = Number(item.workSubstDays || 0);
			var wsUsed = Number(item.workSubstUsedDays || 0);
			var wsBal  = wsDays - wsUsed;

			var svDays = Number(item.summerVacDays || 0);
			var svUsed = Number(item.summerVacUsedDays || 0);
			var svBal  = svDays - svUsed;

			var awDays = Number(item.awardGrantDays || 0);
			var awUsed = Number(item.awardUsedDays || 0);
			var awBal  = awDays - awUsed;

			var res = {
				yy                   : curYy,
				userId               : userId,
				userNm               : item.userNm || "",
				deptNm               : item.deptNm || "",
				enterDt              : enterDt,
				grantDays            : gDays,
				usedDays             : uDays,
				balanceDays          : balDays,
				workSubstDays        : wsDays,
				workSubstUsedDays    : wsUsed,
				workSubstBalanceDays : wsBal,
				summerVacDays        : svDays,
				summerVacUsedDays    : svUsed,
				summerVacBalanceDays : svBal,
				awardGrantDays       : awDays,
				awardUsedDays        : awUsed,
				awardBalanceDays     : awBal,
				summerVacGenInfoList : item.summerVacGenInfoList || "",
				vacSummerInfoList    : item.vacSummerInfoList || "",
				workSubstGenInfoList : item.workSubstGenInfoList || "",
				vacWorkSubstInfoList : item.vacWorkSubstInfoList || ""
			};

			if (typeof callback === "function") {
				callback(res);
			}
		});
	};

	/**
	 * 휴가유형(vacTypeCd) select option 제어 (PM0701P01.html 등 공통 적용)
	 * - 하계휴가, 포상휴가, 포상휴가반차, 대체휴가, 대체휴가반차는 잔여일수가 있어야만 선택 가능하도록 제어
	 * @param {jQuery|HTMLElement} selectEl        #vacTypeCd select 엘리먼트
	 * @param {Object}              balanceInfo     PM07Annual.getUserVacationBalance 결과 객체
	 * @param {String}              savedVacTypeCd  현재 기등록/수정 중인 휴가유형 (수정 모드 시 기선택값 보존용)
	 */
	PM07Annual.applyVacationTypeOptions = function(selectEl, balanceInfo, savedVacTypeCd) {
		var $sel = $(selectEl);
		if (!$sel || $sel.length === 0) return;

		var bal = balanceInfo || {};
		var annualBal    = Number(bal.balanceDays || 0);
		var workSubstBal = Number(bal.workSubstBalanceDays || 0);
		var summerVacBal = Number(bal.summerVacBalanceDays || 0);
		var awardBal     = Number(bal.awardBalanceDays || 0);

		$sel.find('option').each(function() {
			var $opt = $(this);
			var code = $opt.val();
			if (!code) return; // '선택하세요' 건너뜀

			// 원본 텍스트 보관
			var origText = $opt.attr('data-orig-text');
			if (!origText) {
				origText = $opt.text().replace(/\s*\(잔여:?[^\)]*\)/g, '').trim();
				$opt.attr('data-orig-text', origText);
			}

			var isSavedType = (savedVacTypeCd && savedVacTypeCd === code);

			// 1) 하계휴가 (PM07TYPE09)
			if (code === 'PM07TYPE09') {
				if (summerVacBal > 0 || isSavedType) {
					$opt.prop('disabled', false).css('color', '');
					$opt.text(origText + ' (잔여: ' + summerVacBal + '일)');
				} else {
					$opt.prop('disabled', true).css('color', '#aaa');
					$opt.text(origText + ' (잔여: 0일 - 사용불가)');
				}
			}
			// 2) 포상휴가 (PM07TYPE07 - 1일 단위)
			else if (code === 'PM07TYPE07') {
				if (awardBal >= 1.0 || isSavedType) {
					$opt.prop('disabled', false).css('color', '');
					$opt.text(origText + ' (잔여: ' + awardBal + '일)');
				} else {
					$opt.prop('disabled', true).css('color', '#aaa');
					$opt.text(origText + ' (잔여: ' + awardBal + '일 - 잔여부족)');
				}
			}
			// 3) 포상휴가반차 (PM07TYPE08 - 0.5일 단위)
			else if (code === 'PM07TYPE08') {
				if (awardBal >= 0.5 || isSavedType) {
					$opt.prop('disabled', false).css('color', '');
					$opt.text(origText + ' (잔여: ' + awardBal + '일)');
				} else {
					$opt.prop('disabled', true).css('color', '#aaa');
					$opt.text(origText + ' (잔여: ' + awardBal + '일 - 잔여부족)');
				}
			}
			// 4) 대체휴가 (PM07TYPE11 - 1일 단위)
			else if (code === 'PM07TYPE11') {
				if (workSubstBal >= 1.0 || isSavedType) {
					$opt.prop('disabled', false).css('color', '');
					$opt.text(origText + ' (잔여: ' + workSubstBal + '일)');
				} else {
					$opt.prop('disabled', true).css('color', '#aaa');
					$opt.text(origText + ' (잔여: ' + workSubstBal + '일 - 잔여부족)');
				}
			}
			// 5) 대체휴가반차 (PM07TYPE12 - 0.5일 단위)
			else if (code === 'PM07TYPE12') {
				if (workSubstBal >= 0.5 || isSavedType) {
					$opt.prop('disabled', false).css('color', '');
					$opt.text(origText + ' (잔여: ' + workSubstBal + '일)');
				} else {
					$opt.prop('disabled', true).css('color', '#aaa');
					$opt.text(origText + ' (잔여: ' + workSubstBal + '일 - 잔여부족)');
				}
			}
			// 6) 연차 (PM07TYPE01) & 반차 (PM07TYPE02)
			// ※ 휴가일수가 모자라더라도(0 이하/음수) 항상 선택 및 등록 가능해야 함 (선사용/마이너스 연차 허용 정책)
			else if (code === 'PM07TYPE01' || code === 'PM07TYPE02') {
				$opt.prop('disabled', false).css('color', '');
				$opt.text(origText + ' (잔여: ' + annualBal + '일)');
			}
			else {
				// 기타 유형도 기본 활성화 상태 유지
				$opt.prop('disabled', false).css('color', '');
			}
		});

		// 비활성화된 옵션이 현재 선택되어 있다면 선택 해제
		var currentVal = $sel.val();
		if (currentVal && (!savedVacTypeCd || savedVacTypeCd !== currentVal)) {
			var $currOpt = $sel.find('option[value="' + currentVal + '"]');
			if ($currOpt.prop('disabled')) {
				$sel.val('');
				if (typeof onVacTypeChanged === 'function') {
					onVacTypeChanged();
				}
			}
		}
	};

	/**
	 * 휴가 신청 시 잔여일수 검증 헬퍼
	 * @param {String} vacTypeCd   선택된 휴가코드
	 * @param {Number} deductDays  차감일수
	 * @param {Object} balanceInfo 잔여현황 객체
	 * @returns {Object} { isValid: boolean, message: string }
	 */
	PM07Annual.validateVacationBalance = function(vacTypeCd, deductDays, balanceInfo) {
		if (!vacTypeCd || !balanceInfo) return { isValid: true, message: "" };

		// 연차(PM07TYPE01), 반차(PM07TYPE02)는 휴가일수가 모자라도 항상 선택 및 등록 가능 (마이너스 연차 허용 정책)
		if (vacTypeCd === 'PM07TYPE01' || vacTypeCd === 'PM07TYPE02') {
			return { isValid: true, message: "" };
		}

		var days = Number(deductDays || 0);
		if (vacTypeCd === 'PM07TYPE09') { // 하계휴가
			var svBal = Number(balanceInfo.summerVacBalanceDays || 0);
			if (svBal <= 0 || svBal < days) {
				return {
					isValid: false,
					message: "하계휴가 잔여일수가 부족합니다. (잔여: " + svBal + "일, 신청: " + days + "일)"
				};
			}
		} else if (vacTypeCd === 'PM07TYPE07' || vacTypeCd === 'PM07TYPE08') { // 포상휴가, 포상휴가반차
			var awBal = Number(balanceInfo.awardBalanceDays || 0);
			var reqMin = (vacTypeCd === 'PM07TYPE08') ? 0.5 : 1.0;
			if (awBal < reqMin || awBal < days) {
				return {
					isValid: false,
					message: "포상휴가 잔여일수가 부족합니다. (잔여: " + awBal + "일, 신청: " + days + "일)"
				};
			}
		} else if (vacTypeCd === 'PM07TYPE11' || vacTypeCd === 'PM07TYPE12') { // 대체휴가, 대체휴가반차
			var wsBal = Number(balanceInfo.workSubstBalanceDays || 0);
			var reqMin = (vacTypeCd === 'PM07TYPE12') ? 0.5 : 1.0;
			if (wsBal < reqMin || wsBal < days) {
				return {
					isValid: false,
					message: "대체휴가 잔여일수가 부족합니다. (잔여: " + wsBal + "일, 신청: " + days + "일)"
				};
			}
		}

		return { isValid: true, message: "" };
	};

	// 전역 호환 함수 등록
	window.PM07Annual = PM07Annual;
	window.calculateAnnualGrantDays = PM07Annual.calculateGrantDays;

})(window);
