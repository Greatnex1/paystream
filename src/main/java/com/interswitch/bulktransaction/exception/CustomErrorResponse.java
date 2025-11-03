package com.interswitch.bulktransaction.exception;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
public class CustomErrorResponse {

    private String message;
    private int status;
    private LocalDateTime timestamp = LocalDateTime.now();

    public CustomErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public CustomErrorResponse(String message) {
        this.message = message;}

}

