//
//  CollectionViewController.swift
//  WelcomeView
//
//  Created by Hiroshi Watanabe on 11/11/15.
//  Copyright © 2015 routeal. All rights reserved.
//

import UIKit

class CustomUICollectionViewCell : UICollectionViewCell{
    var textLabel : UILabel?

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        textLabel = UILabel(frame: CGRectMake(0, 0, frame.width, frame.height))
        textLabel?.text = "nil"
        textLabel?.textAlignment = NSTextAlignment.Center
        self.contentView.addSubview(textLabel!)
    }
}

class WelcomeViewController: UIViewController, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {

    let welcomeTransitionDelegate = WelcomeTransitionDelegate()

    struct Intro {
        let title: String
        let color: String
    }

    static let intros: [Intro] = [
               Intro(
                   title: "What",
                   color: "99CC33"
                   ),
               Intro(
                   title: "When",
                   color: "FF9933"
                   ),
               Intro(
                   title: "Where",
                   color: "CCCCCC"
                   ),
               Intro(
                   title: "Which",
                   color: "CC6666"
                   ),
               Intro(
                   title: "who",
                   color: "0099CC"
                   ),
               Intro(
                   title: "How",
                   color: "999966"
                   ),
               Intro(
                   title: "About",
                   color: "33CCCC"
                   ),
               Intro(
                   title: "FAQ",
                   color: "FFCC33"
                   ),
           ]

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.title = "Welcome Cocoger"

        self.navigationController!.navigationBar.barTintColor = "208695".hexColor
        self.navigationController!.navigationBar.tintColor = "208695".hexColor
        self.navigationController!.navigationBar.translucent = false
        self.navigationController!.navigationBar.titleTextAttributes = [
            NSForegroundColorAttributeName : UIColor.whiteColor(),
        ]

        let signin = UIBarButtonItem(title: "Sign In", style: .Plain, target: self, action: "signin:")
        let signup = UIBarButtonItem(title: "Sign Up", style: .Plain, target: self, action: "signup:")
        let space = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.FlexibleSpace, target: nil, action: nil)
        let items = [space, signin, space, signup, space]
        self.setToolbarItems(items as [UIBarButtonItem], animated: false)
        self.navigationController?.setToolbarHidden(false, animated: true)

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

    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return WelcomeViewController.intros.count
    }

    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let cell: CustomUICollectionViewCell = collectionView.dequeueReusableCellWithReuseIdentifier("cell", forIndexPath: indexPath) as! CustomUICollectionViewCell
        cell.layer.masksToBounds = true
        cell.layer.cornerRadius = 8
        cell.backgroundColor = WelcomeViewController.intros[indexPath.row].color.hexColor
        cell.textLabel?.text = WelcomeViewController.intros[indexPath.row].title
        return cell
    }

    func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        let cell: CustomUICollectionViewCell = collectionView.dequeueReusableCellWithReuseIdentifier("cell", forIndexPath: indexPath) as! CustomUICollectionViewCell
        let controller = DetailViewController()
        controller.view.backgroundColor = WelcomeViewController.intros[indexPath.row].color.hexColor
        controller.transitioningDelegate = welcomeTransitionDelegate
        controller.from = CGRectInset(cell.frame, 20, 20)
        presentViewController(controller, animated: true)  {
        }
    }

    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout,
                        sizeForItemAtIndexPath indexPath: NSIndexPath) -> CGSize {
        let screen = UIScreen.mainScreen().bounds
        let screenWidth = screen.size.width
        let size = (screenWidth-16*4)/3
        return CGSizeMake(size, size)
    }

    func signin(sender: UIBarButtonItem) {
    }

    func signup(sender: UIBarButtonItem) {
    }

}
