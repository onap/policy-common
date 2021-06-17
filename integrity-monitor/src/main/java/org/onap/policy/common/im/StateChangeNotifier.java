/*-
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2017, 2020-2021 AT&T Intellectual Property. All rights reserved.
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

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*
 * This is implementing the Observer interface to make it specific for
 * state management.
 *
 * It saves the StateManagement object and a String message that is
 * passed in when notifyObservers is called by the Observable
 * host class.
 *
 * It provides an abstract method for handling the state change
 * so this class must be overwritten and made concrete for the
 * Observer who is monitoring the state changes.
 */



/**
 * StateChangeNotifier class implements the Observer pattern and is used to distribute state change
 * notifications to any entity that registers a derived class with an instance of the
 * StateManagement class.
 *
 */
@Getter
public class StateChangeNotifier {
    private static final Logger logger = LoggerFactory.getLogger(StateChangeNotifier.class);
    // The observable class
    StateManagement stateManagement;

    // A string argument passed by the observable class when
    // StateManagement:notifyObservers(String changed) is called
    String message;

    /**
     * Invoked to indicate that something observed by this notifier has changed.
     * @param observable    item that has changed
     * @param changed       message indicating what change was made
     */
    public void update(StateManagement observable, String changed) {
        this.stateManagement = observable;
        this.message = changed;
        handleStateChange();
    }

    /**
     * Handle state change.
     */
    public void handleStateChange() {
        if (logger.isDebugEnabled()) {
            logger.debug("handleStateChange, message: {}", this.message);
        }
    }
}
