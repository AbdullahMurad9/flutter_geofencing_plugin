package com.example.flutter_geofencing

import GeofenceErrorMessages
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry


/** FlutterGeofencingPlugin */
class FlutterGeofencingPlugin: FlutterPlugin, MethodCallHandler, OnCompleteListener<Void?>,ActivityAware,
  PluginRegistry.RequestPermissionsResultListener {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var context: Context
//  private flutterBinding :FlutterPluginBinding
private var activity: Activity? = null
  private val pendingResult: MethodChannel.Result? = null
  /**
   * Tracks whether the user requested to add or remove geofences, or to do neither.
   */
  private enum class PendingGeofenceTask {
    ADD, REMOVE, NONE
  }

  /**
   * Provides access to the Geofencing API.
   */
  private var mGeofencingClient: GeofencingClient? = null

  /**
   * The list of geofences used in this sample.
   */
  private var mGeofenceList: ArrayList<Geofence>? = arrayListOf()

  /**
   * Used when requesting to add or remove geofences.
   */
  private var mGeofencePendingIntent: PendingIntent? = null

  // Buttons for kicking off the process of adding or removing geofences.
  private var mAddGeofencesButton: Button? = null
  private var mRemoveGeofencesButton: Button? = null
  private var mPendingGeofenceTask = PendingGeofenceTask.NONE
  var list: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    arrayOf(
      Manifest.permission.ACCESS_COARSE_LOCATION,
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.POST_NOTIFICATIONS
    )
  } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    arrayOf(
      Manifest.permission.ACCESS_COARSE_LOCATION,
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
  } else {
    arrayOf(
      Manifest.permission.ACCESS_COARSE_LOCATION,
      Manifest.permission.ACCESS_FINE_LOCATION
    )
  }

//  private val CHANNEL = "method.channels/geofence"

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_geofencing")
    context=flutterPluginBinding.applicationContext
//    val binding=
    channel.setMethodCallHandler(this)

    // onstart of flutteractivit
    if (!checkPermissions()) {
      //requestPermissions();
      showBackgroundLocationDialog()
    } else {
      if (handleBackgroundPermission()) return
      performPendingGeofenceTask()
    }

    // oncreate of flutteractivit

    list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS
      )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
      )
    } else {
      arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
      )
    }
    // Get the UI widgets.
    //mAddGeofencesButton = findViewById<View>(R.id.add_geofences_button) as Button
    //mRemoveGeofencesButton = findViewById<View>(R.id.remove_geofences_button) as Button

    // Empty list for storing geofences.
    mGeofenceList = ArrayList()

    // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
    mGeofencePendingIntent = null
    setButtonsEnabledState()

    // Get the geofences used. Geofence data is hard coded in this sample.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      populateGeofenceList()
    }
    mGeofencingClient = context?.let { LocationServices.getGeofencingClient(it) }

  }

  override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    } else if (call.method == "addGeofence") {
      //Log.e("AAAAAAAAA", "Eeeee")
      val id = call.argument("id") as String?
      val lat = call.argument("lat") as Double?
      val long = call.argument("long") as Double?
      val radius = call.argument("radius") as Int?
      addGeofencesButtonHandler(id!!,LatLng(lat!!, long!!),radius!!)
//      addGeofencesButtonHandler("test",LatLng(31.475839994232548, 74.3425799238205),500)
      /*val batteryLevel = getBatteryLevel() addGeofencesButtonHandler(id!!,LatLng(lat!!, long!!),radius!!)

  if (batteryLevel != -1) {
      result.success(batteryLevel)
  } else {
      result.error("UNAVAILABLE", "Battery level not available.", null)
  }*/

    }else if (call.method == "removeGeofence") {
      removeGeofencesButtonHandler()
    }else if (call.method == "removeSingleGeofence") {
      val id = call.argument("id") as String?
      removeSingleGeofencesHandler(id!!)
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  /** methods*/
  fun removeSingleGeofencesHandler(id: String){
    mGeofencingClient!!.removeGeofences(listOf(id))
  }

  private fun handleBackgroundPermission(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      list = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
      )
    }
    val permissionState = ActivityCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
    if (permissionState != PackageManager.PERMISSION_GRANTED) {
      requestPermissions()
      //showBackgroundLocationDialog()
      return true
    }
    return false
  }

  private val geofencingRequest: GeofencingRequest
    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private get() {
      val builder = GeofencingRequest.Builder()

      // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
      // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
      // is already inside that geofence.
      builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)

      // Add the geofences to be monitored by geofencing service.
      builder.addGeofences(mGeofenceList!!)

      // Return a GeofencingRequest.
      return builder.build()
    }

  /**
   * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
   * specified geofences. Handles the success or failure results returned by addGeofences().
   */
  fun addGeofencesButtonHandler(view: View?) {
    if (!checkPermissions()) {
      mPendingGeofenceTask = PendingGeofenceTask.ADD
      //requestPermissions();
      showBackgroundLocationDialog()
      return
    }
    addGeofences()
  }

  fun addGeofencesButtonHandler(id:String, latLng: LatLng, radius:Int) {
    mGeofenceList = ArrayList()
    mGeofenceList!!.add(
      Geofence.Builder() // Set the request ID of the geofence. This is a string to identify this
        // geofence.
        .setRequestId(id) // Set the circular region of this geofence.
        .setCircularRegion(
          latLng.latitude,
          latLng.longitude,
          /*Constants.GEOFENCE_RADIUS_IN_METERS*/radius.toFloat()
        ) // Set the expiration duration of the geofence. This geofence gets automatically
        // removed after this period of time.
        .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS) // Set the transition types of interest. Alerts are only generated for these
        // transition. We track entry and exit transitions in this sample.
        .setTransitionTypes(
          Geofence.GEOFENCE_TRANSITION_ENTER or
                  Geofence.GEOFENCE_TRANSITION_EXIT
        ) // Create the geofence.
        .build()
    )
    if (!checkPermissions()) {
      mPendingGeofenceTask = PendingGeofenceTask.ADD
      //requestPermissions();
      showBackgroundLocationDialog()
      return
    }
    addGeofences()
  }

  fun showBackgroundLocationDialog() {
    AlertDialog.Builder(context)
      .setTitle("Background Location")
      .setMessage("This app collects location data to enable location based reminders even when the app is closed.") // Specifying a listener allows you to take an action before dismissing the dialog.
      // The dialog is automatically dismissed when a dialog button is clicked.
      .setPositiveButton(
        android.R.string.yes
      ) { dialog, which -> // Continue with delete operation
        requestPermissions()
      } // A null listener allows the button to dismiss the dialog and take no further action.
      .setNegativeButton(android.R.string.no, null)
      .setIcon(android.R.drawable.ic_dialog_alert)
      .show()
  }

  /**
   * Adds geofences. This method should be called after the user has granted the location
   * permission.
   */
  @SuppressLint("MissingPermission")
  private fun addGeofences() {
    if (!checkPermissions()) {
      showSnackbar(context.getString(R.string.insufficient_permissions))
      return
    }
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

      return
    }
    mGeofencingClient?.addGeofences(geofencingRequest, geofencePendingIntent!!)
        ?.addOnCompleteListener(this)
  }

  /**
   * Removes geofences, which stops further notifications when the device enters or exits
   * previously registered geofences.
   */
  fun removeGeofencesButtonHandler(view: View?) {
    if (!checkPermissions()) {
      mPendingGeofenceTask = PendingGeofenceTask.REMOVE
      //requestPermissions();
      showBackgroundLocationDialog()
      return
    }
    removeGeofences()
  }

  fun removeGeofencesButtonHandler() {
    if (!checkPermissions()) {
      mPendingGeofenceTask = PendingGeofenceTask.REMOVE
      //requestPermissions();
      showBackgroundLocationDialog()
      return
    }
    removeGeofences()
  }

  /**
   * Removes geofences. This method should be called after the user has granted the location
   * permission.
   */
  private fun removeGeofences() {
    if (!checkPermissions()) {
      showSnackbar("insufficient_permissions")
      return
    }
    mGeofencingClient!!.removeGeofences(geofencePendingIntent!!).addOnCompleteListener(this)
  }

  /**
   * Runs when the result of calling [.addGeofences] and/or [.removeGeofences]
   * is available.
   *
   * @param task the resulting Task, containing either a result or error.
   */
  override fun onComplete(task: Task<Void?>) {
    mPendingGeofenceTask = PendingGeofenceTask.NONE
    if (task.isSuccessful) {
      updateGeofencesAdded(!geofencesAdded)
      setButtonsEnabledState()
      val messageId =
        if (geofencesAdded) R.string.geofences_added else R.string.geofences_removed
      // Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show()
    } else {
      // Get the status code for the error and log it using a user-friendly message.
      val errorMessage = GeofenceErrorMessages.getErrorString(context, task.exception)
      Log.w(TAG, errorMessage)
    }
  }

  private val geofencePendingIntent: PendingIntent?
    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private get() {
      // Reuse the PendingIntent if we already have it.
      if (mGeofencePendingIntent != null) {
        return mGeofencePendingIntent
      }
      val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
      // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
      // addGeofences() and removeGeofences().
      mGeofencePendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE)
      } else {
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
      }
      return mGeofencePendingIntent
    }

  /**
   * This sample hard codes geofence data. A real app might dynamically create geofences based on
   * the user's location.
   */
  @RequiresApi(Build.VERSION_CODES.N)
  private fun populateGeofenceList() {
    Constants.BAY_AREA_LANDMARKS.forEach { (s, latLng) ->
      mGeofenceList!!.add(
        Geofence.Builder() // Set the request ID of the geofence. This is a string to identify this
          // geofence.
          .setRequestId(s) // Set the circular region of this geofence.
          .setCircularRegion(
            latLng.latitude,
            latLng.longitude,
            Constants.GEOFENCE_RADIUS_IN_METERS
          ) // Set the expiration duration of the geofence. This geofence gets automatically
          // removed after this period of time.
          .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS) // Set the transition types of interest. Alerts are only generated for these
          // transition. We track entry and exit transitions in this sample.
          .setTransitionTypes(
            Geofence.GEOFENCE_TRANSITION_ENTER or
                    Geofence.GEOFENCE_TRANSITION_EXIT
          ) // Create the geofence.
          .build()
      )
    }

  }

  /**
   * Ensures that only one button is enabled at any time. The Add Geofences button is enabled
   * if the user hasn't yet added geofences. The Remove Geofences button is enabled if the
   * user has added geofences.
   */
  private fun setButtonsEnabledState() {
    if (geofencesAdded) {
      //mAddGeofencesButton!!.isEnabled = false
      //mRemoveGeofencesButton!!.isEnabled = true
    } else {
      // mAddGeofencesButton!!.isEnabled = true
      // mRemoveGeofencesButton!!.isEnabled = false
    }
  }

  /**
   * Shows a [Snackbar] using `text`.
   *
   * @param text The Snackbar text.
   */
  private fun showSnackbar(text: String) {
    Toast.makeText(context, text,Toast.LENGTH_LONG).show()
    // method_channel_code
//    val container = findViewById<View>(android.R.id.content)
//    if (container != null) {
//      Snackbar.make(container, text, Snackbar.LENGTH_LONG).show()
//    }
  }

  /**
   * Shows a [Snackbar].
   *
   * @param mainTextStringId The id for the string resource for the Snackbar text.
   * @param actionStringId   The text of the action item.
   * @param listener         The listener associated with the Snackbar action.
   */
  private fun showSnackbar(
    mainTextStringId: Int, actionStringId: Int,
    listener: View.OnClickListener
  ) {
    Snackbar.make(
      activity!!.findViewById(android.R.id.content),
      context.getString(mainTextStringId),
      Snackbar.LENGTH_INDEFINITE
    )
      .setAction(context.getString(actionStringId), listener).show()
  }

  private val geofencesAdded: Boolean
    /**
     * Returns true if geofences were added, otherwise false.
     */
    private get() = context.getSharedPreferences(Constants.GEOFENCES_ADDED_KEY, Context.MODE_PRIVATE).getBoolean(
      Constants.GEOFENCES_ADDED_KEY, false
    )

  /**
   * Stores whether geofences were added ore removed in [SharedPreferences];
   *
   * @param added Whether geofences were added or removed.
   */

  private fun updateGeofencesAdded(added: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
      //PreferenceManager.getDefaultSharedPreferences(context)
//      context.getSharedPreferences(Constants.GEOFENCES_ADDED_KEY, Context.MODE_PRIVATE)
//        .edit().clear()
//        .apply()
      context.getSharedPreferences(Constants.GEOFENCES_ADDED_KEY, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(Constants.GEOFENCES_ADDED_KEY, added)
        .apply()
      Log.i("tet", context.getSharedPreferences(Constants.GEOFENCES_ADDED_KEY, Context.MODE_PRIVATE).getBoolean(
        Constants.GEOFENCES_ADDED_KEY, false
      ).toString())

    }
  }

  /**
   * Performs the geofencing task that was pending until location permission was granted.
   */
  private fun performPendingGeofenceTask() {
    if (mPendingGeofenceTask == PendingGeofenceTask.ADD) {
      addGeofences()
    } else if (mPendingGeofenceTask == PendingGeofenceTask.REMOVE) {
      removeGeofences()
    }
  }

  /**
   * Return the current state of the permissions needed.
   */
  private fun checkPermissions(): Boolean {
    for (s in list) {
      val permissionState = ActivityCompat.checkSelfPermission(
        context,
        s
      )
      if (permissionState != PackageManager.PERMISSION_GRANTED) {
        return false
      }
    }
    return true
  }

  private fun requestPermissions() {
    var count = 0
    for (s in list) {
      val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
        context as Activity,
        s
      )
      if (shouldProvideRationale) {
        count++
      }
    }
    /*        boolean shouldProvideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);*/

    // Provide an additional rationale to the user. This would happen if the user denied the
    // request previously, but didn't check the "Don't ask again" checkbox.
    if ( /*shouldProvideRationale*/count > 0) {
      Log.i(TAG, "Displaying permission rationale to provide additional context.")
      showSnackbar(
        R.string.permission_rationale, android.R.string.ok
      ) { // Request permission
        ActivityCompat.requestPermissions(
          context as Activity,
          list,
          REQUEST_PERMISSIONS_REQUEST_CODE
        )
      }
    } else {
      Log.i(TAG, "Requesting permission")
      // Request permission. It's possible this can be auto answered if device policy
      // sets the permission in a given state or the user denied the permission
      // previously and checked "Never ask again".
      ActivityCompat.requestPermissions(
        context as Activity,
        list,
        REQUEST_PERMISSIONS_REQUEST_CODE
      )
    }
  }

  /**
   * Callback received when a permissions request has been completed.
   */
//  open fun onRequestPermissionsResult(
//    requestCode: Int,
//    permissions: Array<String?>?,
//    @NonNull grantResults: IntArray
//  ): Boolean {
//    if (requestCode == 1) {
//      if (pendingResult != null) {
//        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//          pendingResult.success(true)
//        } else {
//          pendingResult.success(false)
//        }
//        pendingResult = null
//      }
//      return true
//    }
//    return false
//  }
  override fun onRequestPermissionsResult(
    requestCode: Int, permissions: Array<String>,
    grantResults: IntArray
  ): Boolean {
//    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    Log.i(TAG, "onRequestPermissionResult")
    if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
      if (grantResults.size <= 0) {
        // If user interaction was interrupted, the permission request is cancelled and you
        // receive empty arrays.
        Log.i(TAG, "User interaction was cancelled.")
      } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Log.i(TAG, "Permission granted.")
        if (handleBackgroundPermission()) return true
        performPendingGeofenceTask()
      } else {
        // Permission denied.

        // Notify the user via a SnackBar that they have rejected a core permission for the
        // app, which makes the Activity useless. In a real app, core permissions would
        // typically be best requested during a welcome-screen flow.

        // Additionally, it is important to remember that a permission might have been
        // rejected without asking the user for permission (device policy or "Never ask
        // again" prompts). Therefore, a user interface affordance is typically implemented
        // when permissions are denied. Otherwise, your app could appear unresponsive to
        // touches or interactions which have required permissions.
        showSnackbar(
          R.string.permission_denied_explanation, R.string.settings
        ) { // Build intent that displays the App settings screen.
          val intent = Intent()
          intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
          val uri = Uri.fromParts(
            "package",
            BuildConfig.LIBRARY_PACKAGE_NAME, null
          )
          intent.data = uri
          intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
          activity?.startActivity(intent)
        }
        mPendingGeofenceTask = PendingGeofenceTask.NONE
      }
      return true
    }
    return false
  }


  companion object {
    private val TAG = "Flutter MainActivity"
    private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {

    activity = binding.getActivity();
    binding.addRequestPermissionsResultListener(this);
  }

  override fun onDetachedFromActivityForConfigChanges() {

    activity = null;
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {

    activity = binding.getActivity();
    binding.addRequestPermissionsResultListener(this);
  }

  override fun onDetachedFromActivity() {
    activity = null;
  }
}
