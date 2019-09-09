/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.utils.coder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.emitter.Emitter;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.resolver.Resolver;
import org.yaml.snakeyaml.serializer.Serializer;

/**
 * YAML encoder and decoder using the "standard" mechanism, which is currently gson. All
 * of the methods perform conversion to/from YAML (instead of JSON).
 */
public class StandardYamlCoder extends StandardCoder {

    /**
     * Constructs the object.
     */
    public StandardYamlCoder() {
        super();
    }

    @Override
    protected String toJson(Object object) {
        StringWriter output = new StringWriter();
        toJson(output, object);
        return output.toString();
    }

    @Override
    protected void toJson(Writer target, Object object) {
        DumperOptions dumper = new DumperOptions();
        Serializer serializer = new Serializer(new Emitter(target, dumper), new Resolver(), dumper, null);

        try {
            serializer.open();
            serializer.serialize(makeYaml(toJsonTree(object)));
            serializer.close();

        } catch (IOException e) {
            throw new YAMLException(e);
        }
    }

    @Override
    protected <T> T fromJson(String yaml, Class<T> clazz) {
        Node node = new Yaml().compose(new StringReader(yaml));
        return fromJson(makeJson(node), clazz);
    }

    @Override
    protected <T> T fromJson(Reader source, Class<T> clazz) {
        Node node = new Yaml().compose(source);
        return fromJson(makeJson(node), clazz);
    }

    /**
     * Converts an arbitrary gson element into a corresponding Yaml node.
     *
     * @param jel gson element to be converted
     * @return a yaml node corresponding to the element
     */
    private Node makeYaml(JsonElement jel) {
        if (jel.isJsonArray()) {
            return makeYamlSequence((JsonArray) jel);

        } else if (jel.isJsonObject()) {
            return makeYamlMap((JsonObject) jel);

        } else if (jel.isJsonPrimitive()) {
            return makeYamlPrim((JsonPrimitive) jel);

        } else {
            return new ScalarNode(Tag.NULL, "", null, null, DumperOptions.ScalarStyle.PLAIN);
        }
    }

    /**
     * Converts an arbitrary gson array into a corresponding Yaml sequence.
     *
     * @param jel gson element to be converted
     * @return a yaml node corresponding to the element
     */
    private Node makeYamlSequence(JsonArray jel) {
        List<Node> nodes = new ArrayList<>(jel.size());
        jel.forEach(item -> nodes.add(makeYaml(item)));

        return new SequenceNode(Tag.SEQ, true, nodes, null, null, DumperOptions.FlowStyle.AUTO);
    }

    /**
     * Converts an arbitrary gson object into a corresponding Yaml map.
     *
     * @param jel gson element to be converted
     * @return a yaml node corresponding to the element
     */
    private Node makeYamlMap(JsonObject jel) {
        List<NodeTuple> nodes = new ArrayList<>(jel.size());

        for (Entry<String, JsonElement> entry : jel.entrySet()) {
            Node key = new ScalarNode(Tag.STR, entry.getKey(), null, null, DumperOptions.ScalarStyle.PLAIN);
            Node value = makeYaml(entry.getValue());

            nodes.add(new NodeTuple(key, value));
        }

        return new MappingNode(Tag.MAP, true, nodes, null, null, DumperOptions.FlowStyle.AUTO);
    }

    /**
     * Converts an arbitrary gson primitive into a corresponding Yaml scalar.
     *
     * @param jel gson element to be converted
     * @return a yaml node corresponding to the element
     */
    private Node makeYamlPrim(JsonPrimitive jel) {
        Tag tag;
        if (jel.isNumber()) {
            Class<? extends Number> clazz = jel.getAsNumber().getClass();

            if (clazz == Double.class || clazz == Float.class) {
                tag = Tag.FLOAT;

            } else {
                tag = Tag.INT;
            }

        } else if (jel.isBoolean()) {
            tag = Tag.BOOL;

        } else {
            tag = Tag.STR;
        }

        return new ScalarNode(tag, jel.getAsString(), null, null, DumperOptions.ScalarStyle.PLAIN);
    }

    /**
     * Converts an arbitrary Yaml node into a corresponding gson element.
     *
     * @param node node to be converted
     * @return a gson element corresponding to the node
     */
    private JsonElement makeJson(Node node) {
        if (node instanceof MappingNode) {
            return makeJsonObject((MappingNode) node);

        } else if (node instanceof SequenceNode) {
            return makeJsonArray((SequenceNode) node);

        } else {
            return makeJsonprimitive((ScalarNode) node);
        }

        // yaml doesn't appear to use anchor nodes when decoding
    }

    /**
     * Converts a Yaml sequence into a corresponding gson array.
     *
     * @param node node to be converted
     * @return a gson element corresponding to the node
     */
    private JsonElement makeJsonArray(SequenceNode node) {
        List<Node> nodes = node.getValue();
        JsonArray array = new JsonArray(nodes.size());

        for (Node subnode : nodes) {
            array.add(makeJson(subnode));
        }

        return array;
    }

    /**
     * Converts a Yaml map into a corresponding gson object.
     *
     * @param node node to be converted
     * @return a gson element corresponding to the node
     */
    private JsonElement makeJsonObject(MappingNode node) {
        JsonObject obj = new JsonObject();

        for (NodeTuple tuple : node.getValue()) {
            Node key = tuple.getKeyNode();
            String skey = ((ScalarNode) key).getValue();

            obj.add(skey, makeJson(tuple.getValueNode()));
        }

        return obj;
    }

    /**
     * Converts a Yaml scalar into a corresponding gson primitive.
     *
     * @param node node to be converted
     * @return a gson element corresponding to the node
     */
    private JsonElement makeJsonprimitive(ScalarNode node) {
        try {
            Tag tag = node.getTag();

            if (tag == Tag.INT) {
                return new JsonPrimitive(Long.valueOf(node.getValue()));

            } else if (tag == Tag.FLOAT) {
                return new JsonPrimitive(Double.valueOf(node.getValue()));

            } else if (tag == Tag.BOOL) {
                return new JsonPrimitive(Boolean.valueOf(node.getValue()));

            } else if (tag == Tag.NULL) {
                return JsonNull.INSTANCE;

            } else {
                return new JsonPrimitive(node.getValue());
            }

        } catch (NumberFormatException ex) {
            return new JsonPrimitive(node.getValue());
        }
    }
}
