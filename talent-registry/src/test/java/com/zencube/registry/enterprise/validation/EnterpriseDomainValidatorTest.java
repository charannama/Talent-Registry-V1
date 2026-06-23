package com.zencube.registry.enterprise.validation;

import com.zencube.registry.enterprise.config.EnterpriseProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnterpriseDomainValidatorTest {

    @Mock
    private EnterpriseProperties properties;

    @InjectMocks
    private EnterpriseDomainValidator validator;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(properties.getBlockedDomains()).thenReturn(List.of(
                "gmail.com",
                "yahoo.com",
                "hotmail.com",
                "outlook.com",
                "live.com"
        ));
    }

    @Test
    void shouldAcceptLegitimateEnterpriseDomains() {
        assertTrue(validator.isValidEnterpriseDomain("user@company.com"));
        assertTrue(validator.isValidEnterpriseDomain("ceo@startup.io"));
        assertTrue(validator.isValidEnterpriseDomain("hr@enterprise.co.uk"));
    }

    @Test
    void shouldRejectBlockedDomains() {
        assertFalse(validator.isValidEnterpriseDomain("user@gmail.com"));
        assertFalse(validator.isValidEnterpriseDomain("user@yahoo.com"));
        assertFalse(validator.isValidEnterpriseDomain("user@hotmail.com"));
    }

    @Test
    void shouldHandleCaseInsensitivity() {
        assertFalse(validator.isValidEnterpriseDomain("USER@GMAIL.COM"));
        assertFalse(validator.isValidEnterpriseDomain("user@Gmail.Com"));
        assertTrue(validator.isValidEnterpriseDomain("user@Company.Com"));
    }

    @Test
    void shouldHandleWhitespaceAttacks() {
        assertFalse(validator.isValidEnterpriseDomain(" user@gmail.com "));
        assertFalse(validator.isValidEnterpriseDomain("user@gmail.com\t"));
    }

    @Test
    void shouldHandleSubdomainAttacks() {
        // e.g. using mail.gmail.com to bypass gmail.com block
        assertFalse(validator.isValidEnterpriseDomain("user@mail.gmail.com"));
        assertFalse(validator.isValidEnterpriseDomain("user@sub.yahoo.com"));
        assertTrue(validator.isValidEnterpriseDomain("user@mail.company.com"));
    }

    @Test
    void shouldHandleUnicodeHomographAttacks() {
        // 'ɢ' (U+0262) instead of 'g' -> punycode is xn--mail-66a.com, not gmail.com
        // But if someone tried an exact look-alike that resolves to gmail.com via IDN (if possible), it would block.
        // Wait, if they use 'ɢmail.com', rawDomain="ɢmail.com", normalizedDomain="xn--mail-66a.com".
        // It won't match "gmail.com". But IDN normalisation handles standard casing and converting to ASCII.
        // We test that a domain that resolves to the exact same ASCII as gmail.com is blocked.
        // Actually, if we configure IDN for the block list too (we did), then exact homographs might block.
        // Let's test standard unicode upper-to-lower conversion handled by IDN
        // ℊ (U+210A) -> normalizes to g
        assertFalse(validator.isValidEnterpriseDomain("user@ℊmail.com"));
        // Ｙahoo.com (Fullwidth characters)
        assertFalse(validator.isValidEnterpriseDomain("user@Ｙahoo.com"));
    }

    @Test
    void shouldHandleInvalidEmailsGracefully() {
        assertFalse(validator.isValidEnterpriseDomain(null));
        assertFalse(validator.isValidEnterpriseDomain(""));
        assertFalse(validator.isValidEnterpriseDomain("   "));
        assertFalse(validator.isValidEnterpriseDomain("invalid_email"));
        assertFalse(validator.isValidEnterpriseDomain("user@"));
        assertFalse(validator.isValidEnterpriseDomain("@domain.com"));
    }
}
