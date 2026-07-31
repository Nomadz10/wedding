package com.wedding.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Guards /admin/** — everything except the login page requires an admin session flag. */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    public static final String ADMIN_SESSION_KEY = "ADMIN_AUTHENTICATED";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        // Allow the login page and its POST through unauthenticated.
        if (path.equals("/admin/login") || path.equals("/admin/logout")) {
            return true;
        }
        HttpSession session = request.getSession(false);
        boolean authed = session != null && Boolean.TRUE.equals(session.getAttribute(ADMIN_SESSION_KEY));
        if (authed) {
            return true;
        }
        response.sendRedirect("/admin/login");
        return false;
    }
}
