package webserver;

import annotation.GetMapping;
import controller.Controller;
import http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

public class HandlerMapping {
    private static final Map<String, Method> GET_MAP = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(HandlerMapping.class);


    static {
        Arrays.stream(Controller.class.getMethods())
                .filter(method -> method.isAnnotationPresent(GetMapping.class))
                .forEach(method -> {
                    GetMapping annotation = method.getAnnotation(GetMapping.class);
                    GET_MAP.put(annotation.value(), method);
                });
    }

    private HandlerMapping() {
    }

    // 주어진 HttpRequest를 통해 매핑된 메소드를 찾아 반환
    public static Method getMethodMapped(HttpRequest request) {
        for(String s : GET_MAP.keySet()){
            logger.debug("key = {}, value = {}",s,GET_MAP.get(s));
        }
        if (request.isGetMethod()) {
            String path = request.getPath();
            if (GET_MAP.containsKey(path)) {

                return GET_MAP.get(path);
            }
        }
        return null;
    }
}
