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
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
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
public class GoogleAuthenticationService {

    private final Logger logger = LoggerFactory.getLogger(GoogleAuthenticationService.class);

    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final List<String> SCOPES = List.of(TasksScopes.TASKS_READONLY, TasksScopes.TASKS);

    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private @Nullable String oauth2accessToken;

    private @Nullable String oauth2refreshToken;

    private OAuthClientService oauth2Service;

    public GoogleAuthenticationService() {
    }

    public Credential createCredentials(String hostname, int port) throws IOException, GeneralSecurityException {
        InputStream secretInputStream = GoogleTaskHandler.class.getResourceAsStream(CREDENTIALS_FILE_PATH);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(secretInputStream));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH))).build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setHost(hostname).setPort(port)
                .setCallbackPath("/openhab").build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public TaskList readTasks() throws GeneralSecurityException, IOException {

        logger.info("Start reading task");

        Tasks service = new Tasks.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY,
                createCredentials("", 8080)).setApplicationName(APPLICATION_NAME).build();

        TaskList tasklists = service.tasklists().get("MTc0NDQ5MDgzNTM0NTY0ODE1Nzg6MDow").execute();

        logger.info("Getting tasks {} ", tasklists.size());

        return tasklists;
    }

    public void readingTasks() throws IOException, InterruptedException {

        logger.info("Start reading tasks from gooogle using oauth2 token {}", oauth2accessToken);

        HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10)).build();

        // https://gmail.googleapis.com/gmail/v1/users/{userId}/messages
        // https://tasks.googleapis.com/tasks/v1/users/@me/lists/MTc0NDQ5MDgzNTM0NTY0ODE1Nzg6MDow

        HttpRequest request = HttpRequest.newBuilder().GET()
                .uri(URI.create(
                        "https://tasks.googleapis.com/tasks/v1/users/@me/lists/MTc0NDQ5MDgzNTM0NTY0ODE1Nzg6MDow"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .header("Authorization", "Bearer " + oauth2accessToken).header("Content-Type", "application/json")
                .build();

        logger.info("Start sending request {} ", request.uri());

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        logger.info("Getting Google Task Response {} {} ", response.statusCode(), response.body());

        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();

            List<GoogleTask> messageList = mapper.readValue(response.body(), new TypeReference<>() {
            });

            logger.info("Found new task list {} ", messageList.size());

        }
    }

    public void getAuthorizationResponse(String deviceCode) {

        logger.info("Starting get authorization access code");

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

            }

        } catch (Exception error) {
            logger.error("Error during getting access token ", error);
        }
    }

    public void startGoogleAccess() {

        logger.info("Starting get authorization access code");

        HttpClient httpClient = HttpClient.newBuilder().version(Version.HTTP_2).connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(
                "https://accounts.google.com/o/oauth2/v2/auth?client_id=60812911905-og6ku0g78g7f3gg2rkmkhlf1avl5iele.apps.googleusercontent.com&"
                        + "redirect_uri=localhost:8080/openhab&response_type=code&scope=task_read"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .header("Content-Type", "application/x-www-form-urlencoded").build();

        logger.info("Sending Request {} ", request.uri().toString());

        try {

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();

            GoogleAuthentificationResponse authentificationResponse = mapper.readValue(response.body(),
                    GoogleAuthentificationResponse.class);

            logger.info("Getting Google Device Authorization Response {} ", authentificationResponse);

            if (authentificationResponse != null && authentificationResponse.getAccessToken() != null) {
                this.oauth2accessToken = authentificationResponse.getAccessToken();
                this.oauth2refreshToken = authentificationResponse.getRefreshToken();

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

    public String getOauth2accessToken() {
        return oauth2accessToken;
    }

    public String getOauth2refreshToken() {
        return oauth2refreshToken;
    }
}
