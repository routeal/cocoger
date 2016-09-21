//
//  SignupTableViewController.swift
//  SokoraNow
//
//  Created by Hiroshi Watanabe on 6/30/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit
import MessageUI
import FBSDKCoreKit

class InviteTableViewController: UITableViewController {

    init() {
        super.init(style:UITableViewStyle.Grouped)
    }

    required init(coder: NSCoder) {
        fatalError("NSCoding not supported")
    }

    private override init(nibName nibNameOrNil: String!, bundle nibBundleOrNil: NSBundle!) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    deinit {
        print("deinit: InviteTableViewController")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        self.title = NSLocalizedString("Invite Friends", comment: "viewcontroller title")
        self.navigationController!.setNavigationBarHidden(false, animated: false)
        self.tableView.registerClass(UITableViewCell.classForCoder(), forCellReuseIdentifier: NSStringFromClass(UITableViewCell))
    }

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        var numberOfRowsInSection = 3
        if FBSDKAccessToken.currentAccessToken() != nil {
            numberOfRowsInSection++
        }
        return numberOfRowsInSection
    }

    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        return tableView.dequeueReusableCellWithIdentifier(NSStringFromClass(UITableViewCell), forIndexPath: indexPath) 
    }

    override func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell, forRowAtIndexPath indexPath: NSIndexPath) {
        let row = indexPath.row
        if row == 0 {
            cell.imageView!.image = Image.getImage(named: "userplus.png")!
            cell.textLabel!.text = NSLocalizedString("Send to App User", comment: "tableview label")
            //cell.textLabel!.font = UIFont.systemFontOfSize(UIFont.systemFontSize())
            cell.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
        } else if row == 1 {
            cell.imageView!.image = Image.getImage(named: "email.png")!
            cell.textLabel!.text = NSLocalizedString("SMS", comment: "tableview label")
            //cell.textLabel!.font = UIFont.systemFontOfSize(UIFont.systemFontSize())
            cell.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
            if !MFMessageComposeViewController.canSendText() {
                cell.imageView!.alpha = 0.5
                cell.textLabel!.alpha = 0.5
            }
        } else if row == 2 {
            cell.imageView!.image = Image.getImage(named: "email.png")!
            cell.textLabel!.text = NSLocalizedString("Email", comment: "tableview label")
            //cell.textLabel!.font = UIFont.systemFontOfSize(UIFont.systemFontSize())
            cell.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
            if !MFMailComposeViewController.canSendMail() {
                cell.imageView!.alpha = 0.5
                cell.textLabel!.alpha = 0.5
            }
        } else if row == 3 {
            cell.imageView!.image = Image.getImage(named: "FB-f-Logo__blue_29.png")!.resizeToWidth(16)
            cell.textLabel!.text = NSLocalizedString("Facebook", comment: "tableview label")
            //cell.textLabel!.font = UIFont.systemFontOfSize(UIFont.systemFontSize())
            cell.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
        }
    }

    override func tableView(tableView: UITableView, willSelectRowAtIndexPath indexPath: NSIndexPath) -> NSIndexPath? {
        if indexPath.row == 1 {
            if !MFMessageComposeViewController.canSendText() {
                return nil
            }
        }
        if indexPath.row == 2 {
            if !MFMailComposeViewController.canSendMail() {
                return nil
            }
        }
        return indexPath
    }

    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        switch indexPath.row {
        case 0:
            let controller = FindTableViewController()
            self.navigationController!.setNavigationBarHidden(false, animated: false)
            self.navigationController!.pushViewController(controller, animated: true)
        case 1:
            let controller = MFMessageComposeViewController()
            controller.body = NSLocalizedString("SMS message body", comment: "body message for SMS")
            controller.messageComposeDelegate = self
            self.navigationController!.presentViewController(controller, animated: true, completion: nil)
        case 2:
            let controller = MFMailComposeViewController()
            controller.mailComposeDelegate = self
            controller.setSubject(NSLocalizedString("let's cocoger", comment: "subject message for Email"))
            controller.setMessageBody(NSLocalizedString("Email message body", comment: "body message for Email"), isHTML: false)
            self.navigationController!.presentViewController(controller, animated: true, completion: nil)
        case 3:
            let controller = ProviderInviteTableViewController()
            self.navigationController!.setNavigationBarHidden(false, animated: false)
            self.navigationController!.pushViewController(controller, animated: true)
        default: break
        }
    }

}

extension InviteTableViewController: MFMailComposeViewControllerDelegate {

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

extension InviteTableViewController: MFMessageComposeViewControllerDelegate {

    func messageComposeViewController(controller: MFMessageComposeViewController, didFinishWithResult result: MessageComposeResult) {
        self.dismissViewControllerAnimated(true, completion: nil)
    }

}
