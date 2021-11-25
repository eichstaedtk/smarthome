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

    public String clientID;
    public String clientSecret;
    public String redirectURI;
    public String taskListID;

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectURI() {
        return redirectURI;
    }

    public void setRedirectURI(String redirectURI) {
        this.redirectURI = redirectURI;
    }

    public String getTaskListID() {
        return taskListID;
    }

    public void setTaskListID(String taskListID) {
        this.taskListID = taskListID;
    }

    @Override
    public String toString() {
        return "GoogleTaskConfiguration{" + "clientID='" + clientID + '\'' + ", clientSecret='" + clientSecret + '\''
                + ", redirectURI='" + redirectURI + '\'' + ", taskListID='" + taskListID + '\'' + '}';
    }
}
