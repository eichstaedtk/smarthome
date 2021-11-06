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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link GoogleDeviceCodeResponse} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Konrad Eichstädt - Initial contribution
 */
public class GoogleDeviceCodeResponse {

    @JsonProperty("device_code")
    private String deviceCode;

    @JsonProperty("expires_in")
    private String expiresIn;

    @JsonProperty("interval")
    private String interval;

    @JsonProperty("user_code")
    private String userCode;

    @JsonProperty("verification_url")
    private String verificationUrl;

    @JsonProperty("error")
    private String error;

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getVerificationUrl() {
        return verificationUrl;
    }

    public void setVerificationUrl(String verificationUrl) {
        this.verificationUrl = verificationUrl;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "GoogleDeviceCodeResponse{" + "deviceCode='" + deviceCode + '\'' + ", expiresIn='" + expiresIn + '\''
                + ", interval='" + interval + '\'' + ", userCode='" + userCode + '\'' + ", verificationUrl='"
                + verificationUrl + '\'' + ", error='" + error + '\'' + '}';
    }
}
