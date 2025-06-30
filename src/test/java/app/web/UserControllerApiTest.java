package app.web;

import app.TestBuilder;
import app.user.model.User;
import app.user.model.UserPrinciple;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerApiTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void putUnauthorizedRequestToChangeUserStatus_shouldRedirectToNotFoundView() throws Exception {

        UserPrinciple principle = new UserPrinciple(TestBuilder.aRandomUser());
        UUID userId = principle.getUser().getId();

        MockHttpServletRequestBuilder request = put("/users/{id}/status", userId)
                .with(user(principle))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/not-found"));
        verify(userService, never()).changeUserStatus(any());

    }

    @Test
    void putAuthorizedRequestToChangeUserStatus_shouldRedirectToUsersView() throws Exception {

        UserPrinciple principle = new UserPrinciple(TestBuilder.aRandomAdmin());
        UUID userId = principle.getUser().getId();

        MockHttpServletRequestBuilder request = put("/users/{id}/status", userId)
                .with(user(principle))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
        verify(userService, times(1)).changeUserStatus(any());

    }
}
