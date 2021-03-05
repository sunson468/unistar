package com.up1234567.unistar.central.data.us;

import com.up1234567.unistar.common.discover.UnistarServiceNode;
import com.up1234567.unistar.common.util.SecurityUtil;
import com.up1234567.unistar.common.util.StringUtil;
import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "us_app_node")
@CompoundIndexes({
        @CompoundIndex(name = "uidx_ns_an_h_p", def = "{'namespace':1,'appname':1,'host':1,'port':1}", unique = true),
})
public class AppNode {

    private final static String AT = "@";

    @Indexed
    private String namespace;
    private String appname;

    @Indexed
    private String nodeId;  // 用于标识节点
    private String host;    // 应用地址
    private int port;       // 应用端口

    private String group;   // 分组标记
    private String profiles; //

    // ==============================================
    // 注册为服务提供者
    private boolean serverable; // 可提供服务
    private int weight;
    private EStatus serviceStatus;     // 服务状态

    // ==============================================
    // 注册为服务发现者
    private boolean discoverable; // 可发现服务
    private String discovers; // 发现的服务列表
    private EStatus discoverStatus; // 可发现服务状态

    // ==============================================
    // 注册为任务执行器
    private boolean taskable; // 为执行器
    private String tasks;    // 该节点支持的任务列表
    private EStatus taskStatus;    // 执行器状态

    // ==============================================
    // 连接相关
    private String connectId;      // 连接ID
    private String connectCenter; // 连接中心
    private long lastConnectTime; // 上次接入时间
    private long lastDisonnectTime; // 上次断开时间

    @Indexed
    private boolean manual; // 是否手动添加
    // 主动添加的节点，注册中心会主动发起活跃检测
    private String manualAnurl; // 活跃检测地址

    public void toNodeId() {
        nodeId = SecurityUtil.md5(host + StringUtil.COLON + port);
    }

    public enum EStatus {
        ON, OFF;
    }

    /**
     * 是否为可用服务节点, 只有状态为ON的节点才会对外提供服务
     *
     * @return
     */
    public boolean isServiceAvailable() {
        return EStatus.ON.equals(serviceStatus);
    }

    /**
     * 是否为可用任务节点, 只有状态为ON的节点才会执行任务
     *
     * @return
     */
    public boolean isTaskAvailable() {
        return EStatus.ON.equals(taskStatus);
    }

    /**
     * 转为服务节点Node
     *
     * @return
     */
    public UnistarServiceNode toServiceNode() {
        UnistarServiceNode node = new UnistarServiceNode();
        node.setNodeId(nodeId);
        node.setNamespace(namespace);
        node.setService(appname);
        node.setHost(host);
        node.setPort(port);
        node.setWeight(weight);
        node.setAvailable(isServiceAvailable());
        return node;
    }

    /**
     * 新增发现的服务，不断累计
     *
     * @param service
     */
    public void addDiscovers(String service) {
        List<String> services = StringUtil.fromCommaString(discovers);
        if (!services.contains(service)) {
            services.add(service);
            discovers = StringUtil.toCommaString(services);
        }
    }

    /**
     * 包装心跳地址
     *
     * @return
     */
    public String wrapManualAnurl() {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtil.HTTP_PROTO);
        sb.append(host);
        if (port != 80) {
            sb.append(StringUtil.COLON).append(port);
        }
        sb.append(StringUtil.SLASH).append(manualAnurl);
        return sb.toString();
    }

}
