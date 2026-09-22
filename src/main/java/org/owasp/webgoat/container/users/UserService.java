package org.owasp.webgoat.container.users;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.flywaydb.core.Flyway;
import org.owasp.webgoat.container.lessons.Initializeable;
import org.owasp.webgoat.users.EmailDomainAllowList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author nbaars
 * @since 3/19/17.
 */
@Service
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;
  private final UserTrackerRepository userTrackerRepository;
  private final JdbcTemplate jdbcTemplate;
  private final Function<String, Flyway> flywayLessons;
  private final List<Initializeable> lessonInitializables;
  private final Set<String> allowedEmailDomains;

  public UserService(
      UserRepository userRepository,
      UserTrackerRepository userTrackerRepository,
      JdbcTemplate jdbcTemplate,
      Function<String, Flyway> flywayLessons,
      List<Initializeable> lessonInitializables,
      @Value("${webgoat.user.allowed-email-domains:}") String allowedEmailDomains) {
    this.userRepository = userRepository;
    this.userTrackerRepository = userTrackerRepository;
    this.jdbcTemplate = jdbcTemplate;
    this.flywayLessons = flywayLessons;
    this.lessonInitializables = lessonInitializables;
    this.allowedEmailDomains = EmailDomainAllowList.parseAllowedDomains(allowedEmailDomains);
  }

  @Override
  public WebGoatUser loadUserByUsername(String username) throws UsernameNotFoundException {
    String normalizedUsername =
        EmailDomainAllowList.extractLocalPartIfAllowed(username, allowedEmailDomains)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    WebGoatUser webGoatUser = userRepository.findByUsername(normalizedUsername);
    if (webGoatUser == null) {
      throw new UsernameNotFoundException("User not found");
    } else {
      webGoatUser.createUser();
      lessonInitializables.forEach(l -> l.initialize(webGoatUser));
    }
    return webGoatUser;
  }

  public void addUser(String username, String password) {
    String normalizedUsername = EmailDomainAllowList.normalizeUsernameForStorage(username);
    // get user if there exists one by the name
    var userAlreadyExists = userRepository.existsByUsername(normalizedUsername);
    var webGoatUser = userRepository.save(new WebGoatUser(normalizedUsername, password));

    if (!userAlreadyExists) {
      userTrackerRepository.save(
          new UserTracker(
              normalizedUsername)); // if user previously existed it will not get another tracker
      createLessonsForUser(webGoatUser);
    }
  }

  private void createLessonsForUser(WebGoatUser webGoatUser) {
    jdbcTemplate.execute("CREATE SCHEMA \"" + webGoatUser.getUsername() + "\" authorization dba");
    flywayLessons.apply(webGoatUser.getUsername()).migrate();
  }

  public List<WebGoatUser> getAllUsers() {
    return userRepository.findAll();
  }
}
