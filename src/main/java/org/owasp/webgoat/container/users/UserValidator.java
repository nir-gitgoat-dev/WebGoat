package org.owasp.webgoat.container.users;

import java.util.Objects;
import java.util.Set;
import org.owasp.webgoat.users.EmailDomainAllowList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author nbaars
 * @since 3/19/17.
 */
@Component
public class UserValidator implements Validator {

  private final UserRepository userRepository;
  private final Set<String> allowedEmailDomains;

  public UserValidator(
      UserRepository userRepository,
      @Value("${webgoat.user.allowed-email-domains:}") String allowedEmailDomains) {
    this.userRepository = userRepository;
    this.allowedEmailDomains = EmailDomainAllowList.parseAllowedDomains(allowedEmailDomains);
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return UserForm.class.equals(clazz);
  }

  @Override
  public void validate(Object o, Errors errors) {
    UserForm userForm = (UserForm) o;

    if (!EmailDomainAllowList.isAllowed(userForm.getUsername(), allowedEmailDomains)) {
      errors.rejectValue("username", "username.invalid.domain");
    } else if (userRepository.findByUsername(
            EmailDomainAllowList.normalizeUsernameForStorage(userForm.getUsername()))
        != null) {
      errors.rejectValue("username", "username.duplicate");
    }

    if (!Objects.equals(userForm.getMatchingPassword(), userForm.getPassword())) {
      errors.rejectValue("matchingPassword", "password.diff");
    }
  }
}
