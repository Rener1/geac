package br.com.geac.backend.aplication.services;

import br.com.geac.backend.domain.entities.User;
import br.com.geac.backend.domain.enums.Role;
import br.com.geac.backend.domain.exceptions.TokenGenerationException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.exceptions.JWTCreationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", "test-secret-key");
    }

    @Test
    @DisplayName("generateToken e validateToken devem funcionar com token valido")
    void generateAndValidateToken_Success() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@test.com");
        user.setName("User Test");
        user.setRole(Role.STUDENT);

        String token = tokenService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(tokenService.validateToken(token)).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("validateToken deve retornar vazio quando token for invalido")
    void validateToken_InvalidToken_ReturnsEmptyString() {
        assertThat(tokenService.validateToken("token-invalido")).isEmpty();
    }

    @Test
    @DisplayName("generateToken deve lancar TokenGenerationException quando biblioteca falha")
    void generateToken_WhenLibraryFails_ThrowsTokenGenerationException() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@test.com");
        user.setName("User Test");
        user.setRole(Role.STUDENT);

        JWTCreator.Builder builder = mock(JWTCreator.Builder.class, RETURNS_SELF);
        when(builder.sign(any())).thenThrow(new JWTCreationException("boom", null));

        try (MockedStatic<JWT> jwt = mockStatic(JWT.class)) {
            jwt.when(JWT::create).thenReturn(builder);

            assertThatThrownBy(() -> tokenService.generateToken(user))
                    .isInstanceOf(TokenGenerationException.class)
                    .hasMessage("Erro ao gerar token")
                    .hasCauseInstanceOf(JWTCreationException.class);
        }
    }
}
