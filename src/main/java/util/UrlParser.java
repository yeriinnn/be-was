package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UrlParser {

    public static String extractPathFromRequestLine(String requestLine) {
        // 요청 라인에서 URL 추출
        String[] tokens = requestLine.split("\\s+");
        if (tokens.length >= 2) {
            return tokens[1];
        }

        return null; // 유효한 URL이 없는 경우
    }

    public static Map<String, String> extractHeaders(BufferedReader reader) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String headerLine;
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            // 헤더 추출
            String[] headerTokens = headerLine.split(":");
            if (headerTokens.length == 2) {
                String key = headerTokens[0].trim();
                String value = headerTokens[1].trim();
                headers.put(key, value);
            }
        }
        return headers;
    }
}
