package com.example.demo.cinema.dto;

public class CommonResult {

    private boolean success;
    private String message;

    public CommonResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static CommonResult success() {
        return new CommonResult(true, "Đăng ký thành công!");
    }

    public static CommonResult failure(String message) {
        return new CommonResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
