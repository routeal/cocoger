//
//  SignupTableViewController.swift
//  SokoraNow
//
//  Created by Hiroshi Watanabe on 6/30/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit
import PKHUD
import MessageUI

protocol LicenseTableViewControllerDelegate: class {
    func done() -> Void
}

class LicenseTableViewController: UITableViewController {

    let ContinueIdentifier:String = "Continue"

    enum Cell: Int {
        case Continue = 0,
             Total
    }

    var continueLabel: UILabel!
    var termbutton: UIButton!
    var privbutton: UIButton!
    var isTermViewed: Bool = false
    var isPrivacyViewed: Bool = false

    weak var delegate: LicenseTableViewControllerDelegate?

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
        print("deinit: SettingsTableViewController")
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        self.tableView.registerClass(UITableViewCell.classForCoder(), forCellReuseIdentifier: ContinueIdentifier)
    }

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        self.title = NSLocalizedString("License Agreement", comment: "viewcontroller title")
        self.navigationController!.setNavigationBarHidden(false, animated: false)
        self.navigationController?.setToolbarHidden(true, animated: false)
        if isTermViewed {
            termbutton.setImage(Image.getImage(named:"checkbox.png"), forState: .Normal)
        }
        if isPrivacyViewed {
            privbutton.setImage(Image.getImage(named:"checkbox.png"), forState: .Normal)
        }
        if isTermViewed && isPrivacyViewed {
            continueLabel.textColor = view.tintColor
        }
    }

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return Cell.Total.rawValue
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch (section) {
        case Cell.Continue.rawValue:
            return 1
        default:
            return 0
        }
    }

    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var id: String!
        if indexPath.section == Cell.Continue.rawValue {
            id = ContinueIdentifier
        }
        return tableView.dequeueReusableCellWithIdentifier(id, forIndexPath: indexPath) 
    }

    override func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell, forRowAtIndexPath indexPath: NSIndexPath) {
        switch (indexPath.section) {
        case Cell.Continue.rawValue:
            cell.textLabel!.text = NSLocalizedString("Continue", comment: "tableview label")
            cell.textLabel!.textColor = UIColor.grayColor()
            cell.textLabel!.textAlignment = .Center
            continueLabel = cell.textLabel!
        default: break
        }
    }

    override func tableView(tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let scale: CGFloat = UIScreen.mainScreen().scale
        let m = 8 * scale * 2

        let view = UIView()

        let label = UILabel()
        label.translatesAutoresizingMaskIntoConstraints = false
        label.font = UIFont.boldSystemFontOfSize(20)
        /*
        let underlineString = NSAttributedString(
                string: NSLocalizedString("Welcome to Cocoger", comment: "license agreement title"),
                attributes: [NSUnderlineStyleAttributeName: NSUnderlineStyle.StyleSingle.rawValue])
        */
        //label.attributedText = underlineString
        label.text = NSLocalizedString("Welcome to Cocoger", comment: "license agreement title")
        label.textAlignment = .Center
        //label.backgroundColor = UIColor.cyanColor()
        view.addSubview(label)

        let content = UILabel()
        content.font = UIFont.systemFontOfSize(UIFont.labelFontSize())
        content.translatesAutoresizingMaskIntoConstraints = false
        content.lineBreakMode = .ByWordWrapping
        content.numberOfLines = 0
        //content.backgroundColor = UIColor.redColor()
        content.text = NSLocalizedString(
                "Cocoger lets you share location with your friends and family members, allowing to keeping privacy and comfort.  Please accept the Term of Use and the Privacy Policy, and press 'Continue' for sign up.",
                comment: "license agreement message")
        view.addSubview(content)

        let sub1 = UIView()
        sub1.translatesAutoresizingMaskIntoConstraints = false
        let term = UILabel()
        let gestureRecognizer = UITapGestureRecognizer(target: self, action: Selector("termPressed"))
        term.addGestureRecognizer(gestureRecognizer)
        term.userInteractionEnabled = true
        term.translatesAutoresizingMaskIntoConstraints = false
        term.font = UIFont.boldSystemFontOfSize(18)
        let underlineString2 = NSAttributedString(
                string: NSLocalizedString("Term of Use", comment: "license term of use"),
                attributes: [NSUnderlineStyleAttributeName: NSUnderlineStyle.StyleSingle.rawValue])
        term.attributedText = underlineString2
        term.textAlignment = .Left
        term.textColor = tableView.tintColor
        sub1.addSubview(term)
        let image = Image.getImage(named: "uncheckbox.png")
        termbutton = UIButton(type: .System)
        //termbutton.setBackgroundImage(image, forState: UIControlState.Normal)
        termbutton.setImage(image, forState: .Normal)
        termbutton.translatesAutoresizingMaskIntoConstraints = false
        sub1.addSubview(termbutton)
        sub1.addLayoutConstraints(["V:|-[tbutton]-|", "V:|-[term]-|",
                                   "H:|-[tbutton]-[term]"],
                                  views: ["term": term, "tbutton":termbutton])

        view.addSubview(sub1)

        let sub2 = UIView()
        sub2.translatesAutoresizingMaskIntoConstraints = false
        let privacy = UILabel()
        let gestureRecognizer2 = UITapGestureRecognizer(target: self, action: Selector("privacyPressed"))
        privacy.addGestureRecognizer(gestureRecognizer2)
        privacy.userInteractionEnabled = true
        privacy.translatesAutoresizingMaskIntoConstraints = false
        privacy.font = UIFont.boldSystemFontOfSize(18)
        let underlineString3 = NSAttributedString(
                string: NSLocalizedString("Privacy Policy", comment: "privacy policy"),
                attributes: [NSUnderlineStyleAttributeName: NSUnderlineStyle.StyleSingle.rawValue])
        privacy.attributedText = underlineString3
        privacy.textAlignment = .Left
        privacy.textColor = tableView.tintColor
        sub2.addSubview(privacy)
        privbutton = UIButton(type: .System)
        privbutton.setImage(image, forState: .Normal)
        privbutton.translatesAutoresizingMaskIntoConstraints = false
        sub2.addSubview(privbutton)
        sub2.addLayoutConstraints(["V:|-[pbutton]-|", "V:|-[privacy]-|",
                                   "H:|-[pbutton]-[privacy]"],
                                  views: ["privacy": privacy, "pbutton":privbutton])
        view.addSubview(sub2)

        view.addLayoutConstraints(["V:|-\(m)-[label]-[content(>=50)]-[sub1]-[sub2]-\(m)-|",
                                   "H:|-\(m)-[label]-\(m)-|",
                                   "H:|-\(m)-[content]-\(m)-|",
                                   "H:|-\(m)-[sub1]-\(m)-|",
                                   "H:|-\(m)-[sub2]-\(m)-|"],
                                  views: ["label": label, "content": content, "sub1": sub1, "sub2":sub2])

        return view
    }

    override func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 350
    }

    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return UITableViewAutomaticDimension
    }

    override func tableView(tableView: UITableView, willSelectRowAtIndexPath indexPath: NSIndexPath) -> NSIndexPath? {
        if isTermViewed && isPrivacyViewed {
            return indexPath
        }
        return nil
    }

    func termPressed() {
        let controller = WebViewController()
        controller.title = NSLocalizedString("Term of Use", comment: "viewcontoller title")
        controller.url = "\(Router.WebURLString)/m/terms.html?lang=\(UIDevice.lang)"
        self.navigationController!.pushViewController(controller, animated: true)
        isTermViewed = true
    }

    func privacyPressed() {
        let controller = WebViewController()
        controller.title = NSLocalizedString("Privacy Policy", comment: "viewcontoller title")
        controller.url = "\(Router.WebURLString)/m/privacy.html?lang=\(UIDevice.lang)"
        self.navigationController!.pushViewController(controller, animated: true)
        isPrivacyViewed = true
    }

    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        if indexPath.section == Cell.Continue.rawValue {
            self.navigationController!.popViewControllerAnimated(true)
            if delegate != nil {
                delegate!.done()
            }
        }
    }

}
