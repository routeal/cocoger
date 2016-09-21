//
//  SignificantLocationTracker.swift
//  ulocate
//
//  Created by Hiroshi Watanabe on 6/10/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit
import CoreLocation

enum LocationUpdateMode {
    case None
    case Significant
    case Precise
}

protocol LocationTrackerDelegate: class {
    func locationAuthorized(status: Bool) -> Void

    func locationUpdated(location: CLLocation) -> Void
}

let locationTracker = LocationTracker()

class LocationTracker: NSObject, CLLocationManagerDelegate {

    var locationManager: CLLocationManager = CLLocationManager()

    var locationUpdateMode: LocationUpdateMode = .None

    var pingUpdateMode: LocationUpdateMode = .None

    weak var delegate: LocationTrackerDelegate?

    override init() {
        super.init()
    }

    func checkLocationServicesEnabled() {
        // lazy delegation
        locationManager.delegate = self

        if CLLocationManager.locationServicesEnabled() &&
          CLLocationManager.authorizationStatus() == .AuthorizedAlways {
            if delegate != nil {
                delegate!.locationAuthorized(true)
            }
            return
        }

        if CLLocationManager.authorizationStatus() != .AuthorizedAlways {
            locationManager.requestAlwaysAuthorization()
        }
    }

    func locationManager(manager: CLLocationManager, didChangeAuthorizationStatus status: CLAuthorizationStatus) {
        if !CLLocationManager.locationServicesEnabled() {
            return
        }

        // .Denied when the user rejects location access
        if (status == CLAuthorizationStatus.Denied) {
            NSLog("Location Service authorizationStatus denied")
            if delegate != nil {
                delegate!.locationAuthorized(false)
            }
        } else if (status == CLAuthorizationStatus.Restricted) {
            NSLog("Location Service authorizationStatus failed:Restricted")
            // not handled
        } else if (status == CLAuthorizationStatus.AuthorizedWhenInUse) {
            NSLog("Location Service authorizationStatus failed:AuthorizedWhenInUse")
            // not handled
        }
        // .NotDetermined when the user has never elected the app autorization before
        else if status == CLAuthorizationStatus.NotDetermined {
            NSLog("Location Service not determined: never be authorized before")
            locationManager.requestAlwaysAuthorization()
        }
        // .AuthorizedAlways when the app is allowed to access
        // location always, which differs from .AuthorizedWhenInUse
        else if status == CLAuthorizationStatus.AuthorizedAlways {
            NSLog("Location Service authorized")
            if delegate != nil {
                delegate!.locationAuthorized(true)
            }
        }
    }

    func locationManager(manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        // NSLog("LocationTracker Update")
        if let location = locations.last {
            if pingUpdateMode != .None {
                NSLog("pingUpdateMode")
                Location.save(location, completion: nil)
                start(pingUpdateMode)
                pingUpdateMode = .None
            }

            if locationUpdateMode == .Significant {
                if Location.shouldSave(location, significant: true) {
                    Location.save(location, completion: nil)
                }
            } else if locationUpdateMode == .Precise {
                if delegate != nil {
                    delegate!.locationUpdated(location)
                } else if Location.shouldSave(location) {
                    Location.save(location, completion: nil)
                }
            }
        }
    }

    func locationManager(manager: CLLocationManager, didFailWithError error: NSError) {
        NSLog("didFailWithError: " + error.description)
    }

    func start(mode: LocationUpdateMode) {
        if mode == .Significant {
            NSLog("startMonitoringSignificantLocationChanges")
            locationUpdateMode = .Significant
            locationManager.stopUpdatingLocation()
            locationManager.desiredAccuracy = kCLLocationAccuracyBest
            locationManager.distanceFilter = kCLDistanceFilterNone
            locationManager.startMonitoringSignificantLocationChanges()
        } else if mode == .Precise {
            NSLog("startUpdatingLocation")
            locationUpdateMode = .Precise
            locationManager.stopMonitoringSignificantLocationChanges()
            locationManager.desiredAccuracy = kCLLocationAccuracyBest
            locationManager.distanceFilter = 5
            locationManager.startUpdatingLocation()
        }
    }

    func ping() {
        pingUpdateMode = locationUpdateMode
        locationManager.stopMonitoringSignificantLocationChanges()
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.distanceFilter = 5
        locationManager.startUpdatingLocation()
    }

    func stop() {
        if locationUpdateMode == .Significant {
            locationManager.stopMonitoringSignificantLocationChanges()
        } else if locationUpdateMode == .Precise {
            locationManager.stopUpdatingLocation()
        }
        locationUpdateMode = .None
    }
}
