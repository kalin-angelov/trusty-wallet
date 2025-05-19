package app.wallet.service;

import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.wallet.model.WalletType;
import app.wallet.repository.WalletRepository;
import app.web.dto.ChargeOwnWalletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class WalletService {

    private static final String SENDER = "Trusty Wallet";

    private final WalletRepository walletRepository;
    private final TransactionService transactionService;

    @Autowired
    public WalletService(WalletRepository walletRepository, TransactionService transactionService) {
        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
    }

    public Wallet createDefaultWallet(User user) {

        Wallet wallet = initializeWallet(user);
        walletRepository.save(wallet);

        log.info("Wallet it id [%s] successfully create for user with username [%s] and id [%s]".formatted(wallet.getId(), user.getUsername(), user.getId()));
        return wallet;
    }

    private Wallet initializeWallet (User user) {

        return Wallet.builder()
                .owner(user)
                .balance(new BigDecimal(10))
                .type(WalletType.PAYABLE)
                .status(WalletStatus.ACTIVE)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public Map<UUID, List<Transaction>> getLastTransactions(List<Wallet> wallets) {

        Map<UUID, List<Transaction>> userWalletsAllLastTransactions = new LinkedHashMap<>();

        for (Wallet wallet : wallets) {
            List<Transaction> lastTransactions = transactionService.getWalletLastTransaction(wallet);
            userWalletsAllLastTransactions.put(wallet.getId(), lastTransactions);
        }

        return userWalletsAllLastTransactions;
    }

    public Wallet changeStatus(UUID id, User user) {

        Wallet wallet = walletRepository.findByIdAndOwnerId(id, user.getId()).orElseThrow();

        if (wallet.getStatus() == WalletStatus.ACTIVE) {
            wallet.setStatus(WalletStatus.INACTIVE);
        } else {
            wallet.setStatus(WalletStatus.ACTIVE);
        }

        walletRepository.save(wallet);

        return wallet;
    }

    @Transactional
    public Transaction chargeUpWallet(UUID id, ChargeOwnWalletRequest chargeOwnWalletRequest) {

        Wallet wallet = walletRepository.findById(id).orElseThrow();
        String descriptionOfTransaction = "Charging wallet - [%.2f]".formatted(chargeOwnWalletRequest.getAmount().doubleValue());

        if (wallet.getStatus() == WalletStatus.INACTIVE) {
            return transactionService.createTransactionForChargingOwnWallet(
                    wallet.getOwner(),
                    SENDER,
                    wallet.getOwner().getUsername(),
                    chargeOwnWalletRequest.getAmount(),
                    wallet.getBalance(),
                    TransactionType.DEPOSIT,
                    descriptionOfTransaction,
                    TransactionStatus.FAILED,
                    "Wallet is invalid"

            );
        }

        wallet.setBalance(wallet.getBalance().add(chargeOwnWalletRequest.getAmount()));
        walletRepository.save(wallet);

        return transactionService.createTransactionForChargingOwnWallet(
                wallet.getOwner(),
                SENDER,
                wallet.getOwner().getUsername(),
                chargeOwnWalletRequest.getAmount(),
                wallet.getBalance(),
                TransactionType.DEPOSIT,
                descriptionOfTransaction,
                TransactionStatus.SUCCEEDED,
                null
        );
    }
}
