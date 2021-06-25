/*
 * Copyright 2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.amazonaws.serverless.sample.springboot2.controller;



import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;


@RestController
@EnableWebMvc
public class ServerlessController {

    @RequestMapping(path = "/serverless", method = RequestMethod.GET)
    public String serverless(@RequestParam("limit") Optional<Integer> limit, Principal principal) throws UnknownHostException {
        final InetAddress localHost = InetAddress.getLocalHost();
        return localHost.toString();
    }

}
