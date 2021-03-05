package com.up1234567.unistar.central.api.model;

import lombok.Data;

@Data
public class BaseDataInModel<T> extends BaseInModel {

    private T data;

}
