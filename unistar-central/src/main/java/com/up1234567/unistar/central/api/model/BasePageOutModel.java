package com.up1234567.unistar.central.api.model;

import lombok.Data;

@Data
public class BasePageOutModel extends BaseOutModel {

    private long count;
    private boolean more;
}
