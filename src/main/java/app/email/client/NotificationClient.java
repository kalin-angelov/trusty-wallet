package app.email.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "notification-sender-app", url = "http://localhost:8081/api/v1/notifications")
public interface NotificationClient {

    @GetMapping("/preferences")
    ResponseEntity<String> getUserNotificationSetting(@RequestParam(name = "userId") UUID userId);

}
