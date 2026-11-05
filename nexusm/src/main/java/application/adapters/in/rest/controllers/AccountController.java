package application.adapters.in.rest.controllers;

import application.adapters.in.rest.documentation.ApiDocumentation;
import application.adapters.in.rest.mappers.UserMapper;
import application.adapters.in.rest.requests.AddressRequest;
import application.adapters.in.rest.requests.ChangePasswordRequest;
import application.adapters.in.rest.requests.UpdateProfileRequest;
import application.adapters.in.rest.responses.ErrorResponse;
import application.adapters.in.rest.responses.MessageResponse;
import application.adapters.in.rest.responses.UserResponse;
import application.adapters.in.rest.security.AuthenticatedUser;
import application.domain.ports.in.AccountPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Account", description = "Profile, addresses, password and logout of any authenticated user")
@SecurityRequirement(name = ApiDocumentation.BEARER_AUTH)
@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountPort port;
    private final UserMapper userMapper;

    @Operation(summary = "Find the profile of the authenticated user")
    @ApiResponse(responseCode = "200", description = "The profile")
    @ApiResponse(responseCode = "401", description = ApiDocumentation.UNAUTHORIZED,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/profile")
    public UserResponse findProfile(@AuthenticationPrincipal AuthenticatedUser me) {
        return userMapper.toResponse(port.findProfile(me.userId()));
    }

    @Operation(summary = "Update the name or the phone")
    @ApiResponse(responseCode = "200", description = "The updated profile")
    @ApiResponse(responseCode = "400", description = ApiDocumentation.BAD_REQUEST,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = ApiDocumentation.UNAUTHORIZED,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping("/profile")
    public UserResponse updateProfile(@AuthenticationPrincipal AuthenticatedUser me,
                                      @Valid @RequestBody UpdateProfileRequest request) {
        return userMapper.toResponse(port.updateProfile(me.userId(), request.fullName(),
            userMapper.toPhone(request.phone())));
    }

    @Operation(summary = "Add an address")
    @ApiResponse(responseCode = "201", description = "The profile with the new address")
    @ApiResponse(responseCode = "400", description = ApiDocumentation.BAD_REQUEST,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = ApiDocumentation.UNAUTHORIZED,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "The user has too many addresses",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse addAddress(@AuthenticationPrincipal AuthenticatedUser me,
                                   @Valid @RequestBody AddressRequest request) {
        return userMapper.toResponse(port.addAddress(me.userId(), userMapper.toDomain(request)));
    }

    /** An address has no id, so it is identified by its data and sent in the body. */
    @Operation(summary = "Remove an address", description = "An address has no id, so the body identifies it")
    @ApiResponse(responseCode = "200", description = "The profile without the address")
    @ApiResponse(responseCode = "400", description = "The body is invalid or the address is not registered",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = ApiDocumentation.UNAUTHORIZED,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/addresses/remove")
    public UserResponse removeAddress(@AuthenticationPrincipal AuthenticatedUser me,
                                      @Valid @RequestBody AddressRequest request) {
        return userMapper.toResponse(port.removeAddress(me.userId(), userMapper.toDomain(request)));
    }

    @Operation(summary = "Change the password", description = "Every session of the user is closed, "
        + "including the current one")
    @ApiResponse(responseCode = "200", description = "The password changed")
    @ApiResponse(responseCode = "400", description = ApiDocumentation.BAD_REQUEST,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "The token is invalid or the current password is wrong",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "The new password does not follow the policy",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/password")
    public MessageResponse changePassword(@AuthenticationPrincipal AuthenticatedUser me,
                                          @Valid @RequestBody ChangePasswordRequest request) {
        port.changePassword(me.userId(), request.currentPassword(), request.newPassword());
        return new MessageResponse("Password changed; every session was closed");
    }

    @Operation(summary = "Close the current session")
    @ApiResponse(responseCode = "200", description = "The token was revoked")
    @ApiResponse(responseCode = "401", description = ApiDocumentation.UNAUTHORIZED,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/logout")
    public MessageResponse logout(@AuthenticationPrincipal AuthenticatedUser me) {
        port.logout(me.token());
        return new MessageResponse("Session closed; the token was revoked");
    }
}
