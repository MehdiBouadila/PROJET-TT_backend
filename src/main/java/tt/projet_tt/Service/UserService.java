package tt.projet_tt.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import tt.projet_tt.Entities.Profile;
import tt.projet_tt.Entities.User;
import tt.projet_tt.Repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User register(User user) {
        String hashedPassword = passwordEncoder.encode(user.getMdp());
        user.setMdp(hashedPassword);
        return userRepository.save(user);
    }

    public String login(String email, String mdp) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(mdp, user.getMdp())) {
                return "LOGIN_SUCCESS";
            } else {
                return "INVALID_PASSWORD";
            }
        } else {
            return "USER_NOT_FOUND";
        }
    }

    public String loginAndGenerateToken(String email, String mdp) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(mdp, user.getMdp())) {
                return jwtService.generateToken(new org.springframework.security.core.userdetails.User(
                        user.getEmail(), user.getMdp(), new ArrayList<>()
                ));
            } else {
                throw new RuntimeException("Invalid password");
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public User loginAndReturnUser(String email, String mdp) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getMdp().equals(mdp)) {
                return user;
            } else {
                throw new RuntimeException("Invalid password");
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();
            return getUserByEmail(email);
        }
        throw new RuntimeException("No authenticated user found");
    }

    public User updateCurrentUser(User updatedUser) {
        User currentUser = getCurrentUser();

        // Update fields
        currentUser.setNom(updatedUser.getNom());
        currentUser.setPrenom(updatedUser.getPrenom());
        currentUser.setEmail(updatedUser.getEmail());

        // If password is being updated
        if (updatedUser.getMdp() != null && !updatedUser.getMdp().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(updatedUser.getMdp());
            currentUser.setMdp(hashedPassword);
        }

        return userRepository.save(currentUser);
    }

    public User updateUser(Long id, User updatedUser) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();

            // Update fields
            existingUser.setNom(updatedUser.getNom());
            existingUser.setPrenom(updatedUser.getPrenom());
            existingUser.setEmail(updatedUser.getEmail());

            // If password is being updated
            if (updatedUser.getMdp() != null && !updatedUser.getMdp().isEmpty()) {
                String hashedPassword = passwordEncoder.encode(updatedUser.getMdp());
                existingUser.setMdp(hashedPassword);
            }

            return userRepository.save(existingUser);
        } else {
            throw new RuntimeException("User not found with ID: " + id);
        }
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User with ID " + id + " does not exist.");
        }
        userRepository.deleteById(id);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            return userOptional.get();
        } else {
            throw new RuntimeException("User not found with ID: " + id);
        }
    }
    /// admin

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User adminUpdateUser(Long id, User updatedData) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Admin can update everything
        if (updatedData.getNom() != null) user.setNom(updatedData.getNom());
        if (updatedData.getPrenom() != null) user.setPrenom(updatedData.getPrenom());
        if (updatedData.getEmail() != null) user.setEmail(updatedData.getEmail());

        if (updatedData.getProfile() != null) user.setProfile(updatedData.getProfile());

        if (updatedData.getMdp() != null && !updatedData.getMdp().isEmpty()) {
            user.setMdp(passwordEncoder.encode(updatedData.getMdp()));
        }

        return userRepository.save(user);
    }

}