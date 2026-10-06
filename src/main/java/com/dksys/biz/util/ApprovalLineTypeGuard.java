package com.dksys.biz.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApprovalLineTypeGuard {
	private static final Logger logger = LoggerFactory.getLogger(ApprovalLineTypeGuard.class);

	private ApprovalLineTypeGuard() {
	}

	public static List<Map<String, String>> moveStagePostToEnd(List<Map<String, String>> approvalList, String label) {
		if (approvalList == null || approvalList.isEmpty()) {
			return approvalList;
		}

		List<Integer> slots = new ArrayList<>();
		List<Map<String, String>> ordered = new ArrayList<>();
		List<Map<String, String>> posts = new ArrayList<>();
		boolean moved = false;

		for (int i = 0; i < approvalList.size(); i++) {
			Map<String, String> item = approvalList.get(i);
			if (isShareLine(item)) {
				continue;
			}
			slots.add(i);
			if (isPost(item)) {
				posts.add(item);
			} else {
				if (!posts.isEmpty()) {
					moved = true;
				}
				ordered.add(item);
			}
		}

		if (!moved) {
			return approvalList;
		}

		ordered.addAll(posts);
		List<Map<String, String>> result = new ArrayList<>(approvalList);
		for (int k = 0; k < slots.size(); k++) {
			result.set(slots.get(k), ordered.get(k));
		}

		logger.warn("결재선 후결 재정렬: label={}, postCount={}", label, posts.size());
		return result;
	}

	public static void convertPostToAppr(List<Map<String, String>> approvalList, String label) {
		if (approvalList == null || approvalList.isEmpty()) {
			return;
		}

		int convertCount = 0;
		for (Map<String, String> item : approvalList) {
			if (!isShareLine(item) && isPost(item)) {
				item.put("lineType", "APPR");
				convertCount++;
			}
		}

		if (convertCount > 0) {
			logger.warn("결재선 후결 변환: label={}, converted_count={}", label, convertCount);
		}
	}

	private static boolean isShareLine(Map<String, String> item) {
		if (item == null) {
			return false;
		}
		String gb = item.get("gb");
		String todoDiv1CodeId = item.get("todoDiv1CodeId");
		String todoDiv2CodeId = item.get("todoDiv2CodeId");

		return "공유".equals(gb)
			|| "TODODIV10".equals(todoDiv1CodeId)
			|| (todoDiv2CodeId != null && todoDiv2CodeId.startsWith("TODODIV1"));
	}

	private static boolean isPost(Map<String, String> item) {
		String t = item.get("lineType");
		return t != null && "POST".equals(t.trim());
	}
}
