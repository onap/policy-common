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

import org.yaml.snakeyaml.error.Mark;

/**
 * Runtime Exception generated by StandardYamlCoder.
 */
public class YamlException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final Mark problemMark;


    /**
     * Constructs the object.
     *
     * @param reason reason for the exception
     * @param problemMark where the exception occurred within the input
     */
    public YamlException(String reason, Mark problemMark) {
        super(reason);

        this.problemMark = problemMark;
    }

    /**
     * Constructs the object.
     *
     * @param reason reason for the exception
     * @param problemMark where the exception occurred within the input
     * @param cause cause of the exception
     */
    public YamlException(String reason, Mark problemMark, Throwable cause) {
        super(reason, cause);

        this.problemMark = problemMark;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + problemMark;
    }
}
