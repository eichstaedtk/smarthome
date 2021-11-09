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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GoogleTaskHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Konrad Eichstädt - Initial contribution
 */
@NonNullByDefault
public class GoogleTaskHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(GoogleTaskHandler.class);

    private @Nullable GoogleTaskConfiguration config;

    private final Map<String, Task> tasks = new HashMap<>();

    public GoogleTaskHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        logger.info("Start Handle Command for Channel {} {}", channelUID.getId(), command.toFullString());

        if (command instanceof RefreshType) {
            scheduler.submit(this::updateTasks);
        }
    }

    public void updateTasks() {

        for (Entry<String, Task> task : tasks.entrySet()) {

            logger.info("Update All Channel and States {} ", task.getKey());

            updateState(new ChannelUID(getThing().getUID(), "Task_" + task.getKey()),
                    StringType.valueOf(task.getValue().getTitle()));
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(GoogleTaskConfiguration.class);
        this.tasks.put("1", new Task("1", "Erste Aufgabe", "2021-11-07", "Open"));
        this.tasks.put("2", new Task("2", "Zweite Aufgabe", "2021-11-07", "Open"));

        updateStatus(ThingStatus.ONLINE);

        ThingHandlerCallback thingHandlerCallback = getCallback();

        for (Entry<String, Task> task : tasks.entrySet()) {

            Channel channel = thingHandlerCallback
                    .createChannelBuilder(new ChannelUID(getThing().getUID(), "Task_" + task.getKey()),
                            new ChannelTypeUID("googletask", "title"))
                    .withLabel("Task " + task.getKey()).build();

            updateThing(editThing().withChannel(channel).build());
        }

        scheduler.scheduleAtFixedRate(() -> this.updateTasks(), 0, 60, TimeUnit.SECONDS);

        try {

        } catch (Exception e) {
            logger.error("Error during reading task from google {} ", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }

        logger.info("End initialization GoogleTasks....");
    }

    public void setConfig(GoogleTaskConfiguration config) {
        this.config = config;
    }
}
