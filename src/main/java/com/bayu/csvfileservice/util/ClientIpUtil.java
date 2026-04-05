package com.bayu.csvfileservice.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class ClientIpUtil {

    public String getClientIp(HttpServletRequest servletRequest) {
        String clientIp = servletRequest.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = servletRequest.getHeader("X-Real-IP");
        }
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = servletRequest.getRemoteAddr();
        }
        log.info("Client IP Address: {}", clientIp);
        return clientIp;
    }

}
