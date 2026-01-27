package com.user.JWT;

import com.user.Entity.User;
import com.user.Enum.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

	@Value("${jwt.secretkey}")
	private String jwtSecretKey;

	private SecretKey getSecretKey(){
		return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
	}

	// The thing we're done as we have seen in image that is in Notion :https://www.notion.so/JWT-JSON-Web-Token-2c2ad6030d4280c7b38fde1b377ae0b6?source=copy_link#2ecad6030d4280f68772f8e32cf31862
	public String getSubjectFromToken(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(getSecretKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
		return claims.getSubject();
	}

	public String generateUserToServiceToken(User user, Role activeRole){
		return Jwts.builder()
				.subject(user.getEmail())
				.claim("userId", user.getId())
				.claim("role", activeRole.name())
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis()+1000*60*60*48)) //2 Days
				.signWith(getSecretKey())
				.compact();
	}

	public String generateServiceToServiceToken(String serviceName) {
		return Jwts.builder()
				.subject(serviceName)
				.claim("type", "SERVICE")
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis()+1000*60*60*48)) //2 days
				.signWith(getSecretKey())
				.compact();
	}

	public boolean isServiceToken(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(getSecretKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();

		return "SERVICE".equals(claims.get("type"));
	}

}
