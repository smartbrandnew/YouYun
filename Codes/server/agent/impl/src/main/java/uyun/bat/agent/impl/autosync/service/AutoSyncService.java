package uyun.bat.agent.impl.autosync.service;


import uyun.bat.agent.impl.autosync.entity.Client;
import uyun.bat.agent.impl.autosync.entity.SyncFilesetV2;

import java.io.File;

public interface AutoSyncService {
    /**
     * 查询指定client定义信息，含fileset与action定义
     *
     * @param id
     * @return
     */
    Client getClientById(String id);


    /**
     * 查询指定client当前版本号
     *
     * @param id
     * @return
     */
    String getClientVersionById(String id);


    /**
     * 查询指定client的文件详情列表
     *
     * @param id
     * @return
     */
    SyncFilesetV2 getClientFileSetById(String id);

    /**
     * 获取指定客户端的指定文件
     * @param clientId
     * @param filename
     * @return
     */
    File getClientFileByName(String clientId, String filename);



}
