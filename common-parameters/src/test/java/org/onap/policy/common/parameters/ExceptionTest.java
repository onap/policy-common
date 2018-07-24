package org.onap.policy.common.parameters;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class ExceptionTest {

    @Test
    void testParameterException() {
        assertEquals("Parameter Exception", new ParameterException("Parameter Exception").getMessage());

        String exceptionObject = "Exception Object";
        assertEquals("Exception Object",
                        new ParameterException("Parameter Exception", exceptionObject).getObject().toString());

        Exception testException = new IOException("IO Exception");
        assertEquals("Parameter Exception\ncaused by: Parameter Exception\ncaused by: IO Exception",
                        new ParameterException("Parameter Exception", testException, exceptionObject)
                                        .getCascadedMessage());
    }

    @Test
    void testParameterRuntimeException() {
        assertEquals("Parameter Exception", new ParameterRuntimeException("Parameter Exception").getMessage());

        String exceptionObject = "Exception Object";
        assertEquals("Exception Object",
                        new ParameterRuntimeException("Parameter Exception", exceptionObject).getObject().toString());

        Exception testException = new IOException("IO Exception");
        assertEquals("Parameter Exception\ncaused by: Parameter Exception\ncaused by: IO Exception",
                        new ParameterRuntimeException("Parameter Exception", testException, exceptionObject)
                                        .getCascadedMessage());
    }
}
