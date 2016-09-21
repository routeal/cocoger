//
//  AppDelegate.swift
//  WelcomeView
//
//  Created by Hiroshi Watanabe on 11/11/15.
//  Copyright © 2015 routeal. All rights reserved.
//

import UIKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?
    var navigationController: UINavigationController?

    func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject: AnyObject]?) -> Bool {
        // Override point for customization after application launch.

        // creates the navigation controller
        navigationController = UINavigationController()
        navigationController!.pushViewController(WelcomeViewController(), animated: false)

        // programatically creates the UIWindow
        window = UIWindow(frame: UIScreen.mainScreen().bounds)
        window!.backgroundColor = UIColor.whiteColor()
        window!.rootViewController = navigationController
        window!.makeKeyAndVisible()

        return true
    }

    func applicationWillResignActive(application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
    }

    func applicationDidEnterBackground(application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    }

    func applicationWillEnterForeground(application: UIApplication) {
        // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
    }

    func applicationDidBecomeActive(application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    }

    func applicationWillTerminate(application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    }


}

extension String {

    var hexColor: UIColor {
        var cString:String = self.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet()).uppercaseString

        if (cString.hasPrefix("#")) {
            cString = (cString as NSString).substringFromIndex(1)
        }

        if (cString.characters.count != 6) {
            return UIColor.clearColor()
        }

        let rString = (cString as NSString).substringToIndex(2)
        let gString = ((cString as NSString).substringFromIndex(2) as NSString).substringToIndex(2)
        let bString = ((cString as NSString).substringFromIndex(4) as NSString).substringToIndex(2)

        var r:CUnsignedInt = 0, g:CUnsignedInt = 0, b:CUnsignedInt = 0;
                                                    NSScanner(string: rString).scanHexInt(&r)
                                                    NSScanner(string: gString).scanHexInt(&g)
                                                    NSScanner(string: bString).scanHexInt(&b)


                                                    return UIColor(red: CGFloat(r) / 255.0, green: CGFloat(g) / 255.0, blue: CGFloat(b) / 255.0, alpha: CGFloat(1))
    }

}
