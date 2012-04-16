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

package org.dynalang.dynalink.beans;

import org.dynalang.dynalink.DynamicLinkerFactory;
import org.dynalang.dynalink.linker.CallSiteDescriptor;
import org.dynalang.dynalink.linker.GuardedInvocation;
import org.dynalang.dynalink.linker.GuardingDynamicLinker;
import org.dynalang.dynalink.linker.LinkRequest;
import org.dynalang.dynalink.linker.LinkerServices;

/**
 * A linker for POJOs. Normally used as the ultimate fallback linker by the {@link DynamicLinkerFactory} so it is given
 * the chance to link calls to all objects that no other language runtime recognizes. Specifically, this linker will:
 * <ul>
 * <li>expose all methods of form {@code setXxx()}, {@code getXxx()}, and {@code isXxx()} as property setters and
 * getters for {@code dyn:setProp} and {@code dyn:getProp} operations;</li>
 * <li>expose all methods for {@code dyn:callPropWithThis} operation;</li>
 * <li>expose all fields as properties, unless there are getters or setters for the properties of the same name;</li>
 * <li>expose {@code dyn:getLength}, {@code dyn:getItem} and {@code dyn:setItem} on native Java arrays, as well as
 * {@link java.util.List} and {@link java.util.Map} objects; ({@code dyn:getLength} works on any
 * {@link java.util.Collection});</li>
 * <li>expose {@code dyn:new} on instances of {@link java.lang.Class} as calls to constructors;</li>
 * <li>expose static methods, fields, and properties of classes as an object accessible through virtual property named
 * {@code statics} on instances of {@link java.lang.Class}.</li>
 * </ul>
 *  Overloaded method resolution is handled for property setters, methods, and constructors. Variable argument
 *  invocation is handled for methods and constructors. Currently, only public fields and methods are supported.
 *
 * @author Attila Szegedi
 * @version $Id: $
 */
public class BeansLinker implements GuardingDynamicLinker {
    private static final ClassValue<GuardingDynamicLinker> linkers = new ClassValue<GuardingDynamicLinker>() {
        @Override
        protected GuardingDynamicLinker computeValue(Class<?> clazz) {
            return clazz == Class.class ? new ClassLinker() : clazz == ClassStatics.class ? new ClassStaticsLinker() :
                new BeanLinker(clazz);
        }
    };

    /**
     * Creates a new POJO linker.
     */
    public BeansLinker() {
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest request, final LinkerServices linkerServices)
            throws Exception {
        final Object[] arguments = request.getArguments();
        if(arguments == null || arguments.length == 0) {
            // Can't handle static calls; must have a receiver
            return null;
        }

        final CallSiteDescriptor callSiteDescriptor = request.getCallSiteDescriptor();
        final int l = callSiteDescriptor.getNameTokenCount();
        // All names conforming to the dynalang MOP should have at least two tokens, the first one being "dyn"
        if(l < 2 || !"dyn".equals(callSiteDescriptor.getNameToken(0))) {
            return null;
        }

        final Object receiver = arguments[0];
        if(receiver == null) {
            // Can't operate on null
            return null;
        }
        return linkers.get(receiver.getClass()).getGuardedInvocation(request, linkerServices);
    }
}