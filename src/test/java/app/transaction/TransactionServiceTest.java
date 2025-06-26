package app.transaction;

import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.transaction.repository.TransactionRepository;
import app.transaction.service.TransactionService;
import app.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void givenHappyPath_whenGetTransaction() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.SUCCEEDED)
                .amount(new BigDecimal(10))
                .balanceLeft(new BigDecimal(150))
                .build();

        when(transactionRepository.findByIdAndOwnerId(transaction.getId(), user.getId())).thenReturn(Optional.of(transaction));

        Transaction result = transactionService.getTransaction(transaction.getId(), user.getId());

        assertEquals(result.getStatus(), transaction.getStatus());
        assertEquals(result.getType(), transaction.getType());
        assertEquals(result.getOwner(), user);
        assertEquals(result.getAmount(), transaction.getAmount());
        assertEquals(result.getBalanceLeft(), transaction.getBalanceLeft());
    }

    @Test
    void givenHappyPath_whenGetAllUserTransaction() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        Transaction firstTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.SUCCEEDED)
                .amount(new BigDecimal(10))
                .balanceLeft(new BigDecimal(150))
                .createdOn(LocalDateTime.of(2025, 01, 15, 06, 01))
                .build();

        Transaction secondTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCEEDED)
                .amount(new BigDecimal(10))
                .balanceLeft(new BigDecimal(150))
                .createdOn(LocalDateTime.of(2024, 06, 05, 01, 59))
                .build();

        List<Transaction> userTransactions = List.of(firstTransaction, secondTransaction);

        when(transactionRepository.findAllTransactionByOwnerId(user.getId())).thenReturn(userTransactions);

        List<Transaction> result = transactionService.getAllUserTransactions(user.getId());

       assertThat(result).hasSize(2);
    }
}
