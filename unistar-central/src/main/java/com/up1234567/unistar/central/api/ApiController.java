package com.up1234567.unistar.central.api;

import com.up1234567.unistar.central.support.auth.AuthToken;
import com.up1234567.unistar.central.api.model.BaseOutModel;
import com.up1234567.unistar.central.data.us.App;
import com.up1234567.unistar.central.service.us.impl.AppService;
import com.up1234567.unistar.central.ws.ConfigHandler;
import com.up1234567.unistar.common.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 这个
 */
@RestController
@RequestMapping(AuthToken.PATH_PREFIX + "client")
public class ApiController {

    @Autowired
    private AppService appService;

    @Autowired
    private ConfigHandler configHandler;

    @GetMapping("config/{namespace}/{app}")
    public BaseOutModel list(@PathVariable("namespace") String namespace,
                             @PathVariable("app") String appname,
                             @RequestParam(value = "token", required = false) String token,
                             @RequestParam(value = "profiles", required = false) String profiles) {
        BaseOutModel outModel = new BaseOutModel();
        App app = appService.findApp(namespace, appname);
        if (app == null) {
            outModel.setRetMsg("app is not found");
            return outModel;
        } else if (StringUtils.isEmpty(token) || !token.equals(app.getToken())) {
            outModel.setRetMsg("app's token check failure");
            return outModel;
        }
        outModel.setData(configHandler.findConfig(namespace, appname, StringUtil.fromCommaString(profiles)));
        outModel.setRetCode(BaseOutModel.RET_OK);
        return outModel;
    }

}
