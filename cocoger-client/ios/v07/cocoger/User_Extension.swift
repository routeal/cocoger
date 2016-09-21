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

enum Gender: Int {
    case Boy = 0
    case Girl = 1
}

enum PhotoType: UInt {
    case Asset = 1
    case Photo = 2
    case Facebook = 4
    case Template = 8
}

extension User {

    override class func objectMapping() -> EKManagedObjectMapping {
        let mapping = EKManagedObjectMapping(entityName: "User")

        mapping.mapPropertiesFromDictionary(["authToken":"_authToken"])
        mapping.mapPropertiesFromDictionary(["bod":"_bod"])
        mapping.mapPropertiesFromDictionary(["email":"_email"])
        mapping.mapPropertiesFromDictionary(["gender":"_gender"])
        mapping.mapPropertiesFromDictionary(["id":"_id"])
        mapping.mapPropertiesFromDictionary(["name":"_name"])
        mapping.mapPropertiesFromDictionary(["photoName":"_photoName"])
        mapping.mapPropertiesFromDictionary(["photoType":"_photoType"])
        //mapping.mapPropertiesFromDictionary([   "device":"_device"])
        mapping.mapPropertiesFromDictionary(["myColor":"_myColor"])
        mapping.mapPropertiesFromDictionary(["boyColor":"_boyColor"])
        mapping.mapPropertiesFromDictionary(["girlColor":"_girlColor"])
        mapping.mapPropertiesFromDictionary(["frameColor":"_frameColor"])
        mapping.mapPropertiesFromDictionary(["provider":"_provider"])

        // convert a UTC date format string to NSDate
        let formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        mapping.mapKeyPath("created", toProperty: "_created", withDateFormatter: formatter)
        mapping.mapKeyPath("updated", toProperty: "_revised", withDateFormatter: formatter)

        return mapping
    }

    class func user() -> User? {
        if let obj = User.MR_findAll() {
            if obj.count > 0 {
                return obj[0] as? User
            }
        }
        return nil
    }

    class func signout(completion: (() -> Void)) {
        if let user = User.user() {
            Rest.request(Router(api: "/m/auth/signout", method: "GET", token: user._authToken),
                         completion: {(status: Bool, response: AnyObject?) -> Void in
                user._authToken = ""
                user.managedObjectContext!.MR_saveToPersistentStoreAndWait()
                Friend.MR_truncateAll()
                Image.MR_truncateAll()
                completion()
            })
        }
    }

    class func deleteAccount(completion: (() -> Void)) {
        if let user = User.user() {
            Rest.request(Router(api: "/m/users", method: "DELETE", token: user._authToken),
                         completion: {(status: Bool, response: AnyObject?) -> Void in
                User.MR_truncateAll()
                Location.MR_truncateAll()
                Friend.MR_truncateAll()
                Image.MR_truncateAll()
                completion()
            })
        }
    }

    class func signup(name: String, gender: Int, bod: Int,
                      email: String, password: String, deviceToken: String?,
                      completion: ((status: Bool, error: String?) -> Void)) {

        var parameters: [String: AnyObject] = [
            "email"      : email,
            "password"   : password,
            "name"       : name,
            "bod"        : bod,
            "gender"     : gender,
        ]

        var device_parameters: [String: AnyObject!] = [
            "device"	      : UIDevice.hardwareString,
            "platform"	      : UIDevice.currentDevice().systemName,
            "platformVersion" : UIDevice.currentDevice().systemVersion,
            "lang"            : UIDevice.lang,
            "country"         : UIDevice.country,
            "isSimulator"     : UIDevice.simulator,
        ]

        if deviceToken != nil {
            device_parameters["deviceToken"] = deviceToken
        }

        parameters["device_content"] = device_parameters

        Rest.request(Router(api: "/m/auth/signup", method: "POST", parameters: parameters),
                     completion: {(status: Bool, response: AnyObject?) -> Void in

            let json: Dictionary<String, AnyObject>? = response as? Dictionary<String, AnyObject>

            if !status {
                return completion(status: false, error: json!["message"] as? String)
            }

            // upon success of the new user signup, delete the
            // previous user and all the friends from the database
            User.MR_truncateAll()

            let user = User.objectWithProperties(json, inContext: NSManagedObjectContext.MR_defaultContext())
            user.managedObjectContext!.MR_saveToPersistentStoreAndWait()

            if let locationRange = json!["locationRange"] as? Dictionary<String, AnyObject> {
                LocationRange.setup(locationRange)
            }

            Image.MR_truncateAll()
            Image.list({(status: Bool, error: String?) in
                           Friend.MR_truncateAll()
                           completion(status: true, error: nil)
                       })
        })

    }

    class func signin(email: String, password: String, deviceToken: String?,
                      completion: ((status: Bool, error: String?) -> Void)) {

        var parameters: [String: AnyObject] = [
            "email"      : email,
            "password"   : password,
        ]

        var device_parameters: [String: AnyObject!] = [
            "device"	      : UIDevice.hardwareString,
            "platform"	      : UIDevice.currentDevice().systemName,
            "platformVersion" : UIDevice.currentDevice().systemVersion,
            "lang"            : UIDevice.lang,
            "country"         : UIDevice.country,
            "isSimulator"     : UIDevice.simulator,
        ]

        if deviceToken != nil {
            device_parameters["deviceToken"] = deviceToken
        }

        parameters["device_content"] = device_parameters

        Rest.request(Router(api: "/m/auth/signin", method: "POST", parameters: parameters),
                     completion: {(status: Bool, response: AnyObject?) -> Void in

            let json: Dictionary<String, AnyObject>? = response as? Dictionary<String, AnyObject>

            if !status {
                completion(status: false, error: json!["message"] as? String)
                return
            }

            User.MR_truncateAll()

            let user = User.objectWithProperties(json, inContext: NSManagedObjectContext.MR_defaultContext())
            user.managedObjectContext!.MR_saveToPersistentStoreAndWait()

            if let locationRange = json!["locationRange"] as? Dictionary<String, AnyObject> {
                LocationRange.setup(locationRange)
            }

            Image.MR_truncateAll()
            Image.list({(status: Bool, error: String?) in
                Friend.MR_truncateAll()
                Friend.load(completion: {(status: Bool, error: String?) in
                  completion(status: true, error: nil)
                })
            })
        })
    }


    class func signin(fbToken: String, fbID: String, deviceToken: String?,
                      completion: ((status: Bool, error: String?) -> Void)) {

        var parameters: [String: AnyObject] = [:]
        parameters["access_token"] = fbToken

        var device_parameters: [String: AnyObject!] = [
            "device"	      : UIDevice.hardwareString,
            "platform"	      : UIDevice.currentDevice().systemName,
            "platformVersion" : UIDevice.currentDevice().systemVersion,
            "lang"            : UIDevice.lang,
            "country"         : UIDevice.country,
            "isSimulator"     : UIDevice.simulator,
        ]

        if deviceToken != nil {
            device_parameters["deviceToken"] = deviceToken
        }

        parameters["device_content"] = device_parameters

        Rest.request(Router(api: "/m/auth/facebook", method: "POST", parameters: parameters),
                     completion: {(status: Bool, response: AnyObject?) -> Void in

            let json: Dictionary<String, AnyObject>? = response as? Dictionary<String, AnyObject>

            if !status {
                completion(status: false, error: json!["message"] as? String)
                return
            }

            User.MR_truncateAll()

            let user = User.objectWithProperties(json, inContext: NSManagedObjectContext.MR_defaultContext())
            user._providerToken = fbToken
            user._providerID = fbID
            user.managedObjectContext!.MR_saveToPersistentStoreAndWait()

            if let locationRange = json!["locationRange"] as? Dictionary<String, AnyObject> {
                LocationRange.setup(locationRange)
            }

            Image.MR_truncateAll()
            Image.list({(status: Bool, error: String?) in
                Friend.MR_truncateAll()
                Friend.load(completion: {(status: Bool, error: String?) in
                    completion(status: true, error: nil)
                })
            })
        })
    }

    class func update(name: String? = nil, gender: Int? = nil, bod: Int? = nil,
                      photoType: UInt? = nil, photoName: String? = nil, deviceToken: String? = nil,
                      myColor: String? = nil, girlColor: String? = nil, boyColor: String? = nil,
                      frameColor: String? = nil,
                      completion: ((status: Bool, error: String?) -> Void)?) {

        var parameters: [String: AnyObject] = [:]

        if name != nil {
            parameters["name"] = name
        }
        if  bod != nil {
            parameters["bod"] = bod
        }
        if  gender != nil {
            parameters["gender"] = gender
        }
        if  photoType != nil {
            parameters["photoType"] = photoType
        }
        if  photoName != nil {
            parameters["photoName"] = photoName
        }
        if  deviceToken != nil {
            parameters["deviceToken"] = deviceToken
        }
        if  myColor != nil {
            parameters["myColor"] = myColor
        }
        if  girlColor != nil {
            parameters["girlColor"] = girlColor
        }
        if  boyColor != nil {
            parameters["boyColor"] = boyColor
        }
        if  frameColor != nil {
            parameters["frameColor"] = frameColor
        }

        if parameters.count == 0 {
            completion!(status: true, error: nil)
            return
        }

        let user = User.user()!

        Rest.request(Router(api: "/m/users", method: "PUT",
                            parameters: parameters, token: user._authToken),
                     completion: {(status: Bool, response: AnyObject?) -> Void in

            let json: Dictionary<String, AnyObject>? = response as? Dictionary<String, AnyObject>

            if !status {
                if completion != nil {
                    completion!(status: false, error: json!["message"] as? String)
                }
                return
            }

            if name != nil {
                user._name = name!
            }
            if  bod != nil {
                user._bod = bod!
            }
            if  gender != nil {
                user._gender = gender!
            }
            if  photoType != nil {
                user._photoType = photoType!
            }
            if  photoName != nil {
                user._photoName = photoName!
            }
            if  myColor != nil {
                user._myColor = myColor!
            }
            if  girlColor != nil {
                user._girlColor = girlColor!
            }
            if  boyColor != nil {
                user._boyColor = boyColor!
            }
            if  frameColor != nil {
                user._frameColor = frameColor!
            }
            user.managedObjectContext!.MR_saveToPersistentStoreAndWait()

            if completion != nil {
                completion!(status: status, error: nil)
            }
        })
    }

    class func search(search: String,
                      completion: ((status: Bool,
                                    response: [Dictionary<String, AnyObject>]?,
                                    error: String?) -> Void)) {

        let parameters: [String: AnyObject] = [
            "search"      : search,
        ]

        let user = User.user()!

        Rest.request(Router(api: "/m/users/search", method: "GET",
                            parameters: parameters, token: user._authToken),
                     completion: {(status: Bool, response: AnyObject?) -> Void in

            if !status {
                if let err = response as? Dictionary<String, AnyObject> {
                    completion(status: false, response: nil, error: err["message"] as? String)
                } else {
                    completion(status: false, response: nil, error: "Failed to search")
                }
                return
            }

            let json: [Dictionary<String, AnyObject>]? = response as? [Dictionary<String, AnyObject>]

            completion(status: status, response: json, error: nil)
        })
    }

    class func feedback(message: String, completion: ((status: Bool, error: String?) -> Void)? = nil) {

        let parameters: [String: AnyObject] = [
            "feedback"      : message,
        ]

        let user = User.user()!

        Rest.request(Router(api: "/users/feedback", method: "POST",
                            parameters: parameters, token: user._authToken),
                     completion: {(status: Bool, response: AnyObject?) -> Void in

            if completion != nil {
                if !status {
                    var json: Dictionary<String, AnyObject>? = response as? Dictionary<String, AnyObject>
                    completion!(status: false, error: json!["message"] as? String)
                } else {
                    completion!(status: status, error: nil)
                }
            }

        })
    }

    class func provider(id: String, completion: ((status: Bool, user: [String : AnyObject]?, error: String?) -> Void)? = nil) {
        let parameters: [String: AnyObject] = [
            "id" : id
        ]

        let user = User.user()!

        Rest.request(Router(api: "/m/users/provider", method: "GET",
                            parameters: parameters, token: user._authToken),
                     completion: {(status: Bool, response: AnyObject?) -> Void in

            var json: Dictionary<String, AnyObject>? = response as? Dictionary<String, AnyObject>

            if status {
                completion!(status: false, user: json, error: json!["message"] as? String)
            } else {
                completion!(status: false, user: nil, error: json!["message"] as? String)
            }
        })
    }

    class func restorePassword(email: String, completion: ((status: Bool, error: String?) -> Void)? = nil) {

        let parameters: [String: AnyObject] = [
            "email" : email,
            "lang"  : UIDevice.lang
        ]

        Rest.request(Router(api: "/auth/forgot", method: "POST", parameters: parameters),
                     completion: {(status: Bool, response: AnyObject?) -> Void in

            if completion != nil {
                if !status {
                    var json: Dictionary<String, AnyObject>? = response as? Dictionary<String, AnyObject>
                    completion!(status: false, error: json!["message"] as? String)
                } else {
                    completion!(status: status, error: nil)
                }
            }

        })
    }

    class var available: Bool {

        if let user = User.user() {
            //println("me user found")
            if user._authToken.isEmpty {
                print("me user no authToken")
                // authToken is not assigned yet
                /*
            } else if user._revised.timeIntervalSinceDate(NSDate.dateMonthFromNow(-3)) <= 0 {
                print("me user authToken expired")
                // clear the authToken
                user._authToken = ""
                user.managedObjectContext!.MR_saveToPersistentStoreAndWait()
                */
            } else {
                //println("me user loggedin")
                //println("photoName: \(user.photoName) type: \(user.photoType)")
                return true
            }
        } else {
            print("me user not found")
        }

        return false
    }


    class var created: NSDate {
        if let u = User.user() {
            return u._created
        }
        return NSDate()
    }

    class var revised: NSDate {
        if let u = User.user() {
            return u._revised
        }
        return NSDate()
    }

    class var id: String {
        if let u = User.user() {
            return u._id
        }
        return ""
    }

    class var name: String {
        if let u = User.user() {
            if !u._authToken.isEmpty {
                return u._name
            }
        }
        return NSLocalizedString("Future of You", comment: "no user name")
    }

    class var authToken: String {
        if let u = User.user() {
            return u._authToken
        }
        return ""
    }

    class var email: String {
        if let u = User.user() {
            return u._email
        }
        return ""
    }

    class var bod: Int {
        if let u = User.user() {
            return u._bod as Int
        }
        return 1980
    }

    class var gender: Int {
        if let u = User.user() {
            return u._gender as Int
        }
        return Gender.Boy.rawValue
    }

    class var photoName: String {
        if User.available {
            if let u = User.user() {
                return u._photoName
            }
        }
        return "person"
    }

    class var photoType: UInt {
        if User.available {
            if let u = User.user() {
                return u._photoType as UInt
            }
        }
        return 0
    }

    class var device: String {
        if let u = User.user() {
            return u._device
        }
        return ""
    }

    class var myColor: String {
        if let u = User.user() {
            return u._myColor
        }
        return "66ffff"
    }

    class var girlColor: String {
        if let u = User.user() {
            return u._girlColor
        }
        return "ff6633"
    }

    class var boyColor: String {
        if let u = User.user() {
            return u._boyColor
        }
        return "9bb7a7"
    }

    class var frameColor: String {
        if let u = User.user() {
            return u._frameColor
        }
        return "007aff"
    }

    class var location: CLLocation? {
        get {
            if let u = User.user() {
                if u._latitude == 0 || u._longitude == 0 {
                    return nil
                }
                return CLLocation(latitude: Double(u._latitude), longitude: Double(u._longitude))
            }
            return nil
        }
        set(loc) {
            if let u = User.user() {
                u._latitude = loc!.coordinate.latitude
                u._longitude = loc!.coordinate.longitude
                u.managedObjectContext!.MR_saveToPersistentStoreAndWait()
            }
        }
    }

    class var providerID: String {
        if let u = User.user() {
            return u._providerID
        }
        return ""
    }

    class var provider: String {
        if let u = User.user() {
            return u._provider
        }
        return ""
    }

}
