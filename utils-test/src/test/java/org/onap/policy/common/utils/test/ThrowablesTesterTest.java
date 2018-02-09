/*
 * ============LICENSE_START=======================================================
 * Common Utils-Test
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

public class ThrowablesTesterTest {

	@Test
	public void test() {
		assertEquals(2, new ThrowablesTester().testThrowable(SimpleThrowable.class));
		assertEquals(5, new ThrowablesTester().testThrowable(StaticThrowable.class));
	}

	@Test
	public void testNoConstructorsThrowable() {
		// this will not throw an error, but it should return 0, as there are
		// no matching constructors
		assertEquals(0, new ThrowablesTester().testThrowable(NoConstructorsThrowable.class));
	}

	@Test(expected = AssertionError.class)
	public void testIgnoreMessageThrowable() {
		new ThrowablesTester().testThrowable(IgnoreMessageThrowable.class);
	}

	@Test(expected = AssertionError.class)
	public void testIgnoreCauseThrowable() {
		new ThrowablesTester().testThrowable(IgnoreCauseThrowable.class);
	}

	@Test(expected = AssertionError.class)
	public void testAlwaysSuppressThrowable() {
		new ThrowablesTester().testThrowable(AlwaysSuppressThrowable.class);
	}

	@Test(expected = AssertionError.class)
	public void testNeverSuppressThrowable() {
		new ThrowablesTester().testThrowable(NeverSuppressThrowable.class);
	}

	@Test(expected = AssertionError.class)
	public void testAlwaysWritableThrowable() {
		new ThrowablesTester().testThrowable(AlwaysWritableThrowable.class);
	}

	@Test(expected = AssertionError.class)
	public void testNeverWritableThrowable() {
		new ThrowablesTester().testThrowable(NeverWritableThrowable.class);
	}

	@Test(expected = ConstructionError.class)
	public void testThrowInstantiationException() {
		new ThrowablesTester().testThrowable(ThrowInstantiationException.class);
	}

	/**
	 * Used to test a failure case - message text is ignored.
	 */
	public static class IgnoreMessageThrowable extends Throwable {
		private static final long serialVersionUID = 1L;

		public IgnoreMessageThrowable(String message) {
			super("bogus");
		}
	}

	/**
	 * Used to test a failure case - cause is ignored.
	 */
	public static class IgnoreCauseThrowable extends Throwable {
		private static final long serialVersionUID = 1L;

		public IgnoreCauseThrowable(Throwable cause) {
			super(new Throwable("another cause"));
		}
	}

	/**
	 * Used to test a failure case - this has no standard constructors. The only
	 * constructor it has takes an "int", thus it is not one of the standard
	 * constructors.
	 */
	public static class NoConstructorsThrowable extends Throwable {
		private static final long serialVersionUID = 1L;

		public NoConstructorsThrowable(int value) {
			super();
		}
	}

	/**
	 * Used to test a failure case - always suppresses.
	 */
	public static class AlwaysSuppressThrowable extends Throwable {
		private static final long serialVersionUID = 1L;

		public AlwaysSuppressThrowable(String message, Throwable cause, boolean enableSuppression,
				boolean writableStackTrace) {
			super(message, cause, true, writableStackTrace);
		}
	}

	/**
	 * Used to test a failure case - never suppresses.
	 */
	public static class NeverSuppressThrowable extends Throwable {
		private static final long serialVersionUID = 1L;

		public NeverSuppressThrowable(String message, Throwable cause, boolean enableSuppression,
				boolean writableStackTrace) {
			super(message, cause, false, writableStackTrace);
		}
	}

	/**
	 * Used to test a failure case - always allows stack writes.
	 */
	public static class AlwaysWritableThrowable extends Throwable {
		private static final long serialVersionUID = 1L;

		public AlwaysWritableThrowable(String message, Throwable cause, boolean enableSuppression,
				boolean writableStackTrace) {
			super(message, cause, enableSuppression, true);
		}
	}

	/**
	 * Used to test a failure case - never allows stack writes.
	 */
	public static class NeverWritableThrowable extends Throwable {
		private static final long serialVersionUID = 1L;

		public NeverWritableThrowable(String message, Throwable cause, boolean enableSuppression,
				boolean writableStackTrace) {
			super(message, cause, enableSuppression, false);
		}
	}

	/**
	 * Used to test a failure case - throws InstantiationException when
	 * constructed.
	 */
	public static class ThrowInstantiationException extends Throwable {
		private static final long serialVersionUID = 1L;

		public ThrowInstantiationException(String message, Throwable cause, boolean enableSuppression,
				boolean writableStackTrace) throws InstantiationException {

			throw new InstantiationException(ThrowablesTester.EXPECTED_EXCEPTION_MSG);
		}
	}

	/**
	 * Used to test a simple success case.
	 */
	public static class SimpleThrowable extends Throwable {
		private static final long serialVersionUID = 1L;

		public SimpleThrowable() {
			super();
		}

		public SimpleThrowable(String message) {
			super(message);
		}
	}

	/**
	 * Used to test the exhaustive success case.
	 */
	public static class StaticThrowable extends Throwable {
		private static final long serialVersionUID = 1L;

		public StaticThrowable() {
			super();
		}

		public StaticThrowable(String message) {
			super(message);
		}

		public StaticThrowable(Throwable cause) {
			super(cause);
		}

		public StaticThrowable(String message, Throwable cause) {
			super(message, cause);
		}

		public StaticThrowable(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}
	}

}
