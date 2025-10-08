package com.compiler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    @JsonProperty("access_token")
    private String token;

    @JsonProperty("token_type")
    private String type =  "Bearer";

    @JsonProperty("email")
    private String email;

    @JsonProperty("expires_in")
    private Long expiresIn;

    public AuthResponse(String token, String email, Long expiresIn) {
        this.token = token;
        this.email = email;
        this.expiresIn = expiresIn;
        this.type = "Bearer";
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "token='[PROTECTED]'" +
                ", type='" + type + '\'' +
                ", email='" + email + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
}
