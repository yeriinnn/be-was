package http;

import java.io.*;
import java.nio.file.*;

public class HttpResponse {
    private static final String ROOT_PATH = "src/main/resources";
    private static final String INDEX = "templates";

    private HttpStatus status;
    private String filePath;
    private String responseText;

    private String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    private static final String CRLF = "\r\n";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String LOCATION = "Location";

    private HttpResponse(HttpStatus status, String filePath) {
        this.status = status;
        this.filePath = filePath;
    }

    public static HttpResponse redirect() {
        return new HttpResponse(HttpStatus.FOUND, INDEX);
    }

    public static HttpResponse init(String filePath) {
        return new HttpResponse(HttpStatus.OK, filePath);
    }

    public static HttpResponse ok(String filePath) {
        ContentType type = ContentType.findBy(filePath);
        return new HttpResponse(HttpStatus.OK, filePath, type.getMime());
    }

    public static HttpResponse internalServerError(String filePath) {
        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, filePath);
    }

    public static HttpResponse internalServerErrorWithText(String errorMessage) {
        HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, "/index.html");
        response.setResponseText(errorMessage);
        return response;
    }

    public void setResponseText(String text) {
        this.responseText = text;
    }

    public HttpResponse() {

    }
    private HttpResponse(HttpStatus status, String filePath, String contentType) {
        this.status = status;
        this.filePath = filePath;
    }

    public void setResponse(HttpStatus status) {
        this.status = status;
    }

    public void writeResponseToOutputStream(OutputStream out) {
        DataOutputStream dos = new DataOutputStream(out);
        try {
            responseHeader(dos);
            responseBody(dos);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void responseBody(DataOutputStream dos) throws IOException {
        if (status == HttpStatus.OK || status == HttpStatus.FOUND) {
            if (responseText == null) {
                try {
                    writeContentFromFile(dos);
                } catch (FileNotFoundException e) {
                    HttpResponse notFoundResponse = HttpResponse.init("/index.html");
                    notFoundResponse.setResponse(HttpStatus.NOT_FOUND);
                    notFoundResponse.writeResponseToOutputStream(dos);
                }
            } else {
                dos.write(responseText.getBytes());
            }
        }
    }

    private void writeContentFromFile(DataOutputStream dos) throws IOException {
        Path filePathObj;

        // 파일 경로에 따라 올바른 위치 설정
        if (this.filePath.startsWith("static")) {
            filePathObj = Paths.get(ROOT_PATH, this.filePath);
        } else {
            filePathObj = Paths.get(ROOT_PATH, INDEX, this.filePath);
        }

        // 나머지 코드는 그대로 유지
        if (!Files.exists(filePathObj)) {
            throw new FileNotFoundException("File not found");
        }

        byte[] body = Files.readAllBytes(filePathObj);
        dos.write(body, 0, body.length);
    }


    private void responseHeader(DataOutputStream dos) throws IOException {
        ContentType type = ContentType.findBy(this.filePath);
        dos.writeBytes("HTTP/1.1 " + status.getCode() + " " + status.getDescription() + CRLF);
        dos.writeBytes(CONTENT_TYPE + ": " + type.getMime() + ";charset=utf-8" + CRLF);
        if (location != null) {
            dos.writeBytes("Location: " + location);
        }
        System.out.println("location = "+location);

        if (status == HttpStatus.FOUND) {
            dos.writeBytes(LOCATION + ": " + INDEX + CRLF);
        }

        dos.writeBytes(CRLF);
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "status=" + status +
                ", filePath='" + filePath + '}';
    }
}
