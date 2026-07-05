package com.store_ops_backend.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
public class VapidProperties {

    @Value("${push.vapid.public-key:}")
    private String publicKey;

    @Value("${push.vapid.private-key:}")
    private String privateKey;

    @Value("${push.vapid.subject:}")
    private String subject;

    public boolean isConfigured() {
        return publicKey != null && !publicKey.isBlank()
            && privateKey != null && !privateKey.isBlank()
            && subject != null && !subject.isBlank();
    }
}
