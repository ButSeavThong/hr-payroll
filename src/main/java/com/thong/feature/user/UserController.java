package com.thong.feature.user;

import com.cloudinary.Cloudinary;
import com.thong.domain.User;
import com.thong.feature.user.dto.UpdateProfileRequest;
import com.thong.feature.user.dto.UserProfileResponse;
import com.thong.service.CloudinaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserSerivce userService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    public List<UserProfileResponse> getAllUsers() {
        return userService.getUserProfiles();
    }

    @GetMapping("/profile/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    public UserProfileResponse getProfileById( @PathVariable Integer id) {
        return userService.getUserProfile(id);
    }

    @PatchMapping("update/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_EMPLOYEE', 'SCOPE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public UserProfileResponse updateProfileById( @PathVariable Integer id, @Valid @RequestBody UpdateProfileRequest updateProfileRequest) {
        return userService.UpdateProfileById(id, updateProfileRequest);
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_EMPLOYEE', 'SCOPE_ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            // Get the email from JWT token (subject)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assert authentication != null;
            String email = authentication.getName(); // This is the 'sub' claim from JWT
            // Find user by email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(userMapper.toUserProfileResponse(user));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
            );
        }
    }


    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    public ResponseEntity<UserProfileResponse> toggleUserStatus(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.toggleUserStatus(id));
    }

    // added new feature
    // ── Upload / update profile image ─────────────────────────────────────────
    @PatchMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('SCOPE_EMPLOYEE', 'SCOPE_ADMIN')")
    public ResponseEntity<UserProfileResponse> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt) {

        String email = jwt.getSubject();
        return ResponseEntity.ok(userService.updateProfileImage(email, file));
    }

    // ── Remove profile image ───────────────────────────────────────────────────
    @DeleteMapping("/profile-image")
    @PreAuthorize("hasAnyAuthority('SCOPE_EMPLOYEE', 'SCOPE_ADMIN')")
    public ResponseEntity<UserProfileResponse> removeProfileImage(
            @AuthenticationPrincipal Jwt jwt) {

        String email = jwt.getSubject();
        return ResponseEntity.ok(userService.removeProfileImage(email));
    }
}
