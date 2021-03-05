package com.up1234567.unistar.central.support.cache.mem;

import com.up1234567.unistar.central.support.cache.IUnistarCache;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UnistarCacheMemory implements IUnistarCache {

    private final ConcurrentHashMap<String, MemoryVal> KV = new ConcurrentHashMap<>();
    private final Object KV_LOCK = new Object();

    @Override
    public boolean has(String k) {
        return KV.containsKey(k);
    }

    @Override
    public void set(String k, String v, int seconds) {
        KV.put(k, new MemoryVal(v, seconds));
    }

    @Override
    public void expire(String k, int seconds) {
        MemoryVal v = KV.get(k);
        v.expired(seconds);
    }

    @Override
    public void del(Collection<String> ks) {
        if (CollectionUtils.isEmpty(ks)) return;
        ks.forEach(KV::remove);
    }

    @Override
    public String get(String k) {
        MemoryVal mv = KV.get(k);
        if (mv == null || mv.isExprired()) return null;
        return String.valueOf(mv.val());
    }

    @Override
    public List<String> get(Collection<String> ks) {
        List<String> retList = new ArrayList<>();
        ks.forEach(k -> retList.add(get(k)));
        return retList;
    }

    @Override
    public synchronized void inc(String k, int num) {
        MemoryVal v = KV.get(k);
        if (v == null) v = new MemoryVal("0", 0);
        v.setVal(Long.parseLong(v.val()) + num);
    }

    @Override
    public void setAdd(String k, Collection<String> vs) {
        synchronized (KV_LOCK) {
            MemoryVal mv = KV.get(k);
            if (mv == null) mv = new MemoryVal(new HashSet<>());
            KV.put(k, mv);
            mv.valAsSet().addAll(vs);
        }
    }

    @Override
    public void setDel(String k, Collection<String> vs) {
        synchronized (KV_LOCK) {
            MemoryVal mv = KV.get(k);
            if (mv == null) return;
            mv.valAsSet().removeAll(vs);
        }
    }

    @Override
    public long setLen(String k) {
        synchronized (KV_LOCK) {
            MemoryVal mv = KV.get(k);
            if (mv == null) return 0;
            return mv.valAsSet().size();
        }
    }

    @Override
    public Set<String> setGet(String k) {
        synchronized (KV_LOCK) {
            MemoryVal mv = KV.get(k);
            if (mv == null) return new HashSet<>();
            return mv.valAsSet();
        }
    }

    @Override
    public long listAdd(String k, String v) {
        synchronized (KV_LOCK) {
            MemoryVal mv = KV.get(k);
            if (mv == null) mv = new MemoryVal(new ArrayList<>());
            KV.put(k, mv);
            mv.valAsList().add(v);
            return mv.valAsList().size();
        }
    }

    @Override
    public void listDel(String k, Collection<String> vs) {
        synchronized (KV_LOCK) {
            MemoryVal mv = KV.get(k);
            if (mv == null) return;
            mv.valAsList().removeAll(vs);
        }
    }

    @Override
    public void listTrim(String k, int start, int end) {
        synchronized (KV_LOCK) {
            MemoryVal mv = KV.get(k);
            if (mv == null) return;
            int size = mv.valAsList().size();
            if (start < 0) start = 0;
            if (end == -1) end = size;
            end = Math.min(end, size);
            mv.setVal(mv.valAsList().subList(start, end));
        }
    }

    @Override
    public List<String> listGet(String k) {
        synchronized (KV_LOCK) {
            MemoryVal mv = KV.get(k);
            if (mv == null) return new ArrayList<>();
            return mv.valAsList();
        }
    }

    @Override
    public long listLen(String k) {
        synchronized (KV_LOCK) {
            MemoryVal mv = KV.get(k);
            if (mv == null) return 0;
            return mv.valAsList().size();
        }
    }

}
