package webserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServer {
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private static final int DEFAULT_PORT = 8080;
    private static final int AWAIT_TERMINATION_SECONDS = 60; // 대기 시간을 60초로 설정

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
            // 현재 수행 중인 작업들이 완료될 때까지 최대 60초 대기
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(AWAIT_TERMINATION_SECONDS, TimeUnit.SECONDS)) {
                    // 지정된 시간 내에 작업들이 완료되지 않으면 강제 종료
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.error("Error waiting for ExecutorService termination: {}", e.getMessage());
                // 혹시 대기 중에 인터럽트가 발생하면 로그를 남기고 계속 진행
            }
        }
    }
}
