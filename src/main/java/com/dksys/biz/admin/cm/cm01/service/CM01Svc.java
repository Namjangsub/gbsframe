package com.dksys.biz.admin.cm.cm01.service;

import java.util.List;
import java.util.Map;

public interface CM01Svc {

    public List<Map<String, String>> selectAuthList();

	public int insertAuth(Map<String, String> param);

	public int deleteAuth(Map<String, String> param);

	public int updateAuth(Map<String, String> param);

	public int updateAuthRole(Map<String, String> param);

	public List<Map<String, Object>> selectMenuAuth(String[] authArray, Map<String, Object> param);

	public List<Map<String, Object>> selectMenuAuthNew(String[] authArray, Map<String, Object> param);

	public List<Map<String, Object>> selectSubMenuAuth(String[] authArray, String upMenuId);

	public int insertFavoritesMenu(Map<String, String> param);

	public int deleteFavoritesMenu(Map<String, String> param);

	public int selectFavoritesMenuCount(Map<String, String> param);
}