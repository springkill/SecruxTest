package com.secrux.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.secrux.lambda.LambdaLabyrinth;

@RestController
@RequestMapping("/api/lambda")
public class LambdaController {

    private final LambdaLabyrinth lambdaLabyrinth;

    public LambdaController(LambdaLabyrinth lambdaLabyrinth) {
        this.lambdaLabyrinth = lambdaLabyrinth;
    }

    @GetMapping
    public List<String> execute(@RequestParam(value = "optionalInput", required = false) String optionalInput) throws Exception {
        return lambdaLabyrinth.run(optionalInput);
    }
}
