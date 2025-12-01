package com.secrux.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secrux.unreachable.UnreachableTrap;

@RestController
@RequestMapping("/api/unreachable")
public class UnreachableController {

    private final UnreachableTrap unreachableTrap;

    public UnreachableController(UnreachableTrap unreachableTrap) {
        this.unreachableTrap = unreachableTrap;
    }

    @GetMapping
    public List<String> execute() {
        return unreachableTrap.run();
    }
}
