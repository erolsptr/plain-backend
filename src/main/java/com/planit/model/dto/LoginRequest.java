// src/main/java/com/planit/model/dto/LoginRequest.java
package com.planit.model.dto;

// Bu, sadece e-posta ve şifre taşımak için kullanılan basit bir DTO'dur.
public record LoginRequest(String email, String password) {
}