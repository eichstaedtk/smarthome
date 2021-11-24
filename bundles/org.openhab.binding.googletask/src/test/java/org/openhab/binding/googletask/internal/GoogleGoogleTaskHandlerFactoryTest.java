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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.googletask.internal.GoogleTaskBindingConstants.THING_TYPE_GOOGLE_TASK_API;

import org.junit.jupiter.api.Test;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.osgi.service.http.HttpService;

/**
 * Testing the GoogleTaskHandlerFactory
 *
 * @author konrad.eichstaedt@gmx.de on 09.10.21.
 */
public class GoogleGoogleTaskHandlerFactoryTest {

    private final OAuthFactory oAuthFactory = mock(OAuthFactory.class);
    private final HttpService httpService = mock(HttpService.class);

    @Test
    void testSupportedThing() {

        GoogleTaskHandlerFactory factory = new GoogleTaskHandlerFactory(oAuthFactory, httpService);

        assertTrue(factory.supportsThingType(THING_TYPE_GOOGLE_TASK_API));
    }
}
