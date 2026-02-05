package com.dksys.biz.user.mm.mm01.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MM01Mapper {

	List<Map<String, String>> selectMindMapList(Map<String, String> paramMap);

	List<Map<String, String>> selectAgendaList(Map<String, String> paramMap);

}
