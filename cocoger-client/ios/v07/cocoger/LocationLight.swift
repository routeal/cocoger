import Foundation
import CoreData
import EasyMapping
import CoreLocation
import AddressBookUI
import Contacts

class LocationLight : EKObjectModel {
    var accuracy: NSNumber = 0
    var altitude: NSNumber = 0
    var latitude: NSNumber = 0
    var longitude: NSNumber = 0
    var speed: NSNumber = 0
    var created: NSDate = NSDate()
    var city: String = ""
    var country: String = ""
    var county: String = ""
    var state: String = ""
    var street: String = ""
    var town: String = ""
    var timezone: String = ""
    var user: String = ""
    var zip: String = ""
    var device: String = ""

    override class func objectMapping() -> EKObjectMapping{
        var mapping = EKObjectMapping(objectClass: self)

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
        func toDate(key:String!, value:AnyObject!) -> AnyObject! {
            let dateString: String = value as! String
            let formatter = NSDateFormatter()
            formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
            return formatter.dateFromString(dateString)
        }
        // convert a NSDate object into Unix Epoch
        func toEpoch(value:AnyObject!) -> AnyObject! {
            let date:NSDate = value as! NSDate
            return date.timeIntervalSince1970 * 1000 // in milliseconds
        }
        mapping.mapKeyPath("created", toProperty: "created", withValueBlock: toDate, reverseBlock: toEpoch)
        return mapping
    }

    deinit {
        //print("deinit: LocationLight")
    }

    var coordinate: CLLocationCoordinate2D {
        return CLLocationCoordinate2D(latitude: Double(self.latitude),
                                      longitude: Double(self.longitude))
    }

    var location: CLLocation {
        return CLLocation(coordinate: self.coordinate,
                          altitude: Double(self.altitude),
                          horizontalAccuracy: Double(self.accuracy),
                          verticalAccuracy: 0,
                          course: 0,
                          speed: Double(self.speed),
                          timestamp: self.created)
    }

    func rangeCoordinate(range: Int, completion: (coordinate: CLLocationCoordinate2D?) -> Void) {
        var addressComponents: [NSObject : AnyObject] = [:]

        switch range {
        case LocationRange.None:
            completion(coordinate: nil)
            return

        case LocationRange.Street:
            completion(coordinate: self.coordinate)
            return

        case LocationRange.Town:
            addressComponents[kABPersonAddressStreetKey] = self.town
            fallthrough

        case LocationRange.City:
            addressComponents[kABPersonAddressCityKey] = self.city
            fallthrough

        case LocationRange.County:
            addressComponents[kABPersonAddressStateKey] = self.county
            fallthrough

        case LocationRange.State:
            if let county = addressComponents[kABPersonAddressStateKey] as? String {
                addressComponents[kABPersonAddressStateKey] = county + " " + self.state
            } else {
                addressComponents[kABPersonAddressStateKey] = self.state
            }
            fallthrough

        case LocationRange.Country:
            addressComponents[kABPersonAddressCountryKey] = self.country
            fallthrough

        default: break
        }

        let address = ABCreateStringWithAddressDictionary(addressComponents, false)

        if address.isEmpty {
            completion(coordinate: nil)
            return
        }

        // println("rangeCoordinate: \(address)")

        Location.location(address, completion: {(location: CLLocation?) in
            if location == nil {
                completion(coordinate: nil)
            } else {
                completion(coordinate: location!.coordinate)
            }
        })
    }

    func title(range: Int) -> String {
        var addressComponents: [NSObject:AnyObject] = [:]

        switch range {
        case LocationRange.None: return ""

        case LocationRange.Street:
            if self.town.isEmpty {
                addressComponents[kABPersonAddressStreetKey] = self.street
            } else {
                if UIDevice.lang == "ja" {
                    addressComponents[kABPersonAddressStreetKey] = self.town + " " + self.street
                } else {
                    addressComponents[kABPersonAddressStreetKey] = self.street + " " +  self.town
                }
                addressComponents[kABPersonAddressCityKey] = self.city
            }
            break

        case LocationRange.Town:
            if self.town.isEmpty {
                addressComponents[kABPersonAddressCityKey] = self.city
            } else {
                addressComponents[kABPersonAddressStreetKey] = self.town
                addressComponents[kABPersonAddressCityKey] = self.city
            }
            break

        case LocationRange.City:
            addressComponents[kABPersonAddressCityKey] = self.city
            /*
            if self.county.isEmpty {
                addressComponents[kABPersonAddressStateKey] = self.state
            } else {
                addressComponents[kABPersonAddressStateKey] = self.county
            }
            */
            break

        case LocationRange.County:
            if self.state.isEmpty {
                addressComponents[kABPersonAddressStateKey] = self.county
            } else {
                addressComponents[kABPersonAddressStateKey] = self.county + " " + self.state
            }
            break

        case LocationRange.State:
            addressComponents[kABPersonAddressStateKey] = self.state
            //addressComponents[kABPersonAddressCountryKey] = self.country
            break

        case LocationRange.Country:
            addressComponents[kABPersonAddressCountryKey] = self.country
            break

        default: break
        }

        let address = ABCreateStringWithAddressDictionary(addressComponents, false)
        if UIDevice.country.caseInsensitiveCompare("JP") == .OrderedSame {
            return address.stringByReplacingOccurrencesOfString("\n", withString: "").stringByReplacingOccurrencesOfString(" ", withString: "")
        }
        return address.stringByReplacingOccurrencesOfString("\n", withString: "")
    }

    func address(range: Int) -> String {
        var addressComponents: [NSObject:AnyObject] = [:]

        switch range {
        case LocationRange.None: return ""

        case LocationRange.Country:
            addressComponents[kABPersonAddressCountryKey] = self.country
            break

        case LocationRange.Street:
            //addressComponents[kABPersonAddressZIPKey] = self.zip
            addressComponents[kABPersonAddressStreetKey] = self.street
            fallthrough

        case LocationRange.Town:
            if let street = addressComponents[kABPersonAddressStreetKey] as? String {
                if UIDevice.lang == "ja" {
                    addressComponents[kABPersonAddressStreetKey] = self.town + " " + street
                } else {
                    addressComponents[kABPersonAddressStreetKey] = street + " " + self.town
                }
            } else {
                addressComponents[kABPersonAddressStreetKey] = self.town
            }
            fallthrough

        case LocationRange.City:
            addressComponents[kABPersonAddressCityKey] = self.city
            fallthrough

        case LocationRange.County:
            addressComponents[kABPersonAddressStateKey] = self.county
            fallthrough

        case LocationRange.State:
            if let county = addressComponents[kABPersonAddressStateKey] as? String {
                addressComponents[kABPersonAddressStateKey] = county + " " + self.state
            } else {
                addressComponents[kABPersonAddressStateKey] = self.state
            }
            fallthrough

        default: break
        }

        return ABCreateStringWithAddressDictionary(addressComponents, false)
    }

    func address(street: String, town: String, city: String, county: String,
                 state: String, country: String, zip: String) {
        self.street = street
        self.town = town
        self.city = city
        self.county = county
        self.state = state
        self.country = country
        self.zip = zip
    }

}
