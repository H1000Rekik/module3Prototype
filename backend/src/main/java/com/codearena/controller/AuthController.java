package com.codearena.controller;

import com.codearena.model.User;
import com.codearena.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Use a fixed key for simplicity in dev/testing
    public static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
        String username = creds.get("username");
        String password = creds.get("password");

        // Simple register if not exists
        Optional<User> optUser = userRepository.findByUsername(username);
        User user;
        if (optUser.isEmpty()) {
            user = new User(username, passwordEncoder.encode(password));
            userRepository.save(user);
        } else {
            user = optUser.get();
            if (!passwordEncoder.matches(password, user.getPassword())) {
                return ResponseEntity.status(401).body("Invalid credentials");
            }
        }

        String token = Jwts.builder()
                .setSubject(username)
                .claim("id", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(SECRET_KEY)
                .compact();

        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("user", user);
        return ResponseEntity.ok(resp);
    }
}
