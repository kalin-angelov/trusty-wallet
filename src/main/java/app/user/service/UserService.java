package app.user.service;

import app.credit.model.Credit;
import app.credit.service.CreditService;
import app.exception.EmailAlreadyExistException;
import app.exception.UsernameAlreadyExistException;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import app.web.dto.EditRequest;
import app.web.dto.RegisterRequest;
import app.web.dto.UsersReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;
    private final CreditService creditService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, WalletService walletService, CreditService creditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
        this.creditService = creditService;
    }

    @Cacheable("users")
    public List<User> getAllUsers() {
        return userRepository.findAll().stream().sorted(Comparator.comparing(User::getCreatedOn)).toList();
    }

    public List<User> getAllActiveUsers() {
        return this.getAllUsers().stream().filter(User::isActive).toList();
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public User register(RegisterRequest registerRequest) {

        Optional<User> optionalUserByUsername = userRepository.findByUsername(registerRequest.getUsername());
        Optional<User> optionalUserByEmail = userRepository.findByEmail(registerRequest.getEmail());

        if (optionalUserByEmail.isPresent()) {
            log.info("User with email [%s] already exist.".formatted(registerRequest.getEmail()));
            throw new EmailAlreadyExistException("User with this email already exist.");
        }

        if (optionalUserByUsername.isPresent()) {
            log.info("User with username [%s] already exist.".formatted(registerRequest.getUsername()));
            throw new UsernameAlreadyExistException("User with this username already exist.");
        }

        User user = initializeUser(registerRequest);
        List<Wallet> userWallets = walletService.createUserWallets(user);
        Credit userCredit = creditService.createCredit(user);

        user.setCredit(userCredit);
        user.setWallets(userWallets);
        userRepository.save(user);
        log.info("User with id [%s] and username [%s] created successfully".formatted(user.getId(), user.getUsername()));
        return user;
    }

    @CacheEvict(value = "users", allEntries = true)
    public User editUser (UUID id, EditRequest editRequest) {

        Optional<User> optionalEmail = userRepository.findByEmail(editRequest.getEmail());

        if (optionalEmail.isPresent()) {
            log.info("User with email [%s] already exist.".formatted(editRequest.getEmail()));
            throw new EmailAlreadyExistException("User with this email already exist.");
        }

        User user = userRepository.findById(id).orElseThrow();

        if ((user.getFirstName() == null && !editRequest.getFirstName().isEmpty()) || (user.getFirstName() != null && !editRequest.getFirstName().isEmpty())) {
            user.setFirstName(editRequest.getFirstName());
        }

        if ((user.getLastName() == null && !editRequest.getLastName().isEmpty()) || (user.getLastName() != null && !editRequest.getLastName().isEmpty())) {
            user.setLastName(editRequest.getLastName());
        }

        if (!editRequest.getEmail().isEmpty()) {
            user.setEmail(editRequest.getEmail());
        }

        if ((user.getProfilePic() == null && !editRequest.getProfilePic().isEmpty()) || (user.getProfilePic() != null && !editRequest.getProfilePic().isEmpty())) {
            user.setProfilePic(editRequest.getProfilePic());
        }

        user.setUpdatedOn(LocalDateTime.now());

        userRepository.save(user);
        log.info("User with id [%s] and username [%s] successfully updated".formatted(user.getId(), user.getUsername()));

        return user;
    }

    private User initializeUser (RegisterRequest registerRequest) {

        return User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .role(UserRole.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User with id [%s] do not exist.".formatted(id)));
    }

    @CacheEvict(value = "users", allEntries = true)
    public void changeUserStatus(UUID id) {

        User user = getUserById(id);

        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    public UsersReport getUsersReport() {

        List<User> allUsers = userRepository.findAll();
        long activeUsers = allUsers.stream().filter(User::isActive).count();
        long inactiveUsers = allUsers.stream().filter(user -> !user.isActive()).count();
        long adminUsers = allUsers.stream().filter(user -> user.getRole() == UserRole.ADMIN).count();
        long nonAdmins = allUsers.stream().filter(user -> user.getRole() == UserRole.USER).count();

        return UsersReport.builder()
                .totalUsers(allUsers.size())
                .activeUser(activeUsers)
                .inactiveUsers(inactiveUsers)
                .admins(adminUsers)
                .nonAdmins(nonAdmins)
                .createdOn(LocalDateTime.now())
                .build();
    }

    public List<User> getAllUsersWithUnpaidCredit() {

        LocalDate date = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        return getAllActiveUsers().stream().filter(user ->
                user.getCredit().getNextPaymentOn().isEqual(date)).toList();
    }
}
