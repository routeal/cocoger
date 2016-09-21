import UIKit

extension UIAlertController {

    class func simpleAlert(title: String?, message: String? = nil,
                           ok: String = "OK", cancel: String? = nil, dismiss: Int? = nil,
                           handler: ((index: Int) -> Void)? = nil,
                           completion: ((alert: UIAlertController) -> Void)? = nil) {

        // creates the alertcontroller
        let alert = UIAlertController(title: title, message: message, preferredStyle: .Alert)

        // adds the cancel button only when the handler is specified
        if cancel != nil && handler != nil {
            alert.addAction(UIAlertAction(title: cancel!, style: .Cancel) { action -> Void in
                                if handler != nil {
                                    handler!(index: 0)
                                }
                            })
        }

        // adds the default button
        alert.addAction(UIAlertAction(title: ok, style: .Default) { action -> Void in
                            if handler != nil {
                                handler!(index: 1)
                            }
                        })

        // NOTE: ipad support
        if let vc = UIViewController.viewController() {
            let sender = vc.view.subviews[0]
            if let popover = alert.popoverPresentationController {
                popover.sourceView  = sender as UIView
                popover.sourceRect = (sender as UIView).bounds
            }
        }

        // displays the alert with the option of the automatic dismissal
        alert.present(animated: true, completion: {
                          if dismiss != nil {
                              let delay = Double(dismiss!) * Double(NSEC_PER_SEC)
                              let time = dispatch_time(DISPATCH_TIME_NOW, Int64(delay))
                              dispatch_after(time, dispatch_get_main_queue()) { () -> Void in
                                  alert.dismissViewControllerAnimated(true) {
                                      if completion != nil {
                                          completion!(alert: alert)
                                      }
                                  }
                              }
                          } else if completion != nil {
                              completion!(alert: alert)
                          }
                      })
    }

}
