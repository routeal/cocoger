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

class UpdateFriendTableViewController: UITableViewController {

    let ProfileIdentifier:String = "Profile"
    let LocationIdentifier:String = "Location"

    enum Cell: Int {
        case Location = 0,
             Profile,
             Total
    }

    enum Profile: Int {
        case Name = 0,
             Range,
             Photo,
             Total
    }

    var friend: Friend!
    var address: String?
    var rangeView: LocationRangeView!
    var photoView: PhotoSelectView!
    var nameView: UITextField!
    var hasLocation: Bool = false
    var photoFacebookImageView: UIImageView!
    var photoViewSwitch: UISwitch!

    init(friend: Friend, hasLocation: Bool = false, address: String? = nil) {
        super.init(style:UITableViewStyle.Grouped)
        self.friend = friend
        self.hasLocation = hasLocation
        self.address = address
    }

    required init(coder: NSCoder) {
        fatalError("NSCoding not supported")
    }

    private override init(nibName nibNameOrNil: String!, bundle nibBundleOrNil: NSBundle!) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    deinit {
        print("deinit: UpdateFriendTableViewController")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        self.tableView.keyboardDismissMode = .OnDrag
        self.tableView.registerClass(InputTableViewCell.classForCoder(), forCellReuseIdentifier: ProfileIdentifier)
        self.tableView.registerClass(LocationTableViewCell.classForCoder(), forCellReuseIdentifier: LocationIdentifier)
    }

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        let cancelStr = NSLocalizedString("Cancel", comment: "barbutton title")
        let left = UIBarButtonItem(title: cancelStr, style: .Plain, target: self, action: "cancel")
        let updateStr = NSLocalizedString("Update", comment: "barbutton title")
        let right = UIBarButtonItem(title: updateStr, style: .Plain, target: self, action: "update")
        setupNavigation(friend.name, left: left, right: right)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

}

// UITableViewDataSource
extension UpdateFriendTableViewController {

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return hasLocation ? Cell.Total.rawValue : (Cell.Total.rawValue - 1)
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        let sec = hasLocation ? section : (section + 1)
        switch sec {
        case Cell.Profile.rawValue:
            return Profile.Total.rawValue
        default:
            return 1
        }
    }

    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var id: String!
        let sec = hasLocation ? indexPath.section : (indexPath.section + 1)
        if sec == Cell.Location.rawValue {
            id = LocationIdentifier
        } else if sec == Cell.Profile.rawValue {
            id = ProfileIdentifier
        }
        return tableView.dequeueReusableCellWithIdentifier(id, forIndexPath: indexPath)
    }

}

// UITableViewDelegate
extension UpdateFriendTableViewController {

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
            c.textLabel!.text = self.address

        case Cell.Profile.rawValue:
        switch indexPath.row {
        case Profile.Name.rawValue:
            let c: InputTableViewCell = cell as! InputTableViewCell
            c.label.text = NSLocalizedString("Name", comment: "tableview label")
            c.input.text = friend.name
            c.input.keyboardType = .Default
            c.input.returnKeyType = .Done
            nameView = c.input

        case Profile.Range.rawValue:
            let c: InputTableViewCell = cell as! InputTableViewCell
            c.label.hidden = true
            c.input.hidden = true

            rangeView = LocationRangeView(frame: CGRectZero)
            //rangeView.noneHidden = false
            rangeView.alertIndicator = true
            rangeView.translatesAutoresizingMaskIntoConstraints = false
            rangeView.value = Int(friend.range)
            cell.contentView.addSubview(rangeView)
            cell.contentView.addLayoutConstraints(["H:|-8-[range]-8-|", "V:|[range(48)]"],
                                                  views: ["range":rangeView])

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
            if FBSDKAccessToken.currentAccessToken() == nil || friend.providerID.isEmpty {
                onoff.hidden = true
                imageView.hidden = true
            } else {
                Image.getImage(friend.providerID, type: PhotoType.Facebook.rawValue, completion: {(image: UIImage?) in
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
            photoView.name = friend.photoName
            photoView.backgroundColor = UIColor.clearColor()
            photoView.translatesAutoresizingMaskIntoConstraints = false
            cell.contentView.addSubview(photoView)
            cell.contentView.addLayoutConstraints(["H:|-[view]-|",
                                                   "H:|-[photo]-|",
                                                   "V:|[view]-[photo(64)]"],
                                                  views: ["photo":photoView, "view":view])
            photoFacebookImageView = imageView
            setPhotoMode(friend.photoType == PhotoType.Facebook.rawValue)

        default: break
        }
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
        let sec = hasLocation ? indexPath.section : (indexPath.section + 1)
        if sec == Cell.Location.rawValue {
            let cell = LocationTableViewCell()
            cell.textLabel!.text = self.address
            cell.setNeedsLayout()
            cell.layoutIfNeeded()
            return cell.contentView.systemLayoutSizeFittingSize(UILayoutFittingCompressedSize).height + 1
        } else if sec == Cell.Profile.rawValue {
            if indexPath.row == Profile.Range.rawValue {
                return 48+8
            } else if indexPath.row == Profile.Photo.rawValue {
                return 64+40+8*2
            }
        }
        return UITableViewAutomaticDimension
    }

    override func tableView(tableView: UITableView, willSelectRowAtIndexPath indexPath: NSIndexPath) -> NSIndexPath? {
        return nil
    }

    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
    }

}

extension UpdateFriendTableViewController: UITextFieldDelegate {

    func textFieldShouldReturn(textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return true
    }

}

extension UpdateFriendTableViewController {

    func cancel() {
        self.navigationController!.popViewControllerAnimated(true)
    }

    func update() {
        nameView.resignFirstResponder()

        /*
        println("range: \(rangeView.value)")
        println("photo: \(photoView.name) \(photoView.type)")
        println("name: \(nameView.text)")
        */

        if nameView.text!.isEmpty {
            UIAlertController.simpleAlert(
                NSLocalizedString("Name can not be empty", comment: "alert for empty name"))
            return
        }

        if rangeView.value < Int(friend.range) {
            let title = NSLocalizedString("Common Location Range",
                                          comment: "alert for location range change title")
            let message = NSLocalizedString(
                    "More precise location range has to be agreed by your friend.  Click OK to send a request to your friend.",
                    comment: "alert for location range change message")
            let okLabel = NSLocalizedString("OK", comment: "action label")
            let cancelLabel = NSLocalizedString("Cancel", comment: "action label")
            UIAlertController.simpleAlert(
                title, message: message, ok: okLabel, cancel: cancelLabel,
                handler: { [weak self] (index: Int) -> Void in
                    switch index {
                    case 0:
                        self!.rangeView.value = Int(self!.friend.range)
                    default: break
                    }
                    self!.updateImpl()
                }
            )
        } else {
            updateImpl()
        }
    }

    func updateImpl() {
        PKHUD.sharedHUD.show()

        // FIXME: quick custom photo upload
        if Image.getImage(photoView.name, type: photoView.type) == nil &&
           photoView.type == PhotoType.Photo.rawValue {
            Image.saveImage(photoView.image!, name: photoView.name, completion: {[weak self](status: Bool) in
                self!.upload()
            })
        } else {
            upload()
        }
    }

    func upload() {
        let photoType: UInt? = photoViewSwitch.on ? PhotoType.Facebook.rawValue : photoView.type
        let photoName: String? = photoViewSwitch.on ? friend.providerID : photoView.name

        Friend.update(
            friend.user, name: nameView.text, range: rangeView.value,
            photoType: photoType, photoName: photoName,
            completion: { [weak self](status: Bool, error: String?) in
                dispatch_async(dispatch_get_main_queue()) {
                    PKHUD.sharedHUD.hide(animated: false)
                    if status {
                        self!.cancel()
                    } else {
                        let title = NSLocalizedString("Friend update error", comment: "alert for friend update error")
                        var msg = ""
                        if let e = error {
                            msg = NSLocalizedString(e, comment: "alert for friend update error")
                        }
                        UIAlertController.simpleAlert(title, message: msg)
                    }
        }})
    }
}
