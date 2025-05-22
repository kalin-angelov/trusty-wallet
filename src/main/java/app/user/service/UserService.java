package app.user.service;

import app.exception.UsernameAlreadyExistException;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import app.wallet.service.WalletService;
import app.web.dto.EditRequest;
import app.web.dto.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final WalletService walletService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, WalletService walletService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
    }

    @Transactional
    public User register(RegisterRequest registerRequest) {

        Optional<User> optionalUser = userRepository.findByUsername(registerRequest.getUsername());

        if (optionalUser.isPresent()) {
            log.info("User with username [%s] already exist.".formatted(registerRequest.getUsername()));
            throw new UsernameAlreadyExistException("User with username [%s] already exist.".formatted(registerRequest.getUsername()));
        }

        User user = initializeUser(registerRequest);
        Wallet wallet = walletService.createDefaultWallet(user);

        user.setWallets(List.of(wallet));
        userRepository.save(user);
        log.info("User with id [%s] and username [%s] created successfully".formatted(user.getId(), user.getUsername()));
        return user;
    }

    public User editUser (UUID id, EditRequest editRequest) {

        User user = userRepository.findById(id).orElseThrow();

        user.setFirstName(editRequest.getFirstName());
        user.setLastName(editRequest.getLastName());
        user.setEmail(editRequest.getEmail());
        user.setProfilePic(editRequest.getProfilePic());
        user.setUpdatedOn(LocalDateTime.now());

        userRepository.save(user);
        log.info("User with id [%s] and username [%s] successfully updated".formatted(user.getId(), user.getUsername()));

        return user;
    }

    private User initializeUser (RegisterRequest registerRequest) {

        return User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id).orElseThrow();
    }
}
