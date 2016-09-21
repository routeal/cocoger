//
//  CollectionViewController.swift
//  WelcomeView
//
//  Created by Hiroshi Watanabe on 11/11/15.
//  Copyright © 2015 routeal. All rights reserved.
//

import UIKit

class CustomUICollectionViewCell : UICollectionViewCell {
    var textLabel : UILabel?

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        textLabel = UILabel(frame: CGRectMake(0, 0, frame.width, frame.height))
        textLabel?.text = nil
        textLabel?.textAlignment = NSTextAlignment.Center
        self.contentView.addSubview(textLabel!)
    }
}

class WelcomeViewController: UIViewController {

    struct Intro {
        let title: String
        let color: String
        let url: String
    }

    static let intros: [Intro] = [
               Intro(
                   title: NSLocalizedString("What", comment: "welcome"),
                   color: "FFCC33",
                   url: "what.html"
                   ),
               /*
               Intro(
                   title: NSLocalizedString("How", comment: "welcome"),
                   color: "c9eee0",
                   url: "how.html"
                   ),
               */
               Intro(
                   title: NSLocalizedString("Why", comment: "welcome"),
                   color: "999966",
                   url: "why.html"
                   ),
               Intro(
                   title: NSLocalizedString("Use Case", comment: "welcome"),
                   color: "FF9933",
                   url: "case.html"
                   ),
               Intro(
                   title: NSLocalizedString("Use Case", comment: "welcome"),
                   color: "0099CC",
                   url: "case2.html"
                   ),
               Intro(
                   title: NSLocalizedString("Note", comment: "welcome"),
                   color: "CC6666",
                   url: "note.html"
                   ),
               Intro(
                   title: NSLocalizedString("FAQ", comment: "welcome"),
                   color: "99CC33",
                   url: "faq.html"
                   ),
               Intro(
                   title: NSLocalizedString("Privacy Policy", comment: "welcome"),
                   color: "33CCCC",
                   url: "privacy.html"
                   ),
               Intro(
                   title: NSLocalizedString("Term of Use", comment: "welcome"),
                   color: "EEDFCC",
                   url: "terms.html"
                   ),
           ]

    deinit {
        print("deinit: WelcomeViewController")
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.title = NSLocalizedString("Welcome to Cocoger", comment: "welcome")

        let signin = UIBarButtonItem(title: NSLocalizedString("Sign In", comment: "welcome"), style: .Plain, target: self, action: "signin:")
        let signup = UIBarButtonItem(title: NSLocalizedString("Sign Up", comment: "welcome"), style: .Plain, target: self, action: "signup:")
        let space = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.FlexibleSpace, target: nil, action: nil)
        let items = [space, signin, space, signup, space]
        self.setToolbarItems(items as [UIBarButtonItem], animated: false)

        let flowLayout = UICollectionViewFlowLayout()
        flowLayout.scrollDirection = .Vertical
        flowLayout.minimumLineSpacing = 16.0
        flowLayout.sectionInset = UIEdgeInsetsMake(0, 16, 0, 16)
        flowLayout.headerReferenceSize = CGSizeMake(32, 32)

        let collectionView = UICollectionView(frame: view.frame, collectionViewLayout: flowLayout)
        collectionView.registerClass(CustomUICollectionViewCell.self, forCellWithReuseIdentifier: "cell")
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.backgroundColor = UIColor.clearColor()
        view.addSubview(collectionView)

        var viewConstraints = [NSLayoutConstraint]()
        collectionView.translatesAutoresizingMaskIntoConstraints = false
        viewConstraints.append(NSLayoutConstraint(item: collectionView, attribute: .Top, relatedBy: .Equal, toItem: view, attribute: .Top, multiplier: 1.0, constant: 0.0))
        viewConstraints.append(NSLayoutConstraint(item: collectionView, attribute: .Bottom, relatedBy: .Equal, toItem: view, attribute: .Bottom, multiplier: 1.0, constant: 0.0))
        viewConstraints.append(NSLayoutConstraint(item: collectionView, attribute: .Leading, relatedBy: .Equal, toItem: view, attribute: .Leading, multiplier: 1.0, constant: 0.0))
        viewConstraints.append(NSLayoutConstraint(item: collectionView, attribute: .Trailing, relatedBy: .Equal, toItem: view, attribute: .Trailing, multiplier: 1.0, constant: 0.0))
        view.addConstraints(viewConstraints)
    }

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        // disable the navigation bar
        self.navigationController?.setNavigationBarHidden(false, animated: false)
        self.navigationController?.setToolbarHidden(false, animated: false)
    }

}

extension WelcomeViewController: UICollectionViewDataSource {

    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return WelcomeViewController.intros.count
    }

    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let cell: CustomUICollectionViewCell = collectionView.dequeueReusableCellWithReuseIdentifier("cell", forIndexPath: indexPath) as! CustomUICollectionViewCell
        cell.layer.masksToBounds = true
        cell.layer.cornerRadius = 8
        cell.backgroundColor = WelcomeViewController.intros[indexPath.row].color.hexColor
        cell.textLabel?.numberOfLines = 0
        cell.textLabel?.text = WelcomeViewController.intros[indexPath.row].title
        return cell
    }

    func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        let url = Router.WebURLString + "/m/" + WelcomeViewController.intros[indexPath.row].url + "?lang=\(UIDevice.lang)"

        let controller = WebViewController()
        controller.title = WelcomeViewController.intros[indexPath.row].title
        controller.url = url
        self.navigationController?.pushViewController(controller, animated: true)
    }

}

extension WelcomeViewController: UICollectionViewDelegateFlowLayout {

    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout,
                        sizeForItemAtIndexPath indexPath: NSIndexPath) -> CGSize {
        let screen = UIScreen.mainScreen().bounds
        let screenWidth = screen.size.width
        let size = (screenWidth-16*4)/3
        return CGSizeMake(size, size)
    }

}

extension WelcomeViewController {

    func signin(sender: UIBarButtonItem) {
        self.navigationController!.pushViewController(SignInTableViewController(), animated: true)
    }

    func signup(sender: UIBarButtonItem) {
        let sign = SignInTableViewController()
        let license = LicenseTableViewController()
        license.delegate = sign
        self.navigationController!.pushViewController(sign, animated: false)
        self.navigationController!.pushViewController(license, animated: true)
    }

}
