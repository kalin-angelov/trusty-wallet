package app.web;

import app.transaction.model.Transaction;
import app.user.model.User;
import app.user.model.UserPrinciple;
import app.user.service.UserService;
import app.wallet.service.WalletService;
import app.web.dto.ChargeOwnWalletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/wallets")
public class WalletController {

    private final UserService userService;
    private final WalletService walletService;

    @Autowired
    public WalletController(UserService userService, WalletService walletService) {
        this.userService = userService;
        this.walletService = walletService;
    }

    @GetMapping
    public ModelAndView getWalletPage (@AuthenticationPrincipal UserPrinciple userPrinciple) {

        User user = userService.getUserById(userPrinciple.getUser().getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("wallets");
        modelAndView.addObject("user", user);
        modelAndView.addObject("chargeOwnWalletRequest", new ChargeOwnWalletRequest());
        return modelAndView;
    }

    @PutMapping("/{id}/status")
    public ModelAndView changeWalletStatus(@PathVariable UUID id, @AuthenticationPrincipal UserPrinciple userPrinciple) {

        User user = userService.getUserById(userPrinciple.getUser().getId());

        walletService.changeStatus(id, user);

        return new ModelAndView("redirect:/wallets");
    }

    @PutMapping("/{id}/charge")
    public ModelAndView chargeWalletUp(@PathVariable UUID id ,@Valid ChargeOwnWalletRequest chargeOwnWalletRequest, BindingResult bindingResult, @AuthenticationPrincipal UserPrinciple userPrinciple) {

        User user = userService.getUserById(userPrinciple.getUser().getId());
        Transaction transaction = walletService.chargeUpWallet(user, id, chargeOwnWalletRequest.getAmount());

        return new ModelAndView("redirect:/transactions/" + transaction.getId());
    }
}
