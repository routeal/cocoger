//
//  SignupTableViewController.swift
//  SokoraNow
//
//  Created by Hiroshi Watanabe on 6/30/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit
import PKHUD

class SendInviteTableViewController: UITableViewController {

    var userID: String!
    var userName: String!
    var userEmail: String!
    var textView: UITextView!
    var locationRange: LocationRangeView!

    init(id: String, name: String, email: String) {
        super.init(style:UITableViewStyle.Grouped)
        // initial title
        self.title = NSLocalizedString("Send Invitation", comment: "viewcontroller title")
        self.userID = id
        self.userName = name
        self.userEmail = email
    }

    required init(coder: NSCoder) {
        fatalError("NSCoding not supported")
    }

    private override init(nibName nibNameOrNil: String!, bundle nibBundleOrNil: NSBundle!) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    deinit {
        print("deinit: SendInviteTableViewController")
    }

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.title = NSLocalizedString("Send Invitation", comment: "viewcontroller title")

        self.tableView.keyboardDismissMode = .OnDrag
        self.tableView.registerClass(UITableViewCell.classForCoder(), forCellReuseIdentifier: NSStringFromClass(UITableViewCell))

        let cancelStr = NSLocalizedString("Cancel", comment: "barbutton title")
        self.navigationItem.leftBarButtonItem = UIBarButtonItem(title: cancelStr, style: .Plain, target: self, action: "cancel")
        let sendStr = NSLocalizedString("Send", comment: "barbutton title")
        self.navigationItem.rightBarButtonItem = UIBarButtonItem(title: sendStr, style: .Plain, target: self, action: "send")
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

}

extension SendInviteTableViewController {

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 3
    }

    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        return tableView.dequeueReusableCellWithIdentifier(NSStringFromClass(UITableViewCell), forIndexPath: indexPath) 
    }

    override func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell, forRowAtIndexPath indexPath: NSIndexPath) {
        let row = indexPath.row
        if row == 0 {
            let label: UILabel = UILabel(frame: CGRectZero)
            //label.font = UIFont.systemFontOfSize(UIFont.labelFontSize())
            label.text = NSLocalizedString("To:", comment: "tableview label")
            label.translatesAutoresizingMaskIntoConstraints = false
            cell.contentView.addSubview(label)

            let name: UILabel = UILabel(frame: CGRectZero)
            //name.font = UIFont.systemFontOfSize(UIFont.labelFontSize())
            name.text = userName
            name.translatesAutoresizingMaskIntoConstraints = false
            cell.contentView.addSubview(name)

            let views = ["label":label, "name":name]
            let constraints = ["H:|-[label]-[name]", "V:|-[label]-|", "V:|-[name]-|"]
            cell.contentView.addLayoutConstraints(constraints, views: views)
        } else if row == 1 {
            textView = UITextView(frame: CGRectZero)
            textView.text = NSLocalizedString("Hi, I'd like to share locations with you by distance below.  Thanks.", comment: "tableview text")
            //textView.font = UIFont.systemFontOfSize(UIFont.labelFontSize())
            textView.translatesAutoresizingMaskIntoConstraints = false
            cell.contentView.addSubview(textView)
            cell.contentView.addLayoutConstraints(["H:|-[text]-|", "V:|-[text]-|"], views: ["text":textView])
        } else if row == 2 {
            locationRange = LocationRangeView(frame: CGRectZero)
            locationRange.size = 2
            //locationRange.noneHidden = true
            locationRange.translatesAutoresizingMaskIntoConstraints = false
            cell.contentView.addSubview(locationRange)
            cell.contentView.addLayoutConstraints(["H:|-[range]-|", "V:|-[range]-|"], views: ["range":locationRange])
        }
    }

    override func tableView(tableView: UITableView, willSelectRowAtIndexPath indexPath: NSIndexPath) -> NSIndexPath? {
        return nil
    }

    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        if indexPath.row == 0 {
            return 44
        } else if indexPath.row == 1 {
            return 128
        } else if indexPath.row == 2 {
            return 64
        }
        return UITableViewAutomaticDimension
    }
}

extension SendInviteTableViewController {

    func cancel() {
        self.navigationController!.popViewControllerAnimated(true)
    }

    func send() {
        print("id=\(userID) name=\(userName) email=\(userEmail) location=\(locationRange.value)")
        print("text=\(textView.text)")

        textView.resignFirstResponder()

        PKHUD.sharedHUD.show()

        Friend.invite(userID, range: locationRange.value, message: textView.text,
            completion: { [weak self] (status: Bool, error: String?) in
                dispatch_async(dispatch_get_main_queue()) {
                    PKHUD.sharedHUD.hide(animated: false)
                    if status {
                        let msgStr = NSLocalizedString("Invitation has been sent.", comment: "alert message")
                        let okLabel = NSLocalizedString("OK", comment: "alert action")
                        let alert = UIAlertController(title: msgStr, message: nil, preferredStyle: .Alert)
                        alert.addAction(UIAlertAction(title: okLabel, style: .Cancel) { action -> Void in
                            self!.cancel()
                        })
                        alert.present(animated: true, completion: nil)
                    } else {
                        let msgStr = NSLocalizedString("Failed to send invitation", comment: "alert message")
                        UIAlertController.simpleAlert(msgStr, message: error)
                    }
                }
            })
    }

}
