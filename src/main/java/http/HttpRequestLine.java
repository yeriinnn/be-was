package http;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class HttpRequestLine {
    private String method;
    private Uri uri;
    private String version;

    private HttpRequestLine(String requestLine) {
        if(requestLine == null){
            return;
        }
        String[] tokens = requestLine.split(" ");
        this.method = tokens[0];
        this.uri = Uri.from(tokens[1]);
        this.version = tokens[2];
    }

    public static HttpRequestLine from(String requestLine) {
        return new HttpRequestLine(URLDecoder.decode(requestLine, StandardCharsets.UTF_8));
    }

    String getMethod() {
        return method;
    }

    Uri getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return method + " " + uri + " " + version;
    }
}