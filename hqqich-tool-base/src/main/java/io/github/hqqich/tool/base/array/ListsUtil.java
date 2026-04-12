package io.github.hqqich.tool.base.array;

import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class ListsUtil {


	/**
	 * 按指定大小，分隔集合，将集合按规定个数分为n个部分
	 *
	 * @param list
	 * @param len
	 * @return
	 */
	public static <T> List<List<T>> splitList(List<T> list, int len) {
		if (list == null || list.size() == 0 || len < 1) {
			return null;
		}
		List<List<T>> result = new ArrayList<List<T>>();
		int size = list.size();
		int count = (size + len - 1) / len;
		for (int i = 0; i < count; i++) {
			List<T> subList = list.subList(i * len, ((i + 1) * len > size ? size : len * (i + 1)));
			result.add(subList);
		}
		return result;
	}


	/**
	 * 将一个list均分成n个list
	 *
	 * @param list
	 * @param n
	 * @param <T>
	 * @return
	 */
	public static <T> List<List<T>> averageAssign(List<T> list, int n) {
		List<List<T>> result = new ArrayList<List<T>>();
		int remaider = list.size() % n;  //(先计算出余数)
		int number = list.size() / n;  //然后是商
		int offset = 0;//偏移量
		for (int i = 0; i < n; i++) {
			List<T> value = null;
			if (remaider > 0) {
				value = list.subList(i * number + offset, (i + 1) * number + offset + 1);
				remaider--;
				offset++;
			} else {
				value = list.subList(i * number + offset, (i + 1) * number + offset);
			}
			result.add(value);
		}
		return result;
	}


    /**
     * 求 newList 相对于 oldList 的 新增、删除、未变更 三个部分
     * @param oldList
     * @param newList
     * @return 新增、删除、未变更
     * @param <T>
     */
    public static <T> Triple<List<T>, List<T>, List<T>> comparisonList(List<T> oldList, List<T> newList) {
        List<T> added = new ArrayList<>();
        List<T> removed = new ArrayList<>();
        List<T> unchanged = new ArrayList<>();

        if (oldList == null) {
            oldList = java.util.Collections.emptyList();
        }
        if (newList == null) {
            newList = java.util.Collections.emptyList();
        }

        java.util.Set<T> oldSet = new java.util.HashSet<>(oldList);
        java.util.Set<T> newSet = new java.util.HashSet<>(newList);

        // 新增与未变更（保持 newList 顺序）
        for (T n : newList) {
            if (oldSet.contains(n)) {
                unchanged.add(n);
            } else {
                added.add(n);
            }
        }

        // 删除项（依据 oldList）
        for (T o : oldList) {
            if (!newSet.contains(o)) {
                removed.add(o);
            }
        }

        return Triple.of(added, removed, unchanged);
    }



}
