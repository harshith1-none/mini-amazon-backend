package com.harshith.mini_amazon_backend.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    // FIXED (previously): this bean used to be `return new ObjectMapper();`
    // with no modules registered. A bare ObjectMapper doesn't know how to
    // serialize java.time types like LocalDateTime, so any response
    // containing one (every GlobalExceptionHandler error body, every Order)
    // would throw InvalidDefinitionException mid-write - which is why error
    // JSON used to cut off right after `"timestamp":`.
    //
    // I initially "fixed" this by deleting the bean entirely, assuming
    // Spring Boot's autoconfigured ObjectMapper would still be available for
    // injection elsewhere - it wasn't, which is why startup then failed with
    // UnsatisfiedDependencyException on JwtAuthenticationEntryPoint's
    // constructor. This bean needs to exist AND be correctly configured.
    //
    // registerModule(new JavaTimeModule()) teaches Jackson how to
    // (de)serialize LocalDateTime/LocalDate/etc. Disabling
    // WRITE_DATES_AS_TIMESTAMPS makes it write them as readable ISO-8601
    // strings ("2026-08-06T14:35:58.349") instead of a numeric epoch array,
    // which is what you want in JSON responses.
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
