package com.aimentor.dto;

public class CodeRequest {
    private String code;
    private String mode; // NEW

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

