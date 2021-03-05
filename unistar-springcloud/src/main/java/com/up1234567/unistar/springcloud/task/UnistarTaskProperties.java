package com.up1234567.unistar.springcloud.task;

import lombok.Data;

@Data
public class UnistarTaskProperties {

    private boolean available = true;

    private int parallel = 2;

}
