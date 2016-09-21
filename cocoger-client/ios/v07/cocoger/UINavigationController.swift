import UIKit

extension UINavigationController {

    convenience init(custom: Bool) {
        self.init()
        navigationBar.barTintColor = "208695".hexColor
        navigationBar.tintColor = "99FFFF".hexColor
        navigationBar.translucent = false
        navigationBar.titleTextAttributes = [
            NSForegroundColorAttributeName : UIColor.whiteColor(),
        ]
        setNavigationBarHidden(true, animated: false)
    }

    convenience init(custom: Bool, rootViewController: UIViewController) {
        self.init(rootViewController: rootViewController)
        navigationBar.barTintColor = "208695".hexColor
        navigationBar.tintColor = "99FFFF".hexColor
        navigationBar.translucent = false
        navigationBar.titleTextAttributes = [
            NSForegroundColorAttributeName : UIColor.whiteColor(),
        ]
        setNavigationBarHidden(true, animated: false)
    }

}
