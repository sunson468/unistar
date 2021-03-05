package com.up1234567.unistar.central.support.data.extend;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UnistarPageResult<T> {

    private int page;
    private int size;
    private long count;
    private List<T> data = new ArrayList();

    /**
     * 是否还有记录
     *
     * @return
     */
    public boolean more() {
        return page < totalpage();
    }

    /**
     * 总页数
     *
     * @return
     */
    public long totalpage() {
        return (count / size) + (count % size == 0 ? 0 : 1);
    }
}
