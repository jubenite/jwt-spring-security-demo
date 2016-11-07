package org.tsystems.demojwt.test.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.tsystems.demojwt.proxy.security.JwtTokenTool;

/**
 * Created by t-systems.
 */
public class JwtTokenUtilTest {

    private JwtTokenTool jwtTokenUtil;

    @Before
    public void init() {
        jwtTokenUtil = new JwtTokenTool();
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", 3600000L);
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", "mySecret");
    }

    @Test
    public void testGenerateTokenGeneratesDifferentTokensForDifferentCreationDates() throws Exception {
        final Map<String, Object> claims = createClaims();
        final String token = jwtTokenUtil.generateToken(claims);

        final Map<String, Object> claimsForLaterToken = createClaims();
        final String laterToken = jwtTokenUtil.generateToken(claimsForLaterToken);

        assertThat(token).isNotEqualTo(laterToken);
    }

    private Map<String, Object> createClaims() {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put(JwtTokenTool.CLAIM_KEY_USERNAME, "testUser");
        claims.put(JwtTokenTool.CLAIM_KEY_AUDIENCE, "testAudience");
        return claims;
    }

}