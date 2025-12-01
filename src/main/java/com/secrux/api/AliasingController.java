package com.secrux.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secrux.aliasing.AliasingAmbiguity;

@RestController
@RequestMapping("/api/aliasing")
public class AliasingController {

    private final AliasingAmbiguity aliasingAmbiguity;

    public AliasingController(AliasingAmbiguity aliasingAmbiguity) {
        this.aliasingAmbiguity = aliasingAmbiguity;
    }

    @GetMapping
    public List<String> execute() {
        return aliasingAmbiguity.run();
    }
}
