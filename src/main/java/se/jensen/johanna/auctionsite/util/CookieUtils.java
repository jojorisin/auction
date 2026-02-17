package se.jensen.johanna.auctionsite.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {
    @Value("${app.jwt.refresh-expiration-ms}")
    private Long refreshExpirationMs;

    @Value("${app.cookie.same-site}")
    private String sameSite;

    @Value("${app.cookie-secure}")
    private Boolean cookieSecure;

    /**
     * httpOnly(boolean) stops JavaScript theft and makes the cookie invisible
     * secure(boolean) makes sure cookies are only sent through secure connections (HTTPS)
     * path(String path) makes sure the browser will only return the cookie to urls that starts with the path
     * maxAge(long seconds) defines the expiration of the cookie. maxAge of 0 och negative will be removed instantly
     * sameSite(String Strict/Lax/None) defines whether the cookie will be sent across domains. Lax is standard
     *
     * @param refreshToken refreshToken string to wrap in a cookie
     * @return Returns refreshToken wrapped in a cookie
     */
    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .maxAge(refreshExpirationMs)
                .httpOnly(true)
                .path("/")
                .sameSite(sameSite)
                .secure(cookieSecure)
                .build();
    }

    /**
     * Cleans the cookie and returns an empty cookie
     *
     * @return An expired cookie used to tell the browser to delete the refreshToken
     */
    public ResponseCookie getCleanResponseCookie() {
        return ResponseCookie.from("refreshToken", "")
                .maxAge(0)
                .httpOnly(true)
                .path("/auth")
                .secure(cookieSecure)
                .build();

    }

}
