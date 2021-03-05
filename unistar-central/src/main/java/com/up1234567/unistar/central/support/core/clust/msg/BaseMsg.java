package com.up1234567.unistar.central.support.core.clust.msg;

import lombok.Data;

import java.util.List;

@Data
public class BaseMsg {
    // 所属空间
    private String namespace;
    // 目标地址
    private List<String> clients;
}
