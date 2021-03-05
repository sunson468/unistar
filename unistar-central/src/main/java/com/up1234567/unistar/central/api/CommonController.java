package com.up1234567.unistar.central.api;

import com.up1234567.unistar.central.api.model.BaseInModel;
import com.up1234567.unistar.central.api.model.BaseOutModel;
import com.up1234567.unistar.central.api.model.cent.CentConditionModel;
import com.up1234567.unistar.central.service.base.impl.BaseService;
import com.up1234567.unistar.central.support.auth.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping(AuthToken.PATH_PREFIX + "common")
public class CommonController {

    @Autowired
    private BaseService baseService;

    /**
     * 依赖条件
     *
     * @param inModel
     * @returns
     */
    @PostMapping("condition")
    public CentConditionModel condition(@RequestBody BaseInModel inModel) {
        CentConditionModel retModel = new CentConditionModel();
        retModel.setGroups(baseService.listGroup().stream().filter(g -> g.isValid(inModel.getNamespace())).collect(Collectors.toList()));
        retModel.setTasks(baseService.listTask().stream().filter(t -> t.isValid(inModel.getNamespace())).collect(Collectors.toList()));
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

}
