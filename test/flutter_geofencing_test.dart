import 'package:flutter_geofencing_plugin/flutter_geofencing.dart';
import 'package:flutter_geofencing_plugin/flutter_geofencing_method_channel.dart';
import 'package:flutter_geofencing_plugin/flutter_geofencing_platform_interface.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterGeofencingPlatform
    with MockPlatformInterfaceMixin
    implements FlutterGeofencingPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  Future addGeoFence({required String id, required double lat, required double long, required int radius}) {
    // TODO: implement addGeoFence
    throw UnimplementedError();
  }

  @override
  Future removeGeoFence() {
    // TODO: implement removeGeoFence
    throw UnimplementedError();
  }

  @override
  Future removeSingleGeofence({required String id}) {
    // TODO: implement removeSingleGeofence
    throw UnimplementedError();
  }
}

void main() {
  final FlutterGeofencingPlatform initialPlatform = FlutterGeofencingPlatform.instance;

  test('$MethodChannelFlutterGeofencing is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterGeofencing>());
  });

  test('getPlatformVersion', () async {
    FlutterGeofencing flutterGeofencingPlugin = FlutterGeofencing();
    MockFlutterGeofencingPlatform fakePlatform = MockFlutterGeofencingPlatform();
    FlutterGeofencingPlatform.instance = fakePlatform;

    expect(await flutterGeofencingPlugin.getPlatformVersion(), '42');
  });
}
