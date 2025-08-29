// app.js 최상단(또는 DOM 로드 후)에 한 번만
const ctx = GanttCommon.createContextMenu('#barContextMenu');
//window.ctx = ctx;
const UNSTACKED = { PM: new Set(), PLAN: new Set(), DO: new Set() }; // 여기에 있으면 "겹침 해제" 상태

//메뉴를 띄우는 순간(우클릭/더블클릭 직후) 그 위치의 날짜를 바로 계산해서 payload에 저장
function mapEventToDateOnTrack(ev, $track) {
    if (!$track || !$track.length || !ev) return null;

    // 1) 포인터 X (마우스/터치 호환)
    const clientX =
        (ev.clientX ?? ev.pageX ?? ev.originalEvent?.clientX ?? ev.originalEvent?.pageX);
    if (!Number.isFinite(clientX)) return null;

    // 2) 트랙 기준 로컬 X
    const rect = $track[0].getBoundingClientRect();
    const localX = Math.max(0, Math.floor(clientX - rect.left));

    // 3) px/day 확보
    let pxPerDay = Number(GanttApp?.state?.pxPerDay);
    if (!Number.isFinite(pxPerDay) || pxPerDay <= 0) {
        const cssVal = getComputedStyle(document.documentElement).getPropertyValue('--px-per-day').trim();
        pxPerDay = Number(cssVal.replace('px', '')) || 6;
    }

    // 4) day index + 클램프
    const start = GanttCommon.sod(GanttApp.state.viewStart);
    const end   = GanttCommon.sod(GanttApp.state.viewEnd);
    const total = GanttCommon.days(start, end); // 0..total
    let dayIdx  = Math.floor(localX / pxPerDay);
    dayIdx      = Math.max(0, Math.min(total, dayIdx));

    // 5) 날짜 산출
    return new Date(+start + dayIdx * GanttCommon.MS);
}

// 전역 래퍼
function openContextMenu(ev, payload = {}) {
	// payload 보강(트랙에서 salesCd/cat 취득)
	const $t = payload.$track || $(ev?.target).closest('.row-track');
	const enriched = {
			...payload,
			$track		: $t,
			salesCd		: payload.salesCd ?? $t?.data('row'),
			cat			: payload.cat     ?? $t?.data('cat'),
	};

    // ★ 오픈 시점 날짜를 즉시 계산해 저장
    const openDateObj = mapEventToDateOnTrack(ev, $t);
    if (openDateObj) {
        enriched.openDateObj = openDateObj;        // Date 객체
        enriched.openDate    = GanttCommon.fmt(openDateObj); // 'YYYY-MM-DD'
    }
    
	const $ctx = $('#barContextMenu');
	$ctx.data('payload', enriched);                     // 메뉴 라우터가 참조하는 payload 주입
	
	// 더블클릭 경로: 메뉴 띄우지 않고 edit 즉시 실행
	if (enriched.action === 'edit') {
		$ctx.find('[data-action="edit"]').trigger('click'); // 편집 클릭을 프로그램적으로 발생
		return;
	}
	// ★ PM 바에서의 일반 우클릭은 컨텍스트 메뉴 미표시
	if (enriched.cat === 'PM') {
	  return; // 아무 액션 없이 차단(더블클릭은 위에서 이미 처리됨)
	}
	
	// 일반 우클릭은 메뉴 표시
	ctx.open(ev, enriched);
}

//트랙 DOM 기준 로컬 X를 구하고, pxPerDay를 안전하게 숫자로 보정한 뒤, 뷰 범위 내로 클램프하여 날짜를 산출
function dateFromPointerOnTrack($track, pointer) {
    if (!$track || !$track.length) return null;

    const rect = $track[0].getBoundingClientRect();

    // 1) 로컬 X 계산: clientX 기준 (뷰포트 스크롤 무관)
    let clientX = Number(pointer?.clientX);
    if (!Number.isFinite(clientX) || clientX <= 0) {
        // 혹시 없으면 pageX - rect.left(page) 로 보정
        const pageX = Number(pointer?.pageX);
        const pageLeft = rect.left + window.pageXOffset;
        clientX = Number.isFinite(pageX) ? (pageX - pageLeft + rect.left) : rect.left;
    }
    const localX = Math.max(0, Math.floor(clientX - rect.left));

    // 2) pxPerDay 확보 (state → CSS 변수 순으로)
    let pxPerDay = Number(GanttApp?.state?.pxPerDay);
    if (!Number.isFinite(pxPerDay) || pxPerDay <= 0) {
        const cssVal = getComputedStyle(document.documentElement).getPropertyValue('--px-per-day').trim();
        pxPerDay = Number(cssVal.replace('px', '')) || 6;
    }

    // 3) 일 인덱스 및 클램프
    const start = GanttCommon.sod(GanttApp.state.viewStart);
    const end   = GanttCommon.sod(GanttApp.state.viewEnd);
    const total = GanttCommon.days(start, end);     // 0..total
    let dayIdx  = Math.floor(localX / pxPerDay);
    dayIdx      = Math.max(0, Math.min(total, dayIdx));

    // 4) 날짜 생성
    const ts = +start + (dayIdx * GanttCommon.MS);
    const d  = new Date(ts);

    // 디버깅 로그
    console.log('[dateFromPointerOnTrack]', {
        clientX, rectLeft: rect.left, localX, pxPerDay, dayIdx,
        viewStart: GanttCommon.fmt(start), calcDate: GanttCommon.fmt(d)
    });

    return d;
}


function clampToPlanBoundsIfNeeded(cat, salesCd, keyPrefix9, sDate, eDate) {
    if (cat !== 'PLAN') return { s: sDate, e: eDate };
    const b = GanttApp._makeCoreOptions(cat).getPlanBounds?.(keyPrefix9);
    if (!b) return { s: sDate, e: eDate };
    const s = (sDate < b.s) ? b.s : sDate;
    const e = (eDate > b.e) ? b.e : eDate;
    return { s, e };
}

// 전역 유틸: 항상 payload를 DOM에 저장
function showBarContext(ev, payload){
    const $menu = $('#barContextMenu');
    if (!$menu.length) return;

    // ★ DO 행이거나, item 이 없으면 "일정 추가" 숨김 (단일 토글로 일관 처리)
    const canInsert = !!(payload && payload.item) && payload.cat !== 'DO';
    $menu.find('li[data-action="insert"]').toggle(canInsert);

	$menu.find('li[data-action="complete"]').toggle(false);
	$menu.find('li[data-action="delete"]').toggle(false);
	$menu.find('li[data-action="cancle"]').toggle(false);
	if (payload.item.wbsPlanMngId == jwt.userId) {
	    if (payload.cat == 'DO') {
	    	if (payload && payload.item.expectMh && payload.item.doneYn!='Y' ) {
	    		$menu.find('li[data-action="complete"]').toggle(true);
	    		$menu.find('li[data-action="delete"]').toggle(true);
	    		$menu.find('li[data-action="cancle"]').toggle(false);
	    	} else {
	    		$menu.find('li[data-action="cancle"]').toggle(true);
	    	}
	    } else {
	    	$menu.find('li[data-action="delete"]').toggle(true);
	    }
	}
    
	const $ctx = $('#barContextMenu');
	if (!$ctx.length) { console.error('barContextMenu 엘리먼트 없음'); return; }
	
    const $t = payload.$track || $(ev?.target).closest('.row-track');

    // ★ 포인터/오픈 날짜가 없으면 여기서 보강
    if (!payload.pointer) {
        payload.pointer = { clientX: ev?.clientX ?? 0, clientY: ev?.clientY ?? 0, pageX: ev?.pageX ?? 0, pageY: ev?.pageY ?? 0 };
    }
    if (!payload.openDateObj) {
        const od = mapEventToDateOnTrack(ev, $t);
        if (od) {
            payload.openDateObj = od;
            payload.openDate = GanttCommon.fmt(od);
        }
    }

	// salesCd 보강(우클릭한 bar/track에서 가져옴)
	const salesCd = payload.$track?.data('row') ?? payload.salesCd;
	const merged  = { ...payload, salesCd };

	// 1) payload 저장 (★ 중요)
	$ctx.data('payload', merged);

	// 2) 항목 활성/비활성
	const editable = GanttCommon.rules?.canDragOrResize?.(merged.cat, merged.item) !== false;
	$ctx.find('[data-action="edit"],[data-action="duplicate"],[data-action="delete"]')
		.toggleClass('disabled', !editable);

	// 3) 위치 계산 후 표시
	const pad = 4, vw = window.innerWidth, vh = window.innerHeight;
	const mw = Math.max(160, $ctx.outerWidth() || 200);
	const mh = Math.max(80,  $ctx.outerHeight() || 160);
	let x = ev.clientX, y = ev.clientY;
	if (x + mw + pad > vw) x = vw - mw - pad;
	if (y + mh + pad > vh) y = vh - mh - pad;
	$ctx.css({ left:x, top:y, display:'block', position:'fixed', zIndex:10050 });

	// 4) 바깥클릭/ESC/스크롤로 닫기
	// ★ capture 단계 전역 닫기 리스너
	const onDocClickCap = (e) => { if (!e.target.closest('#barContextMenu')) hideBarContext(); };
	const onCtxMenuCap  = (e) => { if (!e.target.closest('#barContextMenu')) hideBarContext(); };
	const onKeydownCap  = (e) => { if (e.key === 'Escape') hideBarContext(); };
	const onScrollWin   = ()  => hideBarContext();
	const onResizeWin   = ()  => hideBarContext();
	document.addEventListener('click',       onDocClickCap, true);
	document.addEventListener('mousedown',   onDocClickCap, true);
	document.addEventListener('contextmenu', onCtxMenuCap,  true);
	document.addEventListener('keydown',     onKeydownCap,  true);
	window.addEventListener('scroll',  onScrollWin, { passive:true });
	window.addEventListener('resize',  onResizeWin);
	$ctx.data('__cap', { onDocClickCap, onCtxMenuCap, onKeydownCap, onScrollWin, onResizeWin });
	 
}

function hideBarContext(){
	const $ctx = $('#barContextMenu');
	if (!$ctx.length) return;
	// 캡처 리스너 해제
	// 특정 엘리먼트 트리 안에서의 동작을 캡처: click, mousedown, keydown, contextmenu (사용자 입력 관련 이벤트)
	// windows에서만 걸러지는 이벤트임  : resize, scroll, focus, blur  (뷰포트 변화 이벤트)
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
	$(document).off('.ctx'); $(window).off('.ctx');
}
//=== 2.1 캐시 & 백엔드 로더 ===
const _insertMenuCache = new Map();
/**
 * WBS 코드 목록 조회 (캐시 사용)
 * 기대 응답: [{code:'WBSCODE0101', name:'업무분석'}, ...]
 */
function loadInsertMenuItems(salesCd, cat) {
//    const list = [
//        {code:'WBSCODE0101', name:'업무분석'},
//        {code:'WBSCODE0102', name:'업무검토'},
//        {code:'WBSCODE0103', name:'업무개발'}
//    ];
//    return Promise.resolve(list); //  항상 Promise 반환
	let execUrl = '';
	if (cat == 'PLAN') {
		execUrl = '/user/wb/wb26/selectWbsTaskTempletGantList'
	} else if (cat == 'DO') {
//		execUrl = '/user/wb/wb26/selectWbsTaskTempletGantList'
		return Promise.resolve([]);
	} else {
		return Promise.resolve([]);
	}
	
    const key = salesCd + '|' + cat;
//    if (_insertMenuCache.has(key)) return Promise.resolve(_insertMenuCache.get(key));
    
    return new Promise((resolve, reject) => {	//  항상 Promise 반환
        postAjax(execUrl, { "salesCd" : salesCd,  "userId" : jwt.userId }, null,  // 백엔드 API 일정템플릭 불러오기
            function (res) {
                const list = Array.isArray(res?.result) ? res.result : (res?.list || []);
                _insertMenuCache.set(key, list);
                resolve(list);
            },
            function (err) { reject(err); }
        );
    });

}

// === 2.2 서브메뉴 DOM 렌더/위치 ===
function showInsertSubmenu(payload) {
    if ((payload.cat === 'DO') || (!payload || !payload.item)) { // 바(item)가 없으면 서브메뉴 실행 막기
        hideInsertSubmenu();   // 혹시 남아있던 서브메뉴 초기화
        return;                // 아예 실행하지 않음
    }

    const $menu = $('#barContextMenu');
    if (!$menu.length) return;
    
    $menu.find('.submenu').remove(); 
    
    // 1) 앵커(li[data-action="insert"]) 기준 위치 계산
    const $anchor = $menu.find('.ctx-list > li[data-action="insert"]');
    if (!$anchor.length) return;
    
    // 새 submenu DOM 생성
    const $sub = $('<div class="submenu" />').appendTo($menu);
    
    // 2) 일단 "로딩..." 표출
    $sub.empty().append('<div class="empty">불러오는 중...</div>').show();

    // 3) 위치 계산 (메뉴 내부 좌표계 기준)
    const a = $anchor[0].getBoundingClientRect();
    const m = $menu[0].getBoundingClientRect();
    const left = Math.round((a.right - m.left) + 6);
    const top  = Math.round(a.top - m.top);
    $sub.css({ left, top, display: 'block' });

    // 4) 데이터 로드 후 항목 렌더
    Promise.resolve(loadInsertMenuItems(payload.salesCd, payload.cat))
    .then(list => {
        $sub.empty();
        if (!list || list.length === 0) {
            $sub.append('<div class="empty">등록 가능한 항목이 없습니다</div>');
            return;
        }
        list.forEach(row => {
            const code = String(row.code || '').trim();
            const name = String(row.name || code).trim();
            const codeKind   = String(row.codeKind  || '').trim();
            const wbsPlansDt = String(row.wbsPlansDt || '').trim();
            const wbsPlaneDt = String(row.wbsPlaneDt  || '').trim();
            const dayCnt     = String(row.dayCnt    || '').trim();
            const $it = $('<div class="submenu-item" />')
                .attr('data-code', code)
                .attr('data-name', name)
                .attr('data-codeKind', codeKind)
                .attr('data-wbsPlansDt', wbsPlansDt)
                .attr('data-wbsPlaneDt', wbsPlaneDt)
                .attr('data-dayCnt', dayCnt)
                .text(name);
            $sub.append($it);
        });
    })
    .catch(() => {
        $sub.empty().append('<div class="empty">목록을 불러오지 못했습니다</div>');
    });
}

//닫기
function hideInsertSubmenu() {
    $('#barContextMenu .submenu').hide().empty();
}

//메뉴 바깥 클릭 시 닫기
$(document).on('click', (e) => {
    if (!$(e.target).closest('#barContextMenu').length) {
        hideInsertSubmenu();
    }
});

// === 2.3 항목 클릭 시 실행 (신규 바 생성 → 부분렌더 → 저장 API) ===
$(document).on('click', '#barContextMenu .submenu .submenu-item', function () {
    const $menu   = $('#barContextMenu');
    const payload = $menu.data('payload') || {};
    const salesCd = payload.salesCd;
    const cat     = payload.cat;
    const $track  = payload.$track || $(`.row-track[data-row="${salesCd}"][data-cat="${cat}"]`);
    
    if (!salesCd || !cat || !$track.length) {
        alert('대상 트랙을 찾을 수 없습니다.');
        return;
    }
    

    // 1) 서브메뉴 data-* 읽기
    const $item       = $(this);
    const code        = $item.data('code');
    const name        = $item.data('name');
    const codeKind    = $item.data('codekind');     // data-codeKind
    const plansStart  = $item.data('wbsplansdt');   // ms 타임스탬프 예상
    const plansEnd    = $item.data('wbsplanedt');   // ms 타임스탬프 예상
    const dayCnt      = $item.data('daycnt');

    //선택된 코드: WBSCODE04TASK6 가공품 발주 ▷ 입고 WBSCODE04 1753196400000 1757948400000 
    console.log("선택된 코드:", code, name, codeKind, plansStart, plansEnd, dayCnt);
    
    // 2) 날짜 산출: payload.openDateObj 우선(있으면), 없으면 템플릿 기간 사용
//    const hasOpenDate = (payload.openDateObj instanceof Date);
    const sDate = new Date(Number(plansStart));
    const eDate = new Date(Number(plansEnd));

    // 안전성: Invalid Date 방지
    const s = GanttCommon.fmt(sDate);
    const e = GanttCommon.fmt(eDate);

    // 3) 로컬 상태 반영
    const bucket = GanttApp.state.rowsData[salesCd] 
        || (GanttApp.state.rowsData[salesCd] = { PM: [], PLAN: [], DO: [] });
    const arr = bucket[cat];

    const newItem = {
        salesCd,
        key					: code,                         // 서버에서 최종 key 내려줄 수 있음
        wbsPlanCodeKind		: codeKind,
        label				: name,
        start				: s,
        end					: e,
        cat,
        confirmYn			: (cat === 'PLAN' ? 'Y' : 'N'),
        doneYn				: 'N',
        progress			: 0,
        expectMh			: 0,
        wbsPlanMngId		: jwt.userId,
        wbsPlanMngIdNm		: jwt.userNm,
        daycnt				: Number.isFinite(Number(dayCnt)) ? Number(dayCnt) : undefined,
    };
    arr.push(newItem);

    // 4) 부분 재렌더
//    GanttCore.renderRowSVG($track, arr, GanttApp.state, GanttApp._makeCoreOptions(cat, $track, salesCd));

    // 5) 저장 API 호출 (낙관적 갱신 → 실패 시 롤백/재조회 권장)
    try {
        const coCd   = $('#coCd').val() || $('#coCd_S').val();  // 폼 id 불일치 대비
        const totalDays = Math.max( 1, GanttCommon.days( GanttCommon.parse(newItem.start),  GanttCommon.parse(newItem.end) ) + 1 );
        
        const rowListObj = {
            ...newItem,
            coCd,
            wbsPlanCodeId		: '',
            salesCd				: newItem.salesCd,
            verNo				: (payload.rowMeta?.verNo ?? 1),      // TODO: 실제 verNo 소스 연결
            wbsPlanCodeNm		: newItem.label,
            wbsPlanMngId		: newItem.wbsPlanMngId,
            wbsPlansDt			: newItem.start,
            wbsPlaneDt			: newItem.end,
            daycnt				: totalDays,
            expectMh			: newItem.expectMh,
            creatId				: jwt.userId,
            creatPgm			: "WB0602M01",
            seq					: 1,
            year				: new Date().getFullYear(),
            wbsPlanStsCodeId	: 'WBSPLANSTS10',
        };

        postAjax("/user/wb/wb22/wbsLevel2GantInsert", rowListObj, null, function (data) {
            if (data.resultCode != 200) {
                alert(data.resultMessage);
            } else {
//            	GanttApp.loadAndRender(); // 복구
            	GanttApp.reloadAndRenderRow(newItem.salesCd);
            }
        });
    } catch (err) {
        console.error('[wbsLevel2Insert][error]', err);
    }
    
    // 6) 메뉴 정리
    hideInsertSubmenu();
    try { ctx?.hide?.(); } catch {}
    $(document).off('.ctx .gctx');
    $(window).off('.ctx .gctx');
});


// ===== GanttApp : 데이터/헤더/행/메뉴/바인딩 =====
// 즉시 실행 함수 표현식(IIFE, Immediately Invoked Function Expression) 패턴
// 함수 안에 변수를 정의하면 var, let, const 모두 지역 변수가 됨
// 내부에서 root === window
// 전역 오염을 막으면서도, 필요시 root.xxx 형태로 API를 공개
(function (root) {
	'use strict';

	const GC = root.GanttCommon;
	const CORE = root.GanttCore;

	const state = {
		viewStart: new Date('2025-04-24'),
		viewEnd: new Date('2025-07-01'),
		pxPerDay: 6,
		categories: ['PM', 'PLAN', 'DO'],
		rowDefs: [],
		rowsData: {}
	};

	// 메뉴 라우팅
	ctx.on('openDetail', ({ salesCd, item }) => {
		openModal("/static/html/user/wb/wb22/WB2201P01.html", 1200, 900, "WBS 계획등록", {
			coCd			: item.coCd,
			salesCd,
			histYn			: "N",
			wbsPlanCodeId	: item.key
		}, function (result) { 
//			GanttApp.loadAndRender();
			GanttApp.reloadAndRenderRow(salesCd);
		});
	});
	
	ctx.on('insert', (payload) => {
	    if (!payload || !payload.item) {
	        // 바가 선택되지 않은 상태 → 무시
	        return false;
	    }
		// TODO: 편집 패널
	    const salesCd = payload.salesCd;
	    const cat     = payload.cat;
	    const item    = payload.item;

	    console.log('[insert]', salesCd, cat, item);
	    // 서브메뉴 열기
	    if (cat == 'PLAN') {
			showInsertSubmenu(payload);
			return false; // ← 메뉴 닫지 말고 유지!
	    } else if (cat == 'DO') {
	    	customAlert('실행결과 추가는 PLAN에 있는 일정을 마우스 드롭다운으로 처리하세요!')
	    }
	});
	ctx.on('edit', ({ salesCd, cat, item }) => {
		// TODO: 편집 패널
		console.log('[edit]', salesCd, cat, item);
		try { ctx.hide?.(); } catch {}
		try { hideBarContext?.(); } catch {}
		openModal("/static/html/user/wb/wb22/WB2201P01.html", 1200, 900, "WBS 계획등록", {
			coCd			: item.coCd,
			salesCd,
			histYn			: "N",
			wbsPlanCodeId	: item.key
		}, function (result) { 
//			GanttApp.loadAndRender();
			GanttApp.reloadAndRenderRow(salesCd);
		});

	});
	ctx.on('duplicate', ({ salesCd, cat, item }) => {
//		const dstCat = (cat === 'PM') ? 'PLAN' : 'DO';
		const dstCat = cat;
		const arr = state.rowsData[salesCd][dstCat];
		const cloned = { ...item, key: 'NEW-' + Date.now(), cat: dstCat, confirmYn: (dstCat === 'PLAN' ? 'Y' : 'N'), doneYn: 'N' };
		arr.push(cloned);
		
		const $row = $(`.row-track[data-row="${salesCd}"][data-cat="${dstCat}"]`);
	    CORE.renderRowSVG($row, arr, state, makeCoreOptions(dstCat, $row, salesCd));

	    // 메뉴/핸들러 정리
	    try { ctx.hide?.(); } catch {}
	    hideBarContext?.();
	    $(document).off('.ctx .gctx');
	    $(window).off('.ctx .gctx');
	    // TODO: 저장 API
		console.log('[duplicate]', salesCd, cat, item);
	});
	ctx.on('delete', ({ salesCd, cat, item }) => {
		if (!GC.rules.canDelete(cat, item)) { customAlert('현재 상태에서는 삭제할 수 없습니다.'); return; }
		if (item.wbsPlanMngId != jwt.userId) { customAlert(item.wbsPlanMngIdNm + '님만 삭제 할 수 없습니다.'); return; }

		const formData = {
				fileTrgtKey		: item.fileTrgtKey,
				coCd			: item.coCd,
				salesCd			: item.salesCd,
				wbsPlanCodeId	: item.key,
				wbsRsltsNo		: item.wbsPlanNo,
				cat				: cat,
		}
		// 백엔드에서  PLAN --> wb22Mapper.wbsLevel2Delete(paramMap);		// 담당자 계획 삭제
		// 백엔드에서  DO   --> wb22Mapper.wbsRsltsGantDelete(paramMap);	// 담당자 실적 삭제
		postAjax("/user/wb/wb22/wbsRsltsGantDelete", formData, null, function (data) {
	        if (data.resultCode != 200) {
	        	customAlert(data.resultMessage);
	        	return;
	        } else {

	    		const arr = state.rowsData[salesCd][cat];
	    		const idx = arr.findIndex(a => a.key === item.key);
	    		if (idx > -1) {
	    			arr.splice(idx, 1);

	    	        const $row = $(`.row-track[data-row="${salesCd}"][data-cat="${cat}"]`);
	    	        CORE.renderRowSVG($row, arr, state, makeCoreOptions(cat, $row, salesCd));

	    	        // ✅ 메뉴/핸들러 정리
	    	        try { ctx.hide?.(); } catch {}
	    	        hideBarContext?.();
	    	        $(document).off('.ctx .gctx');
	    	        $(window).off('.ctx .gctx');
	    	        // TODO: 삭제 API
	    			console.log('[delete]', salesCd, cat, item);
	    			
	    	    }
	        }
	    });
		
	});
	// 일정확정 메뉴 라우팅
	ctx.on('complete',  ({ salesCd, cat, item }) => {

		console.log('[complete]', salesCd, cat, item);
        // TODO: 삭제 API
		if (cat =='DO') {
			completeMh(item.label, item.expectMh).then(mh => {
				if (mh.status === "cancel") {
			        console.log("사용자가 취소를 눌렀습니다.");
			        return;
			    }
				console.log("입력된 공수:", String(mh.value).trim());

			    const sDt = GanttCommon.parse(item.start); // Date
			    const eDt = GanttCommon.parse(item.end);   // Date
			    const days = Math.max(1, GanttCommon.days(sDt, eDt) + 1); // 양끝 포함
				const formData = {
				    	rsltFileTrgtKey		: item.fileTrgtKey,
				    	rsltRate			: 100,
				    	rsltIntocnt			: gPasFloatChk(String(mh.value).trim()),
				    	rsltDaycnt			: days,
				        rsltStrtDt			: item.start,
				        rsltEndDt			: item.end,
				        userId				: jwt.userId,
				        pgmId				: $('#pgmId').val(),
				        flag				: 'Y',
					}
				
				postAjax("/user/wb/wb26/update_wb26", formData, null, function (data) {
			        if (data.resultCode != 200) {
			        	customAlert(data.resultMessage);
			        } else {
						var formData = new FormData();
						formData.append("flag", 'Y');
						formData.append("rsltsFileTrgtKey", item.fileTrgtKey);
						formData.append("userId", jwt.userId);
						formData.append("pgmId", $("#pgmId").val());
						filePostAjax("/user/wb/wb22/wbsRsltsconfirm", formData, function(data){
							if(data.resultCode != 200){
								customAlert(data.resultMessage);
							} else {
					            // 성공 시점: 바 상태 갱신
					            // 1) item 객체 속성 수정
								item.doneYn = 'Y';
								
								// 2) 간트 바 재렌더
								const arr = GanttApp.state.rowsData[salesCd][cat];
								const $row = $(`.row-track[data-row="${salesCd}"][data-cat="${cat}"]`);
								GanttCore.renderRowSVG($row, arr, GanttApp.state, makeCoreOptions(cat, $row, salesCd));
							}
						});
			        }
			    });
				

	
			});
		}
		
	});
	

	// 일정확정취소 메뉴 라우팅
	ctx.on('cancle',  ({ salesCd, cat, item }) => {

		console.log('[cancle]', salesCd, cat, item);
        // TODO: 삭제 API
		if (cat =='DO') {
			var formData = new FormData();
			formData.append("flag", 'N');
			formData.append("rsltsFileTrgtKey", item.fileTrgtKey);
			formData.append("userId", jwt.userId);
			formData.append("pgmId", $("#pgmId").val());
			filePostAjax("/user/wb/wb22/wbsRsltsconfirm", formData, function(data){
				if(data.resultCode != 200){
					customAlert(data.resultMessage);
				} else {
		            // 성공 시점: 바 상태 갱신
		            // 1) item 객체 속성 수정
					item.doneYn = 'N';

					// 2) 간트 바 재렌더
					const arr = GanttApp.state.rowsData[salesCd][cat];
					const $row = $(`.row-track[data-row="${salesCd}"][data-cat="${cat}"]`);
					GanttCore.renderRowSVG($row, arr, GanttApp.state, makeCoreOptions(cat, $row, salesCd));
				}
			});
		}
		
	});
	// 문제등록 메뉴 라우팅
	ctx.on('problem',  ({ salesCd, cat, item }) => {

		console.log('[problem]', salesCd, cat, item);
        // TODO: 삭제 API
		if (cat =='PLAN') {
			var paramObj = {
					"actionType"      : "C1",
					"coCd"            : item.coCd,
					"salesCd"         : item.salesCd,
					"wbsPlanNo"       : item.wbsPlanNo,
					"wbsPlanCodeKind" : item.wbsPlanCodeKind,
					"wbsPlanCodeId"   : item.key,
					"wbsPlanCodeNm"   : item.label,
					"wbsPlanMngNm"    : item.wbsPlanMngIdNm,
					"wbsPlansDt"      : item.s,
					"wbsPlaneDt"      : item.e,
				};
				openSecondModal("/static/html/user/wb/wb24/WB2401P01.html", 1200, $('body').height()-41, "계획문제등록", paramObj, function () {

				});
		} else if (cat =='DO') {
				var paramObj = {
					"actionType"      : "C2",
					"coCd"            : item.coCd,
					"salesCd"         : item.salesCd,
					"wbsPlanNo"       : item.wbsPlanNo,
					"wbsRsltsNo"      : item.wbsPlanNo,
					"wbsPlanCodeKind" : item.wbsPlanCodeKind,
					"wbsPlanCodeId"   : item.key,
					"wbsPlanCodeNm"   : item.label,
					"wbsPlanMngNm"    : item.wbsPlanMngIdNm,
					"wbsRsltssDt"     : item.s,
					"wbsRsltseDt"     : item.e,
				};
				openSecondModal("/static/html/user/wb/wb24/WB2401P11.html", 1200, $('body').height()-41, "실적문제등록", paramObj, function (){

				});
		} else {
			return;
		}
		
	});

	// 메뉴 라우팅
	ctx.on('workreport', ({ salesCd, cat, item }) => {
		console.log('[workreport]', salesCd, cat, item);
        // TODO: 삭제 API
		openModal("/static/html/user/pm/pm01/PM0101P01.html",  1100, 750, "작업일보등록", {
			actionType		: 'C',
			coCd			: item.coCd,
			ordrsNo			: item.ordrsNo,
			salesCd,
			wbsPlanCodeKind	: item.wbsPlanCodeKind,
			wbsPlanCodeId	: item.key,
			wbsPlanCodeIdNm	: item.label,
			wbsPlanNo		: item.wbsPlanNo,
			cat				: item.cat,
			workRptId		: item.wbsPlanMngId,
			workRptIdNm		: item.wbsPlanMngIdNm,
			start			: item.start,
			end				: item.end,
			workRptRmk		: '일정관리에서 생성',
		}, function (result) { 
//			GanttApp.loadAndRender();
		});
	});
	
	async function completeMh(label, expectMh=0.0) {
		const result  = await customPrompt(
	        `${label} 작업에 투입된 공수를 입력하세요:`,
	        expectMh,
	        "해당 작업을 위해 투입된 일수"
	    );
		return result;
	    // 확인 버튼: 문자열, 취소: null
		// result = { status: "ok" | "cancel", value: string|null }
//		if (result.status === "cancel") {
//			return null;  // 취소 시 null 반환
//	    }
//
//	    return String(result.value).trim(); // 확인 시 입력값 문자열 반환
	}
	
	function makeCoreOptions(cat, $track, rowKey, extraOpts = {}) {
		return {
			cat,
			$track,
	        state,
	        ...extraOpts,
	        allowRowMove: true,          // 세로 이동 탐지 활성화
	        getPlanBounds: (prefix9) => {
	            const pmArr = (state.rowsData[rowKey]?.PM) || [];
	            const pm = pmArr.find(x => String(x.key || '').slice(0, 9) === String(prefix9));
	            if (!pm) return null;
	            return { s: GanttCommon.parse(pm.start), e: GanttCommon.parse(pm.end) };
	          },
	          
			onApplyChange: (ds, s, e, $t) => {

				const rKey = $t.data('row');
				const c    = $t.data('cat');
				const arr  = state.rowsData[rKey][c];
				const idx  = arr.findIndex(x => x.key === ds.key);
				if (idx < 0) return;

				const item = arr[idx];
				// --- 로컬 헬퍼: 스냅백 + 핸들러 정리 ---
				function snapback() {
					CORE.renderRowSVG($t, arr, state, makeCoreOptions(c, $t, rKey));
					try { ctx?.hide?.(); } catch {}
					$(document).off('.ctx .gctx');
					$(window).off('.ctx .gctx');
				}
				


				// --- 로컬 헬퍼: 권한/상태 검사 ---
				function canEdit(c, item, rKey) {
					// 수주 메타 (PM 확정 여부 등)
					const rowMeta = state.rowDefs.find(r => r.key === rKey) || {};

					// 1) PM: 담당자 본인만, 그리고 PM 확정('Y')이면 수정 불가
					if (c === 'PM') {
						if (item.smrizeId !== jwt.userId && jwt.userId != 'ycy' && jwt.userId != 'js.nam') {
							customAlert('해당 일정은 영업총괄 PM 또는 윤치영부장에게 일정 변경 요청 바랍니다. 수정할 수 없습니다.');
							return false;
						}
						if (item.confirmYn === 'Y') {
							customAlert('확정 완료된 자료는 수정이 불가능합니다.');
							return false;
						}
						return true;
					}

					// 2) PLAN: PM 일정이 확정되어 있어야만(해당 수주의 pmCloseYn === 'Y') 수정 가능,
					//          또한 담당자 본인만 수정 가능
					if (c === 'PLAN') {
						if (item.wbsPlanMngId !== jwt.userId && jwt.userId != 'js.nam') {
							customAlert(item.wbsPlanMngIdNm  + '님이 담당자입니다. 담당자 외 수정 금지!');
							return false;
						}
						if (rowMeta.pmCloseYn !== 'Y') {
							customAlert('영업총괄PM 일정 미확정 상태이므로 담당자 일정 수정이 불가합니다.');
							return false;
						}
						return true;
					}

					// 3) DO: 담당자 본인만 수정 가능
					if (c === 'DO') {
						if (item.wbsPlanMngId !== jwt.userId && jwt.userId != 'js.nam') {
							customAlert(item.wbsPlanMngIdNm  + '님이 담당자입니다. 담당자 외 수정 금지!');
							return false;
						}
						return true;
					}

					// 기타 카테고리 없음
					return false;
				}
				
				// --- 권한/상태 검사 ---
				if (!canEdit(c, item, rKey)) {
					// 로컬 상태 미변경 상태이므로 바로 스냅백만
					snapback();
					return;
				}



			    // 1) 허용된 경우에만 로컬 상태 갱신
			    item.start = GanttCommon.fmt(s);
			    item.end   = GanttCommon.fmt(e);

			    // 2) 부분 재렌더: 현재 겹침 상태 유지 + 정렬 OFF
			    // 편집 후 부분 렌더는 정렬 비활성화(현재 순서 유지)
			    const stacked = !UNSTACKED[c].has(rKey);
			    const extra   = { stackToFirstLane: stacked, sortBars: false, keepLane: true, writeBackLane: true };
			    CORE.renderRowSVG($t, arr, state, makeCoreOptions(c, $t, rKey, extra));
			    
				// TODO: 저장 API
			    // 3) ★ 여기서 백엔드 저장 API 호출
			    //    필요 payload: 수주코드(rKey), 구분(c), 키(ds.key), 시작/종료일
			    try { wbsBarChange(c, item); } catch (err) { console.error('[onApplyChange][save error]', err); }
				console.log('[onApplyChange]', rKey, c, item);
				
			    // 4) (선택) 핸들러/임시 상태 정리
			    try { ctx?.hide?.(); } catch {}
			    $(document).off('.ctx .gctx');
			    $(window).off('.ctx .gctx');
			},

			onCopy: (ds, s, e, $t) => {
//				const srcCat = $t.data('cat');
//				const rKey = $t.data('row');
//				const dstCat = srcCat;
//				const dstArr = state.rowsData[rKey][dstCat];

				
//				const cloned = {
//					...ds.item,
//					key: 'NEW-' + Date.now(),
//					cat: dstCat,
//					start: GanttCommon.fmt(s),
//					end: GanttCommon.fmt(e),
//					confirmYn: (dstCat === 'PLAN' ? 'Y' : 'N'),
//					doneYn: 'N'
//				};
//				dstArr.push(cloned);
//
//				const $dst = $(`.row-track[data-row="${rKey}"][data-cat="${dstCat}"]`);
//				CORE.renderRowSVG($dst, dstArr, state, makeCoreOptions(dstCat, $dst, rKey));
				// TODO: 저장 API
//				console.log('[onCopy]', rKey, srcCat, cloned);
			},

	        onRowChange: ({ key, item, src, dst, range }) => {
	            // 0) 방어: 소스/타겟 필수값
	            if (!src?.salesCd || !src?.cat || !dst?.salesCd || !dst?.cat) return false;

	            // 1) 같은 트랙이면 여기로 오지 않음(코어에서 처리). 여기선 교차 트랙만 온다고 가정.

	            // 2) 정책: 같은 수주 + PLAN→DO만 허용 (copy), 그 외 전부 불허
	            const sameSales = (src.salesCd === dst.salesCd);
	            const isPlanToDo = (src.cat === 'PLAN' && dst.cat === 'DO');
	            if (!(sameSales && isPlanToDo)) {
	                customAlert('이동 불가: 교차 트랙 이동은 PLAN → DO (동일 수주) 복사만 허용됩니다.');
	                return false; // 스냅백
	            }
	            if (item.wbsPlanMngId !== jwt.userId && jwt.userId != 'js.nam') {
					customAlert(item.wbsPlanMngIdNm  + '님이 담당자입니다. 담당자 외 수정 금지!');
					return false;
				}

	            // 3) 복사 생성 (원본 유지)
	            const srcArr = state.rowsData[src.salesCd]?.[src.cat] || [];
	            const dstArr = state.rowsData[dst.salesCd]?.[dst.cat] || (state.rowsData[dst.salesCd] = (state.rowsData[dst.salesCd] || {PM:[],PLAN:[],DO:[]}), state.rowsData[dst.salesCd][dst.cat]);

	            const idx = srcArr.findIndex(x => x.key === key);
	            if (idx < 0) { console.warn('원본 아이템을 찾을 수 없습니다.', key); return false; }

	            // 3-1) 중복 체크 (라벨 기준)
	            const dup = dstArr.find(x => x.label === item.label);
	            if (dup) { customAlert("동일자료가 실적에 존재합니다."); return false; }

	            
	            const cloned = { ...srcArr[idx] };
	            cloned.key   = 'NEW-' + Date.now();          // 임시 키
	            cloned.cat   = 'DO';                          // 목적지 cat
	            cloned.start = GanttCommon.fmt(range.s);
	            cloned.end   = GanttCommon.fmt(range.e);
	            // 초기화/정책값
	            cloned.doneYn    = 'N';
	            // PLAN의 confirmYn은 DO에는 의미 없으니 유지/변환 여부는 정책에 맞게:
	            // cloned.confirmYn = 'N';

	            dstArr.push(cloned);

	            // 4) 부분 렌더: 원본/대상 두 트랙만 다시 그림
	            const $srcTrack = src.$track || $(`.row-track[data-row="${src.salesCd}"][data-cat="${src.cat}"]`);
	            const $dstTrack = dst.$track || $(`.row-track[data-row="${dst.salesCd}"][data-cat="${dst.cat}"]`);
	            if ($srcTrack?.length) GanttCore.renderRowSVG($srcTrack, state.rowsData[src.salesCd][src.cat], state, makeCoreOptions(src.cat, $srcTrack, src.salesCd));
	            if ($dstTrack?.length) GanttCore.renderRowSVG($dstTrack, state.rowsData[dst.salesCd][dst.cat], state, makeCoreOptions(dst.cat, $dstTrack, dst.salesCd));


	    	    // 1) 일수/공수 계산 (필요 정책에 맞게 보정)
	    	    const sDt = GanttCommon.parse(cloned.start); // Date
	    	    const eDt = GanttCommon.parse(cloned.end);   // Date
	    	    const days = Math.max(1, GanttCommon.days(sDt, eDt) + 1); // 양끝 포함
	            
	            
				console.log('[DND COPY]', key, item, src, dst, range )
	            // 5) 저장 API (추가등록) 
				const formData = {
		            	coCd				: $('#coCd_S').val(),
		                wbsRsltsNo			: item.wbsPlanNo,
		                wbsPlanCodeKind2_P	: item.wbsPlanCodeKind,
		                wbsPlanCodeId2_P	: item.key,
		                salesCd2_P			: item.salesCd,
		                wbsRsltsId			: jwt.userId,		//작성자
		                wbsRsltsRate		: 0,
		                wbsRsltsMh			: 0,
		                wbsRsltsCnts		: '',				//주요내용
		                wbsRsltssDt			: cloned.start,
		                wbsRsltseDt			: cloned.end,
		                rsltsDaycnt			: days,
		                userId				: jwt.userId,
		                pgmId				: $('#pgmId').val(),
		                fromCat				: 'PLAN',
		                toCat				: 'DO',
		                copyYn				:  'Y'
		        }
	            postAjax('/user/wb/wb22/wbsRsltsGantInsert', formData, null, function (data) {
			        if (data.resultCode != 200) {
//			        	customAlert(data.resultMessage);
			        	customAlert('추가 실패입니다.  전산실 확인 바랍니다!');
			        	return false; 
			        }
//	                GanttApp.loadAndRender(); // 복구
			        GanttApp.reloadAndRenderRow(item.salesCd);
	            });

	            return true; // 드롭 확정
	        },
	        
			onContext: (ev, payload) => {
				// 1) 공통 보강
	            const $t = payload.$track || $track || $(ev.target).closest('.row-track');
	            const salesCd = payload.salesCd || ($t && $t.data('row'));
	            const catSafe = payload.cat || ($t && $t.data('cat')) || cat;
	            const enriched = { ...payload, salesCd, cat: catSafe, $track: $t };

	            // 2) 더블클릭(= action: 'edit')은 메뉴를 띄우지 않고 즉시 편집 실행
	            if (enriched.action === 'edit') {
	                const $ctx = $('#barContextMenu');
	                $ctx.data('payload', enriched);
	                $ctx.find('[data-action="edit"]').trigger('click');
	                return;
	            }

	            // 3) 일반 우클릭은 전역 openContextMenu() 경유 (편집/권한/좌표 보정 포함)
	            openContextMenu(ev, enriched);
			}
			
		};
	}


	function hasConfirmEditPrivilege() {
		//영엄총괄PM 및 윤치영부장만 수정가능하게 제한 코드 추가 해야함.
	    const user = jwt.userId;
	    if (['영업총괄PM', '윤치영부장'].includes(jwt.userId)) return true;
	    return false;
	}

	//Backend 일괄 수정 함수
	function wbsBarChange(c, item) {

	    // 1) 일수/공수 계산 (필요 정책에 맞게 보정)
	    const sDt = GanttCommon.parse(item.start); // Date
	    const eDt = GanttCommon.parse(item.end);   // Date
	    const days = Math.max(1, GanttCommon.days(sDt, eDt) + 1); // 양끝 포함
	    // 2) 백엔드 포맷 (YYYYMMDD 필요한 경우)
//	    const fmt8 = (dStr) => (dStr || '').replace(/-/g, ''); // '2025-08-01' -> '20250801'

        // 사용자/프로그램 정보
        const userId = jwt?.userId;
        const pgmId  = $('#pgmId').val();

        // 필수 키 존재 여부(업무 스펙에 따라 조정)
        const hasFileKey = !!item.fileTrgtKey;

    	if (!hasFileKey) { customAlert("필수 키 값이 없습니다. (fileTrgtKey)"); return; }
    	
	    switch (c) {
	        // -----------------------------------------------------------------
	        // PM 계획 변경
	        // -----------------------------------------------------------------
		    case 'PM': {
		    	
				const formData = {
						fileTrgtKey		: item.fileTrgtKey,
						wbsPlanCodeNm	: '',
						wbsPlanCodeKind	: 'WBSCODE',
						wbsPlanCodeId	: item.key,
						wbsPlanMngId	: item.wbsPlanMngId,
						expectMh		: item.expectMh,
				    	wbsPlansDt		: item.start,
				    	wbsPlaneDt		: item.end,
				    	daycnt			: days,
				        
				        flag			: 'N',
				        creatId			: jwt.userId,
				        creatPgm		: $('#pgmId').val()
					}
					
				postAjax("/user/wb/wb22/wbsLevel1GantUpdate", formData, null, function (data) {
			        if (data.resultCode != 200) {
			        	customAlert(data.resultMessage);
			        }
			    });
	
	            break;
			}
			
	
	        // -----------------------------------------------------------------
	        // PLAN 계획 변경
	        // -----------------------------------------------------------------
	        case 'PLAN': {
				const formData = {
						fileTrgtKey		: item.fileTrgtKey,
						wbsPlanCodeNm	: item.label,
						wbsPlanCodeKind	: item.wbsPlanCodeKind,
						wbsPlanCodeId	: item.key,
						wbsPlanMngId	: item.wbsPlanMngId,
						expectMh		: item.expectMh,
				    	wbsPlansDt		: item.start,
				    	wbsPlaneDt		: item.end,
				    	daycnt			: days,
				        
				        flag			: 'N',
				        creatId			: jwt.userId,
				        creatPgm		: $('#pgmId').val()
					}
				postAjax("/user/wb/wb22/wbsLevel1GantUpdate", formData, null, function (data) {
			        if (data.resultCode != 200) {
			        	customAlert(data.resultMessage);
			        // (선택) 서버 재조회 후 해당 행만 재렌더
			        // searchData(); 또는 해당 row만 재로딩
			    	// renderAll(); // (선택) 또는 부분만 갱신 유지
			        }
			    });
	
	            break;
			}
	
	        // -----------------------------------------------------------------
	        // DO(실적) 변경
	        // -----------------------------------------------------------------
	        case 'DO': {
				const formData = {
				    	rsltFileTrgtKey		: item.fileTrgtKey,
				    	rsltRate			: item.progress,
				    	rsltIntocnt			: item.expectMh,
				    	rsltDaycnt			: days,
				        rsltStrtDt			: item.start,
				        rsltEndDt			: item.end,
				        userId				: jwt.userId,
				        pgmId				: $('#pgmId').val()
					}
				
				postAjax("/user/wb/wb26/update_wb26", formData, null, function (data) {
			        if (data.resultCode != 200) {
			        	customAlert(data.resultMessage);
			        // (선택) 서버 재조회 후 해당 행만 재렌더
			        // searchData(); 또는 해당 row만 재로딩
			    	// renderAll(); // (선택) 또는 부분만 갱신 유지
			        }
			    });
	
	            break;
			}
	
	        // -----------------------------------------------------------------
	        default:
	            console.warn("[WBS] 미지원 카테고리:", c, item);
	            break;
	    }
	}
	
	
	// 헤더/행
	function buildHeader() {
		const start = GC.sod(state.viewStart), end = GC.sod(state.viewEnd);
		const totalDays = GC.days(start, end) + 1, totalW = totalDays * state.pxPerDay;

		const labelW = GC.cssVarPx('--label-width');
		const badgeW = GC.cssVarPx('--badge-col-width');
		const gapW = GC.cssVarPx('--col-gap');
		const leftRailW = labelW + gapW + badgeW;

		$('.header').css('width', (leftRailW + totalW) + 'px');
		const $m = $('#rulerMonth').empty();
		const $w = $('#rulerWeek').empty();
		const $d = $('#rulerDay').empty();

		let cur = new Date(start);
		while (cur <= end) {
			const endOfMonth = new Date(cur.getFullYear(), cur.getMonth() + 1, 0);
			const segEnd = end < endOfMonth ? end : endOfMonth;
			const len = GC.days(cur, segEnd) + 1;
			$('<div class="cell"></div>')
				.css('width', (len * state.pxPerDay) + 'px')
				.text(`${cur.getFullYear()}년 ${cur.getMonth() + 1}월`).appendTo($m);
			cur = new Date(segEnd); cur.setDate(cur.getDate() + 1);
		}

		function weekOfYear(d) { const y0 = new Date(d.getFullYear(), 0, 1); return String(Math.floor((GC.days(y0, d) + y0.getDay()) / 7) + 1).padStart(2, '0'); }
		cur = new Date(start);
		while (cur <= end) {
			const deltaToNextSun = (7 - cur.getDay()) % 7;
			const nextSun = new Date(cur.getFullYear(), cur.getMonth(), cur.getDate() + deltaToNextSun);
			const blockEnd = (deltaToNextSun === 0) ? cur : new Date(+nextSun - GC.MS);
			const segEnd = end < blockEnd ? end : blockEnd;
			const len = GC.days(cur, segEnd) + 1;
			$('<div class="cell"></div>')
				.css('width', (len * state.pxPerDay) + 'px')
				.text('W' + weekOfYear(cur)).appendTo($w);
			cur = new Date(segEnd); cur.setDate(cur.getDate() + 1);
		}

		const todayT = GC.sod(new Date()).getTime();
		for (let i = 0; i < totalDays; i++) {
			const dt = new Date(+start + i * GC.MS);
			const $c = $('<div class="cell"></div>').text(dt.getDate());
			if ([0, 6].includes(dt.getDay())) $c.addClass('day-weekend');
			if (GC.sod(dt).getTime() === todayT) $c.addClass('day-today');
			$d.append($c);
		}

		$('.ruler-row').css('width', totalW + 'px');
		$('#headerRulers').css('width', totalW + 'px');
		$('.header-right').css('width', `calc(${totalW}px + var(--badge-col-width))`);
		const rulersH = document.getElementById('headerRulers').offsetHeight;
		$('#planBadge').css('height', rulersH + 'px');
	}

	function buildRows() {
		const $container = $('#rowsContainer').empty();
		const totalDays = GC.days(state.viewStart, state.viewEnd) + 1;
		const totalW = totalDays * state.pxPerDay;

		const labelW = GC.cssVarPx('--label-width');
		const badgeW = GC.cssVarPx('--badge-col-width');
		const gapW = GC.cssVarPx('--col-gap');
		const leftRailW = labelW + gapW + badgeW;

		state.rowDefs.forEach(row => {
			const $group = $('<div class="row-group"></div>');
			$('<div class="group-label"></div>')
				.html(
					'<span style="color:white;">' + row.label + '</span>' +
					'<span style="color:yellow;">' + (row.기계종류 || '') + '</span>' +
					'<span style="color:white;">' + (row.eqpNm || '') + '</span>' +
					'<span style="color:yellow;">' + (row.mngIdNm || '') + ' - ' + (row.수주구분 || '') + '</span>' +
					'<span style="color:white;">' + (row.기계구분 || '') + '</span>' +
					'<span style="color:white;">' + (row.clntNm || '') + '(' + (row.clntPjtNm || '') + ')</span>' +
					'<span style="color:blue;">' + ((row.closeYn=='Y')?'과제확정':'과제미확정') + ',  ' + ((row.pmCloseYn=='Y')?'PM일정확정':'PM일정미확정')  + '</span>'
				).appendTo($group);

			$group.css('width', (leftRailW + totalW) + 'px');
			const $box = $('<div class="group-box"></div>').css('width', (badgeW + gapW + totalW + 16) + 'px');
			$group.append($box);

			state.categories.forEach(cat => {
				const $line = $('<div class="group-line"></div>');
				$('<div class="line-badge"></div>').text(cat).appendTo($line);
				$('<div class="row-track"></div>')
					.attr('data-row', row.key)
					.attr('data-cat', cat)
					.appendTo($line);
				$box.append($line);
			});

			$container.append($group);
		});
	}

	function paintTrackShading() {
		const start = GC.sod(state.viewStart);
		const end = GC.sod(state.viewEnd);
		const total = GC.days(start, end) + 1;
		const todayT = +GC.sod(new Date());

		$('.row-track .shade').remove();
		for (let i = 0; i < total; i++) {
			const dt = new Date(+start + i * GC.MS);
			const left = i * state.pxPerDay;
			const w = state.pxPerDay;
			const wknd = [0, 6].includes(dt.getDay());
			const isToday = (+GC.sod(dt) === todayT);
			if (!wknd && !isToday) continue;
			const cls = isToday ? 'today' : 'weekend';
			$('.row-track').each(function () {
				$('<div class="shade ' + cls + '"></div>')
					.css({ left: left, width: w })
					.appendTo(this);
			});
		}
	}

	function drawTodayLine() {
		const t = GC.sod(new Date());
		if (t < state.viewStart || t > state.viewEnd) return;
		const left = GC.makeXFromDate(state)(t);
		$('.row-track').each(function () {
			$(this).append($('<div class="today-line"/>').css({ left }));
		});
	}

	function syncTimelineWidths() {
		const totalDays = GC.days(GC.sod(state.viewStart), GC.sod(state.viewEnd)) + 1;
		const totalW = totalDays * state.pxPerDay;
		$('#rulerMonth, #rulerWeek, #rulerDay').css('width', totalW + 'px');
		$('.row-track').css('width', totalW + 'px');
	}

	function updateFontSizes(pxPerDay) {
		const ratio = pxPerDay / 24;
		const clamp = v => Math.min(14, Math.max(8, Math.round(v)));
		document.documentElement.style.setProperty('--label-font-size', clamp(14 * ratio) + 'px');
		document.documentElement.style.setProperty('--ruler-font-size', clamp(12 * ratio) + 'px');
		document.documentElement.style.setProperty('--bar-font-size', clamp(12 * ratio) + 'px');
	}

	function setZoom(px, renderCall=true) {
		try { openProgress(true); } catch { }
		px = Math.max(2, Math.min(26, px | 0));
		state.pxPerDay = px;
		document.documentElement.style.setProperty('--px-per-day', px + 'px');
		$('#zoom').val(px); $('#zoomVal').text(px);
		updateFontSizes(px);
		if (renderCall) renderAll();	//초기 설정시에 실행 제외
	}

	function renderAll() {
		buildHeader();
		buildRows();
		
		$('.row-track').each(function(){
			const $track = $(this);
			const rowKey = $track.data('row');
			const cat    = $track.data('cat');
			const items  = state.rowsData?.[rowKey]?.[cat] || [];

			// 기본: 겹침 ON (UNSTACKED에 없으면 겹침)
			const stacked = !UNSTACKED[cat].has(rowKey); // true면 겹침, false면 다중 레인
			const extra   = { stackToFirstLane: stacked, sortBars: true, keepLane: true, writeBackLane: !stacked  };// 전체 렌더 시 정렬 ON

			CORE.renderRowSVG($track, items, state, makeCoreOptions(cat, $track, rowKey, extra));
//			GanttCore.renderRowSVG($track, items, GanttApp.state, makeCoreOptions(cat, $track, rowKey, extra));
		});
		
		drawTodayLine();
		paintTrackShading();
		syncTimelineWidths();
		try { openProgress(false); } catch { }
		
	}

	// 데이터 로드/매핑
	function catFrom(planType = '') {
		const t = (planType || '').toUpperCase();
		if (t.includes('PM')) return 'PM';
		if (t.includes('실적')) return 'DO';
		return 'PLAN';
	}

	// 전체  기계의 데이터 서버에서 불러와 다시 그림
	function loadAndRender() {
		if (!inputValidation($('input[required]'))) return;
		try { openProgress(true); } catch { }
		const formData = {};
		$('[data-search]').each(function () {
			formData[$(this).data('search')] = $(this).hasClass('input_calendar')
				? $(this).val().replace(/\-/g, '')
				: $(this).val();
		});
		formData.pageNo = 1;
		formData.recordCnt = $('#recordCnt').val();

		const val = $('#multiple-checkboxes-prdtGrp').val();
		formData.prdtGrp = val ? val.join(',') : '';

		postAjax("/user/wb/wb26/select_wb2605_List", formData, null, function (data) {
			const list = data.result || [];					// WBS 상세 정보
			const metaList = data.resultMetaList || [];		// SalesCd Meta 정보
			const today = new Date();
			const pad = n => (n < 10 ? '0' + n : '' + n);
			const todayStr = today.getFullYear() + '-' + pad(today.getMonth() + 1) + '-' + pad(today.getDate());
			const endDtS = String($('#endDt_S').val() || '');

			const rowsData = Object.create(null);
			const metaMap = new Map();
			let minDate = null, maxDate = $(endDt_S).val();

			for (const r of metaList) {
				const sales = (r && r.salesCd) ? String(r.salesCd).trim() : '';
				if (!sales) continue;

				if (!metaMap.has(sales)) {
					metaMap.set(sales, {
						label		: sales, 
						key			: sales,
						pmCloseYn	: (r.pmCloseYn === 'Y') ? 'Y' : 'N',			//PM일정 확정여부
						closeYn		: (r.closeYn   === 'Y') ? 'Y' : 'N',			//과제 확정여부
						기계종류		: (r['기계종류'] || '').toString().trim(),
						mngIdNm		: (r.smrizeIdNm || '').toString().trim(),
						eqpNm		: (r.eqpNm || '').toString().trim().slice(0, 24),
						수주구분		: (r['수주구분'] || '').toString().trim(),
						아이템구분		: (r['아이템구분'] || '').toString().trim(),
						기계구분		: (r['기계구분'] || '').toString().trim(),
						clntPjtNm	: (r['clntPjtNm'] || '').toString().trim(),
						clntNm		: (r['clntNm'] || '').toString().trim(),
						외주업체		: (r.mkerCdNm || '').toString().trim()
					});
				}
			}

			state.rowDefs = Array.from(metaMap.values());
			
			
			for (const r of list) {
				const sales = (r && r.salesCd) ? String(r.salesCd).trim() : '';
				if (!sales) continue;
				
			    
				const start = r.wbsPlansDtFm || '';
				const end = r.wbsPlaneDtFm ? r.wbsPlaneDtFm :GanttCommon.fmt(new Date());
				const item = {
					key					: r.wbsPlanCodeId,
					salesCd				: r.salesCd,
					coCd				: r.coCd,
					ordrsNo				: r.ordrsNo,
					wbsPlanNo			: r.wbsPlanNo,
					wbsPlanCodeKind		: r.wbsPlanCodeKind,
					label				: (r.wbsPlanCodeNm || r.wbsPlanCodeId || '').toString().trim(),
					start, end,
					cat					: r.planType,
					confirmYn			: r.confirmYn || 'N',  			// PM/PLAN 확정 여부 ('Y'|'N')
					doneYn				: r.pmCloseYn || 'N',        	// DO 실적확정 ('Y'|'N')
					overdue				: r.overDue || 'N',        		// 일정지연여부
					progress			: r.wbsRsltsRate || 0,      	// 진척율(0~100), 선택
					expectMh			: r.expectMh || 0,      		// 투입공수
					fileTrgtKey			: r.fileTrgtKey || null,
					wbsPlanMngId		: r.wbsPlanMngId || null,
					wbsPlanMngIdNm		: r.wbsPlanMngIdNm || null,
					smrizeId			: r.smrizeId || null,      		// 담당자, 선택
					smrizeIdNm			: r.smrizeIdNm || null,  		// 담당자, 선택
				};

				let bucket = rowsData[sales];
				if (!bucket) bucket = rowsData[sales] = { PM: [], PLAN: [], DO: [] };
				bucket[item.cat].push(item);
				
				if (start) { if (!minDate || start < minDate) minDate = start; if (!maxDate || start > maxDate) maxDate = start; }
				if (end) { if (!minDate || end < minDate) minDate = end; if (!maxDate || end > maxDate) maxDate = end; }
			}

			const toDate = s => s ? new Date(s + 'T00:00:00') : null;
			state.viewStart = toDate(minDate || todayStr) || new Date();
			state.viewEnd = toDate((maxDate && endDtS && endDtS > maxDate) ? endDtS : (maxDate || todayStr)) || new Date();
			state.rowsData = rowsData;

			renderAll();
		}, false);
	}
	
	// 전체 재렌더링 없이 특정 기계의 데이터만 서버에서 불러와 다시 그림
	function renderSingleRow(salesCd) {
	    for (const cat of GanttApp.state.categories) {
	        const $track = $(`.row-track[data-row="${salesCd}"][data-cat="${cat}"]`);
	        const items = GanttApp.state.rowsData?.[salesCd]?.[cat] || [];
	        const stacked = !UNSTACKED[cat].has(salesCd);
	        const extra = {
	            stackToFirstLane: stacked,
	            sortBars: true,
	            keepLane: true,
	            writeBackLane: !stacked
	        };
	        GanttCore.renderRowSVG($track, items, GanttApp.state, makeCoreOptions(cat, $track, salesCd, extra));
	    }
	}

	// 전체 재렌더링 없이 특정 기계의 데이터만 서버에서 불러와 다시 그림
	function reloadAndRenderRow(salesCd) {
		const formData = {};
		$('[data-search]').each(function () {
			formData[$(this).data('search')] = $(this).hasClass('input_calendar')
				? $(this).val().replace(/\-/g, '')
				: $(this).val();
		});
		formData.pageNo = 1;
		formData.recordCnt = $('#recordCnt').val();

		const val = $('#multiple-checkboxes-prdtGrp').val();
		formData.prdtGrp = val ? val.join(',') : '';
		formData.salesCd = salesCd;
		postAjax("/user/wb/wb26/select_wb2605_List", formData, null, function (data) {
				const list = data.result || [];					// WBS 상세 정보
	            if (!data || !data.result.length) {
	                console.warn('해당 salesCd의 데이터 없음:', salesCd);
	                return;
	            }


				const rowsData = Object.create(null);
				for (const r of list) {
					const sales = (r && r.salesCd) ? String(r.salesCd).trim() : '';
					if (!sales) continue;
					
				    
					const start = r.wbsPlansDtFm || '';
					const end = r.wbsPlaneDtFm ? r.wbsPlaneDtFm :GanttCommon.fmt(new Date());
					const item = {
						key					: r.wbsPlanCodeId,
						salesCd				: r.salesCd,
						coCd				: r.coCd,
						ordrsNo				: r.ordrsNo,
						wbsPlanNo			: r.wbsPlanNo,
						wbsPlanCodeKind		: r.wbsPlanCodeKind,
						label				: (r.wbsPlanCodeNm || r.wbsPlanCodeId || '').toString().trim(),
						start, end,
						cat					: r.planType,
						confirmYn			: r.confirmYn || 'N',  			// PM/PLAN 확정 여부 ('Y'|'N')
						doneYn				: r.pmCloseYn || 'N',        	// DO 실적확정 ('Y'|'N')
						overdue				: r.overDue || 'N',        		// 일정지연여부
						progress			: r.wbsRsltsRate || 0,      	// 진척율(0~100), 선택
						expectMh			: r.expectMh || 0,      		// 투입공수
						fileTrgtKey			: r.fileTrgtKey || null,
						wbsPlanMngId		: r.wbsPlanMngId || null,
						wbsPlanMngIdNm		: r.wbsPlanMngIdNm || null,
						smrizeId			: r.smrizeId || null,      		// 담당자, 선택
						smrizeIdNm			: r.smrizeIdNm || null,  		// 담당자, 선택
					};

					let bucket = rowsData[sales];
					if (!bucket) bucket = rowsData[sales] = { PM: [], PLAN: [], DO: [] };
					bucket[item.cat].push(item);
				}
				GanttApp.state.rowsData[salesCd] = rowsData[salesCd];

	            // 특정 트랙 다시 렌더링
	            renderSingleRow(salesCd);
	    });
	}
	
	// UI 바인딩
	function bindUI() {
		$('#zoom').on('change', function () { setZoom(+this.value); });
		$('#zoom').on('input', function () { const val = $(this).val(); $('#zoomVal').text(val);});
		$('#timeline').on('wheel', function (e) { if (e.ctrlKey) { e.preventDefault(); setZoom(state.pxPerDay + (e.originalEvent.deltaY > 0 ? -2 : 2)); } });

		$('#multiple-checkboxes-prdtGrp').multiselect({ includeSelectAllOption: true }).on('change', function () { loadAndRender(); });

		$(document).on('contextmenu', '.bar', function (ev) {
		    ev.preventDefault();

			const $bar    = $(this);
			const $row    = $bar.closest('.row-track');
			const salesCd = $row.data('row');
			const cat     = $row.data('cat');
			const key     = $bar.data('key'); // 렌더링 시 부여된 키
			
			const itemArr = state.rowsData[salesCd]?.[cat] || [];
			const item    = itemArr.find(x => x.key === key);
			
			if (!item) return;			
			
			const rowMeta = state.rowDefs.find(r => r.key === salesCd) || {};
//			if (cat === 'PM' || cat === 'PLAN') {	//PM 전체일정은 입력 화면에서만 입력가능하게 제한처리
			if (cat === 'PM') {	//PM 전체일정은 입력 화면에서만 입력가능하게 제한처리
				openModal("/static/html/user/wb/wb22/WB2201P01.html", 1200, 900, "WBS 계획등록", {
					coCd: $('#coCd_S').val(),
					salesCd,
					histYn: "N",
				}, function (result) { 
//					GanttApp.loadAndRender();
					reloadAndRenderRow(salesCd);
				});
				return;
			} else {
			
				// ★ 우클릭 위치를 날짜로 환산해 payload에 포함
			    const openDateObj = mapEventToDateOnTrack(ev, $row);
			    const payload = {
			        salesCd,
			        cat,
			        item,
			        isNew		: false,
			        $track		: $row,
			        $bar		: $bar,
			        pointer		: { clientX: ev.clientX, clientY: ev.clientY, pageX: ev.pageX, pageY: ev.pageY },
			        openDateObj,
			        openDate	: openDateObj ? GanttCommon.fmt(openDateObj) : undefined,
			    };
	
			        		
			    showBarContext(ev, payload);
			}
		});
		    
		/*******************************************************************
		 *  PM 배지를 클릭할 때마다 토글되는 기능
		 *******************************************************************/
		// 배지 클릭 핸들러 (bindUI 이후 전역 위임)
		$(document).on('click', '.group-line .line-badge', function () {
			const $line  = $(this).closest('.group-line');
			const $track = $line.find('.row-track').first();
			const cat    = $track.data('cat');         // 'PM' | 'PLAN' | 'DO'
			const rowKey = $track.data('row');

			if (!rowKey || !cat) return;

			// 토글: UNSTACKED에 있으면 제거(겹침 ON), 없으면 추가(겹침 해제)
			if (UNSTACKED[cat].has(rowKey)) UNSTACKED[cat].delete(rowKey);
			else UNSTACKED[cat].add(rowKey);

			const stacked = !UNSTACKED[cat].has(rowKey);
			const items   = GanttApp.state.rowsData?.[rowKey]?.[cat] || [];
			const extra   = { stackToFirstLane: stacked, sortBars: true, keepLane: true, writeBackLane: !stacked }; // 클릭 재렌더는 정렬 ON

			CORE.renderRowSVG($track, items, GanttApp.state, makeCoreOptions(cat, $track, rowKey, extra));
		});

	}

	// PLAN이 PM 범위를 벗어나지 않도록, 같은 row에서 key 9자리 prefix가 같은 PM 바를 찾아 한계를 반환
	function getPlanBounds(prefix, rowKey) {
		const pmArr = (state.rowsData[rowKey]?.PM) || [];
		const pm = pmArr.find(x => String(x.key || '').slice(0, 9) === String(prefix));
		if (!pm) return null;
		return {
			s: GanttCommon.parse(pm.start),
			e: GanttCommon.parse(pm.end),
		};
	}

	// 페이지 로드시
	$(function () {
		root.GanttApp.setZoom(state.pxPerDay, false);
		loadAndRender();
		bindUI();
	});
	

	
	// 공개 API
	root.GanttApp = {
		state, renderAll, loadAndRender, setZoom,
		_makeCoreOptions: makeCoreOptions,
		reloadAndRenderRow
	};
	

})(window);
