package uyun.bat.web.api.reference.service;

import uyun.bat.web.api.reference.entity.ResourceReference;

import java.util.List;

public interface ReferenceService {
    /**
     * 获取资源参考
     *
     * @param name
     * @return
     */
    ResourceReference getResourceRefByName(String name);

    /**
     * 获取资源参考列表
     *
     * @return
     */
    List<ResourceReference> getResourceRefs();

}
