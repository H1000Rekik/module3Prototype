package com.codearena.controller;

import com.codearena.model.Submission;
import com.codearena.service.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "http://localhost:4200")
public class SubmissionController {

    private final SubmissionService service;

    public SubmissionController(SubmissionService service) {
        this.service = service;
    }

    /**
     * POST /api/submissions
     * Body: { username, code, mode }
     * mode = "run" (public tests) | "submit" (all tests)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> submit(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "anonymous");
        String code = body.get("code");
        String mode = body.getOrDefault("mode", "run");

        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Code is required"));
        }

        Submission s = service.createSubmission(username, code, mode);
        return ResponseEntity.ok(Map.of(
                "id", s.getId(),
                "status", s.getStatus(),
                "message", "Submission queued"));
    }

    /**
     * GET /api/submissions/{id}
     * Poll for result
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSubmission(@PathVariable("id") Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/submissions/user/{username}
     * Submission history
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<List<Submission>> getUserSubmissions(@PathVariable("username") String username) {
        return ResponseEntity.ok(service.getByUsername(username));
    }
}
