package app.web;

import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.EditRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
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

        if (bindingResult.hasErrors()) {
            User user = userService.getUserById(id);
            ModelAndView modelAndView = new ModelAndView();

            modelAndView.setViewName("profile");
            modelAndView.addObject("user", user);
            return modelAndView;
        }

        userService.editUser(id, editRequest);

        return new ModelAndView("redirect:/home");
    }
}
