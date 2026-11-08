package com.dd01xc.service.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MaskingTest {

    @Test
    void ipv4HidesLastOctet() {
        assertThat(Masking.ip("203.0.113.42")).isEqualTo("203.0.113.x");
    }

    @Test
    void ipv6KeepsNetworkPrefixOnly() {
        assertThat(Masking.ip("2001:db8:85a3:0:0:8a2e:370:7334")).isEqualTo("2001:db8:85a3::x");
        assertThat(Masking.ip("::1")).isEqualTo("1::x");
    }

    @Test
    void unknownFormatsAreHidden() {
        assertThat(Masking.ip("UNKNOWN")).isEqualTo("hidden");
        assertThat(Masking.ip("::ffff:192.0.2.1")).isEqualTo("hidden");
        assertThat(Masking.ip(null)).isNull();
    }

    @Test
    void emailKeepsDomain() {
        assertThat(Masking.account("alice.smith@example.com")).isEqualTo("al***@example.com");
        assertThat(Masking.account("bob@example.com")).isEqualTo("b***@example.com");
        assertThat(Masking.account("@example.com")).isEqualTo("***@example.com");
    }

    @Test
    void usernameIsMasked() {
        assertThat(Masking.account("alice")).isEqualTo("al***");
        assertThat(Masking.account("al")).isEqualTo("a***");
        assertThat(Masking.account(null)).isNull();
    }
}
