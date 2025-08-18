package app.email.client;

import app.email.client.dto.NotificationPreferenceRequest;
import app.email.client.dto.NotificationPreferenceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "notification-sender-app", url = "http://localhost:8081/api/v1/notifications")
public interface NotificationClient {

    @PostMapping("/preferences")
    ResponseEntity<Void> upsertNotificationSetting(@RequestBody NotificationPreferenceRequest request);

    @GetMapping("/preferences")
    ResponseEntity<NotificationPreferenceResponse> getUserNotificationSetting(@RequestParam(name = "userId") UUID userId);

    @PutMapping("/preferences")
    ResponseEntity<NotificationPreferenceResponse> changeSendingSetting(@RequestParam(name = "userId") UUID userId, @RequestParam(name = "enabled") boolean enabled);
}
