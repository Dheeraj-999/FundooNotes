package com.fundoo.notes.controller;

/*
 * Class Name  : RedisController
 * Description : Exposes REST endpoints for Redis in-memory key-value caching operations
 *               and Time-To-Live (TTL) OTP generation/verification.
 * Author      : Dheeraj Buchha
 * Note        : Refactored and maintained for FundooNotes (Original: Utkarsh Jain)
 * Date        : 27/11/2026
 */

import com.fundoo.notes.service.OtpService;
import com.fundoo.notes.service.RedisService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/redis")
public class RedisController {

    private final RedisService redisService;
    private final OtpService otpService;

    public RedisController(RedisService redisService, OtpService otpService) {
        this.redisService = redisService;
        this.otpService = otpService;
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, String>> save(
            @RequestParam String key,
            @RequestParam String value) {

        redisService.save(key, value, Duration.ofMinutes(10));
        return ResponseEntity.ok(Map.of("message", "Data saved successfully"));
    }

    @GetMapping("/get/{key}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String key) {
        Object retrievedValue = redisService.get(key);
        if (retrievedValue == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("key", key, "value", retrievedValue));
    }

    @DeleteMapping("/delete/{key}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String key) {
        redisService.delete(key);
        return ResponseEntity.ok(Map.of("message", "Data deleted successfully"));
    }

    @PostMapping("/otp/generate")
    public ResponseEntity<Map<String, String>> generateOtp(@RequestParam String email) {
        String generatedOtp = otpService.generateOtp(email);
        return ResponseEntity.ok(Map.of("email", email, "otp", generatedOtp));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<Map<String, String>> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp) {

        boolean isOtpValid = otpService.verifyOtp(email, otp);
        if (!isOtpValid) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired OTP"));
        }
        return ResponseEntity.ok(Map.of("message", "OTP verified successfully"));
    }
}
