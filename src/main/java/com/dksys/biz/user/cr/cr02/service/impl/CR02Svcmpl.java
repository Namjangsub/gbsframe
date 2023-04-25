package com.dksys.biz.user.cr.cr02.service.impl;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.user.cr.cr01.mapper.CR01Mapper;
import com.dksys.biz.user.cr.cr02.mapper.CR02Mapper;
import com.dksys.biz.user.cr.cr02.service.CR02Svc;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Service
@Transactional(rollbackFor = Exception.class)
public class CR02Svcmpl implements CR02Svc {
	@Autowired
	CR02Mapper cr02Mapper;

	@Autowired
	CM08Svc cm08Svc;

	@Override
	public int selectOrdrsCount(Map<String, String> param) {
		return cr02Mapper.selectOrdrsCount(param);
	}

	@Override
	public List<Map<String, Object>> selectOrdrsList(Map<String, String> param) {
		return cr02Mapper.selectOrdrsList(param);
	}

	@Override
	public Map<String, Object> selectOrdrsInfo(Map<String, String> paramMap) {

		Map<String, Object> ordrsInfo = cr02Mapper.selectOrdrsInfo(paramMap);

		return ordrsInfo;


	}
	@Override
	public String selectMaxOrdrsNo(Map<String, String> param) {
		return cr02Mapper.selectMaxOrdrsNo(param);
	}
	@Override
	public void insertOrdrs(Map<String, String> param,MultipartHttpServletRequest mRequest){
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		param.put("ordrsNo",selectMaxOrdrsNo(param));
		cr02Mapper.insertOrdrs(param);
		List<Map<String, String>> planArr = gson.fromJson(removeEmptyObjects(param.get("planArr")), mapList);
		for (Map<String, String> planMap : planArr) {
			planMap.put("coCd", param.get("coCd"));
			planMap.put("ordrsNo", param.get("ordrsNo"));
			planMap.put("estNo", param.get("estNo"));
			planMap.put("userId", param.get("userId"));
			planMap.put("pgmId", param.get("pgmId"));
			planMap.put("currUnit", param.get("currUnit"));

			cr02Mapper.insertClmnPlanHis(planMap);
			cr02Mapper.insertClmnPlan(planMap);
		}

		List<Map<String, String>> detailArr = gson.fromJson(removeEmptyObjects(param.get("detailArr")), mapList);

		for (Map<String, String> detailMap : detailArr) {

			detailMap.put("coCd", param.get("coCd"));
			detailMap.put("ordrsNo", param.get("ordrsNo"));
			detailMap.put("estNo", param.get("estNo"));
			detailMap.put("userId", param.get("userId"));
			detailMap.put("pgmId", param.get("pgmId"));
			detailMap.put("currUnit", param.get("currUnit"));

			cr02Mapper.insertOrdrsDetail(detailMap);
		}

		for (int i = 0; i < mRequest.getFiles("files").size(); i++) {
			String nodeIdKey = "nodeId_" + i;
			String nodeId = mRequest.getParameter(nodeIdKey);

			// 각 파일에 대한 fileTrgtKey를 가져옵니다.
			String fileTrgtTyp = mRequest.getParameter("fileTrgtTyp_" + i);

			// 각 파일에 대해 uploadFile 메소드를 호출하며, nodeId와 fileTrgtKey를 인자로 전달합니다.
			cm08Svc.uploadFile(fileTrgtTyp, param.get("ordrsNo"), mRequest, nodeId);
		}



	}

	@Override
	public void updateOrdrs(Map<String, String> param){

		cr02Mapper.insertOrdrs(param);
		cr02Mapper.insertOrdrsDetail(param);
		cr02Mapper.insertClmnPlan(param);


	}

	public static String removeEmptyObjects(String jsonArrayString) {
		if (jsonArrayString == null || !jsonArrayString.startsWith("[") || !jsonArrayString.endsWith("]")) {
			return jsonArrayString;
		}

		JsonParser parser = new JsonParser();
		JsonArray jsonArray = parser.parse(jsonArrayString).getAsJsonArray();
		JsonArray filteredJsonArray = new JsonArray();

		for (JsonElement jsonElement : jsonArray) {
			if (jsonElement.isJsonObject()) {
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				if (!jsonObject.entrySet().isEmpty()) {
					filteredJsonArray.add(jsonObject);
				}
			} else if (!jsonElement.isJsonNull()) {
				filteredJsonArray.add(jsonElement);
			}
		}

		Gson gson = new Gson();
		return gson.toJson(filteredJsonArray);
	}



	// ...
}