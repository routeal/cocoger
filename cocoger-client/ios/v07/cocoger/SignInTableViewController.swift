//
//  SignupTableViewController.swift
//  SokoraNow
//
//  Created by Hiroshi Watanabe on 6/30/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit
import PKHUD
import FBSDKCoreKit
import FBSDKLoginKit
import SlideMenuControllerSwift

class SignInTableViewController: UITableViewController {

    enum Cell: Int {
        case Facebook = 0,
             Profile,
             SignIn,
             SignUp,
             Total
    }

    enum Profile: Int {
        case Email = 0,
             Password,
             Name,
             Gender,
             Bod,
             Total
    }

    enum SignMode  {
        case In
        case Up
    }

    let FacebookIdentifier: String = "Facebook"
    let ProfileIdentifier: String = "Profile"
    let SignInIdentifier: String = "SignIn"
    let SignUpIdentifier: String = "SignUp"

    let facebookReadPermissions = ["public_profile", "email", "user_friends"]

    // initial mode is Sign In
    var signMode: SignMode = .In

    // current textfield whose has the input editor
    var currentTextField: UITextField!

    var profileTexts = [Int:UITextField]()

    var genderControl: UISegmentedControl!

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
        print("deinit: SignInTableViewController")
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        self.title = NSLocalizedString("Sign In", comment: "viewcontroller title")
        self.navigationController!.setNavigationBarHidden(false, animated: false)
        self.navigationController?.setToolbarHidden(true, animated: false)
        self.tableView.keyboardDismissMode = .OnDrag
        self.tableView.registerClass(UITableViewCell.classForCoder(), forCellReuseIdentifier: FacebookIdentifier)
        self.tableView.registerClass(InputTableViewCell.classForCoder(), forCellReuseIdentifier: ProfileIdentifier)
        self.tableView.registerClass(UITableViewCell.classForCoder(), forCellReuseIdentifier: SignInIdentifier)
        self.tableView.registerClass(UITableViewCell.classForCoder(), forCellReuseIdentifier: SignUpIdentifier)
    }

}

// UITableViewDataSource
extension SignInTableViewController {

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return Cell.Total.rawValue
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch section {
        case Cell.Facebook.rawValue:
            return 1
        case Cell.Profile.rawValue:
            switch signMode {
            case .In:
                return 2
            case .Up:
                return Profile.Total.rawValue
            }
        case Cell.SignIn.rawValue:
            if signMode == .In {
                return 2
            } else if signMode == .Up {
                return 1
            }
        case Cell.SignUp.rawValue:
            return 1
        default:
            return 1
        }
        return 1
    }

    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var id: String!
        if indexPath.section == Cell.Facebook.rawValue {
            id = FacebookIdentifier
        } else if indexPath.section == Cell.Profile.rawValue {
            id = ProfileIdentifier
        } else if indexPath.section == Cell.SignIn.rawValue {
            id = SignInIdentifier
        } else if indexPath.section == Cell.SignUp.rawValue {
            id = SignUpIdentifier
        }
        return tableView.dequeueReusableCellWithIdentifier(id, forIndexPath: indexPath)
    }

}

// UITableViewDelegate
extension SignInTableViewController {

    override func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell, forRowAtIndexPath indexPath: NSIndexPath) {
        switch (indexPath.section) {
        case Cell.Facebook.rawValue:
            let fbButton : FBSDKLoginButton = FBSDKLoginButton()
            fbButton.translatesAutoresizingMaskIntoConstraints = false
            fbButton.center = cell.center
            fbButton.readPermissions = facebookReadPermissions
            fbButton.delegate = self

            cell.contentView.addSubview(fbButton)
            cell.contentView.addLayoutConstraints(["H:|-60-[fb]-60-|", "V:|[fb]|"], views: ["fb":fbButton])

            cell.selectionStyle = .None
            cell.backgroundColor = tableView.backgroundColor

        case Cell.Profile.rawValue:
            let c: InputTableViewCell = cell as! InputTableViewCell
            c.input.delegate = self
            switch indexPath.row {
            case Profile.Email.rawValue:
                c.label.text = NSLocalizedString("Email", comment: "tableview label")
                if signMode == .In && User.provider == "local" {
                    c.input.text = User.email
                } else {
                    c.input.text = ""
                }
                c.input.placeholder = NSLocalizedString("user@cocoger.com", comment: "tableview place holder")
                c.input.keyboardType = .EmailAddress
                c.input.returnKeyType = .Next
                profileTexts[Profile.Email.rawValue] = c.input
                currentTextField = c.input
            case Profile.Password.rawValue:
                c.label.text = NSLocalizedString("Password", comment: "tableview label")
                c.input.text = ""
                c.input.placeholder = NSLocalizedString("Min. 6 characters", comment: "tableview place holder")
                c.input.keyboardType = .Default
                c.input.returnKeyType = (signMode == .In) ? .Done : .Next
                c.input.secureTextEntry = true
                profileTexts[Profile.Password.rawValue] = c.input
            case Profile.Name.rawValue:
                c.label.text = NSLocalizedString("Name", comment: "tableview label")
                c.input.placeholder = NSLocalizedString("cocoger", comment: "cocoger")
                c.input.keyboardType = .Default
                c.input.returnKeyType = .Next
                profileTexts[Profile.Name.rawValue] = c.input
            case Profile.Gender.rawValue:
                let c: InputTableViewCell = cell as! InputTableViewCell
                c.label.hidden = true
                c.input.hidden = true

                let label = UILabel()
                label.translatesAutoresizingMaskIntoConstraints = false
                //label.font = UIFont.systemFontOfSize(UIFont.labelFontSize())
                label.text = NSLocalizedString("Gender", comment: "tableview label")
                cell.contentView.addSubview(label)

                let genders = [NSLocalizedString("Boy", comment: "segmentedcontrol label"),
                               NSLocalizedString("Girl", comment: "segmentedcontrol label")]
                genderControl = UISegmentedControl(items: genders)
                genderControl.translatesAutoresizingMaskIntoConstraints = false
                genderControl.selectedSegmentIndex = User.gender
                //var attr = NSDictionary(object: UIFont.systemFontOfSize(UIFont.labelFontSize()), forKey: NSFontAttributeName)
                //genderControl.setTitleTextAttributes(attr as [NSObject : AnyObject], forState: .Normal)
                cell.contentView.addSubview(genderControl)

                let views = ["label":label, "gender":genderControl]
                let constraints = ["H:|-[label(90)]-[gender(160)]", "V:|-[label]-|", "V:|-[gender]-|"]
                cell.contentView.addLayoutConstraints(constraints, views: views)

            case Profile.Bod.rawValue:
                c.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
                c.label.text = NSLocalizedString("Birth Year", comment: "tableview label")
                c.input.text = "\(User.bod)"
                c.input.keyboardType = .Default
                c.input.returnKeyType = .Done
                profileTexts[Profile.Bod.rawValue] = c.input
            default: break
            }

        case Cell.SignIn.rawValue:
            if let label = cell.textLabel {
                label.textAlignment = .Center
                if indexPath.row == 0 {
                    label.text = (signMode == .In) ?
                                 NSLocalizedString("Sign In", comment: "tableview label") :
                                 NSLocalizedString("Sign Up", comment: "tableview label")
                    label.textColor = tableView.tintColor
                    //label.font = UIFont.systemFontOfSize(UIFont.buttonFontSize())
                    label.enabled = true
                } else if indexPath.row == 1 {
                    label.text = NSLocalizedString("Forgot Password", comment: "tableview label")
                    label.textColor = tableView.tintColor
                    //label.font = UIFont.systemFontOfSize(UIFont.systemFontSize())
                }
            }

        case Cell.SignUp.rawValue:
            if let label = cell.textLabel {
                label.text = (signMode == .In) ?
                             NSLocalizedString("Don't have an account?  Sign Up",
                                               comment: "signup prompt message") :
                             NSLocalizedString("Have an account?  Sign In",
                                               comment: "signin prompt message")
                label.textColor = tableView.tintColor
                //label.font = UIFont.systemFontOfSize(UIFont.systemFontSize())
                label.textAlignment = .Center
            }

        default: break
        }
    }

    override func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        return nil
    }

    override func tableView(tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        if section == Cell.Facebook.rawValue {
            let view = UIView()
            let footerLabel: UILabel = UILabel()
            footerLabel.text = NSLocalizedString("OR", comment: "tableview footer")
            footerLabel.textAlignment = .Center
            footerLabel.translatesAutoresizingMaskIntoConstraints = false
            view.addSubview(footerLabel)
            view.addLayoutConstraints(["H:|-[label]-|", "V:|-[label]-|"], views: ["label":footerLabel])
            return view

        }
        return nil
    }

    override func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if section == Cell.Profile.rawValue {
            return 1
        }
        /*
        if section == Cell.Facebook.rawValue {
            return UITableViewAutomaticDimension
        } else if section == Cell.Profile.rawValue {
            return 1
        }
        return 4 // UITableViewAutomaticDimension
        */
        return UITableViewAutomaticDimension
    }

    override func tableView(tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        if section == Cell.Facebook.rawValue {
            return 28
        }
        return UITableViewAutomaticDimension
    }

    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return UITableViewAutomaticDimension
    }

    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)

        if indexPath.section == Cell.Facebook.rawValue {
            // do nothing
        } else if indexPath.section == Cell.SignIn.rawValue {
            if indexPath.row == 0 {
                if signMode == .In {
                    signin()
                } else if signMode == .Up {
                    signup()
                }
            } else if indexPath.row == 1 {
                forgotPassword()
            }
        } else if indexPath.section == Cell.SignUp.rawValue {
            if signMode == .In {
                let vc = LicenseTableViewController()
                vc.delegate = self
                self.navigationController!.pushViewController(vc, animated: true)
            } else if signMode == .Up {
                self.title = NSLocalizedString("Sign In", comment: "tableview label")
                signMode = .In
            }
            tableView.reloadData()
        }
    }

}

extension SignInTableViewController: UITextFieldDelegate {

    func textFieldShouldReturn(textField: UITextField) -> Bool {
        if textField == profileTexts[Profile.Email.rawValue] {
            profileTexts[Profile.Password.rawValue]!.becomeFirstResponder()
        } else if textField == profileTexts[Profile.Password.rawValue] {
            if signMode == .In {
                textField.resignFirstResponder()
                signin()
            } else if signMode == .Up {
                profileTexts[Profile.Name.rawValue]!.becomeFirstResponder()
            }
        } else if textField == profileTexts[Profile.Name.rawValue] {
            textField.resignFirstResponder()
        } else if textField == profileTexts[Profile.Bod.rawValue] {
            textField.resignFirstResponder()
            signup()
        }
        return true
    }

    func textFieldShouldBeginEditing(textField: UITextField) -> Bool {
        if textField == profileTexts[Profile.Bod.rawValue] {
            currentTextField.resignFirstResponder()
            let controller = AgePickerViewController()
            controller.target = profileTexts[Profile.Bod.rawValue]
            controller.year = Int(profileTexts[Profile.Bod.rawValue]!.text!)!
            self.navigationController!.pushViewController(controller, animated: true)
            return false
        }
        return true
    }

    func textField(textField: UITextField, shouldChangeCharactersInRange range: NSRange, replacementString string: String) -> Bool {
        return true
    }

}

extension SignInTableViewController {

    func forgotPassword() {
        let controller = ForgotTableViewController()
        self.navigationController!.pushViewController(controller, animated: true)
    }

    func showMainViewController() {
        let mainViewController = UINavigationController(custom: true, rootViewController: MapViewController())
        let leftMenuViewController = MenuViewController()
        let slideMenuController = SlideMenuController(mainViewController: mainViewController,
                                                      leftMenuViewController: leftMenuViewController)
        slideMenuController.automaticallyAdjustsScrollViewInsets = true

        self.navigationController?.setNavigationBarHidden(true, animated: false)
        self.navigationController?.setToolbarHidden(true, animated: false)

        self.navigationController!.setViewControllers([slideMenuController], animated: false)
    }

    func signup() {
        if profileTexts[Profile.Email.rawValue]!.text!.isEmpty ||
          profileTexts[Profile.Password.rawValue]!.text!.isEmpty ||
          profileTexts[Profile.Name.rawValue]!.text!.isEmpty ||
          profileTexts[Profile.Bod.rawValue]!.text!.isEmpty {
            UIAlertController.simpleAlert(
                NSLocalizedString("Found an empty field", comment: "alert for empty field"))
            return
        }
        if !profileTexts[Profile.Email.rawValue]!.text!.isEmail {
            UIAlertController.simpleAlert(
                NSLocalizedString("Email in invalid format",
                                  comment: "alert for invalid email address"))
            return
        }
        if profileTexts[Profile.Password.rawValue]!.text!.characters.count < 6 {
            UIAlertController.simpleAlert(
                NSLocalizedString("Password too short", comment: "alert for short password"))
            return
        }

        PKHUD.sharedHUD.show()

        let defaults = NSUserDefaults.standardUserDefaults()
        let deviceToken: String? = defaults.stringForKey("deviceToken")

        User.signup(profileTexts[Profile.Name.rawValue]!.text!,
                    gender: genderControl.selectedSegmentIndex,
                    bod: Int(profileTexts[Profile.Bod.rawValue]!.text!)!,
                    email: profileTexts[Profile.Email.rawValue]!.text!,
                    password: profileTexts[Profile.Password.rawValue]!.text!,
                    deviceToken: deviceToken,
                    completion: {[weak self](status: Bool, error: String?) in

            dispatch_async(dispatch_get_main_queue()) {

                PKHUD.sharedHUD.hide(animated: false)

                if status {
                    self!.showMainViewController()
                } else {
                    var msg = "Can't sign up due to an error"
                    if let e = error {
                        msg = e
                    }
                    UIAlertController.simpleAlert(NSLocalizedString(msg, comment: "signup error"))
                }
            }
        })
    }

    func signin() {
        if profileTexts[Profile.Email.rawValue]!.text!.isEmpty || profileTexts[Profile.Password.rawValue]!.text!.isEmpty {
            UIAlertController.simpleAlert(NSLocalizedString("Found an empty field", comment: "alert for empty field"))
            return
        }
        if !profileTexts[Profile.Email.rawValue]!.text!.isEmail {
            UIAlertController.simpleAlert(NSLocalizedString("Email in invalid format", comment: "alert for invalid email"))
            return
        }
        if profileTexts[Profile.Password.rawValue]!.text!.characters.count < 6 {
            UIAlertController.simpleAlert(NSLocalizedString("Password too short", comment: "alert for short password"))
            return
        }

        PKHUD.sharedHUD.show()

        let defaults = NSUserDefaults.standardUserDefaults()
        let deviceToken: String? = defaults.stringForKey("deviceToken")

        User.signin(profileTexts[Profile.Email.rawValue]!.text!,
                    password: profileTexts[Profile.Password.rawValue]!.text!,
                    deviceToken: deviceToken,
                    completion: {[weak self](status: Bool, error: String?) in

            dispatch_async(dispatch_get_main_queue()) {

                PKHUD.sharedHUD.hide(animated: false)

                if status {
                    // delete all the other view controllers
                    self!.showMainViewController()
                } else {
                    var msg = "Can't sign in due to an error"
                    if let error = error {
                        msg = error
                    }
                    UIAlertController.simpleAlert(NSLocalizedString(msg, comment: "signin error"))
                }
            }
        })
    }

}

extension SignInTableViewController: LicenseTableViewControllerDelegate {

    func done() {
        if self.signMode == .In {
            self.title = NSLocalizedString("Sign Up", comment: "tableview label")
            self.signMode = .Up
            tableView.reloadData()
        } else if self.signMode == .Up {
            self.showMainViewController()
        }
    }

}

extension SignInTableViewController: FBSDKLoginButtonDelegate {

    func loginButton(loginButton: FBSDKLoginButton, didCompleteWithResult result: FBSDKLoginManagerLoginResult!, error: NSError!) {
        if error != nil {
            print("error")
            return
        }

        if result.isCancelled {
            print("result cancelled")
            return
        }

        let declinedPermissions = Array(result.declinedPermissions).map( {"\($0)"} )
        if (declinedPermissions.indexOf("email") != nil) {
            // TODO: missing email is fatal, should abort the facebook login
            print("email is missing")

            UIAlertController.simpleAlert(
                NSLocalizedString("Email permission is required for Facebook login.",
                                  comment: "facebook email permission declined"))

            // revoke the facebook login
            let request = FBSDKGraphRequest(graphPath:"/me/permissions", parameters: nil, HTTPMethod: "delete")

            request.startWithCompletionHandler { (connection : FBSDKGraphRequestConnection!, result : AnyObject!, error : NSError!) -> Void in
                FBSDKLoginManager().logOut()
            }

            return
        }

        PKHUD.sharedHUD.show()

        let defaults = NSUserDefaults.standardUserDefaults()
        let deviceToken: String? = defaults.stringForKey("deviceToken")

        User.signin(result.token.tokenString, fbID: result.token.userID, deviceToken: deviceToken,
                    completion: {[weak self](status: Bool, error: String?)  in
            dispatch_async(dispatch_get_main_queue()) {
                PKHUD.sharedHUD.hide(animated: false)
                if status {
                    if self!.signMode == .Up {
                        self!.showMainViewController()
                    } else {
                        self!.signMode = .Up
                        let vc = LicenseTableViewController()
                        vc.navigationItem.hidesBackButton = true
                        vc.delegate = self
                        self!.navigationController!.pushViewController(vc, animated: true)
                    }
                } else {
                    UIAlertController.simpleAlert(
                        NSLocalizedString("Facebook login error",
                                          comment: "facebook login error"), message: error)
                }
            }
        })
    }

    func loginButtonDidLogOut(loginButton: FBSDKLoginButton) {
    }
}
