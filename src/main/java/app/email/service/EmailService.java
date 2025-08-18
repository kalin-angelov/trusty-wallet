package app.email.service;

import app.email.client.NotificationClient;
import app.email.client.dto.NotificationPreferenceRequest;
import app.email.client.dto.NotificationPreferenceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class EmailService {
    
    @Autowired
    private NotificationClient notificationClient;


    public void saveNotificationSetting(UUID userId, boolean isEmailEnable , String email) {

        NotificationPreferenceRequest request = NotificationPreferenceRequest.builder()
                .userId(userId)
                .notificationEnabled(isEmailEnable)
                .type("EMAIL")
                .contactInfo(email)
                .build();

        ResponseEntity<Void> httpResponse = notificationClient.upsertNotificationSetting(request);

        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            log.error("[Feign call to notification-sender-app failed] Can't save notification settings for user with id - [%s]".formatted(userId));
        }
     }

    public NotificationPreferenceResponse getNotificationSettings(UUID userId) {

       ResponseEntity<NotificationPreferenceResponse> httpResponse = notificationClient.getUserNotificationSetting(userId);

       if(!httpResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Notification setting for user with id - [%s] is not found".formatted(userId));
       }

       return httpResponse.getBody();
    }

    public void changeNotificationSetting(UUID userId, boolean enabled) {

        ResponseEntity<NotificationPreferenceResponse> httpResponse = notificationClient.changeSendingSetting(userId, enabled);

        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            log.error("[Feign call to notification-sender-app failed] Can't make changes on notification setting with user ID - [%s].".formatted(userId));
        }

    }
}
