/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.extensions.cors.impl.webmodule;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.ebaysf.web.cors.CORSFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.webapp.modules.WebModuleAbstract;

import lombok.Getter;

/**
 * WebModule providing support for CORS
 * 
 * @since 2.0
 */
@Service
@Named("isisMapCors.WebModuleServerCors")
@Qualifier("CORS")
@Order(OrderPrecedence.HIGH)
public final class WebModuleCors extends WebModuleAbstract {

    private final static String CORS_FILTER_NAME = "CORS Filter";

    @Getter
    private final String name = "CORS";

    @Inject
    public WebModuleCors(ServiceInjector serviceInjector) {
        super(serviceInjector);
    }



    /*
    <filter>
        <filter-name>CORS Filter</filter-name>
        <filter-class>org.ebaysf.web.cors.CORSFilter</filter-class>
        <init-param>
            <param-name>cors.allowed.origins</param-name>
            <param-value>*</param-value>
        </init-param>
        <init-param>
            <param-name>cors.allowed.headers</param-name>
            <param-value>Content-Type,Accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization,Cache-Control,If-Modified-Since,Pragma</param-value>
        </init-param>
        <init-param>
            <param-name>cors.exposed.headers</param-name>
            <param-value>Authorization</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>CORS Filter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
     */
    @Override
    public Can<ServletContextListener> init(ServletContext ctx) throws ServletException {

        registerFilter(ctx, CORS_FILTER_NAME, CORSFilter.class)
            .ifPresent(filterReg -> {
                filterReg.setInitParameter("cors.allowed.origins", "*");
                filterReg.setInitParameter("cors.allowed.headers", "Content-Type,Accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization,Cache-Control,If-Modified-Since,Pragma");
                filterReg.setInitParameter("cors.exposed.headers", "Authorization");

                filterReg.addMappingForUrlPatterns(
                        null,
                        false,
                        this.webModuleContext.getProtectedPaths().toArray(String.class));

            });

        return Can.empty(); // registers no listeners
    }


}
