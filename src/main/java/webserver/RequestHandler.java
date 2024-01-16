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
    private OutputStream outputStream; // 외부에서 주입된 OutputStream

    public RequestHandler(Socket connectionSocket, OutputStream outputStream) {
        this.connection = connectionSocket;
        this.outputStream = outputStream;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = outputStream) {
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

            // 동기화 추가
            synchronized (out) {
                response200Header(out, body.length); // 200 OK 응답 헤더 작성
                responseBody(out, body); // 응답 바디 작성
            }
        } else {
            sendNotFoundError(out); // 파일이 존재하지 않을 경우 404 에러 응답
        }
    }

    // 200 OK 응답 헤더 작성
    private void response200Header(OutputStream out, int lengthOfBodyContent) {
        try {
            out.write("HTTP/1.1 200 OK \r\n".getBytes());
            out.write("Content-Type: text/html;charset=utf-8\r\n".getBytes());
            out.write(("Content-Length: " + lengthOfBodyContent + "\r\n").getBytes());
            out.write("\r\n".getBytes());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    // 응답 바디 작성
    private void responseBody(OutputStream out, byte[] body) {
        try {
            out.write(body, 0, body.length);
            out.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    // 404 Not Found 에러 응답 작성
    private void sendNotFoundError(OutputStream out) throws IOException {
        out.write("HTTP/1.1 404 Not Found \r\n".getBytes());
        out.write("Content-Type: text/plain;charset=utf-8\r\n".getBytes());
        out.write("Content-Length: 0\r\n".getBytes());
        out.write("\r\n".getBytes());
        out.flush();
    }
}
