package com.sohu.tv.mq.cloud.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 * map工具
 * 
 * @author yongfeigao
 * @date 2019年11月19日
 */
public class MapUtil {
	/**
	 * map size optimize
	 * //http://greenrobot.me/devpost/java-faster-less-jvm-garbage/?utm_source=%E4%BC%AF%E4%B9%90%E5%A4%B4%E6%9D%A1&utm_medium=%E7%82%B9%E5%87%BB&utm_campaign=%E9%93%BE%E6%8E%A5
	 * @param size
	 * @return
	 */
	public static int getMapSize(int size) {
		return (int)Math.ceil(size / 0.7);
	}
	
	/**
	 * 按照value进行排序
	 * @param <K>
	 * @param <V>
	 * @param mapToSort
	 * @param desc
	 * @return
	 */
	public static <K, V extends Comparable<V>> Map<K, V> sortMapByValues(
			final Map<K, V> mapToSort, final boolean desc) {
		List<Map.Entry<K, V>> entries = new ArrayList<Map.Entry<K, V>>(
				mapToSort.size());

		entries.addAll(mapToSort.entrySet());

		Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
			public int compare(final Map.Entry<K, V> entry1,
					final Map.Entry<K, V> entry2) {
				if(desc) {
					return entry2.getValue().compareTo(entry1.getValue());
				}else {
					 return entry1.getValue().compareTo(entry2.getValue());
				}
			}
		});

		Map<K, V> sortedMap = new LinkedHashMap<K, V>();

		for (Map.Entry<K, V> entry : entries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}
}
