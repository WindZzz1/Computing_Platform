package com.yupi.springbootinit.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 *
 * @author YU
 */
@Component
public class JwtUtil {

    /**
     * 密钥
     */
    private static final String SECRET = "your-32byte-long-secure-key-12345678";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    /**
     * Token有效期：7天（单位：毫秒）
     */
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000;

    /**
     * 生成Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param roleCode 角色编码
     * @return JWT Token
     */
    public String generateToken(Long userId, String username, String roleCode) {
        Map<String, Object> claims = new HashMap<>(); //jwt的payload，存储自定义信息
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roleCode", roleCode);
        return createToken(claims);
    }

    /**
     * 创建Token
     *
     * @param claims 自定义声明
     * @return JWT Token
     */
    private String createToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setClaims(claims) //payload
                .setIssuedAt(now) //生成时间
                .setExpiration(expirationDate) //过期时间
                .signWith(SECRET_KEY,SignatureAlgorithm.HS256) //HS256加密算法
                .compact(); //合并
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = extractClaims(token);
        return claims != null ? claims.get("userId", Long.class) : null;
    }

    /**
     * 从Token中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = extractClaims(token);
        return claims != null ? claims.get("username", String.class) : null;
    }

    /**
     * 从Token中获取角色编码
     *
     * @param token JWT Token
     * @return 角色编码
     */
    public String getRoleCodeFromToken(String token) {
        Claims claims = extractClaims(token);
        return claims != null ? claims.get("roleCode", String.class) : null;
    }

    /**
     * 验证Token是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims != null && !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 提取Token中的Claims
     *
     * @param token JWT Token
     * @return Claims
     */
    private Claims extractClaims(String token) {
        try {
            return Jwts.parserBuilder()  //jwt解析器
                    .setSigningKey(SECRET_KEY)
                    .build()  //构造完成
                    .parseClaimsJws(token)  //把 token 拆成三部分（头、载荷、签名）
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断Token是否过期
     *
     * @param claims Claims
     * @return 是否过期
     */
    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 获取Token过期时间
     *
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = extractClaims(token);
        return claims != null ? claims.getExpiration() : null;
    }
}
