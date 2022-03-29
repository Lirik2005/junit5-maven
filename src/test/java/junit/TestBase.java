package junit;

import junit.extension.GlobalExtension;
import junit.extension.PostProcessingExtension;
import junit.extension.UserServiceParamResolver;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({                                       // аннотация необходима для внедрения зависимости и автоматического создания userService
        UserServiceParamResolver.class,
        GlobalExtension.class
    //    PostProcessingExtension.class
})
public abstract class TestBase {

}
