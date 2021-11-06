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

/**
 * The {@link GoogleTaskConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Konrad Eichstädt - Initial contribution
 */
public class GoogleTaskConfiguration {

    /**
     * Sample configuration parameters. Replace with your own.
     */
    public String hostname;
    public int port;
    public String taskListID;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getTaskListID() {
        return taskListID;
    }

    public void setTaskListID(String taskListID) {
        this.taskListID = taskListID;
    }

    @Override
    public String toString() {
        return "GoogleTaskConfiguration{" + "hostname='" + hostname + '\'' + ", port=" + port + ", taskListID='"
                + taskListID + '\'' + '}';
    }
}
