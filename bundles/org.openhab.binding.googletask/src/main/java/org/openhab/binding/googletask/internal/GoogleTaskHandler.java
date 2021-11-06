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

import static org.osgi.service.application.ApplicationDescriptor.APPLICATION_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.TaskList;

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

    private @Nullable String oauth2accessToken;

    private @Nullable String oauth2refreshToken;

    public GoogleTaskHandler(Thing thing) {
        super(thing);
    }

    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final List<String> SCOPES = List.of(TasksScopes.TASKS_READONLY, TasksScopes.TASKS);

    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    public Credential createCredentials() throws IOException, GeneralSecurityException {
        InputStream secretInputStream = GoogleTaskHandler.class.getResourceAsStream(CREDENTIALS_FILE_PATH);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(secretInputStream));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH))).build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setHost(config.getHostname())
                .setPort(config.getPort()).setCallbackPath("/openhab").build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public TaskList readTasks() throws GeneralSecurityException, IOException {

        logger.info("Start reading task of {}", config);

        Tasks service = new Tasks.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY,
                createCredentials()).setApplicationName(APPLICATION_NAME).build();

        // "MTc0NDQ5MDgzNTM0NTY0ODE1Nzg6MDow"
        TaskList tasklists = service.tasklists().get(config.getTaskListID()).execute();

        logger.info("Getting tasks {} ", tasklists.size());

        return tasklists;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        logger.info("Start Handle Command for Channel {} {}", channelUID.getId(), command.toFullString());

        if (GoogleTaskBindingConstants.CHANNEL_GET_TASKS.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                logger.info("Getting Refresh Command");
                try {
                    TaskList taskList = readTasks();

                    updateState(channelUID.getId(), StringType.valueOf(taskList.getTitle()));

                } catch (Exception e) {
                    updateState(channelUID.getId(), StringType.valueOf(e.getMessage()));
                }
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    public void getAuthorizationResponse(String deviceCode) {

        HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10)).build();

        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(
                "client_id=60812911905-og6ku0g78g7f3gg2rkmkhlf1avl5iele.apps.googleusercontent.com&client_secret=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"
                        + "&device_code=" + deviceCode
                        + "&grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Adevice_code"))
                .uri(URI.create("https://oauth2.googleapis.com/token"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .header("Content-Type", "application/x-www-form-urlencoded").build();

        logger.info("Sending Request {} ", request.uri().toString());

        try {

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();

            GoogleAuthentificationResponse authentificationResponse = mapper.readValue(response.body(),
                    GoogleAuthentificationResponse.class);

            logger.info("Getting Google Device Authorization Response {} ", authentificationResponse);

            if (authentificationResponse != null && authentificationResponse.getAccessToken() != null) {
                this.oauth2accessToken = authentificationResponse.getAccessToken();
                this.oauth2refreshToken = authentificationResponse.getRefreshToken();

                updateStatus(ThingStatus.ONLINE);
            }

        } catch (Exception error) {
            logger.error("Error during getting access token ", error);
        }
    }

    public GoogleDeviceCodeResponse getAuthorizationforDevices() throws IOException, InterruptedException {

        HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10)).build();

        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(
                "client_id=60812911905-og6ku0g78g7f3gg2rkmkhlf1avl5iele.apps.googleusercontent.com&scope=email"))
                .uri(URI.create("https://oauth2.googleapis.com/device/code"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .header("Content-Type", "application/x-www-form-urlencoded").build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        GoogleDeviceCodeResponse googleDeviceCodeResponse = mapper.readValue(response.body(),
                GoogleDeviceCodeResponse.class);
        logger.info("Getting Google Device Authorization Code Response {} ", googleDeviceCodeResponse);
        return googleDeviceCodeResponse;
    }

    @Override
    public void initialize() {
        config = getConfigAs(GoogleTaskConfiguration.class);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        logger.info("Start initialization GoogleTasks....");

        try {
            GoogleDeviceCodeResponse response = getAuthorizationforDevices();

            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Please authorize the device: " + response.getVerificationUrl() + " " + response.getUserCode());

            scheduler.scheduleWithFixedDelay(() -> this.getAuthorizationResponse(response.getDeviceCode()), 0, 1,
                    TimeUnit.MINUTES);

        } catch (Exception e) {
            logger.error("Error during reading task from google {} ", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }

        logger.info("End initialization GoogleTasks....");

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    public void setConfig(GoogleTaskConfiguration config) {
        this.config = config;
    }
}
