//
//  SignupTableViewController.swift
//  SokoraNow
//
//  Created by Hiroshi Watanabe on 6/30/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit
import PKHUD
import MessageUI
import FBSDKCoreKit
import FBSDKLoginKit

class SettingsTableViewController: UITableViewController, MFMailComposeViewControllerDelegate, FBSDKLoginButtonDelegate {

    let ProfileIdentifier:String = "Profile"
    let AppInfoIdentifier:String = "AppInfo"
    let SignOutIdentifier:String = "SignOut"
    let DeleteIdentifier:String  = "Delete"

    enum Cell: Int {
        case Profile = 0,
             AppInfo,
             SignOut,
             Delete,
             Total
    }

    enum Profile: Int {
        case Profile = 0,
             Color,
             Notification,
             Total
    }

    enum AppInfo: Int {
        case FAQ = 0,
             Privacy,
             Term,
             Feedback,
             Total
    }

    init() {
        super.init(style:UITableViewStyle.Grouped)
    }

    required init(coder: NSCoder) {
        fatalError("NSCoding not supported")
    }

    private override init(nibName nibNameOrNil: String!, bundle nibBundleOrNil: NSBundle!) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    deinit {
        print("deinit: SettingsTableViewController")
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        self.title = NSLocalizedString("Settings", comment: "viewcontroller title")
        self.navigationController!.setNavigationBarHidden(false, animated: false)
        self.tableView.registerClass(UITableViewCell.classForCoder(),
                                     forCellReuseIdentifier: ProfileIdentifier)
        self.tableView.registerClass(UITableViewCell.classForCoder(),
                                     forCellReuseIdentifier: AppInfoIdentifier)
        self.tableView.registerClass(UITableViewCell.classForCoder(),
                                     forCellReuseIdentifier: SignOutIdentifier)
        self.tableView.registerClass(UITableViewCell.classForCoder(),
                                     forCellReuseIdentifier: DeleteIdentifier)
    }

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return Cell.Total.rawValue
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch (section) {
        case Cell.Profile.rawValue:
            return Profile.Total.rawValue
        case Cell.AppInfo.rawValue:
            if MFMailComposeViewController.canSendMail() {
                return AppInfo.Total.rawValue
            } else {
                return AppInfo.Total.rawValue - 1
            }
        case Cell.SignOut.rawValue:
            return 1
        case Cell.Delete.rawValue:
            if FBSDKAccessToken.currentAccessToken() == nil {
                return 1
            } else {
                return 0
            }
        default:
            return 0
        }
    }

    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var id: String!
        if indexPath.section == Cell.Profile.rawValue {
            id = ProfileIdentifier
        } else if indexPath.section == Cell.AppInfo.rawValue {
            id = AppInfoIdentifier
        } else if indexPath.section == Cell.SignOut.rawValue {
            id = SignOutIdentifier
        } else if indexPath.section == Cell.Delete.rawValue {
            id = DeleteIdentifier
        }
        return tableView.dequeueReusableCellWithIdentifier(id, forIndexPath: indexPath)
    }

    override func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell,
                            forRowAtIndexPath indexPath: NSIndexPath) {
        switch (indexPath.section) {
        case Cell.Profile.rawValue:
            let row = indexPath.row
            if row == 0 {
                cell.imageView!.image = UIImage(named: "profile")!
                cell.textLabel!.text = NSLocalizedString("My Profile", comment: "tableview label")
                cell.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
            } else if row == 1 {
                cell.imageView!.image = UIImage(named: "palette")!.resizeToWidth(16)
                cell.textLabel!.text = NSLocalizedString("Colors", comment: "tableview label")
                cell.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
            } else if row == 2 {
                cell.imageView!.image = UIImage(named: "notification")!.resizeToWidth(16)
                cell.imageView!.translatesAutoresizingMaskIntoConstraints = false
                cell.textLabel!.text = NSLocalizedString("Notification", comment: "tableview label")
                cell.textLabel!.translatesAutoresizingMaskIntoConstraints = false
                let onoff = UISwitch()
                onoff.translatesAutoresizingMaskIntoConstraints = false
                cell.contentView.addSubview(onoff)

                view.addLayoutConstraints(["H:|-[image(16)]-16-[label(100)]",
                                           "H:[onoff]-16-|",
                                           "V:|-14-[image(16)]-|",
                                           "V:|-[label]-|",
                                           "V:|-[onoff]-|"],
                                          views: ["image": cell.imageView!, "label":cell.textLabel!, "onoff":onoff])

                //cell.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
            }
        case Cell.AppInfo.rawValue:
            let row = indexPath.row
            if row == 0 {
                cell.imageView!.image = UIImage(named: "faq")!
                cell.textLabel!.text = NSLocalizedString("FAQ", comment: "tableview label")
                cell.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
            } else if row == 1 {
                cell.imageView!.image = UIImage(named: "privacy")!
                cell.textLabel!.text = NSLocalizedString("Privacy Policy", comment: "tableview label")
                cell.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
            } else if row == 2 {
                cell.imageView!.image = UIImage(named: "about")!
                cell.textLabel!.text = NSLocalizedString("Term of Use", comment: "tableview label")
                cell.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
            } else if row == 3 {
                cell.imageView!.image = UIImage(named: "feedback")!
                cell.textLabel!.text = NSLocalizedString("Feedback", comment: "tableview label")
                cell.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
            }
        case Cell.SignOut.rawValue:
            if FBSDKAccessToken.currentAccessToken() == nil {
                cell.textLabel!.text = NSLocalizedString("Sign Out", comment: "tableview label")
                cell.textLabel!.textColor = tableView.tintColor
                cell.textLabel!.textAlignment = .Center
            } else {
                let fbButton : FBSDKLoginButton = FBSDKLoginButton()
                fbButton.translatesAutoresizingMaskIntoConstraints = false
                fbButton.center = cell.center
                fbButton.delegate = self

                cell.contentView.addSubview(fbButton)
                cell.contentView.addLayoutConstraints(["H:|-60-[fb]-60-|", "V:|[fb]|"], views: ["fb":fbButton])

                cell.selectionStyle = .None
                cell.backgroundColor = tableView.backgroundColor
            }
        case Cell.Delete.rawValue:
            cell.textLabel!.text = NSLocalizedString("Delete Account", comment: "tableview label")
            cell.textLabel!.textColor = UIColor.redColor()
            cell.textLabel!.textAlignment = .Center
        default: break
        }
    }

    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        if indexPath.section == Cell.SignOut.rawValue {
            if FBSDKAccessToken.currentAccessToken() != nil {
                return 44
            }
        }
        return UITableViewAutomaticDimension
    }

    override func tableView(tableView: UITableView, willSelectRowAtIndexPath indexPath: NSIndexPath) -> NSIndexPath? {
        return indexPath
    }

    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        if indexPath.section == Cell.Profile.rawValue {
            if indexPath.row == Profile.Profile.rawValue {
                let controller = ProfileTableViewController()
                self.navigationController!.pushViewController(controller, animated: true)
            } else if indexPath.row == Profile.Color.rawValue {
                let controller = ColorTableViewController()
                self.navigationController!.pushViewController(controller, animated: true)
            }
        } else if indexPath.section == Cell.AppInfo.rawValue {
            switch indexPath.row {
            case AppInfo.FAQ.rawValue:
                let controller = WebViewController()
                controller.title = NSLocalizedString("FAQ", comment: "tableview label")
                controller.url = "\(Router.WebURLString)/m/faq.html?lang=\(UIDevice.lang)"
                self.navigationController!.setNavigationBarHidden(false, animated: false)
                self.navigationController!.pushViewController(controller, animated: true)

            case AppInfo.Privacy.rawValue:
                let controller = WebViewController()
                controller.title = NSLocalizedString("Privacy Policy", comment: "tableview label")
                controller.url = "\(Router.WebURLString)/m/privacy.html?lang=\(UIDevice.lang)"
                self.navigationController!.setNavigationBarHidden(false, animated: false)
                self.navigationController!.pushViewController(controller, animated: true)

            case AppInfo.Term.rawValue:
                let controller = WebViewController()
                controller.title = NSLocalizedString("Term of Use", comment: "tableview label")
                controller.url = "\(Router.WebURLString)/m/terms.html?lang=\(UIDevice.lang)"
                self.navigationController!.setNavigationBarHidden(false, animated: false)
                self.navigationController!.pushViewController(controller, animated: true)

            case AppInfo.Feedback.rawValue:
                let controller = MFMailComposeViewController()
                controller.mailComposeDelegate = self
                controller.setToRecipients([NSLocalizedString("cocoger.routeal@gmail.com", comment: "feedback email address")])
                controller.setSubject(
                    NSLocalizedString("User Feedback", comment: "subject message for Feedback"))
                self.navigationController!.presentViewController(controller, animated: true) {
                    let msg = NSLocalizedString(
                            "Your suggestions, ideas, and problem reporting are important to us.  Thank you.",
                        comment: "body message for Feedback")
                    UIAlertController.simpleAlert(msg, dismiss: 15)
                }

            default: break
            }
        } else if indexPath.section == Cell.SignOut.rawValue {
            PKHUD.sharedHUD.show()
            User.signout() {
                dispatch_async(dispatch_get_main_queue()) {
                    PKHUD.sharedHUD.hide(animated: false)
                    locationTracker.stop()
                    self.navigationController!.setViewControllers([WelcomeViewController()], animated: false)
                }
            }
        } else if indexPath.section == Cell.Delete.rawValue {
            let delStr = NSLocalizedString("Delete", comment: "alert label")
            let cancelLabel = NSLocalizedString("Cancel", comment: "alert label")
            let message = NSLocalizedString("Do you really want to delete your account?", comment: "alert message")
            UIAlertController.simpleAlert(
                message, ok: delStr, cancel: cancelLabel,
                handler: {(index: Int) -> Void in
                    switch index {
                    case 1:
                        PKHUD.sharedHUD.show()
                        User.deleteAccount() {
                            dispatch_async(dispatch_get_main_queue()) {
                                PKHUD.sharedHUD.hide(animated: false)
                                locationTracker.stop()
                                self.navigationController!.setViewControllers([WelcomeViewController()], animated: false)
                            }
                        }

                    default: break
                    }
                }
            )
        }
    }

    func mailComposeController(controller: MFMailComposeViewController,
                               didFinishWithResult result: MFMailComposeResult, error: NSError?) {
        controller.dismissViewControllerAnimated(true, completion: nil)

        switch result {
        case MFMailComposeResultSent:
            // sent the message to the server
	    break
        default: break
        }
    }

    func loginButtonDidLogOut(loginButton: FBSDKLoginButton) {
        PKHUD.sharedHUD.show()
        User.signout() {
            dispatch_async(dispatch_get_main_queue()) {
                PKHUD.sharedHUD.hide(animated: false)
                locationTracker.stop()
                self.navigationController!.setViewControllers([WelcomeViewController()], animated: false)
            }
        }
    }

    func loginButton(loginButton: FBSDKLoginButton,
                     didCompleteWithResult result: FBSDKLoginManagerLoginResult!,
                     error: NSError!) {
        // do nothing
    }
}
