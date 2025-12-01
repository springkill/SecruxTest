package com.secrux.api.dto;

import java.util.ArrayList;
import java.util.List;

public class ReflectionCommandRequest {
    private List<String> command = new ArrayList<>();

    public List<String> getCommand() {
        return command;
    }

    public void setCommand(List<String> command) {
        this.command = command;
    }
}
