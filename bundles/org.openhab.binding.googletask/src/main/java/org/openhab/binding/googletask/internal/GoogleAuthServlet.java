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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GoogleAuthServlet} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Konrad Eichstädt - Initial contribution
 */
public class GoogleAuthServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(GoogleAuthServlet.class);

    private final GoogleTaskHandler googleTaskHandler;

    public GoogleAuthServlet(GoogleTaskHandler googleTaskHandler) {
        this.googleTaskHandler = googleTaskHandler;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        logger.info("Getting Google Auth Callback {} ", req.getRequestURI());
        logger.info("Getting Request Query String {} ", req.getQueryString());

        try {
            if (req.getQueryString() == null) {
                resp.sendRedirect(googleTaskHandler.getAuthUrl());
            } else {

                final MultiMap<String> params = new MultiMap<>();
                UrlEncoded.decodeTo(req.getQueryString(), params, StandardCharsets.UTF_8.name());
                final String reqCode = params.getString("code");
                final String reqError = params.getString("error");

                logger.info("Found Authorization Code {} ", reqCode);
                if (reqCode != null && !reqCode.isEmpty() && (reqError == null || reqError.isEmpty())) {
                    googleTaskHandler.authorize(reqCode);
                    googleTaskHandler.readingTasks();
                    resp.sendRedirect("/settings/things/" + googleTaskHandler.getThingUID());
                }
            }
        } catch (InterruptedException | OAuthResponseException | OAuthException error) {
            logger.error("Error during authentication", error);
            throw new ServletException("Error during authentication ....", error);
        }
    }
}
