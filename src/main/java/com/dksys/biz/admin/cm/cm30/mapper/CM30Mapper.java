package com.dksys.biz.admin.cm.cm30.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CM30Mapper {

	String selectGridConfig(Map<String, String> param);

	int saveGridConfig(Map<String, String> param);

	int deleteGridConfig(Map<String, String> param);

}
