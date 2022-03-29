package junit;

import junit.extension.GlobalExtension;
import junit.extension.PostProcessingExtension;
import junit.extension.UserServiceParamResolver;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({                                       // аннотация необходима для внедрения зависимости и автоматического создания userService
        UserServiceParamResolver.class,
        GlobalExtension.class,
        MockitoExtension.class
    //    PostProcessingExtension.class
})
public abstract class TestBase {

}
