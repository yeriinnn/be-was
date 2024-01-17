# java-was-2023

Java Web Application Server 2023

## 프로젝트 정보 

이 프로젝트는 우아한 테크코스 박재성님의 허가를 받아 https://github.com/woowacourse/jwp-was 
를 참고하여 작성되었습니다.


## 개발환경

java 17 version
IntelliJ


## 프로젝트 소개

# 요구사항1) 정적인 html 파일 응답
http://localhost:8080/index.html 로 접속했을 때 src/main/resources/templates 디렉토리의 index.html 파일을 읽어 클라이언트에 응답
'''
private void serveIndexHtml(OutputStream out) throws IOException {
        ...
            String filePath = "src/main/resources/templates/index.html";
            Path indexPath = Paths.get(filePath);
            byte[] body = Files.readAllBytes(indexPath);

        ...
    }
'''

# 요구사항2) HTTP Request 내용 출력
서버로 들어오는 HTTP Request의 내용을 읽고 로거(log.debug)를 이용해 출력
- 중요하다고 생각하는 request message 파싱
    - host
    - accept
    - user-agent
    - referer

'''
    private void parseAndLogHttpRequest(String requestLine, BufferedReader reader) {
        // HTTP 메서드 파싱
        String[] parts = requestLine.split("\\s+"); 
        if (parts.length >= 3) {
            String httpMethod = parts[0];
            String uri = parts[1];
            String httpVersion = parts[2];

            // HTTP 요청 헤더 파싱
            Map<String, String> headers = parseHeaders(reader);

            // 로거를 이용하여 HTTP Request 내용 및 메서드, 헤더 출력
            ...

        } 
        ...
    }

    private Map<String, String> parseHeaders(BufferedReader reader) {
        Map<String, String> headers = new HashMap<>();
        String line;
        
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                String[] headerParts = line.split(":\\s+", 2); /
                if (headerParts.length == 2) {
                    headers.put(headerParts[0], headerParts[1]);
                }
            }
        
        ...

        return headers;
    }
'''

# 요구사항3) Concurrent 패키지 사용
'''
public void run() {
        lock.lock();
        try{ 
            ...
        }catch (Exception e){
            ...
        } finally {
            lock.unlock();
        }
}
'''


## 추가학습

## HTTP
- 웹 어플리케이션에서 HTML, JS, CSS 같은 파일을 웹 서버에게 요청하고 받아오는 핵심 프로토콜
- request와 response 동작에 기반하여 서비스 제공
- HTTP 1.0
    - 연결 수립 → 동작 → 연결 해제
    - 문서를 전송받으면 연결을 끊고 다시 연결하여 Data 전
- HTTP 1.1
    - Multiple Request 처리 가능 → 요청이 많을 수록 연속적인 응답을 제공하는 pipeline 방식
    - 하나의 IP 주소가 다수의 웹사이트와 연결 가능

# HTTP 요청 프로토콜
- 요청 방식을 정의
- 클라이언트의 정보를 담고 있음
- Request Line
    - 요청타입 + 공백 + URI + 공백 + HTTP 버전
- 요청 타입
    - GET
        - BODY가 없기 때문에 DATA를 url에 담아 보냄
        - Request line(Data를 포함)+ Header로 구성
    - POST
        - BODY에 데이타를 포함시켜 보냄
        - Request line + Header + Body

# HTTP 응답 프로토콜
- 사용자가 볼 웹 페이지를 담고 있음
- Status line
    - HTTP 버전 + 공백 + 상태코드 + 공백 + 상태문구
- 상태코드
    - 200: 성공
    - 400: 클라이언트의 오류
    - 500: 서버의 오류

# URI 구조
- scheme ://host[:port][/path][?query]


## 스레드
- 동일한 자원을 공유하는 여러개의 스레드가 번갈아가며 동작 → 어떤 task가 먼저 실행될 지 알 수 없음 → 개발자가 의도하지 않은 동작이 발생할 수 있음 → 동시성 문제 발생
- lock()과 unlock()을 이용하여 동시성 문제 해결


## Concurrent 패키지
<aside>
💡 java.util.concurrent
</aside>

# 주요 클래스
- Locks: 상호 배제를 사용할 수 있는 클래스 제공
- Atomic: 동기화가 되어있는 변수를 제공
- Executors: 쓰레드 풀 생성, 스레드 생명주기 관리, task 등록과 실행 등을 간편하게 처리
- Queue: thread-safe한 FIFO 큐를 제공
- Synchronizers: 특수한 목적의 동기화를 처리하는 5개의 클래스 제공
