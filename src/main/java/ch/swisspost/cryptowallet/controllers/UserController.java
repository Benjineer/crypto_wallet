package ch.swisspost.cryptowallet.controllers;

import ch.swisspost.cryptowallet.dtos.RegisterRequest;
import ch.swisspost.cryptowallet.entities.User;
import ch.swisspost.cryptowallet.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return ResponseEntity
                .created(URI.create(String.format("/api/v1/user/%s", user.getId())))
                .body(user);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<User> getUser(@AuthenticationPrincipal(expression = "username") String username) {
        return ResponseEntity.ok(userService.getUser(username));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<Void> updateUser(@AuthenticationPrincipal(expression = "username") String username,
                                           @RequestBody User updatedUser) {
        userService.updateUser(username, updatedUser);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<Void> removeUser(@AuthenticationPrincipal(expression = "username") String username) {
        userService.removeUser(username);
        return ResponseEntity.ok().build();
    }
}
