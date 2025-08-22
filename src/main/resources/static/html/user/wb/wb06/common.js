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
//	const showTip = (x, y, text) => $tt.text(text).css({ left: x + tipPad, top: y + tipPad, display: 'block' });
	const showTip = (x, y, html) => $tt.html(html).css({ left: x + tipPad, top: y + tipPad, display: 'block' });
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

			// ▼ 1) 전체 화면 투명 백드롭 생성 (메뉴 아래, 콘텐츠 위)
			const $bd = $('<div class="ctx-backdrop" />').appendTo(document.body);
			// 여백 클릭/터치/우클릭 모두 → 메뉴 닫기
			$bd.on('mousedown.gctx click.gctx contextmenu.gctx touchstart.gctx', function (e) {
			  e.preventDefault();
			  hide();
			});
			// 메뉴 객체에 백드롭 보관 (hide 시 제거)
			$ctx.data('__bd', $bd);
			
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

			// ★ 바깥 클릭/우클릭/ESC/스크롤을 capture 단계에서 감지해 닫기

			// ★ 바깥 클릭/우클릭/ESC/스크롤을 capture 단계에서 감지해 닫기
			const onDocClickCap = (e) => { if (!e.target.closest(selector)) hide(); };
			const onCtxMenuCap  = (e) => { if (!e.target.closest(selector)) hide(); };
			const onKeydownCap  = (e) => { if (e.key === 'Escape') hide(); };
			const onScrollWin   = ()  => hide();
			const onResizeWin   = ()  => hide();
			document.addEventListener('click',       onDocClickCap, true);
			document.addEventListener('mousedown',   onDocClickCap, true);
			document.addEventListener('contextmenu', onCtxMenuCap,  true);
			document.addEventListener('keydown',     onKeydownCap,  true);
			window.addEventListener('scroll',  onScrollWin, { passive:true });
			window.addEventListener('resize',  onResizeWin);
			// 해제를 위해 핸들 보관
			$ctx.data('__cap', { onDocClickCap, onCtxMenuCap, onKeydownCap, onScrollWin, onResizeWin });

		}

		function hide() {
			// 캡처 리스너 해제
			const cap = $ctx.data('__cap');
			if (cap) {
				document.removeEventListener('click',       cap.onDocClickCap, true);
				document.removeEventListener('mousedown',   cap.onDocClickCap, true);
				document.removeEventListener('contextmenu', cap.onCtxMenuCap,  true);
				document.removeEventListener('keydown',     cap.onKeydownCap,  true);
				window.removeEventListener('scroll', cap.onScrollWin);
				window.removeEventListener('resize', cap.onResizeWin);
				$ctx.removeData('__cap');
			}
			$ctx.hide().removeData('payload');
			// (호환용) 기존 네임스페이스 리스너도 제거
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

		// 드래그로 스크롤 (좌/우만 또는 상/하만 - 축 고정)
		(function enableFreePan(){
			const $viewport = $('#timeline');
			let panning=false, id=null, sx=0, sy=0, sl0=0, st0=0;
			
			$(document).on('pointerdown', '.group-box', function(e){
				const isLeft = e.pointerType === 'mouse' ? e.button === 0 : true;
				if (!isLeft) return;
				if ($(e.target).closest('.bar, .handle').length) return;
				
				panning = true; id = e.pointerId || 1;
				sx = e.clientX; sy = e.clientY;
				sl0 = $viewport.scrollLeft(); st0 = $viewport.scrollTop();
				e.target.setPointerCapture?.(id);
				$viewport.addClass('panning');
				e.preventDefault();
			});
			
			$(document).on('pointermove', function(e){
				if (!panning) return;
				const dx = e.clientX - sx;
				const dy = e.clientY - sy;
				$viewport.scrollLeft(sl0 - dx);  // 좌우
				$viewport.scrollTop(st0 - dy);   // 상하
			});
			
			$(document).on('pointerup pointercancel', function(){
				if (!panning) return;
				panning=false; id=null; $viewport.removeClass('panning');
			});
		})();
		
	// 상태 룰(예시)
	function canDragOrResize(cat, item) {
		if (item.wbsPlanMngId != jwt.userId) return false;
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
