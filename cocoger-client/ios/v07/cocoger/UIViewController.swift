//
//  ViewController.swift
//  sokora
//
//  Created by Hiroshi Watanabe on 9/25/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit
import SlideMenuControllerSwift

/**
 * FIXME: this is an evil short cut which confuses things.  Need to be removed
 */

extension UIViewController {

    func show() {
        present(animated: true, completion: nil)
    }

    func present(animated animated: Bool, completion: (() -> Void)?) {
        if let rootVC = UIApplication.sharedApplication().keyWindow?.rootViewController {
            presentFromController(rootVC, animated: animated, completion: completion)
        }
    }

    private func presentFromController(controller: UIViewController, animated: Bool,
                                       completion: (() -> Void)?) {
        if  let navVC = controller as? UINavigationController,
            let visibleVC = navVC.visibleViewController {
                presentFromController(visibleVC, animated: animated, completion: completion)
        } else
        if  let tabVC = controller as? UITabBarController,
            let selectedVC = tabVC.selectedViewController {
                presentFromController(selectedVC, animated: animated, completion: completion)
        } else {
            controller.presentViewController(self, animated: animated, completion: completion)
        }
    }

    class func viewController() ->  UIViewController? {
        if let controller = UIApplication.sharedApplication().keyWindow?.rootViewController {
            if  let navVC = controller as? UINavigationController,
                let visibleVC = navVC.visibleViewController {
                return visibleVC
            } else if  let tabVC = controller as? UITabBarController,
                       let selectedVC = tabVC.selectedViewController {
                return selectedVC
            } else {
                return controller
            }
        }
        return nil
    }

}

extension UIViewController {

    func setupNavigation(title: String, left: UIBarButtonItem, right: UIBarButtonItem) {
        self.title = title
        if let nav = self.navigationController {
            nav.setNavigationBarHidden(false, animated: false)
            self.navigationItem.leftBarButtonItem = left
            self.navigationItem.rightBarButtonItem = right
        }
    }

}


extension UIViewController {

    func mapViewController() -> MapViewController? {
        if let slideMenuController = self.slideMenuController() {
            if let navigationController = slideMenuController.mainViewController as? UINavigationController {
                if let mapViewController = navigationController.viewControllers[0] as? MapViewController {
                    return mapViewController
                }
            }

        }
        return nil
    }

    class func mapViewController2() -> MapViewController? {
        if let nav = UIApplication.sharedApplication().keyWindow?.rootViewController as? UINavigationController {
            if let sm = nav.viewControllers[0] as? SlideMenuController {
                if let navigationController = sm.mainViewController as? UINavigationController {
                    if let mapViewController = navigationController.viewControllers[0] as? MapViewController {
                        return mapViewController
                    }
                }
            }
        }
        return nil
    }

}
