package com.aimentor.dto;

public class HistoryResponse {

    private Long id;
    private String code;
    private String result;

    public HistoryResponse(Long id, String code, String result) {
        this.id = id;
        this.code = code;
        this.result = result;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getResult() { return result; }
}