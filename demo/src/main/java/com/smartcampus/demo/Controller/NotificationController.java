package com.smartcampus.demo.Controller;

import com.smartcampus.demo.Entity.Notification;
import com.smartcampus.demo.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("api/v1/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /** Any authenticated user can get all notifications. */
    @GetMapping("/getall")
    public Iterable<Notification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    /** Any authenticated user can get notifications by user ID. */
    @GetMapping("/user/{userId}")
    public Iterable<Notification> getNotificationsByUserId(@PathVariable String userId) {
        return notificationService.getNotificationsByUserId(userId);
    }

    /** Any authenticated user can get unread notifications by user ID. */
    @GetMapping("/user/{userId}/unread")
    public Iterable<Notification> getUnreadNotificationsByUserId(@PathVariable String userId) {
        return notificationService.getUnreadNotificationsByUserId(userId);
    }

    /** Any authenticated user can get a notification by ID. */
    @GetMapping("/{id}")
    public ResponseEntity<?> getNotificationById(@PathVariable String id) {
        Optional<Notification> notificationOpt = notificationService.getNotificationById(id);
        
        if (notificationOpt.isPresent()) {
            return new ResponseEntity<>(notificationOpt.get(), HttpStatus.OK);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Notification not found");
            
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    /** Any authenticated user can mark a notification as read. */
    @PutMapping("/read/{id}")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable String id) {
        Optional<Notification> notificationOpt = notificationService.getNotificationById(id);
        
        if (notificationOpt.isPresent()) {
            notificationService.markNotificationAsRead(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification marked as read");
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Notification not found");
            
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    /** Any authenticated user can mark all notifications as read for a user. */
    @PutMapping("/read/all/user/{userId}")
    public ResponseEntity<?> markAllNotificationsAsRead(@PathVariable String userId) {
        notificationService.markAllNotificationsAsRead(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "All notifications marked as read");
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /** Any authenticated user can delete a notification. */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable String id) {
        Optional<Notification> notificationOpt = notificationService.getNotificationById(id);
        
        if (notificationOpt.isPresent()) {
            notificationService.deleteNotification(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification deleted successfully");
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Notification not found");
            
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    /** Any authenticated user can get unread notification count for a user. */
    @GetMapping("/count/user/{userId}/unread")
    public ResponseEntity<?> countUnreadNotifications(@PathVariable String userId) {
        long count = notificationService.countUnreadNotifications(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
