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
package org.apache.isis.applib.annotation;

/**
 * The available policies as to whether action invocations are reified into commands.
 *
 * <p>
 *     Note: this enum is <i>not</i> an inner class of the {@link org.apache.isis.applib.annotation.Action} annotation
 *     because in the future we may also support commands for {@link org.apache.isis.applib.annotation.Property} and
 *     {@link org.apache.isis.applib.annotation.Collection}.
 * </p>
 */
public enum CommandReification {
    /**
     * Whether the action should be handled as a command is per the default command configured in <tt>isis.properties</tt>.
     *
     * <p>
     *     If no command policy is configured, then the action is <i>not</i> treated as a command.
     * </p>
     */
    AS_CONFIGURED,
    /**
     * Handle the action as a command, irrespective of any configuration settings.
     */
    ENABLED,
    /**
     * Do not handle the action as a command, irrespective of any configuration settings.
     */
    DISABLED,
    /**
     * Ignore the value provided by this annotation (meaning that the framework will keep searching, in meta
     * annotations or superclasses/interfaces).
     */
    NOT_SPECIFIED
}
