package com.ceniuch.common.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class ValidatorLoggingRuntimeHints implements RuntimeHintsRegistrar {

    private static final String[] GENERATED_TYPES = {
            "org.hibernate.validator.internal.util.logging.Log_$logger",
            "org.hibernate.validator.internal.util.logging.Messages_$bundle"
    };

    private static final String[] CONSTRAINT_VALIDATORS = {
            "org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator",
            "org.hibernate.validator.internal.constraintvalidators.bv.NotBlankValidator"
    };

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        for (String type : GENERATED_TYPES) {
            hints.reflection().registerTypeIfPresent(classLoader, type,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    // JBoss Logging reads Messages_$bundle.INSTANCE reflectively
                    // (Messages.getBundle() -> getField("INSTANCE").get(null)).
                    MemberCategory.ACCESS_DECLARED_FIELDS);
        }
        for (String type : CONSTRAINT_VALIDATORS) {
            hints.reflection().registerTypeIfPresent(classLoader, type,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        }
    }
}