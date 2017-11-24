package uyun.bat.dashboard.impl.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;

public class JsonUtil {
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

}