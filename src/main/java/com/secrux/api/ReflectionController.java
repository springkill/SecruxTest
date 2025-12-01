package com.secrux.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secrux.api.dto.ReflectionCommandRequest;
import com.secrux.reflection.ReflectionMaze;

@RestController
@RequestMapping("/api/reflection")
public class ReflectionController {

    private final ReflectionMaze reflectionMaze;

    public ReflectionController(ReflectionMaze reflectionMaze) {
        this.reflectionMaze = reflectionMaze;
    }

    @PostMapping
    public Map<String, Object> execute(@RequestBody(required = false) ReflectionCommandRequest request) throws Exception {
        List<String> command = request != null && request.getCommand() != null
                ? request.getCommand()
                : Collections.emptyList();
        String result = reflectionMaze.run(command);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("command", command);
        payload.put("result", result);
        return payload;
    }
}
