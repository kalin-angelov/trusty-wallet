package app.wallet.service;

import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.wallet.model.WalletType;
import app.wallet.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class WalletService {

    private final WalletRepository walletRepository;

    @Autowired
    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Wallet createDefaultWallet(User user) {

        Wallet wallet = initializeWallet(user);

        log.info("Wallet it id [%s] successfully create for user with username [%s] and id [%s]".formatted(wallet.getId(), user.getUsername(), user.getId()));
        return wallet;
    }

    private Wallet initializeWallet (User user) {

        return Wallet.builder()
                .owner(user)
                .balance(new BigDecimal(0))
                .type(WalletType.PAYABLE)
                .status(WalletStatus.ACTIVE)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }
}
