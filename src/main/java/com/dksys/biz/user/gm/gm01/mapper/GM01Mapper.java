package com.dksys.biz.user.gm.gm01.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GM01Mapper {

	int selectSecUsbCount(Map<String, String> paramMap);

	List<Map<String, String>> selectSecUsbList(Map<String, String> paramMap);

	String selectSecUsbNextMngNo(Map<String, String> paramMap);

	int insertSecUsbOut(Map<String, String> paramMap);

	int updateSecUsbOut(Map<String, String> paramMap);

	int updateSecUsbIn(Map<String, String> paramMap);

	int updateSecUsbInCancel(Map<String, String> paramMap);

	int deleteSecUsb(Map<String, String> paramMap);

}
