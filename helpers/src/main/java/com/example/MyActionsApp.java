/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.ActionsSdkApp;
import com.google.actions.api.ConstantsKt;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;
import com.google.actions.api.response.helperintent.Confirmation;
import com.google.actions.api.response.helperintent.DateTimePrompt;
import com.google.actions.api.response.helperintent.Permission;
import com.google.actions.api.response.helperintent.Place;
import com.google.api.services.actions_fulfillment.v2.model.DateTime;
import com.google.api.services.actions_fulfillment.v2.model.Location;

public class MyActionsApp extends ActionsSdkApp {

  @ForIntent("actions.intent.MAIN")
  public ActionResponse welcome(ActionRequest request) {
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    responseBuilder.add("Hi! I can show you different types of helpers.");
    responseBuilder.add("Which would you like to see?");
    responseBuilder.addSuggestions(
        new String[] {
          "Permission", "Confirmation", "Date/Time", "Place",
        });
    return responseBuilder.build();
  }

  @ForIntent("actions.intent.TEXT")
  public ActionResponse text(ActionRequest request) {
    String query = request.getRawInput().getQuery().toLowerCase();
    if (query.equals("permission")) {
      return permission(request);
    } else if (query.equals("confirmation")) {
      return confirmation(request);
    } else if (query.equals("date time") || query.equals("date/time") || query.equals("datetime")) {
      return datetime(request);
    } else if (query.equals("place")) {
      return place(request);
    } else {
      ResponseBuilder responseBuilder = getResponseBuilder(request);
      responseBuilder.add("You didn't name a supported helper!");
      responseBuilder.add("Which one would you like to see?");
      return responseBuilder.build();
    }
  }

  public ActionResponse permission(ActionRequest request) {
    // [START asdk_java_permission_reason]
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    String[] permissions = new String[] {ConstantsKt.PERMISSION_NAME};
    String context = "To address you by name";
    // Location permissions only work for verified users
    // https://developers.google.com/actions/assistant/guest-users
    if (request.getUser().getUserVerificationStatus().equals("VERIFIED")) {
      // Could use PERMISSION_DEVICE_COARSE_LOCATION instead for city, zip code
      permissions =
          new String[] {
            ConstantsKt.PERMISSION_NAME, ConstantsKt.PERMISSION_DEVICE_PRECISE_LOCATION
          };
    }
    responseBuilder
        .add("PLACEHOLDER")
        .add(new Permission().setPermissions(permissions).setContext(context));

    return responseBuilder.build();
    // [END asdk_java_permission_reason]
  }

  // [START asdk_java_permission_accepted]
  @ForIntent("actions.intent.PERMISSION")
  public ActionResponse handlePermission(ActionRequest request) {
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    Location location = request.getDevice().getLocation();
    String name = request.getUser().getProfile().getDisplayName();

    if (request.isPermissionGranted()) {
      responseBuilder.add("Okay " + name + ", I see you're at " + location.getFormattedAddress());
    } else {
      responseBuilder.add("Looks like I can't get your information");
    }
    responseBuilder
        .add("Would you like to try another helper?")
        .addSuggestions(new String[] {"Confirmation", "DateTime", "Place"});

    return responseBuilder.build();
  }
  // [END asdk_java_permission_accepted]

  public ActionResponse datetime(ActionRequest request) {
    // [START asdk_java_datetime_reason]
    ResponseBuilder responseBuilder = getResponseBuilder(request);

    responseBuilder
        .add("PLACEHOLDER")
        .add(
            new DateTimePrompt()
                .setDateTimePrompt("When would you like to schedule the appointment?")
                .setDatePrompt("What day was that?")
                .setTimePrompt("What time works for you?"));
    return responseBuilder.build();
    // [END asdk_java_datetime_reason]
  }

  // [START asdk_java_datetime_accepted]
  @ForIntent("actions.intent.DATETIME")
  public ActionResponse datetimeHandler(ActionRequest request) {
    ResponseBuilder responseBuilder = getResponseBuilder(request);

    DateTime dateTimeValue = request.getDateTime();
    Integer day = dateTimeValue.getDate().getDay();
    Integer month = dateTimeValue.getDate().getMonth();
    Integer hours = dateTimeValue.getTime().getHours();
    Integer minutes = dateTimeValue.getTime().getMinutes();
    String minutesStr = (minutes != null) ? String.valueOf(minutes) : "00";
    responseBuilder.add(
        "<speak>"
            + "Great, we will see you on "
            + "<say-as interpret-as=\"date\" format=\"dm\">"
            + day
            + "-"
            + month
            + "</say-as>"
            + "<say-as interpret-as=\"time\" format=\"hms12\" detail=\"2\">"
            + hours
            + ":"
            + minutesStr
            + "</say-as>"
            + "</speak>");
    responseBuilder
        .add("Would you like to try another helper?")
        .addSuggestions(new String[] {"Confirmation", "Permission", "Place"});
    return responseBuilder.build();
  }
  // [END asdk_java_datetime_accepted]

  public ActionResponse place(ActionRequest request) {
    // [START asdk_java_place_reason]
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    responseBuilder
        .add("PLACEHOLDER")
        .add(
            new Place()
                .setRequestPrompt("Where would you like to go?")
                .setPermissionContext("To find  a location"));
    return responseBuilder.build();
    // [END asdk_java_place_reason]
  }

  // [START asdk_java_place_accepted]
  @ForIntent("actions.intent.PLACE")
  public ActionResponse handlePlace(ActionRequest request) {
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    Location location = request.getPlace();
    if (location == null) {
      responseBuilder.add("Sorry, I couldn't get a location from you.");
    } else {
      responseBuilder.add(location.getName() + " sounds like a great place to go!");
    }
    responseBuilder
        .add("Would you like to try another helper?")
        .addSuggestions(new String[] {"Confirmation", "DateTime", "Permission"});
    return responseBuilder.build();
  }
  // [END asdk_java_place_accepted]

  public ActionResponse confirmation(ActionRequest request) {
    // [START asdk_java_confirmation_reason]
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    responseBuilder
        .add("PLACEHOLDER")
        .add(new Confirmation().setConfirmationText("Can you confirm?"));
    return responseBuilder.build();
    // [END asdk_java_confirmation_reason]
  }

  // [START asdk_java_confirmation_accepted]
  @ForIntent("actions.intent.CONFIRMATION")
  public ActionResponse handleConfirmation(ActionRequest request) {
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    boolean userConfirmation = request.getUserConfirmation();
    responseBuilder
        .add(userConfirmation ? "Thank you for confirming" : "No problem, you have not confirmed")
        .addSuggestions(new String[] {"Place", "DateTime", "Permission"});
    return responseBuilder.build();
  }
  // [END asdk_java_confirmation_accepted]

}
