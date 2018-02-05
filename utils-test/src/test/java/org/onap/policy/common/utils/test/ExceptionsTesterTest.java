/*
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.utils.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ExceptionsTesterTest {

	@Test
	public void test() throws Exception {
		assertEquals(2, new ExceptionsTester().test(SimpleException.class));
		assertEquals(8, new ExceptionsTester().test(StaticException.class));
	}

	@Test(expected = AssertionError.class)
	public void testIgnoreMessageException() throws Exception {
		new ExceptionsTester().test(IgnoreMessageException.class);
	}

	@Test(expected = AssertionError.class)
	public void testIgnoreCauseException() throws Exception {
		new ExceptionsTester().test(IgnoreCauseException.class);
	}

	@Test(expected = AssertionError.class)
	public void testNonStaticException() throws Exception {
		new ExceptionsTester().test(NoConstructorsException.class);
	}

	@Test(expected = AssertionError.class)
	public void testAlwaysSuppressException() throws Exception {
		new ExceptionsTester().test(AlwaysSuppressException.class);
	}

	@Test(expected = AssertionError.class)
	public void testNeverSuppressException() throws Exception {
		new ExceptionsTester().test(NeverSuppressException.class);
	}

	@Test(expected = AssertionError.class)
	public void testAlwaysWritableException() throws Exception {
		new ExceptionsTester().test(AlwaysWritableException.class);
	}

	@Test(expected = AssertionError.class)
	public void testNeverWritableException() throws Exception {
		new ExceptionsTester().test(NeverWritableException.class);
	}

	/**
	 * Used to test a failure case - message text is ignored.
	 */
	public static class IgnoreMessageException extends Exception {
		private static final long serialVersionUID = 1L;

		public IgnoreMessageException(String message) {
			super("bogus");
		}
	}

	/**
	 * Used to test a failure case - cause is ignored.
	 */
	public static class IgnoreCauseException extends Exception {
		private static final long serialVersionUID = 1L;

		public IgnoreCauseException(Throwable cause) {
			super(new Exception("another cause"));
		}
	}

	/**
	 * Used to test a failure case - this has no standard constructions.
	 */
	public static class NoConstructorsException extends Exception {
		private static final long serialVersionUID = 1L;

		public NoConstructorsException(int value) {
			super(String.valueOf(value));
		}
	}

	/**
	 * Used to test a failure case - always suppresses.
	 */
	public static class AlwaysSuppressException extends Exception {
		private static final long serialVersionUID = 1L;

		public AlwaysSuppressException(String message, Throwable cause, boolean enableSuppression,
				boolean writableStackTrace) {
			super(message, cause, true, writableStackTrace);
		}
	}

	/**
	 * Used to test a failure case - never suppresses.
	 */
	public static class NeverSuppressException extends Exception {
		private static final long serialVersionUID = 1L;

		public NeverSuppressException(String message, Throwable cause, boolean enableSuppression,
				boolean writableStackTrace) {
			super(message, cause, false, writableStackTrace);
		}
	}

	/**
	 * Used to test a failure case - always allows stack writes.
	 */
	public static class AlwaysWritableException extends Exception {
		private static final long serialVersionUID = 1L;

		public AlwaysWritableException(String message, Throwable cause, boolean enableSuppression,
				boolean writableStackTrace) {
			super(message, cause, enableSuppression, true);
		}
	}

	/**
	 * Used to test a failure case - never allows stack writes.
	 */
	public static class NeverWritableException extends Exception {
		private static final long serialVersionUID = 1L;

		public NeverWritableException(String message, Throwable cause, boolean enableSuppression,
				boolean writableStackTrace) {
			super(message, cause, enableSuppression, false);
		}
	}

	/**
	 * Used to test a simple success case.
	 */
	public static class SimpleException extends Exception {
		private static final long serialVersionUID = 1L;

		public SimpleException() {
			super();
		}

		public SimpleException(String message) {
			super(message);
		}
	}

	/**
	 * Used to test the exhaustive success case.
	 */
	public static class StaticException extends Exception {
		private static final long serialVersionUID = 1L;

		public StaticException() {
			super();
		}

		public StaticException(String message) {
			super(message);
		}

		public StaticException(Throwable cause) {
			super(cause);
		}

		public StaticException(String message, Throwable cause) {
			super(message, cause);
		}

		public StaticException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}

		// same as above, but with Exceptions substituted for Throwables

		public StaticException(Exception cause) {
			super(cause);
		}

		public StaticException(String message, Exception cause) {
			super(message, cause);
		}

		public StaticException(String message, Exception cause, boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}
	}

}
