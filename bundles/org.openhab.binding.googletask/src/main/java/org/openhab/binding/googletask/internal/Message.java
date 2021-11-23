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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The {@link GoogleDeviceCodeResponse} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Konrad Eichstädt - Initial contribution
 */

@JsonIgnoreProperties
public class Message {

    private String id;

    private String threadId;

    private String snippet;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }
}
