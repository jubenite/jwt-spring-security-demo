package org.tsystems.demojwt.proxy.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenTool implements Serializable {

	private static final long serialVersionUID = -3301605591108950415L;

	public static final String CLAIM_KEY_USERNAME = "sub";
	public static final String CLAIM_KEY_AUDIENCE = "aud";
	public static final String CLAIM_KEY_ROLES = "rol";

	private static final String AUDIENCE_WEB = "web";

	@Value("${jwt.expiration:3600}")
	private Long expiration;

	@Value("${jwt.secret}")
	protected String secret;

	public String getUsernameFromToken(String token) {
		String username;
		try {
			final Claims claims = getClaimsFromToken(token);
			username = claims.getSubject();
		} catch (Exception e) {
			username = null;
		}
		return username;
	}

	public Date getCreatedDateFromToken(String token) {
		Date created;
		try {
			final Claims claims = getClaimsFromToken(token);
			created = claims.getIssuedAt();
		} catch (Exception e) {
			created = null;
		}
		return created;
	}

	public Date getExpirationDateFromToken(String token) {
		Date expiration;
		try {
			final Claims claims = getClaimsFromToken(token);
			expiration = claims.getExpiration();
		} catch (Exception e) {
			expiration = null;
		}
		return expiration;
	}

	public String getAudienceFromToken(String token) {
		String audience;
		try {
			final Claims claims = getClaimsFromToken(token);
			audience = (String) claims.get(CLAIM_KEY_AUDIENCE);
		} catch (Exception e) {
			audience = null;
		}
		return audience;
	}

	public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
		List<GrantedAuthority> authorities = null;
		try {
			final Claims claims = getClaimsFromToken(token);
			if (claims != null)
				authorities = convertList((ArrayList<?>) claims.get(CLAIM_KEY_ROLES), s -> transform(s));
		} catch (Exception e) {
			authorities = null;
		}
		return authorities;
	}

	private <T, U> List<U> convertList(List<T> from, Function<T, U> func) {
		return from.stream().map(func).collect(Collectors.toList());
	}

	private GrantedAuthority transform(Object s) {
		LinkedHashMap<?, ?> map = (LinkedHashMap<?, ?>) s;
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority((String) map.get("authority"));
		return authority;
	}

	protected Claims getClaimsFromToken(String token) {
		Claims claims;
		try {
			claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
		} catch (Exception e) {
			claims = null;
		}
		return claims;
	}

	public Boolean validateToken(String token) {
		return (!isTokenExpired(token));
	}

	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
		claims.put(CLAIM_KEY_ROLES, userDetails.getAuthorities());
		claims.put(CLAIM_KEY_AUDIENCE, generateAudience());
		return generateToken(claims);
	}

	public String generateToken(Map<String, Object> claims) {
		return Jwts.builder().setClaims(claims).setExpiration(generateExpirationDate()).setIssuedAt(new Date())
				.signWith(SignatureAlgorithm.HS256, secret).compact();
	}

	public Boolean canTokenBeRefreshed(String token, Date lastPasswordReset) {
		final Date created = getCreatedDateFromToken(token);
		return !isCreatedBeforeLastPasswordReset(created, lastPasswordReset) && (!isTokenExpired(token));
	}

	public String refreshToken(String token) {
		String refreshedToken;
		try {
			final Claims claims = getClaimsFromToken(token);
			refreshedToken = generateToken(claims);
		} catch (Exception e) {
			refreshedToken = null;
		}
		return refreshedToken;
	}

	private Date generateExpirationDate() {
		return new Date(System.currentTimeMillis() + expiration * 1000);
	}

	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
		return (lastPasswordReset != null && created.before(lastPasswordReset));
	}

	private String generateAudience() {
		return AUDIENCE_WEB;
	}
}