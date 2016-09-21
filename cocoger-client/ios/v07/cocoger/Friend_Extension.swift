import Foundation
import EasyMapping
import MagicalRecord
import CoreLocation

extension Friend {

    override class func objectMapping() -> EKManagedObjectMapping {
        let mapping = EKManagedObjectMapping(entityName: "Friend")

        mapping.mapPropertiesFromDictionary(["range":"range"])
        mapping.mapPropertiesFromDictionary(["name":"name"])
        mapping.mapPropertiesFromDictionary(["gender":"gender"])
        mapping.mapPropertiesFromDictionary(["photoName":"photoName"])
        mapping.mapPropertiesFromDictionary(["photoType":"photoType"])
        mapping.mapPropertiesFromDictionary(["user":"user"])
        mapping.mapPropertiesFromDictionary(["provider":"provider"])
        mapping.mapPropertiesFromDictionary(["providerID":"providerID"])

        let formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        mapping.mapKeyPath("created", toProperty: "created", withDateFormatter: formatter)

        return mapping
    }

    class func invite(user: String, range: Int, message: String,
                      completion: ((status: Bool, error: String?) -> Void)?) {

        let parameters: [String: AnyObject] = [
            "user"	: user,
            "range"     : range,
            "message"   : message,
        ]

        Rest.request(Router(api: "/m/friends/invite", method: "POST",
                            parameters: parameters, token: User.authToken),
                     completion: {(status: Bool, response: AnyObject?) -> Void in

            var json: Dictionary<String, AnyObject>? = response as? Dictionary<String, AnyObject>

            if completion != nil {
                if status {
                    completion!(status: true, error: nil)
                } else {
                    completion!(status: false, error: json!["message"] as? String)
                }
            }
        })
    }

    class func ping(user: String, completion: ((status: Bool, error: String?) -> Void)?) {

        let parameters: [String: AnyObject] = [
            "user"	: user,
        ]

        Rest.request(Router(api: "/m/friends/ping", method: "POST",
                            parameters: parameters, token: User.authToken),
                     completion: {(status: Bool, response: AnyObject?) -> Void in

            var json: Dictionary<String, AnyObject>? = response as? Dictionary<String, AnyObject>

            if completion != nil {
                if status {
                    completion!(status: true, error: nil)
                } else {
                    completion!(status: false, error: json!["message"] as? String)
                }
            }
        })
    }

    class func accept(friend: String, id: String, range: Int,
                      completion: ((status: Bool, error: String?) -> Void)?) {

        let parameters: [String: AnyObject] = [
            "friend"	: friend,
            "id"	: id,
            "range"     : range,
        ]

        Rest.request(Router(api: "/m/friends/invite", method: "PUT",
                            parameters: parameters, token: User.authToken),
                     completion: {(status: Bool, response: AnyObject?) -> Void in

            var json: Dictionary<String, AnyObject>? = response as? Dictionary<String, AnyObject>

            if completion != nil {
                if status {
                    completion!(status: true, error: nil)
                } else {
                    completion!(status: false, error: json!["message"] as? String)
                }
            }
        })
    }

    class func decline(friend: String, id: String,
                       completion: ((status: Bool, error: String?) -> Void)?) {

        let parameters: [String: AnyObject] = [
            "friend"	: friend,
            "id"	: id,
        ]

        Rest.request(Router(api: "/m/friends/invite", method: "DELETE",
                            parameters: parameters, token: User.authToken),
                     completion: {(status: Bool, response: AnyObject?) -> Void in

            var json: Dictionary<String, AnyObject>? = response as? Dictionary<String, AnyObject>

            if completion != nil {
                if status {
                    completion!(status: true, error: nil)
                } else {
                    completion!(status: false, error: json!["message"] as? String)
                }
            }
        })
    }

    class func load(friend: String? = nil,
                    completion: ((status: Bool, error: String?) -> Void)? = nil) {
        var parameters: [String: AnyObject] = [:]

        if friend != nil {
            parameters["friend"] = friend
        }

        Rest.request(Router(api: "/m/friends", method: "GET",
                            parameters: parameters, token: User.authToken),
                     completion: {(status: Bool, response: AnyObject?) -> Void in

            if !status {
                if completion != nil {
                    var json: Dictionary<String, AnyObject>? = response as? Dictionary<String, AnyObject>
                    completion!(status: false, error: json!["message"] as? String)
                }
                return
            }

            let json: [Dictionary<String, AnyObject>]? = response as? [Dictionary<String, AnyObject>]

            if json != nil {
                for obj in json! {
                    let friend = Friend.objectWithProperties(
                                     obj, inContext: NSManagedObjectContext.MR_defaultContext())
                    friend.managedObjectContext!.MR_saveToPersistentStoreAndWait()
                }
            }

            if completion != nil {
                completion!(status: true, error: nil)
            }
        })

    }

    // delete the all friends, load again, and save
    class func reload(completion: ((status: Bool, error: String?) -> Void)? = nil) {
        Rest.request(Router(api: "/m/friends", method: "GET", token: User.authToken),
                     completion: {(status: Bool, response: AnyObject?) -> Void in

            if !status {
                if completion != nil {
                    var json: Dictionary<String, AnyObject>? = response as? Dictionary<String, AnyObject>
                    completion!(status: false, error: json!["message"] as? String)
                }
                return
            }

            if let json = response as? [Dictionary<String, AnyObject>] {
                Friend.MR_truncateAll()
                for obj in json {
                    print(obj)
                    let friend = Friend.objectWithProperties(
                                     obj, inContext: NSManagedObjectContext.MR_defaultContext())
                    friend.managedObjectContext!.MR_saveToPersistentStoreAndWait()
                }
            }

            if completion != nil {
                completion!(status: true, error: nil)
            }
        })

    }

    class func remove(friend: Friend, completion: ((status: Bool, error: String?) -> Void)? = nil) {
        let parameters: [String: AnyObject] = [
            "owner" : User.id,
            "user" : friend.user,
        ]

        Rest.request(Router(api: "/m/friends/", method: "DELETE",
                            parameters: parameters, token: User.authToken),
                     completion: {(status: Bool, response: AnyObject?) -> Void in

            if completion != nil {
                if status {
                    friend.MR_deleteEntity()
                    completion!(status: true, error: nil)
                } else {
                    var json: Dictionary<String, AnyObject>? = response as? Dictionary<String, AnyObject>
                    completion!(status: false, error: json!["message"] as? String)
                }
            }
        })
    }

    class func update(user: String, name: String? = nil, range: Int? = nil,
                      photoType: UInt? = nil, photoName: String? = nil,
                      completion: ((status: Bool, error: String?) -> Void)? = nil) {

        let friend = Friend.MR_findFirstByAttribute("user", withValue: user)

        var parameters: [String: AnyObject] = [:]
        parameters["user"] = user
        if name != nil && name != friend.name {
            parameters["name"] = name!
        }
        if range != nil && range != Int(friend.range) {
            parameters["range"] = range!
        }
        if photoType != nil && photoType != friend.photoType {
            parameters["photoType"] = photoType!
        }
        if photoName != nil && photoName != friend.photoName {
            parameters["photoName"] = photoName!
        }

        if parameters.count <= 1 {
            // let the view closed without showing any error
            if completion != nil {
                completion!(status: true, error: nil)
            }
            return
        }

        Rest.request(Router(api: "/m/friends", method: "PUT",
                            parameters: parameters, token: User.authToken),
                     completion: {(status: Bool, response: AnyObject?) -> Void in

            var json: Dictionary<String, AnyObject>? = response as? Dictionary<String, AnyObject>

            if !status {
                if completion != nil {
                    completion!(status: false, error: json!["message"] as? String)
                }
                return
            }

            if name != nil && name != friend.name {
                friend.name = name!
            }
            if range != nil && range > Int(friend.range) {
                friend.range = range!
            }
            if photoType != nil && photoType != friend.photoType {
                friend.photoType = photoType!
            }
            if photoName != nil && photoName != friend.photoName {
                friend.photoName = photoName!
            }

            friend.managedObjectContext!.MR_saveToPersistentStoreAndWait()

            if completion != nil {
                completion!(status: status, error: nil)
            }
        })
    }

    class func acceptRange(user: String, id: String, range: Int,
                           completion: ((status: Bool, error: String?) -> Void)? = nil) {

        let parameters: [String: AnyObject] = [
            "user"	: user,
            "id"	: id,
            "range"     : range,
        ]

        Rest.request(Router(api: "/m/friends/range", method: "PUT",
                            parameters: parameters, token: User.authToken),
                     completion: {(status: Bool, response: AnyObject?) -> Void in

            var json: Dictionary<String, AnyObject>? = response as? Dictionary<String, AnyObject>

            if completion != nil {
                if status {
                    completion!(status: true, error: nil)
                } else {
                    completion!(status: false, error: json!["message"] as? String)
                }
            }
        })
    }

    class func declineRange(user: String, id: String, completion: ((status: Bool, error: String?) -> Void)? = nil) {

        let parameters: [String: AnyObject] = [
            "user"	: user,
            "id"	: id,
        ]

        Rest.request(Router(api: "/m/friends/range", method: "DELETE",
                            parameters: parameters, token: User.authToken),
                     completion: {(status: Bool, response: AnyObject?) -> Void in

            var json: Dictionary<String, AnyObject>? = response as? Dictionary<String, AnyObject>

            if completion != nil {
                if status {
                    completion!(status: true, error: nil)
                } else {
                    completion!(status: false, error: json!["message"] as? String)
                }
            }
        })
    }

    class func coordinate(friend: Friend, completion: ((status: Bool, location: LocationLight?) -> Void)) {
        return Friend.position(friend.user, completion: completion)
    }

    class func position(user: String, completion: ((status: Bool, location: LocationLight?) -> Void)) {
        let parameters: [String: AnyObject] = [
            "user" : user
        ]
        Rest.request(Router(api: "/m/locations/latest", method: "GET",
                            parameters: parameters, token: User.authToken),
                     completion: {(status: Bool, response: AnyObject?) -> Void in
            if (status) {
                if let json = response as? Dictionary<String, AnyObject> {
                    let location = LocationLight(properties: json)
                    completion(status: true, location: location)
                }
            } else {
                completion(status: false, location: nil)
            }
        })
    }

}
