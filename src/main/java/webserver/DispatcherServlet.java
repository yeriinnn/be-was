package webserver;

import controller.Controller;
import http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import http.HttpRequest;
import http.HttpResponse;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static java.lang.invoke.MethodType.methodType;

public class DispatcherServlet {
    private static final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);
    private static final DispatcherServlet servlet = new DispatcherServlet();


    //디스패처서블릿 싱글톤 적용
    private DispatcherServlet() {
    }
    public static DispatcherServlet getInstance() {
        return servlet;
    }

    // http 요청 처리를 위한 시작점
    public void doService(HttpRequest request, OutputStream out) throws Throwable {
        logger.debug("Request Start\n{}", request);
        try {
            HttpResponse httpResponse = new HttpResponse();

            Method method = HandlerMapping.getMethodMapped(request);

            String path;

            if (method == null && request.isGetMethod()) {
                path = request.getPath();
            } else {
                path = String.valueOf(handle(request, method));
                logger.debug("path = {}", path);
            }

            if (path.startsWith("redirect:")) {
                // 리다이렉트를 위한 처리
                httpResponse.setResponse(HttpStatus.FOUND); // 302 Found 상태 코드 사용
                httpResponse.setLocation(path.substring("redirect:".length()));
            } else {
                String filePath = getPathForRequest(path);
                logger.debug("filepath = {}", filePath);

                if (filePath != null) {
                    String fileContent = readFileContent(filePath);
                    HttpResponse response = HttpResponse.ok(filePath);
                    response.setResponseText(fileContent);
                    processDispatchResult(response, out);
                    return;
                }
            }
            logger.debug("location = {}", httpResponse.getLocation());
            processDispatchResult(httpResponse, out);
            logger.debug("Response End\n{}", httpResponse);
        } catch (Throwable throwable) {
            handleException(throwable, out);
        }
    }

    private String getPathForRequest(String requestPath) {
        if(requestPath.startsWith("redirect:")){
            return requestPath;
        }
        else if (requestPath.endsWith(".html")) {
            // HTML 파일은 templates에서 찾기
            return Paths.get("src/main/resources/templates", requestPath).toString();
        } else {
            // 그외의 파일은 static에서 찾기
            return Paths.get("src/main/resources/static", requestPath).toString();
        }
    }

    private String readFileContent(String filePath) throws IOException {
        byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
        return new String(fileBytes, StandardCharsets.UTF_8);
    }

    private String handle(HttpRequest request, Method method) throws Throwable {
        String path = request.getPath();
        if (hasRequestPathMapped(method)) {
            return executeRequest(request, method);
        } else {
            return path;
        }
    }

    private boolean hasRequestPathMapped(Method method) {
        return method != null;
    }

    private String executeRequest(HttpRequest request, Method method) throws Throwable {
        MethodHandle methodHandle = getMethodHandle(method);
        if (hasParameter(methodHandle.type())) {
            Map<String, String> map = (request.isGetMethod()) ? request.getQuery() : null;
            return (String) methodHandle.invoke(map);
        } else {
            return (String) methodHandle.invoke();
        }
    }

    private MethodHandle getMethodHandle(Method method) throws NoSuchMethodException, IllegalAccessException {
        MethodType methodType = (hasParameter(method)) ? methodType(String.class, method.getParameterTypes()) : methodType(String.class);
        return MethodHandles.lookup()
                .findVirtual(Controller.class, method.getName(), methodType)
                .bindTo(new Controller());
    }

    private boolean hasParameter(MethodType methodType) {
        return methodType.parameterCount() != 0;
    }

    private boolean hasParameter(Method method) {
        return method.getParameterCount() != 0;
    }

    private void processDispatchResult(HttpResponse response, OutputStream out) {
        response.writeResponseToOutputStream(out);
    }

    private void handleException(Throwable throwable, OutputStream out) {
        try {
            String errorMessage = "Internal Server Error: " + throwable.getMessage();
            HttpResponse response = HttpResponse.internalServerErrorWithText(errorMessage);
            processDispatchResult(response, out);
        } catch (Throwable e) {
            logger.error("Error while handling exception", e);
        }
    }
}
