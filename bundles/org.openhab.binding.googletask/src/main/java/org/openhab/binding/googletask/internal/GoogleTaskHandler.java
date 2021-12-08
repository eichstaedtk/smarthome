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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link GoogleTaskHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Konrad Eichstädt - Initial contribution
 */
@NonNullByDefault
public class GoogleTaskHandler extends BaseThingHandler implements AccessTokenRefreshListener {

    private final Logger logger = LoggerFactory.getLogger(GoogleTaskHandler.class);

    private @Nullable GoogleTaskConfiguration config;

    private final OAuthFactory oAuthFactory;

    private @Nullable OAuthClientService oAuthClientService;

    private @Nullable String oauth2accessToken;

    private final List<GoogleTask> googleGoogleTasks = new LinkedList<>();

    private final HttpService httpService;

    private @Nullable ScheduledFuture<?> refreshJob;

    private static final String GOOGLE_AUTH_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_AUTH_AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    public static final String TASK_SCOPES = Stream.of("https://www.googleapis.com/auth/tasks")
            .collect(Collectors.joining(" "));

    public GoogleTaskHandler(Thing thing, OAuthFactory oAuthFactory, HttpService httpService) {
        super(thing);
        this.oAuthFactory = oAuthFactory;
        this.httpService = httpService;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        logger.info("Start Handle Command for Channel {} {}", channelUID.getId(), command.toFullString());

        if (GoogleTaskBindingConstants.CHANNEL_GET_TASKS.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                logger.info("Getting Refresh Command");

                logger.info("Update Channel with {} ",
                        googleGoogleTasks.stream().map(GoogleTask::getTitle).collect(Collectors.joining(",")));

                updateState(channelUID.getId(), StringType
                        .valueOf(googleGoogleTasks.stream().map(GoogleTask::getTitle).collect(Collectors.joining(","))));
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(GoogleTaskConfiguration.class);
        logger.info("Start initialization GoogleTasks.... with following config {} ", config);

        if (oAuthClientService == null) {
            oAuthClientService = oAuthFactory.createOAuthClientService(thing.getUID().getAsString(),
                    GOOGLE_AUTH_TOKEN_URL, GOOGLE_AUTH_AUTHORIZE_URL, config.getClientID(), config.getClientSecret(),
                    TASK_SCOPES, false);
        }

        oAuthClientService.addAccessTokenRefreshListener(this);

        try {

            httpService.registerServlet("/connectgoogle", new GoogleAuthServlet(this), new Hashtable<>(),
                    httpService.createDefaultHttpContext());

            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Please open page /connectgoogle for authorization");

        } catch (Exception e) {
            logger.error("Error during authentication", e);
        }

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                readingTasks();
            } catch (Exception e) {
                logger.error("Error reading Tasks from Google API", e);
            }
        }, 0, 5, TimeUnit.MINUTES);

        logger.info("End initialization GoogleTasks....");
    }

    public void setConfig(GoogleTaskConfiguration config) {
        this.config = config;
    }

    public String getAuthUrl() throws OAuthException {
        return oAuthClientService.getAuthorizationUrl(config.getRedirectURI(), null, thing.getUID().getAsString());
    }

    public void authorize(String code) throws OAuthException, OAuthResponseException, IOException {
        AccessTokenResponse accessTokenResponse = oAuthClientService.getAccessTokenResponseByAuthorizationCode(code,
                config.getRedirectURI());
        oauth2accessToken = accessTokenResponse.getAccessToken();
    }

    @Override
    public void onAccessTokenResponse(AccessTokenResponse tokenResponse) {

        logger.info("On Access Token Response ... {}", tokenResponse);

        oauth2accessToken = tokenResponse.getAccessToken();
    }

    public void readingTasks() throws IOException, InterruptedException {

        logger.info("Start reading tasks from gooogle using oauth2 token {}", oauth2accessToken);

        HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10)).build();

        HttpRequest request = HttpRequest.newBuilder().GET()
                .uri(URI.create("https://tasks.googleapis.com/tasks/v1/lists/" + config.getTaskListID() + "/tasks"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .header("Authorization", "Bearer " + oauth2accessToken).header("Content-Type", "application/json")
                .build();

        logger.info("Start sending request {} ", request.uri());

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        logger.info("Getting Google Task Response {} {} ", response.statusCode(), response.body());

        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();

            GoogleTaskList messageList = mapper.readValue(response.body(), GoogleTaskList.class);

            if (!messageList.getItems().isEmpty()) {
                googleGoogleTasks.clear();
                googleGoogleTasks.addAll(messageList.getItems());

                updateStatus(ThingStatus.ONLINE);
            }

            logger.info("Found new task list {} ", googleGoogleTasks.size());

        }
    }

    public String getThingUID() {
        return thing.getUID().getAsString();
    }
}
