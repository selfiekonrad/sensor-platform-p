package com.ceniuch.sensormanagementservice.config;

import com.ceniuch.common.config.CommonConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonConfig.class})
public class ManagementConfig {
}
