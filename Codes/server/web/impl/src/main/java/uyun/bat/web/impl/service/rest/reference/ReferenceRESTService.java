package uyun.bat.web.impl.service.rest.reference;

import com.alibaba.dubbo.config.annotation.Service;
import uyun.bat.datastore.api.service.ResourceService;
import uyun.bat.web.api.reference.entity.ResourceReference;
import uyun.bat.web.api.reference.service.ReferenceService;
import uyun.bat.web.api.resource.entity.ResourceList;
import uyun.bat.web.impl.common.service.ServiceManager;
import uyun.bat.web.impl.common.util.ResourceRefInfo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service(protocol = "rest")
@Path("v2/references")
public class ReferenceRESTService implements ReferenceService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("resources/query")
    public ResourceReference getResourceRefByName(@QueryParam("name") String name) {
        if (!ResourceRefInfo.isResExist(name)) {
            throw new IllegalArgumentException("The collection of resources does not exist");
        }
        ResourceReference resourceRef = new ResourceReference();
        resourceRef.setName(name);
        resourceRef.setManualUrl(ResourceRefInfo.getManualUrl(name));
        resourceRef.setIcoUrl(ResourceRefInfo.getIcoUrl(name));
        resourceRef.setCategory(ResourceRefInfo.getCategory(name));
        return resourceRef;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("resources/list")
    public List<ResourceReference> getResourceRefs() {
        List<ResourceReference> resourceReferences = ResourceRefInfo.getAllresources();
        resourceReferences.sort(new Comparator<ResourceReference>() {
            @Override
            public int compare(ResourceReference o1, ResourceReference o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return resourceReferences;
    }
}
