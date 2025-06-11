package app.web;

import app.user.model.User;
import app.user.model.UserPrinciple;
import app.user.service.UserService;
import app.web.dto.EditRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getAllUsers(@AuthenticationPrincipal UserPrinciple userPrinciple) {

        User user = userService.getUserById(userPrinciple.getUser().getId());

        List<User> users = userService.getAllUsers();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("users");
        modelAndView.addObject("user", user);
        modelAndView.addObject("users", users);

        return modelAndView;
    }

    @GetMapping("/{id}/profile")
    public ModelAndView getUserProfilePage (@PathVariable UUID id) {

        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getUserById(id);

        modelAndView.setViewName("profile");
        modelAndView.addObject("user", user);
        modelAndView.addObject("editRequest", new EditRequest());

        return modelAndView;
    }

    @PutMapping("/{id}/profile")
    public ModelAndView editProfile(@PathVariable UUID id, @Valid EditRequest editRequest, BindingResult bindingResult) {

        User user = userService.getUserById(id);
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("profile");
        modelAndView.addObject("user", user);

        if (!bindingResult.hasErrors()) {
            userService.editUser(id, editRequest);
            modelAndView.addObject("editRequest", new EditRequest());
            return modelAndView;
        }

        return modelAndView;
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeUserRole(@PathVariable UUID id) {
        userService.changeUserRole(id);

        return "redirect:/users";
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeUserStatus(@PathVariable UUID id) {
        userService.changeUserStatus(id);

        return "redirect:/users";
    }
}
