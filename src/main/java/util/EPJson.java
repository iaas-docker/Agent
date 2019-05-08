package util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.HashMap;
import java.util.Map;

public class EPJson {

    private static Gson gson = new Gson();

    public static String string(Object... params) {
        return object(params).toString();
    }

    public static JsonElement object(Object... params){
        Map map = new HashMap();
        for (int i = 0; i < params.length; i+=2) {
            map.put(params[i], params[i+1]);
        }
        return gson.toJsonTree(map);
    }

    public static <T> T objectAs(String object, Class<T> T){
        return gson.fromJson(object, T);
    }
}