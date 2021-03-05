package com.up1234567.unistar.common;

import com.up1234567.unistar.common.limit.UnistarAppLimit;
import lombok.Data;

import java.util.List;

@Data
public class UnistarReadyOutParam {

    private List<UnistarAppLimit> appLimits;

}
