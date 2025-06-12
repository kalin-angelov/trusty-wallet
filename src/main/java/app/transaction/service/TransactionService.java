package app.transaction.service;

import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.transaction.repository.TransactionRepository;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.web.dto.TransactionsReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
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

        List<Transaction> transactions = transactionRepository.findAllTransactionByOwnerId(id);

        if (!transactions.isEmpty()) {
            transactions.sort(Comparator.comparing(Transaction::getCreatedOn).reversed());
        }

        return transactions;
    }

    public Transaction getTransaction(UUID id, UUID ownerId) {
        return transactionRepository.findByIdAndOwnerId(id, ownerId).orElseThrow();
    }

    public Transaction initializeTransaction(User owner, String sender, String receiver, BigDecimal amount, BigDecimal balanceLeft, TransactionType type, String description, TransactionStatus status, String failureReason) {

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

    public TransactionsReport getTransactionsReport() {
        List<Transaction> allTransactions = transactionRepository.findAll();
        BigDecimal totalTransactionAmount = allTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long successfulTransactions = allTransactions.stream()
                .filter(transaction -> transaction.getStatus() == TransactionStatus.SUCCEEDED)
                .count();
        long unsuccessfulTransactions = allTransactions.stream()
                .filter(transaction -> transaction.getStatus() == TransactionStatus.FAILED)
                .count();

        return TransactionsReport.builder()
                .totalTransactions(allTransactions.size())
                .totalTransactionAmount(totalTransactionAmount)
                .successfulTransactions(successfulTransactions)
                .unsuccessfulTransactions(unsuccessfulTransactions)
                .createdOn(LocalDateTime.now())
                .build();
    }
}
