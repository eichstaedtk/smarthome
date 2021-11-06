/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.googletask.internal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.openhab.binding.googletask.internal.GoogleTaskBindingConstants.THING_TYPE_GOOGLE_TASK_API;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.thing.Thing;

/**
 * Testing the GoogleTaskHandler
 *
 * @author konrad.eichstaedt@gmx.de on 09.10.21.
 */

@ExtendWith(MockitoExtension.class)
public class GoogleTaskHandlerTest {

    @Mock
    Thing thing;

    @Test
    void testReadingTasks() throws IOException, InterruptedException {

        when(thing.getThingTypeUID()).thenReturn(THING_TYPE_GOOGLE_TASK_API);

        GoogleTaskHandler handler = (GoogleTaskHandler) new GoogleTaskHandlerFactory().createHandler(thing);
        GoogleTaskConfiguration configuration = new GoogleTaskConfiguration();
        configuration.setHostname("localhost");
        configuration.setPort(8081);
        configuration.setTaskListID("MTc0NDQ5MDgzNTM0NTY0ODE1Nzg6MDow");

        if (handler != null) {

            handler.setConfig(configuration);

            assertNotNull(handler);
        }
    }
}
