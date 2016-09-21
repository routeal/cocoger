//
//  SignupTableViewController.swift
//  SokoraNow
//
//  Created by Hiroshi Watanabe on 6/30/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit
import PKHUD

class ColorTableViewController: UITableViewController {

    var colorChooser: ColorChooserTableViewCell!

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
        print("deinit: ColorTableViewController")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        tableView.registerClass(ColorChooserTableViewCell.classForCoder(), forCellReuseIdentifier: NSStringFromClass(ColorChooserTableViewCell))
    }

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        let title = NSLocalizedString("Colors", comment: "viewcontoller title")
        let cancelStr = NSLocalizedString("Cancel", comment: "batbutton title")
        let left = UIBarButtonItem(title: cancelStr, style: .Plain, target: self, action: "cancel")
        let updateStr = NSLocalizedString("Update", comment: "barbutton title")
        let right = UIBarButtonItem(title: updateStr, style: .Plain, target: self, action: "update")
        setupNavigation(title, left: left, right: right)
    }

}

// UITableViewDataSource
extension ColorTableViewController {

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch section {
        case 0:
            return 1
        default:
            return 0
        }
    }

    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        return tableView.dequeueReusableCellWithIdentifier(NSStringFromClass(ColorChooserTableViewCell), forIndexPath: indexPath)
    }

}

// UITableViewDelegate
extension ColorTableViewController {

    override func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell, forRowAtIndexPath indexPath: NSIndexPath) {
        colorChooser = cell as! ColorChooserTableViewCell
        colorChooser.myColor = User.myColor
        colorChooser.girlColor = User.girlColor
        colorChooser.boyColor = User.boyColor
        colorChooser.frameColor = User.frameColor
    }

    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return 298
    }
}


extension ColorTableViewController {

    func cancel() {
        self.navigationController!.popViewControllerAnimated(true)
    }

    func update() {
        PKHUD.sharedHUD.show()
        User.update(
            myColor: (colorChooser.myColor == User.myColor) ? nil : colorChooser.myColor,
            girlColor: (colorChooser.girlColor == User.girlColor) ? nil : colorChooser.girlColor,
            boyColor: (colorChooser.boyColor == User.boyColor) ? nil : colorChooser.boyColor,
            frameColor: (colorChooser.frameColor == User.frameColor) ? nil : colorChooser.frameColor,
            completion: { [weak self] (status: Bool, error: String?) in
                dispatch_async(dispatch_get_main_queue()) {
                    PKHUD.sharedHUD.hide(animated: false)
                    if status {
                        self!.cancel()
                        if let mapViewController = self!.mapViewController() {
                            mapViewController.refresh()
                        }
                    } else {
                        UIAlertController.simpleAlert("Color Error", message: error)
                    }
                }
            })
    }

}
