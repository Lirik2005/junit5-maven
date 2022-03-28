package junit.service;

import junit.dto.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Аннотация @TestInstance(TestInstance.Lifecycle.PER_METHOD) - Жизненный цикл теста по умолчанию и указывать необязательно. В таком случае
 * userService в каждом тесте создается заново и объекты отличаются (разные хеш-коды). Если поставить аннотацию @TestInstance
 * (TestInstance.Lifecycle.PER_CLASS), то userService создастся только один раз и будет использоваться во всех тестах. В таком случае методы
 * под аннотацией @BeforeAll могут быть НЕСТАТИЧЕСКИМИ!!!
 */

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class UserServiceTest {

    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "111");
    private static final User OLEG = User.of(3, "Oleg", "222");

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

        assertThat(users, empty());
        assertTrue(users.isEmpty(), "User list should be empty"); // message отображается, если тест не срабатывает и дает ошибку
    }

    @Test
    void usersSizeIfUserAdded() {
        System.out.println("Test 2: " + this);
        userService.add(IVAN);
        userService.add(PETR);
        userService.add(OLEG);
        List<User> users = userService.getAll();

        assertThat(users).hasSize(3);
        //    assertEquals(3, users.size());
    }

    @Test
    void loginSuccessIfUserExists() {
        userService.add(IVAN);
        Optional<User> maybeUser = userService.login(IVAN.getUserName(), IVAN.getPassword());

        assertThat(maybeUser).isPresent();
        // assertTrue(maybeUser.isPresent());
        maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
        //  maybeUser.ifPresent(user -> assertEquals(IVAN, user));
    }

    @Test
    void throwExceptionIfUserNameOrPasswordIsNull() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> userService.login(null, "dummy")),
                () -> assertThrows(IllegalArgumentException.class, () -> userService.login("dummy", null))
        );

    }

    @Test
    void usersConvertedToMapById() {
        userService.add(IVAN);
        userService.add(PETR);
        Map<Integer, User> users = userService.getAllConvertedByID();

        assertThat(users, hasKey(IVAN.getId()));

        assertAll(
                () -> assertThat(users).containsKeys(IVAN.getId(), PETR.getId()),
                () -> assertThat(users).containsValues(IVAN, PETR)
        );


    }

    @Test
    void loginFailIfPasswordIncorrect() {
        userService.add(IVAN);
        Optional<User> maybeUser = userService.login(IVAN.getUserName(), "111");

        assertTrue(maybeUser.isEmpty());

    }

    @Test
    void loginFailIfUserDoesNotExist() {
        userService.add(IVAN);
        Optional<User> maybeUser = userService.login("Kiril", IVAN.getPassword());

        assertTrue(maybeUser.isEmpty());
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
