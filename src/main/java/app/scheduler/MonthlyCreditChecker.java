package app.scheduler;

import app.credit.service.CreditService;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MonthlyCreditChecker {

    @Autowired
    private UserService userService;

    @Autowired
    private CreditService creditService;

    @Scheduled(cron = "0 0 0 1 * *")
    public void stopUserActivity() {

        List<User> allUsersWithUnpaidCredit = userService.getAllUsersWithUnpaidCredit();

        for( User user : allUsersWithUnpaidCredit ) {

            if (!user.getCredit().getAmount().equals(new BigDecimal(0))) {
                userService.changeUserStatus(user.getId());
                creditService.changeCreditStatus(user);
            }

        }

    }
}
