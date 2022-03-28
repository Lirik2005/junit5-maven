package junit.service;

import junit.dto.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceTest {

    @Test
    void usersEmptyIfNoUserAdded() {
        UserService userService = new UserService();
        List<User> users = userService.getAll();
        assertTrue(users.isEmpty(), "User list should be empty"); // message отображается, если тест не срабатывает и дает ошибку
    }
}
