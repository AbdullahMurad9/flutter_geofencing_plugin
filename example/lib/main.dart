import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_geofencing_plugin/flutter_geofencing.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  dynamic geoFence;
  final _flutterGeofencingPlugin = FlutterGeofencing();

  @override
  void initState() {
    super.initState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [

            MaterialButton(onPressed: () async {
              geoFence =
                  await _flutterGeofencingPlugin.addGeoFence(id: '123', lat: 31.475839994232548, long: 74.3425799238205, radius: 500);
            },child: Text("Add GeoFence"),),
            SizedBox(height: 20,),
            MaterialButton(onPressed: () async {
              geoFence =
              await _flutterGeofencingPlugin.removeGeoFence();
            },child: Text("Remove GeoFence"),)
          ],
        ),
      ),
    );
  }
}
