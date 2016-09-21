//
//  SignupTableViewController.swift
//  SokoraNow
//
//  Created by Hiroshi Watanabe on 6/30/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit

class FindTableViewController: UITableViewController {

    let SearchIdentifier:String = "Search"
    let ResultsIdentifier:String = "Results"

    var search: UISearchBar!
    var searchText: String? // search result
    var indicator: UIActivityIndicatorView!
    var status: UILabel! // status from the server
    var results: [[String : AnyObject]] = [] // results from the server

    enum Cell: Int {
        case Search = 0,
             Results,
             Total
    }

    init() {
        super.init(style:UITableViewStyle.Grouped)
        self.title = NSLocalizedString("Find Friends", comment: "viewcontroller title")
    }

    required init(coder: NSCoder) {
        fatalError("NSCoding not supported")
    }

    private override init(nibName nibNameOrNil: String!, bundle nibBundleOrNil: NSBundle!) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    deinit {
        print("deinit: FindTableViewController")
    }

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        self.tableView.keyboardDismissMode = .OnDrag
        self.tableView.registerClass(UITableViewCell.classForCoder(), forCellReuseIdentifier: SearchIdentifier)
        self.tableView.registerClass(UITableViewCell.classForCoder(), forCellReuseIdentifier: ResultsIdentifier)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

}

extension FindTableViewController {

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return Cell.Total.rawValue
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch (section) {
        case Cell.Search.rawValue:
            return 1
        case Cell.Results.rawValue:
            return (results.count == 0) ? 1 : results.count
        default:
            return 0
        }
    }

    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var id: String!
        if indexPath.section == Cell.Search.rawValue {
            id = SearchIdentifier
        } else if indexPath.section == Cell.Results.rawValue {
            id = ResultsIdentifier
        }
        return tableView.dequeueReusableCellWithIdentifier(id, forIndexPath: indexPath) 
    }

    override func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell, forRowAtIndexPath indexPath: NSIndexPath) {
        switch (indexPath.section) {
        case Cell.Search.rawValue:
            search = UISearchBar()
            search.delegate = self
            search.sizeToFit()
            search.showsSearchResultsButton = true
            search.showsScopeBar = true
            search.translatesAutoresizingMaskIntoConstraints = false
            cell.contentView.addSubview(search)
            cell.contentView.addLayoutConstraints(["|[search]|"], views: ["search":search])

        case Cell.Results.rawValue:
            if results.count == 0 {
                cell.textLabel!.text = NSLocalizedString("Your friend name here", comment: "tableview label")
            } else {
                if let name = results[indexPath.row]["name"] as? String {
                    cell.textLabel!.text = name
                }
                cell.imageView!.image = Image.getImage(named: "diamond.png")!
                // cell.accessoryView = UIImageView(image: UIImage(named: "plus"))
                cell.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
            }
        default: break
        }
    }

    override func tableView(tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        if section == Cell.Search.rawValue {
            let frame: CGRect  = tableView.rectForHeaderInSection(Cell.Search.rawValue)
            let view = UIView(frame: frame)

            let name = UILabel()
            name.translatesAutoresizingMaskIntoConstraints = false
            name.text = NSLocalizedString("Search by Name & Email", comment: "tableview header")
            view.addSubview(name)

            view.addConstraint(
                NSLayoutConstraint(
                    item: name,
                    attribute: NSLayoutAttribute.CenterX,
                    relatedBy: NSLayoutRelation.Equal,
                    toItem: view,
                    attribute: NSLayoutAttribute.CenterX,
                    multiplier: 1,
                    constant:0)
            )
            view.addConstraint(
                NSLayoutConstraint(
                    item: name,
                    attribute: NSLayoutAttribute.CenterY,
                    relatedBy: NSLayoutRelation.Equal,
                    toItem: view,
                    attribute: NSLayoutAttribute.CenterY,
                    multiplier: 1,
                    constant:0)
            )

            return view
        } else if section == Cell.Results.rawValue {
            let frame: CGRect  = tableView.rectForHeaderInSection(Cell.Search.rawValue)
            let view = UIView(frame: frame)

            indicator = UIActivityIndicatorView()
            indicator.hidesWhenStopped = true
            indicator.center = CGPointMake(view.bounds.size.width/2, 0)
            indicator.activityIndicatorViewStyle = UIActivityIndicatorViewStyle.Gray
            view.addSubview(indicator)

            status = UILabel(frame: CGRectMake(12, 0, view.bounds.size.width-12, view.bounds.size.height))
            status.font = UIFont.systemFontOfSize(UIFont.smallSystemFontSize())
            status.center = CGPointMake(view.bounds.size.width/2, 0)
            status.lineBreakMode = .ByWordWrapping
            status.numberOfLines = 0
            status.textColor = UIColor.redColor()
            view.addSubview(status)

            return view
        }
        return nil
    }

    override func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if section == Cell.Search.rawValue {
            return 44
        }
        return UITableViewAutomaticDimension
    }

    override func tableView(tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return UITableViewAutomaticDimension
    }

    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return tableView.rowHeight
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
            let id = (results[row]["id"] as? String)!
            let name = (results[row]["name"] as? String)!
            let email = (results[row]["email"] as? String)!
            let controller = SendInviteTableViewController(id: id, name: name, email: email)
            self.navigationController!.pushViewController(controller, animated: true)
        }
    }

}

extension FindTableViewController: UISearchBarDelegate {

    func searchBar(searchBar: UISearchBar, textDidChange searchText: String) {
        self.searchText = searchText
    }

    func searchBarTextDidBeginEditing(searchBar: UISearchBar) {
    }

    func searchBarTextDidEndEditing(searchBar: UISearchBar) {
    }

    func searchBarCancelButtonClicked(searchBar: UISearchBar) {
        searchText = nil
    }

    func searchBarSearchButtonClicked(searchBar: UISearchBar) {
        searchBar.resignFirstResponder()
        if searchText != nil && !searchText!.isEmpty {
            search(searchText!)
        }
    }

    func search(text: String) {
        status.text = ""

        indicator.startAnimating()

        func done(status: Bool, response: [Dictionary<String, AnyObject>]?, error: String? = nil) {
            dispatch_async(dispatch_get_main_queue()) {
                self.indicator.stopAnimating()
                if status {
                    if response == nil || response!.count == 0 {
                        self.status.text = NSLocalizedString("Not found", comment: "friend search")
                    } else {
                        self.results = []
                        for obj in response! {
                            var user: [String : AnyObject] = [:]
                            user["name"] = obj["name"]
                            user["email"] = obj["email"]
                            user["id"] = obj["id"]
                            self.results.append(user)
                        }
                        self.tableView.reloadData()
                    }
                } else {
                    if let e = error {
                        // TODO: localization with the server
                        self.status.text = e
                    } else {
                        self.status.text = NSLocalizedString("Failed to search", comment: "friend search")
                    }
                }
            }
        }

        User.search(text, completion: done)
    }

}
