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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;

/**
 * The {@link GoogleTaskList} is a domain object class of google task list
 *
 * @author Konrad Eichstädt - Initial contribution
 */

@JsonIgnoreProperties
public class GoogleTaskList {

    private String etag;

    private String kind;

    @JsonIgnoreProperties
    private List<GoogleTask> items;

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public List<GoogleTask> getItems() {
        return items;
    }

    public void setItems(List<GoogleTask> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GoogleTaskList that = (GoogleTaskList) o;
        return Objects.equals(etag, that.etag) && Objects.equals(kind, that.kind)
            && Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(etag, kind, items);
    }

    @Override
    public String toString() {
        return "GoogleTaskList{" +
            "etag='" + etag + '\'' +
            ", kind='" + kind + '\'' +
            ", items=" + items +
            '}';
    }
}
