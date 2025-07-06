package app.web;

import app.credit.service.CreditService;
import app.transaction.model.Transaction;
import app.user.model.User;
import app.user.model.UserPrinciple;
import app.user.service.UserService;
import app.wallet.service.WalletService;
import app.web.dto.CreditRequest;
import app.web.dto.TransferRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/transfers")
public class TransferController {

    private final UserService userService;
    private final WalletService walletService;
    private final CreditService creditService;

    @Autowired
    public TransferController(UserService userService, WalletService walletService, CreditService creditService) {
        this.userService = userService;
        this.walletService = walletService;
        this.creditService = creditService;
    }

    @GetMapping
    public ModelAndView getTransferPage(@AuthenticationPrincipal UserPrinciple userPrinciple) {

        User user = userService.getUserById(userPrinciple.getUser().getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("transfer");
        modelAndView.addObject("transferRequest", new TransferRequest());
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @PostMapping
    public ModelAndView createTransfer(@Valid TransferRequest transferRequest, BindingResult bindingResult, @AuthenticationPrincipal UserPrinciple userPrinciple) {

        User user = userService.getUserById(userPrinciple.getUser().getId());
        Transaction transaction = walletService.makeTransfer(user, transferRequest);

        return new ModelAndView("redirect:/transactions/" + transaction.getId());
    }

    @GetMapping("/credit")
    public ModelAndView getPayCreditView(@AuthenticationPrincipal UserPrinciple userPrinciple) {
        User user = userService.getUserById(userPrinciple.getUser().getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("credit");
        modelAndView.addObject("creditRequest", new CreditRequest());
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @PostMapping("/credit")
    public ModelAndView payCredit(@AuthenticationPrincipal UserPrinciple userPrinciple, @Valid CreditRequest creditRequest, BindingResult bindingResult) {
        User user = userService.getUserById(userPrinciple.getUser().getId());

        Transaction transaction = walletService.payCredit(user, creditRequest.getSender());

        return new ModelAndView("redirect:/transactions/" + transaction.getId());
    }

}
