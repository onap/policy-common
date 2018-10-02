package org.onap.policy.common.utils.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.onap.policy.common.utils.test.PolicyAssert.assertException;

import org.junit.Test;

public class PolicyAssertTest {

    private static final String EXPECTED = "expected exception";

    @Test
    public void test_ExpectedEx() {
        assertException(IllegalArgumentException.class, () -> {
            throw new IllegalArgumentException(EXPECTED);
        });
    }

    @Test
    public void test_IncorrectEx() {
        try {
            assertException(IllegalStateException.class, () -> {
                throw new IllegalArgumentException(EXPECTED);
            });

            fail("incorrect exception type");

        } catch (AssertionError err) {
            assertTrue(err.getMessage().contains("incorrect exception type"));
        }
    }

    @Test
    public void test_MissingEx() {
        try {
            assertException(IllegalArgumentException.class, () -> {
            });

            fail("missing exception");

        } catch (AssertionError err) {
            assertTrue(err.getMessage().contains("missing exception"));
        }
    }

}
