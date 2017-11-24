package uyun.bat.web.impl.common.util;


import org.codehaus.jackson.map.ObjectMapper;
import uyun.bat.web.api.reference.entity.ResourceReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceRefInfo {
    private static Map<String, Map<String, String>> resInfo = new HashMap<>();

    static {
        ObjectMapper mapper = new ObjectMapper();
        try {
            resInfo = mapper.readValue(getJsonFile(), Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Parsing the Json file fails");
        }
    }

    public static boolean isResExist(String name) {
        return resInfo.containsKey(name);
    }

    public static String getIcoUrl(String name) {
        return resInfo.get(name).get("icoUrl");
    }

    public static String getManualUrl(String name) {
        return resInfo.get(name).get("manualUrl");
    }


    public static String getCategory(String name) {
        return resInfo.get(name).get("category");
    }

    public static List<ResourceReference> getAllresources() {
        List<ResourceReference> resourceRefList = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> entry : resInfo.entrySet()) {
            ResourceReference resourceReference = new ResourceReference();
            resourceReference.setName(entry.getKey());
            resourceReference.setIcoUrl(entry.getValue().get("icoUrl"));
            resourceReference.setManualUrl(entry.getValue().get("manualUrl"));
            resourceReference.setCategory(entry.getValue().get("category"));
            resourceRefList.add(resourceReference);
        }
        return resourceRefList;
    }

    private static File getJsonFile() {
        String[] searchPaths = new String[] { "/conf/", "/../conf/", "/../../conf/", "/src/main/resources/conf/" };
        String dir = System.getProperty("work.dir", System.getProperty("user.dir"));
        for (String path : searchPaths) {
            File file = new File(dir, path + "resreference.json");
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

}
