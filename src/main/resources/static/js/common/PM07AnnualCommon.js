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

			var wsDays = Number(item.workSubstDays || 0);
			var wsUsed = Number(item.workSubstUsedDays || 0);
			var wsBal  = Math.round((wsDays - wsUsed) * 10) / 10;

			var svDays = Number(item.summerVacDays || 0);
			var svUsed = Number(item.summerVacUsedDays || 0);
			var svBal  = Math.round((svDays - svUsed) * 10) / 10;

			var awDays = Number(item.awardGrantDays || 0);
			var awUsed = Number(item.awardUsedDays || 0);
			var awBal  = Math.round((awDays - awUsed) * 10) / 10;

			// 전년도 이월/선사용 (PREV_DAYS: TB_PM07M02.REM_DAYS)
			var pDays = 0;
			if (item.prevDays !== null && item.prevDays !== undefined && item.prevDays !== "") {
				pDays = Number(item.prevDays);
			} else if (enterDt) {
				var prevYy = String(Number(curYy) - 1);
				var pCalcGrant = PM07Annual.calculateGrantDays(enterDt, prevYy, moment(prevYy + "1231", "YYYYMMDD"));
				var pUsed = Number(item.prevUsedDays || 0);
				pDays = pCalcGrant - pUsed;
			}

			// 연차 잔여일수(미사용일수) 계산 (PM0711M01 미사용일수 공식과 100% 동일):
			// 발생일수(gDays) + 전년도이월/선사용(pDays) - 연차실사용일수(uDays)
			// ※ 휴일대체휴가(wsBal), 하계휴가(svBal), 포상휴가(awBal)는 연차에서 차감하지 않고 각각 독립적으로 계산
			var balDays = gDays + pDays - uDays;
			balDays = Math.round(balDays * 10) / 10;

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

			// 원본 텍스트 보관 (기존 잔여일 표시 패턴을 말끔히 제거하고 순수 코드명 보존)
			var origText = $opt.attr('data-orig-text');
			if (!origText) {
				origText = $opt.text().replace(/\s*\([^\)]*잔여[^\)]*\)/g, '').trim();
				$opt.attr('data-orig-text', origText);
			}

			var isSavedType = (savedVacTypeCd && savedVacTypeCd === code);

			// 1) 하계휴가 (PM07TYPE09)
			if (code === 'PM07TYPE09') {
				if (summerVacBal > 0 || isSavedType) {
					$opt.prop('disabled', false).removeAttr('disabled').css('color', '');
					$opt.text(origText + ' (잔여: ' + summerVacBal + '일)');
				} else {
					$opt.prop('disabled', true).css('color', '#aaa');
					$opt.text(origText + ' (잔여: 0일 - 사용불가)');
				}
			}
			// 2) 포상휴가 (PM07TYPE07 - 1일 단위)
			else if (code === 'PM07TYPE07') {
				if (awardBal >= 1.0 || isSavedType) {
					$opt.prop('disabled', false).removeAttr('disabled').css('color', '');
					$opt.text(origText + ' (잔여: ' + awardBal + '일)');
				} else {
					$opt.prop('disabled', true).css('color', '#aaa');
					$opt.text(origText + ' (잔여: ' + awardBal + '일 - 잔여부족)');
				}
			}
			// 3) 포상휴가반차 (PM07TYPE08 - 0.5일 단위)
			else if (code === 'PM07TYPE08') {
				if (awardBal >= 0.5 || isSavedType) {
					$opt.prop('disabled', false).removeAttr('disabled').css('color', '');
					$opt.text(origText + ' (잔여: ' + awardBal + '일)');
				} else {
					$opt.prop('disabled', true).css('color', '#aaa');
					$opt.text(origText + ' (잔여: ' + awardBal + '일 - 잔여부족)');
				}
			}
			// 4) 대체휴가 (PM07TYPE11 - 1일 단위)
			else if (code === 'PM07TYPE11') {
				if (workSubstBal >= 1.0 || isSavedType) {
					$opt.prop('disabled', false).removeAttr('disabled').css('color', '');
					$opt.text(origText + ' (잔여: ' + workSubstBal + '일)');
				} else {
					$opt.prop('disabled', true).css('color', '#aaa');
					$opt.text(origText + ' (잔여: ' + workSubstBal + '일 - 잔여부족)');
				}
			}
			// 5) 대체휴가반차 (PM07TYPE12 - 0.5일 단위)
			else if (code === 'PM07TYPE12') {
				if (workSubstBal >= 0.5 || isSavedType) {
					$opt.prop('disabled', false).removeAttr('disabled').css('color', '');
					$opt.text(origText + ' (잔여: ' + workSubstBal + '일)');
				} else {
					$opt.prop('disabled', true).css('color', '#aaa');
					$opt.text(origText + ' (잔여: ' + workSubstBal + '일 - 잔여부족)');
				}
			}
			// 6) 연차 (PM07TYPE01) & 반차 (PM07TYPE02)
			// ※ 연차 및 반차는 잔여일수가 0이거나 음수(-)여도 무조건 선택 및 신청 가능해야 함 (선사용/마이너스 연차 허용 정책)
			else if (code === 'PM07TYPE01' || code === 'PM07TYPE02') {
				$opt.prop('disabled', false).removeAttr('disabled').css('color', '');
				var dispBal = (Math.round(annualBal * 10) / 10);
				$opt.text(origText + ' (잔여: ' + dispBal + '일)');
			}
			else {
				// 기타 유형도 기본 활성화 상태 유지
				$opt.prop('disabled', false).removeAttr('disabled').css('color', '');
			}
		});

		// 비활성화된 옵션이 현재 선택되어 있다면 선택 해제 (단, 연차/반차는 0 또는 음수여도 절대 선택 해제 금지)
		var currentVal = $sel.val();
		if (currentVal && (!savedVacTypeCd || savedVacTypeCd !== currentVal)) {
			if (currentVal !== 'PM07TYPE01' && currentVal !== 'PM07TYPE02') {
				var $currOpt = $sel.find('option[value="' + currentVal + '"]');
				if ($currOpt.prop('disabled')) {
					$sel.val('');
					if (typeof onVacTypeChanged === 'function') {
						onVacTypeChanged();
					}
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

	// ========================================================================================
	// [PM07Annual.Popover] 공용 마우스 호버 신청서 상세 팝오버 & 모달 오픈 엔진
	// ----------------------------------------------------------------------------------------
	// PM0701P02, PM0701P04, PM0711M01, PM0810M01 등 모든 연차/대체휴가 화면에서 공용 사용
	// ========================================================================================
	var POPOVER_TIMER = null;
	var POPOVER_SHOW_TIMER = null;
	var POPOVER_REQUEST_ID = 0;
	var POPOVER_CURRENT_KEY = null; // 현재 열려있는 팝오버 대상 식별자 (마우스 이동 시 불필요한 refresh 차단)
	var POPOVER_ON_CLOSE_CALLBACK = null;
	var IS_OVER_TRIGGER = false; // 트리거 요소 호버 상태 추적
	var IS_OVER_POPOVER = false; // 팝오버 컨테이너 호버 상태 추적

	// 트리거와 팝오버 둘 다에서 마우스가 완전히 벗어났을 때만 안전 닫기 (400ms 유예, PM0810M01 동일)
	function checkAndHidePopover(delayMs) {
		if (POPOVER_TIMER) clearTimeout(POPOVER_TIMER);
		POPOVER_TIMER = setTimeout(function() {
			if (!IS_OVER_TRIGGER && !IS_OVER_POPOVER) {
				$('#pm07CommonVacationPopover').hide();
				POPOVER_TIMER = null;
				POPOVER_CURRENT_KEY = null;
			}
		}, delayMs || 400);
	}

	// 휴가유형 명칭 정제 헬퍼
	function cleanTypeName(txt) {
		if (!txt) return "";
		return String(txt).replace(/\(.*?\)/g, "").trim();
	}

	// 날짜 범위 포맷터 헬퍼
	function formatRange(st, ed) {
		if (st && ed) return st === ed ? st : (st + " ~ " + ed);
		return st || ed || "";
	}

	// 모달 스택 깊이에 따른 안전한 모달 오픈 (1차: openModal/openSecondModal, 2차 이상: openThirdModal)
	function openSmartModal(url, width, height, title, paramObj, callback) {
		window.deviceType = window.deviceType || 'desktop';
		var stackDepth = 0;
		if (typeof modalStack !== 'undefined' && modalStack) {
			if (typeof modalStack.size === 'function') {
				stackDepth = modalStack.size();
			} else if (modalStack.modalArr) {
				stackDepth = modalStack.modalArr.length;
			}
		}

		if (stackDepth >= 2 && typeof openThirdModal === 'function') {
			if (typeof thirdModal !== 'undefined' && thirdModal && thirdModal.config) {
				thirdModal.config.zIndex = 2001;
			}
			openThirdModal(url, width, height, title, paramObj, function(res) {
				if (typeof callback === 'function') callback(res);
			});
			setTimeout(function() {
				if (typeof thirdModal !== 'undefined' && thirdModal && thirdModal.activeModal) {
					thirdModal.activeModal.css("z-index", "2001");
				}
				$('.ax-mask').last().css("z-index", "2000");
			}, 50);
		} else if (stackDepth >= 1 && typeof openSecondModal === 'function') {
			openSecondModal(url, width, height, title, paramObj, function(res) {
				if (typeof callback === 'function') callback(res);
			});
		} else if (typeof openModal === 'function') {
			openModal(url, width, height, title, paramObj, function(res) {
				if (typeof callback === 'function') callback(res);
			});
		}
	}

	PM07Annual.Popover = {
		// 공통 스타일 자동 1회 주입 (PM0810M01과 100% 동일 톤앤매너, ::before 가상 요소 배제로 마우스 커서 침범 원천 차단)
		injectStyle: function() {
			if ($('#pm07CommonPopoverStyle').length === 0) {
				var css = '<style id="pm07CommonPopoverStyle">' +
					'.pm07-common-popover {' +
					'	position: fixed !important;' +
					'	z-index: 999999;' +
					'	display: none;' +
					'	background: #ffffff;' +
					'	border: 1px solid #3b62b1;' +
					'	border-radius: 6px;' +
					'	box-shadow: 0 4px 18px rgba(0, 0, 0, 0.22);' +
					'	padding: 10px 14px;' +
					'	min-width: 250px;' +
					'	max-width: 420px;' +
					'	max-height: 420px;' +
					'	overflow-y: auto;' +
					'	font-size: 12px;' +
					'	color: #333333;' +
					'	pointer-events: auto;' +
					'}' +
					'.pm07-common-popover .popover-title {' +
					'	font-weight: bold; color: #3b62b1; border-bottom: 1px solid #e9ecef;' +
					'	padding-bottom: 5px; margin-bottom: 6px; font-size: 12px;' +
					'}' +
					'.pm07-common-popover .popover-content {' +
					'	max-height: 280px; overflow-y: auto; overflow-x: hidden; padding-right: 4px;' +
					'}' +
					'.pm07-common-popover .popover-content::-webkit-scrollbar { width: 6px; }' +
					'.pm07-common-popover .popover-content::-webkit-scrollbar-thumb { background-color: #cbd5e1; border-radius: 3px; }' +
					'.pm07-common-popover .popover-content::-webkit-scrollbar-track { background-color: #f1f5f9; }' +
					'.pm07-common-popover .popover-item {' +
					'	padding: 4px 0; line-height: 1.4; color: #444444; border-bottom: 1px dashed #f0f0f0;' +
					'}' +
					'.pm07-common-popover .popover-item:last-child { border-bottom: none; }' +
					'.pm07-common-popover .popover-item.clickable {' +
					'	cursor: pointer; border-radius: 4px; padding: 5px 8px; margin: 2px 0;' +
					'	transition: background-color 0.15s ease, color 0.15s ease;' +
					'}' +
					'.pm07-common-popover .popover-item.clickable:hover {' +
					'	background-color: #ebf5fb; color: #1e40af;' +
					'}' +
					'.pm07-common-popover .popover-item.clickable:hover .pop-link-icon {' +
					'	color: #1e40af;' +
					'}' +
					'/* 호버 트리거 내부 아이콘/텍스트 노드 간 마우스 이벤트 핑퐁 방지 */' +
					'.used-days-info-trigger *, .award-used-days-trigger * {' +
					'	pointer-events: none !important;' +
					'}' +
					'</style>';
				$('head').append(css);
			}
		},

		// 공통 팝오버 DOM 컨테이너 반환 (없으면 생성 및 이벤트 바인딩)
		getContainer: function() {
			PM07Annual.Popover.injectStyle();
			var $pop = $('#pm07CommonVacationPopover');
			if ($pop.length === 0) {
				$('body').append(
					'<div id="pm07CommonVacationPopover" class="pm07-common-popover">' +
						'<div class="popover-title"><i class="fas fa-calendar-check"></i> <span id="pm07CommonPopTitle"></span></div>' +
						'<div id="pm07CommonPopContent" class="popover-content"></div>' +
					'</div>'
				);
				$pop = $('#pm07CommonVacationPopover');
			}

			// 팝오버 창 위에 마우스가 위치했을 때 닫힘 방지 및 지연 닫힘 (Hover Group 패턴)
			$pop.off('mouseenter mouseleave').on({
				mouseenter: function() {
					IS_OVER_POPOVER = true;
					if (POPOVER_TIMER) {
						clearTimeout(POPOVER_TIMER);
						POPOVER_TIMER = null;
					}
				},
				mouseleave: function() {
					IS_OVER_POPOVER = false;
					checkAndHidePopover(400);
				}
			});

			// 팝오버 항목 클릭 시 해당 신청서 모달 오픈
			$pop.off('click', '.popover-item.clickable').on('click', '.popover-item.clickable', function(e) {
				e.stopPropagation();
				var targetReqNo = $(this).attr('data-req-no');
				var targetKind = $(this).attr('data-kind') || 'vac';
				if (targetReqNo) {
					PM07Annual.Popover.hideImmediately();
					PM07Annual.Popover.openDetailModal(targetKind, targetReqNo, POPOVER_ON_CLOSE_CALLBACK);
				}
			});

			return $pop;
		},

		// 마우스 커서 위치 기반 정밀 배치 함수 (PM0810M01과 100% 동일: 커서 아래 16px, 오른쪽 10px로 커서 히트박스와 절대 겹치지 않음)
		positionPopover: function(e, $triggerEl) {
			var $pop = $('#pm07CommonVacationPopover');
			if (!$pop.length || !$pop.is(':visible')) return;

			var clientX = 0, clientY = 0;
			if (e && (typeof e.clientX !== 'undefined')) {
				clientX = e.clientX;
				clientY = e.clientY;
			} else if ($triggerEl && $triggerEl.length) {
				var rect = $triggerEl[0].getBoundingClientRect();
				clientX = rect.left + 10;
				clientY = rect.bottom;
			}

			if (!clientX && !clientY) return;

			var popW = $pop.outerWidth() || 280;
			var popH = $pop.outerHeight() || 180;
			var winW = $(window).width();
			var winH = $(window).height();

			// 마우스 커서 아래 16px, 오른쪽 10px에 배치 (커서 히트박스와 절대 겹치지 않음)
			var posX = clientX + 10;
			var posY = clientY + 16;

			// 우측 화면 밖으로 넘어가면 마우스 왼쪽으로
			if (clientX + popW + 15 > winW) {
				posX = clientX - popW - 10;
			}
			// 하단 화면 밖으로 넘어가면 마우스 위쪽으로
			if (clientY + popH + 20 > winH) {
				posY = clientY - popH - 16;
			}
			if (posX < 10) posX = 10;
			if (posY < 10) posY = 10;

			$pop.css({
				'top': posY + 'px',
				'left': posX + 'px'
			});
		},

		// 상세 모달 오픈
		openDetailModal: function(kind, reqNo, callback) {
			if (!reqNo) return;
			var paramObj = {
				"actionType": "U",
				"reqNo": reqNo
			};
			if (kind === 'sw') {
				paramObj.pgmId = "PM0801P01";
				openSmartModal("/static/html/user/pm/pm08/PM0801P01.html", 1100, 800, "휴일대체근무 신청서 상세", paramObj, callback);
			} else {
				paramObj.pgmId = "PM0701P01";
				openSmartModal("/static/html/user/pm/pm07/PM0701P01.html", 1100, 700, "휴가 신청서 상세", paramObj, callback);
			}
		},

		// 지연 숨김 (400ms 유예 및 Hover Group 검증)
		hide: function() {
			if (POPOVER_SHOW_TIMER) {
				clearTimeout(POPOVER_SHOW_TIMER);
				POPOVER_SHOW_TIMER = null;
			}
			checkAndHidePopover(400);
		},

		// 즉시 숨김
		hideImmediately: function() {
			if (POPOVER_SHOW_TIMER) {
				clearTimeout(POPOVER_SHOW_TIMER);
				POPOVER_SHOW_TIMER = null;
			}
			if (POPOVER_TIMER) {
				clearTimeout(POPOVER_TIMER);
				POPOVER_TIMER = null;
			}
			IS_OVER_TRIGGER = false;
			IS_OVER_POPOVER = false;
			$('#pm07CommonVacationPopover').hide();
			POPOVER_CURRENT_KEY = null;
		},

		// 팝오버 표출 (PM0810M01과 100% 동일 로직: e 수신 및 positionPopover 적용)
		show: function($triggerEl, opt, e) {
			if (POPOVER_TIMER) {
				clearTimeout(POPOVER_TIMER);
				POPOVER_TIMER = null;
			}
			var $pop = PM07Annual.Popover.getContainer();

			POPOVER_ON_CLOSE_CALLBACK = (opt && typeof opt.onClose === 'function') ? opt.onClose : null;

			var coCd = (opt && opt.coCd) || (typeof jwt !== 'undefined' ? jwt.coCd : 'GUN') || 'GUN';
			var userId = (opt && (opt.userId || opt.id)) ? (opt.userId || opt.id) : '';
			var userNm = (opt && opt.userNm) ? opt.userNm : '사원';
			var curYy = (opt && opt.yy) ? opt.yy : (window.moment ? moment().format('YYYY') : '2026');
			var colKey = (opt && opt.colKey) ? opt.colKey : '';
			var awardTypeCd = (opt && opt.awardTypeCd) ? opt.awardTypeCd : '';
			var awardNm = (opt && opt.awardTypeNm) ? opt.awardTypeNm : (awardTypeCd === 'PM07M04TYPE51' ? '하계대체휴가일수' : '포상휴가');
			var rawSt = (opt && opt.stDt) ? String(opt.stDt).replace(/[^0-9]/g, '') : '';
			var rawEd = (opt && opt.edDt) ? String(opt.edDt).replace(/[^0-9]/g, '') : '';

			// 동일 대상 팝오버가 이미 열려 있는 경우: 닫기 타이머를 즉시 해제하고 그대로 유지 (재조회 및 깜빡임 방지)
			var targetKey = userId + '_' + colKey + '_' + curYy + '_' + awardTypeCd + '_' + rawSt + '_' + rawEd;
			if ($pop.is(':visible') && POPOVER_CURRENT_KEY === targetKey) {
				if (POPOVER_TIMER) {
					clearTimeout(POPOVER_TIMER);
					POPOVER_TIMER = null;
				}
				return;
			}
			POPOVER_CURRENT_KEY = targetKey;

			var myRequestId = ++POPOVER_REQUEST_ID;

			// 타이틀 결정
			var titleText = userNm + ' 님의 신청 이력';
			if (colKey === "workSubstDays" || colKey === "sw" || colKey === "sw_all") {
				titleText = userNm + ' 님의 휴일대체근무(발생) 이력';
			} else if (colKey === "workSubstUsedDays" || colKey === "vac" || colKey === "vac_all") {
				titleText = userNm + ' 님의 근무대체(사용) 이력';
			} else if (colKey === "summerVacDays") {
				titleText = userNm + ' 님의 하계휴가대체(발생) 이력';
			} else if (colKey === "summerVacUsedDays") {
				titleText = userNm + ' 님의 하계휴가대체(사용) 이력';
			} else if (colKey === "usedDays" || colKey === "ann" || colKey === "ann_all") {
				titleText = userNm + ' 님의 연차/반차(사용) 이력';
			} else if (colKey === "awardUsedDays" || colKey === "award") {
				titleText = userNm + ' 님의 [' + awardNm + '] 사용 이력';
			}
			$('#pm07CommonPopTitle').text(titleText);
			$('#pm07CommonPopContent').html('<div class="popover-item" style="color: #888888;"><i class="fas fa-spinner fa-spin"></i> 실시간 신청서 백엔드 조회 중...</div>');

			// 팝오버 표시 및 커서 안전거리 위치 계산
			$pop.css('pointer-events', 'auto').show();
			PM07Annual.Popover.positionPopover(e, $triggerEl);

			// 1) 휴일대체근무 발생 (workSubstDays)
			if (colKey === "workSubstDays" || colKey === "sw" || colKey === "sw_all") {
				var reqParam = {
					"coCd": coCd,
					"userId": userId,
					"reqDtFrom": curYy + "0101",
					"reqDtTo": curYy + "1231"
				};
				postAjax("/user/pm/pm08/selectSubstituteWorkList", reqParam, null, function(data) {
					if (myRequestId !== POPOVER_REQUEST_ID) return;
					var list = (data && (data.result || data.resultList || data.list)) ? (data.result || data.resultList || data.list) : [];
					var htmlStr = '';
					$.each(list, function(i, row) {
						var dtText = row.holidayDt || row.reqDt || '';
						var tmText = (row.stTm && row.edTm) ? (' (' + row.stTm + '~' + row.edTm + ')') : '';
						var targetReqNo = row.reqNo || row.req_no || '';
						var reason = row.specialReason ? (' [' + $.trim(row.specialReason) + ']') : '';
						htmlStr += '<div class="popover-item clickable" data-kind="sw" data-req-no="' + targetReqNo + '" title="클릭 시 휴일대체근무 신청서 확인">' +
									'<i class="fas fa-check-circle" style="color: #337ab7; margin-right: 5px;"></i> ' +
									dtText + tmText + ' 휴일대체근무' + reason +
									' <i class="fas fa-external-link-alt pop-link-icon" style="font-size: 10px; color: #337ab7; margin-left: 5px;" title="신청서 열기"></i>' +
									'</div>';
					});
					if (!htmlStr) {
						htmlStr = '<div class="popover-item" style="color: #888888;">등록된 휴일대체근무(발생) 신청 이력이 없습니다.</div>';
					}
					$('#pm07CommonPopContent').html(htmlStr);
				}, false);
			}
			// 2) 하계휴가대체 발생 (summerVacDays) - 등록 문자열
			else if (colKey === "summerVacDays") {
				var infoText = (opt && opt.infoText) ? opt.infoText : "";
				var htmlStr = '';
				if (infoText) {
					var items = infoText.split(';');
					$.each(items, function(i, val) {
						var rawVal = $.trim(val);
						if (!rawVal) return;
						htmlStr += '<div class="popover-item"><i class="fas fa-check-circle" style="color: #337ab7; margin-right: 5px;"></i> ' + rawVal + '</div>';
					});
				}
				if (!htmlStr) {
					htmlStr = '<div class="popover-item" style="color: #888888;">등록된 하계휴가대체(발생) 등록 이력이 없습니다.</div>';
				}
				$('#pm07CommonPopContent').html(htmlStr);
			}
			// 3) 그 외 휴가 사용일수 (연차, 대체휴가사용, 하계휴가사용, 포상휴가사용)
			else {
				var rawSt = (opt && opt.stDt) ? String(opt.stDt).replace(/[^0-9]/g, '') : '';
				var rawEd = (opt && opt.edDt) ? String(opt.edDt).replace(/[^0-9]/g, '') : '';
				var calParam = {
					"coCd": coCd,
					"reqId": userId,
					"userId": userId,
					"reqDtFrom": (rawSt || (curYy + "0101")),
					"reqDtTo": (rawEd || (curYy + "1231")),
					"stDt": (rawSt || (curYy + "0101")),
					"edDt": (rawEd || (curYy + "1231"))
				};

				postAjax("/user/pm/pm07/selectVacationCalendarList", calParam, null, function(data) {
					if (myRequestId !== POPOVER_REQUEST_ID) return;
					var list = (data && (data.result || data.resultList || data.list)) ? (data.result || data.resultList || data.list) : [];
					var htmlStr = '';

					$.each(list, function(i, row) {
						var vacTypeCd = row.vacTypeCd || row.vac_type_cd || '';
						var rawNm = String(row.vacTypeNm || row.vac_type_nm || '');

						// 컬럼별 필터링
						if (colKey === "usedDays" || colKey === "ann" || colKey === "ann_all") {
							if (vacTypeCd !== 'PM07TYPE01' && vacTypeCd !== 'PM07TYPE02') return true;
						} else if (colKey === "workSubstUsedDays" || colKey === "vac" || colKey === "vac_all") {
							if (vacTypeCd !== 'PM07TYPE11' && vacTypeCd !== 'PM07TYPE12' && rawNm.indexOf('대체') === -1) return true;
						} else if (colKey === "summerVacUsedDays") {
							if (vacTypeCd !== 'PM07TYPE09' && rawNm.indexOf('하계') === -1) return true;
						} else if (colKey === "awardUsedDays" || colKey === "award") {
							if (awardTypeCd === 'PM07M04TYPE51') {
								if (vacTypeCd !== 'PM07TYPE09' && rawNm.indexOf('하계') === -1) return true;
							} else {
								if (vacTypeCd !== 'PM07TYPE07' && vacTypeCd !== 'PM07TYPE08' && rawNm.indexOf('포상') === -1) return true;
							}
						}

						// 날짜 유효기간 필터링
						var rowDt = String(row.rawStDt || row.stDt || row.st_dt || '').replace(/[^0-9]/g, '');
						if (rawSt && rawEd && rowDt) {
							if (rowDt < rawSt || rowDt > rawEd) return true;
						}

						var vNm = cleanTypeName(rawNm) || (colKey === "usedDays" ? "연차" : "휴가");
						var stDisp = row.stDt || row.st_dt || '';
						var edDisp = row.edDt || row.ed_dt || '';
						var dtText = formatRange(stDisp, edDisp);
						var vDays = Number(row.deductDays || row.deduct_days || row.vacDays || row.vac_days || 0);
						if (!vDays || isNaN(vDays) || vDays === 0) vDays = 1;
						var daysText = ' (' + (Math.round(vDays * 10) / 10) + '일)';
						var rmk = row.reqRmk || row.req_rmk || row.rmk || '';
						var rmkText = rmk ? (' [' + $.trim(rmk) + ']') : '';
						var targetReqNo = row.reqNo || row.req_no || '';

						htmlStr += '<div class="popover-item clickable" data-kind="vac" data-req-no="' + targetReqNo + '" title="클릭 시 휴가신청서 확인">' +
									'<i class="fas fa-check-circle" style="color: #337ab7; margin-right: 5px;"></i> ' +
									dtText + ' ' + vNm + daysText + rmkText +
									' <i class="fas fa-external-link-alt pop-link-icon" style="font-size: 10px; color: #337ab7; margin-left: 5px;" title="신청서 열기"></i>' +
									'</div>';
					});

					if (!htmlStr) {
						var emptyMsg = "등록된 상세 신청 이력이 없습니다.";
						if (colKey === "workSubstUsedDays") emptyMsg = "등록된 근무대체(사용) 신청 이력이 없습니다.";
						else if (colKey === "summerVacUsedDays") emptyMsg = "등록된 하계휴가대체(사용) 신청 이력이 없습니다.";
						else if (colKey === "usedDays") emptyMsg = "등록된 연차/반차 상세 신청 이력이 없습니다.";
						else if (colKey === "awardUsedDays" || colKey === "award") emptyMsg = "등록된 [" + awardNm + "] 사용 이력이 없습니다.";

						htmlStr = '<div class="popover-item" style="color: #888888;">' + emptyMsg + '</div>';
					}

					$('#pm07CommonPopContent').html(htmlStr);
				}, false);
			}
		},

		// 간편 이벤트 바인딩 헬퍼 (Hover Group 패턴 적용으로 포커스/마우스 이동 시 무한 깜빡임 완벽 차단)
		bindHover: function(selector, optionsGetter) {
			$(document).off('mouseenter mouseleave click', selector).on({
				mouseenter: function(e) {
					IS_OVER_TRIGGER = true;
					var $el = $(this);
					if (POPOVER_TIMER) {
						clearTimeout(POPOVER_TIMER);
						POPOVER_TIMER = null;
					}
					if (POPOVER_SHOW_TIMER) {
						clearTimeout(POPOVER_SHOW_TIMER);
						POPOVER_SHOW_TIMER = null;
					}
					POPOVER_SHOW_TIMER = setTimeout(function() {
						POPOVER_SHOW_TIMER = null;
						if (!IS_OVER_TRIGGER && !IS_OVER_POPOVER) return;
						var opt = (typeof optionsGetter === 'function') ? optionsGetter($el) : optionsGetter;
						if (opt) {
							PM07Annual.Popover.show($el, opt, e);
						}
					}, 150);
				},
				mouseleave: function(e) {
					IS_OVER_TRIGGER = false;
					if (POPOVER_SHOW_TIMER) {
						clearTimeout(POPOVER_SHOW_TIMER);
						POPOVER_SHOW_TIMER = null;
					}
					PM07Annual.Popover.hide();
				},
				click: function(e) {
					e.stopPropagation();
					IS_OVER_TRIGGER = true;
					var $el = $(this);
					if (POPOVER_SHOW_TIMER) {
						clearTimeout(POPOVER_SHOW_TIMER);
						POPOVER_SHOW_TIMER = null;
					}
					var opt = (typeof optionsGetter === 'function') ? optionsGetter($el) : optionsGetter;
					if (opt) {
						PM07Annual.Popover.show($el, opt, e);
					}
				}
			}, selector);
		}
	};

	// 전역 호환 함수 등록
	window.PM07Annual = PM07Annual;
	window.calculateAnnualGrantDays = PM07Annual.calculateGrantDays;

})(window);
