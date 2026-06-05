package com.ceniuch.common.encryption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EncryptionServiceTest {

    private EncryptionService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new EncryptionService();
    }

    @Test
    void encryptKeyForDb_thenDecryptKeyFromDb_returnsOriginal() {
        String original = "super-secret-api-key-12345";

        String encrypted = service.encryptKeyForDb(original);
        String decrypted = service.decryptKeyFromDb(encrypted);

        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    void encryptKeyForDb_sameInput_returnsSameCiphertext() {
        String input = "deterministic-key";

        String first = service.encryptKeyForDb(input);
        String second = service.encryptKeyForDb(input);

        assertThat(first).isEqualTo(second);
    }

    @Test
    void encryptKeyForDb_differentInputs_returnDifferentCiphertexts() {
        String a = service.encryptKeyForDb("key-a");
        String b = service.encryptKeyForDb("key-b");

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void encryptKeyForDb_nullInput_returnsNull() {
        assertThat(service.encryptKeyForDb(null)).isNull();
    }

    @Test
    void decryptKeyFromDb_nullInput_returnsNull() {
        assertThat(service.decryptKeyFromDb(null)).isNull();
    }

    @Test
    void encryptKeyForDb_outputIsNotCleartext() {
        String input = "another-key";

        String encrypted = service.encryptKeyForDb(input);

        assertThat(encrypted).doesNotContain(input);
    }
}
