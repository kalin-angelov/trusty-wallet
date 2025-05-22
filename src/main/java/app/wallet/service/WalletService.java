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
import app.web.dto.TransferRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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
                .type(WalletType.DEFAULT)
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
    public Transaction chargeUpWallet(User owner, UUID walletId, BigDecimal amount) {

        Wallet wallet = walletRepository.findByIdAndOwnerId(walletId, owner.getId()).orElseThrow();
        String description = "Charging wallet - %.2f EUR".formatted(amount.doubleValue());

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedOn(LocalDateTime.now());
        walletRepository.save(wallet);

        return transactionService.initializeTransaction(
                wallet.getOwner(),
                SENDER,
                wallet.getOwner().getUsername(),
                amount,
                wallet.getBalance(),
                TransactionType.DEPOSIT,
                description,
                TransactionStatus.SUCCEEDED,
                null
        );
    }

    @Transactional
    public Transaction makeTransfer(User user,TransferRequest transferRequest) {

        Wallet senderWallet = walletRepository.findByIdAndOwnerId(transferRequest.getSender(), user.getId()).orElseThrow();
        Optional<Wallet> optionalReceiverWallet = walletRepository.findAllByOwnerUsername(transferRequest.getReceiver())
                .stream()
                .filter(wallet -> wallet.getStatus() == WalletStatus.ACTIVE)
                .findFirst();
        String descriptionOfTransaction = "Transfer From: %s To: %s".formatted(senderWallet.getOwner().getUsername(), transferRequest.getReceiver());
        boolean walletCanHandleTransaction = chargeWallet(senderWallet, transferRequest.getAmount());

        if (optionalReceiverWallet.isEmpty() || !walletCanHandleTransaction) {
            return transactionService.initializeTransaction(
                    senderWallet.getOwner(),
                    transferRequest.getReceiver(),
                    senderWallet.getOwner().getUsername(),
                    transferRequest.getAmount(),
                    senderWallet.getBalance(),
                    TransactionType.DEPOSIT,
                    descriptionOfTransaction,
                    TransactionStatus.FAILED,
                    "Invalid criteria for transaction"
            );
        }

        Wallet receiverWallet = optionalReceiverWallet.get();
        receiverWallet.setBalance(receiverWallet.getBalance().add(transferRequest.getAmount()));
        receiverWallet.setUpdatedOn(LocalDateTime.now());
        walletRepository.save(receiverWallet);

        senderWallet.setBalance(senderWallet.getBalance().subtract(transferRequest.getAmount()));
        senderWallet.setUpdatedOn(LocalDateTime.now());
        walletRepository.save(senderWallet);

        return transactionService.initializeTransaction(
                senderWallet.getOwner(),
                SENDER,
                senderWallet.getOwner().getUsername(),
                transferRequest.getAmount(),
                senderWallet.getBalance(),
                TransactionType.DEPOSIT,
                descriptionOfTransaction,
                TransactionStatus.SUCCEEDED,
                null
        );
    }

    public boolean chargeWallet (Wallet wallet, BigDecimal amount) {

        boolean canHandleTransaction = true;

        if (wallet.getStatus() == WalletStatus.INACTIVE || wallet.getBalance().compareTo(amount) < 0) {
            canHandleTransaction = false;
            return canHandleTransaction;
        }

        return canHandleTransaction;

    }
}
