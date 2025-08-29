package tt.projet_tt.Controllers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tt.projet_tt.Entities.User;
import tt.projet_tt.Service.UserService;


import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "*")
@CrossOrigin(origins = "content-eus2.infrastructure.2.azurestaticapps.net")
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User register(@Valid @RequestBody User user) {
        return userService.register(user);
    }

//    @PostMapping("/login")
//    public User login(@Valid @RequestBody Map<String, String> loginData) {
//        String email = loginData.get("email");
//        String mdp = loginData.get("mdp");
//        return userService.loginAndReturnUser(email, mdp);
//    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String mdp = loginData.get("mdp");
        String token = userService.loginAndGenerateToken(email, mdp);
        return Collections.singletonMap("token", token);
    }

    @GetMapping("/get_current_user")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            User currentUser = userService.getCurrentUser();

            // Create response without password for security
            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("id", currentUser.getMatricule());
            userProfile.put("nom", currentUser.getNom());
            userProfile.put("prenom", currentUser.getPrenom());
            userProfile.put("email", currentUser.getEmail());
            userProfile.put("profile", currentUser.getProfile());

            return ResponseEntity.ok(userProfile);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get user profile: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    @PutMapping("/profile")
    public ResponseEntity<?> updateCurrentUserProfile(@RequestBody User updateRequest) {
        try {

            User currentUser = userService.getCurrentUser();

            // Update only the fields that are provided
            if (updateRequest.getNom() != null && !updateRequest.getNom().trim().isEmpty()) {
                currentUser.setNom(updateRequest.getNom());
            }
            if (updateRequest.getPrenom() != null && !updateRequest.getPrenom().trim().isEmpty()) {
                currentUser.setPrenom(updateRequest.getPrenom());
            }
            if (updateRequest.getEmail() != null && !updateRequest.getEmail().trim().isEmpty()) {
                currentUser.setEmail(updateRequest.getEmail());
            }

            // Only set password if it's provided and not empty
            if (updateRequest.getMdp() != null && !updateRequest.getMdp().trim().isEmpty()) {
                currentUser.setMdp(updateRequest.getMdp());
            }

            User savedUser = userService.updateCurrentUser(currentUser);

            // Return success response without password
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("user", Map.of(
                    "id", savedUser.getMatricule(),
                    "nom", savedUser.getNom(),
                    "prenom", savedUser.getPrenom(),
                    "email", savedUser.getEmail(),
                    "profile", savedUser.getProfile()
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update profile: " + e.getMessage());
            return ResponseEntity.status(400).body(errorResponse);
        }
    }

    @PutMapping("/update/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return userService.updateUser(id, updatedUser);
    }

//    @PostMapping("/delete/{id}")
//    public String deleteUser(@PathVariable Long id, @RequestBody Map<String, String> currentUserData) {
//        String profile = currentUserData.get("profile");
//        userService.deleteUser(id, profile);
//        return "User deleted successfully";
//    }

    @GetMapping("/user/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
    @GetMapping("/get_profile")
    public User getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getUserByEmail(userDetails.getUsername());
    }

    // Update logged-in user's profile
    @PutMapping("/update_profile")
    public User updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestBody User updatedUser) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        return userService.updateUser(currentUser.getMatricule(), updatedUser);
    }
//    @GetMapping("/get_current_user")
//    public ResponseEntity<User> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
//        String token = authHeader.replace("Bearer ", "");
//        String email = extractEmailFromJWT(token);
//        User user = userService.getUserByEmail(email);
//        return ResponseEntity.ok(user);
//    }
//
//    private String extractEmailFromJWT(String token) {
//        Claims claims = Jwts.parser()
//                .setSigningKey("TT")
//                .parseClaimsJws(token)
//                .getBody();
//        return claims.getSubject();
//    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful. Please remove token from client storage.");
        return ResponseEntity.ok(response);
    }
//admin
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/delete/{id}")
public String deleteUser(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
    User currentUser = userService.getUserByEmail(userDetails.getUsername());

    if (!"ADMIN".equalsIgnoreCase(currentUser.getProfile().toString()))
    {
        throw new RuntimeException("Only ADMIN users can delete accounts.");
    }

    userService.deleteUser(id);
    return "User deleted successfully";
}
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/update/{id}")
    public ResponseEntity<?> adminUpdateUser(
            @PathVariable Long id,
            @RequestBody User updatedUserData
    ) {
        try {
            User updatedUser = userService.adminUpdateUser(id, updatedUserData);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User updated successfully by admin");
            response.put("user", Map.of(
                    "id", updatedUser.getMatricule(),
                    "nom", updatedUser.getNom(),
                    "prenom", updatedUser.getPrenom(),
                    "email", updatedUser.getEmail(),
                    "profile", updatedUser.getProfile()
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/admin/role")
    public ResponseEntity<?> getUserRoles(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(roles);
    }

}
