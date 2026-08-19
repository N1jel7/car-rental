package com.innowise.carrental.filter;

import com.innowise.carrental.entity.Role;
import com.innowise.carrental.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private FilterChain chain;

    private final AuthFilter authFilter = new AuthFilter();

    @Test
    void doFilter_noSession_redirectsToLoginAndStopsChain() throws Exception {
        // given
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/car-rental");

        // when
        authFilter.doFilter(request, response, chain);

        // then
        verify(response).sendRedirect("/car-rental/login");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_sessionWithoutUser_redirectsToLogin() throws Exception {
        // given
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AuthFilter.SESSION_USER)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/car-rental");

        // when
        authFilter.doFilter(request, response, chain);

        // then
        verify(response).sendRedirect("/car-rental/login");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_regularUserOnAdminPath_returnsForbiddenAndStopsChain() throws Exception {
        // given
        User regularUser = User.builder().role(Role.USER).build();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AuthFilter.SESSION_USER)).thenReturn(regularUser);
        when(request.getContextPath()).thenReturn("/car-rental");
        when(request.getRequestURI()).thenReturn("/car-rental/admin/cars");

        // when
        authFilter.doFilter(request, response, chain);

        // then
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_adminUserOnAdminPath_letsRequestThrough() throws Exception {
        // given
        User admin = User.builder().role(Role.ADMIN).build();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AuthFilter.SESSION_USER)).thenReturn(admin);
        when(request.getContextPath()).thenReturn("/car-rental");
        when(request.getRequestURI()).thenReturn("/car-rental/admin/cars");

        // when
        authFilter.doFilter(request, response, chain);

        // then
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_loggedInUserOnNonAdminPath_letsRequestThrough() throws Exception {
        // given
        User regularUser = User.builder().role(Role.USER).build();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AuthFilter.SESSION_USER)).thenReturn(regularUser);
        when(request.getContextPath()).thenReturn("/car-rental");
        when(request.getRequestURI()).thenReturn("/car-rental/bookings");

        // when
        authFilter.doFilter(request, response, chain);

        // then
        verify(chain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

}
