package app.web;

import app.transaction.model.Transaction;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.user.model.UserPrinciple;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final UserService userService;
    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getTransactionsPage (@AuthenticationPrincipal UserPrinciple userPrinciple) {

        User user = userService.getUserById(userPrinciple.getUser().getId());
        List<Transaction> transactions = transactionService.getAllUserTransactions(user.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("transactions");
        modelAndView.addObject("user", user);
        modelAndView.addObject("transactions", transactions);

        return modelAndView;
    }

    @GetMapping("/{id}")
    public ModelAndView getTransactionResultPage(@PathVariable UUID id, @AuthenticationPrincipal UserPrinciple userPrinciple) {

        User user = userService.getUserById(userPrinciple.getUser().getId());
        Transaction transaction = transactionService.getTransaction(id, user.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("transaction", transaction);
        modelAndView.setViewName("transaction-result");
        return modelAndView;
    }
}
