package app.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionsReport {

    private long totalTransactions;
    private BigDecimal totalTransactionAmount;
    private long successfulTransactions;
    private long unsuccessfulTransactions;
    private LocalDateTime createdOn;
}
