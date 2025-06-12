package app.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsersReport {

    private long totalUsers;
    private long activeUser;
    private long inactiveUsers;
    private long admins;
    private long nonAdmins;
    private LocalDateTime createdOn;
}
