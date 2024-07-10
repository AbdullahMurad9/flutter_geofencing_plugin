import Flutter
import UIKit
import CoreLocation
import UserNotifications

public class FlutterGeofencingPlugin: NSObject, FlutterPlugin, CLLocationManagerDelegate, UNUserNotificationCenterDelegate {

  private var locationManager = CLLocationManager()
  private var completion: ((CLLocation) -> Void)?
  private var geofences = [CLCircularRegion]()
  private var locationChannel: FlutterMethodChannel?

  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "flutter_geofencing", binaryMessenger: registrar.messenger())
    let instance = FlutterGeofencingPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
    instance.locationChannel = channel
    instance.setupLocationManager()
    instance.setupNotificationCenter()
  }
 private func setupLocationManager() {
    locationManager.delegate = self
    locationManager.requestAlwaysAuthorization()
    locationManager.allowsBackgroundLocationUpdates = true
    locationManager.showsBackgroundLocationIndicator = true
  }

  private func setupNotificationCenter() {
    UNUserNotificationCenter.current().delegate = self
    UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { (authorized, error) in
      if let error = error {
        print("Error requesting authorization for notifications: \(error.localizedDescription)")
      }
      if authorized {
        print("Notifications authorized.")
      } else {
        print("Notifications not authorized.")
      }
    }
  }

  if #available(iOS 10.0, *) {
            UNUserNotificationCenter.current().delegate = self
        }


        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { (authorized, error) in
            if let error = error {
                print("\(Date()) -- There was an error requesting authorization to use notifications. Error: \(error.localizedDescription) -- \("\(#line) --- OF \(#function) --- IN \(#file)")")
            }
            if authorized {
                UNUserNotificationCenter.current().delegate = self
                print("\(Date()) -- ✅ The user authorized notifications. -- \("\(#line) --- OF \(#function) --- IN \(#file)")")
            } else {
                print("\(Date()) -- ❌ The user did not authorized notifications. -- \("\(#line) --- OF \(#function) --- IN \(#file)")")
            }

            if launchOptions?[UIApplication.LaunchOptionsKey.location] != nil {
  //              self.locationManager = CLLocationManager()
  //              self.locationManager.delegate = self

  //              self.notificationCenter = UNUserNotificationCenter.current()
  //              self.notificationCenter.delegate = self

                UNUserNotificationCenter.current().delegate = self
                self.getUserLocation (completion: { loc in},geofenceData: [])
  //              UIApplication.shared.applicationIconBadgeNumber =  UIApplication.shared.applicationIconBadgeNumber + 1
            } else {
                // your app's "normal" behaviour goes here

            }

        }


        // controller view
        let controller : FlutterViewController = window?.rootViewController as! FlutterViewController
        //Method channel
        locationChannel = FlutterMethodChannel(name: "method.channels/geofence",
                                                   binaryMessenger: controller.binaryMessenger)
        var locationManager = LocationManager(locationChannel: locationChannel)

  // build in function
  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "getPlatformVersion":
      result("iOS " + UIDevice.current.systemVersion)
    case "addGeofence":
          if let args = call.arguments as? [[String: Any]] {
            getUserLocation(completion: { _ in }, geofenceData: args)
          }
    case "removeSingleGeofence":
      if let args = call.arguments as? [String: Any], let id = args["id"] as? String {
        stopMonitoringForSpecificRegion(identifier: id)
      }
    case "removeGeofence":
      stopMonitoringForAllRegions()
    default:
      result(FlutterMethodNotImplemented)
    }

    GeneratedPluginRegistrant.register(with: self)
        return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }

  override func userNotificationCenter(
          _ center: UNUserNotificationCenter,
          willPresent notification: UNNotification,
          withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
      ) {
          // Handle the notification here
          completionHandler([.banner, .sound, .badge]) // You can customize the presentation options
      }

      func postLocalNotifications(eventTitle: String){
          UNUserNotificationCenter.current().delegate = self
          let center = UNUserNotificationCenter.current()
          let content = UNMutableNotificationContent()
          content.title = eventTitle
          content.body = "You've entered a new region"
          content.sound = .default
          let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
          let notificationRequest:UNNotificationRequest = UNNotificationRequest(identifier: "Region", content: content, trigger: trigger)
          center.add(notificationRequest, withCompletionHandler: { (error) in
              if let error = error {
                  print("\(Date()) -- Unable to add notification request: \(error.localizedDescription) -- \("\(#line) --- OF \(#function) --- IN \(#file)")")
              } else{
                  print("\(Date()) -- Successfully Add notification request -- \("\(#line) --- OF \(#function) --- IN \(#file)")")
              }
          })
      }

      //----------

      public func getUserLocation(completion: @escaping ((CLLocation) -> Void), geofenceData: [[String :Any]]) {
          self.completion = completion
          manager.requestAlwaysAuthorization()
          manager.delegate = self
          manager.startUpdatingLocation()
          manager.distanceFilter = 100
          manager.desiredAccuracy = kCLLocationAccuracyBest
          manager.allowsBackgroundLocationUpdates = true
          manager.showsBackgroundLocationIndicator = true

          for i in stride(from: 0, to: geofenceData.count, by: 1){
              let latitude: CLLocationDegrees  = (geofenceData[i])["latitude"] as! Double
              var longitude: CLLocationDegrees  = (geofenceData[i])["longitude"] as! Double
              var radius: Double = (geofenceData[i])["radius"] as! Double
              var identifier: String = (geofenceData[i])["id"] as! String
              print("\(latitude), \(longitude), \(radius), \(identifier)")
              let geoFenceRegion:CLCircularRegion = CLCircularRegion(center: CLLocationCoordinate2DMake(latitude,longitude), radius: radius, identifier: identifier) //
              geoFenceRegion.notifyOnExit = true
              geoFenceRegion.notifyOnEntry = true
              manager.startMonitoring(for: geoFenceRegion)
              geofences.append(geoFenceRegion)
          }

      }

      public func stopMonitoringForSpecificRegion(identifier: String){
          for geofence in geofences{
              if(geofence.identifier == identifier){
                  manager.stopMonitoring(for: geofence)
              }
          }
      }

      public func stopMonitoringForAllRegions(){
          for geofence in geofences{
              manager.stopMonitoring(for: geofence)

          }
      }

  //    public func saveToDatabase(){
  //        do{
  //            let newLocationData = LocationData()
  //            newLocationData.latitude = 31.4756654
  //            newLocationData.longitude = 74.3409037
  //            newLocationData.radius = 100
  //            newLocationData.identifier = "M3T"
  //
  //            realm.beginWrite()
  //
  //            realm.add(newLocationData)
  //
  //            try! realm.commitWrite()
  //        }catch{
  //            print("error")
  //        }
  //    }

  //    public func fetchFromDatabase(){
  //        let locationDataList = realm.objects(LocationData.self)
  //    }

  //    public func deleteAllFromDatabase(){
  //        realm.beginWrite()
  //        realm.delete(realm.objects(LocationData.self))
  //        try! realm.commitWrite()
  //    }

      public func resolveLocationName(with location: CLLocation, completion: @escaping ((String?) -> Void)) {
          let geocoder = CLGeocoder()
          geocoder.reverseGeocodeLocation(location, preferredLocale: .current) { placemarks, error in
              guard let place = placemarks?.first, error == nil else {
                  completion(nil)
                  return
              }
              print("\(Date()) -- Place : \(place) -- \("\(#line) --- OF \(#function) --- IN \(#file)")")
              var name = ""
              if let locality = place.locality {
                  name += locality
              }
              if let adminRegion = place.administrativeArea {
                  name += ", \(adminRegion)"
              }
              print("\(Date()) -- Place's Name : \(name) -- \("\(#line) --- OF \(#function) --- IN \(#file)")")
              completion(name)
          }
      }
  }

  // MARK: - CLLocationManagerDelegate
  extension AppDelegate {
      func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
          guard let location = locations.first else {return}
          print("Location : \(location)")

          completion?(location)
      }

      func locationManager(_ manager: CLLocationManager, monitoringDidFailFor region: CLRegion?, withError error: Error) {
      //        Logger.write(text: "\(error.localizedDescription)", to: kLogsFile)
          self.postLocalNotifications(eventTitle: "Error: \(error.localizedDescription)")
          }

      func locationManager(_ manager: CLLocationManager, didEnterRegion region: CLRegion) {
          print("\(Date()) -- Entered: \(region.identifier) ==> Do something -- \("\(#line) --- OF \(#function) --- IN \(#file)")")
          self.postLocalNotifications(eventTitle: "Entered: \(region.identifier)")
          locationChannel.invokeMethod("addGeofence", arguments: ["In", region.identifier])
          UIApplication.shared.applicationIconBadgeNumber =  UIApplication.shared.applicationIconBadgeNumber + 1
      }

      func locationManager(_ manager: CLLocationManager, didExitRegion region: CLRegion) {
          print("\(Date()) -- Exited: \(region.identifier) ==> Do something -- \("\(#line) --- OF \(#function) --- IN \(#file)")")
          self.postLocalNotifications(eventTitle: "Exited: \(region.identifier)")
          locationChannel.invokeMethod("addGeofence", arguments: ["Out", region.identifier])
          UIApplication.shared.applicationIconBadgeNumber =  UIApplication.shared.applicationIconBadgeNumber + 1
      }

      func locationManager(_ manager: CLLocationManager, didDetermineState region: CLRegion) {
          print("\(Date()) -- Inside: \(region.identifier) ==> Do something -- \("\(#line) --- OF \(#function) --- IN \(#file)")")
          self.postLocalNotifications(eventTitle: "Inside: \(region.identifier)")

          UIApplication.shared.applicationIconBadgeNumber =  UIApplication.shared.applicationIconBadgeNumber + 1
      }
}

// MARK: - Notification ==> Register Notification on AppDelegate
extension LocationManager {
}