package com.wallet.ewallet.dto;

import lombok.Data;

@Data
public class RegisterRequest {

    private String email;
    private String phone;
    private String password;

}