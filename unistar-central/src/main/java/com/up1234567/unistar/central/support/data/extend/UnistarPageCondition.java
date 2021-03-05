package com.up1234567.unistar.central.support.data.extend;

import lombok.Data;
import org.apache.commons.collections4.MapUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class UnistarPageCondition {

    public final static int PAGE_1 = 1;
    public final static int SIZE_1 = 1;
    public final static int SIZE_5 = 5;
    public final static int SIZE_10 = 10;
    public final static String SORT_ASC = "ASC";
    public final static String SORT_DESC = "DESC";

    private Map<String, String> sorts;
    private Map<String, Object> filters;
    private int page;
    private int limit;
    private boolean countable; // 是否汇总，默认不汇总

    /**
     * @param f
     * @return
     */
    public boolean containFilter(String f) {
        if (MapUtils.isEmpty(filters)) {
            return false;
        }
        return filters.containsKey(f);
    }

    /**
     * @param f
     * @return
     */
    public String getFilter(String f) {
        if (MapUtils.isEmpty(filters)) {
            return null;
        }
        return String.valueOf(filters.get(f));
    }

    /**
     * @param f
     * @return
     */
    public long getFilterAsLong(String f) {
        if (MapUtils.isEmpty(filters)) {
            return 0L;
        }
        return Long.parseLong(String.valueOf(filters.getOrDefault(f, "0")));
    }


    /**
     * 添加一个筛选条件
     *
     * @param f
     * @param v
     */
    public void addFilter(String f, Object v) {
        if (MapUtils.isEmpty(filters)) {
            filters = new LinkedHashMap<>();
        }
        filters.put(f, v);
    }


    /**
     * 移除一个筛选条件
     *
     * @param f
     */
    public void removeFilter(String f) {
        if (MapUtils.isNotEmpty(filters)) {
            filters.remove(f);
        }
    }

    /**
     * 添加一个筛选条件
     *
     * @param f
     * @param d
     */
    public void addSort(String f, String d) {
        if (MapUtils.isEmpty(sorts)) {
            sorts = new LinkedHashMap<>();
        }
        sorts.put(f, d);
    }

}
