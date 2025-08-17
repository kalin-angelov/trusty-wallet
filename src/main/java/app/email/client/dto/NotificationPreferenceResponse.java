package app.email.client.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationPreferenceResponse {

    private String type;

    private boolean enabled;

    private String contactInfo;
}
