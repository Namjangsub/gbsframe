package com.dksys.biz.user.wb.wb20.service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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

	// 사용자별 SseEmitter 보관 Map (동시성 보장을 위해 ConcurrentHashMap 사용, key: userId)
	private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

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
		SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

		// 연결 종료/타임아웃/에러 발생 시 Map에서 정리
		emitter.onCompletion(() -> emitters.remove(userKey));
		emitter.onTimeout(() -> {
			emitter.complete();
			emitters.remove(userKey);
		});
		emitter.onError((e) -> {
			emitter.complete();
			emitters.remove(userKey);
		});

		emitters.put(userKey, emitter);

		// 1) 연결 성공 더미 이벤트 전송 (브라우저 최초 연결 확인 및 503 방지)
		try {
			emitter.send(SseEmitter.event()
					.name("connect")
					.data("connected", MediaType.TEXT_PLAIN));
		} catch (IOException e) {
			emitters.remove(userKey);
		}

		// 2) 연결 즉시 최신 결재/공유 건수를 1회 푸시하여 화면 초기화 지원
		sendLatestCount(userKey, coCd);

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
		if (userId == null || !emitters.containsKey(userId)) {
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
	 * 특정 사용자에게 결재/공유 카운트 데이터 푸시
	 * @param userId 대상 사용자
	 * @param countData approvalCnt, shareCnt, (선택) noti 가 담긴 객체
	 */
	public void sendCountEvent(String userId, Object countData) {
		SseEmitter emitter = emitters.get(userId);
		if (emitter != null) {
			try {
				emitter.send(SseEmitter.event()
						.name("todoCount")
						.data(countData, MediaType.APPLICATION_JSON));
			} catch (Exception e) {
				logger.debug("[SSE] Emitter disconnected for user {}: {}", userId, e.getMessage());
				emitters.remove(userId);
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
			if (uid != null && emitters.containsKey(uid.trim())) {
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
					if (notiInfo != null && emitters.containsKey(key)) {
						sendLatestCountWithNoti(key, coCd, notiInfo);
					}
				}
			}
		}

		if (allRelatedUserIds != null) {
			for (String uid : allRelatedUserIds) {
				if (uid != null && !targetSet.contains(uid.trim()) && emitters.containsKey(uid.trim())) {
					sendLatestCountWithNoti(uid.trim(), coCd, null);
				}
			}
		}
	}
}
