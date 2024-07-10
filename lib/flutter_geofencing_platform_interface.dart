import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_geofencing_method_channel.dart';

abstract class FlutterGeofencingPlatform extends PlatformInterface {
  /// Constructs a FlutterGeofencingPlatform.
  FlutterGeofencingPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterGeofencingPlatform _instance = MethodChannelFlutterGeofencing();

  /// The default instance of [FlutterGeofencingPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterGeofencing].
  static FlutterGeofencingPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterGeofencingPlatform] when
  /// they register themselves.
  static set instance(FlutterGeofencingPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<dynamic> addGeoFence({required String id, required double lat, required double long, required int radius}) {
    throw UnimplementedError('addGeoFence() has not been implemented.');
  }

  Future<dynamic> removeGeoFence() {
    throw UnimplementedError('removeGeoFence() has not been implemented.');
  }

  Future<dynamic> removeSingleGeofence({required String id}) {
    throw UnimplementedError('addGeoFence() has not been implemented.');
  }
}

// "lat": 31.475839994232548 ,
// "long": 74.3425799238205 ,
// "radius":  500,