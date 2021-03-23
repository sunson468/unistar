package com.up1234567.unistar.common.logger;

import com.up1234567.unistar.common.UnistarParam;
import lombok.Data;

import java.util.List;

@Data
public class UnistarLoggerSearchParam extends UnistarParam {

    private String searchId;
    private String keyword; // 关键词
    private int before;  // 前几行
    private int after; // 后几行
    private int maxline;  // 最多多少行
    private List<String> results;

}
