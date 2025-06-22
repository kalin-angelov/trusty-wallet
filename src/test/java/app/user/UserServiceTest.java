package app.user;

import app.credit.service.CreditService;
import app.exception.EmailAlreadyExistException;
import app.exception.UsernameAlreadyExistException;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import app.web.dto.EditRequest;
import app.web.dto.RegisterRequest;
import app.web.dto.UsersReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
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
    void givenHappyPath_whenRegister() {

        UUID userId = UUID.randomUUID();
        String hashedPassword = "hash-password1234";
        List<Wallet> userWallets = List.of(new Wallet(), new Wallet(), new Wallet());
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("newUser")
                .email("newUserEmail@gmail.com")
                .password("password")
                .build();

        User newUser = User.builder()
                .id(userId)
                .email(registerRequest.getEmail())
                .username(registerRequest.getUsername())
                .password(hashedPassword)
                .role(UserRole.USER)
                .isActive(true)
                .wallets(userWallets)
                .build();

        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(hashedPassword);

        User userResult = userService.register(registerRequest);

        assertEquals(newUser.getUsername(), userResult.getUsername());
        assertEquals(newUser.getEmail(), userResult.getEmail());
        assertEquals(newUser.getRole(), userResult.getRole());
        assertEquals(newUser.isActive(), userResult.isActive());
        assertThat(newUser.getWallets()).hasSize(3);
        verify(userRepository, times(1)).save(userResult);
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

    @Test
    void givenEmptyFirstName_whenEditUser_thenTheFirstNameIsNotChanged() {

        UUID userId = UUID.randomUUID();
        EditRequest editRequest = EditRequest.builder()
                .firstName("")
                .lastName("newLastName")
                .email("newEmail")
                .profilePic("newProfilePic")
                .build();

        User user = User.builder()
                .id(userId)
                .username("user")
                .email("oldEmail")
                .firstName("oldFirstName")
                .lastName("oldLastName")
                .profilePic("oldProfilePic")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(editRequest.getEmail())).thenReturn(Optional.empty());

        User editUser = userService.editUser(userId, editRequest);

        assertEquals(editUser.getFirstName(), user.getFirstName());
        assertEquals(editUser.getLastName(), editRequest.getLastName());
        assertEquals(editUser.getEmail(), editRequest.getEmail());
        assertEquals(editUser.getProfilePic(), editRequest.getProfilePic());
        verify(userRepository, times(1)).save(editUser);
    }

    @Test
    void givenEmptyLastName_whenEditUser_thenTheLastNameIsNotChanged() {

        UUID userId = UUID.randomUUID();
        EditRequest editRequest = EditRequest.builder()
                .firstName("newFirstName")
                .lastName("")
                .email("newEmail")
                .profilePic("newProfilePic")
                .build();

        User user = User.builder()
                .id(userId)
                .username("user")
                .email("oldEmail")
                .firstName("oldFirstName")
                .lastName("oldLastName")
                .profilePic("oldProfilePic")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(editRequest.getEmail())).thenReturn(Optional.empty());

        User editUser = userService.editUser(userId, editRequest);

        assertEquals(editUser.getFirstName(), editRequest.getFirstName());
        assertEquals(editUser.getLastName(), user.getLastName());
        assertEquals(editUser.getEmail(), editRequest.getEmail());
        assertEquals(editUser.getProfilePic(), editRequest.getProfilePic());
        verify(userRepository, times(1)).save(editUser);
    }

    @Test
    void givenEmptyProfilePic_whenEditUser_thenTheProfilePicIsNotChanged() {

        UUID userId = UUID.randomUUID();
        EditRequest editRequest = EditRequest.builder()
                .firstName("newFirstName")
                .lastName("newLastName")
                .email("newEmail")
                .profilePic("")
                .build();

        User user = User.builder()
                .id(userId)
                .username("user")
                .email("oldEmail")
                .firstName("oldFirstName")
                .lastName("oldLastName")
                .profilePic("oldProfilePic")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(editRequest.getEmail())).thenReturn(Optional.empty());

        User editUser = userService.editUser(userId, editRequest);

        assertEquals(editUser.getFirstName(), editRequest.getFirstName());
        assertEquals(editUser.getLastName(), editRequest.getLastName());
        assertEquals(editUser.getEmail(), editRequest.getEmail());
        assertEquals(editUser.getProfilePic(), user.getProfilePic());
        verify(userRepository, times(1)).save(editUser);
    }

    @Test
    void givenEmptyEmail_whenEditUser_thenTheEmailIsNotChanged() {

        UUID userId = UUID.randomUUID();
        EditRequest editRequest = EditRequest.builder()
                .firstName("newFirstName")
                .lastName("newLastName")
                .email("")
                .profilePic("newProfilePic")
                .build();

        User user = User.builder()
                .id(userId)
                .username("user")
                .email("oldEmail")
                .firstName("oldFirstName")
                .lastName("oldLastName")
                .profilePic("oldProfilePic")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(editRequest.getEmail())).thenReturn(Optional.empty());

        User editUser = userService.editUser(userId, editRequest);

        assertEquals(editUser.getFirstName(), editRequest.getFirstName());
        assertEquals(editUser.getLastName(), editRequest.getLastName());
        assertEquals(editUser.getEmail(), user.getEmail());
        assertEquals(editUser.getProfilePic(), editRequest.getProfilePic());
        verify(userRepository, times(1)).save(editUser);
    }

    @Test
    void givenHappyPath_whenEditUser() {

        UUID userId = UUID.randomUUID();
        EditRequest editRequest = EditRequest.builder()
                .firstName("newFirstName")
                .lastName("newLastName")
                .email("newEmail")
                .profilePic("newProfilePic")
                .build();

        User user = User.builder()
                .id(userId)
                .username("user")
                .email("oldEmail")
                .firstName("oldFirstName")
                .lastName("oldLastName")
                .profilePic("oldProfilePic")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(editRequest.getEmail())).thenReturn(Optional.empty());

        User editUser = userService.editUser(userId, editRequest);

        assertEquals(editUser.getFirstName(), editRequest.getFirstName());
        assertEquals(editUser.getLastName(), editRequest.getLastName());
        assertEquals(editUser.getEmail(), editRequest.getEmail());
        assertEquals(editUser.getProfilePic(), editRequest.getProfilePic());
        verify(userRepository, times(1)).save(editUser);
    }

    @Test
    void givenUnExistingUserId_whenGetUserById_thenExceptionIsThrown() {

        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserById(userId));
    }

    @Test
    void givenHappyPath_whenGetUserById() {

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("email")
                .username("username")
                .password("hashed-password123")
                .isActive(true)
                .role(UserRole.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User findUser = userService.getUserById(userId);

        assertEquals(findUser.getId(), user.getId());
        assertEquals(findUser.getUsername(), user.getUsername());
        assertEquals(findUser.getEmail(), user.getEmail());
        assertEquals(findUser.isActive(), user.isActive());
        assertEquals(findUser.getRole(), user.getRole());
    }

    @Test
    void givenUserIsActive_whenChangeUserStatus_thenUserStatusIsChangedToInactive() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .isActive(true)
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.changeUserStatus(user.getId());

        assertFalse(user.isActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenUserIsInactive_whenChangeUserStatus_thenUserStatusIsChangedToActive() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .isActive(false)
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.changeUserStatus(user.getId());

        assertTrue(user.isActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenHappyPath_whenGetUsersReport() {

        User firstUser = User.builder()
                .id(UUID.randomUUID())
                .email("firstUserEmail")
                .username("firstUserUsername")
                .password("hashed-password123")
                .isActive(true)
                .role(UserRole.ADMIN)
                .build();

        User secondUser = User.builder()
                .id(UUID.randomUUID())
                .email("secondUserEmail")
                .username("secondUserUsername")
                .password("hashed-password123")
                .isActive(false)
                .role(UserRole.USER)
                .build();

        User thirdUser = User.builder()
                .id(UUID.randomUUID())
                .email("thirdUserEmail")
                .username("thirdUserUsername")
                .password("hashed-password123")
                .isActive(true)
                .role(UserRole.USER)
                .build();

        List<User> users = List.of(firstUser, secondUser, thirdUser);

        when(userRepository.findAll()).thenReturn(users);

        UsersReport usersReport = userService.getUsersReport();

        assertEquals(usersReport.getTotalUsers(), 3);
        assertEquals(usersReport.getActiveUser(), 2);
        assertEquals(usersReport.getInactiveUsers(), 1);
        assertEquals(usersReport.getAdmins(), 1);
        assertEquals(usersReport.getNonAdmins(), 2);
    }

    @Test
    void givenHappyPath_whenGetAllUsers() {

        User firstUser = User.builder()
                .id(UUID.randomUUID())
                .email("firstUserEmail")
                .username("firstUserUsername")
                .password("hashed-password123")
                .isActive(true)
                .role(UserRole.ADMIN)
                .createdOn(LocalDateTime.of(2025, 01, 25, 00, 00))
                .build();

        User secondUser = User.builder()
                .id(UUID.randomUUID())
                .email("secondUserEmail")
                .username("secondUserUsername")
                .password("hashed-password123")
                .isActive(false)
                .role(UserRole.USER)
                .createdOn(LocalDateTime.of(2025, 02, 22, 00, 00))
                .build();

        User thirdUser = User.builder()
                .id(UUID.randomUUID())
                .email("thirdUserEmail")
                .username("thirdUserUsername")
                .password("hashed-password123")
                .isActive(true)
                .role(UserRole.USER)
                .createdOn(LocalDateTime.of(2025, 03, 06, 00, 00))
                .build();

        List<User> users = List.of(firstUser, secondUser, thirdUser);

        when(userRepository.findAll()).thenReturn(users);

        List<User> usersResult = userService.getAllUsers();

        assertThat(usersResult).hasSize(3);
    }
}
