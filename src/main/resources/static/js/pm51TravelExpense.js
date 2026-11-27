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

/*
 * 파일첨부(#fileList_area) 펼침 시 모달 하단 스크롤 자동 이동 (PM5101P01 / PM5102P01 공용)
 * sticky footer 위에서 #fileAttachCnts가 펼쳐졌을 때 화면 아래로 밀린 영역을 즉시 노출한다.
 */
window.pm51BindFileAreaAutoScroll = function($popArea) {
	if (!$popArea || !$popArea.length) return;
	$popArea.off('click.pm51FileScroll', '#fileList_area #fileAttachTxt')
		.on('click.pm51FileScroll', '#fileList_area #fileAttachTxt', function() {
			setTimeout(function() {
				var el = $popArea[0];
				if (el) {
					el.scrollTop = el.scrollHeight;
				}
			}, 50);
		});
};

/*
 * 일반 저장(신청서/복명서) FormData에 첨부파일 및 권한체크 필수 메타데이터 추가 공용 함수
 * - 파일이 실제 첨부된 폴더(nodeId)를 우선 comonCd로 전송해, 첨부 후 루트(FILETREE) 등 타 폴더로 클릭 이동해도 권한 오류가 나지 않도록 보장한다.
 * - 대리/회계/타 법인 사용자가 저장할 때도 파일 권한체크가 실제 로그인 사용자(jwt.userId / jwt.coCd) 기준으로 수행되도록 fileUserId/fileCoCd를 함께 전달한다.
 * - 최대 용량 초과 시 경고 후 false 반환.
 */
window.pm51AppendFileUploadData = function(formData, opts) {
	opts = opts || {};
	var fileUploadArr = [];
	var totalFileSize = 0;
	var rawFiles = (typeof treeModule !== 'undefined' && typeof treeModule.getFileArr === 'function')
		? (treeModule.getFileArr() || []) : [];
	var deleteFileArr = (typeof treeModule !== 'undefined' && typeof treeModule.getDeleteFileArr === 'function')
		? (treeModule.getDeleteFileArr() || []) : [];

	$.each(rawFiles, function(idx, obj) {
		if (obj && obj.fileKey == 0) {
			formData.append('files', obj.file);
			if (obj.file && obj.file.size) {
				totalFileSize += obj.file.size;
			}
			var cloneObj = $.extend({}, obj);
			cloneObj.file = '';
			fileUploadArr.push(cloneObj);
		}
	});

	if (totalFileSize > 0) {
		var MAX_SIZE = 200 * 1024 * 1024;
		postAjaxSync('/admin/cm/cm08/getMaxFileSize', null, null, function(data) {
			if (data && data.resultCode == 200 && data.maxFileSize) {
				var mbStr = String(data.maxFileSize).toUpperCase().replace('MB', '').replace('GB', '').trim();
				var mbInt = parseInt(mbStr, 10);
				if (!isNaN(mbInt)) {
					if (String(data.maxFileSize).toUpperCase().indexOf('GB') > -1) {
						mbInt = mbInt * 1024;
					}
					MAX_SIZE = mbInt * 1024 * 1024;
				}
			}
		});
		if (totalFileSize >= MAX_SIZE) {
			var limitMb = Math.round(MAX_SIZE / (1024 * 1024));
			var attachMb = Math.round(totalFileSize / (1024 * 1024));
			alert('첨부파일 자료 용량이 많습니다. 한도:' + limitMb + 'MB < 첨부:' + attachMb + 'MB\n분리해서 업로드하세요!');
			return false;
		}
	}

	var selectedNodeId = (typeof opts.getComonCd === 'function') ? $.trim(opts.getComonCd() || '') : '';
	var attachedNodeId = (fileUploadArr[0] && $.trim(fileUploadArr[0].nodeId || '')) || '';
	var comonCd = attachedNodeId || selectedNodeId || 'FILETREE';
	var fileCoCd = (fileUploadArr[0] && $.trim(fileUploadArr[0].coCd || ''))
		|| (typeof jwt !== 'undefined' && jwt && jwt.coCd)
		|| 'GUN';
	var fileUserId = (typeof jwt !== 'undefined' && jwt && jwt.userId) || '';

	formData.set('comonCd', comonCd);
	formData.set('fileCoCd', fileCoCd);
	formData.set('fileUserId', fileUserId);
	formData.set('uploadFileArr', JSON.stringify(fileUploadArr));
	formData.set('deleteFileArr', JSON.stringify(deleteFileArr));
	return true;
};

/*
 * 파일트리 초기화 공용 함수 (PM5101P01 출장신청서 / PM5102P01 출장복명서 공용)
 * - 문서 상세 조회 이후 1회만 호출되도록 하여 더미 키 비동기 응답이 실제 파일 목록을 덮어쓰는 레이스 컨디션을 방지한다.
 * - 문서 법인코드(coCd)와 스코프 셀렉터($popArea)를 사용해 결재자 조회·2차 모달(원천문서 조회)에서도 정확히 조회한다.
 */
window.pm51InitFileTree = function(opts) {
	opts = opts || {};
	var $form = opts.$form && opts.$form.length ? opts.$form : $('#popForm');
	var $popArea = $form.closest('.popup_area');
	if (window.modalStack && modalStack.last() && modalStack.last().paramObj) {
		modalStack.last().paramObj.gridObj = undefined;
	}
	var docNo = $.trim(opts.docNo || '');
	var fileTrgtKey = $.trim($form.find('#fileTrgtKey').val() || '') || docNo;
	if (!fileTrgtKey) {
		fileTrgtKey = (opts.tempPrefix || 'PM51_') + moment().format('YYYYMMDDHHmmss');
	}
	$form.find('#fileTrgtKey').val(fileTrgtKey);

	var fileCoCd = $.trim(opts.coCd || '')
		|| (typeof jwt !== 'undefined' && jwt && jwt.coCd)
		|| $.trim($form.find('#coCd').val() || '')
		|| 'GUN';

	($popArea.length ? $popArea.find('#fileList_area') : $('#fileList_area')).empty();
	var fileParam = {
		fileTrgtTyp: opts.fileTrgtTyp || 'PM5101P01',
		fileTrgtKey: fileTrgtKey,
		todoNo: docNo || fileTrgtKey,
		coCd: fileCoCd,
		userId: (typeof jwt !== 'undefined' && jwt && jwt.userId) || '',
		comonCd: 'FILETREE'
	};
	treeModule.initAll('deptTreeOrdars', 'file-grid', 'FILETREE', fileParam, 'fileList_area', fileCoCd);
};

/*
 * 첨부파일만 단독 저장 공용 처리 (PM5101P01 출장신청서 / PM5102P01 출장복명서 공용)
 * 읽기전용/결재잠금 상태에서도 coCd·userId·pgmId·fileTrgtKey·comonCd 필수 파라미터가
 * 누락되거나 중복 전송되지 않도록 새 FormData에 명시적으로 구성해 /admin/cm/cm08/fileUpload를 호출한다.
 */
window.pm51SaveFileOnly = function(opts) {
	opts = opts || {};
	var $form = opts.$form && opts.$form.length ? opts.$form : $('#popForm');
	var docNo = $.trim(opts.docNo || '');
	if (!docNo) {
		alert(opts.emptyDocMsg || '문서 번호가 없습니다.');
		return;
	}

	var formData = new FormData();
	if (window.pm51AppendFileUploadData(formData, { getComonCd: opts.getComonCd }) === false) {
		return;
	}

	var uploadFileArr = [];
	var deleteFileArr = [];
	try { uploadFileArr = JSON.parse(formData.get('uploadFileArr') || '[]'); } catch (e) { uploadFileArr = []; }
	try { deleteFileArr = JSON.parse(formData.get('deleteFileArr') || '[]'); } catch (e2) { deleteFileArr = []; }
	if (uploadFileArr.length === 0 && deleteFileArr.length === 0) {
		alert('변경된 파일이 없습니다.');
		return;
	}

	var coCd = formData.get('fileCoCd')
		|| $.trim($form.find('#coCd').val() || '')
		|| 'GUN';
	var userId = formData.get('fileUserId') || $.trim($form.find('#userId').val() || '');
	var pgmId = $.trim($form.find('#pgmId').val() || '') || opts.defaultPgmId || '';
	var fileTrgtKey = $.trim($form.find('#fileTrgtKey').val() || '') || docNo;

	formData.set('userId', userId);
	formData.set('coCd', coCd);
	formData.set('pgmId', pgmId);
	formData.set('fileTrgtKey', fileTrgtKey);

	openProgress(true);
	filePostAjax('/admin/cm/cm08/fileUpload', formData, function(data) {
		openProgress(false);
		if (data && data.resultCode == 200) {
			alert('파일이 저장되었습니다.');
			$form.closest('.popup_area').find('#fileSaveBtn, #fileUpBtn').hide();
			if (typeof opts.onSuccess === 'function') {
				opts.onSuccess(data);
			}
		} else if (data && data.resultCode == 900) {
			alert(data.resultMessage);
		} else {
			alert('파일 저장 중 오류 발생: ' + ((data && data.resultMessage) || '알 수 없는 오류'));
		}
	});
};

/*
 * PM51 공통코드(SPECRTS14/SPECRTS15 등) CODE_ETC 사용자 ID 목록 조회 (단일 공용 헬퍼).
 * - jQuery 1.11.1 동적 모달 로딩 시 $(document).ready()가 파일 하단 var 초기화보다 먼저 실행되어
 *   `undefined !== null`로 오판하던 호이스팅 버그를 원천 차단한다.
 * - 백엔드 PM51SvcImpl.selectAcctMngApproverIds()(selectCodeInfo)와 동일하게 selectCodeInfo를
 *   우선 조회하고, 응답이 없으면 selectChildCodeList로 폴백해 USE_YN·대소문자 차이로 인한 누락을 방지한다.
 */
(function() {
	var specrtsIdsCache = {};

	function extractSpecrtsIdsFromText(codeEtc) {
		var raw = String(codeEtc || '').trim();
		if (!raw || raw.toLowerCase() === 'null') return [];
		var parts = raw.replace(/\s/g, '').split(',');
		var ids = [];
		for (var i = 0; i < parts.length; i++) {
			var id = String(parts[i] || '').trim();
			if (id) ids.push(id);
		}
		return ids;
	}

	window.pm51GetSpecrtsUserIds = function(codeId, forceRefresh) {
		var key = String(codeId || '').trim().toUpperCase();
		if (!key) return [];
		if (!forceRefresh && Object.prototype.hasOwnProperty.call(specrtsIdsCache, key)) {
			return specrtsIdsCache[key].slice();
		}

		var ids = [];
		var resolved = false;
		if (typeof postAjaxSync === 'function') {
			postAjaxSync('/admin/cm/cm05/selectCodeInfo', { codeId: key }, null, function(data) {
				var info = data && (data.codeInfo || data.result);
				if (info && typeof info === 'object') {
					resolved = true;
					var codeEtc = info.codeEtc || info.CODE_ETC || '';
					ids = ids.concat(extractSpecrtsIdsFromText(codeEtc));
				}
			});
			if (ids.length === 0) {
				postAjaxSync('/admin/cm/cm05/selectChildCodeList', { codeId: key }, null, function(data) {
					var list = (data && (data.childCodeList || data.resultList || data.result)) || [];
					if (Array.isArray(list)) {
						resolved = true;
						for (var i = 0; i < list.length; i++) {
							var item = list[i] || {};
							var codeEtc = item.codeEtc || item.CODE_ETC || '';
							ids = ids.concat(extractSpecrtsIdsFromText(codeEtc));
						}
					}
				});
			}
		}

		var dedup = [];
		var seen = {};
		for (var j = 0; j < ids.length; j++) {
			var norm = ids[j].toLowerCase();
			if (!seen[norm]) {
				seen[norm] = true;
				dedup.push(ids[j]);
			}
		}
		if (resolved) {
			specrtsIdsCache[key] = dedup;
		}
		return dedup.slice();
	};

	window.pm51CheckAccountingAuth = function(userId) {
		var loginId = String(
			(userId !== undefined && userId !== null ? userId : '')
			|| (typeof jwt !== 'undefined' && jwt && jwt.userId)
			|| ''
		).trim().toLowerCase();
		if (!loginId) return false;
		var ids = window.pm51GetSpecrtsUserIds('SPECRTS15');
		for (var i = 0; i < ids.length; i++) {
			if (String(ids[i] || '').trim().toLowerCase() === loginId) {
				return true;
			}
		}
		return false;
	};

	window.pm51ResetSpecrtsCache = function(codeId) {
		if (codeId) {
			delete specrtsIdsCache[String(codeId).trim().toUpperCase()];
		} else {
			specrtsIdsCache = {};
		}
	};
})();

/*
 * 출장신청서(PM5101P01) 회계팀 자금집행담당자(SPECRTS15) / 관리부서 미결 결재자 권한 정책 단일 평가 함수.
 * applyEditPolicy / blockApprovalEntryEdit / travelerGrid 편집 가드 / payRmk 정책이
 * 서로 다른 조건식으로 어긋나 회귀가 재발하지 않도록 단일 진실 원천(Single Source of Truth)으로 관리한다.
 */
window.pm51EvalReqFinancePolicy = function(ctx) {
	ctx = ctx || {};
	var paid = !!ctx.paid;
	var rptExist = !!ctx.rptExist;
	var rptPaid = !!ctx.rptPaid;
	var financeLocked = paid || rptPaid;
	var isAccounting = !!ctx.isAccounting;
	var isMngPending = !!ctx.isMngPending;
	var isApprovalMode = !!ctx.isApprovalMode;
	var isMyPending = !!ctx.isMyPending;
	var generalApprovalDone = !!ctx.generalApprovalDone;
	var sequentialBlocked = !!ctx.sequentialBlocked;
	var hasCompletedMng = !!ctx.hasCompletedMng;
	var actionType = String(ctx.actionType || '');

	var isFinanceActor = isAccounting || isMngPending;
	var canFinanceManage = !financeLocked && isFinanceActor && actionType !== 'CHG';
	var canEditPayRemarksAndCurrency = canFinanceManage;
	var canEditTravelerAmounts = canFinanceManage;
	var canEditTravelerGridInPayMode = canFinanceManage;
	var canShowAutoCalc = canFinanceManage;
	var canShowFinanceSaveBtn = canFinanceManage;
	var canShowPayBtn = canFinanceManage && generalApprovalDone && !sequentialBlocked;
	var canShowMngApprovalLineBtns = canFinanceManage && !hasCompletedMng;
	var canEditCashierConfirm = isAccounting && !financeLocked;
	var bypassApprovalEntryLock = canFinanceManage || (isApprovalMode && isMyPending);

	return {
		paid: paid,
		rptExist: rptExist,
		rptPaid: rptPaid,
		financeLocked: financeLocked,
		isAccounting: isAccounting,
		isMngPending: isMngPending,
		isFinanceActor: isFinanceActor,
		generalApprovalDone: generalApprovalDone,
		canFinanceManage: canFinanceManage,
		canEditPayRemarksAndCurrency: canEditPayRemarksAndCurrency,
		canEditTravelerAmounts: canEditTravelerAmounts,
		canEditTravelerGridInPayMode: canEditTravelerGridInPayMode,
		canShowAutoCalc: canShowAutoCalc,
		canShowFinanceSaveBtn: canShowFinanceSaveBtn,
		canShowPayBtn: canShowPayBtn,
		canShowMngApprovalLineBtns: canShowMngApprovalLineBtns,
		canEditCashierConfirm: canEditCashierConfirm,
		bypassApprovalEntryLock: bypassApprovalEntryLock
	};
};

/*
 * 출장복명서(PM5102P01) 회계팀 자금집행담당자(SPECRTS15) 권한 정책 단일 평가 함수.
 */
window.pm51EvalRptFinancePolicy = function(ctx) {
	ctx = ctx || {};
	var actionType = String(ctx.actionType || '');
	var paid = !!ctx.paid;
	var isAccounting = !!ctx.isAccounting;
	var isMyPending = !!ctx.isMyPending;
	var generalApprovalDone = !!ctx.generalApprovalDone;
	var sequentialBlocked = !!ctx.sequentialBlocked;

	var canFinanceManage = isAccounting && !paid && actionType !== 'C';
	var canEditArea2 = canFinanceManage;
	var canShowFinanceSaveBtn = canFinanceManage;
	var canShowPayBtn = canFinanceManage && generalApprovalDone && !sequentialBlocked;
	var canShowLineApproveBtn = isMyPending && !sequentialBlocked;

	return {
		actionType: actionType,
		paid: paid,
		isAccounting: isAccounting,
		generalApprovalDone: generalApprovalDone,
		sequentialBlocked: sequentialBlocked,
		canFinanceManage: canFinanceManage,
		canEditArea2: canEditArea2,
		canShowFinanceSaveBtn: canShowFinanceSaveBtn,
		canShowPayBtn: canShowPayBtn,
		canShowLineApproveBtn: canShowLineApproveBtn
	};
};
