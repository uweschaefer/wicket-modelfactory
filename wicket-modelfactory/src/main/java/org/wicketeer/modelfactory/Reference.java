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

class Reference
{
    private final Object object;
    private final Exception invokationPath;

    Reference(final Object o)
    {
        object = checkNotNull(o);
        invokationPath = new Exception();
    }

    Object getObject()
    {
        return object;
    }

    Exception getInvokationPath()
    {
        return invokationPath;
    }

}
