//
//
// Copyright 2012-2012 Uwe Schäfer <uwe@codesmell.de>
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package org.wicketeer.modelfactory;

import static org.wicketeer.modelfactory.Preconditions.checkNotNull;

import org.apache.wicket.Application;
import org.apache.wicket.RuntimeConfigurationType;

/**
 * Bundles a Reference to an Object together with an Exception that can be used
 * to inform about the Reference's creation. This can be used while in Wicket's
 * DEVELOPEMT-Mode to find out, where this Reference was instantiated.
 *
 * @author uweschaefer
 */
class Reference {
    private final Object object;
    private final Exception invokationPath;
    private static volatile Boolean createExceptionForDebug = null;

    /**
     * @param objectToReference
     *            the object this Reference should point to.
     * @throws NullPointerException
     *             if the object to reference if null
     */
    protected Reference(final Object objectToReference) throws NullPointerException {
        object = checkNotNull(objectToReference);

        if (Reference.createExceptionForDebug == null) {
            Reference.createExceptionForDebug = RuntimeConfigurationType.DEVELOPMENT
                    .equals(Application.get().getConfigurationType());
        }

        if (Reference.createExceptionForDebug) {
            invokationPath = new Exception();
        }
        else {
            invokationPath = null;
        }
    }

    /**
     * @return the object passed in on creation
     */
    protected Object getObject() {
        return object;
    }

    /**
     * @return Exception that was create on creation or null, if called within
     *         RuntimeConfigurationType.DEPLOYMENT
     */
    protected Exception getInvokationPath() {
        return invokationPath;
    }

}
