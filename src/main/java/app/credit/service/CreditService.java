package app.credit.service;

import app.credit.model.Credit;
import app.credit.model.CreditStatus;
import app.credit.repository.CreditRepository;
import app.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

@Service
public class CreditService {

    private final CreditRepository creditRepository;

    @Autowired
    public CreditService(CreditRepository creditRepository) {
        this.creditRepository = creditRepository;
    }

    public Credit createCredit (User user) {

        LocalDate firstDayOfTheNextMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfNextMonth());

        Credit credit =  Credit.builder()
                .owner(user)
                .status(CreditStatus.PAYED)
                .amount(new BigDecimal(0))
                .payedOn(null)
                .nextPaymentOn(firstDayOfTheNextMonth)
                .build();

        creditRepository.save(credit);
        return credit;
    }

    public void addAmountToCredit(User owner, BigDecimal amount) {

        Credit credit = getCreditByOwnerId(owner.getId());
        credit.setAmount(credit.getAmount().add(amount));
        creditRepository.save(credit);
    }

    public Credit payCredit(User user) {
        Credit credit = getCreditByOwnerId(user.getId());
        credit.setAmount(new BigDecimal(0));
        creditRepository.save(credit);

        return credit;
    }

    public Credit getCreditByOwnerId(UUID id) {
        return creditRepository.findByOwnerId(id).orElseThrow(() -> new IllegalArgumentException("Credit with owner id [%s] do not exist.".formatted(id)));
    }
}
