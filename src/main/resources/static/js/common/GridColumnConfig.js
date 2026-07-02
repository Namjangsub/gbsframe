/**
 * GridColumnConfig.js
 * 로그인 사용자별 AX5Grid 컬럼 구성(순서/표시여부/너비) 저장/복원 공통 유틸
 *
 * 사용법 (화면 적용):
 *   var defaultColumns = [ ...화면 기본 컬럼 정의... ];
 *   // 그리드 init 시
 *   columns: GridColumnConfig.apply("BM0101M01", "first-grid", defaultColumns)
 *   // 편집 팝업 열기
 *   GridColumnConfig.openEditor("BM0101M01", "first-grid", defaultColumns, function(){ 그리드 재구성 });
 *
 * 저장 형식(JSON 트리): leaf {key, label, width, hidden} / group {label, groupId, columns:[...]}
 *  - 그룹헤더(다단 헤더)는 groupId(있으면) 또는 label 기준, 실제 컬럼(leaf)은 key 기준으로 병합한다.
 *    ※ 그룹 label이 동적으로 바뀌는 화면(월별 라벨 등)은 그룹 정의에 groupId를 부여할 것.
 *  - formatter/editor 등 함수 속성은 저장하지 않고 화면 기본 정의에서 가져온다.
 *
 * footSum(총계)/grouping.columns(서브토탈)가 있는 화면은 위치 기반 배열 대신
 * buildSumRow(mergedColumns, sumDef)로 최종 컬럼 순서에 맞는 배열을 생성해 사용한다.
 *   sumDef = { label: "총계"|function, labelColspan: 10(선택), sums: {key: {collector,formatter,align}, ...} }
 */
var GridColumnConfig = (function() {

	// 화면당 그리드 재구성(initView 반복 호출) 시 중복 조회 방지 캐시
	var cache = {};

	function cacheKey(pgmId, gridId) {
		return pgmId + "|" + gridId;
	}

	// 서버에 저장된 설정 조회 (동기 - 그리드 init 시점에 필요)
	function fetchConfig(pgmId, gridId) {
		if (typeof jwt === 'undefined' || !jwt || !jwt.userId) return null;
		var key = cacheKey(pgmId, gridId);
		if (key in cache) return cache[key];
		var saved = null;
		var paramObj = {"userId": jwt.userId, "pgmId": pgmId, "gridId": gridId};
		postAjaxSync("/admin/cm/cm30/selectGridConfig", paramObj, null, function(data) {
			if (data.columnConfig) {
				try {
					saved = JSON.parse(data.columnConfig);
				} catch (e) {
					saved = null;
				}
			}
		});
		cache[key] = saved;
		return saved;
	}

	// 설정 저장/삭제 후 캐시 무효화 (편집팝업에서 호출)
	function invalidate(pgmId, gridId) {
		delete cache[cacheKey(pgmId, gridId)];
	}

	// 컬럼 정의 트리 -> 저장용 메타 트리 (함수 속성 제거)
	function toMeta(columns) {
		var meta = [];
		$.each(columns || [], function(idx, col) {
			if (col.columns) {
				meta.push({"label": (typeof col.label === 'function' ? '' : col.label), "groupId": col.groupId, "columns": toMeta(col.columns)});
			} else {
				meta.push({
					"key": col.key,
					"label": col.label,
					"width": col.width,
					"hidden": !!col.hidden
				});
			}
		});
		return meta;
	}

	// 저장된 메타 트리를 화면 기본 컬럼 정의에 병합
	// - 순서: 저장된 순서 우선, 저장에 없는 신규 컬럼은 원래 순서대로 뒤에 추가
	// - leaf: key 매칭 (width/hidden 반영)
	// - group: groupId 매칭 우선(동적 label 대응), 없으면 label 매칭 (자식 재귀 병합)
	function mergeColumns(defCols, savedCols) {
		if (!savedCols) return defCols;
		var used = [];
		var result = [];

		$.each(savedCols, function(i, saved) {
			var matchIdx = -1;
			$.each(defCols, function(j, def) {
				if (used.indexOf(j) > -1) return true;
				if (saved.columns) {
					if (def.columns && (saved.groupId || def.groupId
							? saved.groupId === def.groupId
							: def.label === saved.label)) { matchIdx = j; return false; }
				} else {
					if (!def.columns && def.key === saved.key) { matchIdx = j; return false; }
				}
			});
			if (matchIdx > -1) {
				used.push(matchIdx);
				var def = defCols[matchIdx];
				var col = $.extend({}, def);
				if (saved.columns && def.columns) {
					col.columns = mergeColumns(def.columns, saved.columns);
				} else {
					if (typeof saved.width === 'number' && saved.width > 0) col.width = saved.width;
					col.hidden = !!saved.hidden;
				}
				result.push(col);
			}
		});
		// 저장 이후 화면에 새로 추가된 컬럼은 기본 순서대로 뒤에 붙인다
		$.each(defCols, function(j, def) {
			if (used.indexOf(j) === -1) result.push(def);
		});
		return result;
	}

	// 그리드 init 시 호출: 저장된 사용자 설정이 있으면 병합된 컬럼 배열 반환
	function apply(pgmId, gridId, defaultColumns) {
		var saved = fetchConfig(pgmId, gridId);
		return mergeColumns(defaultColumns, saved);
	}

	// 트리에서 화면에 보이는 leaf 컬럼만 순서대로 평탄화 (colGroup과 동일 기준: hidden 제외)
	function flattenVisible(columns) {
		var leaves = [];
		$.each(columns || [], function(idx, col) {
			if (col.hidden) return true;
			if (col.columns) {
				leaves = leaves.concat(flattenVisible(col.columns));
			} else {
				leaves.push(col);
			}
		});
		return leaves;
	}

	// footSum(총계)/grouping.columns(서브토탈)용 위치 배열을 최종 컬럼 순서 기반으로 생성
	// sumDef.sums에 정의된 key가 보이는 위치에 합계 셀을 배치하고, 그 외는 빈 셀로 채운다.
	// 합계대상 컬럼을 숨기면 해당 합계도 자연히 제외된다.
	function buildSumRow(mergedColumns, sumDef) {
		var leaves = flattenVisible(mergedColumns);
		var firstSumIdx = -1;
		$.each(leaves, function(i, leaf) {
			if (sumDef.sums[leaf.key]) { firstSumIdx = i; return false; }
		});
		if (firstSumIdx < 1) firstSumIdx = 1;

		var labelColspan = firstSumIdx;
		if (typeof sumDef.labelColspan === 'number' && sumDef.labelColspan > 0 && sumDef.labelColspan < firstSumIdx) {
			labelColspan = sumDef.labelColspan;
		}

		var row = [{"label": sumDef.label, "colspan": labelColspan, "align": sumDef.labelAlign || "center"}];
		for (var i = labelColspan; i < leaves.length; i++) {
			var def = sumDef.sums[leaves[i].key];
			row.push(def ? $.extend({"key": leaves[i].key}, def) : {});
		}
		return row;
	}

	// 컬럼 편집 팝업 열기. onApplied: 저장/초기화 후 화면에서 그리드를 재구성하는 콜백
	function openEditor(pgmId, gridId, defaultColumns, onApplied) {
		var current = apply(pgmId, gridId, defaultColumns);
		var paramObj = {
			"pgmId": pgmId,
			"gridId": gridId,
			"metaColumns": toMeta(current)
		};
		openModal("/static/html/cmn/modal/gridColumnEdit.html", 560, 620, "그리드 컬럼 설정", paramObj, onApplied);
	}

	return {
		"apply": apply,
		"openEditor": openEditor,
		"toMeta": toMeta,
		"buildSumRow": buildSumRow,
		"invalidate": invalidate
	};
})();
