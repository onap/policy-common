/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation.
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

package org.onap.policy.common.logging.eelf;

import com.att.eelf.i18n.EELFResolvableResourceEnum;
import com.att.eelf.i18n.EELFResourceManager;

/**
 * MessageCodes contains all the messagge codes for EELF logging messages.
 */
public enum MessageCodes implements EELFResolvableResourceEnum {
    // Below is a list of Error Messages taken from com.att.research.xacml.api XACMLErrorConstants
    // found under:
    // policy-engine\XACML\src\main\java\com\att\research\xacml\api\XACMLErrorConstants.java

    ERROR_PERMISSIONS,

    ERROR_SYSTEM_ERROR,

    ERROR_DATA_ISSUE,

    ERROR_SCHEMA_INVALID,

    ERROR_PROCESS_FLOW,

    ERROR_UNKNOWN,

    ERROR_AUDIT,

    // Above is a list of Error Messages taken from com.att.research.xacml.api XACMLErrorConstants
    // found under:
    // policy-engine\XACML\src\main\java\com\att\research\xacml\api\XACMLErrorConstants.java

    // ----------------------5000-5099 Business/Flow Processing Related --------------------/

    BAD_TYPE_WARNING,

    GENERAL_INFO,

    GENERAL_WARNING,

    MISS_PROPERTY_ERROR,

    EXCEPTION_ERROR,

    MISS_PROPERTY_INFO,

    RULE_AUDIT_EXEC_INFO,

    RULE_AUDIT_BEGIN_INFO,

    RULE_AUDIT_END_INFO,

    RULE_AUDIT_START_END_INFO,

    RULE_METRICS_INFO,

    UPDATE_ERROR,

    GENERAL_ERROR,

    // ----------------------New enums should be added above this line
    // ------------------------------------------------------------------/

    // --------------------- The enums below are old code. They should not be used since 1607
    // release and eventually will be removed -----/
    /**
     * Application message which requires no arguments.
     */
    MESSAGE_SAMPLE_NOARGS,

    /**
     * Application message which requires one argument {0}.
     */
    MESSAGE_SAMPLE_ONEARGUMENT,

    /**
     * Audit message which requires one argument {0}.
     */
    AUDIT_MESSAGE_ONEARGUMENT,

    /**
     * Error message which requires one argument {0}.
     */
    ERROR_MESSAGE_ONEARGUMENT,

    /**
     * Metrics message which requires one argument {0}.
     */
    METRICS_MESSAGE_ONEARGUMENT,

    /**
     * Debug message which requires one argument {0}.
     */
    DEBUG_MESSAGE_ONEARGUMENT,

    /**
     * Application message which requires two argument {0} and another argument {1}.
     */
    MESSAGE_SAMPLE_TWOARGUMENTS,

    /**
     * Sample error exception.
     */
    MESSAGE_SAMPLE_EXCEPTION,

    /**
     * Sample warning message.
     */
    MESSAGE_WARNING_SAMPLE,

    /**
     * Sample exception in method {0}.
     */
    MESSAGE_SAMPLE_EXCEPTION_ONEARGUMENT,

    /**
     * Sample trace message.
     */
    MESSAGE_TRACE_SAMPLE,

    /**
     * Sample error message.
     */
    MESSAGE_ERROR_SAMPLE;


    /*
     * Static initializer to ensure the resource bundles for this class are loaded... Here this
     * application loads messages from three bundles.
     */
    static {
        EELFResourceManager.loadMessageBundle("org/onap/policy/common/logging/eelf/Resources");
        String id = EELFResourceManager.getIdentifier(RULE_AUDIT_EXEC_INFO);
        String value = EELFResourceManager.getMessage(RULE_AUDIT_EXEC_INFO);

        PolicyLogger.info("*********************** Rule audit id: " + id);
        PolicyLogger.info("*********************** Rule audit value: " + value);

    }
}
