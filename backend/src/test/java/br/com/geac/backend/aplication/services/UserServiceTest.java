package br.com.geac.backend.aplication.services;

import br.com.geac.backend.domain.entities.User;
import br.com.geac.backend.domain.enums.Role;
import br.com.geac.backend.infrastucture.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("loadUserByUsername deve retornar usuario quando email existe")
    void loadUserByUsername_UserFound() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@test.com");
        user.setRole(Role.STUDENT);

        when(userRepository.findByEmail("user@test.com")).thenReturn(user);

        assertThat(userService.loadUserByUsername("user@test.com")).isEqualTo(user);
    }

    @Test
    @DisplayName("loadUserByUsername deve lancar excecao quando usuario nao existe")
    void loadUserByUsername_UserNotFound() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(null);

        assertThatThrownBy(() -> userService.loadUserByUsername("missing@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing@test.com");
    }
}
