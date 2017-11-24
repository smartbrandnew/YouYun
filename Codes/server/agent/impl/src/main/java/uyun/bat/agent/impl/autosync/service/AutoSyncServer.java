package uyun.bat.agent.impl.autosync.service;


import uyun.bat.agent.impl.autosync.entity.Client;
import uyun.bat.agent.impl.autosync.entity.SyncFileset;
import uyun.bat.agent.impl.autosync.entity.SyncFilesetV2;

import java.io.File;


public interface AutoSyncServer {

    /**
     * 获取当前所有客户端
     * @return
     */
    Client getClient(String clientId);

    /**
     * 获取指定客户端当前所有文件
     * @param clientId
     * @return
     */
    SyncFileset getClientFileset(String clientId);

    /**
     * 获取指定客户端当前所有文件，按V2版本要求返回（含MD5）
     * @param clientId
     * @return
     */
    SyncFilesetV2 getClientFilesetV2(String clientId);

    /**
     * 获取指定客户端的指定文件
     * @param clientId
     * @param filename
     * @return
     */
    File getClientFile(String clientId, String filename);


    /**
     * 启动服务
     */
    void startup();

    /**
     * 停止服务
     */
   void shutdown();

    /**
     * 服务是否在运行
     * @return
     */
   boolean isRunning();

}
