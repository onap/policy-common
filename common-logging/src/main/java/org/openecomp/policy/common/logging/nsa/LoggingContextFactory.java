/*-
 * ============LICENSE_START=======================================================
 * ECOMP-Logging
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

package org.openecomp.policy.common.logging.nsa;


import org.openecomp.policy.common.logging.nsa.impl.SharedContext;
import org.openecomp.policy.common.logging.nsa.impl.Slf4jLoggingContext;

/**
 * A factory for setting up a LoggingContext
 * 
 */
public class LoggingContextFactory
{
	public static class Builder
	{
		public Builder withBaseContext ( LoggingContext lc )
		{
			fBase = lc;
			return this;
		}

		public Builder forSharing ()
		{
			fShared = true;
			return this;
		}

		public LoggingContext build ()
		{
			return fShared ? new SharedContext ( fBase ) : new Slf4jLoggingContext ( fBase );
		}

		private LoggingContext fBase = null;
		private boolean fShared = false;
	}
}
