package com.smartcampus.demo.Controller;

import com.smartcampus.demo.Entity.User;
import com.smartcampus.demo.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/admin")
public class AdminController {

    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "TECHNICIAN", "LECTURER", "USER", "STUDENT");

    @Autowired
    private UserService userService;

    /** Admin users can get all registered users. */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userService.getAllUsers();
        // Strip passwords before sending to client
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(users);
    }

    /** Admin users can update a user's role. */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable String userId, @RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();

        String newRole = body.get("role");
        if (newRole == null || !ALLOWED_ROLES.contains(newRole.toUpperCase())) {
            response.put("success", false);
            response.put("message", "Invalid role. Allowed values: ADMIN, TECHNICIAN, LECTURER, STUDENT");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        User user = userService.findById(userId);
        if (user == null) {
            response.put("success", false);
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        String normalizedRole = newRole.toUpperCase();
        if ("STUDENT".equals(normalizedRole)) {
            normalizedRole = "USER";
        }

        user.setRole(normalizedRole);
        userService.updateUser(user);

        response.put("success", true);
        response.put("message", "Role updated successfully");
        response.put("userId", userId);
        response.put("role", user.getRole());
        return ResponseEntity.ok(response);
    }

    /** Admin users can delete a user account. */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        User user = userService.findById(userId);
        if (user == null) {
            response.put("success", false);
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        String currentUsername = authentication != null ? authentication.getName() : null;
        if (currentUsername != null && currentUsername.equalsIgnoreCase(user.getUsername())) {
            response.put("success", false);
            response.put("message", "You cannot delete your own admin account");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        boolean deleted = userService.deleteUserById(userId);
        if (!deleted) {
            response.put("success", false);
            response.put("message", "Failed to delete user");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("success", true);
        response.put("message", "User deleted successfully");
        response.put("userId", userId);
        return ResponseEntity.ok(response);
    }
}
