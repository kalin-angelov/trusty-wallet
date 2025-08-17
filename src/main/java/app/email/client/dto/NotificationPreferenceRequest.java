package app.email.client.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class NotificationPreferenceRequest {

    private UUID userId;

    private String type;

    private boolean notificationEnabled;

    private String contactInfo;
}
