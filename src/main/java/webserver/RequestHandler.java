package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.UrlParser;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private static final String WEB_ROOT = "src/main/resources/templates"; // 웹 리소스의 루트 디렉토리

    private final Socket connection; // 클라이언트와의 연결을 담당하는 소켓
    private OutputStream outputStream; // 외부에서 주입된 OutputStream

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
        try {
            this.outputStream = connection.getOutputStream(); // 소켓의 OutputStream을 사용
        } catch (IOException e) {
            logger.error("Error creating output stream: {}", e.getMessage());
        }
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = outputStream) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            // 요청 라인 읽기
            String requestLine = reader.readLine();
            logger.debug("HTTP Request: {}", requestLine); // 요청 라인 로그 출력

            // URL 및 헤더 추출
            String url = UrlParser.extractPathFromRequestLine(requestLine);
            Map<String, String> headers = UrlParser.extractHeaders(reader);

            // Host 및 Connection 헤더 추출
            String host = headers.get("Host");
            String connection = headers.get("Connection");

            if (url != null) {
                String method = requestLine.split("\\s+")[0]; // HTTP 메서드 (GET, POST 등)

                // 로그에 메소드, URL, Host, Connection 출력
                logger.debug("Request Details \nMethod: " + method + "\nURL: " + url + "\nHost: " + host + "\nConnection: " + connection);

                //우선 GET에 대한 처리만
                if ("GET".equalsIgnoreCase(method)) {
                    serveStaticFile(out, url); // 정적 파일 서비스 메서드 호출
                }
            } else {
                logger.error("Invalid request format: {}", requestLine);
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

        String fileExtension = getFileExtension(url);

        // 확장자가 .html이면 templates에서, 그 외에는 static에서 찾음
        String resourcePath = (".html".equalsIgnoreCase(fileExtension))
                ? Paths.get(WEB_ROOT, url).toString()
                : Paths.get("src/main/resources/static", url).toString();

        Path filePath = Paths.get(resourcePath);
        File file = filePath.toFile();

        if (file.exists() && !file.isDirectory()) {
            byte[] body = Files.readAllBytes(filePath);

            // 동기화 추가
            synchronized (out) {
                String contentType = getContentType(fileExtension);
                response200Header(out, body.length, contentType); // MIME 타입 추가
                responseBody(out, body); // 응답 바디 작성
            }
        } else {
            sendNotFoundError(out); // 파일이 존재하지 않을 경우 404 에러 응답
        }
    }

    // MIME 타입 설정 메서드
    private String getContentType(String fileExtension) {
        switch (fileExtension.toLowerCase()) {
            case ".html":
                return "text/html;charset=utf-8";
            case ".css":
                return "text/css;charset=utf-8";
            case ".js":
                return "application/javascript;charset=utf-8";
            default:
                return "application/octet-stream"; // 기본적으로 이진 파일로 처리
        }
    }

    // 파일 확장자 추출 메서드
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }

    // 200 OK 응답 헤더 작성
    private void response200Header(OutputStream out, int lengthOfBodyContent, String contentType) {
        try {
            out.write("HTTP/1.1 200 OK \r\n".getBytes());
            out.write(("Content-Type: " + contentType + "\r\n").getBytes());
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