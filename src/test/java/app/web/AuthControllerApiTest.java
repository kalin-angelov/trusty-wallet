package app.web;

import app.TestBuilder;
import app.exception.EmailAlreadyExistException;
import app.exception.UsernameAlreadyExistException;
import app.user.service.UserService;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerApiTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestToRegisterEndpoint_shouldReturnRegisterView() throws Exception {

        MockHttpServletRequestBuilder request = get("/register");

        mockMvc.perform(request)
                .andExpect(status().is(200))
                .andExpect(view().name("register"))
                .andExpect(model().attribute("registerRequest", instanceOf(RegisterRequest.class)));
    }

    @Test
    void getRequestToLoginEndpoint_shouldReturnLoginView() throws Exception {

        MockHttpServletRequestBuilder request = get("/login");

        mockMvc.perform(request)
                .andExpect(status().is(200))
                .andExpect(view().name("login"))
                .andExpect(model().attribute("loginRequest", instanceOf(LoginRequest.class)));
    }

    @Test
    void getRequestToLoginEndpointWithErrorParameter_shouldReturnLoginViewWithErrorMessage() throws Exception {

        MockHttpServletRequestBuilder request = get("/login").param("error", "");

        mockMvc.perform(request)
                .andExpect(status().is(200))
                .andExpect(view().name("login"))
                .andExpect(model().attribute("loginRequest", instanceOf(LoginRequest.class)))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void postRequestToRegisterEndpoint_happyPath() throws Exception {

        MockHttpServletRequestBuilder request = post("/register")
                .formField("email", "email@gmail.com")
                .formField("username", "newUser")
                .formField("password", "123654")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verify(userService, times(1)).register(any());
    }

    @Test
    void postRequestToRegisterEndpointWithInvalidInput_shouldReturnRegisterView() throws Exception {

        MockHttpServletRequestBuilder request = post("/register")
                .formField("email", "")
                .formField("username", "")
                .formField("password", "")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
        verify(userService, never()).register(any());
    }

    @Test
    void postRequestToRegisterEndpointWithAlreadyExistingUsername_shouldReturnRegisterViewWithErrorMessage() throws Exception {

        when(userService.register(any())).thenThrow(new UsernameAlreadyExistException("User with this username already exist."));

        MockHttpServletRequestBuilder request = post("/register")
                .formField("email", "email@gamil.com")
                .formField("username", "username")
                .formField("password", "123654")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("usernameAlreadyExistMessage"));
        verify(userService, never()).register(TestBuilder.aRandomRegisterRequest());
    }

    @Test
    void postRequestToRegisterEndpointWithAlreadyExistingEmail_shouldReturnRegisterViewWithErrorMessage() throws Exception {

        when(userService.register(any())).thenThrow(new EmailAlreadyExistException("Email with this username already exist."));

        MockHttpServletRequestBuilder request = post("/register")
                .formField("email", "email@gamil.com")
                .formField("username", "username")
                .formField("password", "123654")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("emailAlreadyExistMessage"));
        verify(userService, never()).register(TestBuilder.aRandomRegisterRequest());
    }
}
