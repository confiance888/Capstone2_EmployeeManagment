package org.example.employeemanagement.controller;




import jakarta.validation.Valid;
import org.example.employeemanagement.dto.AuthRequest;
import org.example.employeemanagement.dto.AuthResponse;
import org.example.employeemanagement.model.Role;
import org.example.employeemanagement.model.User;
import org.example.employeemanagement.service.JwtService;
import org.example.employeemanagement.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserService userService) {
        .this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String username,
                                      @RequestParam String email,
                                      @RequestParam String password,
                                      @RequestParam(defaultValue = "ROLE_USER") String role) {
        Role parsedRole = Role.valueOf(role);
        User created = userService.registerUser(username, email, password, parsedRole);
        String token = jwtService.generateToken(created.getUsername(), created.getRole().name());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            // find user to get role
            User user = userService.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}
