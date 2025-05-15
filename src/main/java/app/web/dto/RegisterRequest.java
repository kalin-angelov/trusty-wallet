package app.web.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @Size(min = 4, message = "Username must be at least 4 characters long")
    private String username;

    @Size(min = 5, message = "Password must be at least 5 characters long")
    private String password;
}
