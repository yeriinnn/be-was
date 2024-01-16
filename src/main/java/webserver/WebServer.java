package webserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServer {
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private static final int DEFAULT_PORT = 8080;

    public static void main(String args[]) throws Exception {
        int port = (args == null || args.length == 0) ? DEFAULT_PORT : Integer.parseInt(args[0]);

        // 캐시된 스레드 풀을 생성한다.
        ExecutorService executorService = Executors.newCachedThreadPool();

        // 서버 소켓 생성
        try (ServerSocket listenSocket = new ServerSocket(port)) {
            logger.info("Web Application Server started on {} port.", port);

            // 클라이언트 연결을 수락하고 작업을 Executor에 제출
            Socket connection;
            while ((connection = listenSocket.accept()) != null) {
                executorService.submit(new RequestHandler(connection));
            }
        } finally {
            // 작업이 완료되면 Executor 서비스를 종료
            executorService.shutdown();
        }
    }
}
