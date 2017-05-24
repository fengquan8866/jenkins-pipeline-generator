package com.kdx.jenkins.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * map相关工具方法
 * @ClassName:  MapUtil
 * @author: huangchao
 * @date:   2017年5月24日 下午2:49:59
 *
 */
public class MapUtil {

	/**
	 * 合并Map
	 * @Title: merge
	 * @Description: 后面的map属性值会覆盖前面的map属性值
	 * @param maps
	 * @return
	 */
	public static <K, V> Map<K, V> merge(@SuppressWarnings("unchecked") Map<K, V>... maps) {
		Map<K, V> map = maps[0];
		if (map == null) {
			map = new LinkedHashMap<>();
		}
		if (maps.length > 1) {
			for (int i=1; i<maps.length; i++) {
				if (maps[i] != null && maps[i].size() > 0) {
					for (Map.Entry<K, V> e : maps[i].entrySet()) {
						map.put(e.getKey(), e.getValue());
					}
				}
			}
		}
		return map;
	}
	
	/**
	 * 合并到最后一个Map
	 * @Title: merge
	 * @Description: 后面的map属性值会覆盖前面的map属性值
	 * @param maps
	 * @return
	 */
	public static <K, V> Map<K, V> mergeLast(@SuppressWarnings("unchecked") Map<K, V>... maps) {
		Map<K, V> map = maps[maps.length - 1];
		if (map == null) {
			map = new LinkedHashMap<>();
		}
		if (maps.length > 1) {
			for (int i=0; i<maps.length - 1; i++) {
				if (maps[i] != null && maps[i].size() > 0) {
					for (Map.Entry<K, V> e : maps[i].entrySet()) {
						if (!map.containsKey(e.getKey())) {
							map.put(e.getKey(), e.getValue());
						}
					}
				}
			}
		}
		return map;
	}
	
	/**
	 * 合并到一个新map
	 * @Title: mergeNew
	 * @Description: 后面的map属性值会覆盖前面的map属性值
	 * @param maps
	 * @return
	 */
	public static <K, V> Map<K, V> mergeNew(@SuppressWarnings("unchecked") Map<K, V>... maps) {
		Map<K, V> map = new LinkedHashMap<>();
		for (int i=0; i<maps.length; i++) {
			if (maps[i] != null && maps[i].size() > 0) {
				for (Map.Entry<K, V> e : maps[i].entrySet()) {
					map.put(e.getKey(), e.getValue());
				}
			}
		}
		return map;
	}
}
