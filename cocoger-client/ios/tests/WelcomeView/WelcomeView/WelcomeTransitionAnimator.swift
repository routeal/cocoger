import UIKit

class WelcomeTransitionDelegate: NSObject, UIViewControllerTransitioningDelegate {

    func animationControllerForPresentedController(presented: UIViewController, presentingController presenting: UIViewController, sourceController source: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        return WelcomeTransitionAnimator()
    }

    func animationControllerForDismissedController(dismissed: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        return WelcomeTransitionAnimatorDismiss()
    }
}

class WelcomeTransitionAnimator: NSObject, UIViewControllerAnimatedTransitioning {

    var transitionContext: UIViewControllerContextTransitioning?

    func transitionDuration(transitionContext: UIViewControllerContextTransitioning?) -> NSTimeInterval {
        return 0.5
    }

    /**
        With masking transition
    */
    func animateTransition(transitionContext: UIViewControllerContextTransitioning) {
        self.transitionContext = transitionContext

        let fromViewController = transitionContext.viewControllerForKey(UITransitionContextFromViewControllerKey) as! UINavigationController

        let destinationController: DetailViewController = transitionContext.viewControllerForKey(UITransitionContextToViewControllerKey) as! DetailViewController
        let destinationView = destinationController.view

        let containerView = transitionContext.containerView()

        containerView!.addSubview(destinationController.view)

//        let buttonFrame = fromViewController.button.frame
        let buttonFrame = destinationController.from
        //let buttonFrame = destinationController.from
        let endFrame = CGRectMake(-CGRectGetWidth(destinationView.frame)/2, -CGRectGetHeight(destinationView.frame)/2, CGRectGetWidth(destinationView.frame)*2, CGRectGetHeight(destinationView.frame)*2)

        containerView!.addSubview(fromViewController.view)
        containerView!.addSubview(destinationView)

        let maskPath = UIBezierPath(ovalInRect: buttonFrame)

        let maskLayer = CAShapeLayer()
        maskLayer.frame = destinationView.frame
        maskLayer.path = maskPath.CGPath
        destinationController.view.layer.mask = maskLayer

        let bigCirclePath = UIBezierPath(ovalInRect: endFrame)

        let pathAnimation = CABasicAnimation(keyPath: "path")
        pathAnimation.delegate = self
        pathAnimation.fromValue = maskPath.CGPath
        pathAnimation.toValue = bigCirclePath
        pathAnimation.duration = transitionDuration(transitionContext)
        maskLayer.path = bigCirclePath.CGPath
        maskLayer.addAnimation(pathAnimation, forKey: "pathAnimation")
    }

    override func animationDidStop(anim: CAAnimation, finished flag: Bool) {
        if let transitionContext = self.transitionContext {
            transitionContext.completeTransition(!transitionContext.transitionWasCancelled())
        }
    }
}

class WelcomeTransitionAnimatorDismiss: NSObject, UIViewControllerAnimatedTransitioning {

    func transitionDuration(transitionContext: UIViewControllerContextTransitioning?) -> NSTimeInterval {
        return 0.5
    }

    func animateTransition(transitionContext: UIViewControllerContextTransitioning) {
        let destinationController = transitionContext.viewControllerForKey(UITransitionContextToViewControllerKey)! as! UINavigationController

        let containerView = transitionContext.containerView()! as UIView

        destinationController.view.alpha = 0.0
        containerView.addSubview(destinationController.view)

        UIView.animateWithDuration(transitionDuration(transitionContext), animations: { () -> Void in
            destinationController.view.alpha = 1.0
        }) { (finished) -> Void in
            transitionContext.completeTransition(!transitionContext.transitionWasCancelled())
        }
    }
}
