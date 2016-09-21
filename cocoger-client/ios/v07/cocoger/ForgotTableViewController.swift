//
//  ForgotViewController.swift
//  sokora
//
//  Created by Hiroshi Watanabe on 10/22/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit
import PKHUD

class ForgotTableViewController: UITableViewController {

    let EmailIdentifier: String = "Email"
    let SendIdentifier: String = "Send"

    var emailText: UITextField!

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
        print("deinit: ForgotTableViewController")
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        self.title = NSLocalizedString("Password Forgot", comment: "viewcontroller title")
        self.tableView.keyboardDismissMode = .OnDrag
        self.tableView.registerClass(UITableViewCell.classForCoder(), forCellReuseIdentifier: EmailIdentifier)
        self.tableView.registerClass(UITableViewCell.classForCoder(), forCellReuseIdentifier: SendIdentifier)
    }

}

extension ForgotTableViewController {

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 2
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 1
    }

    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var id: String!
        if indexPath.section == 0 {
            id = EmailIdentifier
        } else if indexPath.section == 1 {
            id = SendIdentifier
        }
        return tableView.dequeueReusableCellWithIdentifier(id, forIndexPath: indexPath) 
    }

    override func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell, forRowAtIndexPath indexPath: NSIndexPath) {
        switch (indexPath.section) {
        case 0:
            cell.textLabel!.text = NSLocalizedString("Email", comment: "tableview label")
            cell.textLabel!.translatesAutoresizingMaskIntoConstraints = false
            cell.contentView.addSubview(cell.textLabel!)

            let input = UITextField()
            input.delegate = self
            input.clearButtonMode = .WhileEditing
            input.autocapitalizationType = .None
            input.autocorrectionType = .No
            input.keyboardType = .EmailAddress
            input.returnKeyType = .Done
            input.translatesAutoresizingMaskIntoConstraints = false
            cell.contentView.addSubview(input)
            emailText = input

            let views = ["label":cell.textLabel!, "input":input]
            let constraints = ["V:|-[label]-|","V:|-[input]-|","H:|-[label(80)]-[input]|"]
            cell.contentView.addLayoutConstraints(constraints, views: views)

        case 1:
            cell.textLabel!.text = NSLocalizedString("Send", comment: "tableview label")
            cell.textLabel!.textColor = tableView.tintColor
            cell.textLabel!.textAlignment = .Center

        default: break
        }
    }

    override func tableView(tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        if (section == 0) {
            let view = UIView()

            let title = UILabel()
            title.translatesAutoresizingMaskIntoConstraints = false
            title.font = UIFont.boldSystemFontOfSize(20)
            title.text = NSLocalizedString("Restore your password", comment: "password reset")
            title.textAlignment = .Center
            view.addSubview(title)

            let subtitle = UILabel()
            subtitle.font = UIFont.systemFontOfSize(UIFont.labelFontSize())
            subtitle.translatesAutoresizingMaskIntoConstraints = false
            subtitle.textAlignment = .Center
            subtitle.text = NSLocalizedString("Enter your account email", comment: "password reset")
            view.addSubview(subtitle)

            view.addLayoutConstraints(["V:|-[title]-[subtitle]-|",
                                       "H:|-[title]-|",
                                       "H:|-[subtitle]-|"],
                                      views: ["title": title, "subtitle": subtitle])

            return view
        }
        return nil
    }

    override func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if (section == 0) {
            return 100
        }
        return UITableViewAutomaticDimension
    }

    override func tableView(tableView: UITableView, willSelectRowAtIndexPath indexPath: NSIndexPath) -> NSIndexPath? {
        if indexPath.section == 1 {
            return indexPath
        }
        return nil
    }

    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        if emailText.text!.isEmail {
            restore()
        }
    }

}

extension ForgotTableViewController: UITextFieldDelegate {

    func textFieldShouldReturn(textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        if emailText.text!.isEmail {
            restore()
        }
        return true
    }

}

extension ForgotTableViewController {

    func restore() {
        PKHUD.sharedHUD.show()

        User.restorePassword(emailText.text!,
                             completion: { [weak self](status: Bool, error: String?)  in
            dispatch_async(dispatch_get_main_queue()) {
                PKHUD.sharedHUD.hide(animated: false)
                if status {
                    let fmt = NSLocalizedString("An email has been sent to %@ with further instructions.",
                                                comment: "password restore")
                    let msg = String(format: fmt, self!.emailText.text!)
                    UIAlertController.simpleAlert(
                        msg,
                        completion: {(alert: UIAlertController) -> Void in
                            self!.navigationController!.popViewControllerAnimated(false)
                        })
                } else {
                    var msg = NSLocalizedString("Failed to send the password reset.  Please try again later.",
                                                comment: "password restore")
                    if let e = error {
                        msg = e
                    }
                    UIAlertController.simpleAlert(
                        msg,
                        completion: {(alert: UIAlertController) -> Void in
                            self!.navigationController!.popViewControllerAnimated(false)
                        })
                }
            }
        })
    }
}
