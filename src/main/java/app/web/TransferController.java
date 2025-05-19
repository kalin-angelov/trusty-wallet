package app.web;

import app.user.model.User;
import app.user.model.UserPrinciple;
import app.user.service.UserService;
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

    @Autowired
    public TransferController(UserService userService) {
        this.userService = userService;
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
    public ModelAndView send(@Valid TransferRequest transferRequest, BindingResult bindingResult) {

        if (bindingResult.hasGlobalErrors()) {
            return new ModelAndView("transfer");
        }

        return null;
    }

}
