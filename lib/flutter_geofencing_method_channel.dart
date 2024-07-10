import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_geofencing_platform_interface.dart';

/// An implementation of [FlutterGeofencingPlatform] that uses method channels.
class MethodChannelFlutterGeofencing extends FlutterGeofencingPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_geofencing');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<dynamic> addGeoFence({required String id, required double lat, required double long, required int radius}) async {
    final version = await methodChannel.invokeMethod<dynamic>('addGeofence', {
      "id": id,
      "lat": lat ,
      "long": long ,
      "radius":  radius,
    });
    return version;
  }

  @override
  Future<dynamic> removeGeoFence() async {
    final version = await methodChannel.invokeMethod<dynamic>('removeGeofence');
    return version;
  }

  @override
  Future<dynamic> removeSingleGeofence({required String id}) async {
    final version = await methodChannel.invokeMethod<dynamic>('addGeofence', {
      "id": id,
    });
    return version;
  }
}
