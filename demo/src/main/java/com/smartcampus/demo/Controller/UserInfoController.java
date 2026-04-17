package com.smartcampus.demo.Controller;

import com.smartcampus.demo.Entity.User;
import com.smartcampus.demo.Service.UserService;
import com.smartcampus.demo.Service.OAuth2UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/user")
public class UserInfoController {

    @Autowired
    private OAuth2UserDetailsService oauth2UserDetailsService;

    @Autowired
    private UserService userService;

    /** Any authenticated user can get current user information. */
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication == null || !authentication.isAuthenticated()) {
            response.put("success", false);
            response.put("message", "User not authenticated");
            return ResponseEntity.status(401).body(response);
        }

        response.put("success", true);
        response.put("authenticated", true);
        response.put("principal", authentication.getPrincipal().toString());

        String resolvedEmail = null;
        String resolvedName = null;
        String resolvedUsername = authentication.getName();

        // Check if OAuth2 authenticated
        if (oauth2UserDetailsService.isOAuth2Authenticated()) {
            response.put("authType", "OAuth2");
            response.put("provider", oauth2UserDetailsService.getProvider());
            resolvedEmail = oauth2UserDetailsService.getUserEmail();
            resolvedName = oauth2UserDetailsService.getUserName();
            response.put("email", resolvedEmail);
            response.put("name", resolvedName);
            response.put("picture", oauth2UserDetailsService.getUserPicture());
            response.put("attributes", oauth2UserDetailsService.getCurrentUserAttributes());
        } else {
            response.put("authType", "FormLogin");
            response.put("principal", authentication.getPrincipal());
            response.put("name", resolvedUsername);
        }

        User dbUser = null;
        if (resolvedEmail != null && !resolvedEmail.isBlank()) {
            dbUser = userService.findByEmail(resolvedEmail);
        }
        if (dbUser == null && resolvedUsername != null && !resolvedUsername.isBlank()) {
            dbUser = userService.findByUsername(resolvedUsername);
        }

        if (dbUser != null) {
            response.put("userId", dbUser.get_id());
            response.put("username", dbUser.getUsername());
            response.put("email", dbUser.getEmail());
            response.put("role", dbUser.getRole());
            if (response.get("name") == null) {
                response.put("name", dbUser.getUsername());
            }
        } else {
            response.put("username", resolvedUsername);
            if (response.get("name") == null) {
                response.put("name", resolvedUsername);
            }
        }

        return ResponseEntity.ok(response);
    }

    /** OAuth2-authenticated users can get detailed OAuth2 profile information. */
    @GetMapping("/oauth2-details")
    public ResponseEntity<?> getOAuth2Details() {
        OidcUser oidcUser = oauth2UserDetailsService.getCurrentOidcUser();

        if (oidcUser == null) {
            return ResponseEntity.status(400).body(Map.of(
                "error", "User is not authenticated via OAuth2"
            ));
        }

        Map<String, Object> details = new HashMap<>();
        details.put("email", oidcUser.getEmail());
        details.put("name", oidcUser.getFullName());
        details.put("givenName", oidcUser.getGivenName());
        details.put("familyName", oidcUser.getFamilyName());
        details.put("picture", oidcUser.getAttributes().get("picture"));
        details.put("emailVerified", oidcUser.getAttribute("email_verified"));
        details.put("locale", oidcUser.getAttribute("locale"));

        return ResponseEntity.ok(details);
    }

    /** OAuth2-authenticated users can get basic OAuth2 profile information. */
    @GetMapping("/oauth2-simple")
    public ResponseEntity<?> getOAuth2UserSimple(OidcUser oidcUser) {
        if (oidcUser == null) {
            return ResponseEntity.status(400).body(Map.of(
                "error", "User is not authenticated via OAuth2"
            ));
        }

        return ResponseEntity.ok(Map.of(
            "email", oidcUser.getEmail(),
            "name", oidcUser.getFullName(),
            "picture", oidcUser.getAttributes().get("picture")
        ));
    }
}
