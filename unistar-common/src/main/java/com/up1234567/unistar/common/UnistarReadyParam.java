package com.up1234567.unistar.common;

import com.up1234567.unistar.common.registry.UnistarRegistraionParam;
import com.up1234567.unistar.common.task.UnistarTaskParam;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UnistarReadyParam extends UnistarParam {

    private List<String> profiles;

    // 注册参数， Null不注册
    private UnistarRegistraionParam registraionParam;

    private UnistarTaskParam taskParam;

    // 日志资源名列表
    private List<String> loggerParam;

}
