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

package org.apache.isis.core.progmodel.facets.object.hidden.method;

import java.lang.reflect.Method;

import org.apache.isis.applib.events.VisibilityEvent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.util.AdapterInvokeUtils;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.progmodel.facets.object.hidden.HiddenObjectFacetAbstract;

public class HiddenObjectFacetViaHiddenMethod extends HiddenObjectFacetAbstract {
    private final Method method;

    public HiddenObjectFacetViaHiddenMethod(Method method, FacetHolder holder) {
        super(holder);
        this.method = method;
    }

    @Override
    public String hides(VisibilityContext<? extends VisibilityEvent> ic) {
        final ObjectAdapter toValidate = ic.getTarget();
        return toValidate != null ? hiddenReason(toValidate) : null;
    }

    @Override
    public String hiddenReason(ObjectAdapter target) {
        if (target == null) {
            return null;
        }
        final Boolean isHidden = (Boolean) AdapterInvokeUtils.invoke(method, target);
        return isHidden.booleanValue() ? "Hidden" : null;
    }

    @Override
    public void copyOnto(FacetHolder holder) {
        final HiddenObjectFacetViaHiddenMethod clonedFacet = new HiddenObjectFacetViaHiddenMethod(this.method, holder);
        FacetUtil.addFacet(clonedFacet);
    }

}
