package app.user;

import app.user.model.User;
import app.user.repository.UserRepository;
import app.user.service.MyUserDetailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MyUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MyUserDetailService myUserDetailService;

    @Test
    void givenUnExistingUser_whenLoadUserByUsername_thenExceptionIsThrown() {

        String username = "username";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> myUserDetailService.loadUserByUsername(username));
    }

    @Test
    void givenHappyPath_whenLoadUserByUsername() {

        User user = User.builder()
                .username("username")
                .password("1234")
                .build();

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        UserDetails userDetails = myUserDetailService.loadUserByUsername(user.getUsername());

        assertNotNull(userDetails);
        assertEquals(user.getUsername(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
    }
}
