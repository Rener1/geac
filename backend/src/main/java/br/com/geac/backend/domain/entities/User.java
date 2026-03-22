package br.com.geac.backend.domain.entities;

import br.com.geac.backend.domain.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User implements UserDetails {
    private static final String ROLE_ADMIN_AUTHORITY = "ROLE_ADMIN";
    private static final String ROLE_PROFESSOR_AUTHORITY = "ROLE_PROFESSOR";
    private static final String ROLE_STUDENT_AUTHORITY = "ROLE_STUDENT";
    private static final String ROLE_ORGANIZER_AUTHORITY = "ROLE_ORGANIZER";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", length = 150, nullable = false)
    private String name;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    private Role role;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == Role.ADMIN) {
            return createAuthorities(
                    ROLE_ADMIN_AUTHORITY,
                    ROLE_PROFESSOR_AUTHORITY,
                    ROLE_STUDENT_AUTHORITY,
                    ROLE_ORGANIZER_AUTHORITY
            );
        } else if (this.role == Role.ORGANIZER) {
            return createAuthorities(ROLE_STUDENT_AUTHORITY, ROLE_ORGANIZER_AUTHORITY);
        } else if (this.role == Role.PROFESSOR) {
            return createAuthorities(ROLE_PROFESSOR_AUTHORITY, ROLE_STUDENT_AUTHORITY);
        } else {
            return createAuthorities(ROLE_STUDENT_AUTHORITY);
        }
    }

    private List<SimpleGrantedAuthority> createAuthorities(String... roles) {
        return List.of(roles).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
