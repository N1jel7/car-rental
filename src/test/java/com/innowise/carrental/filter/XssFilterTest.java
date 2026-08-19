package com.innowise.carrental.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class XssFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private ServletResponse response;

    @Mock
    private FilterChain chain;

    private final XssFilter xssFilter = new XssFilter();

    @Test
    void doFilter_scriptTagInParameter_escapesHtmlBeforePassingDownTheChain() throws Exception {
        // given
        when(request.getParameter("comment")).thenReturn("<script>alert(1)</script>");

        ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);

        // when
        xssFilter.doFilter(request, response, chain);

        // then
        verify(chain).doFilter(captor.capture(), eq(response));
        HttpServletRequest wrapped = (HttpServletRequest) captor.getValue();
        assertEquals("&lt;script&gt;alert(1)&lt;/script&gt;", wrapped.getParameter("comment"));
    }

    @Test
    void doFilter_ampersandsQuotesAndApostrophes_areEscaped() throws Exception {
        // given
        when(request.getParameter("name")).thenReturn("Tom & Jerry's \"show\"");

        ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);

        // when
        xssFilter.doFilter(request, response, chain);

        // then
        verify(chain).doFilter(captor.capture(), eq(response));
        HttpServletRequest wrapped = (HttpServletRequest) captor.getValue();
        assertEquals("Tom &amp; Jerry&#x27;s &quot;show&quot;", wrapped.getParameter("name"));
    }

    @Test
    void doFilter_parameterValues_areEscapedElementByElement() throws Exception {
        // given
        when(request.getParameterValues("tags")).thenReturn(new String[]{"<b>bold</b>", "plain"});

        ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);

        // when
        xssFilter.doFilter(request, response, chain);

        // then
        verify(chain).doFilter(captor.capture(), eq(response));
        HttpServletRequest wrapped = (HttpServletRequest) captor.getValue();
        assertArrayEquals(
                new String[]{"&lt;b&gt;bold&lt;/b&gt;", "plain"},
                wrapped.getParameterValues("tags"));
    }

    @Test
    void doFilter_missingParameter_returnsNullWithoutFailing() throws Exception {
        // given
        when(request.getParameter("missing")).thenReturn(null);

        ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);

        // when
        xssFilter.doFilter(request, response, chain);

        // then
        verify(chain).doFilter(captor.capture(), eq(response));
        HttpServletRequest wrapped = (HttpServletRequest) captor.getValue();
        assertNull(wrapped.getParameter("missing"));
    }

}
