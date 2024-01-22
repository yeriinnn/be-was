package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpRequest {
    private final HttpRequestLine requestLine;
    private final HttpHeader header;

    private HttpRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.requestLine = HttpRequestLine.from(br.readLine());
        this.header = HttpHeader.from(br);
    }

    public static HttpRequest from(InputStream in) throws IOException {
        return new HttpRequest(in);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<URI>").append("\n").append(requestLine.toString()).append("\n");
        sb.append("<HEADER>").append("\n").append(header.toString()).append("\n");
        return sb.toString();
    }

    public String getPath() {
        String path = requestLine.getUri().getPath();
        if (path.equals("/")) {
            return "/index.html"; // 기본 경로 설정
        }
        return path;
    }

    public Map<String, String> getQuery() {
        return requestLine.getUri().getQuery();
    }

    public boolean isGetMethod() {
        return "GET".equals(requestLine.getMethod());
    }
}
