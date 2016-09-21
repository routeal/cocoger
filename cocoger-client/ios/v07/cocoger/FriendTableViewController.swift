//
//  SignupTableViewController.swift
//  SokoraNow
//
//  Created by Hiroshi Watanabe on 6/30/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit
import MagicalRecord

class FriendTableViewController: UITableViewController {

    init() {
        super.init(style:UITableViewStyle.Plain)
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
        print("deinit: FriendTableViewController")
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        let title = NSLocalizedString("Friends", comment: "viewcontroller title")
        let backStr = NSLocalizedString("Back", comment: "navigation bar button")
        let left = UIBarButtonItem(title: backStr, style: .Plain, target: self, action: "cancel")
        let right = UIBarButtonItem(barButtonSystemItem: .Add, target: self, action: "add")
        setupNavigation(title, left: left, right: right)

        self.tableView.registerClass(FriendTableViewCell.classForCoder(), forCellReuseIdentifier: NSStringFromClass(FriendTableViewCell))

        refreshControl = UIRefreshControl()
        refreshControl!.addTarget(self, action: "handleRefresh:", forControlEvents: .ValueChanged)
        self.tableView.addSubview(refreshControl!)
    }

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        self.tableView.reloadData()
    }

    override func setEditing(editing: Bool, animated: Bool) {
        super.setEditing(editing, animated: animated)
        tableView!.setEditing(editing, animated: animated)
    }

}

// UITableViewDataSource
extension FriendTableViewController {

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch section {
        case 0:
            return Friend.MR_numberOfEntities() as Int
        default:
            return 0
        }
    }

    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        return tableView.dequeueReusableCellWithIdentifier(NSStringFromClass(FriendTableViewCell), forIndexPath: indexPath)
    }

    override func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        if editingStyle == .Delete {
            let friends = Friend.MR_findAllSortedBy("name", ascending: true)
            if friends.count == 0 {
                return
            }
            if let cell = tableView.cellForRowAtIndexPath(indexPath) as? FriendTableViewCell {
                if let friend = friends[indexPath.row] as? Friend {
                    if cell.name.text == friend.name {
                        Friend.remove(friend, completion: { (status: Bool, error: String?) in
                            if status {
                                tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: .Left)
                            }
                        })
                    }
                }
            }
        }
    }

}

// UITableViewDelegate
extension FriendTableViewController {

    override func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell, forRowAtIndexPath indexPath: NSIndexPath) {
        let friends = Friend.MR_findAllSortedBy("name", ascending: true)
        if friends.count == 0 {
            return
        }
        if let friend = friends[indexPath.row] as? Friend {
            let c: FriendTableViewCell  = cell as! FriendTableViewCell
            Image.getImage(friend.photoName, type: UInt(friend.photoType), completion: { (image: UIImage?) in
                if image == nil {
                    c.nimage.image = Image.getImage(named: "person.png")!.resizeToWidth(24)
                } else {
                    c.nimage.image = image!.resizeToWidth(24)
                }
            })
            c.name.text = friend.name
            c.range.value = Int(friend.range)
        }
    }

    override func tableView(tableView: UITableView, editingStyleForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCellEditingStyle {
        return .Delete
    }

    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        // has to be 44 since some device has way narrower height than the usual height
        return 44
        //return UITableViewAutomaticDimension
    }

    override func tableView(tableView: UITableView, willSelectRowAtIndexPath indexPath: NSIndexPath) -> NSIndexPath? {
        return indexPath
    }

    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        let row = indexPath.row
        let friends = Friend.MR_findAllSortedBy("name", ascending: true) as! [Friend]
        detailView(friends[row])
    }

}

extension FriendTableViewController {

    func detailView(friend: Friend) {
        let controller = UpdateFriendTableViewController(friend: friend)
        self.navigationController!.pushViewController(controller, animated: true)
    }

    func handleRefresh(paramSender: AnyObject) {
        Friend.reload({ [weak self] (status: Bool, error: String?) in
            if status {
                self!.tableView.reloadData()
            }
            self!.refreshControl!.endRefreshing()
        })
    }

    func add() {
        let controller = InviteTableViewController()
        self.navigationController!.pushViewController(controller, animated: true)
    }

    func cancel() {
        self.navigationController!.popViewControllerAnimated(true)
    }

}
