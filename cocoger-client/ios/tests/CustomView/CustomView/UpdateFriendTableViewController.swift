//
//  SignupTableViewController.swift
//  SokoraNow
//
//  Created by Hiroshi Watanabe on 6/30/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit
//import PKHUD

class UpdateFriendTableViewController: UITableViewController, UITextFieldDelegate {

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

    var address: String?
    var rangeView: LocationRangeView!
    var photoView: PhotoSelectView!
    var nameView: UITextField!
    var hasLocation: Bool = false

    init() {
        super.init(style:UITableViewStyle.Grouped)
        self.hasLocation = true
//        self.address = "1350 Flora Ave, San Jose, Ca, 95130, USA, 1350 Flora Ave, San Jose, Ca, 95130, USA,"
        self.address = "1350 Flora Ave, San Jose, Ca, 95130"
        //self.address = "1350 Flora Ave"
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

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.title = "nabe"

        self.navigationController!.setNavigationBarHidden(false, animated: false)
        let cancelStr = NSLocalizedString("Cancel", comment: "barbutton title")
        self.navigationItem.leftBarButtonItem =
            UIBarButtonItem(title: cancelStr, style: .Plain, target: self, action: "cancel")
        let updateStr = NSLocalizedString("Update", comment: "barbutton title")
        self.navigationItem.rightBarButtonItem =
            UIBarButtonItem(title: updateStr, style: .Plain, target: self, action: "checkUpdate")

        self.tableView.keyboardDismissMode = .OnDrag
        self.tableView.registerClass(InputTableViewCell.classForCoder(),
                                     forCellReuseIdentifier: ProfileIdentifier)
        self.tableView.registerClass(LocationTableViewCell.classForCoder(),
                                     forCellReuseIdentifier: LocationIdentifier)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

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

    override func tableView(tableView: UITableView,
                            cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var id: String!
        let sec = hasLocation ? indexPath.section : (indexPath.section + 1)
        if sec == Cell.Location.rawValue {
            id = LocationIdentifier
        } else if sec == Cell.Profile.rawValue {
            id = ProfileIdentifier
        }
        return tableView.dequeueReusableCellWithIdentifier(id, forIndexPath: indexPath) 
    }

    override func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell,
                            forRowAtIndexPath indexPath: NSIndexPath) {
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
            c.input.text = "nabe"
            c.input.keyboardType = .Default
            c.input.returnKeyType = .Done
            nameView = c.input

        case Profile.Range.rawValue:
            let c: InputTableViewCell = cell as! InputTableViewCell
            c.label.hidden = true
            c.input.hidden = true

            rangeView = LocationRangeView(frame: CGRectZero)
            rangeView.noneHidden = false
            rangeView.alertIndicator = true
            rangeView.translatesAutoresizingMaskIntoConstraints = false
            rangeView.value = 3
            cell.contentView.addSubview(rangeView)
            cell.contentView.addLayoutConstraints(["H:|-4-[range]|", "V:|[range(48)]"],
                                                  views: ["range":rangeView])

        case Profile.Photo.rawValue:
            let c: InputTableViewCell = cell as! InputTableViewCell
            c.label.hidden = true
            c.input.hidden = true

            photoView = PhotoSelectView(frame: CGRectZero)
            photoView.backgroundColor = UIColor.clearColor()
            photoView.translatesAutoresizingMaskIntoConstraints = false
            photoView.name = "uncle"
            cell.contentView.addSubview(photoView)
            cell.contentView.addLayoutConstraints(["H:|-[photo]-|", "V:|-[photo(64)]"],
                                                  views: ["photo":photoView])

        default: break
        }
        default: break
        }
    }

    func textFieldShouldBeginEditing(textField: UITextField) -> Bool {
        return true
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
                return 64+8
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

    func cancel() {
        if let nav = UIApplication.sharedApplication().keyWindow!.rootViewController as? UINavigationController {
            if nav == self.navigationController {
                self.navigationController!.popViewControllerAnimated(false)
            } else {
                self.navigationController!.popViewControllerAnimated(false)
                self.navigationController!.presentingViewController!.dismissViewControllerAnimated(true, completion: nil)
            }
        }
    }

    func checkUpdate() {
        nameView.resignFirstResponder()

        /*
        println("range: \(rangeView.value)")
        println("photo: \(photoView.name) \(photoView.type)")
        println("name: \(nameView.text)")
        */

        if rangeView.value < Int(3) {
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
                        self!.rangeView.value = Int(3)
                    default: break
                    }
                    self!.update()
                }
            )
        } else {
            update()
        }
    }

    func update() {
//        PKHUD.sharedHUD.show()

//        println(friend.user)

        /*
        if Image.getImage(photoView.name) == nil && photoView.type == PhotoType.DB.rawValue {
            Image.saveImage(photoView.image!, name: photoView.name, completion: {(status: Bool) in
                self.upload()
            })
        } else {
            upload()
        }
*/
    }

    /*
    func upload() {
            Friend.update(
                friend.user, name: nameView.text, range: rangeView.value,
                photoType: photoView.type, photoName: photoView.name,
                completion: { [weak self](status: Bool, error: String?) in
                    dispatch_async(dispatch_get_main_queue()) {
                    PKHUD.sharedHUD.hide(animated: false)
                    if status {
                        self!.cancel()
                    } else {
                        UIAlertController.simpleAlert(
                            NSLocalizedString("Error happened during friend update",
                                              comment: "alert for friend update error"),
                            message: error)
                    }
                }})
    }
*/
}
