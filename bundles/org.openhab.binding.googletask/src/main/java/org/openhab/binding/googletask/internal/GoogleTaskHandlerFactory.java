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

import static org.openhab.binding.googletask.internal.GoogleTaskBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;

/**
 * The {@link GoogleTaskHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Konrad Eichstädt - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.googletask", service = ThingHandlerFactory.class)
public class GoogleTaskHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_GOOGLE_TASK_API);

    private final OAuthFactory oAuthFactory;
    @NonNullByDefault({})
    private final HttpService httpService;

    @Activate
    public GoogleTaskHandlerFactory(@Reference OAuthFactory oAuthFactory, @Reference HttpService httpService) {
        this.oAuthFactory = oAuthFactory;
        this.httpService = httpService;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_GOOGLE_TASK_API.equals(thingTypeUID)) {
            return new GoogleTaskHandler(thing, oAuthFactory, httpService);
        }

        return null;
    }
}
