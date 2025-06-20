package app.user;

import app.credit.service.CreditService;
import app.exception.EmailAlreadyExistException;
import app.exception.UsernameAlreadyExistException;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.wallet.model.WalletType;
import app.wallet.service.WalletService;
import app.web.dto.EditRequest;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private  UserRepository userRepository;

    @Mock
    private  PasswordEncoder passwordEncoder;

    @Mock
    private  WalletService walletService;

    @Mock
    private  CreditService creditService;

    @InjectMocks
    private UserService userService;

    @Test
    void givenExistingUsername_whenRegister_thenExceptionIsThrown() {

        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("test@gmail.com")
                .username("test")
                .password("test")
                .build();

        User existingUser = User.builder()
                .username("test")
                .build();

        User newUser = User.builder().build();

        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.of(existingUser));

        assertThrows(UsernameAlreadyExistException.class, () -> userService.register(registerRequest));
        verify(userRepository, never()).save(newUser);
    }

    @Test
    void givenExistingEmail_whenRegister_thenExceptionIsThrown() {

        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("test@gmail.com")
                .username("test")
                .password("test")
                .build();


        User existingUser = User.builder()
                .email("test@gmail.com")
                .build();

        User newUser = User.builder().build();

        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(existingUser));

        assertThrows(EmailAlreadyExistException.class, () -> userService.register(registerRequest));
        verify(userRepository, never()).save(newUser);
    }

    @Test
    void givenExistingEmail_whenEditUser_thenExceptionIsThrown() {

        UUID userId = UUID.randomUUID();
        EditRequest editRequest = EditRequest.builder()
                .email("email@gmail.com")
                .build();

        User existingUser = User.builder()
                .email("email@gmail.com")
                .build();

        User user = User.builder().build();
        when(userRepository.findByEmail(editRequest.getEmail())).thenReturn(Optional.of(existingUser));

        assertThrows(EmailAlreadyExistException.class, () -> userService.editUser(userId, editRequest));
        verify(userRepository, never()).save(user);
    }
}
