package app.web;

import app.credit.model.Credit;
import app.credit.service.CreditService;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.user.model.UserPrinciple;
import app.user.service.UserService;
import app.wallet.service.WalletService;
import app.web.dto.TransactionsReport;
import app.web.dto.UsersReport;
import app.web.dto.WalletsReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {

    private final UserService userService;
    private final CreditService creditService;
    private final WalletService walletService;
    private final TransactionService transactionService;

    @Autowired
    public IndexController(UserService userService, CreditService creditService, WalletService walletService, TransactionService transactionService) {
        this.userService = userService;
        this.creditService = creditService;
        this.walletService = walletService;
        this.transactionService = transactionService;
    }

    @GetMapping("/")
    public ModelAndView getIndexPage() {

        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("index");

        return modelAndView;
    }

    @GetMapping("/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal UserPrinciple userPrinciple) {

        User user = userService.getUserById(userPrinciple.getUser().getId());
        Credit credit = creditService.getCreditByOwnerId(user.getId());
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("home");
        modelAndView.addObject("user", user);
        modelAndView.addObject("credit", credit);

        return modelAndView;
    }

    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getReports(@AuthenticationPrincipal UserPrinciple userPrinciple) {

        User user = userService.getUserById(userPrinciple.getUser().getId());
        ModelAndView modelAndView = new ModelAndView();

        UsersReport usersReport = userService.getUsersReport();
        WalletsReport walletsReport = walletService.getWalletsReport();
        TransactionsReport transactionsReport = transactionService.getTransactionsReport();

        modelAndView.setViewName("reports");
        modelAndView.addObject("user", user);
        modelAndView.addObject("usersReport", usersReport);
        modelAndView.addObject("walletsReport", walletsReport);
        modelAndView.addObject("transactionsReport", transactionsReport);

        return modelAndView;
    }
}
