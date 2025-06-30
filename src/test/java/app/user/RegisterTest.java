package app.user;

import app.TestBuilder;
import app.credit.model.Credit;
import app.credit.model.CreditStatus;
import app.credit.service.CreditService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;


import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class RegisterTest {

    @Autowired
    private UserService userService;

    @Autowired
    private CreditService creditService;

    @Test
    void register_happyPath() {

        RegisterRequest registerRequest = TestBuilder.aRandomRegisterRequest();

        User user = userService.register(registerRequest);
        assertNotNull(user);
        assertEquals(user.getWallets().size(), 3);

        Credit credit = creditService.getCreditByOwnerId(user.getId());
        assertEquals(credit.getStatus(), CreditStatus.PAYED);

    }
}
