package org.owasp.webgoat.container.users;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Function;
import org.assertj.core.api.Assertions;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private UserTrackerRepository userTrackerRepository;
  @Mock private JdbcTemplate jdbcTemplate;
  @Mock private Function<String, Flyway> flywayLessons;

  @Test
  void shouldLoadUserUsingAllowedEmailLogin() {
    var user = new WebGoatUser("guest", "password");
    when(userRepository.findByUsername("guest")).thenReturn(user);
    UserService userService =
        new UserService(
            userRepository, userTrackerRepository, jdbcTemplate, flywayLessons, List.of(), "owasp.org");

    var loadedUser = userService.loadUserByUsername("guest@OWASP.ORG");

    Assertions.assertThat(loadedUser.getUsername()).isEqualTo("guest");
    verify(userRepository).findByUsername(eq("guest"));
  }

  @Test
  void shouldRejectLoginWhenDomainIsNotAllowed() {
    UserService userService =
        new UserService(
            userRepository, userTrackerRepository, jdbcTemplate, flywayLessons, List.of(), "owasp.org");

    Assertions.assertThatThrownBy(() -> userService.loadUserByUsername("guest@blocked.org"))
        .isInstanceOf(UsernameNotFoundException.class);
    verify(userRepository, never()).findByUsername(any());
  }

  @Test
  void shouldStoreLocalPartWhenAddingUserWithEmailAddress() {
    UserService userService =
        new UserService(
            userRepository, userTrackerRepository, jdbcTemplate, flywayLessons, List.of(), "owasp.org");
    ArgumentCaptor<WebGoatUser> userCaptor = ArgumentCaptor.forClass(WebGoatUser.class);
    when(userRepository.existsByUsername("guest")).thenReturn(true);

    userService.addUser("guest@owasp.org", "password");

    verify(userRepository).existsByUsername("guest");
    verify(userRepository).save(userCaptor.capture());
    Assertions.assertThat(userCaptor.getValue().getUsername()).isEqualTo("guest");
  }

  @Test
  void shouldThrowExceptionWhenUserIsNotFound() {
    when(userRepository.findByUsername(any())).thenReturn(null);
    UserService userService =
        new UserService(
            userRepository, userTrackerRepository, jdbcTemplate, flywayLessons, List.of(), "owasp.org");
    Assertions.assertThatThrownBy(() -> userService.loadUserByUsername("unknown@owasp.org"))
        .isInstanceOf(UsernameNotFoundException.class);
  }
}
