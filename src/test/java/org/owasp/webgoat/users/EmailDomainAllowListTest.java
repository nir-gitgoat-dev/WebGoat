package org.owasp.webgoat.users;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class EmailDomainAllowListTest {

  @Test
  void shouldAllowExactConfiguredDomain() {
    Set<String> allowedDomains = EmailDomainAllowList.parseAllowedDomains("example.com");

    assertThat(EmailDomainAllowList.extractLocalPartIfAllowed("alice@example.com", allowedDomains))
        .contains("alice");
  }

  @Test
  void shouldMatchDomainsCaseInsensitivelyAndIgnoreOuterWhitespace() {
    Set<String> allowedDomains = EmailDomainAllowList.parseAllowedDomains("example.com");

    assertThat(
            EmailDomainAllowList.extractLocalPartIfAllowed(
                " alice@Example.COM ", allowedDomains))
        .contains("alice");
  }

  @Test
  void shouldRejectNonAllowlistedDomain() {
    Set<String> allowedDomains = EmailDomainAllowList.parseAllowedDomains("example.com");

    assertThat(EmailDomainAllowList.extractLocalPartIfAllowed("alice@blocked.com", allowedDomains))
        .isEmpty();
  }

  @Test
  void shouldRejectSuffixMatch() {
    Set<String> allowedDomains = EmailDomainAllowList.parseAllowedDomains("example.com");

    assertThat(
            EmailDomainAllowList.extractLocalPartIfAllowed(
                "alice@notexample.com", allowedDomains))
        .isEmpty();
  }

  @Test
  void shouldRejectMissingOrInvalidEmailAndEmptyAllowList() {
    assertThat(
            EmailDomainAllowList.extractLocalPartIfAllowed(
                "alice", EmailDomainAllowList.parseAllowedDomains("example.com")))
        .isEmpty();
    assertThat(
            EmailDomainAllowList.extractLocalPartIfAllowed(
                "alice@example.com", EmailDomainAllowList.parseAllowedDomains("")))
        .isEmpty();
  }
}
