package app.credit;

import app.credit.model.Credit;
import app.credit.model.CreditStatus;
import app.credit.repository.CreditRepository;
import app.credit.service.CreditService;
import app.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreditServiceTest {

    @Mock
    private CreditRepository creditRepository;

    @InjectMocks
    private CreditService creditService;

    @Test
    void givenHappyPath_whenCreateCredit() {

        User user = User.builder().build();
        LocalDate firstDayOfTheNextMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfNextMonth());

        Credit expectedCredit =  Credit.builder()
                .owner(user)
                .status(CreditStatus.PAYED)
                .amount(new BigDecimal(0))
                .payedOn(null)
                .nextPaymentOn(firstDayOfTheNextMonth)
                .build();

        Credit result = creditService.createCredit(user);

        assertEquals(result.getStatus(), expectedCredit.getStatus());
        assertEquals(result.getOwner(), expectedCredit.getOwner());
        assertEquals(result.getAmount(), expectedCredit.getAmount());
        assertEquals(result.getNextPaymentOn(), expectedCredit.getNextPaymentOn());
        verify(creditRepository, times(1)).save(result);
    }

    @Test
    void givenAmount_whenAddAmountToCredit_thenTheAmountIsAddedToTheUserCredit() {

        BigDecimal amount = new BigDecimal(20);
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        Credit credit =  Credit.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .status(CreditStatus.PAYED)
                .amount(new BigDecimal(0))
                .build();

        when(creditRepository.findByOwnerId(user.getId())).thenReturn(Optional.of(credit));

        creditService.addAmountToCredit(user, amount);

        assertEquals(credit.getAmount(), new BigDecimal(20));
        verify(creditRepository, times(1)).save(credit);
    }

    @Test
    void givenCreditIsUnpaid_whenPayCredit_thenTheCreditIsNullifiedAndTheStatusIsChanged() {


        LocalDate firstDayOfTheNextMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfNextMonth());
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        Credit credit =  Credit.builder()
                .owner(user)
                .status(CreditStatus.UNPAID)
                .amount(new BigDecimal(0))
                .nextPaymentOn(firstDayOfTheNextMonth)
                .build();

        when(creditRepository.findByOwnerId(user.getId())).thenReturn(Optional.of(credit));

        Credit result = creditService.payCredit(user);

        assertEquals(result.getAmount(), new BigDecimal(0));
        assertEquals(result.getStatus(), CreditStatus.PAYED);
        verify(creditRepository, times(1)).save(result);
    }

    @Test
    void givenHappyPath_whenChangeCreditStatusFromUnpaidToPayed() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        Credit credit =  Credit.builder()
                .owner(user)
                .status(CreditStatus.UNPAID)
                .build();

        when(creditRepository.findByOwnerId(user.getId())).thenReturn(Optional.of(credit));

        Credit result = creditService.changeCreditStatus(user);

        assertEquals(result.getStatus(), CreditStatus.PAYED);
        verify(creditRepository, times(1)).save(result);
    }

    @Test
    void givenHappyPath_whenChangeCreditStatusFromPayedToUnpaid() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        Credit credit =  Credit.builder()
                .owner(user)
                .status(CreditStatus.PAYED)
                .build();

        when(creditRepository.findByOwnerId(user.getId())).thenReturn(Optional.of(credit));

        Credit result = creditService.changeCreditStatus(user);

        assertEquals(result.getStatus(), CreditStatus.UNPAID);
        verify(creditRepository, times(1)).save(result);
    }

}
