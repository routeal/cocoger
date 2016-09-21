//
//  SignificantLocationTracker.swift
//  ulocate
//
//  Created by Hiroshi Watanabe on 6/10/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit
import CoreLocation
import INTULocationManager

enum LocationUpdateMode {
    case None
    case Significant
    case Precise
}

protocol LocationTrackerDelegate: class {
    func locationAuthorized(status: Bool) -> Void

    func locationUpdated(location: CLLocation) -> Void
}

let locationTracker = IntuLocationTracker()

class IntuLocationTracker: NSObject, CLLocationManagerDelegate {

    var locationManager: CLLocationManager = CLLocationManager()

    var locationUpdateMode: LocationUpdateMode = .None

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

    func locationManager(manager: CLLocationManager, didFailWithError error: NSError) {
        NSLog("didFailWithError: " + error.description)
    }

    var locationRequestID: INTULocationRequestID = NSNotFound

    func startLocationUpdateSubscription() {
        let locMgr = INTULocationManager.sharedInstance()
        locationRequestID = locMgr.subscribeToLocationUpdatesWithDesiredAccuracy(.House) {
            (location: CLLocation!, accuracy: INTULocationAccuracy, status:INTULocationStatus!) in
            if status == .Success {
                if self.delegate != nil {
                    self.delegate!.locationUpdated(location)
                } else if Location.shouldSave(location) {
                    Location.save(location, completion: nil)
                }
            }
        }
    }

    func startMonitoringSignificantLocationChanges() {
        let locMgr = INTULocationManager.sharedInstance()
        locationRequestID = locMgr.subscribeToSignificantLocationChangesWithBlock() {
            (location: CLLocation!, accuracy: INTULocationAccuracy, status: INTULocationStatus!) in
            if status == .Success {
                if Location.shouldSave(location, significant: true) {
                    Location.save(location, completion: nil)
                }
            }
        }
    }

    func start(mode: LocationUpdateMode) {
        if locationUpdateMode != .None {
            if locationUpdateMode == mode {
                // already started
                return
            }
            // stop the other mode
            stop()
        }

        if mode == .Significant {
            NSLog("startMonitoringSignificantLocationChanges")
            locationUpdateMode = .Significant
            startMonitoringSignificantLocationChanges()
        } else if mode == .Precise {
            NSLog("startUpdatingLocation")
            locationUpdateMode = .Precise
            startLocationUpdateSubscription()
        }
    }

    func stop() {
        INTULocationManager.sharedInstance().cancelLocationRequest(self.locationRequestID)
        self.locationRequestID = NSNotFound
        locationUpdateMode = .None
    }

    func ping() {
    }

}
