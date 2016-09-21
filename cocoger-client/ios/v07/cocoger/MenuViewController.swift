//
//  MenuViewController.swift
//  cocoger
//
//  Created by Hiroshi Watanabe on 11/29/15.
//  Copyright © 2015 routeal. All rights reserved.
//

import UIKit
import PKHUD
import MessageUI
import FBSDKLoginKit

class MenuViewController: UIViewController {

    var imageView: UIImageView!
    var name: UILabel!
    var email: UILabel!

    override func viewDidLoad() {
        super.viewDidLoad()

        self.view.backgroundColor = UIColor.whiteColor()

        let profileView = createProfileView()
        self.view.addSubview(profileView)

        let tableView = createTableView()
        self.view.addSubview(tableView)

        self.view.addLayoutConstraints(["H:|[profile]|",
                                        "H:|[tableView]|",
                                        "V:|[profile(152)]-[tableView]|"],
                                       views: ["profile":profileView, "tableView":tableView])
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    required init(coder: NSCoder) {
        fatalError("NSCoding not supported")
    }

    override init(nibName nibNameOrNil: String!, bundle nibBundleOrNil: NSBundle!) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    deinit {
        print("deinit: MenuViewController")
    }

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        imageView.image = Image.getImage(User.photoName, type: User.photoType)
        name.text = User.name
        email.text = User.email
    }
}

extension MenuViewController {

    func createProfileView() -> UIView {
        let view = UIView()
        view.translatesAutoresizingMaskIntoConstraints = false
        view.backgroundColor = "208695".hexColor

        imageView = UIImageView()
        imageView.frame = CGRectMake(20, 20, 64, 64)
        view.addSubview(imageView)

        name = UILabel(frame: CGRectMake(20, 92, 240, 24))
        name.font = UIFont.boldSystemFontOfSize(17)
        //name.backgroundColor = UIColor.whiteColor()
        view.addSubview(name)

        email = UILabel(frame: CGRectMake(20, 116, 240, 24))
        //email.backgroundColor = UIColor.whiteColor()
        view.addSubview(email)

        return view
    }

}

extension MenuViewController {

    func createTableView() -> UIView {
        let tableView = UITableView()
        tableView.translatesAutoresizingMaskIntoConstraints = false
        tableView.delegate = self
        tableView.dataSource = self
        tableView.registerClass(UITableViewCell.classForCoder(), forCellReuseIdentifier: NSStringFromClass(UITableViewCell))
        return tableView
    }

}

extension MenuViewController: UITableViewDelegate {

    func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return UITableViewAutomaticDimension
    }

}

extension MenuViewController: UITableViewDataSource {

    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 10
    }

    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        return tableView.dequeueReusableCellWithIdentifier(NSStringFromClass(UITableViewCell), forIndexPath: indexPath)
    }

    func tableView(tableView: UITableView, willSelectRowAtIndexPath indexPath: NSIndexPath) -> NSIndexPath? {
        if indexPath.row == 8 {
            if !MFMailComposeViewController.canSendMail() {
                return nil
            }
        }
        if indexPath.row == 10 {
            return nil
        }
        return indexPath
    }

    func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell, forRowAtIndexPath indexPath: NSIndexPath) {
        //cell.selectionStyle = .None
	cell.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator

        switch indexPath.row {

	case 0:
            cell.imageView!.image = Image.getImage(named: "standardmap.png")!.resizeToWidth(16)
            cell.textLabel!.text = NSLocalizedString("Standard Map", comment: "tableview label")
            cell.accessoryType = UITableViewCellAccessoryType.None

	case 1:
            cell.imageView!.image = Image.getImage(named: "hybridmap.png")!.resizeToWidth(16)
            cell.textLabel!.text = NSLocalizedString("Hybrid Map", comment: "tableview label")
            cell.accessoryType = UITableViewCellAccessoryType.None

	case 2:
            cell.imageView!.image = Image.getImage(named: "reload.png")!.resizeToWidth(16)
            cell.textLabel!.text = NSLocalizedString("Refresh Screen", comment: "tableview label")
            cell.accessoryType = UITableViewCellAccessoryType.None

	case 3:
            cell.imageView!.image = Image.getImage(named: "profile.png")!
            cell.textLabel!.text = NSLocalizedString("My Profile", comment: "tableview label")

	case 4:
            cell.imageView!.image = Image.getImage(named: "palette.png")!.resizeToWidth(16)
            cell.textLabel!.text = NSLocalizedString("Colors", comment: "tableview label")

	case 5:
            cell.imageView!.image = Image.getImage(named: "faq.png")!
            cell.textLabel!.text = NSLocalizedString("FAQ", comment: "tableview label")

	case 6:
            cell.imageView!.image = Image.getImage(named: "privacy.png")!
            cell.textLabel!.text = NSLocalizedString("Privacy Policy", comment: "tableview label")

	case 7:
            cell.imageView!.image = Image.getImage(named: "about.png")!
            cell.textLabel!.text = NSLocalizedString("Term of Use", comment: "tableview label")

	case 8:
            cell.imageView!.image = Image.getImage(named: "feedback.png")!
            cell.textLabel!.text = NSLocalizedString("Feedback", comment: "tableview label")
            if MFMailComposeViewController.canSendMail() {
                cell.imageView!.alpha = 1.0
                cell.textLabel!.alpha = 1.0
            } else {
                cell.imageView!.alpha = 0.5
                cell.textLabel!.alpha = 0.5
            }

	case 9:
            cell.imageView!.image = Image.getImage(named: "profile.png")!
            cell.textLabel!.text = NSLocalizedString("Location History", comment: "action label")

	case 10:
            cell.accessoryType = UITableViewCellAccessoryType.None

	case 11:
            cell.accessoryType = UITableViewCellAccessoryType.None
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

                cell.backgroundColor = tableView.backgroundColor
            }

	default: break
        }
    }

    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)

        let navigationController = slideMenuController()!.mainViewController as! UINavigationController

	switch indexPath.row {

	case 0:
            if let mapViewController = self.mapViewController() {
                mapViewController.normalTapDetected()
            }

	case 1:
            if let mapViewController = self.mapViewController() {
                mapViewController.hybridTapDetected()
            }

	case 2:
            if let mapViewController = self.mapViewController() {
                mapViewController.refresh()
            }

	case 3:
            let controller = ProfileTableViewController()
            navigationController.pushViewController(controller, animated: true)

	case 4:
            let controller = ColorTableViewController()
            navigationController.pushViewController(controller, animated: true)

	case 5:
            let controller = WebViewController()
            controller.title = NSLocalizedString("FAQ", comment: "tableview label")
            controller.url = "\(Router.WebURLString)/m/faq.html?lang=\(UIDevice.lang)"
            navigationController.pushViewController(controller, animated: true)

	case 6:
            let controller = WebViewController()
            controller.title = NSLocalizedString("Privacy Policy", comment: "tableview label")
            controller.url = "\(Router.WebURLString)/m/privacy.html?lang=\(UIDevice.lang)"
            navigationController.pushViewController(controller, animated: true)

	case 7:
            let controller = WebViewController()
            controller.title = NSLocalizedString("Term of Use", comment: "tableview label")
            controller.url = "\(Router.WebURLString)/m/terms.html?lang=\(UIDevice.lang)"
            navigationController.pushViewController(controller, animated: true)

        case 8:
            if MFMailComposeViewController.canSendMail() {
                let controller = MFMailComposeViewController()
                controller.mailComposeDelegate = self
                controller.setToRecipients([NSLocalizedString("cocoger.routeal@gmail.com", comment: "feedback email address")])
                controller.setSubject(NSLocalizedString("User Feedback", comment: "subject message for Feedback"))
                navigationController.presentViewController(controller, animated: true) {
                    let msg = NSLocalizedString(
                            "Your suggestions, ideas, and problem reporting are important to us.  Thank you.",
                            comment: "body message for Feedback")
                    UIAlertController.simpleAlert(msg, dismiss: 15)
                }
            }

	case 9:
            navigationController.pushViewController(LocationHistoryViewController(), animated: true)

	case 10: break

	case 11:
            PKHUD.sharedHUD.show()
            User.signout() {
                dispatch_async(dispatch_get_main_queue()) {
                    PKHUD.sharedHUD.hide(animated: false)
                    locationTracker.stop()
                    if let rootNavigatoinController = UIApplication.sharedApplication().keyWindow?.rootViewController as? UINavigationController {
                        rootNavigatoinController.setViewControllers([WelcomeViewController()], animated: false)
                    }
                }
            }

	    default: break
	}

        self.closeLeft()
    }
}

extension MenuViewController: MFMailComposeViewControllerDelegate {

    func mailComposeController(controller: MFMailComposeViewController, didFinishWithResult result: MFMailComposeResult, error: NSError?) {
        controller.dismissViewControllerAnimated(true, completion: nil)
        switch result {
        case MFMailComposeResultSent:
            // sent the message to the server
	    break
        default: break
        }
    }

}

extension MenuViewController: FBSDKLoginButtonDelegate {

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

    func loginButton(loginButton: FBSDKLoginButton, didCompleteWithResult result: FBSDKLoginManagerLoginResult!, error: NSError!) {
        // do nothing
    }

}
