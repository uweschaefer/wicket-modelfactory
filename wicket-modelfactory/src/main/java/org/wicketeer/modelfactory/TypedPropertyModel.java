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

import org.apache.wicket.model.IObjectClassAwareModel;
import org.apache.wicket.model.PropertyModel;

class TypedPropertyModel<S> extends PropertyModel<S> implements
        IObjectClassAwareModel<S> {
    private static final long serialVersionUID = 1L;
    private Class<S> type;

    TypedPropertyModel(Object t, String path, Class<S> type) {
        super(t, path);
        this.type = Preconditions.checkNotNull(type);
    }

    @Override
    public Class<S> getObjectClass() {
        return type;
    }
}
