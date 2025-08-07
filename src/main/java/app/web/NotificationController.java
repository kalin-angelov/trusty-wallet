package app.web;

import app.user.model.User;
import app.user.model.UserPrinciple;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ModelAndView getNotificationView(@AuthenticationPrincipal UserPrinciple userPrinciple) {

        ModelAndView modelAndView = new ModelAndView();

        User user = userService.getUserById(userPrinciple.getUser().getId());

        modelAndView.setViewName("notifications");
        modelAndView.addObject("user", user);

        return modelAndView;
    }
}
