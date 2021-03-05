package com.up1234567.unistar.central.support.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface IUnistarCache {

    /**
     * 是否存在该k
     *
     * @param k
     * @return
     */
    boolean has(String k);

    /**
     * 设置K，V
     *
     * @param k
     * @param v
     */
    default void set(String k, String v) {
        set(k, v, 0);
    }

    /**
     * 设置K，V，有效期
     *
     * @param k
     * @param v
     * @param seconds 有效秒数
     */
    void set(String k, String v, int seconds);

    /**
     * @param k
     * @param seconds
     */
    void expire(String k, int seconds);

    /**
     * 移除K
     *
     * @param k
     */
    default void del(String k) {
        del(Collections.singletonList(k));
    }

    /**
     * 批量移除K
     *
     * @param ks
     */
    void del(Collection<String> ks);

    /**
     * 获取一个V
     *
     * @param k
     * @return
     */
    String get(String k);

    /**
     * 获取多个Key
     *
     * @param ks
     * @return
     */
    List<String> get(Collection<String> ks);

    /**
     * 增加某个key的值
     *
     * @param k
     * @param num
     */
    void inc(String k, int num);

    /**
     * @param k
     * @param v
     */
    default void setAdd(String k, String v) {
        setAdd(k, Collections.singleton(v));
    }

    /**
     * @param k
     * @param vs
     */
    void setAdd(String k, Collection<String> vs);

    /**
     * @param k
     * @param v
     */
    default void setDel(String k, String v) {
        setDel(k, Collections.singleton(v));
    }

    /**
     * @param k
     * @param vs
     */
    void setDel(String k, Collection<String> vs);

    /**
     * @param k
     * @return
     */
    long setLen(String k);

    /**
     * @param k
     * @return
     */
    Set<String> setGet(String k);

    /**
     * 加入一个Set集合
     *
     * @param k
     * @param v
     */
    long listAdd(String k, String v);

    /**
     * 从一个Set集合中移除
     *
     * @param k
     * @param v
     */
    default void listDel(String k, String v) {
        listDel(k, Collections.singleton(v));
    }

    /**
     * 从一个Set集合中移除
     *
     * @param k
     * @param vs
     */
    void listDel(String k, Collection<String> vs);

    /**
     * 截断数据，移除start索引之前的的数据
     *
     * @param k
     * @param start
     */
    default void listTrim(String k, int start) {
        listTrim(k, start, -1);
    }

    /**
     * @param k
     * @param start >=0
     * @param end   -1 代表到最后
     */
    void listTrim(String k, int start, int end);

    /**
     * 获取集合
     *
     * @param k
     * @return
     */
    List<String> listGet(String k);

    /**
     * @param k
     * @return
     */
    long listLen(String k);

    /**
     * 发布消息，用于Clust之间的订阅
     *
     * @param message
     */
    default void publish(String message) {
    }

}
