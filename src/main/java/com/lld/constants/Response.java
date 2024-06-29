package com.lld.constants;

public enum Response {
    OK("Ok"),
    NOT_FOUND("Not Found");

    private String value;

    Response(String s) {
        this.value  = s;
    }

    public String getValue() {
        return value;
    }
}
