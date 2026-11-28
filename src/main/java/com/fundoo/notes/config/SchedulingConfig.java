package com.fundoo.notes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/* Enables the periodic job that emails reminders when they become due. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
