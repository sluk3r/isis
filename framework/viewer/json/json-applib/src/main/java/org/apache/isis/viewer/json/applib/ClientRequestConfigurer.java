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
package org.apache.isis.viewer.json.applib;

import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.apache.isis.viewer.json.applib.RestfulRequest.RequestParameter;
import org.apache.isis.viewer.json.applib.links.LinkRepresentation;
import org.apache.isis.viewer.json.applib.util.UrlEncodingUtils;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.specimpl.UriBuilderImpl;


/**
 * Configures the body, query string etc of a {@link ClientRequest}.
 * 
 * <p>
 * Needed because, unfortunately, {@link ClientRequest} does not seem to allow the
 * query string to be set directly (only {@link ClientRequest#getQueryParameters() query parameters}).
 * Instead, it is necessary to {@link UriBuilderImpl#replaceQuery(String) use} its underlying 
 * {@link UriBuilderImpl}.  
 */
public class ClientRequestConfigurer {
    
    public static ClientRequestConfigurer create(ClientExecutor executor, String uriTemplate) {
        final UriBuilder uriBuilder = new UriBuilderImpl().uriTemplate(uriTemplate);
        final ClientRequest clientRequest = executor.createRequest(uriBuilder);
        return new ClientRequestConfigurer(clientRequest, uriBuilder);
    }

    private final ClientRequest clientRequest;
    private final UriBuilder uriBuilder;

    ClientRequestConfigurer(final ClientRequest clientRequest, final UriBuilder uriBuilder) {
        this.clientRequest = clientRequest;
        this.uriBuilder = uriBuilder;
    }

    public ClientRequestConfigurer accept(MediaType mediaType) {
        clientRequest.accept(mediaType);
        return this;
    }

    public ClientRequestConfigurer header(String name, String value) {
        clientRequest.header(name, value);
        return this;
    }

    /**
     * Prerequisite to {@link #configureArgs(JsonRepresentation)} or {@link #configureArgs(Map)}.
     */
    public ClientRequestConfigurer setHttpMethod(HttpMethod2 httpMethod2) {
        clientRequest.setHttpMethod(httpMethod2.getJavaxRsMethod());
        return this;
    }

    /**
     * Used when creating a request with arguments to execute.
     * 
     * <p>
     * Typical flow is:
     * <ul>
     * <li> {@link RestfulClient#createRequest(HttpMethod2, String)}
     * <li> {@link RestfulRequest#withArg(RequestParameter, Object)} for each arg
     * <li> {@link RestfulRequest#execute()} - which calls this method.
     * </ul>
     */
    public ClientRequestConfigurer configureArgs(Map<RequestParameter<?>, Object> args) {
        if(clientRequest.getHttpMethod() == null) {
            throw new IllegalStateException("Must set up http method first");
        }
        
        JsonRepresentation argsAsMap = JsonRepresentation.newMap();
        for (RequestParameter<?> requestParam : args.keySet()) {
            put(args, requestParam, argsAsMap);
        }
        getHttpMethod().setUpArgs(this, argsAsMap);
        return this;
    }

    private <P> void put(Map<RequestParameter<?>, Object> args, RequestParameter<P> requestParam, JsonRepresentation argsAsMap) {
        @SuppressWarnings("unchecked")
        final P value = (P)args.get(requestParam);
        final String valueStr = requestParam.getParser().asString(value);
        argsAsMap.mapPut(requestParam.getName(), valueStr);
    }

    /**
     * Used when following links ({@link RestfulClient#follow(LinkRepresentation)}).
     */
    public ClientRequestConfigurer configureArgs(JsonRepresentation requestArgs) {
        if(clientRequest.getHttpMethod() == null) {
            throw new IllegalStateException("Must set up http method first");
        }

        getHttpMethod().setUpArgs(this, requestArgs);
        return this;
    }

    /**
     * Called back from {@link HttpMethod2#setUpArgs(ClientRequestConfigurer, JsonRepresentation)}
     */
    ClientRequestConfigurer body(JsonRepresentation requestArgs) {
        clientRequest.body(MediaType.APPLICATION_JSON_TYPE, requestArgs.toString());
        return this;
    }

    /**
     * Called back from {@link HttpMethod2#setUpArgs(ClientRequestConfigurer, JsonRepresentation)}
     */
    ClientRequestConfigurer queryString(JsonRepresentation requestArgs) {
        if(requestArgs.size() == 0) {
            return this;
        } 
        final String queryString = UrlEncodingUtils.asUrlEncoded(requestArgs.toString());
        uriBuilder.replaceQuery(queryString);
        return this;
    }

    /**
     * Called back from {@link HttpMethod2#setUpArgs(ClientRequestConfigurer, JsonRepresentation)}
     */
    ClientRequestConfigurer queryArgs(JsonRepresentation requestArgs) {
        final MultivaluedMap<String, String> queryParameters = clientRequest.getQueryParameters();
        for(Map.Entry<String, JsonRepresentation> entry: requestArgs.mapIterable()) {
            final String param = entry.getKey();
            final JsonRepresentation argRepr = entry.getValue();
            final String arg = UrlEncodingUtils.asUrlEncoded(argRepr.asArg());
            queryParameters.add(param, arg);
        }
        return this;
    }

    /**
     * For testing.
     */
    ClientRequest getClientRequest() {
        return clientRequest;
    }

    HttpMethod2 getHttpMethod() {
        final String httpMethod = clientRequest.getHttpMethod();
        return HttpMethod2.valueOf(httpMethod);
    }


}