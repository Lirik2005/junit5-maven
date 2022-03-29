package junit.service;


import junit.TestBase;
import junit.dao.UserDao;
import junit.dto.User;
import junit.extension.GlobalExtension;
import junit.extension.UserServiceParamResolver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Аннотация @TestInstance(TestInstance.Lifecycle.PER_METHOD) - Жизненный цикл теста по умолчанию и указывать необязательно. В таком случае
 * userService в каждом тесте создается заново и объекты отличаются (разные хеш-коды). Если поставить аннотацию @TestInstance
 * (TestInstance.Lifecycle.PER_CLASS), то userService создастся только один раз и будет использоваться во всех тестах. В таком случае методы
 * под аннотацией @BeforeAll могут быть НЕСТАТИЧЕСКИМИ!!!
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// @TestMethodOrder(MethodOrderer.OrderAnnotation.class)       аннотация для определения порядка выполнения тестов
// @TestMethodOrder(MethodOrderer.MethodName.class)       аннотация для запуска тестов по алфавиту

@TestMethodOrder(MethodOrderer.DisplayName.class)      // аннотация для более понятного отображения названия тестов

public class UserServiceTest extends TestBase {

    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "111");
    private static final User OLEG = User.of(3, "Oleg", "222");

    @Mock
    private UserDao userDao;
    @InjectMocks
    private UserService userService;

    UserServiceTest(TestInfo testInfo) {
        System.out.println();
    }

    @BeforeAll
    static void init() {
        System.out.println("Before all: ");
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: " + this);
//        Mockito.doReturn(true).when(userDao).delete(IVAN.getId());
//        this.userDao = Mockito.mock(UserDao.class);
//        this.userDao = Mockito.spy(new UserDao());
//        this.userService = new UserService(userDao);
    }

    @Test
    void throwExceptionIfDataBaseIsNotAvailable() {
        Mockito.doThrow(RuntimeException.class).when(userDao).delete(IVAN.getId());

        assertThrows(RuntimeException.class, () -> userService.delete(IVAN.getId()));
    }

    @Test
    void shouldDeleteExistUser() {
        userService.add(IVAN);
        Mockito.doReturn(true).when(userDao).delete(IVAN.getId());
//        Mockito.doReturn(true).when(userDao).delete(Mockito.any());

//        Mockito.when(userDao.delete(IVAN.getId())).thenReturn(true);

        boolean deleteResult = userService.delete(IVAN.getId());
        assertThat(deleteResult).isTrue();
    }


    @Test
    @Order(1)
    void usersEmptyIfNoUserAdded() {
        System.out.println("Test 1: " + this);
        List<User> users = userService.getAll();

        assertThat(users, empty());
        assertTrue(users.isEmpty(), "User list should be empty"); // message отображается, если тест не срабатывает и дает ошибку
    }

    @Test
    @Order(2)
    void usersSizeIfUserAdded() {
        System.out.println("Test 2: " + this);
        userService.add(IVAN);
        userService.add(PETR);
        userService.add(OLEG);
        List<User> users = userService.getAll();

        assertThat(users).hasSize(3);
//        assertEquals(3, users.size());
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

    @AfterEach
    void deleteDataFromDatabase() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    static void closeConnectionPool() {
        System.out.println("After all: ");
    }

    @Nested
    @Tag("Login")
    @DisplayName("Тестирование функционала авторизации")
    class LoginTest {

        @Test
        void loginFailIfUserDoesNotExist() {
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login("Kiril", IVAN.getPassword());

            assertTrue(maybeUser.isEmpty());
        }

        @Test
        void loginFailIfPasswordIncorrect() {
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login(IVAN.getUserName(), "111");

            assertTrue(maybeUser.isEmpty());
        }

        @Test
        @Disabled("Причина отключения теста")
            // тест не будет выполняться, но будет видна причина по которой он отключен
//        @RepeatedTest(5)   //позволяет запустить тест несколько раз
        void checkLoginFunctionalityPerformance() {
            assertTimeout(Duration.ofMillis(300L), () -> {
                Thread.sleep(300L);
                return userService.login("dummy", IVAN.getPassword());
            });
        }


        @Test
        @DisplayName("Удачная авторизация, если пользователь существует")
        void loginSuccessIfUserExists() {
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login(IVAN.getUserName(), IVAN.getPassword());

            assertThat(maybeUser).isPresent();
//            assertTrue(maybeUser.isPresent());
            maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
//              maybeUser.ifPresent(user -> assertEquals(IVAN, user));
        }

        @Test
        void throwExceptionIfUserNameOrPasswordIsNull() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login(null, "dummy")),
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login("dummy", null))
            );
        }

        @ParameterizedTest(name = "{arguments} test")       // в name можно записать название каждого теста по параметру
//        @ArgumentsSource()
//        @EnumSource
        /**
         * Эти аннотации передают null в качестве параметра. Используются только в случае когда в тест принимает только ОДИН параметр!!!
         */
//        @NullSource
//        @EmptySource
//        @ValueSource
//        @NullAndEmptySource

        /**
         * В эту аннотацию передаем статический метод, которые дает нам нужные параметры. В данном случае обращение к статическому методу
         * через полное название класса с помощью решетки потому что тест написан во вложенном классе. В обычном случае аннотация может
         * выглядеть следующим образом: @MethodSource("getArgumentsFroLoginTest")
         */
        @MethodSource("junit.service.UserServiceTest#getArgumentsFroLoginTest")

        /**
         * Эта аннотация способна передавать данные из *.csv. Проблема в том, что можно передавать только стринги или то, что легко
         * конвертируется. Т.е. объект через эту аннотацию не передать. В delimiter указываем разделитель данных (',' ';' ':' и так
         * далее). numLinesToSkip показывает сколько строк в csv-файле надо пропустить (например пропустить заголовок)
         */
//        @CsvFileSource(resources = "/login-test-data.csv", delimiter = ',', numLinesToSkip = 1)
        void loginParametrizedTest(String username, String password, Optional<User> user) {
            userService.add(IVAN);
            userService.add(PETR);

            Optional<User> maybeUser = userService.login(username, password);

            assertThat(maybeUser).isEqualTo(user);
        }


    }

    static Stream<Arguments> getArgumentsFroLoginTest() {
        return Stream.of(
                Arguments.of("Ivan", "123", Optional.of(IVAN)),
                Arguments.of("Petr", "111", Optional.of(PETR)),
                Arguments.of("Petr", "dummy", Optional.empty()),
                Arguments.of("dummy", "123", Optional.empty())
        );
    }
}
