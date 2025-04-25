package com.example.hrms.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

@Slf4j
public class RequestUtils {

    /**
     *
     *
     * @return HttpServletRequest httpServletRequest 객체
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(requestAttributes)
                || !(requestAttributes instanceof ServletRequestAttributes)) {
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    /**
     *
     *
     * @return HttpServletResponse
     */
    public static HttpServletResponse getResponse() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(requestAttributes)
                || !(requestAttributes instanceof ServletRequestAttributes)) {
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getResponse();
    }

    /**
     *
     *
     * @param name 헤더 명
     * @return String 헤더 값
     */
    public static String getRequestHeader(String name) {
        HttpServletRequest httpServletRequest = getRequest();
        if (httpServletRequest != null) {
            return httpServletRequest.getHeader(name);
        } else {
            return null;
        }
    }


    /**
     *
     *
     * @return
     */
    public static String getUserAgent() {
        return Objects.requireNonNull(getRequest()).getHeader("User-Agent");
    }

    /**
     * r
     *
     * @param name 쿠키 명
     * @return String 쿠키 값
     */
    public static String getRequestCookie(String name) {
        try {
            return Arrays.stream(Objects.requireNonNull(getRequest()).getCookies())
                    .filter(c -> c.getName().equals(name))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        } catch (NullPointerException e) {
            log.debug("COOKIE_IS_EMPTY");
        }
        return null;
    }

    public static Cookie[] getRequestCookie() {
        try {
            return Objects.requireNonNull(getRequest()).getCookies();
        } catch (NullPointerException e) {
            log.debug("COOKIE_IS_EMPTY");
        }
        return null;
    }

    public static Enumeration<String> getRequestHeaders() {
        return Objects.requireNonNull(getRequest()).getHeaderNames();
    }

    public static String getRemoteAddr() {
        return Objects.requireNonNull(getRequest()).getRemoteAddr();
    }

    //Session
    public static void setSessionAttr(String name, Object value) {
         Objects.requireNonNull(getRequest()).getSession().setAttribute(name, value);
    }

    public static Object getSessionAttr(String name) {
        return Objects.requireNonNull(getRequest()).getSession().getAttribute(name);
    }

    public static String getSessionId() {
        return Objects.requireNonNull(getRequest()).getSession().getId();
    }

    public static HttpSession session(boolean create) {
        return Objects.requireNonNull(getRequest()).getSession(create);
    }

    public static void invalidSession() {
        HttpSession session = Objects.requireNonNull(getRequest()).getSession(false);
        if (Objects.nonNull(session)) {
            session.invalidate();
        }
    }

    public static String reformParameter(String param) {

        if (param == null) {
            return "";
        }

        return param.replaceAll("'", "\\'").replaceAll("\"", "\\\"");
    }

    public static Map<String, String> parseParametes(String parametes) {
        Map<String, String> query_pairs = new HashMap<String, String>();
        String[] pairs = parametes.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(pair.substring(0, idx), pair.substring(idx + 1));
        }
        return query_pairs;
    }

    public static String parseAgentInfo(String userAgent) {

        String os;
        if (userAgent == null || userAgent.isEmpty()) {
            os = "";
        } else if (userAgent.toLowerCase().contains("windows")) {
            os = "Windows OS";
        } else if (userAgent.toLowerCase().contains("mac")) {
            os = "Mac OS";
        } else if (userAgent.toLowerCase().contains("x11")) {
            os = "Unix OS";
        } else if (userAgent.toLowerCase().contains("android")) {
            os = "Android OS";
        } else if (userAgent.toLowerCase().contains("iphone")) {
            os = "IPhone OS";
        } else {
            os = "Unknown";
        }

        return os;

    }

    public static String getRangeIp(String ip1, String ip2) {
        String mIp1 = ip1 == null || ip1.equals("null") ? "" : ip1;
        String mIp2 = ip2 == null || ip2.equals("null") ? "" : ip2;
        String mIp = "";
        if (!mIp1.isEmpty() && !mIp2.isEmpty()) {
            mIp = mIp1 + "-" + mIp2;
        } else if (!mIp1.isEmpty()) {
            mIp = mIp1;
        } else if (!mIp2.isEmpty()) {
            mIp = mIp2;
        }
        return mIp;
    }

}
