import Foundation
import EasyMapping
import MagicalRecord

extension Image {

    override class func objectMapping() -> EKManagedObjectMapping {
        let mapping = EKManagedObjectMapping(entityName: "Image")
        mapping.mapPropertiesFromDictionary(["name":"name"])
        mapping.mapPropertiesFromDictionary(["url":"url"])
        mapping.mapPropertiesFromDictionary(["type":"type"])
        mapping.mapPropertiesFromDictionary(["user":"user"])
        mapping.mapPropertiesFromDictionary(["data":"data"])
        let formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        mapping.mapKeyPath("created", toProperty: "created", withDateFormatter: formatter)
        return mapping
    }

    class func list(completion: ((status: Bool, error: String?) -> Void)? = nil) {

        Rest.request(Router(api: "/m/images", method: "GET", token: User.authToken),
                     completion: {(status: Bool, response: AnyObject?) -> Void in

            if !status {
                if let json = response as? Dictionary<String, AnyObject> {
                    if let error = json["message"] as? String {
                        if let complete = completion {
                            return complete(status: false, error: error)
                        }
                    }
                }
                return
            }

            Image.MR_truncateAll()

            if let data = response as? [Dictionary<String, AnyObject>] {
                for entry in data {
                    let image = Image.objectWithProperties(
                                    entry, inContext: NSManagedObjectContext.MR_defaultContext())
                    image.managedObjectContext!.MR_saveToPersistentStoreAndWait()
                }
            }

            if completion != nil {
                completion!(status: status, error: nil)
            }
        })

    }

    class func getString(name: String) -> String? {
        let obj = Image.MR_findFirstByAttribute("name", withValue: name)
        if let image = obj {
            return image.url
        }
        return nil
    }

    class func getURL(name: String) -> NSURL? {
        let obj = Image.MR_findFirstByAttribute("name", withValue: name)
        if let image = obj {
            return NSURL(string: image.url)
        }
        return nil
    }

    class func getAll() -> [Image] {
        return Image.MR_findAll() as! [Image]
    }

    class func getImage(name: String, type: UInt, completion: (image: UIImage?) -> Void) {
        let obj = Image.MR_findFirstByAttribute("name", withValue: name)
        if let image = obj {
            if image.data.length > 0 {
                //print("IMAGE FROM DB")
                completion(image: UIImage(data: image.data))
                return
            }
            if image.type == PhotoType.Asset.rawValue || image.type == PhotoType.Template.rawValue {
                if image.url.isEmpty {
                    completion(image: nil)
                    return
                }
                dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), {
                    let url = NSURL(string: image.url)
                    var data: NSData?
                    if url != nil {
                        print("IMAGE ASKING NETWORK")
                        data = NSData(contentsOfURL: url!)
                    }
                    dispatch_async(dispatch_get_main_queue(), {
                        if data == nil {
                            completion(image: nil)
                        } else {
                            image.data = data!
                            image.managedObjectContext!.MR_saveToPersistentStoreAndWait()
                            completion(image: UIImage(data: image.data))
                        }
                    })
                })
            } else if image.type == PhotoType.Photo.rawValue {
                let parameters: [String: AnyObject] = [
                    "user"      : image.user,
                    "name"      : image.name,
                    "type"      : image.type,
                ]
                Rest.request(Router(api: "/m/images", method: "GET",
                                    parameters: parameters, token: User.authToken),
                             completion: {(status: Bool, response: AnyObject?) -> Void in
                    if !status {
                        completion(image: nil)
                        return
                    }
                    if let json = response as? Dictionary<String, AnyObject> {
                        if let data = json["data"] as? String {
                            if let base64Decoded = NSData(base64EncodedString: data,
                                                          options: []) {
                                image.data = base64Decoded
                                image.managedObjectContext!.MR_saveToPersistentStoreAndWait()
                                completion(image: UIImage(data: image.data))
                            }
                        }
                    }
                })
            }
        } else {
            if type == PhotoType.Facebook.rawValue {
                dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), {
                    let square = "https://graph.facebook.com/\(name)/picture?type=square"
                    let url = NSURL(string: square)
                    var data: NSData?
                    if url != nil {
                        print("IMAGE ASKING NETWORK: \(square)")
                        data = NSData(contentsOfURL: url!)
                    }
                    dispatch_async(dispatch_get_main_queue(), {
                        if data == nil {
                            completion(image: nil)
                        } else {
                            let image = Image.MR_createEntity()
                            image.user = User.id
                            image.type = type
                            image.name = name
                            image.data = data!
                            image.managedObjectContext!.MR_saveToPersistentStoreAndWait()
                            completion(image: UIImage(data: data!))
                        }
                    })
                })
            } else {
                completion(image: nil)
            }
        }
    }

    class func getImage(name: String, type: UInt, error: UIImage? = nil) -> UIImage? {
        let obj = Image.MR_findFirstByAttribute("name", withValue: name)
        if let image = obj {
            if image.data.length > 0 {
                return UIImage(data: image.data)
            }
        }
        return error
    }

    class func getImage(named name : String) -> UIImage? {
        if let imgPath = NSBundle.mainBundle().pathForResource(name, ofType: nil) {
            return UIImage(contentsOfFile: imgPath)
        }
        return nil
    }

    class func saveImage(image: UIImage, name: String, completion: ((status: Bool) -> Void)? = nil) {
        var tobesaved: UIImage!
        if image.size.width > 64 || image.size.height > 64 {
            tobesaved = image.resizeToWidth(32)
        } else {
            tobesaved = image
        }

        let saved = Image.MR_createEntity()
        saved.user = User.id
        saved.name = name
        saved.data = UIImagePNGRepresentation(tobesaved)!
        saved.type = PhotoType.Photo.rawValue
        saved.managedObjectContext!.MR_saveToPersistentStoreAndWait()

        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), {
            let parameters: [String: AnyObject] = [
                "user"      : saved.user,
                "name"      : saved.name,
                "type"      : saved.type,
                "data"      : saved.data.base64EncodedStringWithOptions([])
            ]
            Rest.request(Router(api: "/m/images", method: "POST",
                                parameters: parameters, token: User.authToken),
                         completion: { (status: Bool, response: AnyObject?) -> Void in
                if status {
                    print("succeeded to upload the image")
                }
                if completion != nil {
                    completion!(status: status)
                }
            })
        })
    }

    class func deleteImage(name: String, completion: ((status: Bool) -> Void)? = nil) {
        let obj = Image.MR_findFirstByAttribute("name", withValue: name)
        if let image = obj {
            let parameters: [String: AnyObject] = [
                "user"      : image.user,
                "name"      : image.name,
                "type"      : image.type,
            ]
            Rest.request(Router(api: "/m/images", method: "DELETE",
                                parameters: parameters, token: User.authToken),
                         completion: { (status: Bool, response: AnyObject?) -> Void in
                if status {
                    print("succeeded to delete the image")
                    image.MR_deleteEntity()
                }
                if completion != nil {
                    completion!(status: status)
                }
            })
        }
    }

}
