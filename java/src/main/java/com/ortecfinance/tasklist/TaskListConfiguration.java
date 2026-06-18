package com.ortecfinance.tasklist;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the framework-agnostic core into the Spring context.
 *
 * <p>Registering {@link TaskService} as a bean here keeps the core class free of
 * any framework annotations, so it stays usable by the console as a plain object.
 */
@Configuration
public class TaskListConfiguration {

    @Bean
    public TaskService taskService() {
        return new TaskService();
    }
}
