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
public class WalletsReport {

    private long totalWallets;
    private BigDecimal totalWalletsAmount;
    private long activeWallets;
    private long inactiveWallets;
    private LocalDateTime createdOn;
}
