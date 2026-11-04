package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.UserMapper;
import application.adapters.in.rest.requests.AddressRequest;
import application.adapters.in.rest.requests.UpdateProfileRequest;
import application.adapters.in.rest.responses.UserResponse;
import application.adapters.in.rest.security.AuthenticatedUser;
import application.domain.ports.in.UserManagementPort;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador REST del puerto de entrada {@link UserManagementPort}.
 * {@code /me} es el propio usuario; el resto es solo para administradores.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserManagementPort userManagementPort;
    private final UserMapper userMapper;

    public UserController(UserManagementPort userManagementPort, UserMapper userMapper) {
        this.userManagementPort = userManagementPort;
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal AuthenticatedUser me) {
        return ResponseEntity.ok(userMapper.toResponse(userManagementPort.findUserById(me.userId())));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@AuthenticationPrincipal AuthenticatedUser me,
                                                      @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userMapper.toResponse(userManagementPort.updateProfile(me.userId(),
            request.fullName(), request.phone() == null ? null : PhoneNumber.of(request.phone()))));
    }

    @PostMapping("/me/addresses")
    public ResponseEntity<UserResponse> addAddress(@AuthenticationPrincipal AuthenticatedUser me,
                                                   @RequestBody AddressRequest request) {
        return ResponseEntity.ok(userMapper.toResponse(
            userManagementPort.addAddress(me.userId(), userMapper.toDomain(request))));
    }

    @DeleteMapping("/me/addresses")
    public ResponseEntity<UserResponse> removeAddress(@AuthenticationPrincipal AuthenticatedUser me,
                                                      @RequestBody AddressRequest request) {
        return ResponseEntity.ok(userMapper.toResponse(
            userManagementPort.removeAddress(me.userId(), userMapper.toDomain(request))));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll(@RequestParam(required = false) String email) {
        if (email != null && !email.isBlank()) {
            return ResponseEntity.ok(userManagementPort.findByEmail(Email.of(email))
                .map(user -> List.of(userMapper.toResponse(user)))
                .orElse(List.of()));
        }
        return ResponseEntity.ok(userManagementPort.findAllUsers().stream()
            .map(userMapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(userMapper.toResponse(userManagementPort.findUserById(UserId.of(id))));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<UserResponse> approveSeller(@PathVariable String id) {
        return ResponseEntity.ok(userMapper.toResponse(userManagementPort.approveSeller(UserId.of(id))));
    }
}
