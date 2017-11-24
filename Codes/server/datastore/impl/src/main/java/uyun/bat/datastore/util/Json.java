package uyun.bat.datastore.util;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Json {
    private static ObjectMapper mapper = new ObjectMapper();

    public static <T> T decode(String json, Class<T> clas) throws JsonParseException, JsonMappingException, IOException {
        if (json == null)
            return null;
        return mapper.readValue(json, clas);
    }

    public static <T> List<T> getList(String json, Class<T> clazz) throws JsonParseException, JsonMappingException,
            IOException {
        if (json == null)
            return null;
        TypeFactory t = TypeFactory.defaultInstance();
        return mapper.readValue(json, t.constructCollectionType(ArrayList.class, clazz));
    }

    public static String encode(Object obj) throws JsonGenerationException, JsonMappingException, IOException {
        if (obj == null)
            return null;
        return mapper.writeValueAsString(obj);
    }

    public static <T> List<T> getList(File file, Class<T> clazz) throws IOException {
        if (file == null)
            return null;
        TypeFactory t = TypeFactory.defaultInstance();
        return mapper.readValue(file, t.constructCollectionType(ArrayList.class, clazz));
    }

    public static <T> T decode(File file, Class<T> clas) throws JsonParseException, JsonMappingException, IOException {
        if (file == null)
            return null;
        return mapper.readValue(file, clas);
    }
}
