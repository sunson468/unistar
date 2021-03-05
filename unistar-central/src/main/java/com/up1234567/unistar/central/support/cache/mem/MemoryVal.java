package com.up1234567.unistar.central.support.cache.mem;

import com.up1234567.unistar.common.exception.UnistarMethodInvokeException;
import com.up1234567.unistar.common.util.DateUtil;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class MemoryVal {

    @Setter
    private Object val;
    private long exprired; // 失效时间

    public MemoryVal(String v, int seconds) {
        this.val = v;
        if (seconds > 0) {
            this.exprired = DateUtil.now() + seconds * DateUtil.SECOND;
        }
    }

    public MemoryVal(Collection<String> vs) {
        this.val = vs;
    }

    public void expired(int seconds) {
        this.exprired = DateUtil.now() + seconds * DateUtil.SECOND;
    }

    public boolean isExprired() {
        return exprired > 0 && DateUtil.now() >= exprired;
    }

    public String val() {
        return String.valueOf(val);
    }

    public List<String> valAsList() {
        if (val instanceof List) return (List<String>) val;
        throw new UnistarMethodInvokeException("memory val is not list");
    }

    public Set<String> valAsSet() {
        if (val instanceof Set) return (Set<String>) val;
        throw new UnistarMethodInvokeException("memory val is not set");
    }

}
