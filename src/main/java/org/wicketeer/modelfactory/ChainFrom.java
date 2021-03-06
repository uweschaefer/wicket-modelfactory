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

/**
 * 
 */
package org.wicketeer.modelfactory;

import org.apache.wicket.MetaDataKey;

class ChainFrom extends RequestCycleLocal<Object> {
    private static final MetaDataKey<Object> key = new Key();

    static class Key extends MetaDataKey<Object> {
        private static final long serialVersionUID = 1L;
    };


    public ChainFrom() {
        super(key);
    }

    @Override
    public void set(final Object value) {
        Reference ref = (Reference) super.get();
        if (ref != null) {
            super.remove();
            throw new IllegalStateException(
                    "mutliple from() calls. need to call model(); Original invokation at "
                            + render(ref.getInvokationPath()));
        }
        super.set(new Reference(Preconditions.checkNotNull(value)));
    }

    private String render(final Exception invokationPath) {
        StackTraceElement[] st = invokationPath.getStackTrace();
        for (StackTraceElement stackTraceElement : st) {
            String cn = stackTraceElement.getClassName();
            if (!cn.contains(ModelFactory.class.getSimpleName()) && !(cn
                    .contains(ModelFactory.class.getPackage().getName()))) {
                String mn = stackTraceElement.getMethodName();
                int ln = stackTraceElement.getLineNumber();
                String scn = cn.substring(cn.lastIndexOf('.') + 1);
                return scn + "." + mn + " (" + scn + ":" + ln + ")";
            }
        }
        invokationPath.printStackTrace();
        return "";
    }

    @Override
    public Object get() {
        Reference ref = (Reference) super.get();
        if (ref == null) {
            super.remove();
            throw new IllegalStateException(
                    "no from() call registered before!");
        }

        return ref.getObject();
    }
}