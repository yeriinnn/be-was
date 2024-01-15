package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private static final String WEB_ROOT = "src/main/resources/templates"; // 웹 리소스의 루트 디렉토리

    private Socket connection; // 클라이언트와의 연결을 담당하는 소켓

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            // 요청 라인 읽기
            String requestLine = reader.readLine();
            String[] requestParts = requestLine.split("\\s+");

            String method = requestParts[0]; // HTTP 메서드 (GET, POST 등)
            String url = requestParts[1]; // 요청된 URL

            if ("GET".equalsIgnoreCase(method)) {
                serveStaticFile(out, url); // 정적 파일 서비스 메서드 호출
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    // 정적 파일 서비스 메서드
    private void serveStaticFile(OutputStream out, String url) throws IOException {
        if ("/".equals(url)) {
            url = "/index.html"; // 루트 경로일 경우 기본적으로 index.html 제공
        }

        Path filePath = Paths.get(WEB_ROOT, url); // 요청된 URL에 해당하는 파일의 경로
        File file = filePath.toFile();

        if (file.exists() && !file.isDirectory()) {
            byte[] body = Files.readAllBytes(filePath); // 파일 내용을 바이트 배열로 읽어옴
            DataOutputStream dos = new DataOutputStream(out);

            response200Header(dos, body.length); // 200 OK 응답 헤더 작성
            responseBody(dos, body); // 응답 바디 작성
        } else {
            sendNotFoundError(out); // 파일이 존재하지 않을 경우 404 에러 응답
        }
    }

    // 200 OK 응답 헤더 작성
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

    // 응답 바디 작성
    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    // 404 Not Found 에러 응답 작성
    private void sendNotFoundError(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);

        dos.writeBytes("HTTP/1.1 404 Not Found \r\n");
        dos.writeBytes("Content-Type: text/plain;charset=utf-8\r\n");
        dos.writeBytes("Content-Length: 0\r\n");
        dos.writeBytes("\r\n");
        dos.flush();
    }
}
