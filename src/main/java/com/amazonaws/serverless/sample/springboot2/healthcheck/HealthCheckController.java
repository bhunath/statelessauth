package com.amazonaws.serverless.sample.springboot2.healthcheck;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
@EnableWebMvc
public class HealthCheckController {
    private static final Log log = LogFactory.getLog(HealthCheckController.class);

    static int hitcount = 0;

    @RequestMapping(path = "/health", method = RequestMethod.GET)
    public String monitorHealth(){
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.error(e.getMessage());
        }
        hitcount ++;
        String response = "System IP Address : " +
                (localhost.getHostAddress()).trim() + " Hit Count : " +hitcount;
        return response;
    }
}
