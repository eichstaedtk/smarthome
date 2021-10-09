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

import org.junit.jupiter.api.Test;

/**
 * Testing the Google API Configuration
 *
 * @author konrad.eichstaedt@gmx.de on 09.10.21.
 */
public class GoogleTaskConfigurationTest {

    @Test
    void checkConfigurationValues() {
        GoogleTaskConfiguration configuration = new GoogleTaskConfiguration();

        assertNotNull(configuration);
    }
}
