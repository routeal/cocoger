import UIKit

class NotificationHandler {

    enum NotificationCategory: String {
        case None = ""
        case Invitation = "Invite"
        case AcknowledgeInvitation = "AckInvite"
        case FriendRemoval = "FriendRemoval"
        case FriendOut = "FriendOut"
        case FriendIn = "FriendIn"
        case RangeChangeRequest = "RangeChangeRequest"
        case RangeChanged = "RangeChanged"
        case MovedIn = "MovedIn"
        case Move = "Move"
        case Ping = "Ping"
        case Message = "Message"
    }

    init() {
        registerNotification()
    }

    func registerNotification() {
/*
// FIXME: iPad brings up the setting all the time??? need to test wiht the real device
        // enable the notification if not
        if UIDevice.simulator {
            if !UIApplication.sharedApplication().isRegisteredForRemoteNotifications() {
                let url = NSURL(string:UIApplicationOpenSettingsURLString)!
                UIApplication.sharedApplication().openURL(url)
            }
        }
*/

        UIApplication.sharedApplication().unregisterForRemoteNotifications()

        let settings = UIUserNotificationSettings(forTypes: [.Badge, .Alert, .Sound], categories: nil)

        UIApplication.sharedApplication().registerUserNotificationSettings(settings)

        UIApplication.sharedApplication().registerForRemoteNotifications()
    }

    func setDeviceToken(deviceToken: NSData) {
        let characterSet: NSCharacterSet = NSCharacterSet( charactersInString: "<>" )
        let deviceTokenString: String = (deviceToken.description as NSString)
                                        .stringByTrimmingCharactersInSet(characterSet)
                                        .stringByReplacingOccurrencesOfString(" ", withString: "") as String
        NSLog(deviceTokenString)
        let defaults = NSUserDefaults.standardUserDefaults()
        if User.available {
            if let deviceToken = defaults.stringForKey("deviceToken") {
                if deviceToken == deviceTokenString {
                    return
                }
                User.update(deviceToken: deviceTokenString, completion: nil)
            }
        } else {
            defaults.setObject(deviceTokenString, forKey: "deviceToken")
        }
    }

    func didFailToRegisterForRemoteNotifications(error: NSError) {
        // FIXME: need to remove the device token from the server...
	NSLog("Failed to register for remote notification: %s", error)
    }

    func didReceiveRemoteNotification(userInfo: [NSObject : AnyObject]) {
        if !User.available {
            NSLog("didReceiveRemoteNotification while in logout")
            return
        }

        //print(userInfo)

        var info: Dictionary<String, AnyObject>!
        if let t = userInfo["aps"] as? Dictionary<String, AnyObject> {
            info = t
        } else {
            return
        }

        var category: NotificationCategory = .None
        if let c = userInfo["category"] as? String {
            if let n = NotificationCategory(rawValue: c) {
                category = n
            }
        }

        if category == NotificationCategory.Invitation {
            if let message = info["message"] as? String,
               let from    = info["from"] as? String,
               let friend  = info["friend"] as? String,
               let range   = info["range"] as? Int,
               let id      = info["id"] as? String {

                let popupView = LocationRangePopupViewController()
                popupView.title = NSLocalizedString("Friend Request", comment: "notification")
                let fmt = NSLocalizedString("from %@", comment: "notification")
                popupView.name = String(format: fmt, from)
                popupView.message = message
                popupView.range = range
                popupView.popup({ [weak self] (status: Bool, range: Int) in
                    if status {
                        //NSLog("ok")
                        self!.acceptInvite(friend, id: id, range: range)
                    } else {
                        //NSLog("cancel")
                        //NSLog("invitation declined")
                        self!.declineInvite(friend, id: id)
                    }
                })
            }
        } else if category == NotificationCategory.AcknowledgeInvitation {
            if let owner = info["owner"] as? String, // invitor
               let user = info["user"] as? String {  // invitee

                var friend: String!

                if user == User.id {
                    NSLog("I accepted and I am added to his friend")
                    friend = owner
                } else {
                    NSLog("invitaion is accepted")
                    friend = user
                }

                Friend.load(friend, completion: { (status: Bool, error: String?) in
                    if status {
                        if user == User.id {
                            NSLog("I accept and am added")
                        } else {
                            NSLog("invitaion is accepted")
                        }
                    }
                })
            }
        } else if category == NotificationCategory.FriendRemoval {
            if let user = info["user"] as? String {
                if let friend = Friend.MR_findFirstByAttribute("user", withValue: user) {
                    friend.MR_deleteEntity()
                }
            }
        } else if category == NotificationCategory.FriendOut {
            if let user = info["user"] as? String {
                if let friend = Friend.MR_findFirstByAttribute("user", withValue: user) {
                    friend.hasSignedOut = true
                    friend.managedObjectContext!.MR_saveToPersistentStoreAndWait()
                }
            }
        } else if category == NotificationCategory.FriendIn {
            if let user = info["user"] as? String {
                // remove the old one if any
                if let friend = Friend.MR_findFirstByAttribute("user", withValue: user) {
                    friend.hasMoved = true
                    friend.managedObjectContext!.MR_saveToPersistentStoreAndWait()
                } else {
                    Friend.load(user, completion: {(status: Bool, error: String?) in
                        if status {
                            NSLog("friend signed in")
                        }
                    })
                }
            }
        } else if category == NotificationCategory.RangeChangeRequest {
            if let from    = info["from"] as? String,
               let user    = info["user"] as? String,
               let range   = info["range"] as? Int,
               let id      = info["id"] as? String {

                let popupView = LocationRangePopupViewController()
                popupView.title = NSLocalizedString("Location range request", comment: "notification")
                let fmt = NSLocalizedString("from %@", comment: "notification")
                popupView.name = String(format: fmt, from)
                popupView.message = NSLocalizedString(
                        "You have a request for the change of Location Range. Please review the change. You can extend the requested change and accept.",
                        comment: "notification")
                popupView.range = range
                popupView.okLabel = NSLocalizedString("Accept", comment: "action label")
                popupView.cancelLabel = NSLocalizedString("Decline", comment: "action label")
                popupView.popup({(status: Bool, range: Int) in
                    if status {
                        //sNSLog("ok")
                        Friend.acceptRange(user, id: id, range: range)

                        // NOTE: should access the server to update
                        let friend = Friend.MR_findFirstByAttribute("user", withValue: user)
                        friend.range = range
                        friend.managedObjectContext!.MR_saveToPersistentStoreAndWait()
                    } else {
                        //NSLog("cancel")
                        //NSLog("invitation declined")
                        Friend.declineRange(user, id: id)
                    }
                })
            }
        } else if category == NotificationCategory.RangeChanged {
            if let user = info["user"] as? String {
                if let friend = Friend.MR_findFirstByAttribute("user", withValue: user) {
                    if let range = info["range"] as? Int {
                        NSLog("range has changed to \(range)")
                        friend.range = range
                        friend.managedObjectContext!.MR_saveToPersistentStoreAndWait()
                    }
                }
            }
        } else if category == NotificationCategory.Message {
            if let message = info["message"] as? String {
                UIAlertController.simpleAlert(message)
            }
        } else if category == NotificationCategory.MovedIn {
            if let user = info["user"] as? String {
                if let friend = Friend.MR_findFirstByAttribute("user", withValue: user) {
                    friend.hasMoved = true
                    friend.managedObjectContext!.MR_saveToPersistentStoreAndWait()
                    if let alert = info["alert"] as? Dictionary<String, AnyObject> {
                        if let key = alert["loc-key"] as? String {
                            if let args = alert["loc-args"] as? [String] {
                                if args.count == 2 {
                                    let fmt = NSLocalizedString(key, comment: "notification")
                                    let message = String(format: fmt, args[0], args[1])
                                    UIAlertController.simpleAlert(message, dismiss: 15)
                                }
                                if let mapViewController = UIViewController.mapViewController2() {
                                    mapViewController.centerizeFriend(args[0])
                                }
                            }
                        }
                    }
                }
            }
        } else if category == NotificationCategory.Move {
            if let user = info["user"] as? String {
                if let friend = Friend.MR_findFirstByAttribute("user", withValue: user) {
                    friend.hasMoved = true
                    friend.managedObjectContext!.MR_saveToPersistentStoreAndWait()
                }
            }
        } else if category == NotificationCategory.Ping {
            if let user = info["user"] as? String {
                if let friend = Friend.MR_findFirstByAttribute("user", withValue: user) {
                    NSLog("ping received from: \(friend.name)")
                    locationTracker.ping()
                }
            }
        } else {
            NSLog("notification: category not found")
        }
    }

    func acceptInvite(friend: String, id: String, range: Int) {
        Friend.accept(friend, id: id, range: range, completion: {(status: Bool, error: String?) in
                if status {
                    NSLog("the server knows that you have accepted the invitation")
                }
            })
    }

    func declineInvite(friend: String, id: String) {
        Friend.decline(friend, id: id, completion: {(status: Bool, error: String?) in
                if status {
                    NSLog("let the server know that you have declined the invitation")
                }
            })
    }
}
