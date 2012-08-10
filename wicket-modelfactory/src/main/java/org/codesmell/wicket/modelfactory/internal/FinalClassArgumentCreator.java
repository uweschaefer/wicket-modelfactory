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

package org.codesmell.wicket.modelfactory.internal;

/**
 * A factpry for creating arguments placeholder for final classes
 * @author Mario Fusco
 */
public interface FinalClassArgumentCreator<T> {

    /**
     * Create a placeholder for an argument of the final class T using the given seed.
     * @param seed  The seed to generate the unique placeholder
     * @return A placeholder for an argument of class T
     */
    T createArgumentPlaceHolder(int seed);
}
