/*-
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import java.util.Observable;
import java.util.Observer;

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
public class StateChangeNotifier implements Observer {
    private static final Logger logger = LoggerFactory.getLogger(StateChangeNotifier.class);
    // The observable class
    StateManagement stateManagement;

    // A string argument passed by the observable class when
    // Observable:notifyObservers(Object arg) is called
    String message;

    @Override
    public void update(Observable observable, Object arg) {
        this.stateManagement = (StateManagement) observable;
        this.message = (String) arg;
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

    public StateManagement getStateManagement() {
        return stateManagement;
    }

    public String getMessage() {
        return message;
    }
}
