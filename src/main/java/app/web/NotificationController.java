package app.web;

import app.email.client.dto.NotificationPreferenceResponse;
import app.email.service.EmailService;
import app.user.model.User;
import app.user.model.UserPrinciple;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @GetMapping
    public ModelAndView getNotificationView(@AuthenticationPrincipal UserPrinciple userPrinciple) {

        ModelAndView modelAndView = new ModelAndView();

        User user = userService.getUserById(userPrinciple.getUser().getId());

        NotificationPreferenceResponse notificationSetting = emailService.getNotificationSettings(user.getId());
        modelAndView.setViewName("notifications");
        modelAndView.addObject("user", user);
        modelAndView.addObject("notificationSetting", notificationSetting);

        return modelAndView;
    }

    @PutMapping("/{userId}/{enabled}")
    public String changeNotificationSetting(@PathVariable UUID userId, @PathVariable boolean enabled) {

        emailService.changeNotificationSetting(userId, !enabled);

        return "redirect:/notifications";
    }
}
