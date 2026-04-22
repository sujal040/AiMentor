package com.aimentor.controller;

import com.aimentor.entity.User;
import com.aimentor.repositories.UserRepository;
import com.aimentor.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepo, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public String signup(@RequestBody User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        userRepo.save(user);
        return "User registered";
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {

        User dbUser = userRepo.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(user.getPassword(), dbUser.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtUtil.generateToken(user.getUsername());
    }
}