package com.up1234567.unistar.central.support.core.timer;

import com.up1234567.unistar.central.support.core.UnistarNode;
import com.up1234567.unistar.central.support.core.clust.*;
import com.up1234567.unistar.central.support.core.task.IUnistarTaskRunner;
import com.up1234567.unistar.central.support.core.task.UnistarScheduleHandler;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.util.DateUtil;
import lombok.CustomLog;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 心跳和选取任务
 */
@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
public class UnistarTimer implements IUnistarClustListener {

    private final static String TIMER_NAME = "Unistar-Clust-Timer";

    private final IUnistarClustRunner unistarClustRunner;
    private final IUnistarTaskRunner unistarTaskRunner;
    private final UnistarScheduleHandler unistarScheduleHandler;
    //
    private IUnistarCluster unistarCluster;
    private UnistarNode currentNode;

    // ======================================================
    private long lastMasterNotice = DateUtil.now(); // 上次Master的消息
    private boolean master; // 是否为master
    //
    private boolean voting; // 是否在选举中
    private UnistarNodeVoter voter; // 简易投票状态记录和计数器
    private boolean waiting; // 选举已经在进行中，等着

    private AtomicBoolean running = new AtomicBoolean(false);
    private Timer timer;

    /**
     * @param unistarTaskRunner
     * @param unistarClustRunner
     */
    public UnistarTimer(IUnistarTaskRunner unistarTaskRunner, IUnistarClustRunner unistarClustRunner) {
        this.unistarTaskRunner = unistarTaskRunner;
        this.unistarScheduleHandler = new UnistarScheduleHandler(unistarTaskRunner);
        this.unistarClustRunner = unistarClustRunner;
    }

    /**
     * 启动定时器
     */
    public void start(IUnistarCluster unistarCluster, UnistarNode currentNode, boolean clustered) {
        this.unistarCluster = unistarCluster;
        this.currentNode = currentNode;
        // 只有Cluster才监听
        if (clustered) {
            // 增加监听
            unistarCluster.addListener(this
                    , IUnistarClustMsg.TYPE_HEARTBEAT
                    , IUnistarClustMsg.TYPE_VOTE_START
                    , IUnistarClustMsg.TYPE_VOTE_VOTE
                    , IUnistarClustMsg.TYPE_VOTE_PUBLISH
                    , IUnistarClustMsg.TYPE_VOTE_REFUSE
                    , IUnistarClustMsg.TYPE_VOTE_ACCEPT
            );
        } else {
            // 单机启动直接选取
            master = true;
            lastMasterNotice = DateUtil.now();
        }
        // ==========================================================================================
        // 一直启动着的定时器，每5秒一次
        timer = new Timer(TIMER_NAME);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // 确保单例执行
                if (running.compareAndSet(false, true)) {
                    long now = DateUtil.now();
                    try {
                        if (waiting) {
                            //
                        } else if (voting) {
                            // 投票中，投票状态机沿用事件
                            // 根据状态执行任务
                            switch (voter.getCurrent()) {
                                case IUnistarClustMsg.TYPE_VOTE_START:
                                    log.debug("unistar vote master started");
                                    String self = currentNode.toString();
                                    voter.joinVoter(self);
                                    unistarCluster.multicast(IUnistarClustMsg.TYPE_VOTE_START, self);
                                    // 进入下一个阶段
                                    voter.setCurrent(IUnistarClustMsg.TYPE_VOTE_VOTE);
                                    break;
                                case IUnistarClustMsg.TYPE_VOTE_VOTE:   // 投票
                                    log.debug("unistar vote master voted");
                                    UnistarNode clustNode = unistarClustRunner.voteMaster(now);
                                    if (clustNode != null) {
                                        String node = clustNode.toString();
                                        voter.voteVoter(node);
                                        unistarCluster.multicast(IUnistarClustMsg.TYPE_VOTE_VOTE, node);
                                        // 进入下一个阶段
                                        voter.setCurrent(IUnistarClustMsg.TYPE_VOTE_PUBLISH);
                                    } else {
                                        waiting = true;
                                    }
                                    break;
                                case IUnistarClustMsg.TYPE_VOTE_PUBLISH:  // 发布
                                    log.debug("unistar vote master published");
                                    String winner = voter.chooseWinner();
                                    if (winner != null) unistarCluster.multicast(IUnistarClustMsg.TYPE_VOTE_PUBLISH, winner);
                                    // 进入下一个阶段
                                    voter.setCurrent(IUnistarClustMsg.TYPE_VOTE_ACCEPT);
                                    break;
                                case IUnistarClustMsg.TYPE_VOTE_ACCEPT:
                                    log.debug("unistar vote master accepted");
                                    voting = false;
                                    lastMasterNotice = now;
                                    // 自己选为Master
                                    if (voter.getWinner().equals(currentNode.toString())) {
                                        log.debug("chosen as unistar central master");
                                        master = true;
                                    }
                                    voter = null;
                                    break;
                            }
                        } else {
                            // 校验Master
                            if (clustered && invalidMaster()) {
                                // 开启投票
                                log.debug("unistar master invalid, start to vote master");
                                voting = true;
                                voter = new UnistarNodeVoter();
                                return;
                            }
                            // 定时器
                            unistarTaskRunner.heartbeat(master);
                            // ======================================================
                            // Master特有逻辑
                            if (master) {
                                // 更新自己的时间
                                lastMasterNotice = now;
                                // 同步其他人
                                if (clustered) {
                                    unistarCluster.multicast(IUnistarClustMsg.TYPE_HEARTBEAT, null);
                                }
                                // 执行任务
                                unistarScheduleHandler.handle(now);
                            }
                        }
                    } catch (Exception e) {
                        // ignore
                    } finally {
                        running.set(false);
                    }
                }
            }
        }, DateUtil.nextMinuteDate(), DateUtil.SECOND_5);
    }

    /**
     * 检验Master是否还在运行，超过60秒未接受到Master的同步信息则判定为Master失效
     */
    private boolean invalidMaster() {
        return lastMasterNotice < DateUtil.now() - DateUtil.MINUTE;
    }

    @Override
    public void handle(UnistarClustMsg msg) {
        // 自己不处理自己的心跳
        if (msg.getFrom() != null && currentNode.toString().equals(msg.getFrom().toString())) return;
        switch (msg.getType()) {
            case IUnistarClustMsg.TYPE_VOTE_START:
                if (voting) {
                    voter.joinVoter(msg.getBody());
                }
                break;
            case IUnistarClustMsg.TYPE_VOTE_VOTE:
                if (voting) {
                    voter.voteVoter(msg.getBody());
                }
                break;
            case IUnistarClustMsg.TYPE_VOTE_PUBLISH:
                if (voting) {
                    if (voter.checkWinner(msg.getBody())) {
                        voter.setCurrent(IUnistarClustMsg.TYPE_VOTE_ACCEPT);
                    } else {
                        log.debug("unistar vote master refused");
                        voter.setCurrent(IUnistarClustMsg.TYPE_VOTE_START);
                        unistarCluster.multicast(IUnistarClustMsg.TYPE_VOTE_REFUSE, msg.getBody());
                    }
                }
                break;
            case IUnistarClustMsg.TYPE_VOTE_REFUSE:
                if (voting) {
                    // 重新投票
                    voter.setCurrent(IUnistarClustMsg.TYPE_VOTE_START);
                }
                break;
            case IUnistarClustMsg.TYPE_HEARTBEAT: // 来自Master的心跳
                if (master) {
                    // 说明冲突了,冲突的起因，可能是网络间的不通畅导致的
                    master = false;
                }
                if (voting) {
                    // 重新投票
                    voting = false;
                    voter = null;
                }
                if (waiting) {
                    waiting = false;
                }
                lastMasterNotice = DateUtil.now();
                break;
            default:
                // ignore
                break;
        }
    }

    /**
     * 销毁对象
     *
     * @throws Exception
     */
    public void destroy() throws Exception {
        if (running.get() && timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
