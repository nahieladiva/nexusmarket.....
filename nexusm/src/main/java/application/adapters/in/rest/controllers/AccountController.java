package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.UserMapper;
import application.adapters.in.rest.requests.AddressRequest;
import application.adapters.in.rest.requests.ChangePasswordRequest;
import application.adapters.in.rest.requests.UpdateProfileRequest;
import application.adapters.in.rest.responses.MessageResponse;
import application.adapters.in.rest.responses.UserResponse;
import application.adapters.in.rest.security.AuthenticatedUser;
import application.domain.ports.in.AccountPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter of {@link application.domain.ports.in.AccountPort}: the account of any authenticated
 * user. The user always comes from the token.
 */
@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountPort port;
    private final UserMapper userMapper;

    @GetMapping("/profile")
    public UserResponse findProfile(@AuthenticationPrincipal AuthenticatedUser me) {
        return userMapper.toResponse(port.findProfile(me.userId()));
    }

    @PatchMapping("/profile")
    public UserResponse updateProfile(@AuthenticationPrincipal AuthenticatedUser me,
                                      @Valid @RequestBody UpdateProfileRequest request) {
        return userMapper.toResponse(port.updateProfile(me.userId(), request.fullName(),
            userMapper.toPhone(request.phone())));
    }

    @PostMapping("/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse addAddress(@AuthenticationPrincipal AuthenticatedUser me,
                                   @Valid @RequestBody AddressRequest request) {
        return userMapper.toResponse(port.addAddress(me.userId(), userMapper.toDomain(request)));
    }

    /** An address has no id, so it is identified by its data and sent in the body. */
    @PostMapping("/addresses/remove")
    public UserResponse removeAddress(@AuthenticationPrincipal AuthenticatedUser me,
                                      @Valid @RequestBody AddressRequest request) {
        return userMapper.toResponse(port.removeAddress(me.userId(), userMapper.toDomain(request)));
    }

    @PutMapping("/password")
    public MessageResponse changePassword(@AuthenticationPrincipal AuthenticatedUser me,
                                          @Valid @RequestBody ChangePasswordRequest request) {
        port.changePassword(me.userId(), request.currentPassword(), request.newPassword());
        return new MessageResponse("Password changed; every session was closed");
    }

    @PostMapping("/logout")
    public MessageResponse logout(@AuthenticationPrincipal AuthenticatedUser me) {
        port.logout(me.token());
        return new MessageResponse("Session closed; the token was revoked");
    }
}
