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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;

/**
 * Used to test various Exception subclasses. Uses reflection to identify the
 * constructors that the subclass supports.
 */
public class ExceptionsTester {

	/**
	 * Runs tests, on an Exception subclass, for all of the standard
	 * constructors. If the Exception subclass does not support a given type of
	 * constructor, then it skips that test.
	 * 
	 * @param claz
	 *            subclass to be tested
	 * @return the number of constructors that the test found/tested
	 * @throws Exception
	 *             if the Exception cannot be constructed
	 */
	public <T extends Exception> int test(Class<T> claz) throws Exception {
		int ncons = 0;

		ncons += testDefault(claz);
		ncons += testString(claz);
		ncons += testThrowable(claz);
		ncons += testException(claz);
		ncons += testStringThrowable(claz);
		ncons += testStringException(claz);
		ncons += testStringThrowableBooleanBoolean(claz);
		ncons += testStringExceptionBooleanBoolean(claz);
		
		assertTrue(ncons > 0);

		return ncons;
	}

	/**
	 * Tests exceptions created via the default constructor. Verifies that:
	 * <ul>
	 * <li><i>toString()</i> returns a non-null value</li>
	 * <li><i>getMessage()</i> returns null</li>
	 * <li><i>getCause()</i> returns null</li>
	 * </ul>
	 * 
	 * If the Exception subclass does not support this type of constructor, then
	 * this method simply returns.
	 * 
	 * @param claz
	 *            subclass to be tested
	 * @return {@code 1}, if the subclass supports this type of constructor,
	 *         {@code 0} otherwise
	 * @throws Exception
	 *             if the Exception cannot be constructed
	 */
	public <T extends Exception> int testDefault(Class<T> claz) throws Exception {
		Constructor<T> cons;

		try {
			cons = claz.getConstructor();

		} catch (NoSuchMethodException | SecurityException e) {
			// this constructor is not defined so nothing to test
			return 0;
		}

		T ex = cons.newInstance();

		assertNotNull(ex.toString());
		assertNull(ex.getMessage());
		assertNull(ex.getCause());

		return 1;
	}

	/**
	 * Tests exceptions created via the constructor that takes just a String.
	 * Verifies that:
	 * <ul>
	 * <li><i>toString()</i> returns a non-null value</li>
	 * <li><i>getMessage()</i> returns the original message passed to the
	 * constructor</li>
	 * <li><i>getCause()</i> returns null</li>
	 * </ul>
	 * 
	 * If the Exception subclass does not support this type of constructor, then
	 * this method simply returns.
	 * 
	 * @param claz
	 *            subclass to be tested
	 * @return {@code 1}, if the subclass supports this type of constructor,
	 *         {@code 0} otherwise
	 * @throws Exception
	 *             if the Exception cannot be constructed
	 */
	public <T extends Exception> int testString(Class<T> claz) throws Exception {
		Constructor<T> cons;

		try {
			cons = claz.getConstructor(String.class);

		} catch (NoSuchMethodException | SecurityException e) {
			// this constructor is not defined so nothing to test
			return 0;
		}

		T ex = cons.newInstance("hello");

		assertNotNull(ex.toString());
		assertEquals("hello", ex.getMessage());
		assertNull(ex.getCause());

		return 1;
	}

	/**
	 * Tests exceptions created via the constructor that takes just a Throwable.
	 * Verifies that:
	 * <ul>
	 * <li><i>toString()</i> returns a non-null value</li>
	 * <li><i>getMessage()</i> returns the cause's message</li>
	 * <li><i>getCause()</i> returns the original cause passed to the
	 * constructor</li>
	 * </ul>
	 * 
	 * If the Exception subclass does not support this type of constructor, then
	 * this method simply returns.
	 * 
	 * @param claz
	 *            subclass to be tested
	 * @return {@code 1}, if the subclass supports this type of constructor,
	 *         {@code 0} otherwise
	 * @throws Exception
	 *             if the Exception cannot be constructed
	 */
	public <T extends Exception> int testThrowable(Class<T> claz) throws Exception {
		Constructor<T> cons;

		try {
			cons = claz.getConstructor(Throwable.class);

		} catch (NoSuchMethodException | SecurityException e) {
			// this constructor is not defined so nothing to test
			return 0;
		}

		Throwable cause = new Throwable("expected exception");
		T ex = cons.newInstance(cause);

		assertEquals(ex.getMessage(), ex.getMessage());
		assertNotNull(ex.toString());
		assertEquals(cause, ex.getCause());

		return 1;
	}

	/**
	 * Tests exceptions created via the constructor that takes just an
	 * Exception. Verifies that:
	 * <ul>
	 * <li><i>toString()</i> returns a non-null value</li>
	 * <li><i>getMessage()</i> returns the cause's message</li>
	 * <li><i>getCause()</i> returns the original cause passed to the
	 * constructor</li>
	 * </ul>
	 * 
	 * If the Exception subclass does not support this type of constructor, then
	 * this method simply returns.
	 * 
	 * @param claz
	 *            subclass to be tested
	 * @return {@code 1}, if the subclass supports this type of constructor,
	 *         {@code 0} otherwise
	 * @throws Exception
	 *             if the Exception cannot be constructed
	 */
	public <T extends Exception> int testException(Class<T> claz) throws Exception {
		Constructor<T> cons;

		try {
			cons = claz.getConstructor(Exception.class);

		} catch (NoSuchMethodException | SecurityException e) {
			// this constructor is not defined so nothing to test
			return 0;
		}

		Exception cause = new Exception("expected exception");
		T ex = cons.newInstance(cause);

		assertNotNull(ex.toString());
		assertEquals(ex.getMessage(), ex.getMessage());
		assertEquals(cause, ex.getCause());

		return 1;
	}

	/**
	 * Tests exceptions created via the constructor that takes a String and a
	 * Throwable. Verifies that:
	 * <ul>
	 * <li><i>toString()</i> returns a non-null value</li>
	 * <li><i>getMessage()</i> returns the original message passed to the
	 * constructor</li>
	 * <li><i>getCause()</i> returns the original cause passed to the
	 * constructor</li>
	 * </ul>
	 * 
	 * If the Exception subclass does not support this type of constructor, then
	 * this method simply returns.
	 * 
	 * @param claz
	 *            subclass to be tested
	 * @return {@code 1}, if the subclass supports this type of constructor,
	 *         {@code 0} otherwise
	 * @throws Exception
	 *             if the Exception cannot be constructed
	 */
	public <T extends Exception> int testStringThrowable(Class<T> claz) throws Exception {
		Constructor<T> cons;

		try {
			cons = claz.getConstructor(String.class, Throwable.class);

		} catch (NoSuchMethodException | SecurityException e) {
			// this constructor is not defined so nothing to test
			return 0;
		}

		Throwable cause = new Throwable("expected exception");
		T ex = cons.newInstance("world", cause);

		assertNotNull(ex.toString());
		assertEquals("world", ex.getMessage());
		assertEquals(cause, ex.getCause());

		return 1;
	}

	/**
	 * Tests exceptions created via the constructor that takes a String and an
	 * Exception. Verifies that:
	 * <ul>
	 * <li><i>toString()</i> returns a non-null value</li>
	 * <li><i>getMessage()</i> returns the original message passed to the
	 * constructor</li>
	 * <li><i>getCause()</i> returns the original cause passed to the
	 * constructor</li>
	 * </ul>
	 * 
	 * If the Exception subclass does not support this type of constructor, then
	 * this method simply returns.
	 * 
	 * @param claz
	 *            subclass to be tested
	 * @return {@code 1}, if the subclass supports this type of constructor,
	 *         {@code 0} otherwise
	 * @throws Exception
	 *             if the Exception cannot be constructed
	 */
	public <T extends Exception> int testStringException(Class<T> claz) throws Exception {
		Constructor<T> cons;

		try {
			cons = claz.getConstructor(String.class, Exception.class);

		} catch (NoSuchMethodException | SecurityException e) {
			// this constructor is not defined so nothing to test
			return 0;
		}

		Exception cause = new Exception("expected exception");
		T ex = cons.newInstance("world", cause);

		assertNotNull(ex.toString());
		assertEquals("world", ex.getMessage());
		assertEquals(cause, ex.getCause());

		return 1;
	}

	/**
	 * Tests exceptions created via the constructor that takes a String, a
	 * Throwable, and two booleans. Verifies that:
	 * <ul>
	 * <li><i>toString()</i> returns a non-null value</li>
	 * <li><i>getMessage()</i> returns the original message passed to the
	 * constructor</li>
	 * <li><i>getCause()</i> returns the original cause passed to the
	 * constructor</li>
	 * <li>suppressed exceptions can be added, if enabled</li>
	 * <li>the stack trace can be added, if enabled</li>
	 * </ul>
	 * 
	 * If the Exception subclass does not support this type of constructor, then
	 * this method simply returns.
	 * 
	 * @param claz
	 *            subclass to be tested
	 * @return {@code 1}, if the subclass supports this type of constructor,
	 *         {@code 0} otherwise
	 * @throws Exception
	 *             if the Exception cannot be constructed
	 */
	public <T extends Exception> int testStringThrowableBooleanBoolean(Class<T> claz) throws Exception {
		Constructor<T> cons;

		try {
			cons = claz.getConstructor(String.class, Throwable.class, Boolean.TYPE, Boolean.TYPE);

		} catch (NoSuchMethodException | SecurityException e) {
			// this constructor is not defined so nothing to test
			return 0;
		}

		// test each combination of "message" and "cause"
		testMessageCauseCombos(cons);

		// test each combination of the boolean flags
		testSuppressStack(cons);
		testSuppressNoStack(cons);
		testNoSuppressStack(cons);
		testNoSuppressNoStack(cons);

		return 1;
	}

	/**
	 * Tests exceptions created via the constructor that takes a String, an
	 * Exception, and two booleans. Verifies that:
	 * <ul>
	 * <li><i>toString()</i> returns a non-null value</li>
	 * <li><i>getMessage()</i> returns the original message passed to the
	 * constructor</li>
	 * <li><i>getCause()</i> returns the original cause passed to the
	 * constructor</li>
	 * <li>suppressed exceptions can be added, if enabled</li>
	 * <li>the stack trace can be added, if enabled</li>
	 * </ul>
	 * 
	 * If the Exception subclass does not support this type of constructor, then
	 * this method simply returns.
	 * 
	 * @param claz
	 *            subclass to be tested
	 * @return {@code 1}, if the subclass supports this type of constructor,
	 *         {@code 0} otherwise
	 * @throws Exception
	 *             if the Exception cannot be constructed
	 */
	public <T extends Exception> int testStringExceptionBooleanBoolean(Class<T> claz) throws Exception {
		Constructor<T> cons;

		try {
			cons = claz.getConstructor(String.class, Exception.class, Boolean.TYPE, Boolean.TYPE);

		} catch (NoSuchMethodException | SecurityException e) {
			// this constructor is not defined so nothing to test
			return 0;
		}

		// test each combination of "message" and "cause"
		testMessageCauseCombos(cons);

		// test each combination of the boolean flags
		testFlagCombos(cons);

		return 1;
	}

	/**
	 * Tests each combination of values for the "message" and the "cause" when
	 * using the constructor that takes a String, a Throwable/Exception, and two
	 * booleans. Verifies that expected values are returned by <i>toString()/i>,
	 * <i>getMessage()</i>, and <i>getCause()</i>.
	 * </ul>
	 * 
	 * @param cons
	 *            constructor to be invoked
	 * @throws Exception
	 *             if the Exception cannot be constructed
	 */
	private <T extends Exception> void testMessageCauseCombos(Constructor<T> cons) throws Exception {
		T ex;
		Exception cause = new Exception("expected throwable");

		ex = cons.newInstance(null, null, true, true);
		assertNotNull(ex.toString());
		assertNull(ex.getMessage());
		assertNull(ex.getCause());

		ex = cons.newInstance("abc", null, true, true);
		assertNotNull(ex.toString());
		assertEquals("abc", ex.getMessage());
		assertNull(ex.getCause());

		ex = cons.newInstance(null, cause, true, true);
		assertNotNull(ex.toString());
		assertNull(ex.getMessage());
		assertEquals(cause, ex.getCause());

		ex = cons.newInstance("xyz", cause, true, true);
		assertNotNull(ex.toString());
		assertEquals("xyz", ex.getMessage());
		assertEquals(cause, ex.getCause());
	}

	/**
	 * Tests each combination of values for the "message" and the "cause" when
	 * using the constructor that takes a String, a Throwable/Exception, and two
	 * booleans. Verifies that expected values are returned by <i>toString()/i>,
	 * <i>getMessage()</i>, and <i>getCause()</i>.
	 * </ul>
	 * 
	 * @param cons
	 *            constructor to be invoked
	 * @throws Exception
	 *             if the Exception cannot be constructed
	 */
	public <T extends Exception> void testFlagCombos(Constructor<T> cons) throws Exception {
		testSuppressStack(cons);
		testSuppressNoStack(cons);
		testNoSuppressStack(cons);
		testNoSuppressNoStack(cons);
	}

	/**
	 * Tests exceptions constructed with {@code enableSuppression=true} and
	 * {@code writableStackTrace=true}. Verifies that:
	 * <ul>
	 * <li><i>toString()</i> returns a non-null value</li>
	 * <li><i>getMessage()</i> returns the original message passed to the
	 * constructor</li>
	 * <li><i>getCause()</i> returns the original cause passed to the
	 * constructor</li>
	 * <li>suppressed exceptions are added</li>
	 * <li>the stack trace is added</li>
	 * </ul>
	 * 
	 * @param cons
	 *            the exception's class constructor
	 * @throws Exception
	 *             if the Exception cannot be constructed
	 */
	public <T extends Exception> void testSuppressStack(Constructor<T> cons) throws Exception {
		Exception cause = new Exception("expected exception");
		Throwable supr = new Throwable("expected suppressed exception");
		T ex = cons.newInstance("yes,yes", cause, true, true);

		ex.addSuppressed(supr);

		assertNotNull(ex.toString());
		assertEquals("yes,yes", ex.getMessage());
		assertEquals(cause, ex.getCause());

		assertEquals(1, ex.getSuppressed().length);
		assertEquals(supr, ex.getSuppressed()[0]);

		assertTrue(ex.getStackTrace().length > 0);
	}

	/**
	 * Tests exceptions constructed with {@code enableSuppression=true} and
	 * {@code writableStackTrace=false}. Verifies that:
	 * <ul>
	 * <li><i>toString()</i> returns a non-null value</li>
	 * <li><i>getMessage()</i> returns the original message passed to the
	 * constructor</li>
	 * <li><i>getCause()</i> returns the original cause passed to the
	 * constructor</li>
	 * <li>suppressed exceptions are added</li>
	 * <li>the stack trace is <i>not</i> added</li>
	 * </ul>
	 * 
	 * @param cons
	 *            the exception's class constructor
	 * @throws Exception
	 *             if the Exception cannot be constructed
	 */
	public <T extends Exception> void testSuppressNoStack(Constructor<T> cons) throws Exception {
		Exception cause = new Exception("expected exception");
		Throwable supr = new Throwable("expected suppressed exception");
		T ex = cons.newInstance("yes,no", cause, true, false);

		ex.addSuppressed(supr);

		assertNotNull(ex.toString());
		assertEquals("yes,no", ex.getMessage());
		assertEquals(cause, ex.getCause());

		assertEquals(1, ex.getSuppressed().length);
		assertEquals(supr, ex.getSuppressed()[0]);

		assertEquals(0, ex.getStackTrace().length);
	}

	/**
	 * Tests exceptions constructed with {@code enableSuppression=false} and
	 * {@code writableStackTrace=true}. Verifies that:
	 * <ul>
	 * <li><i>toString()</i> returns a non-null value</li>
	 * <li><i>getMessage()</i> returns the original message passed to the
	 * constructor</li>
	 * <li><i>getCause()</i> returns the original cause passed to the
	 * constructor</li>
	 * <li>suppressed exceptions are <i>not</i> added</li>
	 * <li>the stack trace is added</li>
	 * </ul>
	 * 
	 * @param cons
	 *            the exception's class constructor
	 * @throws Exception
	 *             if the Exception cannot be constructed
	 */
	public <T extends Exception> void testNoSuppressStack(Constructor<T> cons) throws Exception {
		Exception cause = new Exception("expected exception");
		Throwable supr = new Throwable("expected suppressed exception");
		T ex = cons.newInstance("no,yes", cause, false, true);

		ex.addSuppressed(supr);

		assertNotNull(ex.toString());
		assertEquals("no,yes", ex.getMessage());
		assertEquals(cause, ex.getCause());

		assertEquals(0, ex.getSuppressed().length);

		assertTrue(ex.getStackTrace().length > 0);
	}

	/**
	 * Tests exceptions constructed with {@code enableSuppression=false} and
	 * {@code writableStackTrace=false}. Verifies that:
	 * <ul>
	 * <li><i>toString()</i> returns a non-null value</li>
	 * <li><i>getMessage()</i> returns the original message passed to the
	 * constructor</li>
	 * <li><i>getCause()</i> returns the original cause passed to the
	 * constructor</li>
	 * <li>suppressed exceptions are <i>not</i> added</li>
	 * <li>the stack trace is <i>not</i> added</li>
	 * 
	 * @param cons
	 *            the exception's class constructor
	 * @throws Exception
	 *             if the Exception cannot be constructed
	 */
	public <T extends Exception> void testNoSuppressNoStack(Constructor<T> cons) throws Exception {
		Exception cause = new Exception("expected exception");
		Throwable supr = new Throwable("expected suppressed exception");
		T ex = cons.newInstance("no,no", cause, false, false);

		ex.addSuppressed(supr);

		assertNotNull(ex.toString());
		assertEquals("no,no", ex.getMessage());
		assertEquals(cause, ex.getCause());

		assertEquals(0, ex.getSuppressed().length);
		assertEquals(0, ex.getStackTrace().length);
	}
}
