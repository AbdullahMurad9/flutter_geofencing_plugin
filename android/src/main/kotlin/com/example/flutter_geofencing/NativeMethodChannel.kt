package com.example.flutter_geofencing

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.flutter.FlutterInjector
import io.flutter.embedding.android.KeyData.CHANNEL
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.dart.DartExecutor.DartEntrypoint
import io.flutter.embedding.engine.loader.FlutterLoader
import io.flutter.plugin.common.MethodChannel


object NativeMethodChannel {
//    private const val CHANNEL_NAME = "channel"
private const val CHANNEL_NAME = "flutter_geofencing"
    private lateinit var methodChannel: MethodChannel

    fun configureChannel(flutterEngine: FlutterEngine) {
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL_NAME)
    }

    fun showNewIdea(entrtyType: String,eventId: String,context: Context) {
        Log.e("AAAAAAVVV", "AAAAAAAAA")
        if (!this::methodChannel.isInitialized){
            /*val engine = FlutterEngine(context.applicationContext)
            configureChannel(engine)
            val entrypoint = DartEntrypoint("lib/flutter-method-channel.dart", "methodHandler")
            engine.dartExecutor.executeDartEntrypoint(entrypoint)
            methodChannel = MethodChannel(engine.dartExecutor.binaryMessenger, CHANNEL_NAME)*/
            val mainHandler = Handler(Looper.getMainLooper())
            val myRunnable = Runnable() {
                run() {
                    val engine = FlutterEngine(context.applicationContext)
                    val flutterLoader: FlutterLoader = FlutterInjector.instance().flutterLoader()
                    if (!flutterLoader.initialized()) {
                        flutterLoader.startInitialization(context.applicationContext)
                    }
                    flutterLoader.ensureInitializationCompleteAsync(context.applicationContext,null,Handler(Looper.getMainLooper())) {
                        val entryPoint = DartExecutor.DartEntrypoint(flutterLoader.findAppBundlePath(), "backgroundServiceCallback")
                        engine.dartExecutor.executeDartEntrypoint(entryPoint, listOf(entrtyType.plus(",").plus(eventId)))
                        // You can call the method channel here if you need to
                        //val localMethodChannel = MethodChannel(engine.dartExecutor.binaryMessenger, "yourChannelName")
                        //localMethodChannel.invokeMethod("yourMethodNameHere", null)
                        /*val localMethodChannel = MethodChannel(engine.dartExecutor.binaryMessenger, CHANNEL_NAME)
                        localMethodChannel.invokeMethod("showNewIdea", entrtyType.plus(",").plus(eventId))*/
                    }
                }
            }
            mainHandler.post(myRunnable)
            return
        }
        methodChannel.invokeMethod("showNewIdea", entrtyType.plus(",").plus(eventId))
    }
}