package br.com.geac.backend.api.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    @Test
    @DisplayName("corsConfigurationSource deve filtrar origens vazias e aplicar configuracao esperada")
    void corsConfigurationSource_Success() {
        CorsConfig corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(
                corsConfig,
                "allowedOrigins",
                "http://localhost:3000, , https://geac-frontend-ttod.onrender.com"
        );

        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        CorsConfiguration configuration =
                source.getCorsConfiguration(new MockHttpServletRequest("GET", "/events"));

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins())
                .containsExactly("http://localhost:3000", "https://geac-frontend-ttod.onrender.com");
        assertThat(configuration.getAllowedMethods())
                .containsExactly("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");
        assertThat(configuration.getAllowedHeaders()).containsExactly("*");
        assertThat(configuration.getAllowCredentials()).isFalse();
    }
}
