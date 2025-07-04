package app.web;

import app.TestBuilder;
import app.transaction.model.Transaction;
import app.user.model.User;
import app.user.model.UserPrinciple;
import app.user.service.UserService;
import app.wallet.service.WalletService;
import app.web.dto.TransferRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferController.class)
public class TransferControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private WalletService walletService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getTransferRequest_shouldReturnTransferView() throws Exception {

        UserPrinciple principle = new UserPrinciple(TestBuilder.aRandomUser());
        UUID userId = principle.getUser().getId();

        when(userService.getUserById(userId)).thenReturn(principle.getUser());

        MockHttpServletRequestBuilder request = get("/transfers")
                .with(user(principle));

        mockMvc.perform(request)
                .andExpect(status().is(200))
                .andExpect(view().name("transfer"))
                .andExpect(model().attribute("user", instanceOf(User.class)))
                .andExpect(model().attribute("transferRequest", instanceOf(TransferRequest.class)));
        verify(userService, times(1)).getUserById(userId);

    }

    @Test
    void postTransferRequest_shouldCreateNewTransferAndRedirectToTransactionsView() throws Exception {

        UserPrinciple principle = new UserPrinciple(TestBuilder.aRandomUser());
        UUID userId = principle.getUser().getId();
        Transaction transaction = TestBuilder.aRandomTransaction();
        UUID transactionId = transaction.getId();
        TransferRequest transferRequest = TestBuilder.aRandomTransferRequest();

        when(userService.getUserById(userId)).thenReturn(principle.getUser());
        when(walletService.makeTransfer(principle.getUser(), transferRequest)).thenReturn(transaction);

        MockHttpServletRequestBuilder request = post("/transfers")
                .formField("sender", String.valueOf(transferRequest.getSender()))
                .formField("receiver", String.valueOf(transferRequest.getReceiver()))
                .formField("amount", String.valueOf(transferRequest.getAmount()))
                .with(user(principle))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions/" + transactionId));
        verify(userService, times(1)).getUserById(userId);
        verify(walletService, times(1)).makeTransfer(principle.getUser(), transferRequest);

    }
}
