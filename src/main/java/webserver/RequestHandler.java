package webserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private final Lock lock;

    public RequestHandler(Socket connectionSocket, Lock lock) {
        this.connection = connectionSocket;
        this.lock = lock;
    }

    public void run() {
        lock.lock();
        try{ //lock: 동시성 제어
            logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                    connection.getPort());

            try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                String requestLine = reader.readLine();

                handleHttpRequest(requestLine, out);
                parseAndLogHttpRequest(requestLine, reader);
            } catch (IOException e) {
                logger.error("Error while processing client request: {}", e.getMessage());
            }

        }catch (Exception e){
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    private void handleHttpRequest(String requestLine, OutputStream out) {
        if (requestLine != null && requestLine.startsWith("GET /index.html")) {
            try {
                serveIndexHtml(out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            logger.warn("Unsupported request received.");
            sendDefaultResponse(out);
        }
    }
    private void sendDefaultResponse(OutputStream out) {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = "Hello World".getBytes();
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private void parseAndLogHttpRequest(String requestLine, BufferedReader reader) {
        // HTTP 메서드 파싱
        String[] parts = requestLine.split("\\s+"); // 공백을 기준으로 문자열 분리
        if (parts.length >= 3) {
            String httpMethod = parts[0];
            String uri = parts[1];
            String httpVersion = parts[2];

            // HTTP 요청 헤더 파싱
            Map<String, String> headers = parseHeaders(reader);

            // 로거를 이용하여 HTTP Request 내용 및 메서드, 헤더 출력
            logger.debug("Received HTTP Request - Method: {}, URI: {}, HTTP Version: {}", httpMethod, uri, httpVersion);
            logger.debug("Host: {}", headers.get("Host"));
            logger.debug("Accept: {}", headers.get("Accept"));
            logger.debug("User-Agent: {}", headers.get("User-Agent"));
            logger.debug("Referer: {}", headers.get("Referer"));

        } else {
            logger.warn("Invalid HTTP Request format: {}", requestLine);
        }
    }

    private Map<String, String> parseHeaders(BufferedReader reader) {
        Map<String, String> headers = new HashMap<>();
        String line;
        try {
            // 빈 줄이 나올 때까지 헤더를 읽어들임
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                String[] headerParts = line.split(":\\s+", 2); // ": "을 기준으로 문자열 분리
                if (headerParts.length == 2) {
                    headers.put(headerParts[0], headerParts[1]);
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing HTTP headers: {}", e.getMessage(), e);
        }
        return headers;
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void serveIndexHtml(OutputStream out) throws IOException {
        try {
            // index.html 파일을 읽어오기
            String filePath = "src/main/resources/templates/index.html";
            Path indexPath = Paths.get(filePath);
            byte[] body = Files.readAllBytes(indexPath);

            // HTTP 응답 보내기
            DataOutputStream dos = new DataOutputStream(out);
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            // 파일을 읽는 도중 오류가 발생하면 500 Internal Server Error를 응답
            DataOutputStream dos = new DataOutputStream(out);
            //response200Header(dos);
            responseBody(dos, "Internal Server Error".getBytes());
        }
    }
}
