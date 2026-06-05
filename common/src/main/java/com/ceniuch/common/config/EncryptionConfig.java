package com.ceniuch.common.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@ImportRuntimeHints(EncryptionRuntimeHints.class)
@Configuration
class EncryptionConfig {}

class EncryptionRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader cl) {
        hints.resources().registerPattern("api_key_keyset.json");
    }
}