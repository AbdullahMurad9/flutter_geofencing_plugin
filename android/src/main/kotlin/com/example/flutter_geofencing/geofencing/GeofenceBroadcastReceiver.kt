/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.flutter_geofencing

import GeofenceErrorMessages.getErrorString
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings.Global.getString
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.example.flutter_geofencing.R
import com.example.flutter_geofencing.geofencing.GeofenceTransitionsJobIntentService

/**
 * Receiver for geofence transition changes.
 *
 *
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a JobIntentService
 * that will handle the intent in the background.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val TAG = "BroadcastReceiver"

    /**
     * Receives incoming intents.
     *
     * @param context the application context.
     * @param intent  sent by Location Services. This Intent is provided to Location
     * Services (inside a PendingIntent) when addGeofences() is called.
     */
    override fun onReceive(context: Context, intent: Intent) {
        // Enqueues a JobIntentService passing the context and intent as parameters
       Log.e("AAAAAAAAA", "AAAAAAAAA")
        GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
        try {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            if (geofencingEvent?.hasError() == true) {
                val errorMessage = getErrorString(
                    context,
                    geofencingEvent.errorCode
                )
                Log.e(TAG, errorMessage)
                return
            }

            // Get the transition type.
            val geofenceTransition = geofencingEvent?.geofenceTransition

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
            ) {

                // Get the geofences that were triggered. A single event can trigger multiple geofences.
                val triggeringGeofences = geofencingEvent.triggeringGeofences

                // Get the transition details as a String.
                val geofenceTransitionDetails = triggeringGeofences?.let {
                    getGeofenceTransitionDetails(
                        geofenceTransition,
                        it
                    )
                }

                // Send notification and log the transition details.
                if (geofenceTransitionDetails != null) {
                    Log.i("TAG 124", geofenceTransitionDetails.split(' ')[1])
                }
                val eventId = geofenceTransitionDetails?.split(' ')?.get(1)
                if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
                    eventId?.let { NativeMethodChannel.showNewIdea("In", it,context) }
                }else{
                    eventId?.let { NativeMethodChannel.showNewIdea("Out", it,context) }
                }
            } else {
                // Log the error.
                Log.e(TAG, context.getString(R.string.geofence_transition_invalid_type, geofenceTransition))
            }
            //NativeMethodChannel.showNewIdea("title",context)
        } catch (e: Exception) {
            Log.e("Error",e.toString())
        }
    }

    private fun getGeofenceTransitionDetails(
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>
    ): String {
        val geofenceTransitionString = getTransitionString(geofenceTransition)

        // Get the Ids of each geofence that was triggered.
        val triggeringGeofencesIdsList = ArrayList<String?>()
        for (geofence in triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.requestId)
        }
        val triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList)
        return "$geofenceTransitionString: $triggeringGeofencesIdsString"
    }

    private fun getTransitionString(transitionType: Int): String {
        return when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Entered"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "Exited"
            else -> "Unknown"
        }
    }
}