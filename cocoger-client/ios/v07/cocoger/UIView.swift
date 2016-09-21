import UIKit

extension UIView {

    func addLayoutConstraints(formats: [String], views: [String : AnyObject]) {
        var c = NSLayoutConstraint.constraintsWithVisualFormat(formats[0], options: [],
                                                               metrics: nil, views: views)
        for var i = 1; i < formats.count; i++ {
            c += NSLayoutConstraint.constraintsWithVisualFormat(formats[i], options: [],
                                                                metrics: nil, views: views)
        }
        self.addConstraints(c)
    }

}

/**
    Extension UIView
    by DaRk-_-D0G
*/
extension UIView {
    /**
    Set x Position

    :param: x CGFloat
    by DaRk-_-D0G
    */
    func setX(x x:CGFloat) {
        var frame:CGRect = self.frame
        frame.origin.x = x
        self.frame = frame
    }
    /**
    Set y Position

    :param: y CGFloat
    by DaRk-_-D0G
    */
    func setY(y y:CGFloat) {
        var frame:CGRect = self.frame
        frame.origin.y = y
        self.frame = frame
    }
    /**
    Set Width

    :param: width CGFloat
    by DaRk-_-D0G
    */
    func setWidth(width width:CGFloat) {
        var frame:CGRect = self.frame
        frame.size.width = width
        self.frame = frame
    }
    /**
    Set Height

    :param: height CGFloat
    by DaRk-_-D0G
    */
    func setHeight(height height:CGFloat) {
        var frame:CGRect = self.frame
        frame.size.height = height
        self.frame = frame
    }
}
