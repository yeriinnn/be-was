package controller;

import annotation.GetMapping;
import db.Database;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);
    private final String USER_ID = "userId";
    private final String PASSWORD = "password";
    private final String NAME = "name";
    private final String EMAIL = "email";

    // GET 회원가입
    @GetMapping(value = "/user/create")
    public String createUser(Map<String, String> query) {
        logger.debug("GET을 통해 유저 회원가입");

        String userId = query.get(USER_ID);
        String password = query.get(PASSWORD);
        String name = query.get(NAME);
        String email = query.get(EMAIL);

        logger.debug("받은 유저 데이터 - userId: {}, password: {}, name: {}, email: {}", userId, password, name, email);

        User user = new User(userId, password, name, email);

        Database.addUser(user);
        logger.debug("유저 생성 완료 : {}", user);

        return "redirect:/user/login.html";
    }
}
