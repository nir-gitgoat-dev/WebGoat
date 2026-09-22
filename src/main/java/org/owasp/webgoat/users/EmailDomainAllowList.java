package org.owasp.webgoat.users;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public final class EmailDomainAllowList {

  private static final Pattern EMAIL_PATTERN = Pattern.compile("^([^\\s@]+)@([^\\s@]+)$");

  private EmailDomainAllowList() {}

  public static Set<String> parseAllowedDomains(String allowedDomains) {
    if (allowedDomains == null || allowedDomains.isBlank()) {
      return Collections.emptySet();
    }
    Set<String> domains = new LinkedHashSet<>();
    Arrays.stream(allowedDomains.split(","))
        .map(String::trim)
        .filter(domain -> !domain.isEmpty())
        .map(domain -> domain.toLowerCase(Locale.ROOT))
        .forEach(domains::add);
    return Collections.unmodifiableSet(domains);
  }

  public static Optional<String> extractLocalPartIfAllowed(
      String loginIdentifier, Set<String> allowedDomains) {
    if (allowedDomains.isEmpty()) {
      return Optional.empty();
    }
    return parseEmail(loginIdentifier)
        .filter(email -> allowedDomains.contains(email.domain()))
        .map(EmailAddress::localPart);
  }

  public static boolean isAllowed(String loginIdentifier, Set<String> allowedDomains) {
    return extractLocalPartIfAllowed(loginIdentifier, allowedDomains).isPresent();
  }

  public static String normalizeUsernameForStorage(String username) {
    return parseEmail(username)
        .map(EmailAddress::localPart)
        .orElse(username == null ? null : username.trim());
  }

  private static Optional<EmailAddress> parseEmail(String loginIdentifier) {
    if (loginIdentifier == null) {
      return Optional.empty();
    }
    String trimmed = loginIdentifier.trim();
    var matcher = EMAIL_PATTERN.matcher(trimmed);
    if (!matcher.matches()) {
      return Optional.empty();
    }
    return Optional.of(
        new EmailAddress(matcher.group(1), matcher.group(2).toLowerCase(Locale.ROOT)));
  }

  private record EmailAddress(String localPart, String domain) {}
}
