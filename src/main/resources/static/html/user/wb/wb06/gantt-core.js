// ===== GanttCore : 행 렌더/드래그·리사이즈/복사 =====
// 즉시 실행 함수 표현식(IIFE, Immediately Invoked Function Expression) 패턴
// 함수 안에 변수를 정의하면 var, let, const 모두 지역 변수가 됨
// 내부에서 root === window
// 전역 오염을 막으면서도, 필요시 root.xxx 형태로 API를 공개
(function (root) {
	'use strict';

	const GC = root.GanttCommon;
	const DRAG_THRESH = 3;

	function layoutSequential(items, state, options = {}) {
		// 옵션 기본값
		const doSort = (options.sortBars !== false);      // 기본: 정렬 O
		const keepLane = !!options.keepLane;              // 기존 레인 유지
		const stackToFirstLane = !!options.stackToFirstLane; // 모두 1줄 겹치기
		
	    // 1) 시작/종료 보정(뷰 범위로 클램프) + 정규화
	    const normalized = items.map((it) => {
	        const s = GC.clampDate(GC.parse(it.start), state.viewStart, state.viewEnd);
	        const e = GC.clampDate(GC.parse(it.end),   state.viewStart, state.viewEnd);
	        return { ...it, s, e };
	    })
	    // 2) 뷰에 실제로 보이는 것만 남김
	    .filter(it => it.e >= state.viewStart && it.s <= state.viewEnd);

	    
	    // 3) 정렬: 시작일 ↑, (동시 시작 시) 종료일 ↑, (그래도 같으면) 라벨/키
	    if (doSort) {
			normalized.sort((a, b) => {
				const ds = a.s - b.s; if (ds) return ds;
				const de = a.e - b.e; if (de) return de;
				const la = String(a.label ?? a.key ?? '');
				const lb = String(b.label ?? b.key ?? '');
				return la.localeCompare(lb);
			});
		}

	    // 4) lane 부여(작은 index일수록 상단), 색 인덱스는 예시대로 유지
		return normalized.map((it, idx) => {
			let lane;
			if (stackToFirstLane) {
				// 겹쳐 보기: 항상 0
				lane = 0;
			} else if (keepLane && Number.isFinite(it.__laneSaved)) {
				// 다중 레인 모드에서 저장해둔 레인 우선
				lane = it.__laneSaved;
			} else if (keepLane && Number.isFinite(it.__lane)) {
				// 혹시 __lane만 있는 경우(초기 1회) 백업
				lane = it.__lane;
			} else {
				// 신규 배치
				lane = idx;
			}
			return { ...it, lane, colorIdx: idx % 3 };
		});
	}

	function renderRowSVG($track, items, state, options = {}) {
		$track.children('svg').remove();

		const totalDays = GC.days(state.viewStart, state.viewEnd) + 1;
		const w = totalDays * state.pxPerDay;
		const laneH = parseInt(getComputedStyle(document.documentElement).getPropertyValue('--lane-height'));
		const laid = layoutSequential(items, state, {sortBars: (options.sortBars !== false), keepLane: !!options.keepLane, stackToFirstLane: !!options.stackToFirstLane});
		const lanes = (options.stackToFirstLane === true) ? 1 : Math.max(1, laid.length);
		const svgH = laneH * lanes;
		// ★ 레인 write-back 제어: 겹쳐 보기에서는 __laneSaved 덮어쓰지 않음
		const writeBackLane = (options.writeBackLane !== false); // 기본 true
		try {
			if (writeBackLane) {
				const laneMap = new Map(laid.map(x => [x.key, x.lane]));
				items.forEach(it => {
					const ln = laneMap.get(it.key);
					if (!Number.isFinite(ln)) return;
					// 현재 레인은 항상 기록
					it.__lane = ln;
					// 다중 레인 모드에서만 복원용 레인도 갱신
					if (!options.stackToFirstLane) {
						it.__laneSaved = ln;
					}
				});
			}
		} catch (e) {}
		
		
		const svg = GC.makeSvgEl('svg', { width: w, height: svgH });
		$track.css({ width: w + 'px', height: svgH + 'px', minHeight: svgH + 'px' }).append(svg);

		const xFromDate = GC.makeXFromDate(state);
		const today = GC.sod(new Date());
		const cat = $track.data('cat');
		const salesCd = $track.data('row'); // 트랙에서 현재 sales 코드 취득

		laid.forEach((it) => {
			const x0 = xFromDate(it.s);
			const bw = Math.max(12, (GC.days(it.s, it.e) + 1) * state.pxPerDay);
			const y = (options.stackToFirstLane === true) ? 0 : (it.lane * laneH);
			const isExpired = it.e < today;

			// 동일 sales의 DO 버킷에서 같은 key의 완료 여부 확인
			const bucket = state.rowsData?.[salesCd];
			const hasDoneInDO = Array.isArray(bucket?.DO)
				? bucket.DO.some(d => String(d.key) === String(it.key) && d.doneYn === 'Y')
				: false;


			// 조건: PLAN && 종료일 경과 && DO 완료 아님 → overdue
			const isOverduePlan = (cat === 'PLAN' && isExpired && !hasDoneInDO);	
				
			const isOwn = (String(it.wbsPlanMngId || '') === String(jwt?.userId || ''));
//			const isOwn =
//			    (String(it.wbsPlanMngId || '') === String(jwt?.userId || '')) ||
//			    (String(it.smrizeId     || '') === String(jwt?.userId || '')); // PM 담당자까지 포함하고 싶을 때

			const cls = ['bar', `ci-${it.colorIdx}`];
			if (isOverduePlan)      cls.push('overdue');
			else if (isExpired)     cls.push('expired'); // 필요 시 유지
			
			if (isOwn) cls.push('own');            // 담당자 본인건 표시
			
			const rect = GC.makeSvgEl('rect', {
				x: x0, y: y, width: bw, height: laneH, rx: 8, ry: 8,
				class: cls.join(' '), 'data-key': it.key           // ← 담당자 본인건 표시
			});
			rect.style.pointerEvents = 'auto';

			const label = GC.makeSvgEl('text', { x: x0 + bw / 2, y: y + laneH / 2, class: 'bar-label' });
			label.textContent = it.label;

			const handleW = 6;
			const lh = GC.makeSvgEl('rect', { x: x0, y: y, width: handleW, height: laneH, class: 'handle', 'data-dir': 'w', 'data-key': it.key });
			const rh = GC.makeSvgEl('rect', { x: x0 + bw - handleW, y: y, width: handleW, height: laneH, class: 'handle', 'data-dir': 'e', 'data-key': it.key });
			lh.style.pointerEvents = 'auto';
			rh.style.pointerEvents = 'auto';
			
			svg.appendChild(rect); svg.appendChild(label); svg.appendChild(lh); svg.appendChild(rh);

			const locked = !GC.rules.canDragOrResize(cat, it);
			if (locked) { rect.classList.add('locked'); rect.style.cursor = 'not-allowed'; rect.style.pointerEvents = 'auto'; }
			else { rect.classList.remove('locked'); rect.style.cursor = 'move'; }
			// 마우스 왼쪽 클릭 시 툴팁 표시
//			rect.addEventListener('pointerdown', (ev) => { if (ev.button !== 0) return; /* 왼쪽 버튼만 */ GC.showTip(ev.clientX, ev.clientY, GC.fmtRange(it.s, it.e, it.label, GC.days)); });
			rect.addEventListener('pointerdown', (ev) => {
			    if (ev.button !== 0) return; // 왼쪽 버튼만

			    const text = `
			    	<b>${it.label}</b><br>
			    	기간: ${GC.fmt(it.s)} ~ ${GC.fmt(it.e)} (${GC.days(it.s, it.e) + 1}일)<br>
			    	담당자: ${it.wbsPlanMngIdNm || '-'}<br>
			    	완료여부: ${it.doneYn === 'Y' ? '완료' : '미완료'}
			    	`;
			    	GC.showTip(ev.clientX, ev.clientY, text);
			});
			// 마우스 버튼 놓으면 툴팁 숨김
			rect.addEventListener('pointerup', (ev) => { if (ev.button !== 0) return; /* 왼쪽 버튼만 */ GC.hideTip(); });
			
			enableDragResize(svg, rect, label, lh, rh, it, y, laneH, state, { cat, $track, ...options, locked });
		});

		return svg;
	}

	// SVG별 공유 상태 확보
	function ensureSvgState(svg) {
		if (!svg.__gcState) {
			svg.__gcState = {
				drag: null,      // 드래그 상태 공유
				binded: false    // 전역 핸들러 1회 바인딩 플래그
			};
		}
		return svg.__gcState;
	}

	function enableDragResize(svg, rect, label, lh, rh, it, baseY, laneH, state, opts) {
		const { locked, cat, $track, onApplyChange, onCopy, onContext, getPlanBounds } = opts;

		let dragState = null, didDrag = false, lastPointerX = null;
		const xFromDate = GC.makeXFromDate(state);
		const dateFromX = GC.makeDateFromX(state);

		// 문서 레벨 핸들러 (드래그 중 전용)
		function bindDocEvents() {
			$(document)
				.on('pointermove.gcore', onPointerMove)
				.on('pointerup.gcore pointercancel.gcore', onPointerUp);
		}
		function unbindDocEvents() {
			$(document).off('.gcore');
		}

		function startDrag(mode) {
			return (ev) => {
				// 오른쪽 버튼 또는 잠김 상태는 무시
				if (ev.button === 2 || locked) { ev.preventDefault(); return; }
				// 왼쪽(0)만 허용, 그 외(button=1/2…) 또는 잠김은 무시
//				if (ev.button !== 0 || locked) { ev.preventDefault(); return; }

				ev.stopPropagation();
				ev.preventDefault();
				ev.target.setPointerCapture?.(ev.pointerId || 1);

				didDrag = false;
				lastPointerX = pageX(ev);

				dragState = {
					mode, 
					key				: it.key, 
					name			: it.label,
					startX			: pageX(ev), 
					origStart		: it.s, 
					origEnd			: it.e,
					widthDays		: Math.max(0, GC.days(it.s, it.e)), // inclusive 계산은 pointerup에서 처리
					svg, rect, label, lh, rh,
					pointerId		: ev.pointerId || 1, target: ev.target,
					baseY, laneH, 
					item			: it,

				    sourceTrack		: $track,
				    sourceSalesCd	: $track?.data('row'),
				    sourceCat		: cat,
				    targetTrack		: null,
				    targetSalesCd	: null,
				    targetCat		: null,
				    options			: opts
				};

				svg.classList.add('dragging');
				// 왼쪽 버튼 드래그에서만 툴팁
//				GC.showTip(ev.clientX, ev.clientY, GC.fmtRange(it.s, it.e, it.label, GC.days));
				const text = `
					<b>${it.label}</b><br>
					기간: ${GC.fmt(it.s)} ~ ${GC.fmt(it.e)} (${GC.days(it.s, it.e) + 1}일)<br>
					담당자: ${it.wbsPlanMngIdNm || '-'}<br>
					완료여부: ${it.doneYn === 'Y' ? '완료' : '미완료'}
					`;
				GC.showTip(ev.clientX, ev.clientY, text);

				// 문서 레벨로 바인딩 (브라우저별 setPointerCapture 이슈 대비)
				bindDocEvents();
			};
		}

		rect.addEventListener('pointerdown', startDrag('move'));
		rh.addEventListener('pointerdown', startDrag('e'));
		lh.addEventListener('pointerdown', startDrag('w'));

		function onPointerMove(e) {
			if (!dragState) return;
			const options = dragState.options;
			const dx = pageX(e) - dragState.startX;
			if (Math.abs(pageX(e) - lastPointerX) > 3) didDrag = true;
			const dd = Math.round(dx / state.pxPerDay);
			// 현재 가상 s/e
			let ns = dragState.origStart;
			let ne = dragState.origEnd;

			if (dragState.mode === 'move') {
				ns = dateFromX(xFromDate(dragState.origStart) + dd * state.pxPerDay);
				ne = new Date(+ns + dragState.widthDays * GC.MS);
			} else if (dragState.mode === 'e') {
				ne = new Date(+dragState.origEnd + dd * GC.MS);
				// 최소 1일 보장
				if (GC.days(dragState.origStart, ne) < 0) ne = dragState.origStart;
			} else if (dragState.mode === 'w') {
				ns = new Date(+dragState.origStart + dd * GC.MS);
				// 최소 1일 보장
				if (GC.days(ns, dragState.origEnd) < 0) ns = dragState.origEnd;
			}

			// PLAN은 PM 범위로 클램프 (동일 prefix-9 PM 바 기준)
			if (cat === 'PLAN' && typeof getPlanBounds === 'function') {
				const pm = getPlanBounds(String(dragState.key||'').slice(0,9));
				if (pm && pm.s && pm.e) {
					// inclusive 범위 안으로 강제
					if (ns < pm.s) ns = pm.s;
					if (ne > pm.e) ne = pm.e;
					// 양끝 모두 붙을 때 최소 1일 유지
					if (GC.days(ns, ne) < 0) ne = ns;
				}
			}

			// 전역 뷰 범위로도 클램프
			ns = GC.clampDate(ns, state.viewStart, state.viewEnd);
			ne = GC.clampDate(ne, state.viewStart, state.viewEnd);

			syncBarVisual(dragState, ns, ne, state);
			GC.moveTip(e.clientX, e.clientY, GC.fmtRange(ns, ne, dragState.name, GC.days));
			


			// === 포인터 아래의 row-track 탐지 및 하이라이트 ===
			if (dragState.mode === 'move') {
			    const $under = findTrackUnderPointer(e.clientX, e.clientY);
			    // 기존 하이라이트 해제
			    if (dragState.targetTrack && (!$under || !isSameTrack(dragState.targetTrack, $under))) {
			        highlightTrack(dragState.targetTrack, false);
			    }
			    if ($under && !isSameTrack($under, dragState.sourceTrack)) {
			        dragState.targetTrack  = $under;
			        dragState.targetSalesCd = $under.data('row');
			        dragState.targetCat     = $under.data('cat');
			        highlightTrack($under, true);
			    } else {
			        dragState.targetTrack  = null;
			        dragState.targetSalesCd = null;
			        dragState.targetCat     = null;
			    }
			}
		}

		function onPointerUp(e) {
			if (!dragState) return;
			const options = dragState.options;
			const nowX = parseFloat(dragState.rect.getAttribute('x'));
			const nowW = parseFloat(dragState.rect.getAttribute('width'));
			
			let s = dateFromX(nowX);
			// width → 일수(inclusive) 역산
			let span = Math.max(1, Math.round(nowW / state.pxPerDay)); // 최소 1칸
			let eDate = new Date(+s + (span - 1) * GC.MS);

			// PLAN은 PM 범위로 한 번 더 보정
			if (cat === 'PLAN' && typeof getPlanBounds === 'function') {
				const prefix9 = String(dragState.key || '').slice(0, 9);
				const pm = getPlanBounds(prefix9);
				if (pm && pm.s && pm.e) {
					if (s < pm.s) s = pm.s;
					if (eDate > pm.e) eDate = pm.e;
					// 최소 1일
					if (GC.days(s, eDate) < 0) eDate = s;
				}
			}

			// 실제 반영

			if (!didDrag) {
			    // 클릭만 한 경우: 아무 것도 하지 않음
			} else {
			    // 교차 트랙으로 움직였는지 판단
			    const movedToAnotherRow = !!dragState.targetTrack && !isSameTrack(dragState.targetTrack, dragState.sourceTrack) && dragState.mode === 'move';
			
			    if (movedToAnotherRow) {
			        // 하이라이트 해제
			        highlightTrack(dragState.targetTrack, false);
			        // 정책 판단 및 처리 위임 (APP 쪽 onRowChange)
			        const ok = options?.onRowChange?.({
			            key:  dragState.key,
			            item: dragState.item,
			            src:  { salesCd: dragState.sourceSalesCd, cat: dragState.sourceCat, $track: dragState.sourceTrack },
			            dst:  { salesCd: dragState.targetSalesCd, cat: dragState.targetCat,  $track: dragState.targetTrack  },
			            range:{ s, e: eDate },
			            copy: undefined
			        });
			        if (ok === false) {
			            // 스냅백: 원본만 재그리기
			            const rows = options.state?.rowsData?.[dragState.sourceSalesCd]?.[dragState.sourceCat] || [];
			            root.GanttCore.renderRowSVG(dragState.sourceTrack, rows, state, options);
			        }
			    } else {
			        // 같은 트랙 내 이동/리사이즈: copy 키(CTRL/ALT) 반영
			        const copy = (e.ctrlKey || e.altKey);
			        if (!copy) onApplyChange?.(dragState, s, eDate, $track);
			        else onCopy?.(dragState, s, eDate, $track);
			    }
			}

			try { dragState.target.releasePointerCapture(dragState.pointerId); } catch {}
			svg.classList.remove('dragging');
			dragState = null;
			GC.hideTip();
			unbindDocEvents();
		}

		// 컨텍스트 메뉴
		rect.addEventListener('contextmenu', (ev) => {
			if (dragState) return;
			onContext?.(ev, { cat, item: it, key: it.key, label: it.label, start: it.s, end: it.e, $track });
		});
		
		function triggerEdit(ev) {
			if (dragState) return;          // 드래그 중이면 무시
			ev.stopPropagation();
			ev.preventDefault();
			onContext?.(ev, { cat, item: it, key: it.key, label: it.label, start: it.s, end: it.e, $track, action: 'edit' });
		}
		
		// 더블클릭 → edit 액션 트리거
		rect.addEventListener('dblclick', triggerEdit);
		label.addEventListener('dblclick', triggerEdit); // 라벨 위 더블클릭도 처리
	}

	function findTrackUnderPointer(clientX, clientY) {
	    const el = document.elementFromPoint(clientX, clientY);
	    if (!el) return null;
	    const t = $(el).closest('.row-track');
	    return t.length ? t : null;
	}
	function isSameTrack($a, $b) { return ($a && $b && $a[0] === $b[0]); }
	function highlightTrack($t, on) { if ($t && $t.length) $t.toggleClass('drop-target', !!on); }
	

	function syncBarVisual(ds, s, e, state) {
		s = GC.clampDate(s, state.viewStart, state.viewEnd);
		e = GC.clampDate(e, state.viewStart, state.viewEnd);
		const xFromDate = GC.makeXFromDate(state);
		const x = xFromDate(s);
		const w = Math.max(12, (GC.days(s, e) + 1) * state.pxPerDay);
		const handleW = 6;

		ds.rect.setAttribute('x', x);
		ds.rect.setAttribute('width', w);
		ds.label.setAttribute('x', x + w / 2);
		ds.label.setAttribute('y', ds.baseY + ds.laneH / 2);
		ds.lh.setAttribute('x', x);
		ds.lh.setAttribute('y', ds.baseY);
		ds.rh.setAttribute('x', x + w - handleW);
		ds.rh.setAttribute('y', ds.baseY);
	}

	function pageX(e) {
		const oe = e.originalEvent || e;
		return oe.touches ? oe.touches[0].pageX : oe.pageX;
	}

	root.GanttCore = { renderRowSVG };
})(window);
