package app.web;

import app.exception.EmailAlreadyExistException;
import app.exception.UsernameAlreadyExistException;
import app.user.model.UserPrinciple;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

@ControllerAdvice
public class ExceptionAdvice {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            AccessDeniedException.class,
            NoResourceFoundException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestValueException.class,
            HttpRequestMethodNotSupportedException.class,
    })
    public ModelAndView handleNotFoundExceptions (Exception exception) {

        return new ModelAndView("not-found");
    }

    @ExceptionHandler(UsernameAlreadyExistException.class)
    public ModelAndView handleUsernameIsAlreadyExistingException(RedirectAttributes redirectAttributes, UsernameAlreadyExistException exception) {

        String errorMessage = exception.getMessage();
        redirectAttributes.addFlashAttribute("usernameAlreadyExistMessage", errorMessage);

        return new ModelAndView("redirect:/register");
    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    public ModelAndView handleEmailAlreadyExistException(@AuthenticationPrincipal UserPrinciple userPrinciple, HttpServletRequest request, RedirectAttributes redirectAttributes, EmailAlreadyExistException exception) {

        String uri = request.getRequestURI();

        String errorMessage = exception.getMessage();
        redirectAttributes.addFlashAttribute("emailAlreadyExistMessage", errorMessage);

        if (uri.contains("/register")) {
            return new ModelAndView("redirect:/register");
        }

        UUID id = userPrinciple.getUser().getId();
        return new ModelAndView("redirect:/users/" + id + "/profile");
    }
}
