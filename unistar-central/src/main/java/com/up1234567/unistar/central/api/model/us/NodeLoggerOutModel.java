package com.up1234567.unistar.central.api.model.us;

import com.up1234567.unistar.central.api.model.BaseOutModel;
import lombok.Data;

import java.util.List;

@Data
public class NodeLoggerOutModel extends BaseOutModel {

    private List<String> levels;

}
