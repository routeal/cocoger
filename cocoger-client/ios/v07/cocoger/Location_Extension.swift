//
//  Location+Method.swift
//  ulocate
//
//  Created by Hiroshi Watanabe on 6/10/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import Foundation
import EasyMapping
import MagicalRecord
import CoreLocation

extension Location {

    override class func objectMapping() -> EKManagedObjectMapping {
        var mapping = EKManagedObjectMapping(entityName: "Location")

        mapping.mapPropertiesFromArray(["accuracy"])
        mapping.mapPropertiesFromArray(["altitude"])
        mapping.mapPropertiesFromArray(["city"])
        mapping.mapPropertiesFromArray(["country"])
        mapping.mapPropertiesFromArray(["county"])
        mapping.mapPropertiesFromArray(["latitude"])
        mapping.mapPropertiesFromArray(["longitude"])
        mapping.mapPropertiesFromArray(["speed"])
        mapping.mapPropertiesFromArray(["state"])
        mapping.mapPropertiesFromArray(["street"])
        mapping.mapPropertiesFromArray(["town"])
        mapping.mapPropertiesFromArray(["timezone"])
        mapping.mapPropertiesFromArray(["user"])
        mapping.mapPropertiesFromArray(["device"])
        mapping.mapPropertiesFromArray(["zip"])

        // convert a UTC format date string into NSDate
        func toDate(key:String!, value:AnyObject!, c:NSManagedObjectContext!) -> AnyObject! {
            let dateString: String = value as! String
            let formatter = NSDateFormatter()
            formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
            return formatter.dateFromString(dateString)
        }

        // convert a NSDate object into Unix Epoch
        func toEpoch(value:AnyObject!, c:NSManagedObjectContext!) -> AnyObject! {
            let date:NSDate = value as! NSDate
            return date.timeIntervalSince1970 * 1000 // in milliseconds
        }

        mapping.mapKeyPath("created", toProperty: "created", withValueBlock: toDate, reverseBlock: toEpoch)

        return mapping
    }


    class func upload() {
        let MaxUploadLocations = 20

        var locations: [Location] = Location.MR_findAll() as! [Location]

        print("========== UPLOAD ================ : \(locations.count)")

        if locations.isEmpty {
            NSLog("No location to upload ")
            return
        }

        // json array
        var copied: [AnyObject] = []

        let loads = locations.count > MaxUploadLocations ? MaxUploadLocations : locations.count

        // upload the first 20 locations
        for var i = 0; i < loads ; i++ {
            // convert into json
            let obj: AnyObject = locations[i].serializedObjectInContext(
                                     NSManagedObjectContext.MR_defaultContext())
            copied.append(obj)
        }

        let parameters = ["locations": copied]

        Rest.request(Router(api: "/m/locations", method: "POST",
                            parameters: parameters, token: User.authToken),
                     completion: { (status: Bool, response: AnyObject?) -> Void in
            if status {
                NSLog("succeeded to upload the locations")
                for var i = 0; i < copied.count ; i++ {
                    locations[i].MR_deleteEntity()
                }
                NSManagedObjectContext.MR_defaultContext().MR_saveToPersistentStoreAndWait()
            } else {
                let json = response as! Dictionary<String, AnyObject>
                if let msg = json["message"] as? String {
                    NSLog("failed to upload the locations: " + msg)
                }
            }
        })
    }

    class func shouldSave(location: CLLocation, significant: Bool = false) -> Bool {
        let MinimumDistance: Double = 15 // meter

        if !User.available {
            NSLog("Location not saved -  user not logged in")
            return false
        }

        /*
        if (location.timestamp.timeIntervalSinceNow < -5.0 ) {
            NSLog("Location not saved -  too old")
            return false
        }
        */

        if let lastLocation = User.location {
            /*
            if !location.timestamp.isSameDate(lastLocation.timestamp) {
                NSLog("new location is logged in a different day from the last location")
                return true
            }
            */
            if significant {
                return true
            }
            let distance = location.distanceFromLocation(lastLocation)
            NSLog("Location distance: %f", distance)
            if distance < MinimumDistance {
                NSLog("Location not saved - not enough movement")
                return false
            }
            if location.horizontalAccuracy < 0 || location.horizontalAccuracy > 2000 {
                NSLog("inacurate horizontalAccuracy: %f", location.horizontalAccuracy)
                return false
            }
            if location.speed >= 0 {
                if location.speed > 250 {
                    // 900 km/h is flight speed
                    return false
                }

                let speedInKilometersPerHour = location.speed * 3.6
                NSLog("Location speed: %f", speedInKilometersPerHour)

                if speedInKilometersPerHour > 250 && distance < 10000 {
                    NSLog("speedInKilometersPerHour: 250")
                    return false
                }

                else if speedInKilometersPerHour > 200 && distance < 2000 {
                    NSLog("speedInKilometersPerHour: 200")
                    return false
                }

                else if speedInKilometersPerHour > 150 && distance < 1500 {
                    NSLog("speedInKilometersPerHour: 150")
                    return false
                }

                else if speedInKilometersPerHour > 100 && distance < 1000 {
                    NSLog("speedInKilometersPerHour: 100")
                    return false
                }

                else if speedInKilometersPerHour > 80 && distance < 500 {
                    NSLog("speedInKilometersPerHour: 80")
                    return false
                }

                else if speedInKilometersPerHour > 40 && distance < 400 {
                    NSLog("speedInKilometersPerHour: 40")
                    return false
                }

                else if speedInKilometersPerHour > 20 && distance < 200 {
                    NSLog("speedInKilometersPerHour: 20")
                    return false
                }
            } else {
                // something wrong
                if distance > 1000 {
                    NSLog("distance: 1000")
                }
                return false
            }
        }

        return true
    }

    class func save(location: CLLocation,
                    completion: ((status: Bool, street: String, town: String, city: String,
                                  county: String, state: String, country: String, zip: String)
                                 -> Void)? = nil) {

        if RawLocation.MR_numberOfEntities() != 0 {
            var failed: Bool = false
            let data: [RawLocation] = RawLocation.MR_findAll() as! [RawLocation]
            for d in data {
                let l = CLLocation(coordinate: CLLocationCoordinate2D(latitude: Double(d.latitude!),
                                                                      longitude: Double(d.longitude!)),
                                   altitude: Double(d.altitude!),
                                   horizontalAccuracy: Double(d.hAccuracy!),
                                   verticalAccuracy: Double(d.vAccuracy!),
                                   course: Double(d.course!),
                                   speed: Double(d.speed!),
                                   timestamp: d.timestamp!)
                d.MR_deleteEntity()
                save2(l, completion: {
                                      (status: Bool, street: String, town: String, city: String,
                                       county: String, state: String, country: String, zip: String) in
                    failed = status
                })
                if failed {
                    return
                }
            }
        }

        save2(location, completion: completion)
    }

    class func save2(location: CLLocation,
                     completion: ((status: Bool, street: String, town: String, city: String,
                                   county: String, state: String, country: String, zip: String)
                                  -> Void)? = nil) {

        User.location = location

        // FIX: this fails a lot
        Location.placemark(
            location,
            completion: {(status: Bool, street: String, town: String, city: String,
                          county: String, state: String, country: String, zip: String) in

            if !status {
                let raw = RawLocation.MR_createEntity()
                raw.altitude = location.altitude
                raw.latitude = location.coordinate.latitude
                raw.longitude = location.coordinate.longitude
                raw.speed = location.speed
                raw.timestamp = location.timestamp
                raw.hAccuracy = location.horizontalAccuracy
                raw.vAccuracy = location.verticalAccuracy
                raw.course = location.course
                raw.managedObjectContext!.MR_saveToPersistentStoreAndWait()

                print("can't get the placemark from the location")
                if completion != nil {
                    completion!(status: false, street: "", town: "", city: "",
                                county: "", state: "", country: "", zip: "")
                }
                return
            }

            print("street:\(street) town:\(town) city:\(city) ")
            print("county:\(county) state:\(state) country:\(country) zip:\(zip)")

            let db:Location = Location.MR_createEntity()
            db.latitude = location.coordinate.latitude
            db.longitude = location.coordinate.longitude
            db.created = location.timestamp // NSDate
            db.speed = location.speed
            db.altitude = location.altitude
            db.accuracy = location.horizontalAccuracy
            db.user = User.id
            db.timezone = NSTimeZone.localTimeZone().name
            db.zip = zip
            db.country = country
            db.state = state
            db.county = county
            db.city = city
            db.town = town
            db.street = street
            db.managedObjectContext!.MR_saveToPersistentStoreAndWait()

            // just for testing, uploads the best location to the server right away
            Location.upload()

            if completion != nil {
                completion!(status: true, street: street, town: town, city: city,
                            county: county, state: state, country: country, zip: zip)
            }
        })
    }

    class func placemark(location: CLLocation,
                         completion:
                             (status: Bool, street: String, town: String, city: String,
                              county: String, state: String, country: String, zip: String) -> Void) {
        CLGeocoder().reverseGeocodeLocation(location, completionHandler: {(placemarks, error) in

            var zip: String = ""
            var country: String = ""
            var state: String = ""
            var county: String = ""
            var city: String = ""
            var town: String = ""
            var street: String = ""

            if let e = error {
                print("reverse geodcode fail: \(e.localizedDescription)")
                completion(status: false, street: street, town: town, city: city,
                           county: county, state: state, country: country, zip: zip)
                return
            }

            if let _ = placemarks {
                if let pm = placemarks!.last {
                    if pm.postalCode != nil {
                        zip = pm.postalCode!
                    }
                    if pm.country != nil {
                        country = pm.country!
                    }
                    // state or province
                    if pm.administrativeArea != nil {
                        state = pm.administrativeArea!
                    }
                    // additional administrative area information - county
                    if pm.subAdministrativeArea != nil {
                        county = pm.subAdministrativeArea!
                    }
                    // city
                    if pm.locality != nil {
                        city = pm.locality!
                    }
                    if pm.subLocality != nil {
                        // not yet
                    }
                    // street
                    if pm.thoroughfare != nil {
                        town = pm.thoroughfare!
                    }
                    if pm.subThoroughfare != nil {
                        street = pm.subThoroughfare!
                    }
                    /*
                    if pm.subThoroughfare != nil {
                        street = pm.subThoroughfare!
                    }
                    */
                    if pm.ISOcountryCode != nil {
                        let defaults = NSUserDefaults.standardUserDefaults()
                        defaults.setObject(pm.ISOcountryCode, forKey: "ISOcountryCode")
                    }
                    completion(status: true, street: street, town: town, city: city,
                               county: county, state: state, country: country, zip: zip)
                    return
                }
            }

            completion(status: false, street: street, town: town, city: city,
                       county: county, state: state, country: country, zip: zip)
        })
    }

    class func location(address: String, completion: (location: CLLocation?) -> Void) {
        let db = AddressLocation.MR_findFirstByAttribute("address", withValue: address)
        if db != nil {
            if let ref = db.reference {
                let num = ref.intValue + 1
                //print("AddressLocation: \(num)")
                if num < 10 {
                    db.reference = NSNumber(int: num)
                    db.managedObjectContext!.MR_saveToPersistentStoreAndWait()
                }
            }
            completion(location: CLLocation(latitude: Double(db.latitude!),
                                            longitude: Double(db.longitude!)))
            return
        }

        CLGeocoder().geocodeAddressString(address, completionHandler: {(placemarks, error) in
            if let _ = error {
                print("Can't get a location from the address: \(address)")
                completion(location: nil)
            } else {
                if let pms = placemarks {
                    if pms.count > 0 {
                        if let l = pms[0].location {

                            if Int(AddressLocation.MR_numberOfEntities()) > 1 {
                                let filter = NSPredicate(format: "reference < 2")
                                if let lessused = AddressLocation.MR_findAllWithPredicate(filter) {
                                    if let last = lessused.last {
                                        last.MR_deleteEntity()
                                    }
                                }
                            }

                            let db = AddressLocation.MR_createEntity()
                            db.address = address
                            db.latitude = l.coordinate.latitude
                            db.longitude = l.coordinate.longitude
                            db.managedObjectContext!.MR_saveToPersistentStoreAndWait()

                            completion(location: l)
                        }
                    }
                }
            }
        })
    }
}
