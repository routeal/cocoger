//
//  SignupTableViewController.swift
//  SokoraNow
//
//  Created by Hiroshi Watanabe on 6/30/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit
import FBSDKCoreKit
import PKHUD

class ProviderInviteTableViewController: UITableViewController {

    let ResultsIdentifier:String = "Results"

    var friendCount = 0

    var results: [[String : AnyObject]] = [] // results from the server

    enum Cell: Int {
        case Results = 0,
             Total
    }

    init() {
        super.init(style:UITableViewStyle.Grouped)
        self.title = NSLocalizedString("Facebook Friends", comment: "viewcontroller title")
    }

    required init(coder: NSCoder) {
        fatalError("NSCoding not supported")
    }

    private override init(nibName nibNameOrNil: String!, bundle nibBundleOrNil: NSBundle!) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    deinit {
        print("deinit: ProviderInviteTableViewController")
    }

    override func loadView() {
    }

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        super.loadView()
        self.tableView.keyboardDismissMode = .OnDrag
        self.tableView.registerClass(UITableViewCell.classForCoder(), forCellReuseIdentifier:ResultsIdentifier)

        retrieveFacebookFriends()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return Cell.Total.rawValue
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch (section) {
        case Cell.Results.rawValue:
            return (results.count == 0) ? 1 : results.count
        default:
            return 0
        }
    }

    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var id: String!
        if indexPath.section == Cell.Results.rawValue {
            id = ResultsIdentifier
        }
        return tableView.dequeueReusableCellWithIdentifier(id, forIndexPath: indexPath)
    }

    override func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell,
                            forRowAtIndexPath indexPath: NSIndexPath) {
        switch (indexPath.section) {
        case Cell.Results.rawValue:
            if results.count == 0 {
                cell.textLabel!.text = NSLocalizedString("No friend found", comment: "tableview label")
            } else {
                if let name = results[indexPath.row]["name"] as? String {
                    cell.textLabel!.text = name
                }

                let img = Image.getImage(named: "diamond.png")!.resizeToWidth(24)

                if let provider = results[indexPath.row]["provider"] as? String {
                    let urlstring = "https://graph.facebook.com/\(provider)/picture?type=square"
                    loadImageAsync(urlstring, imageView: cell.imageView!, placeholder: img)
                } else {
                    cell.imageView!.image = img
                }

                cell.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
            }
        default: break
        }
    }

    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return UITableViewAutomaticDimension
    }

    override func tableView(tableView: UITableView, willSelectRowAtIndexPath indexPath: NSIndexPath) -> NSIndexPath? {
        if results.count > 0 {
            return indexPath
        }
        return nil
    }

    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        if indexPath.section == Cell.Results.rawValue {
            if results.count <= 0 {
                return
            }
            let row = indexPath.row
            if let id = results[row]["id"] as? String,
                let name = results[row]["name"] as? String,
                let email = results[row]["email"] as? String {
                    let controller = SendInviteTableViewController(id: id, name: name, email: email)
                    self.navigationController!.pushViewController(controller, animated: true)
            }
        }
    }

    func retrieveFacebookFriends() {
        PKHUD.sharedHUD.show()
        let params = ["fields": "id, name"]
        let request = FBSDKGraphRequest(graphPath:"/me/friends", parameters: params)
        request.startWithCompletionHandler {
            (connection : FBSDKGraphRequestConnection!, result : AnyObject!, error : NSError!) -> Void in
            if error == nil && result != nil {
                if let friends = result["data"] as? [NSDictionary] {
                    self.friendCount = 0
                    for friend in friends {
                        let id = friend["id"] as! String
                        let name = friend["name"] as! String
                        User.provider(id, completion: {[weak self](status: Bool, user: [String : AnyObject]?, error: String?) in
                            if let u = user {
                                if let _ = Friend.MR_findFirstByAttribute("user", withValue: u["id"]) {
                                    print("already being friend")
                                } else {
                                    var data = u
                                    data["name"] = name
                                    data["provider"] = id
                                    self!.results.append(data)
                                }
                            }
                            if ++self!.friendCount == friends.count {
                                dispatch_async(dispatch_get_main_queue()) {
                                    self!.tableView.reloadData()
                                    PKHUD.sharedHUD.hide(animated: false)
                                }
                            }
                        })
                    }
                }
            } else {
                dispatch_async(dispatch_get_main_queue()) {
                    PKHUD.sharedHUD.hide(animated: false)
                }
                print("Error in getting FB friends: \(error)")
            }
        }
    }

    func loadImageAsync(stringURL: String, imageView: UIImageView, placeholder: UIImage! = nil) {
        imageView.image = placeholder

        let url = NSURL(string: stringURL)
        let requestedURL = NSURLRequest(URL: url!)

        NSURLConnection.sendAsynchronousRequest(requestedURL, queue: NSOperationQueue.mainQueue()) {
            response, data, error in
            if data != nil {
                imageView.image = UIImage(data: data!)!.resizeToWidth(24)
            }
        }
    }

}
