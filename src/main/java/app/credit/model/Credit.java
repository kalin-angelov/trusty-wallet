package app.credit.model;

import app.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Credit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    private User owner;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CreditStatus status;

    @Column(nullable = false)
    private BigDecimal amount;

    private LocalDateTime payedOn;

    private LocalDate nextPaymentOn;
}
