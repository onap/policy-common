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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJSON;
import org.mozilla.javascript.NativeObject;
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

        /*
         * Note: this factory performs a deep conversion, which is necessary for
         * stringify() to work on a complex structure. In only works with the basic java
         * types (e.g., String, Number, List, and Map) - if support for Beans/POJOs is
         * needed, if would have to be enhanced.
         */
        WrapFactory wrapFactory = new WrapFactory() {
            @Override
            public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class<?> staticType) {

                if (javaObject instanceof CharSequence || javaObject instanceof Number
                                || javaObject instanceof Boolean) {
                    return ScriptRuntime.toObject(cx, scope, javaObject);

                } else if (javaObject instanceof List) {
                    List<?> lst = (List<?>) javaObject;
                    Object[] arr = lst.toArray();
                    for (int x = 0; x < arr.length; ++x) {
                        arr[x] = ScriptRuntime.toObject(cx, scope, arr[x]);
                    }
                    return new NativeArray(arr);

                } else if (javaObject instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, ?> map = (Map<String, ?>) javaObject;
                    NativeObject result = new NativeObject();
                    for (Entry<String, ?> entry : map.entrySet()) {
                        Scriptable value = ScriptRuntime.toObject(cx, scope, entry.getValue());
                        result.put(entry.getKey(), result, value);
                    }
                    return result;

                }

                // TODO handle Set type, if needed

                // TODO handle any Beans/POJOs, if needed

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

        ScriptableObject scriptable = context.initStandardObjects();
        Object obj = Context.javaToJS(List.of(Map.of("hello", 20)), scriptable);
        scope.put("abc", scope, obj);

        Script script = context.compileReader(new FileReader("src/test/resources/script.js"), "inline", 1, null);
        Object result = script.exec(context, scope);
        System.out.println("result=" + result);
        Context.exit();
    }
}
