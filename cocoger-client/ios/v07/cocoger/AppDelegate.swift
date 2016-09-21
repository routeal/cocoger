//
//  AppDelegate.swift
//  sokora
//
//  Created by Hiroshi Watanabe on 8/27/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit
import MagicalRecord
import PKHUD
import FBSDKCoreKit
import CoreLocation
#if false
import GoogleMaps
#endif
import SlideMenuControllerSwift

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    private var notificationHandler: NotificationHandler!

    private let db: String = "cocoger.sqlite"

    func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject: AnyObject]?) -> Bool {

        // initializes the Magical RecordtableView.backgroundColor
        MagicalRecord.setupCoreDataStackWithAutoMigratingSqliteStoreNamed(db)

        notificationHandler = NotificationHandler()

#if false
        GMSServices.provideAPIKey("AIzaSyBpST1QmE5au8QJwWenxkiaMvJedymoIV4")

        // enables the background fetch
        UIApplication.sharedApplication().setMinimumBackgroundFetchInterval(
            UIApplicationBackgroundFetchIntervalMinimum)
#endif

        // programatically creates the UIWindow
        window = UIWindow(frame: UIScreen.mainScreen().bounds)
        window!.backgroundColor = UIColor.whiteColor()

        let navigationController = UINavigationController(custom: true)

        if User.available {
            // creates the navigation controller
            let mainViewController = UINavigationController(custom: true, rootViewController: MapViewController())
            let leftMenuViewController = MenuViewController()
            let slideMenuController = SlideMenuController(mainViewController: mainViewController,
                                                          leftMenuViewController: leftMenuViewController)
            slideMenuController.automaticallyAdjustsScrollViewInsets = true
            navigationController.pushViewController(slideMenuController, animated: false)
        } else {
            // creates the navigation controller
            navigationController.pushViewController(WelcomeViewController(), animated: false)
        }

        window!.rootViewController = navigationController
        window!.makeKeyAndVisible()

        // initializes PKHUD
        PKHUD.sharedHUD.contentView = PKHUDSystemActivityIndicatorView()

        if let options = launchOptions {
            if User.available {
                // The presence of this key indicates that the app was
                // launched in response to an incoming location
                // event. The value of this key is an NSNumber object
                // containing a Boolean value. You should use the
                // presence of this key as a signal to create a
                // CLLocationManager object and start location
                // services again. Location data is delivered only to
                // the location manager delegate and not using this
                // key.

                if options[UIApplicationLaunchOptionsLocationKey] != nil {
                    if CLLocationManager.locationServicesEnabled() &&
                       CLLocationManager.authorizationStatus() == .AuthorizedAlways {
                        NSLog("UIApplicationLaunchOptionsLocationKey")
                        locationTracker.start(.Significant)
                    }
                }

                // The presence of this key indicates that a remote
                // notification is available for the app to
                // process. The value of this key is an NSDictionary
                // containing the payload of the remote
                // notification. See the description of
                // application:didReceiveRemoteNotification: for
                // further information about handling remote
                // notifications.

                else if let _ =
                    options[UIApplicationLaunchOptionsRemoteNotificationKey] as? NSDictionary {
                    NSLog("UIApplicationLaunchOptionsRemoteNotificationKey")
                }
            }
        }

        // override point for customization after application launch.
        return FBSDKApplicationDelegate.sharedInstance().application(application, didFinishLaunchingWithOptions: launchOptions)
    }

    func applicationWillResignActive(application: UIApplication) {
        // Sent when the application is about to move from active to
        // inactive state. This can occur for certain types of
        // temporary interruptions (such as an incoming phone call or
        // SMS message) or when the user quits the application and it
        // begins the transition to the background state.

        // Use this method to pause ongoing tasks, disable timers, and
        // throttle down OpenGL ES frame rates. Games should use this
        // method to pause the game.
        NSLog("applicationWillResignActive")
    }

    func applicationDidEnterBackground(application: UIApplication) {
        // Use this method to release shared resources, save user
        // data, invalidate timers, and store enough application state
        // information to restore your application to its current
        // state in case it is terminated later.

        // If your application supports background execution, this
        // method is called instead of applicationWillTerminate: when
        // the user quits.
        NSLog("applicationDidEnterBackground")
        locationTracker.start(.Significant)
        Location.upload()
    }

    func applicationWillEnterForeground(application: UIApplication) {
        // Called as part of the transition from the background to the
        // inactive state; here you can undo many of the changes made
        // on entering the background.
        NSLog("applicationWillEnterForeground")
        locationTracker.start(.Precise)
    }

    func applicationDidBecomeActive(application: UIApplication) {
        // Restart any tasks that were paused (or not yet started)
        // while the application was inactive. If the application was
        // previously in the background, optionally refresh the user
        // interface.
        NSLog("applicationDidBecomeActive")
        FBSDKAppEvents.activateApp()
    }

    func applicationWillTerminate(application: UIApplication) {
        // Called when the application is about to terminate. Save
        // data if appropriate. See also
        // applicationDidEnterBackground:.
        NSLog("applicationWillTerminate")
        locationTracker.start(.Significant)
        // close the Magical Record
        MagicalRecord.cleanUp()
    }

}

extension AppDelegate {

    func application(application: UIApplication, openURL url: NSURL, sourceApplication: String?, annotation: AnyObject) -> Bool {
        return FBSDKApplicationDelegate.sharedInstance().application(
            application, openURL: url, sourceApplication: sourceApplication, annotation: annotation)
    }

}

extension AppDelegate {

    // MARK: - Background fetch

#if false
    func application(application: UIApplication,
                     performFetchWithCompletionHandler
                     completionHandler: (UIBackgroundFetchResult) -> Void) {
        NSLog("background fetch")

        locationTracker.ping()
        // uploads the all locatins in the core data
        //Location.upload()
        // completed with new data
        completionHandler(UIBackgroundFetchResult.NewData)
    }
#endif

}

extension AppDelegate {

    // MARK: - Remote notification

    func application(application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: NSData) {
        notificationHandler.setDeviceToken(deviceToken)
    }

    func application(application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: NSError) {
        notificationHandler.didFailToRegisterForRemoteNotifications(error)
    }

    func application(application: UIApplication, didReceiveRemoteNotification userInfo: [NSObject : AnyObject], fetchCompletionHandler completionHandler: (UIBackgroundFetchResult) -> Void) {
        notificationHandler.didReceiveRemoteNotification(userInfo)
        completionHandler(UIBackgroundFetchResult.NoData)
    }
}
