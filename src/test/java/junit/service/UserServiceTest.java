package junit.service;

import junit.dto.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Аннотация @TestInstance(TestInstance.Lifecycle.PER_METHOD) - Жизненный цикл теста по умолчанию и указывать необязательно. В таком случае
 * userService в каждом тесте создается заново и объекты отличаются (разные хеш-коды). Если поставить аннотацию @TestInstance
 * (TestInstance.Lifecycle.PER_CLASS), то userService создастся только один раз и будет использоваться во всех тестах. В таком случае методы
 * под аннотацией @BeforeAll могут быть НЕСТАТИЧЕСКИМИ!!!
 */

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class UserServiceTest {

    private UserService userService;

    @BeforeAll
    static void init() {
        System.out.println("Before all: ");
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: " + this);
        userService = new UserService();
    }

    @Test
    void usersEmptyIfNoUserAdded() {
        System.out.println("Test 1: " + this);
        List<User> users = userService.getAll();
        assertTrue(users.isEmpty(), "User list should be empty"); // message отображается, если тест не срабатывает и дает ошибку
    }

    @Test
    void usersSizeIfUserAdded() {
        System.out.println("Test 2: " + this);
        userService.add(new User());
        userService.add(new User());
        userService.add(new User());

        List<User> users = userService.getAll();
        assertEquals(3, users.size());
    }

    @AfterEach
    void deleteDataFromDatabase() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    static void closeConnectionPool() {
        System.out.println("After all: ");
    }
}
