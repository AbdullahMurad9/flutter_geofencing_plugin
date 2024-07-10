
import 'flutter_geofencing_platform_interface.dart';

class FlutterGeofencing {
  Future<String?> getPlatformVersion() {
    return FlutterGeofencingPlatform.instance.getPlatformVersion();
  }

  Future<dynamic> addGeoFence({required String id, required double lat, required double long, required int radius}) {
    return FlutterGeofencingPlatform.instance.addGeoFence(id: id,lat: lat,long: long,radius: radius);
  }

  Future removeGeoFence() {
    return FlutterGeofencingPlatform.instance.removeGeoFence();
  }

  Future<dynamic> removeSingleGeofence({required String id,}) {
    return FlutterGeofencingPlatform.instance.removeSingleGeofence(id: id,);
  }
}
