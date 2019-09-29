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
package org.apache.isis.wrapper;

import java.util.EnumSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.isis.applib.services.wrapper.AsyncWrap;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.WrapperFactory.ExecutionMode;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.runtime.system.context.IsisContext;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.With;
import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
@AllArgsConstructor
class AsyncWrapDefault<T> implements AsyncWrap<T> {
    
    @With(AccessLevel.PACKAGE) @NonNull
    private WrapperFactory wrapper;
    
    @With(AccessLevel.PACKAGE) @NonNull
    private T domainObject;
    
    /*getter is API*/
    @Getter @With(AccessLevel.PACKAGE) @NonNull
    private EnumSet<ExecutionMode> executionMode;
    
    /*getter, wither are API*/
    @Getter @With(AccessLevel.PUBLIC) @NonNull
    private ExecutorService executor;  
    
    /*getter, wither are API*/
    @Getter @With(AccessLevel.PUBLIC) @NonNull
    private Consumer<Exception> exceptionHandler;  
    

    @Override
    public <R> Future<R> invoke(Invoke0<? super T, ? extends R> action) {
        
        if(shouldValidate()) {
            // do validation synchronous (with the calling thread)
            val proxy_validateOnly = wrapper.wrap(domainObject, ExecutionMode.NO_EXECUTE);
            action.invoke(proxy_validateOnly);
        }
        
        if(shouldExecute()) {
            return submit(()->action.invoke(domainObject));  // no proxy required
        }
        
        return CompletableFuture.completedFuture(null);
    }


    @Override
    public <R, A1> Future<R> invoke(Invoke1<? super T, ? extends R, A1> action, A1 arg1) {
        
        if(shouldValidate()) {
            // do validation synchronous (with the calling thread)
            val proxy_validateOnly = wrapper.wrap(domainObject, ExecutionMode.NO_EXECUTE);
            action.invoke(proxy_validateOnly, arg1);
        }
        
        if(shouldExecute()) {
            // to also trigger domain events, we need a proxy, but validation (if required)
            // was already done above
            val proxy_executeOnly = wrapper.wrap(domainObject, ExecutionMode.SKIP_RULES);
            return submit(()->action.invoke(proxy_executeOnly, arg1));
        }
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public <R, A1, A2> Future<R> invoke(Invoke2<? super T, ? extends R, A1, A2> action, A1 arg1, A2 arg2) {

        if(shouldValidate()) {
            // do validation synchronous (with the calling thread)
            val proxy_validateOnly = wrapper.wrap(domainObject, ExecutionMode.NO_EXECUTE);
            action.invoke(proxy_validateOnly, arg1, arg2);
        }
        
        if(shouldExecute()) {
            // to also trigger domain events, we need a proxy, but validation (if required)
            // was already done above
            val proxy_executeOnly = wrapper.wrap(domainObject, ExecutionMode.SKIP_RULES);
            return submit(()->action.invoke(proxy_executeOnly, arg1, arg2));
        }
        
        return CompletableFuture.completedFuture(null);
    }
    

    @Override
    public <R, A1, A2, A3> Future<R> invoke(Invoke3<? super T, ? extends R, A1, A2, A3> action, 
            A1 arg1, A2 arg2, A3 arg3) {
        
        if(shouldValidate()) {
            // do validation synchronous (with the calling thread)
            val proxy_validateOnly = wrapper.wrap(domainObject, ExecutionMode.NO_EXECUTE);
            action.invoke(proxy_validateOnly, arg1, arg2, arg3);
        }
        
        if(shouldExecute()) {
         // to also trigger domain events, we need a proxy, but validation (if required)
            // was already done above
            val proxy_executeOnly = wrapper.wrap(domainObject, ExecutionMode.SKIP_RULES);
            return submit(()->action.invoke(proxy_executeOnly, arg1, arg2, arg3));
        }
        
        return CompletableFuture.completedFuture(null);
    }


    @Override
    public <R, A1, A2, A3, A4> Future<R> invoke(Invoke4<? super T, ? extends R, A1, A2, A3, A4> action, 
            A1 arg1, A2 arg2, A3 arg3, A4 arg4) {

        if(shouldValidate()) {
            // do validation synchronous (with the calling thread)
            val proxy_validateOnly = wrapper.wrap(domainObject, ExecutionMode.NO_EXECUTE);
            action.invoke(proxy_validateOnly, arg1, arg2, arg3, arg4);
        }
        
        if(shouldExecute()) {
            // to also trigger domain events, we need a proxy, but validation (if required)
            // was already done above
            val proxy_executeOnly = wrapper.wrap(domainObject, ExecutionMode.SKIP_RULES);
            return submit(()->action.invoke(proxy_executeOnly, arg1, arg2, arg3, arg4));
        }
        
        return CompletableFuture.completedFuture(null);
    }
    

    // -- SUBMISSION
    
    private <R> Future<R> submit(Supplier<R> actionInvocation) {
        
        val transactionService = IsisContext.getTransactionService();
        val authenticationSession = IsisContext.getAuthenticationSession().get();
        //val transactionLatch = IsisTransactionAspectSupport.transactionLatch();
        
        Callable<R> asyncTask = ()->{

            try {
                //transactionLatch.await(); // wait for transaction of the calling thread to complete

                return IsisContext.getSessionFactory().doInSession(
                        ()->transactionService.executeWithinTransaction(actionInvocation),
                        authenticationSession);

            } catch (Exception e) {

//              val wrappedMethod = (Method) 
//              proxy.getClass().getMethod("__isis_wrappedMethod", _Constants.emptyClasses);
      
                val msg = 
                        String.format("Async execution of action '%s' on type '%s' failed.",
                                "[cannot resolve method name - not implemented]",//wrappedMethod.getName(),
                                domainObject.getClass());
                
                
                ;
                
                exceptionHandler.accept(_Exceptions.unrecoverable(msg, e));

                return null;
            }
        };
        
        return executor.submit(asyncTask);
    }
    
    // -- HELPER
    
    private boolean shouldValidate() {
        return !executionMode.contains(ExecutionMode.SKIP_RULE_VALIDATION);
    }
    
    private boolean shouldExecute() {
        return !executionMode.contains(ExecutionMode.SKIP_EXECUTION);
    }
    


}