package com.up1234567.unistar.central.support.data;

import com.up1234567.unistar.central.support.data.extend.UnistarPageCondition;
import com.up1234567.unistar.central.support.data.extend.UnistarPageResult;
import com.up1234567.unistar.common.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface IUnistarDao {

    // 除了完全匹配外，另外支持的查询特性
    // 符号紧跟着属性名，请根据属性名的末尾部分进行匹配
    String OPS_NE = "!=";
    String OPS_GTE = ">=";
    String OPS_LTE = "<=";
    String OPS_GT = ">";
    String OPS_LT = "<";

    String UPT_ADD = "+";
    String UPT_SUB = "-";
    String UPT_MAX = ">";
    String UPT_MIN = "<";

    /**
     * 匹配查询属性和查询类型
     *
     * @param f
     * @return
     */
    default String[] matchQuery(String f) {
        Optional<String> type = Stream.of(OPS_NE, OPS_GTE, OPS_LTE, OPS_GT, OPS_LT).filter(f::endsWith).findFirst();
        return type.map(s -> new String[]{s, f.substring(0, f.lastIndexOf(s))}).orElseGet(() -> new String[]{StringUtil.EMPTY, f});
    }

    /**
     * 匹配更新属性和查询类型
     *
     * @param f
     * @return
     */
    default String[] matchUpdate(String f) {
        Optional<String> type = Stream.of(UPT_ADD, UPT_SUB, UPT_MAX, UPT_MIN).filter(f::endsWith).findFirst();
        return type.map(s -> new String[]{s, f.substring(0, f.lastIndexOf(s))}).orElseGet(() -> new String[]{StringUtil.EMPTY, f});
    }

    /**
     * 插入数据
     *
     * @param entity
     */
    <T> void insert(T entity);

    /**
     * @param entities
     */
    <T> void insertAll(List<T> entities);

    /**
     * @param prop
     * @param val
     * @return
     */
    default <T> T findOneByProp(String prop, Object val, Class<T> clazz) {
        Map<String, Object> props = new HashMap<>();
        props.put(prop, val);
        return findOneByProps(props, clazz);
    }

    /**
     * @param props
     * @return
     */
    <T> T findOneByProps(Map<String, Object> props, Class<T> clazz);

    /**
     * @param clazz
     * @return
     */
    default <T> List<T> listAll(Class<T> clazz) {
        return listByProps(null, clazz);
    }

    /**
     * @param prop
     * @param val
     * @param clazz
     * @return
     */
    default <T> List<T> listByProp(String prop, Object val, Class<T> clazz) {
        Map<String, Object> props = new HashMap<>();
        props.put(prop, val);
        return listByProps(props, clazz);

    }

    /**
     * @param props
     * @return
     */
    <T> List<T> listByProps(Map<String, Object> props, Class<T> clazz);

    /**
     * 统计数量
     *
     * @param clazz
     * @param <T>
     * @return
     */
    default <T> long count(Class<T> clazz) {
        return count(null, clazz);
    }

    /**
     * 统计数量
     *
     * @param props
     * @param clazz
     * @param <T>
     * @return
     */
    <T> long count(Map<String, Object> props, Class<T> clazz);

    /**
     * 根据条件查询数据
     *
     * @param condition
     * @return
     */
    <T> UnistarPageResult<T> listByPageCondition(UnistarPageCondition condition, Class<T> clazz);

    /**
     * @param prop
     * @param val
     * @param update
     * @param clazz
     */
    default <T> void updateOneByProp(String prop, Object val, Map<String, Object> update, Class<T> clazz) {
        Map<String, Object> props = new HashMap<>();
        props.put(prop, val);
        updateOneByProps(props, update, clazz);
    }

    /**
     * @param props
     * @param updates
     * @param clazz
     */
    <T> void updateOneByProps(Map<String, Object> props, Map<String, Object> updates, Class<T> clazz);

    /**
     * @param prop
     * @param val
     * @param updates
     * @param clazz
     * @return
     */
    default <T> long updateMultiByProp(String prop, Object val, Map<String, Object> updates, Class<T> clazz) {
        Map<String, Object> props = new HashMap<>();
        props.put(prop, val);
        return updateMultiByProps(props, updates, clazz);
    }

    /**
     * @param props
     * @param updates
     * @param clazz
     * @return
     */
    <T> long updateMultiByProps(Map<String, Object> props, Map<String, Object> updates, Class<T> clazz);

    /**
     * @param prop
     * @param val
     * @param clazz
     */
    default <T> void removeByProp(String prop, Object val, Class<T> clazz) {
        Map<String, Object> props = new HashMap<>();
        props.put(prop, val);
        removeByProps(props, clazz);
    }

    /**
     * @param props
     * @param clazz
     */
    <T> void removeByProps(Map<String, Object> props, Class<T> clazz);

}
