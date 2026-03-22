package br.com.geac.backend.domain.entities;

import br.com.geac.backend.domain.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("getAuthorities deve retornar todas as roles herdadas para admin")
    void getAuthorities_AdminRole() {
        User user = buildUser(Role.ADMIN);

        assertThat(user.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_PROFESSOR", "ROLE_STUDENT", "ROLE_ORGANIZER");
    }

    @Test
    @DisplayName("getAuthorities deve retornar estudante e organizador para organizer")
    void getAuthorities_OrganizerRole() {
        User user = buildUser(Role.ORGANIZER);

        assertThat(user.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_STUDENT", "ROLE_ORGANIZER");
    }

    @Test
    @DisplayName("getAuthorities deve retornar professor e estudante para professor")
    void getAuthorities_ProfessorRole() {
        User user = buildUser(Role.PROFESSOR);

        assertThat(user.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_PROFESSOR", "ROLE_STUDENT");
    }

    @Test
    @DisplayName("getAuthorities deve retornar apenas estudante para student")
    void getAuthorities_StudentRole() {
        User user = buildUser(Role.STUDENT);

        assertThat(user.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_STUDENT");
        assertThat(user.getUsername()).isEqualTo("user@test.com");
        assertThat(user.getPassword()).isEqualTo("secret");
    }

    private User buildUser(Role role) {
        User user = new User();
        user.setRole(role);
        user.setEmail("user@test.com");
        user.setPassword("secret");
        return user;
    }
}
