/*
 * 결재 건수 SSE 리더 선출 공용 모듈
 * - navigator.locks 로 사용자당 리더 탭 1개만 EventSource 연결, 나머지 탭은 BroadcastChannel 로 수신
 * - navigator.locks 미지원: 탭마다 직접 연결(채널 미사용)
 * - BroadcastChannel 미지원: 리더만 onTodoCount 호출 (나머지 탭은 localStorage storage 이벤트로 동기화)
 */
(function (global) {
	'use strict';

	if (global.SseTodoLeader) return;

	var LOCK_PREFIX = 'gbs-sse-todo-';
	var CHANNEL_PREFIX = 'gbs-sse-todo-';
	var WAKE_MSG = '__wake__';
	var REQ_MSG = '__req__';
	var cur = null;

	function parseData(raw) {
		try {
			return JSON.parse(raw);
		} catch (err) {
			console.warn('[SSE] parse error:', err);
			return null;
		}
	}

	function notify(s, obj) {
		if (!s.onTodoCount) return;
		try {
			s.onTodoCount(obj);
		} catch (err) {
			console.warn('[SSE] onTodoCount error:', err);
		}
	}

	function closeEs(s) {
		if (s.es) {
			try { s.es.close(); } catch (e) {}
			s.es = null;
		}
	}

	function openEs(s) {
		closeEs(s);
		try {
			var es = new EventSource(s.url);
			s.es = es;
			es.addEventListener('todoCount', function (e) {
				if (!e.data || cur !== s) return;
				var obj = parseData(e.data);
				if (!obj) return;
				notify(s, obj);
				if (s.channel) s.channel.postMessage(e.data);
				// 신규 팔로워 탭 초기화용 최근 건수 보관 (noti는 토스트 재표출 방지를 위해 제외)
				delete obj.noti;
				s.last = JSON.stringify(obj);
			});
			es.onerror = function () {
				if (this && this.readyState === EventSource.CLOSED) {
					closeEs(s);
				}
			};
		} catch (err) {
			console.warn('[SSE] init error:', err);
			s.es = null;
		}
	}

	function enqueue(s) {
		s.queued = true;
		s.abort = typeof AbortController === 'function' ? new AbortController() : null;
		var opts = s.abort ? { signal: s.abort.signal } : {};
		navigator.locks.request(LOCK_PREFIX + s.userId, opts, function () {
			if (cur !== s) return;
			return new Promise(function (resolve) {
				s.release = resolve;
				s.leader = true;
				openEs(s);
			});
		}).catch(function () {});
	}

	function attachChannel(s) {
		s.channel = new BroadcastChannel(CHANNEL_PREFIX + s.userId);
		s.channel.onmessage = function (ev) {
			if (cur !== s) return;
			if (ev.data === WAKE_MSG) {
				if (s.leader && !s.es) openEs(s);
				return;
			}
			if (ev.data === REQ_MSG) {
				// 신규/복귀 팔로워의 현재값 요청 → 리더가 최근 건수 응답
				if (s.leader && s.last) s.channel.postMessage(s.last);
				return;
			}
			if (s.leader || typeof ev.data !== 'string') return;
			var obj = parseData(ev.data);
			if (obj) notify(s, obj);
		};
	}

	function start(opts) {
		if (!opts || !opts.url || !opts.userId) return;
		if (cur && cur.userId !== String(opts.userId)) stop();

		if (!cur) {
			var solo = !(navigator.locks && typeof navigator.locks.request === 'function') || typeof global.BroadcastChannel !== 'function';
			cur = {
				userId: String(opts.userId),
				url: opts.url,
				onTodoCount: null,
				es: null,
				channel: null,
				solo: solo,
				leader: false,
				last: null,
				queued: false,
				release: null,
				abort: null
			};
			if (!solo && typeof global.BroadcastChannel === 'function') {
				attachChannel(cur);
			}
		}

		var s = cur;
		s.onTodoCount = opts.onTodoCount || null;

		if (s.solo) {
			s.leader = true;
			if (!s.es) openEs(s);
			return;
		}
		if (s.leader) {
			if (!s.es) openEs(s);
			return;
		}
		if (s.channel) s.channel.postMessage(REQ_MSG);
		if (s.queued) {
			if (s.channel) s.channel.postMessage(WAKE_MSG);
			return;
		}
		enqueue(s);
	}

	function stop() {
		var s = cur;
		if (!s) return;
		cur = null;
		if (s.abort) {
			try { s.abort.abort(); } catch (e) {}
		}
		if (s.release) {
			s.release();
			s.release = null;
		}
		closeEs(s);
		if (s.channel) {
			try { s.channel.close(); } catch (e) {}
			s.channel = null;
		}
	}

	function isActive() {
		return !!cur;
	}

	global.SseTodoLeader = {
		start: start,
		stop: stop,
		isActive: isActive
	};
})(window);
