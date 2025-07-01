package app.web;

import app.TestBuilder;
import app.user.model.User;
import app.user.model.UserPrinciple;
import app.user.service.UserService;
import app.web.dto.EditRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.instanceOf;
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

    @Test
    void getUsersRequestEndpointWithAuthorizedRequest_shouldReturnUsersView() throws Exception {

        UserPrinciple principle = new UserPrinciple(TestBuilder.aRandomAdmin());
        UUID userId = principle.getUser().getId();

        List<User> allUsers = List.of(TestBuilder.aRandomUser(), TestBuilder.aRandomUser(), TestBuilder.aRandomAdmin());

        when(userService.getUserById(userId)).thenReturn(principle.getUser());
        when(userService.getAllUsers()).thenReturn(allUsers);

        MockHttpServletRequestBuilder request = get("/users")
                .with(user(principle));

        mockMvc.perform(request)
                .andExpect(status().is(200))
                .andExpect(view().name("users"))
                .andExpect(model().attribute("user", instanceOf(User.class)))
                .andExpect(model().attribute("users", instanceOf(List.class)));
        verify(userService, times(1)).getUserById(userId);
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUsersRequestEndpointWithUnauthorizedRequest_shouldRedirectToNotFoundView() throws Exception {

        UserPrinciple principle = new UserPrinciple(TestBuilder.aRandomUser());
        UUID userId = principle.getUser().getId();

        MockHttpServletRequestBuilder request = get("/users")
                .with(user(principle));

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/not-found"));
        verify(userService, never()).getUserById(userId);
        verify(userService, never()).getAllUsers();
    }

    @Test
    void getProfileRequest_shouldReturnProfileView() throws Exception {

        UserPrinciple principle = new UserPrinciple(TestBuilder.aRandomUser());
        UUID userId = principle.getUser().getId();

        when(userService.getUserById(userId)).thenReturn(principle.getUser());

        MockHttpServletRequestBuilder request = get("/users/{id}/profile", userId)
                .with(user(principle));

        mockMvc.perform(request)
                .andExpect(status().is(200))
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("user", instanceOf(User.class)))
                .andExpect(model().attribute("editRequest", instanceOf(EditRequest.class)));
        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void putEditProfileRequest_shouldReturnProfileViewWithTheNewEdits() throws Exception {

        UserPrinciple principle = new UserPrinciple(TestBuilder.aRandomUser());
        UUID userId = principle.getUser().getId();

        EditRequest editRequest = TestBuilder.aRandomEditRequest();

        when(userService.getUserById(userId)).thenReturn(principle.getUser());
        when(userService.editUser(userId, editRequest)).thenReturn(any());

        MockHttpServletRequestBuilder request = put("/users/{id}/profile", userId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("firstName", editRequest.getFirstName())
                .param("lastName", editRequest.getLastName())
                .param("email", editRequest.getEmail())
                .param("profilePic", editRequest.getProfilePic())
                .with(user(principle))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is(200))
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("user", instanceOf(User.class)))
                .andExpect(model().attribute("editRequest", instanceOf(EditRequest.class)));

        verify(userService, times(1)).getUserById(userId);
        verify(userService, times(1)).editUser(userId, editRequest);
    }

    @Test
    void putEditProfileRequestWithInvalidInput_shouldReturnProfileViewWithErrorMessage() throws Exception {

        UserPrinciple principle = new UserPrinciple(TestBuilder.aRandomUser());
        UUID userId = principle.getUser().getId();

        EditRequest editRequest = EditRequest.builder()
                .profilePic("newPic")
                .build();

        when(userService.getUserById(userId)).thenReturn(principle.getUser());

        MockHttpServletRequestBuilder request = put("/users/{id}/profile", userId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("profilePic", editRequest.getProfilePic())
                .with(user(principle))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is(200))
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("user", instanceOf(User.class)))
                .andExpect(model().attribute("editRequest", instanceOf(EditRequest.class)));

        verify(userService, times(1)).getUserById(userId);
        verify(userService, never()).editUser(userId, editRequest);
    }
}
