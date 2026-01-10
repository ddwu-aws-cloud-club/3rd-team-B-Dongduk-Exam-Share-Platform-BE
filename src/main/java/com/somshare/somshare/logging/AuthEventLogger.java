package com.somshare.somshare.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthEventLogger {

    @EventListener
    public void onLoginSuccess(AuthenticationSuccessEvent e) {
        String username = e.getAuthentication().getName();
        log.info("AUTH login_success username={}", username);
    }

    @EventListener
    public void onBadCredentials(AuthenticationFailureBadCredentialsEvent e) {
        String username = String.valueOf(e.getAuthentication().getPrincipal());
        log.warn("AUTH login_failed reason=bad_credentials username={}", username);
    }
}
