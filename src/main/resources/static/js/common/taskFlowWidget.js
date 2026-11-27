/* ===================================================================
 * [POC 1단계] 업무 플로우형 네비게이터 (ETO Task-Flow Navigator) 공통 모듈
 * 대상 화면: CM1001M01 / CM1002M01 / CM1101M01
 * =================================================================== */
var curTaskFlowProject = null;
var TF_STATUS_THEME = {
	ONGOING_DELAY:  { label: '지연미완료', color: '#b91c1c', bg: '#fef2f2', badgeCls: 'wbs-badge-ongoing-delay',  dotCls: 'dot-red' },
	ONGOING_WARN:   { label: '마감임박',   color: '#a16207', bg: '#fefce8', badgeCls: 'wbs-badge-ongoing-warn',   dotCls: 'dot-yellow' },
	ONGOING_ONTIME: { label: '정상진행',   color: '#15803d', bg: '#f0fdf4', badgeCls: 'wbs-badge-ongoing-ontime', dotCls: 'dot-green' },
	COMP_DELAY:     { label: '지연완료',   color: '#6d28d9', bg: '#f5f3ff', badgeCls: 'wbs-badge-comp-delay',     dotCls: 'dot-purple' },
	COMP_ONTIME:    { label: '정상완료',   color: '#1d4ed8', bg: '#eff6ff', badgeCls: 'wbs-badge-comp-ontime',    dotCls: 'dot-blue' },
	ALL_ENDED:      { label: '마감완료',   color: '#1d4ed8', bg: '#eff6ff', badgeCls: 'wbs-badge-comp-ontime',    dotCls: 'dot-blue' },
	NONE:           { label: '계획없음',   color: '#64748b', bg: '#f1f5f9', badgeCls: 'wbs-badge-gray',           dotCls: 'dot-gray' }
};

function getTaskFlowWidgetTemplateHtml() {
	return ''
		+ '<div id="taskFlowWidget" class="panel panel-default task_flow_panel">'
		+ '	<div class="panel-heading task_flow_head">'
		+ '		<div class="task_flow_title">'
		+ '			<i class="fa fa-sitemap" style="color: #2a6496; margin-right: 6px;"></i>'
		+ '			<span style="font-weight: bold; font-size: 13px; color: #333;">업무 플로우 네비게이터 (ETO 프로세스 맵)</span>'
		+ '			<span class="badge" style="background-color: #337ab7; font-size: 10px; margin-left: 6px;">POC 1단계</span>'
		+ '		</div>'
		+ '		<div class="task_flow_controls">'
		+ '			<span class="tf-ctrl-label"><i class="fa fa-building"></i> 회사:</span>'
		+ '			<select id="taskFlowCoCdSelect" class="form-control input-sm" data-kind="CO" onchange="onTaskFlowCondChange()" style="width: 82px;">'
		+ '			</select>'
		+ '			<span class="tf-ctrl-label"><i class="fa fa-calendar-alt"></i> 수주기간:</span>'
		+ '			<input type="text" id="taskFlowStrtDt" class="form-control input-sm tf-date-input" onkeyup="if(typeof dateMask===\'function\')dateMask(this);" onchange="onTaskFlowCondChange()" placeholder="시작일자" autocomplete="off" style="width: 84px;" title="클릭하여 수주기간 선택 (시작일)">'
		+ '			<span style="color: #6b7280; font-weight: bold; font-size: 11px;">~</span>'
		+ '			<input type="text" id="taskFlowEndDt" class="form-control input-sm tf-date-input" onkeyup="if(typeof dateMask===\'function\')dateMask(this);" onchange="onTaskFlowCondChange()" placeholder="종료일자" autocomplete="off" style="width: 84px;" title="클릭하여 수주기간 선택 (종료일)">'
		+ '			<span class="tf-ctrl-label"><i class="fa fa-check-square"></i> 마감:</span>'
		+ '			<select id="taskFlowEndYnSelect" class="form-control input-sm" onchange="onTaskFlowCondChange()" style="width: 78px;" title="과제 마감 여부">'
		+ '				<option value="" selected>전체</option>'
		+ '				<option value="N">마감전</option>'
		+ '				<option value="Y">마감완료</option>'
		+ '			</select>'
		+ '			<span class="tf-ctrl-label"><i class="fa fa-filter"></i> 기준 프로젝트:</span>'
		+ '			<div class="eto-rich-select" id="taskFlowProjectRichSelect" data-target-select="#taskFlowProjectSelect" style="width: 390px; max-width: 100%;">'
		+ '				<select id="taskFlowProjectSelect" class="form-control input-sm" onchange="onTaskFlowProjectChange(this.value)" style="display: none;">'
		+ '					<option value="">선택</option>'
		+ '				</select>'
		+ '				<button type="button" class="eto-rich-trigger" aria-haspopup="listbox" aria-expanded="false">'
		+ '					<span class="eto-rich-label">선택</span>'
		+ '					<span class="eto-rich-badges"></span>'
		+ '					<i class="fa fa-caret-down" style="color: #64748b; flex-shrink: 0;"></i>'
		+ '				</button>'
		+ '				<div class="eto-rich-menu" role="listbox"></div>'
		+ '			</div>'
		+ '			<button type="button" class="btn btn-default btn-xs" onclick="reloadTaskFlowStatus()" title="상태 새로고침" style="height: 26px; padding: 2px 7px;">'
		+ '				<i class="fa fa-sync-alt"></i>'
		+ '			</button>'
		+ '			<button type="button" class="btn btn-primary btn-xs" onclick="openFullEtoProcessMap();" title="전체 ETO 프로세스 맵 새창 열기" style="height: 26px; padding: 2px 8px; font-weight: bold; background-color: #2e6da4;">'
		+ '				<i class="fa fa-project-diagram" style="margin-right: 3px;"></i> 전체 ETO 맵'
		+ '			</button>'
		+ '			<button type="button" class="btn btn-default btn-xs" id="taskFlowToggleBtn" onclick="toggleTaskFlowWidget()" title="위젯 접기/펼치기" style="height: 26px; padding: 2px 7px;">'
		+ '				<i class="fa fa-chevron-up"></i>'
		+ '			</button>'
		+ '		</div>'
		+ '	</div>'
		+ '	<div id="taskFlowBody" class="panel-body task_flow_body" style="padding: 10px;">'
		+ '		<div class="task_flow_summary_bar">'
		+ '			<div class="summary_item"><span class="sum_label">수주번호-차수:</span> <strong id="tfOrdrsNo" class="sum_val">-</strong></div>'
		+ '			<div class="summary_item"><span class="sum_label">고객사:</span> <span id="tfClntNm" class="sum_val sum_val_key">-</span></div>'
		+ '			<div class="summary_item"><span class="sum_label">프로젝트:</span> <span id="tfClntPjtNm" class="sum_val sum_val_key">-</span></div>'
		+ '			<div class="summary_item"><span class="sum_label">계약/기계명:</span> <span id="tfCtrtNm" class="sum_val" style="max-width: 240px; display: inline-block; text-overflow: ellipsis; white-space: nowrap; overflow: hidden; vertical-align: bottom;">-</span></div>'
		+ '			<div class="summary_item"><span class="sum_label">납기일:</span> <span id="tfDudt" class="sum_val">-</span></div>'
		+ '			<div class="summary_item" style="margin-left: auto;">'
		+ '				<span class="sum_label">수주확정/결재:</span>'
		+ '				<span id="tfAppBadge" class="badge" style="background-color: #777;">미확인</span>'
		+ '			</div>'
		+ '		</div>'
		+ '		<div class="task_flow_steps">'
		+ '			<div class="flow_step_card" id="flowStep1" data-step="1">'
		+ '				<div class="step_header">'
		+ '					<div class="step_num">01</div>'
		+ '					<div class="step_info">'
		+ '						<div class="step_name">수주(원가) / 과제</div>'
		+ '						<div class="step_badge" id="stepBadge1"><span class="badge status_wait">대기</span></div>'
		+ '					</div>'
		+ '				</div>'
		+ '				<div class="step_metric" id="stepMetric1">수주 결재상태 확인</div>'
		+ '				<div class="step_actions">'
		+ '					<button type="button" class="btn btn-xs btn_flow_jump btn_flow_full" onclick="jumpToTaskFlowMenu(\'/static/html/user/cr/cr02/CR0201M01.html\', \'CR0201M01\')" title="수주(목표)원가관리로 이동">수주(목표)원가</button>'
		+ '					<button type="button" class="btn btn-xs btn_flow_jump" onclick="jumpToTaskFlowMenu(\'/static/html/user/wb/wb21/WB2101M03.html\', \'WB2101M03\')" title="과제등록으로 이동">과제등록</button>'
		+ '					<button type="button" class="btn btn-xs btn_flow_jump" onclick="jumpToTaskFlowMenu(\'/static/html/user/cr/cr50/CR5001M01.html\', \'CR5001M01\')" title="PFU등록으로 이동">PFU등록</button>'
		+ '				</div>'
		+ '			</div>'
		+ '			<div class="flow_arrow"><i class="fa fa-angle-right"></i></div>'
		+ '			<div class="flow_step_card" id="flowStep2" data-step="2">'
		+ '				<div class="step_header">'
		+ '					<div class="step_num">02</div>'
		+ '					<div class="step_info">'
		+ '						<div class="step_name">일정 / 설계</div>'
		+ '						<div class="step_badge" id="stepBadge2"><span class="badge status_wait">대기</span></div>'
		+ '					</div>'
		+ '				</div>'
		+ '				<div class="step_metric" id="stepMetric2">WBS / BOM 현황</div>'
		+ '				<div class="step_actions">'
		+ '					<button type="button" class="btn btn-xs btn_flow_jump" onclick="jumpToTaskFlowMenu(\'/static/html/user/wb/wb07/WB0701M01.html\', \'WB0701M01\')" title="WBS일정관리로 이동">WBS일정관리</button>'
		+ '					<button type="button" class="btn btn-xs btn_flow_jump" onclick="jumpToTaskFlowMenu(\'/static/html/user/bm/bm14/BM1402M01.html\', \'BM1402M01\')" title="BOM관리로 이동">BOM관리</button>'
		+ '					<button type="button" class="btn btn-xs btn_flow_jump" onclick="jumpToTaskFlowMenu(\'/static/html/user/dw/dw02/DW0201M01.html\', \'DW0201M01\')" title="도면관리대장으로 이동">도면관리대장</button>'
		+ '				</div>'
		+ '			</div>'
		+ '			<div class="flow_arrow"><i class="fa fa-angle-right"></i></div>'
		+ '			<div class="flow_step_card" id="flowStep3" data-step="3">'
		+ '				<div class="step_header">'
		+ '					<div class="step_num">03</div>'
		+ '					<div class="step_info">'
		+ '						<div class="step_name">구매 / 외주</div>'
		+ '						<div class="step_badge" id="stepBadge3"><span class="badge status_wait">대기</span></div>'
		+ '					</div>'
		+ '				</div>'
		+ '				<div class="step_metric" id="stepMetric3">발주요청 / 발주현황</div>'
		+ '				<div class="step_actions">'
		+ '					<button type="button" class="btn btn-xs btn_flow_jump" onclick="jumpToTaskFlowMenu(\'/static/html/user/sm/sm02/SM0201M01.html\', \'SM0201M01\')" title="발주관리로 이동">발주관리</button>'
		+ '					<button type="button" class="btn btn-xs btn_flow_jump" onclick="jumpToTaskFlowMenu(\'/static/html/user/sm/sm03/SM0301M01.html\', \'SM0301M01\')" title="입고관리로 이동">입고관리</button>'
		+ '					<button type="button" class="btn btn-xs btn_flow_jump" onclick="jumpToTaskFlowMenu(\'/static/html/user/sm/sm11/SM1101M01.html\', \'SM1101M01\')" title="외주관리로 이동">외주관리</button>'
		+ '				</div>'
		+ '			</div>'
		+ '			<div class="flow_arrow"><i class="fa fa-angle-right"></i></div>'
		+ '			<div class="flow_step_card" id="flowStep4" data-step="4">'
		+ '				<div class="step_header">'
		+ '					<div class="step_num">04</div>'
		+ '					<div class="step_info">'
		+ '						<div class="step_name">생산 / 조립</div>'
		+ '						<div class="step_badge" id="stepBadge4"><span class="badge status_wait">대기</span></div>'
		+ '					</div>'
		+ '				</div>'
		+ '				<div class="step_metric" id="stepMetric4">작업일보 현황</div>'
		+ '				<div class="step_actions">'
		+ '					<button type="button" class="btn btn-xs btn_flow_jump" onclick="jumpToTaskFlowMenu(\'/static/html/user/pm/pm01/PM0101M01.html\', \'PM0101M01\')" title="작업일보관리로 이동">작업일보</button>'
		+ '					<button type="button" class="btn btn-xs btn_flow_jump" onclick="jumpToTaskFlowMenu(\'/static/html/user/pm/pm51/PM5101M01.html\', \'PM5101M01\')" title="출장신청으로 이동">출장신청서관리</button>'
		+ '					<button type="button" class="btn btn-xs btn_flow_jump" onclick="jumpToTaskFlowMenu(\'/static/html/user/pm/pm51/PM5102M01.html\', \'PM5102M01\')" title="출장복명서관리로 이동">출장복명서관리</button>'
		+ '				</div>'
		+ '			</div>'
		+ '			<div class="flow_arrow"><i class="fa fa-angle-right"></i></div>'
		+ '			<div class="flow_step_card" id="flowStep5" data-step="5">'
		+ '				<div class="step_header">'
		+ '					<div class="step_num">05</div>'
		+ '					<div class="step_info">'
		+ '						<div class="step_name">품질 / 검사</div>'
		+ '						<div class="step_badge" id="stepBadge5"><span class="badge status_wait">대기</span></div>'
		+ '					</div>'
		+ '				</div>'
		+ '				<div class="step_metric" id="stepMetric5">검사실적 / 이슈</div>'
		+ '				<div class="step_actions">'
		+ '					<button type="button" class="btn btn-xs btn_flow_jump" onclick="jumpToTaskFlowMenu(\'/static/html/user/qm/qm01/QM0101M01.html\', \'QM0101M01\')" title="발주요청관리로 이동">발주요청관리</button>'
		+ '					<button type="button" class="btn btn-xs btn_flow_jump" onclick="jumpToTaskFlowMenu(\'/static/html/user/bm/bm19/BM1901M01.html\', \'BM1901M01\')" title="문제/이슈관리로 이동">문제/이슈관리</button>'
		+ '					<button type="button" class="btn btn-xs btn_flow_jump" onclick="jumpToTaskFlowMenu(\'/static/html/user/wb/wb07/WB0701M01.html\', \'WB0701M01\')" title="WBS 일정관리(PM)로 이동">WBS 일정관리(PM)</button>'
		+ '				</div>'
		+ '			</div>'
		+ '			<div class="flow_arrow"><i class="fa fa-angle-right"></i></div>'
		+ '			<div class="flow_step_card" id="flowStep6" data-step="6">'
		+ '				<div class="step_header">'
		+ '					<div class="step_num">06</div>'
		+ '					<div class="step_info">'
		+ '						<div class="step_name">출하 / 정산</div>'
		+ '						<div class="step_badge" id="stepBadge6"><span class="badge status_wait">대기</span></div>'
		+ '					</div>'
		+ '				</div>'
		+ '				<div class="step_metric" id="stepMetric6">출하요청 / 매출</div>'
		+ '				<div class="step_actions">'
		+ '					<button type="button" class="btn btn-xs btn_flow_jump" onclick="jumpToTaskFlowMenu(\'/static/html/user/pm/pm03/PM0301M01.html\', \'PM0301M01\')" title="출하요청관리로 이동">출하요청관리</button>'
		+ '					<button type="button" class="btn btn-xs btn_flow_jump" onclick="jumpToTaskFlowMenu(\'/static/html/user/cr/cr10/CR1001M01.html\', \'CR1001M01\')" title="물류진행요청관리로 이동">물류진행요청관리</button>'
		+ '					<button type="button" class="btn btn-xs btn_flow_jump" onclick="jumpToTaskFlowMenu(\'/static/html/user/cr/cr08/CR0801M01.html\', \'CR0801M01\')" title="계산서관리로 이동">매출/계산서</button>'
		+ '				</div>'
		+ '			</div>'
		+ '		</div>'
		+ '	</div>'
		+ '</div>';
}

function mountTaskFlowWidget(mountSelector) {
	if ($('#taskFlowWidget').length > 0) return;
	var $mount = $(mountSelector || '#taskFlowWidgetMount');
	if ($mount.length > 0) {
		$mount.html(getTaskFlowWidgetTemplateHtml());
	} else {
		var $contents = $('#main_area > .contents').first();
		if ($contents.length > 0) {
			$contents.prepend(getTaskFlowWidgetTemplateHtml());
		}
	}
}

function escapeTfHtml(str) {
	if (str === null || str === undefined) return '';
	return String(str)
		.replace(/&/g, '&amp;')
		.replace(/</g, '&lt;')
		.replace(/>/g, '&gt;')
		.replace(/"/g, '&quot;')
		.replace(/'/g, '&#39;');
}

function initTaskFlowRichDropdownEvents() {
	$(document).off('click.tfRichTrigger').on('click.tfRichTrigger', '#taskFlowWidget .eto-rich-trigger', function(e) {
		e.stopPropagation();
		var $wrap = $(this).closest('.eto-rich-select');
		var willOpen = !$wrap.hasClass('open');
		$('#taskFlowWidget .eto-rich-select').removeClass('open').find('.eto-rich-trigger').attr('aria-expanded', 'false');
		if (willOpen) {
			$wrap.addClass('open').find('.eto-rich-trigger').attr('aria-expanded', 'true');
		}
	});
	$(document).off('click.tfRichItem').on('click.tfRichItem', '#taskFlowWidget .eto-rich-item', function(e) {
		e.stopPropagation();
		var $item = $(this);
		var val = $item.attr('data-value');
		var $wrap = $item.closest('.eto-rich-select');
		var $sel = $($wrap.attr('data-target-select'));
		$wrap.removeClass('open').find('.eto-rich-trigger').attr('aria-expanded', 'false');
		if ($sel.length && $sel.val() !== val) {
			$sel.val(val).trigger('change');
		}
	});
	$(document).off('mousedown.tfRichClose').on('mousedown.tfRichClose', function(e) {
		if ($(e.target).closest('#taskFlowWidget .eto-rich-select').length === 0) {
			$('#taskFlowWidget .eto-rich-select').removeClass('open').find('.eto-rich-trigger').attr('aria-expanded', 'false');
		}
	});
	$(document).off('keydown.tfRichEsc').on('keydown.tfRichEsc', function(e) {
		if (e.keyCode === 27) {
			$('#taskFlowWidget .eto-rich-select').removeClass('open').find('.eto-rich-trigger').attr('aria-expanded', 'false');
		}
	});
}

function buildTaskFlowRichBadgeHtml(meta) {
	if (!meta || !meta.hasSummary) return '';
	var theme = TF_STATUS_THEME[meta.statusType] || TF_STATUS_THEME.NONE;
	var html = '<span class="wbs-badge ' + theme.badgeCls + '"><span class="wbs-status-dot ' + theme.dotCls + '"></span>' + escapeTfHtml(meta.statusText || theme.label) + '</span>';
	if (meta.hasIssData) {
		if (meta.tot > 0) {
			var issCls = meta.uncomp > 0 ? 'badge-issue-has-unresolved' : 'badge-issue-all-resolved';
			html += '<span class="badge-issue ' + issCls + '">문제 ' + meta.uncomp + '/' + meta.tot + '</span>';
		} else {
			html += '<span class="wbs-badge wbs-badge-gray">문제 0</span>';
		}
	}
	return html;
}

function applyTaskFlowOptionBadgeStyle($opt, baseText, statusInfo, issUncompCnt, issTotCnt) {
	var st = statusInfo || { type: 'NONE', text: '계획없음' };
	var theme = TF_STATUS_THEME[st.type] || TF_STATUS_THEME.NONE;
	var color = theme.color;
	var hasIssData = (issTotCnt !== undefined && issTotCnt !== null && issTotCnt !== '');
	var tot = hasIssData ? (parseInt(issTotCnt, 10) || 0) : 0;
	var uncomp = hasIssData ? (parseInt(issUncompCnt, 10) || 0) : 0;
	if (st.type === 'NONE' && uncomp > 0) {
		color = '#d9534f';
	}
	var issuePart = hasIssData ? (' | 문제 ' + (tot > 0 ? (uncomp + '/' + tot + '건') : '0건')) : '';
	var fullText = baseText + ' [' + st.text + issuePart + ']';
	$opt.text(fullText).attr('title', fullText).data('badgeMeta', {
		hasSummary: true,
		baseText: baseText,
		statusType: st.type,
		statusText: st.text,
		color: color,
		hasIssData: hasIssData,
		uncomp: uncomp,
		tot: tot
	});
}

function renderTaskFlowRichDropdown($sel) {
	if (!$sel || !$sel.length) return;
	var $wrap = $sel.closest('.eto-rich-select');
	if (!$wrap.length) return;
	var curVal = $sel.val();
	var $selOpt = $sel.find('option:selected');
	var selMeta = $selOpt.data('badgeMeta');
	var selLabel = selMeta ? selMeta.baseText : ($selOpt.data('baseText') || $selOpt.text() || '');
	var $trigger = $wrap.find('.eto-rich-trigger');
	$trigger.find('.eto-rich-label').text(selLabel).attr('title', $selOpt.text() || selLabel)
		.css('color', (selMeta && selMeta.hasSummary && selMeta.color) ? selMeta.color : '#1e293b');
	$trigger.find('.eto-rich-badges').html(buildTaskFlowRichBadgeHtml(selMeta));

	var $menu = $wrap.find('.eto-rich-menu');
	$menu.empty();
	$sel.find('option').each(function() {
		var $o = $(this);
		var oVal = $o.attr('value');
		if (typeof oVal === 'undefined') oVal = $o.val();
		var oMeta = $o.data('badgeMeta');
		var oText = oMeta ? oMeta.baseText : ($o.data('baseText') || $o.text() || '');
		var isSel = (String(oVal) === String(curVal));
		var $row = $('<div class="eto-rich-item' + (isSel ? ' is-selected' : '') + '" role="option"></div>').attr('data-value', oVal);
		var $txt = $('<span class="eto-rich-item-text"></span>').text(oText).attr('title', $o.text() || oText);
		if (oMeta && oMeta.hasSummary && oMeta.color) {
			$txt.css('color', oMeta.color);
		}
		var $bdg = $('<span class="eto-rich-badges"></span>').html(buildTaskFlowRichBadgeHtml(oMeta));
		$row.append($txt).append($bdg);
		$menu.append($row);
	});
}

function initTaskFlowSearchControls() {
	var $coSel = $('#taskFlowCoCdSelect');
	if ($coSel.length) {
		$coSel.empty();
		if (typeof setCommonSelect === 'function') {
			try { setCommonSelect($coSel); } catch (e) {}
		}
		if ($coSel.find('option').length === 0) {
			$coSel.append('<option value="GUN">건양ITT</option><option value="TRN">트루넷</option>');
		}
		if ($coSel.find('option[value="GUN"]').length > 0) {
			$coSel.val('GUN');
		} else {
			$coSel.prop('selectedIndex', 0);
		}
	}
	var defEnd = moment().format('YYYY-MM-DD');
	var defStrt = moment().subtract(9, 'months').startOf('month').format('YYYY-MM-DD');
	$('#taskFlowStrtDt').val(defStrt);
	$('#taskFlowEndDt').val(defEnd);
	$('#taskFlowEndYnSelect').val('');

	$('#taskFlowStrtDt, #taskFlowEndDt').off('click.tfDrp').on('click.tfDrp', function() {
		if (typeof DateRangePicker === 'undefined' || !DateRangePicker.open) return;
		var rect = this.getBoundingClientRect();
		DateRangePicker.open({
			title: '수주기간 선택',
			startLabel: '수주 시작일',
			endLabel: '수주 종료일',
			startDt: $('#taskFlowStrtDt').val() || null,
			endDt: $('#taskFlowEndDt').val() || null,
			px: Math.max(10, Math.round(rect.left - 240)),
			py: Math.max(10, Math.round(rect.bottom)),
			excludeSelectors: ['#taskFlowStrtDt', '#taskFlowEndDt'],
			onApply: function(sd, ed) {
				$('#taskFlowStrtDt').val(sd);
				$('#taskFlowEndDt').val(ed);
				onTaskFlowCondChange();
			}
		});
	});
}

function getTaskFlowSearchCond() {
	return {
		coCd: $('#taskFlowCoCdSelect').val() || 'GUN',
		strtDt: $.trim($('#taskFlowStrtDt').val() || ''),
		endDt: $.trim($('#taskFlowEndDt').val() || ''),
		endYn: $('#taskFlowEndYnSelect').val() || ''
	};
}

function clearTaskFlowSummaryView() {
	curTaskFlowProject = null;
	$('#tfOrdrsNo').text('-');
	$('#tfClntNm').text('-');
	$('#tfClntPjtNm').text('-');
	$('#tfCtrtNm').text('-').attr('title', '');
	$('#tfDudt').text('-');
	$('#tfAppBadge').css('background-color', '#777').text('미확인');
	for (var s = 1; s <= 6; s++) {
		$('#flowStep' + s).removeClass('step_done step_prog step_warn');
		$('#stepBadge' + s).html('<span class="badge status_wait">대기</span>');
		$('#stepMetric' + s).text('조회 대기');
	}
}

function onTaskFlowCondChange() {
	var cond = getTaskFlowSearchCond();
	if (cond.strtDt && !/^\d{4}-\d{2}-\d{2}$/.test(cond.strtDt)) {
		alert('시작일자 형식을 확인해 주세요. (YYYY-MM-DD)');
		$('#taskFlowStrtDt').focus();
		return;
	}
	if (cond.endDt && !/^\d{4}-\d{2}-\d{2}$/.test(cond.endDt)) {
		alert('종료일자 형식을 확인해 주세요. (YYYY-MM-DD)');
		$('#taskFlowEndDt').focus();
		return;
	}
	if (cond.strtDt && cond.endDt && cond.strtDt > cond.endDt) {
		alert('수주 시작일은 종료일보다 클 수 없습니다.');
		$('#taskFlowStrtDt').focus();
		return;
	}
	clearTaskFlowSummaryView();
	loadTaskFlowProjectList();
}

function openFullEtoProcessMap() {
	var cond = getTaskFlowSearchCond();
	var qs = 'coCd=' + encodeURIComponent(cond.coCd)
		+ '&strtDt=' + encodeURIComponent(cond.strtDt)
		+ '&endDt=' + encodeURIComponent(cond.endDt)
		+ '&endYn=' + encodeURIComponent(cond.endYn);
	if (curTaskFlowProject && curTaskFlowProject.ordrsClntCd) {
		qs += '&ordrsClntCd=' + encodeURIComponent(curTaskFlowProject.ordrsClntCd);
	}
	window.open('/static/html/admin/cm/cm10/CM1006M01.html?' + qs, 'EtoFullMapWindow', 'width=1650,height=950,scrollbars=yes,resizable=yes');
}

function initTaskFlowNavigator(mountSelector) {
	mountTaskFlowWidget(mountSelector);

	// 이전 접힘 상태 복원
	var isCollapsed = localStorage.getItem('taskFlowWidget_collapsed') === 'Y';
	if (isCollapsed) {
		$('#taskFlowBody').hide();
		$('#taskFlowToggleBtn i').removeClass('fa-chevron-up').addClass('fa-chevron-down');
	}

	initTaskFlowRichDropdownEvents();
	initTaskFlowSearchControls();
	if (typeof adjustGridHeight === 'function') {
		adjustGridHeight();
	}
	loadTaskFlowProjectList();
}

// 최근 수주 프로젝트 목록 로드 (CM1003M01과 동일하게 selectTaskFlowProjectSummaryList 기준 1회 조회)
function loadTaskFlowProjectList() {
	var cond = getTaskFlowSearchCond();
	var paramObj = {
		"coCd"        : cond.coCd,
		"searchValue" : "",
		"strtDt"      : cond.strtDt,
		"endDt"       : cond.endDt,
		"endYn"       : cond.endYn
	};
	postAjax("/user/cm/cm10/selectTaskFlowProjectSummaryList", paramObj, null, function(data) {
		var list = (data && data.resultList) ? data.resultList : [];
		var $sel = $('#taskFlowProjectSelect');
		$sel.empty();
		$sel.append($('<option></option>').val('').text('선택').data('baseText', '선택'));

		if (list.length === 0) {
			$sel.val('');
			renderTaskFlowRichDropdown($sel);
			clearTaskFlowSummaryView();
			return;
		}

		// 이전에 사용자가 선택한 수주번호 복원 시도 (미선택 시 기본 "선택" 유지)
		var lastKey = sessionStorage.getItem('lastSelectedOrdrsKey');
		if (!lastKey) {
			try {
				var flowCtx = (typeof getFlowContext === 'function') ? getFlowContext() : null;
				if (flowCtx && flowCtx.ordrsNo) {
					lastKey = flowCtx.ordrsNo + '|' + (flowCtx.histNo || '0') + '|' + (flowCtx.coCd || cond.coCd || '');
				} else {
					var userIdKey = 'lastSelectedOrdrsKey_' + ((typeof jwt !== 'undefined' && jwt && jwt.userId) ? jwt.userId : 'USER');
					lastKey = localStorage.getItem(userIdKey);
				}
			} catch (e) {}
		}
		var selectedIdx = 0;

		$.each(list, function(idx, item) {
			var key = item.ordrsNo + '|' + item.histNo + '|' + item.coCd;
			var clnt = item.clntNm || '고객사미정';
			var pjtPart = item.clntPjtNm ? (' [' + item.clntPjtNm + ']') : '';
			var ctrt = item.ctrtNm || item.clntPjtNm || '프로젝트';
			var baseText = '[' + item.ordrsNo + '-' + item.histNo + '] ' + clnt + pjtPart + ' | ' + ctrt;
			var $opt = $('<option></option>').val(key).text(baseText).data('item', item).data('baseText', baseText);
			if (item.wbsStatus || item.issTotCnt !== undefined) {
				var stType = String(item.wbsStatus || 'NONE').toUpperCase();
				var stTheme = TF_STATUS_THEME[stType] || TF_STATUS_THEME.NONE;
				var stText = stTheme.label;
				if (stType === 'NONE' && Number(item.salesCnt || 0) === 0) {
					stText = 'WBS미등록';
				} else if (stType === 'ONGOING_DELAY' && Number(item.wbsMaxDelayDays || 0) > 0) {
					stText += '(+' + item.wbsMaxDelayDays + '일)';
				}
				applyTaskFlowOptionBadgeStyle($opt, baseText, { type: stType, text: stText }, item.issUncompCnt, item.issTotCnt);
			}
			if (lastKey && lastKey === key) {
				selectedIdx = idx + 1;
			}
			$sel.append($opt);
		});

		$sel.prop('selectedIndex', selectedIdx);
		renderTaskFlowRichDropdown($sel);
		var selVal = $sel.val();
		if (selVal) {
			onTaskFlowProjectChange(selVal);
		} else {
			clearTaskFlowSummaryView();
		}
	});
}

// 수주 프로젝트 변경 이벤트
function onTaskFlowProjectChange(val) {
	if (!val) {
		renderTaskFlowRichDropdown($('#taskFlowProjectSelect'));
		try {
			sessionStorage.removeItem('lastSelectedOrdrsKey');
			sessionStorage.removeItem('lastSelectedSalesCd');
			sessionStorage.removeItem('etoPinnedSearchKeyword');
			sessionStorage.removeItem('GBS:taskFlowContext');
			var userIdKey = 'lastSelectedOrdrsKey_' + ((typeof jwt !== 'undefined' && jwt && jwt.userId) ? jwt.userId : 'USER');
			localStorage.removeItem(userIdKey);
		} catch (e) {}
		clearTaskFlowSummaryView();
		if (typeof clearFlowContext === 'function') {
			clearFlowContext();
		}
		return;
	}
	var tokens = val.split('|');
	var ordrsNo = tokens[0];
	var histNo = tokens[1];
	var coCd = tokens[2] || getTaskFlowSearchCond().coCd;

	renderTaskFlowRichDropdown($('#taskFlowProjectSelect'));
	sessionStorage.setItem('lastSelectedOrdrsKey', val);
	try {
		var userIdKey = 'lastSelectedOrdrsKey_' + ((typeof jwt !== 'undefined' && jwt && jwt.userId) ? jwt.userId : 'USER');
		localStorage.setItem(userIdKey, val);
	} catch (e) {}

	var $opt = $('#taskFlowProjectSelect option:selected');
	curTaskFlowProject = $opt.data('item') || { ordrsNo: ordrsNo, histNo: histNo, coCd: coCd };
	if (typeof setFlowContext === 'function') {
		setFlowContext(ordrsNo, histNo || '0', curTaskFlowProject.clntNm || '', curTaskFlowProject.coCd || getTaskFlowSearchCond().coCd);
	}

	loadTaskFlowStepStatus(ordrsNo, histNo, coCd);
}

// 수주 프로젝트 6단계 상태 및 건수 집계 로드 (03.구매/외주는 회사코드 TRN 기준)
function loadTaskFlowStepStatus(ordrsNo, histNo, coCd) {
	var paramObj = {
		"ordrsNo" : ordrsNo,
		"histNo"  : histNo,
		"coCd"    : coCd,
		"smCoCd"  : "TRN"
	};
	postAjax("/user/cm/cm10/selectTaskFlowStepStatusCm1002", paramObj, null, function(data) {
		var st = data && data.result;
		if (!st) {
			st = curTaskFlowProject || {};
		}

		// 1. 요약 바 갱신
		$('#tfOrdrsNo').text((st.ordrsNo || ordrsNo) + '-' + (st.histNo || histNo));
		$('#tfClntNm').text(st.clntNm || '-');
		$('#tfCtrtNm').text(st.ctrtNm || '-').attr('title', st.ctrtNm || '');
		$('#tfClntPjtNm').text(st.clntPjtNm || '-');
		$('#tfDudt').text(st.dudt || '-');

		var isAppY = (st.step1AppYn === 'Y');
		var $appBadge = $('#tfAppBadge');
		if (isAppY) {
			$appBadge.css('background-color', '#5cb85c').text('승인완료');
		} else {
			$appBadge.css('background-color', '#f0ad4e').text('결재진행');
		}

		// 2. 6단계 카드 갱신
		// Step 1: 수주/원가
		var $c1 = $('#flowStep1');
		var $b1 = $('#stepBadge1');
		var $m1 = $('#stepMetric1');
		if (isAppY) {
			$c1.removeClass('step_prog step_wait').addClass('step_done');
			$b1.html('<span class="badge status_done">승인완료</span>');
			$m1.text('목표원가 승인 (확정:' + (st.step1CnfmYn || 'Y') + ')');
		} else {
			$c1.removeClass('step_done step_wait').addClass('step_prog');
			$b1.html('<span class="badge status_prog">진행중</span>');
			$m1.text('수주 목표원가 결재진행');
		}

		// Step 2: 일정/설계
		var $c2 = $('#flowStep2');
		var $b2 = $('#stepBadge2');
		var $m2 = $('#stepMetric2');
		var wbsCnt = Number(st.step2WbsCnt || 0);
		var bomCnt = Number(st.step2BomCnt || 0);
		if (wbsCnt > 0 || bomCnt > 0) {
			$c2.removeClass('step_prog step_wait').addClass('step_done');
			$b2.html('<span class="badge status_done">등록완료</span>');
			$m2.text('WBS ' + wbsCnt + '건 / BOM ' + bomCnt + '건');
		} else if (isAppY) {
			$c2.removeClass('step_done step_wait').addClass('step_prog');
			$b2.html('<span class="badge status_prog">설계대기</span>');
			$m2.text('일정/BOM 미등록');
		} else {
			$c2.removeClass('step_done step_prog').addClass('step_wait');
			$b2.html('<span class="badge status_wait">대기</span>');
			$m2.text('선행 결재 대기');
		}

		// Step 3: 구매/외주
		var $c3 = $('#flowStep3');
		var $b3 = $('#stepBadge3');
		var $m3 = $('#stepMetric3');
		var reqCnt = Number(st.step3ReqCnt || 0);
		var ordrgCnt = Number(st.step3OrdrgCnt || 0);
		if (ordrgCnt > 0) {
			$c3.removeClass('step_prog step_wait').addClass('step_done');
			$b3.html('<span class="badge status_done">발주진행</span>');
			$m3.text('발주 ' + ordrgCnt + '건 (요청 ' + reqCnt + '건)');
		} else if (reqCnt > 0) {
			$c3.removeClass('step_done step_wait').addClass('step_prog');
			$b3.html('<span class="badge status_prog">요청완료</span>');
			$m3.text('발주요청 ' + reqCnt + '건 (발주대기)');
		} else {
			$c3.removeClass('step_done step_prog').addClass('step_wait');
			$b3.html('<span class="badge status_wait">대기</span>');
			$m3.text('발주 내역 없음');
		}

		// Step 4: 생산/조립
		var $c4 = $('#flowStep4');
		var $b4 = $('#stepBadge4');
		var $m4 = $('#stepMetric4');
		var workCnt = Number(st.step4WorkCnt || 0);
		if (workCnt > 0) {
			$c4.removeClass('step_done step_wait').addClass('step_prog');
			$b4.html('<span class="badge status_prog">생산진행</span>');
			$m4.text('작업일보 ' + workCnt + '건 실적');
		} else {
			$c4.removeClass('step_done step_prog').addClass('step_wait');
			$b4.html('<span class="badge status_wait">대기</span>');
			$m4.text('작업일보 미등록');
		}

		// Step 5: 품질/검사
		var $c5 = $('#flowStep5');
		var $b5 = $('#stepBadge5');
		var $m5 = $('#stepMetric5');
		var qmCnt = Number(st.step5QmCnt || 0);
		if (qmCnt > 0) {
			$c5.removeClass('step_prog step_wait').addClass('step_done');
			$b5.html('<span class="badge status_done">검사완료</span>');
			$m5.text('검사실적 ' + qmCnt + '건 완료');
		} else {
			$c5.removeClass('step_done step_prog').addClass('step_wait');
			$b5.html('<span class="badge status_wait">대기</span>');
			$m5.text('품질검사 실적 없음');
		}

		// Step 6: 출하/정산
		var $c6 = $('#flowStep6');
		var $b6 = $('#stepBadge6');
		var $m6 = $('#stepMetric6');
		var trCnt = Number(st.step6TrCnt || 0);
		if (trCnt > 0) {
			$c6.removeClass('step_prog step_wait').addClass('step_done');
			$b6.html('<span class="badge status_done">출하완료</span>');
			$m6.text('출하요청 ' + trCnt + '건 등록');
		} else {
			$c6.removeClass('step_done step_prog').addClass('step_wait');
			$b6.html('<span class="badge status_wait">대기</span>');
			$m6.text('출하 내역 없음');
		}
	});
}

// 위젯 새로고침
function reloadTaskFlowStatus() {
	loadTaskFlowProjectList();
}

// 위젯 접기/펼치기 토글
function toggleTaskFlowWidget() {
	var $body = $('#taskFlowBody');
	var $icon = $('#taskFlowToggleBtn i');
	var isHidden = $body.is(':hidden');

	if (isHidden) {
		$body.slideDown(150, function() {
			$icon.removeClass('fa-chevron-down').addClass('fa-chevron-up');
			localStorage.setItem('taskFlowWidget_collapsed', 'N');
			if (typeof adjustGridHeight === 'function') {
				adjustGridHeight();
			}
		});
	} else {
		$body.slideUp(150, function() {
			$icon.removeClass('fa-chevron-up').addClass('fa-chevron-down');
			localStorage.setItem('taskFlowWidget_collapsed', 'Y');
			if (typeof adjustGridHeight === 'function') {
				adjustGridHeight();
			}
		});
	}
}

// 프로세스 단계 화면으로 점프 (컨텍스트 파라미터 전달)
function jumpToTaskFlowMenu(url, pgmId) {
	var selVal = $('#taskFlowProjectSelect').val();
	var ordrsNo = "";
	var histNo = "";
	var coCd = getTaskFlowSearchCond().coCd;

	if (selVal) {
		var tokens = selVal.split('|');
		ordrsNo = tokens[0] || "";
		histNo = tokens[1] || "";
		coCd = tokens[2] || coCd;
	}

	// 글로벌 세션 스토리지에 작업 컨텍스트 저장
	try {
		var ctx = {
			ordrsNo: ordrsNo,
			histNo: histNo,
			coCd: coCd,
			clntNm: $('#tfClntNm').text() !== '-' ? $('#tfClntNm').text() : '',
			ctrtNm: $('#tfCtrtNm').text() !== '-' ? $('#tfCtrtNm').text() : ''
		};
		sessionStorage.setItem('GBS:taskFlowContext', JSON.stringify(ctx));
	} catch (e) {}

	// URL 파라미터 조합 (ordrsNo, histNo 복합키 보장)
	var targetUrl = url;
	if (ordrsNo) {
		var separator = targetUrl.indexOf('?') > -1 ? '&' : '?';
		targetUrl += separator + 'ordrsNo=' + encodeURIComponent(ordrsNo) + '&histNo=' + encodeURIComponent(histNo) + '&coCd=' + encodeURIComponent(coCd);
	}

	location.href = targetUrl;
}
