/*-
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.im;

public class AllSeemsWellException extends IntegrityMonitorException {

    private static final long serialVersionUID = 1L;

    public AllSeemsWellException() {
        super();
    }

    public AllSeemsWellException(String msg) {
        super(msg);
    }

    public AllSeemsWellException(String msg, Exception cause) {
        super(msg, cause);
    }

    public AllSeemsWellException(Exception cause) {
        super(cause);
    }
}
