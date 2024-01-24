package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpHeader {
    private final Map<String, String> header = new HashMap<>();

    private HttpHeader(BufferedReader br) throws IOException {
        String line;
        while (!isNullOrBlank(line = br.readLine())) {
            int colonIndex = line.indexOf(':');
            header.put(line.substring(0, colonIndex), line.substring(colonIndex + 2));
        }
    }

    public static HttpHeader from(BufferedReader br) throws IOException {
        return new HttpHeader(br);
    }

    private boolean isNullOrBlank(String line) {
        return line == null || line.isBlank();
    }


    @Override
    public String toString() {
        return header.keySet().stream()
                .map(key -> key + ": " + header.get(key))
                .collect(Collectors.joining("\r\n"));
    }
}
