package com.codearena.service;

import com.codearena.model.Submission;
import com.codearena.repository.SubmissionRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SubmissionService {

    private final SubmissionRepository repository;
    private final Judge0Service judge0Service;

    // Hardcoded test cases for the prototype problem (Two Sum)
    // In production these come from the DB
    private static final String[][] PUBLIC_TESTS = {
        {"2 7 11 15\n9", "0 1"},
        {"3 2 4\n6", "1 2"}
    };

    private static final String[][] HIDDEN_TESTS = {
        {"3 3\n6", "0 1"},
        {"1 2 3 4 5\n9", "3 4"},
        {"-1 -2 -3 -4 -5\n-8", "2 4"}
    };

    public SubmissionService(SubmissionRepository repository, Judge0Service judge0Service) {
        this.repository = repository;
        this.judge0Service = judge0Service;
    }

    public Submission createSubmission(String username, String code, String mode) {
        Submission s = new Submission();
        s.setUsername(username);
        s.setCode(code);
        s.setMode(mode);
        s.setStatus("PENDING");
        Submission saved = repository.save(s);
        processAsync(saved.getId());
        return saved;
    }

    @Async
    public void processAsync(Long submissionId) {
        Submission s = repository.findById(submissionId).orElse(null);
        if (s == null) return;

        s.setStatus("PROCESSING");
        repository.save(s);

        try {
            String[][] tests = s.getMode().equals("run") ? PUBLIC_TESTS : HIDDEN_TESTS;

            StringBuilder outputLog = new StringBuilder();
            String finalVerdict = "Accepted";
            int totalRuntime = 0;
            int totalMemory = 0;
            int passedTests = 0;

            // Wrap user code to read input and solve Two Sum
            // User is expected to write a function solve(nums, target) -> list[int]
            // We inject a runner that reads stdin and calls it

            for (int i = 0; i < tests.length; i++) {
                String stdin = tests[i][0];
                String expectedOutput = tests[i][1].trim();

                String wrappedCode = buildRunner(s.getCode(), stdin);

                String token = judge0Service.submitCode(wrappedCode, null);

                // Poll for result (max 10 seconds)
                Map<String, Object> result = null;
                for (int attempt = 0; attempt < 10; attempt++) {
                    Thread.sleep(1000);
                    result = judge0Service.getResult(token);
                    Map<String, Object> status = (Map<String, Object>) result.get("status");
                    int statusId = (Integer) status.get("id");
                    if (statusId > 2) break; // Not In Queue or Processing
                }

                if (result == null) {
                    finalVerdict = "Internal Error";
                    break;
                }

                Map<String, Object> status = (Map<String, Object>) result.get("status");
                int statusId = (Integer) status.get("id");
                String verdict = judge0Service.mapVerdict(statusId);

                String stdout = judge0Service.decodeBase64((String) result.get("stdout")).trim();
                String stderr = judge0Service.decodeBase64((String) result.get("stderr")).trim();
                String compileOutput = judge0Service.decodeBase64((String) result.get("compile_output")).trim();

                Double time = result.get("time") != null ? Double.parseDouble(result.get("time").toString()) : 0.0;
                Integer memory = result.get("memory") != null ? (Integer) result.get("memory") : 0;

                totalRuntime += (int)(time * 1000);
                totalMemory = Math.max(totalMemory, memory);

                outputLog.append("Test ").append(i + 1).append(": ");

                if (!verdict.equals("Accepted")) {
                    outputLog.append(verdict);
                    if (!compileOutput.isEmpty()) outputLog.append(" | ").append(compileOutput);
                    if (!stderr.isEmpty()) outputLog.append(" | ").append(stderr);
                    finalVerdict = verdict;
                    if (s.getMode().equals("submit")) {
                        outputLog.append("\n");
                        s.setErrorOutput(compileOutput.isEmpty() ? stderr : compileOutput);
                        break;
                    }
                } else {
                    if (stdout.equals(expectedOutput)) {
                        outputLog.append("Passed ✓");
                        passedTests++;
                    } else {
                        outputLog.append("Wrong Answer | Expected: [").append(expectedOutput)
                                 .append("] Got: [").append(stdout).append("]");
                        finalVerdict = "Wrong Answer";
                        if (s.getMode().equals("submit")) {
                            outputLog.append("\n");
                            break;
                        }
                    }
                }
                outputLog.append("\n");
            }

            s.setVerdict(finalVerdict);
            s.setRuntime(totalRuntime / tests.length);
            s.setMemory(totalMemory);
            s.setOutput(outputLog.toString());
            s.setStatus("DONE");

        } catch (Exception e) {
            s.setVerdict("Internal Error");
            s.setStatus("DONE");
            s.setErrorOutput(e.getMessage());
        }

        repository.save(s);
    }

    /**
     * Injects a runner around the user's code so it reads from stdin.
     * The problem: given numbers on line 1 and target on line 2, output indices.
     * User writes: def solve(nums, target): ...
     */
    private String buildRunner(String userCode, String stdin) {
        // Parse stdin to pass as args
        String[] lines = stdin.split("\n");
        String numsLine = lines[0];
        String targetLine = lines.length > 1 ? lines[1] : "0";

        return userCode + "\n\n" +
               "import sys\n" +
               "lines = \"\"\"" + stdin + "\"\"\".strip().split('\\n')\n" +
               "nums = list(map(int, lines[0].split()))\n" +
               "target = int(lines[1])\n" +
               "result = solve(nums, target)\n" +
               "print(' '.join(map(str, result)))\n";
    }

    public Optional<Submission> getById(Long id) {
        return repository.findById(id);
    }

    public List<Submission> getByUsername(String username) {
        return repository.findByUsernameOrderBySubmittedAtDesc(username);
    }
}
