/*
 * ============LICENSE_START=======================================================
 * ONAP PAP
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

package org.onap.policy.common.utils.services;

/**
 * Exceptions thrown by the ServiceManager.
 */
public class ServiceManagerException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ServiceManagerException() {
        super();
    }

    public ServiceManagerException(String message) {
        super(message);
    }

    public ServiceManagerException(Throwable cause) {
        super(cause);
    }

    public ServiceManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}
