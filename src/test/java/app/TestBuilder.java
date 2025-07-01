package app;

import app.credit.model.Credit;
import app.credit.model.CreditStatus;
import app.user.model.User;
import app.user.model.UserRole;
import app.web.dto.*;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@UtilityClass
public class TestBuilder {

    public static User aRandomUser () {

        return User.builder()
                .id(UUID.randomUUID())
                .email("randomEmail@gmail.com")
                .username("randomUsername")
                .password("randomPassword")
                .firstName("randomFirstName")
                .lastName("randomLastName")
                .profilePic("randomImgUrl")
                .role(UserRole.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public static User aRandomAdmin () {

        return User.builder()
                .id(UUID.randomUUID())
                .email("randomEmail@gmail.com")
                .username("randomUsername")
                .password("randomPassword")
                .firstName("randomFirstName")
                .lastName("randomLastName")
                .profilePic("randomImgUrl")
                .role(UserRole.ADMIN)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public static Credit aRandomCredit () {

        return Credit.builder()
                .id(UUID.randomUUID())
                .owner(aRandomUser())
                .status(CreditStatus.PAYED)
                .amount(new BigDecimal(0))
                .payedOn(null)
                .nextPaymentOn(LocalDate.now())
                .build();
    }

    public static RegisterRequest aRandomRegisterRequest() {

        return RegisterRequest.builder()
                .email("email@gmail.com")
                .username("username")
                .password("password")
                .build();

    }

    public static LoginRequest aRandomLoginRequest() {

        return LoginRequest.builder()
                .username("username")
                .password("password")
                .build();

    }

    public static EditRequest aRandomEditRequest() {

        return EditRequest.builder()
                .email("email@gmail.com")
                .firstName("firstName")
                .lastName("lastName")
                .profilePic("https://newImgUrl")
                .build();
    }

    public static UsersReport aRandomUserReport() {

        return UsersReport.builder()
                .totalUsers(6)
                .activeUser(4)
                .inactiveUsers(2)
                .admins(1)
                .nonAdmins(5)
                .createdOn(LocalDateTime.now())
                .build();
    }

    public static TransactionsReport aRandomTransactionsReport() {

        return TransactionsReport.builder()
                .totalTransactions(6)
                .totalTransactionAmount(new BigDecimal(100))
                .successfulTransactions(4)
                .unsuccessfulTransactions(2)
                .createdOn(LocalDateTime.now())
                .build();
    }

    public static WalletsReport aRandomWalletsReport() {

        return WalletsReport.builder()
                .totalWallets(18)
                .totalWalletsAmount(new BigDecimal(1500))
                .activeWallets(12)
                .inactiveWallets(6)
                .createdOn(LocalDateTime.now())
                .build();
    }
}
