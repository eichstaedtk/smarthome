package org.openhab.binding.googletask.internal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.thing.Thing;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.openhab.binding.googletask.internal.GoogleTaskBindingConstants.THING_TYPE_GOOGLE_TASK_API;

@ExtendWith(MockitoExtension.class)
public class GoogleTaskHandlerTest {

    @Mock
    Thing thing;

    @Test
    void testReadingTasks() throws GeneralSecurityException, IOException {

        Mockito.when(thing.getThingTypeUID()).thenReturn(THING_TYPE_GOOGLE_TASK_API);

        GoogleTaskHandler handler = (GoogleTaskHandler) new GoogleTaskHandlerFactory().createHandler(thing);

        assertNotNull(handler);

        handler.readTasks();
    }
}
