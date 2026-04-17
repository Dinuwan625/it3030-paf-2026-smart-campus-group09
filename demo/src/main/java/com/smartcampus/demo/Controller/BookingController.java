package com.smartcampus.demo.Controller;

import com.smartcampus.demo.Entity.Booking;
import com.smartcampus.demo.Service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    /** Any authenticated user can create a booking request. */
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (booking.getResourceId() == null || booking.getResourceId().isBlank()) {
                response.put("success", false);
                response.put("message", "Resource ID is required");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            if (booking.getDate() == null || booking.getStartTime() == null || booking.getEndTime() == null) {
                response.put("success", false);
                response.put("message", "Date, start time and end time are required");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            Booking saved = bookingService.createBooking(booking);
            response.put("success", true);
            response.put("message", "Booking request submitted successfully");
            response.put("bookingId", saved.get_id());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create booking: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** Admin and technician users can get all booking requests. */
    @GetMapping("/all")
    public ResponseEntity<?> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    /** Any authenticated user can get bookings by user ID. */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBookingsByUser(@PathVariable String userId) {
        List<Booking> bookings = bookingService.getBookingsByUser(userId);
        return ResponseEntity.ok(bookings);
    }

    /** Any authenticated user can get bookings for a specific resource. */
    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<?> getBookingsByResource(@PathVariable String resourceId) {
        List<Booking> bookings = bookingService.getBookingsByResource(resourceId);
        return ResponseEntity.ok(bookings);
    }

    /** Admin and technician users can update booking status. */
    @PutMapping("/{bookingId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String bookingId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();
        String newStatus = body.get("status");
        String reason = body.get("reason");
        if (newStatus == null || newStatus.isBlank()) {
            response.put("success", false);
            response.put("message", "Status is required");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            String updatedBy = authentication != null ? authentication.getName() : "system";
            Booking updated = bookingService.updateStatus(bookingId, newStatus.toUpperCase(), updatedBy, reason);
            if (updated == null) {
                response.put("success", false);
                response.put("message", "Booking not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            response.put("success", true);
            response.put("message", "Status updated to " + updated.getStatus());
            response.put("status", updated.getStatus());
            response.put("rejectionReason", updated.getRejectionReason());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /** Admin users can delete a booking request. */
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> deleteBooking(@PathVariable String bookingId) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean deleted = bookingService.deleteBookingById(bookingId);
            if (!deleted) {
                response.put("success", false);
                response.put("message", "Booking not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            response.put("success", true);
            response.put("message", "Booking deleted successfully");
            response.put("bookingId", bookingId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete booking: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
