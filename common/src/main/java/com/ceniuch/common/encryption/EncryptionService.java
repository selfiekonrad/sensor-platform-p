package com.ceniuch.common.encryption;

import com.ceniuch.common.exceptions.EncryptionException;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.daead.DeterministicAeadConfig;
import com.google.crypto.tink.internal.RegistryConfiguration;
import com.google.crypto.tink.subtle.Base64;
import lombok.extern.slf4j.Slf4j;

import com.google.crypto.tink.DeterministicAead;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

@Slf4j
@Service
public class EncryptionService {
    private final DeterministicAead deterministicAead;

    public EncryptionService() throws GeneralSecurityException, IOException {
        DeterministicAeadConfig.register();
        KeysetHandle keysetHandle = CleartextKeysetHandle.read(JsonKeysetReader.withInputStream(
                new ClassPathResource("api_key_keyset.json").getInputStream()));

        deterministicAead = keysetHandle.getPrimitive(RegistryConfiguration.get(), DeterministicAead.class);
    }

    public String decryptKeyFromDb(String apiKey) {
        if (apiKey == null) {
            return apiKey;
        }
        byte[] decoded = Base64.decode(apiKey);
        try {
            byte[] decrypted = deterministicAead.decryptDeterministically(decoded, new byte[0]);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            log.error("Could not decrypt api_key {}", apiKey, e);
            throw new EncryptionException(e);
        }
    }

    public String encryptKeyForDb(String apiKey) {
        if (apiKey == null) {
            return null;
        }
        try {
            byte[] encrypted = deterministicAead.encryptDeterministically(apiKey.getBytes(StandardCharsets.UTF_8), new byte[0]);
            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (GeneralSecurityException e) {
            log.error("Could not encrypt api_key {}", apiKey, e);
            throw new EncryptionException(e);
        }
    }
}