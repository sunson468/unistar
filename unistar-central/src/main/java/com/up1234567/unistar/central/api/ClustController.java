package com.up1234567.unistar.central.api;

import com.up1234567.unistar.central.api.model.BaseOutModel;
import com.up1234567.unistar.central.api.model.cent.ClustOutModel;
import com.up1234567.unistar.central.api.model.cent.vo.CentralVo;
import com.up1234567.unistar.central.service.cent.impl.CentralCacheService;
import com.up1234567.unistar.central.service.cent.impl.CentralService;
import com.up1234567.unistar.central.support.auth.ARoleAuth;
import com.up1234567.unistar.central.support.auth.AuthToken;
import com.up1234567.unistar.central.support.auth.EAuthRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@ARoleAuth(EAuthRole.SUPER)
@RequestMapping(AuthToken.PATH_PREFIX + "clust")
public class ClustController {

    @Autowired
    private CentralService centralService;

    @Autowired
    private CentralCacheService centralCacheService;

    @PostMapping("list")
    public ClustOutModel list(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken) {
        ClustOutModel retModel = new ClustOutModel();
        List<CentralVo> appVos = new ArrayList<>();
        centralService.listCentral().forEach(o -> appVos.add(CentralVo.wrap(o)));
        retModel.setData(appVos);
        retModel.setTimering(centralCacheService.isTimering());
        if (!retModel.isTimering()) {
            retModel.setVoting(centralCacheService.isVoting());
        }

        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

}
