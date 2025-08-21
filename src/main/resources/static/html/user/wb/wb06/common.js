// ===== GanttCommon : 유틸/툴팁/컨텍스트메뉴/룰 =====
// 즉시 실행 함수 표현식(IIFE, Immediately Invoked Function Expression) 패턴
// 함수 안에 변수를 정의하면 var, let, const 모두 지역 변수가 됨
// 내부에서 root === window
// 전역 오염을 막으면서도, 필요시 root.xxx 형태로 API를 공개
(function (root) {
	'use strict';

	const MS = 86400000;
	const SVGNS = 'http://www.w3.org/2000/svg';

	// 날짜 유틸
	const sod = d => { const x = new Date(d); x.setHours(0, 0, 0, 0); return x; };
	const fmt = d => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
	const parse = s => sod(new Date(s));
	const days = (a, b) => Math.round((+sod(b) - +sod(a)) / MS);
	const clampDate = (d, s, e) => new Date(Math.min(Math.max(+d, +s), +e));

	// 좌표 변환 팩토리
	function makeXFromDate(state) { return d => days(state.viewStart, sod(d)) * state.pxPerDay; }
	function makeDateFromX(state) { return x => sod(new Date(+state.viewStart + Math.round(x / state.pxPerDay) * MS)); }

	// SVG 유틸
	function makeSvgEl(name, attrs = {}) {
		const el = document.createElementNS(SVGNS, name);
		for (const [k, v] of Object.entries(attrs)) el.setAttribute(k, v);
		return el;
	}

	// CSS var px
	function cssVarPx(name) {
		const v = getComputedStyle(document.documentElement).getPropertyValue(name).trim();
		return parseFloat(v);
	}

	// 툴팁
	const $tt = $('#ganttTooltip');
	const tipPad = 14;
	const showTip = (x, y, text) => $tt.text(text).css({ left: x + tipPad, top: y + tipPad, display: 'block' });
	const moveTip = (x, y, text) => $tt.text(text).css({ left: x + tipPad, top: y + tipPad });
	const hideTip = () => $tt.hide();
	const fmtRange = (s, e, name, daysFn) => `${name} : ${fmt(s)} ~ ${fmt(e)} (${daysFn(s, e) + 1}일)`;

	// 컨텍스트 메뉴(공통 매니저)
	function createContextMenu(selector) {
		const $ctx = $(selector);
		const handlers = {}; // { action: [fn, ...] }
		function open(ev, payload = {}) {
			ev.preventDefault();
			hide();

			// ✅ payload 보강: 바/트랙에서 salesCd, cat, $track 채워넣기
			const $t = payload.$track || $(ev.target).closest('.row-track');
			const salesCd = payload.salesCd ?? ($t && $t.data('row'));
			const cat     = payload.cat     ?? ($t && $t.data('cat'));
			const enriched = { ...payload, $track: $t, salesCd, cat };

			// 위치 계산 (hidden 상태에서 width/height 0 방지용 측정)
			const pad = 4;
			$ctx.css({ display: 'block', visibility: 'hidden' }); // 임시 표시 후 크기 측정
			const menuW = $ctx.outerWidth() || 200;
			const menuH = $ctx.outerHeight() || 160;
			let x = ev.clientX, y = ev.clientY;
			const vw = window.innerWidth, vh = window.innerHeight;
			if (x + menuW + pad > vw) x = vw - menuW - pad;
			if (y + menuH + pad > vh) y = vh - menuH - pad;

			// 최종 표시
			$ctx.data('payload', enriched)
				.css({ left: x, top: y, display: 'block', visibility: 'visible', position: 'fixed', zIndex: 10050 });

			// 바깥 클릭/ESC/스크롤로 닫기
			setTimeout(() => {
				$(document).on('mousedown.gctx', (e) => { if (!$(e.target).closest(selector).length) hide(); });
				$(document).on('contextmenu.gctx', (e) => { if (!$(e.target).closest(selector).length) hide(); });
				$(document).on('keydown.gctx', (e) => { if (e.key === 'Escape') hide(); });
				$(window).on('scroll.gctx resize.gctx', hide);
			}, 0);
		}

		function hide() {
			$ctx.hide().removeData('payload');
			$(document).off('.gctx'); $(window).off('.gctx');
		}

		function on(action, handler) {
			// 1) 내부 핸들러 맵에도 등록
			(handlers[action] ||= []).push(handler);
			// 2) 메뉴 DOM 클릭 라우팅
			$ctx.off(`click.gctx-${action}`, `[data-action="${action}"]`);
			$ctx.on(`click.gctx-${action}`, `[data-action="${action}"]`, function (e) {
				const payload = $ctx.data('payload') || {};
				const keep = (handler(payload, e) === false); // 핸들러가 false를 리턴하면 keep
				if (!keep) hide();
			});
		}
			
		function trigger(action, payload = {}) {
			(handlers[action] || []).forEach(fn => {
				try { fn(payload); } catch (e) { console.error(e); }
			});
		}
			
		return { open, hide, on, trigger, el: $ctx, _handlers: handlers };
	}

	// 상태 룰(예시)
	function canDragOrResize(cat, item) {
		if (cat === 'PM') return item.confirmYn !== 'Y';
		if (cat === 'PLAN') return item.confirmYn === 'Y';
		if (cat === 'DO') return item.doneYn !== 'Y';
		return false;
	}
	function canDelete(cat, item) { return canDragOrResize(cat, item); }
	function canAddTask(cat, rowMeta) {
		if (cat === 'PM') 	return rowMeta.pmCloseYn !== 'Y';	// Y=확정 → 추가 불가
		if (cat === 'PLAN') return rowMeta.pmCloseYn === 'Y';	// N=확정 → 추가, 수정 불가
		if (cat === 'DO') 	return true;						// DO는 항상 가능
		return false;
	}

	root.GanttCommon = {
		MS, sod, fmt, parse, days, clampDate,
		makeXFromDate, makeDateFromX, makeSvgEl,
		cssVarPx, showTip, moveTip, hideTip, fmtRange,
		createContextMenu,
		rules: { canDragOrResize, canDelete, canAddTask }
	};
})(window);
