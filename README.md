# flutter_geofencing

A new Flutter plugin project.

## Getting Started

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/developing-packages/),
a specialized package that includes platform-specific implementation code for
Android and/or iOS.

For help getting started with Flutter development, view the
[online documentation](https://flutter.dev/docs), which offers tutorials,
samples, guidance on mobile development, and a full API reference.

## Android
Add the following lines to your AndroidManifest.xml to register the background service for geofencing:

    <receiver
    android:name="com.example.flutter_geofencing.GeofenceBroadcastReceiver"
    android:enabled="true"
    android:exported="true" />
    
    <service
    android:name="com.example.flutter_geofencing.geofencing.GeofenceTransitionsJobIntentService"
    android:exported="true"
    android:permission="android.permission.BIND_JOB_SERVICE" />

Also request the correct permissions for geofencing:

    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

