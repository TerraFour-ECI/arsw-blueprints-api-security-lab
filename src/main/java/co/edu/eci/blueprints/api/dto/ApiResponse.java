package co.edu.eci.blueprints.api.dto;

public record ApiResponse<T>(int code, String message, T data) {}
