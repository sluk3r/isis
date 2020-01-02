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
package org.apache.isis.webapp.modules;

import java.util.EventListener;
import java.util.Optional;

import javax.servlet.*;

import org.apache.isis.applib.services.inject.ServiceInjector;

public abstract class WebModuleAbstract implements WebModule {

    private final ServiceInjector serviceInjector;

    protected WebModuleContext webModuleContext;

    protected WebModuleAbstract(ServiceInjector serviceInjector) {
        this.serviceInjector = serviceInjector;
    }

    @Override
    public void prepare(final WebModuleContext ctx) {
        this.webModuleContext = ctx;
    }


    protected Optional<FilterRegistration.Dynamic> registerFilter(
            final ServletContext ctx,
            final String filterName,
            final Class<? extends Filter> filterCls) throws ServletException {
        final Filter filter = ctx.createFilter(filterCls);
        final FilterRegistration.Dynamic filterReg = ctx.addFilter(filterName, filter);

        if(filterReg != null) {
            serviceInjector.injectServicesInto(filter);
        }
        return Optional.ofNullable(filterReg);
    }

    protected Optional<ServletRegistration.Dynamic> registerServlet(
            final ServletContext ctx,
            final String servletName,
            final Class<? extends Servlet> servletCls) throws ServletException {
        final Servlet servlet = ctx.createServlet(servletCls);
        final ServletRegistration.Dynamic servletReg = ctx.addServlet(servletName, servlet);
        if(servletReg != null) {
            serviceInjector.injectServicesInto(servlet);
        }
        return Optional.ofNullable(servletReg);
    }

    protected ServletContextListener createListener(
            final ServletContext ctx,
            final Class<? extends ServletContextListener> listenerCls) throws ServletException {
        final ServletContextListener listener = ctx.createListener(listenerCls);
        // we don't add the listener here, they are all added at the end.
        serviceInjector.injectServicesInto(listener);
        return listener;
    }

}
