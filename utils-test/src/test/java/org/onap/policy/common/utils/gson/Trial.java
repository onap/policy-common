/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.common.utils.gson;

import java.io.FileReader;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrapFactory;

public class Trial {

    @Test
    public void test() throws Exception {
        Context context = Context.enter();
        ScriptableObject scope = context.initStandardObjects();
        WrapFactory wrapFactory = new WrapFactory() {
            @Override
            public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class<?> staticType) {
                if (javaObject instanceof CharSequence || javaObject instanceof Number
                                || javaObject instanceof Boolean) {
                    return ScriptRuntime.toObject(cx, scope, javaObject);
                }

                // TODO handle list and map

                throw new UnsupportedOperationException("wrap3");
            }
        };
        context.setWrapFactory(wrapFactory);
        // NativeJavaObject obj = new NativeJavaObject(scope, "hello", String.class);
        // Scriptable obj = ScriptRuntime.toObject(context, scope, "hello");
        // Scriptable obj = ScriptRuntime.toObject(context, scope, Map.of("hello",
        // "world"));
        // String source = new StandardCoder().encode(Map.of("hello", List.of("world")));
        // Object obj = NativeJSON.parse(context, scope, source, (ctx,sc,th,args) ->
        // args[1]);
        // System.out.println("data=" + obj.getClass());
        // System.out.println("obj=" + NativeJSON.stringify(context, scope, obj, null,
        // null));
        // System.out.println("type=" + NativeJSON.parse(context, scope, "{}",
        // (cx,sc,th,args) -> args[1]).getClass());
        Script script = context.compileReader(new FileReader("src/test/resources/script.js"), "inline", 1, null);
        Object result = script.exec(context, scope);
        System.out.println("result=" + result);
        Context.exit();
    }
}
