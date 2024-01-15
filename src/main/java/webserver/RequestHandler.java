package webserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String requestLine = reader.readLine();

            // 요청이 "/index.html" 경로로 오면 파일을 읽어 응답합니다.
            if (requestLine != null && requestLine.startsWith("GET /index.html")) {
                serveIndexHtml(out);
            } else {
                // 기본적으로 "Hello World" 응답을 보냅니다.
                sendHelloWorldResponse(out);
            }

//
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = "Hello World".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void serveIndexHtml(OutputStream out) throws IOException {
        try {
            // index.html 파일을 읽어옵니다.
            Path indexPath = Paths.get("src/main/resources/templates/index.html");
            byte[] body = Files.readAllBytes(indexPath);

            // HTTP 응답을 보냅니다.
            DataOutputStream dos = new DataOutputStream(out);
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            // 파일을 읽는 도중 오류가 발생하면 500 Internal Server Error를 응답합니다.
            DataOutputStream dos = new DataOutputStream(out);
            //response200Header(dos);
            responseBody(dos, "Internal Server Error".getBytes());
        }
    }

    private void sendHelloWorldResponse(OutputStream out) throws IOException {
        // 기본적으로 "Hello World" 응답을 보냅니다.
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = "Hello World".getBytes();
        response200Header(dos, body.length);
        responseBody(dos, body);
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
}
