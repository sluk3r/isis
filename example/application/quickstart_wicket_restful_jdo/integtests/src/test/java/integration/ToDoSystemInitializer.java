/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package integration;

import app.ToDoItemAnalysis;
import dom.todo.ToDoItemContributions;
import dom.todo.ToDoItemSubscriptions;
import dom.todo.ToDoItems;
import fixture.todo.ToDoItemsFixturesService;

import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.services.classdiscovery.ClassDiscoveryServiceUsingReflections;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.integtestsupport.IsisSystemForTest;
import org.apache.isis.core.metamodel.services.bookmarks.BookmarkServiceDefault;
import org.apache.isis.core.runtime.services.memento.MementoServiceDefault;
import org.apache.isis.core.wrapper.WrapperFactoryDefault;
import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusPersistenceMechanismInstaller;
import org.apache.isis.objectstore.jdo.datanucleus.IsisConfigurationForJdoIntegTests;
import org.apache.isis.objectstore.jdo.datanucleus.service.eventbus.EventBusServiceJdo;
import org.apache.isis.objectstore.jdo.datanucleus.service.support.IsisJdoSupportImpl;

/**
 * Holds an instance of an {@link IsisSystemForTest} as a {@link ThreadLocal} on the current thread,
 * initialized with ToDo app's domain services. 
 */
public class ToDoSystemInitializer {
    
    private ToDoSystemInitializer(){}

    public static IsisSystemForTest initIsft() {
        IsisSystemForTest isft = IsisSystemForTest.getElseNull();
        if(isft == null) {
            isft = new ToDoSystemBuilder().build().setUpSystem();
            IsisSystemForTest.set(isft);
        }
        return isft;
    }

    private static class ToDoSystemBuilder extends IsisSystemForTest.Builder {

        public ToDoSystemBuilder() {
            //withFixtures( ... reference data fixtures ...); // if we had any...
            withLoggingAt(org.apache.log4j.Level.INFO);
            with(testConfiguration());
            with(new DataNucleusPersistenceMechanismInstaller());
            
            withServices(
                    new ToDoItems(),
                    new ToDoItemAnalysis(),
                    new ToDoItemContributions(),
                    new ToDoItemsFixturesService(),
                    new ClassDiscoveryServiceUsingReflections(),
                    new ToDoItemSubscriptions(),
                    new WrapperFactoryDefault(),
                    new IsisJdoSupportImpl(),
                    new Bulk.InteractionContext(),
                    new EventBusServiceJdo(),
                    new QueryResultsCache(),
                    new MementoServiceDefault(),
                    new BookmarkServiceDefault()
                    );
        }

        private static IsisConfiguration testConfiguration() {
            final IsisConfigurationForJdoIntegTests testConfiguration = new IsisConfigurationForJdoIntegTests();
            testConfiguration.addRegisterEntitiesPackagePrefix("dom");
            return testConfiguration;
        }
    }
}
