package com.up1234567.unistar.common.discover;

import com.up1234567.unistar.common.limit.UnistarAppLimit;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UnistarDiscoverOutParam {

    private List<UnistarServiceNode> serviceNodes;
    private List<UnistarAppLimit> appLimits;

}
