package com.dgc.dm.web.controller;

import com.dgc.dm.core.exception.DecisionException;
import com.dgc.dm.web.configuration.ApplicationConfiguration;
import com.dgc.dm.web.facade.ExcelFacade;
import com.dgc.dm.web.facade.ModelFacade;
import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import static com.dgc.dm.web.controller.CommonController.MODEL_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.MockitoAnnotations.initMocks;

class CommonControllerTest {

    @Mock
    private ExcelFacade mockExcelFacade;
    @Mock
    private ModelFacade mockModelFacade;

    @InjectMocks
    private CommonController commonControllerUnderTest;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void testResolveException() {
        // Setup
        final Exception exception = new Exception("s");

        // Run the test
        final ModelAndView result = commonControllerUnderTest.resolveException(null, null, "object", exception);

        // Verify the results
        assertEquals("s", result.getModel().get(MODEL_MESSAGE));
    }

    @Test
    void testResolveException_MaxUploadSizeExceededException() {
        // Setup
        final MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(ApplicationConfiguration.MAX_UPLOAD_SIZE);

        // Run the test
        final ModelAndView result = commonControllerUnderTest.resolveException(null, null, "object", exception);

        // Verify the results
        assertEquals(CommonController.MAX_UPLOAD_SIZE_EXCEEDED, result.getModel().get(MODEL_MESSAGE));
    }

    @Test
    void testResolveException_HttpRequestMethodNotSupportedException() {
        // Setup
        final HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("Test");

        // Run the test
        final ModelAndView result = commonControllerUnderTest.resolveException(null, null, "object", exception);

        // Verify the results
        assertEquals(CommonController.HOME_VIEW, result.getViewName());
    }

    @Test
    void testResolveException_NoHandlerFoundException() {
        // Setup
        final NoHandlerFoundException exception = new NoHandlerFoundException(null, null, null);

        // Run the test
        final ModelAndView result = commonControllerUnderTest.resolveException(null, null, "object", exception);

        // Verify the results
        assertEquals(CommonController.HOME_VIEW, result.getViewName());
    }

    @Test
    void testResolveException_ClientAbortException() {
        // Setup
        final ClientAbortException exception = new ClientAbortException();

        // Run the test
        final ModelAndView result = commonControllerUnderTest.resolveException(null, null, "object", exception);

        // Verify the results
        assertNull(result.getModel().get(MODEL_MESSAGE));
    }

    @Test
    void testResolveException_DecisionException() {
        // Setup
        final DecisionException exception = new DecisionException("test");

        // Run the test
        final ModelAndView result = commonControllerUnderTest.resolveException(null, null, "object", exception);

        // Verify the results
        assertEquals("test", result.getModel().get(MODEL_MESSAGE));
    }
}
