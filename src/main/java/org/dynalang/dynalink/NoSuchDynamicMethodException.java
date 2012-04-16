/*
   Copyright 2009-2012 Attila Szegedi

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.dynalang.dynalink;

import org.dynalang.dynalink.linker.GuardingDynamicLinker;

/**
 * Thrown at the invocation if the call site can not be linked by any available {@link GuardingDynamicLinker}. (Maybe
 * just use NoAccess?)
 *
 * @author Attila Szegedi
 * @version $Id: $
 */
public class NoSuchDynamicMethodException extends RuntimeException {
    private static final long serialVersionUID = 1L;
}