package com.example.springreact.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Rate Limiting
 * Permite ajustar los límites desde application.properties
 */
@Configuration
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitConfig {
    
    // Máximo de intentos permitidos
    private int maxAttempts = 5;
    
    // Duración en minutos para el refill
    private int refillMinutes = 1;
    
    public int getMaxAttempts() {
        return maxAttempts;
    }
    
    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }
    
    public int getRefillMinutes() {
        return refillMinutes;
    }
    
    public void setRefillMinutes(int refillMinutes) {
        this.refillMinutes = refillMinutes;
    }
}
