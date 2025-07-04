package app.web;

import app.TestBuilder;
import app.transaction.model.Transaction;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.user.model.UserPrinciple;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
public class TransactionControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getTransactionRequest_shouldReturnTransactionView() throws Exception {

        UserPrinciple principle = new UserPrinciple(TestBuilder.aRandomUser());
        UUID userId = principle.getUser().getId();
        List<Transaction> userTransactions = List.of(TestBuilder.aRandomTransaction(), TestBuilder.aRandomTransaction());

        when(userService.getUserById(userId)).thenReturn(principle.getUser());
        when(transactionService.getAllUserTransactions(userId)).thenReturn(userTransactions);

        MockHttpServletRequestBuilder request = get("/transactions")
                .with(user(principle));

        mockMvc.perform(request)
                .andExpect(status().is(200))
                .andExpect(view().name("transactions"))
                .andExpect(model().attribute("user", instanceOf(User.class)))
                .andExpect(model().attribute("transactions", instanceOf(List.class)));
        verify(userService, times(1)).getUserById(userId);
        verify(transactionService, times(1)).getAllUserTransactions(userId);
    }

    @Test
    void getTransactionResultRequest_shouldReturnTransactionResultView() throws Exception {

        UserPrinciple principle = new UserPrinciple(TestBuilder.aRandomUser());
        UUID userId = principle.getUser().getId();
        Transaction transaction = TestBuilder.aRandomTransaction();
        UUID transactionId = transaction.getId();

        when(userService.getUserById(userId)).thenReturn(principle.getUser());
        when(transactionService.getTransaction(transactionId, userId)).thenReturn(transaction);

        MockHttpServletRequestBuilder request = get("/transactions/{id}", transactionId)
                .with(user(principle));

        mockMvc.perform(request)
                .andExpect(status().is(200))
                .andExpect(view().name("transaction-result"))
                .andExpect(model().attribute("user", instanceOf(User.class)))
                .andExpect(model().attribute("transaction", instanceOf(Transaction.class)));
        verify(userService, times(1)).getUserById(userId);
        verify(transactionService, times(1)).getTransaction(transactionId, userId);
    }
}
