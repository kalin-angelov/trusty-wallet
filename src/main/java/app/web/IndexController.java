package app.web;

import app.credit.model.Credit;
import app.credit.service.CreditService;
import app.user.model.User;
import app.user.model.UserPrinciple;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {

    private final UserService userService;
    private final CreditService creditService;

    @Autowired
    public IndexController(UserService userService, CreditService creditService) {
        this.userService = userService;
        this.creditService = creditService;
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
}
