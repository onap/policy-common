/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.http.server.test;

import com.google.gson.annotations.SerializedName;
import lombok.ToString;
import org.onap.policy.common.gson.annotation.GsonJsonProperty;

/**
 * "ECHO" request and response supporting serialization and de-serialization via
 * both jackson and gson.
 */
@ToString
public class RestEchoReqResp {
    @GsonJsonProperty("reqId")
    @SerializedName("reqId")
    private int requestId;

    @GsonJsonProperty("textValue")
    @SerializedName("textValue")
    private String text;

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
