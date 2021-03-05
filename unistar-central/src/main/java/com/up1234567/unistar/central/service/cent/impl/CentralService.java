package com.up1234567.unistar.central.service.cent.impl;

import com.up1234567.unistar.central.data.cent.Central;
import com.up1234567.unistar.central.support.data.IUnistarDao;
import com.up1234567.unistar.common.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CentralService {

    @Autowired
    private IUnistarDao unistarDao;

    /**
     * @param host
     * @param port
     * @return
     */
    public Central findCentral(String host, int port) {
        Map<String, Object> props = new HashMap<>();
        props.put("host", host);
        props.put("port", port);
        return unistarDao.findOneByProps(props, Central.class);
    }

    /**
     * @param central
     */
    public void createCentral(Central central) {
        central.setCreateTime(DateUtil.now());
        unistarDao.insert(central);
    }

    /**
     * 取消所有master
     */
    public void cancelMasterCentral() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("master", false);
        unistarDao.updateMultiByProp("master", true, updates, Central.class);
    }

    /**
     * @param central
     */
    public void activeCentral(Central central) {
        Map<String, Object> props = new HashMap<>();
        props.put("host", central.getHost());
        props.put("port", central.getPort());
        Map<String, Object> updates = new HashMap<>();
        updates.put("online", central.isOnline());
        updates.put("version", central.getVersion());
        updates.put("master", central.isMaster());
        updates.put("memoryFree", central.getMemoryFree());
        updates.put("memoryTotal", central.getMemoryTotal());
        updates.put("memoryMax", central.getMemoryMax());
        updates.put("processors", central.getProcessors());
        updates.put("lastActiveTime", central.getLastActiveTime());
        unistarDao.updateOneByProps(props, updates, Central.class);
    }

    /**
     * @return
     */
    public List<Central> listCentral() {
        return unistarDao.listAll(Central.class);
    }


}
