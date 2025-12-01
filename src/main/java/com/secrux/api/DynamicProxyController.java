package com.secrux.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.secrux.dynamic.DynamicProxyShowcase;

@RestController
@RequestMapping("/api/dynamic-proxy")
public class DynamicProxyController {

    private final DynamicProxyShowcase dynamicProxyShowcase;

    public DynamicProxyController(DynamicProxyShowcase dynamicProxyShowcase) {
        this.dynamicProxyShowcase = dynamicProxyShowcase;
    }

    @GetMapping
    public List<String> execute(@RequestParam(value = "input", required = false) String input,
                                @RequestParam(value = "fallbackInput", required = false) String fallbackInput) throws Exception {
        return dynamicProxyShowcase.run(input, fallbackInput);
    }
}
