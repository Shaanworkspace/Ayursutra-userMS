package com.user.JWT;

import com.user.Entity.User;
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

	public String generateToken(User user){
		return Jwts.builder()
				.subject(user.getEmail())
				.claim("userId", user.getId())
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis()+1000*60*60))
				.signWith(getSecretKey())
				.compact();
	}

	// The thing we're done as we have seen in image that is in Notion :https://www.notion.so/JWT-JSON-Web-Token-2c2ad6030d4280c7b38fde1b377ae0b6?source=copy_link#2ecad6030d4280f68772f8e32cf31862
	public String getUsernameByToken(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(getSecretKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
		return claims.getSubject();
	}
}
