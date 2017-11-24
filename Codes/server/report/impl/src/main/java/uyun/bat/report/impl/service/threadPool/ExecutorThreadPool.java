package uyun.bat.report.impl.service.threadPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lilm on 17-3-7.
 */
public class ExecutorThreadPool {

    //定3个线程执行,超出则队列中等待
    private ExecutorService pool = Executors.newFixedThreadPool(3);

    private static ExecutorThreadPool executorThreadPool;

    private static synchronized void initPool () {
        executorThreadPool = new ExecutorThreadPool();
    }

    public static ExecutorService getExecutorService () {
        if (executorThreadPool == null) {
            initPool();
        }
        return executorThreadPool.pool;
    }
    
}
