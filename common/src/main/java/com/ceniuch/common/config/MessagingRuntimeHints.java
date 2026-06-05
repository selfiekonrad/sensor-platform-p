package com.ceniuch.common.config;

import com.ceniuch.common.events.SensorDataEvent;
import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class MessagingRuntimeHints implements RuntimeHintsRegistrar {

    private final BindingReflectionHintsRegistrar bindingRegistrar = new BindingReflectionHintsRegistrar();

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Transitively covers SensorDataEvent plus its property types
        // (UUID, Instant, Float, and the SensorType / Unit enums).
        bindingRegistrar.registerReflectionHints(hints.reflection(), SensorDataEvent.class);
    }
}