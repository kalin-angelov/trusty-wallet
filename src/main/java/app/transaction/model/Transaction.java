package app.transaction.model;

import app.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private User owner;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false)
    private String receiver;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal balanceLeft;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TransactionStatus status;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdOn;
}
