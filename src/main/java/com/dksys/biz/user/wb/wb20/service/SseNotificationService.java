package com.dksys.biz.user.wb.wb20.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 결재/공유 건수 실시간 푸시 서비스 (Server-Sent Events)
 * - 사용자가 접속(top.html / mobile menu.html) 시 SseEmitter를 등록하고,
 *   결재 상신/승인/반려 또는 실시간 갱신 시 대상 사용자에게 이벤트를 푸시합니다.
 * - 수만 명 동시 접속 시에도 불필요한 반복 DB 쿼리를 차단하고 이벤트 기반으로 동작합니다.
 */
@Service
public class SseNotificationService {

	private static final Logger logger = LoggerFactory.getLogger(SseNotificationService.class);

	// 타임아웃: 30분 (연결 만료 시 브라우저의 EventSource가 자동으로 재연결을 시도함)
	private static final Long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

	// 재접속 쇄도 완화를 위해 타임아웃에 더하는 최대 지터 (0~5분)
	private static final long SSE_JITTER_MAX_MS = 5 * 60 * 1000L;

	// 사용자별 SseEmitter 목록 보관 Map (key: userId, 한 사용자가 여러 기기/탭에서 동시 접속 가능)
	private final Map<String, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

	// 건수 조회 전용 소형 스레드풀 (요청 스레드에서 DB 조회하지 않도록 분리, 포화 시 조회 작업은 버림 - 화면 캐시로 보완)
	private final ThreadPoolExecutor countExecutor = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(200), newDaemonThreadFactory("sse-count-"), new ThreadPoolExecutor.DiscardPolicy());

	@Autowired
	private WB20Svc wb20Svc;

	/**
	 * 사용자의 SSE 구독 등록
	 * @param userId 사원번호/사용자ID
	 * @param coCd 회사코드
	 * @return SseEmitter
	 */
	public SseEmitter subscribe(String userId, String coCd) {
		if (userId == null || userId.trim().isEmpty()) {
			return null;
		}

		String userKey = userId.trim();
		SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS + ThreadLocalRandom.current().nextLong(SSE_JITTER_MAX_MS + 1));

		// 연결 종료/타임아웃/에러 발생 시 자기 자신만 목록에서 제거
		emitter.onCompletion(() -> removeEmitter(userKey, emitter));
		emitter.onTimeout(() -> {
			emitter.complete();
			removeEmitter(userKey, emitter);
		});
		emitter.onError((e) -> {
			emitter.complete();
			removeEmitter(userKey, emitter);
		});

		addEmitter(userKey, emitter);

		// 1) 연결 성공 더미 이벤트 전송 (브라우저 최초 연결 확인 및 503 방지)
		try {
			emitter.send(SseEmitter.event()
					.name("connect")
					.data("connected", MediaType.TEXT_PLAIN));
		} catch (Exception e) {
			removeEmitter(userKey, emitter);
			return emitter;
		}

		// 2) 연결 즉시 최신 결재/공유 건수를 1회 푸시하여 화면 초기화 지원 (DB 조회는 별도 스레드에서 수행)
		countExecutor.execute(() -> sendLatestCount(userKey, coCd));

		return emitter;
	}

	/**
	 * 특정 사용자에게 최신 결재/공유 건수를 DB에서 산출하여 즉시 푸시
	 * @param userId 사용자ID
	 * @param coCd 회사코드
	 */
	public void sendLatestCount(String userId, String coCd) {
		sendLatestCountWithNoti(userId, coCd, null);
	}

	/**
	 * 특정 사용자에게 최신 결재/공유 건수 + 신규 결재 알림(Noti) 함께 푸시
	 * @param userId 사용자ID
	 * @param coCd 회사코드
	 * @param notiInfo 알림 상세 (senderNm, todoDiv2Nm, todoTitl, todoNo 등)
	 */
	public void sendLatestCountWithNoti(String userId, String coCd, Map<String, Object> notiInfo) {
		if (userId == null || !hasEmitter(userId)) {
			return;
		}

		try {
			Map<String, String> param = new ConcurrentHashMap<>();
			param.put("coCd", coCd != null ? coCd : "GUN");
			param.put("salesMngId", userId);
			param.put("boardParam", "BOARD");
			param.put("sanctnSttus", "N");

			Map<String, Integer> countMap = wb20Svc.selectToDoCountGrouped(param);
			Map<String, Object> pushPayload = new HashMap<>(countMap);
			if (notiInfo != null && !notiInfo.isEmpty()) {
				pushPayload.put("noti", notiInfo);
			}
			sendCountEvent(userId, pushPayload);
		} catch (Exception e) {
			logger.warn("[SSE] Failed to query and send count for user {}: {}", userId, e.getMessage());
		}
	}

	/**
	 * 특정 사용자의 모든 연결에 결재/공유 카운트 데이터 푸시 (전송 실패한 연결만 제거)
	 * @param userId 대상 사용자
	 * @param countData approvalCnt, shareCnt, (선택) noti 가 담긴 객체
	 */
	public void sendCountEvent(String userId, Object countData) {
		if (userId == null) {
			return;
		}
		CopyOnWriteArrayList<SseEmitter> list = emitters.get(userId);
		if (list == null) {
			return;
		}
		for (SseEmitter emitter : list) {
			try {
				emitter.send(SseEmitter.event()
						.name("todoCount")
						.data(countData, MediaType.APPLICATION_JSON));
			} catch (Exception e) {
				logger.debug("[SSE] Emitter disconnected for user {}: {}", userId, e.getMessage());
				removeEmitter(userId, emitter);
			}
		}
	}

	/**
	 * 결재/공유 발생 시 관련된 여러 사용자에게 일괄 카운트 갱신 트리거 (비동기 안전)
	 * @param userIds 대상 사번 목록
	 * @param coCd 회사코드
	 */
	public void triggerCountUpdate(String coCd, String... userIds) {
		triggerCountUpdateWithNoti(coCd, null, userIds);
	}

	/**
	 * 결재 상신 등 알림 정보가 수반되는 카운트 갱신 트리거
	 * @param coCd 회사코드
	 * @param notiInfo 알림 정보
	 * @param userIds 대상 사번 목록
	 */
	public void triggerCountUpdateWithNoti(String coCd, Map<String, Object> notiInfo, String... userIds) {
		if (userIds == null || userIds.length == 0) return;
		for (String uid : userIds) {
			if (uid != null && hasEmitter(uid.trim())) {
				sendLatestCountWithNoti(uid.trim(), coCd, notiInfo);
			}
		}
	}

	/**
	 * 순차/병행결재 알림 트리거:
	 * 현재 결재 순번인 사람(targetUserIdWithNoti)에게만 툴팁 알림(noti)을 띄우고,
	 * 나머지 관련된 사용자들에게는 백그라운드 카운트 갱신(무알림)만 수행
	 * @param coCd 회사코드
	 * @param targetUserIdWithNoti 실제 툴팁 알림을 받을 현재 차례 결재자 1명
	 * @param notiInfo 알림 정보
	 * @param allRelatedUserIds 전체 관련자 사번 목록
	 */
	public void triggerSequentialApprovalNoti(String coCd, String targetUserIdWithNoti, Map<String, Object> notiInfo, String... allRelatedUserIds) {
		if (targetUserIdWithNoti != null) {
			triggerSequentialApprovalNoti(coCd, Collections.singletonList(targetUserIdWithNoti), notiInfo, allRelatedUserIds);
		} else {
			triggerCountUpdate(coCd, allRelatedUserIds);
		}
	}

	/**
	 * 병행결재/합의/협조 다중 알림 지원 트리거:
	 * 현재 단계에서 동시에 결재/합의/협조해야 하는 모든 결재자(targetUserIdsWithNoti)에게 툴팁 알림(noti)을 띄우고,
	 * 나머지 관련된 사용자들에게는 백그라운드 카운트 갱신(무알림)만 수행
	 * @param coCd 회사코드
	 * @param targetUserIdsWithNoti 실제 툴팁 알림을 받을 현재 차례 결재자 목록 (병행합의/협조 등 다수 가능)
	 * @param notiInfo 알림 정보
	 * @param allRelatedUserIds 전체 관련자 사번 목록
	 */
	public void triggerSequentialApprovalNoti(String coCd, java.util.Collection<String> targetUserIdsWithNoti, Map<String, Object> notiInfo, String... allRelatedUserIds) {
		Set<String> targetSet = new HashSet<>();
		if (targetUserIdsWithNoti != null) {
			for (String tid : targetUserIdsWithNoti) {
				if (tid != null && !tid.trim().isEmpty()) {
					String key = tid.trim();
					targetSet.add(key);
					if (notiInfo != null && hasEmitter(key)) {
						sendLatestCountWithNoti(key, coCd, notiInfo);
					}
				}
			}
		}

		if (allRelatedUserIds != null) {
			for (String uid : allRelatedUserIds) {
				if (uid != null && !targetSet.contains(uid.trim()) && hasEmitter(uid.trim())) {
					sendLatestCountWithNoti(uid.trim(), coCd, null);
				}
			}
		}
	}

	/**
	 * 30초마다 모든 연결에 주석 이벤트(ping)를 보내 유휴 연결 끊김을 방지하고, 전송 실패한 연결은 제거
	 */
	@Scheduled(fixedDelay = 30000)
	public void sendHeartbeat() {
		for (Map.Entry<String, CopyOnWriteArrayList<SseEmitter>> entry : emitters.entrySet()) {
			String userKey = entry.getKey();
			for (SseEmitter emitter : entry.getValue()) {
				try {
					emitter.send(SseEmitter.event().comment("ping"));
				} catch (Exception e) {
					removeEmitter(userKey, emitter);
				}
			}
		}
	}

	@PreDestroy
	private void shutdownCountExecutor() {
		countExecutor.shutdown();
	}

	/**
	 * 사용자에게 살아있는 연결이 있는지 확인
	 * @param userId 사용자ID
	 * @return 연결이 1개 이상 있으면 true
	 */
	private boolean hasEmitter(String userId) {
		if (userId == null) {
			return false;
		}
		CopyOnWriteArrayList<SseEmitter> list = emitters.get(userId);
		return list != null && !list.isEmpty();
	}

	/**
	 * 사용자 연결 목록에 연결 추가 (목록 조회/생성과 추가를 원자적으로 수행하여 정리 작업과의 경합 방지)
	 * @param userKey 사용자ID
	 * @param emitter 추가할 연결
	 */
	private void addEmitter(String userKey, SseEmitter emitter) {
		emitters.compute(userKey, (k, list) -> {
			CopyOnWriteArrayList<SseEmitter> target = list != null ? list : new CopyOnWriteArrayList<SseEmitter>();
			target.add(emitter);
			return target;
		});
	}

	/**
	 * 사용자 연결 목록에서 해당 연결만 제거하고, 목록이 비면 키까지 제거
	 * @param userKey 사용자ID
	 * @param emitter 제거할 연결
	 */
	private void removeEmitter(String userKey, SseEmitter emitter) {
		emitters.computeIfPresent(userKey, (k, list) -> {
			list.remove(emitter);
			return list.isEmpty() ? null : list;
		});
	}

	/**
	 * 건수 조회용 데몬 스레드 팩토리
	 * @param prefix 스레드명 접두사
	 * @return ThreadFactory
	 */
	private static ThreadFactory newDaemonThreadFactory(String prefix) {
		AtomicInteger seq = new AtomicInteger(1);
		return r -> {
			Thread t = new Thread(r, prefix + seq.getAndIncrement());
			t.setDaemon(true);
			return t;
		};
	}
}
