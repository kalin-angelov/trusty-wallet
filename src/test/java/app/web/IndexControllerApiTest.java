package app.web;

import app.TestBuilder;
import app.credit.service.CreditService;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.user.model.UserPrinciple;
import app.user.service.UserService;
import app.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
public class IndexControllerApiTest {

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private CreditService creditService;
    @MockitoBean
    private WalletService walletService;
    @MockitoBean
    private TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestToIndexEndpoint_shouldReturnIndexView() throws Exception {

        MockHttpServletRequestBuilder request = get("/");

        mockMvc.perform(request)
                .andExpect(status().is(200))
                .andExpect(view().name("index"));
    }

    @Test
    void getRequestToHomeEndpointWithUnauthenticatedRequest_shouldRedirectToLoginView() throws Exception {

        MockHttpServletRequestBuilder request = get("/home");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection());
        verify(userService, never()).getUserById(any());
    }

    @Test
    void getRequestToHomeEndpointWith_shouldReturnHomeView() throws Exception {

        UserPrinciple principle = new UserPrinciple(TestBuilder.aRandomUser());
        UUID userId = principle.getUser().getId();

        when(userService.getUserById(userId)).thenReturn(principle.getUser());
        when(creditService.getCreditByOwnerId(userId)).thenReturn(TestBuilder.aRandomCredit());

        MockHttpServletRequestBuilder request = get("/home")
                .with(user(principle));

        mockMvc.perform(request)
                .andExpect(status().is(200))
                .andExpect(view().name("home"))
                .andExpect(model().attribute("user", instanceOf(User.class)))
                .andExpect(model().attributeExists("credit"));
        verify(userService, times(1)).getUserById(userId);
        verify(creditService, times(1)).getCreditByOwnerId(userId);
    }
}
