package com.up1234567.unistar.common.async;

import com.up1234567.unistar.common.IUnistarConst;
import lombok.CustomLog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 异步线程执行
 */
@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
public class PoolExecutorService {

    private final ExecutorService executor;

    public PoolExecutorService() {
        this(1);
    }

    public PoolExecutorService(int threads) {
        executor = Executors.newFixedThreadPool(threads);
    }

    /**
     * @param runnable
     */
    public void execute(Runnable runnable) {
        try {
            executor.execute(() -> {
                try {
                    runnable.run();
                } catch (Exception e) {
                    log.debug("execute run error occur, ", e);
                }
            });
        } catch (Exception e) {
            log.debug("execute error occur, ", e);
        }
    }


}
