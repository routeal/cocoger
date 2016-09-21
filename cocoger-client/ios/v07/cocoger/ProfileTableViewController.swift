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

class ProfileTableViewController: UITableViewController {

    enum Cell: Int {
        case Location = 0,
             Profile,
             Logout,
             Delete,
             Total
    }

    enum Profile: Int {
        case Photo = 0,
             Name,
             Gender,
             Bod,
             Member,
             Total
    }

    var profileTextFields = [Int:UITextField]()
    var genderControl: UISegmentedControl!
    var photoView: PhotoSelectView!
    var hasLocation: Bool = false
    var userLocationAddress: String?
    var photoFacebookImageView: UIImageView!
    var photoViewSwitch: UISwitch!

    init(hasLocation: Bool = false, address: String? = nil) {
        super.init(style:UITableViewStyle.Grouped)
        self.hasLocation = hasLocation
        self.userLocationAddress = address
    }

    required init(coder: NSCoder) {
        fatalError("NSCoding not supported")
    }

    override init(nibName nibNameOrNil: String!, bundle nibBundleOrNil: NSBundle!) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    deinit {
        print("deinit: ProfileTableViewController")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        self.tableView.keyboardDismissMode = .OnDrag
        self.tableView.registerClass(LocationTableViewCell.classForCoder(), forCellReuseIdentifier: NSStringFromClass(LocationTableViewCell))
        self.tableView.registerClass(InputTableViewCell.classForCoder(), forCellReuseIdentifier: NSStringFromClass(InputTableViewCell))
        self.tableView.registerClass(UITableViewCell.classForCoder(), forCellReuseIdentifier: NSStringFromClass(UITableViewCell))
    }

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        let cancelStr = NSLocalizedString("Cancel", comment: "barbutton title")
        let left = UIBarButtonItem(title: cancelStr, style: .Plain, target: self, action: "cancel")
        let updateStr = NSLocalizedString("Update", comment: "barbutton title")
        let right  = UIBarButtonItem(title: updateStr, style: .Plain, target: self, action: "update")
        setupNavigation(User.name, left: left, right: right)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

}

// UITableViewDataSource
extension ProfileTableViewController {

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return hasLocation ? Cell.Total.rawValue : (Cell.Total.rawValue - 2)
    }

    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var id: String!
        let sec = hasLocation ? indexPath.section : (indexPath.section + 1)
        if sec == Cell.Location.rawValue {
            id = NSStringFromClass(LocationTableViewCell)
        } else if sec == Cell.Profile.rawValue {
            id = NSStringFromClass(InputTableViewCell)
        } else if sec == Cell.Logout.rawValue {
            id = NSStringFromClass(UITableViewCell)
        } else if sec == Cell.Delete.rawValue {
            id = NSStringFromClass(UITableViewCell)
        }
        return tableView.dequeueReusableCellWithIdentifier(id, forIndexPath: indexPath)
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        let sec = hasLocation ? section : (section + 1)
        switch sec {
        case Cell.Profile.rawValue:
            return Profile.Total.rawValue
        case Cell.Delete.rawValue:
            if FBSDKAccessToken.currentAccessToken() == nil {
                return 1
            } else {
                return 0
            }
        default:
            return 1
        }
    }

}

// UITableViewDelegate
extension ProfileTableViewController {

    func switchPhotoMode(sender: UISwitch) {
         if (sender.on) {
             setPhotoMode(true)
         } else {
             setPhotoMode(false)
         }
    }

    func setPhotoMode(fb: Bool) {
         if (fb) {
             photoViewSwitch.on = true
             photoFacebookImageView.alpha = 1.0
             photoView.userInteractionEnabled = false
             photoView.alpha = 0.5
         } else {
             photoViewSwitch.on = false
             photoFacebookImageView.alpha = 0.5
             photoView.userInteractionEnabled = true
             photoView.alpha = 1.0
         }
    }

    override func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell, forRowAtIndexPath indexPath: NSIndexPath) {
        let sec = hasLocation ? indexPath.section : (indexPath.section + 1)
        switch sec {
        case Cell.Location.rawValue:
            let c: LocationTableViewCell = cell as! LocationTableViewCell
            c.textLabel!.text = userLocationAddress

        case Cell.Profile.rawValue:
        switch (indexPath.row) {
        case Profile.Photo.rawValue:
            let c: InputTableViewCell = cell as! InputTableViewCell
            c.label.hidden = true
            c.input.hidden = true

            let view = UIView()
            view.translatesAutoresizingMaskIntoConstraints = false
            let label = UILabel()
            label.translatesAutoresizingMaskIntoConstraints = false
            label.text = NSLocalizedString("Photo", comment: "tableviewcell label")
            view.addSubview(label)
            let onoff = UISwitch()
            onoff.addTarget(self, action: "switchPhotoMode:", forControlEvents: .ValueChanged)
            onoff.translatesAutoresizingMaskIntoConstraints = false
            view.addSubview(onoff)
            photoViewSwitch = onoff
            let imageView = UIImageView()
            imageView.translatesAutoresizingMaskIntoConstraints = false
            view.addSubview(imageView)
            if FBSDKAccessToken.currentAccessToken() == nil {
                onoff.hidden = true
                imageView.hidden = true
            } else {
                Image.getImage(User.providerID, type: PhotoType.Facebook.rawValue, completion: {(image: UIImage?) in
                    imageView.image = image
                })
            }
            view.addLayoutConstraints(["H:|[label(100)]-[onoff]-32-[note(32)]",
                                       "V:|-[label]-|",
                                       "V:|-[note(32)]-|",
                                       "V:|-[onoff]-|"],
                                      views: ["label":label, "onoff":onoff, "note":imageView])
            cell.contentView.addSubview(view)

            photoView = PhotoSelectView(frame: CGRectZero)
            photoView.name = User.photoName
            photoView.backgroundColor = UIColor.clearColor()
            photoView.translatesAutoresizingMaskIntoConstraints = false
            cell.contentView.addSubview(photoView)
            cell.contentView.addLayoutConstraints(["H:|-[view]-|",
                                                   "H:|-[photo]-|",
                                                   "V:|[view]-[photo(64)]"],
                                                  views: ["photo":photoView, "view":view])
            photoFacebookImageView = imageView
            setPhotoMode(User.photoType == PhotoType.Facebook.rawValue)

        case Profile.Bod.rawValue:
            let c: InputTableViewCell = cell as! InputTableViewCell
            c.label.text = NSLocalizedString("Birth Year", comment: "tableview label")
            c.input.delegate = self
            c.input.text = "\(User.bod)"
            profileTextFields[Profile.Bod.rawValue] = c.input
            cell.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator

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
            genderControl.selectedSegmentIndex = User.gender
            genderControl.translatesAutoresizingMaskIntoConstraints = false
            //var attr = NSDictionary(object: UIFont.systemFontOfSize(UIFont.labelFontSize()), forKey: NSFontAttributeName)
            //genderControl.setTitleTextAttributes(attr as [NSObject : AnyObject], forState: .Normal)
            cell.contentView.addSubview(genderControl)

            let views = ["label":label, "gender":genderControl]
            let constraints = ["H:|-[label(90)]-[gender(160)]", "V:|-[label]-|", "V:|-[gender]-|"]
            cell.contentView.addLayoutConstraints(constraints, views: views)

        case Profile.Name.rawValue:
            let c: InputTableViewCell = cell as! InputTableViewCell
            c.label.text = NSLocalizedString("Name", comment: "tableview label")
            c.input.delegate = self
            c.input.text = User.name
            c.input.keyboardType = .Default
            c.input.returnKeyType = .Done
            profileTextFields[Profile.Name.rawValue] = c.input

        case Profile.Member.rawValue:
            let c: InputTableViewCell = cell as! InputTableViewCell
            c.label.text = NSLocalizedString("Member Since", comment: "tableview label")
            c.label.font = UIFont.systemFontOfSize(UIFont.systemFontSize())
            c.input.delegate = self

            let formatter = NSDateFormatter()
            formatter.dateStyle = NSDateFormatterStyle.ShortStyle
            formatter.timeStyle = NSDateFormatterStyle.ShortStyle
            c.input.text = formatter.stringFromDate(User.created)
            profileTextFields[Profile.Member.rawValue] = c.input

        default: break
        }

        case Cell.Logout.rawValue:
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

        case Cell.Delete.rawValue:
            cell.textLabel!.text = NSLocalizedString("Delete Account", comment: "tableview label")
            cell.textLabel!.textColor = UIColor.redColor()
            cell.textLabel!.textAlignment = .Center

        default: break
        }
    }

    override func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return UITableViewAutomaticDimension
    }

    override func tableView(tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return UITableViewAutomaticDimension
    }

    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        let section = hasLocation ? indexPath.section : (indexPath.section + 1)
        if section == Cell.Location.rawValue {
            let cell = LocationTableViewCell()
            cell.textLabel!.text = userLocationAddress
            cell.setNeedsLayout()
            cell.layoutIfNeeded()
            return cell.contentView.systemLayoutSizeFittingSize(UILayoutFittingCompressedSize).height + 1
        } else if section == Cell.Profile.rawValue {
            if indexPath.row == Profile.Photo.rawValue {
                return 64+40+8*2
            }
        }
        return UITableViewAutomaticDimension
    }

    override func tableView(tableView: UITableView, willSelectRowAtIndexPath indexPath: NSIndexPath) -> NSIndexPath? {
        let section = hasLocation ? indexPath.section : (indexPath.section + 1)
        if section == Cell.Logout.rawValue || section == Cell.Delete.rawValue {
            return indexPath
        }
        return nil
    }

    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        let section = hasLocation ? indexPath.section : (indexPath.section + 1)
        if section == Cell.Logout.rawValue {
            if FBSDKAccessToken.currentAccessToken() != nil {
                return
            }
            PKHUD.sharedHUD.show()
            User.signout() {
                dispatch_async(dispatch_get_main_queue()) {
                    PKHUD.sharedHUD.hide(animated: false)
                    locationTracker.stop()
                    self.navigationController!.setViewControllers([WelcomeViewController()], animated: false)
                }
            }
        } else if section == Cell.Delete.rawValue {
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

}

extension ProfileTableViewController {

    func cancel() {
        self.navigationController!.popViewControllerAnimated(true)
    }

    func update() {
        profileTextFields[Profile.Name.rawValue]!.resignFirstResponder()

        // quick check of empty name
        let name: String? = self.profileTextFields[Profile.Name.rawValue]!.text
        if name == nil || name!.isEmpty {
            UIAlertController.simpleAlert(NSLocalizedString("Name can not be empty", comment: "alert for empty name"))
            return
        }

        PKHUD.sharedHUD.show()

        // FIXME: quick custom photo upload
        if Image.getImage(photoView.name, type: photoView.type) == nil &&
           photoView.type == PhotoType.Photo.rawValue {
            Image.saveImage(photoView.image!, name: photoView.name, completion: {(status: Bool) in
                self.upload()
            })
        } else {
            upload()
        }
    }

    func upload() {
        let name: String? = self.profileTextFields[Profile.Name.rawValue]!.text
        let gender: Int? = self.genderControl.selectedSegmentIndex
        let bod: Int? = Int(self.profileTextFields[Profile.Bod.rawValue]!.text!)
        let photoType: UInt? = self.photoViewSwitch.on ? PhotoType.Facebook.rawValue : self.photoView.type
        let photoName: String? = self.photoViewSwitch.on ? User.providerID : self.photoView.name

        User.update(
            (name == User.name) ? nil : name,
            gender: (gender == User.gender) ? nil : gender,
            bod: (bod == User.bod) ? nil : bod,
            photoType: (photoType == User.photoType) ? nil : photoType,
            photoName: (photoName == User.photoName) ? nil : photoName,
            completion: {(status: Bool, error: String?) in
                dispatch_async(dispatch_get_main_queue()) {
                    PKHUD.sharedHUD.hide(animated: false)
                    if status {
                        self.cancel()
                    } else {
                        UIAlertController.simpleAlert(
                            NSLocalizedString("Failed to update profile",
                                              comment: "error for updating profile"), message: error)
                    }
                }
            })
    }

}

extension ProfileTableViewController: UITextFieldDelegate {

    func textFieldShouldBeginEditing(textField: UITextField) -> Bool {
        if textField == profileTextFields[Profile.Bod.rawValue] {
            let controller = AgePickerViewController()
            controller.target = profileTextFields[Profile.Bod.rawValue]
            controller.year = User.bod
            self.navigationController!.setNavigationBarHidden(false, animated: false)
            self.navigationController!.pushViewController(controller, animated: true)
            return false
        }
        if textField == profileTextFields[Profile.Member.rawValue] {
            return false
        }
        return true
    }

    func textFieldShouldReturn(textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return true
    }

}

extension ProfileTableViewController: FBSDKLoginButtonDelegate {

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
