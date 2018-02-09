/*-
 * ============LICENSE_START=======================================================
 * feature-session-persistence
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

package org.onap.policy.common.utils.test.jpa;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import javax.persistence.EntityManagerFactory;

import org.junit.Before;
import org.junit.Test;

public class EntityMgrFactoryCloserTest {
	
	private EntityManagerFactory factory;
	

	@Before
	public void setUp() throws Exception {
		factory = mock(EntityManagerFactory.class);
	}


	/**
	 * Verifies that the constructor does not do anything extra before
	 * being closed.
	 */
	@Test
	public void testEntityMgrFactoryCloser() {
		EntityMgrFactoryCloser c = new EntityMgrFactoryCloser(factory);
		
		assertEquals(factory, c.getFactory());

		// verify not closed yet
		verify(factory, never()).close();
		
		c.close();

		verify(factory).close();
	}

	@Test
	public void testgetFactory() {
		try(EntityMgrFactoryCloser c = new EntityMgrFactoryCloser(factory)) {
			assertEquals(factory, c.getFactory());
		}
	}
	
	/**
	 * Verifies that the manager gets closed when close() is invoked.
	 */
	@Test
	public void testClose() {
		EntityMgrFactoryCloser c = new EntityMgrFactoryCloser(factory);
		
		c.close();
		
		// should be closed
		verify(factory).close();
	}

	/**
	 * Ensures that the manager gets closed when "try" block exits normally.
	 */
	@Test
	public void testClose_TryWithoutExcept() {
		try(EntityMgrFactoryCloser c = new EntityMgrFactoryCloser(factory)) {
			
		}
		
		verify(factory).close();
	}

	/**
	 * Ensures that the manager gets closed when "try" block throws an
	 * exception.
	 */
	@Test
	public void testClose_TryWithExcept() {
		try {
			try(EntityMgrFactoryCloser c = new EntityMgrFactoryCloser(factory)) {
				throw new Exception("expected exception");
			}
			
		} catch (Exception e) {
		}
		
		verify(factory).close();
	}

}
