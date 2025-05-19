package app.transaction.service;

import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.transaction.repository.TransactionRepository;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> getWalletLastTransaction (Wallet wallet) {

        return transactionRepository.findAllBySenderOrReceiver(wallet.getId().toString(), wallet.getId().toString())
                .stream()
                .filter(transaction -> transaction.getOwner().getId() == wallet.getOwner().getId())
                .limit(3)
                .collect(Collectors.toList());
    }

    public List<Transaction> allUserTransactions (UUID id) {

        return transactionRepository.findAllTransactionByOwnerId(id);
    }

    public Transaction getTransaction(UUID id, UUID ownerId) {
        return transactionRepository.findByIdAndOwnerId(id, ownerId).orElseThrow();
    }

    public Transaction createTransactionForChargingOwnWallet(User owner, String sender, String receiver, BigDecimal amount, BigDecimal balanceLeft, TransactionType type, String description, TransactionStatus status, String failureReason) {

        Transaction transaction = Transaction.builder()
                .owner(owner)
                .sender(sender)
                .receiver(receiver)
                .amount(amount)
                .balanceLeft(balanceLeft)
                .type(type)
                .description(description)
                .status(status)
                .failureReason(failureReason)
                .createdOn(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        return transaction;
    }
}
