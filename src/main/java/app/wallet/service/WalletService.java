package app.wallet.service;

import app.credit.service.CreditService;
import app.exception.WalletDoNotExistException;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.transaction.model.TransactionTypeStatus;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.wallet.model.WalletType;
import app.wallet.repository.WalletRepository;
import app.web.dto.TransferRequest;
import app.web.dto.WalletsReport;
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
    private final CreditService creditService;

    @Autowired
    public WalletService(WalletRepository walletRepository, TransactionService transactionService, CreditService creditService) {
        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
        this.creditService = creditService;
    }

    public List<Wallet> createUserWallets(User user) {

        Wallet defaultWallet = initializeWallet(user, new BigDecimal(10), WalletType.DEFAULT, WalletStatus.ACTIVE);
        Wallet savingWallet = initializeWallet(user, new BigDecimal(0), WalletType.SAVING, WalletStatus.INACTIVE);
        Wallet payableWallet = initializeWallet(user, new BigDecimal(0), WalletType.PAYABLE, WalletStatus.INACTIVE);
        List<Wallet> userWallets = new ArrayList<>(List.of(defaultWallet, savingWallet, payableWallet));
        walletRepository.saveAll(userWallets);

        log.info("Default Wallet with id [%s], Saving Wallet with id [%s], Payable Wallet with id [%s] successfully create for user with username [%s] and id [%s]".formatted(defaultWallet.getId(), savingWallet.getId(), payableWallet.getId(), user.getUsername(), user.getId()));
        return userWallets;
    }

    private Wallet initializeWallet (User user, BigDecimal amount, WalletType type, WalletStatus status) {

        return Wallet.builder()
                .owner(user)
                .balance(amount)
                .type(type)
                .status(status)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public Wallet changeStatus(UUID id, User user) {

        Wallet wallet = walletRepository.findByIdAndOwnerId(id, user.getId()).orElseThrow( () -> new RuntimeException("Wallet with id [%s] and owner [%s] do not exist.".formatted(id, user)));

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

        Wallet wallet = walletRepository.findByIdAndOwnerId(walletId, owner.getId()).orElseThrow(() ->new WalletDoNotExistException("Wallet with id [%s] do not exist in database.".formatted(walletId)));
        String description = "Charging wallet - %.2f EUR.".formatted(amount.doubleValue());

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedOn(LocalDateTime.now());
        walletRepository.save(wallet);

        creditService.addAmountToCredit(owner, amount);

        return transactionService.initializeTransaction(
                wallet.getOwner(),
                SENDER,
                wallet.getOwner().getUsername(),
                amount,
                wallet.getBalance(),
                TransactionType.DEPOSIT,
                description,
                TransactionStatus.SUCCEEDED,
                TransactionTypeStatus.MAIN,
                null
        );
    }

    @Transactional
    public Transaction makeTransfer(User user,TransferRequest transferRequest) {

        Wallet senderWallet = walletRepository.findByIdAndOwnerId(transferRequest.getSender(), user.getId()).orElseThrow(() ->new WalletDoNotExistException("Wallet with id [%s] do not exist in database.".formatted(transferRequest.getSender())));
        Optional<Wallet> optionalReceiverWallet = walletRepository.findAllByOwnerUsername(transferRequest.getReceiver())
                .stream()
                .filter(wallet -> wallet.getStatus() == WalletStatus.ACTIVE)
                .findFirst();
        String descriptionOfTransaction = "Transferring currency from [%s] to [%s]".formatted(senderWallet.getOwner().getUsername(), transferRequest.getReceiver());
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
                    TransactionTypeStatus.MAIN,
                    "Invalid criteria for transaction"
            );
        }

        Wallet receiverWallet = optionalReceiverWallet.get();
        receiverWallet.setBalance(receiverWallet.getBalance().add(transferRequest.getAmount()));
        receiverWallet.setUpdatedOn(LocalDateTime.now());
        walletRepository.save(receiverWallet);

        transactionService.initializeTransaction(
                receiverWallet.getOwner(),
                senderWallet.getOwner().getUsername(),
                receiverWallet.getOwner().getUsername(),
                transferRequest.getAmount(),
                receiverWallet.getBalance(),
                TransactionType.DEPOSIT,
                descriptionOfTransaction,
                TransactionStatus.SUCCEEDED,
                TransactionTypeStatus.SECONDARY,
                null
        );

        senderWallet.setBalance(senderWallet.getBalance().subtract(transferRequest.getAmount()));
        senderWallet.setUpdatedOn(LocalDateTime.now());
        walletRepository.save(senderWallet);

        return transactionService.initializeTransaction(
                senderWallet.getOwner(),
                senderWallet.getOwner().getUsername(),
                receiverWallet.getOwner().getUsername(),
                transferRequest.getAmount(),
                senderWallet.getBalance(),
                TransactionType.DEPOSIT,
                descriptionOfTransaction,
                TransactionStatus.SUCCEEDED,
                TransactionTypeStatus.MAIN,
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

    public WalletsReport getWalletsReport() {

        List<Wallet> allWallets = walletRepository.findAll();
        BigDecimal totalWalletsAmount = allWallets.stream()
                .map(Wallet::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long activeWallets = allWallets.stream().filter(wallet -> wallet.getStatus() == WalletStatus.ACTIVE).count();
        long inactiveWallets = allWallets.stream().filter(wallet -> wallet.getStatus() == WalletStatus.INACTIVE).count();

        return WalletsReport.builder()
                .totalWallets(allWallets.size())
                .totalWalletsAmount(totalWalletsAmount)
                .activeWallets(activeWallets)
                .inactiveWallets(inactiveWallets)
                .createdOn(LocalDateTime.now())
                .build();
    }
}
