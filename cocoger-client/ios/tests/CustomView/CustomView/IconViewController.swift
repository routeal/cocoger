//
//  IconViewController.swift
//  CustomView
//
//  Created by Hiroshi Watanabe on 10/11/15.
//  Copyright (c) 2015 test. All rights reserved.
//

import UIKit

class IconViewController: UIViewController {

    required init(coder: NSCoder) {
        fatalError("NSCoding not supported")
    }

    override init(nibName nibNameOrNil: String!, bundle nibBundleOrNil: NSBundle!) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    deinit {
        print("deinit: IconViewController")
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.title = "Annotation Icon Sample"

        self.navigationItem.rightBarButtonItem =
            UIBarButtonItem(title: "Next", style: .Plain, target: self, action: "next")

        // Do any additional setup after loading the view.

        let sizeOfScreen: CGSize  = UIScreen.mainScreen().bounds.size

        let rect5 = CGRectMake((sizeOfScreen.width - 200) / 2, 100, 200, 200)
        let uiimage = UIImageView()
        uiimage.frame = rect5
        uiimage.backgroundColor = UIColor.lightGrayColor()
        self.view.addSubview(uiimage)

        uiimage.image = createAnnotationImage(CGSizeMake(200,200), image: UIImage(named: "alien"))
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    func next() {
        self.navigationController!.pushViewController(PhotoEditorViewController(), animated: true)
    }

    func createAnnotationImage(size: CGSize, image: UIImage? = nil,
                               bgColor: UIColor = UIColor.cyanColor()) -> UIImage? {
        let canvasSize = size
        let borderBackground = bgColor
        _ = bgColor
        let circleAlpha: CGFloat = 0.6
        let imageAlpha: CGFloat = 0.8

        let circleR = (canvasSize.width*2/3) / 2
        let circleRect = CGRectMake((canvasSize.width-circleR*2) / 2, 0, circleR*2, circleR*2)

        let circleX = circleRect.origin.x + circleR // should be (size.width / 2)
        let circleY = circleR

        let startX = circleX + circleR * cos(240.degreesToRadians)
        let startY = circleY - circleR * sin(240.degreesToRadians)

        let endX = circleX + circleR * cos(300.degreesToRadians)
        let endY = circleY - circleR * sin(300.degreesToRadians)

        let lastX = circleX
        let lastY = size.height

        // Create the context
        UIGraphicsBeginImageContext(canvasSize)
        let ctx = UIGraphicsGetCurrentContext()

        CGContextSetAlpha(ctx, circleAlpha)

        CGContextSetLineWidth(ctx, 1.0)
        CGContextSetStrokeColorWithColor(ctx, borderBackground.CGColor)
        CGContextSetFillColorWithColor(ctx, UIColor.cyanColor().CGColor)
        CGContextFillEllipseInRect(ctx, circleRect)
        CGContextStrokeEllipseInRect(ctx, circleRect)

        CGContextBeginPath(ctx);
        CGContextMoveToPoint   (ctx, startX, startY);  // top left
        CGContextAddLineToPoint(ctx, endX, endY);  // mid right
        CGContextAddLineToPoint(ctx, lastX, lastY);  // bottom left
        CGContextClosePath(ctx);
        CGContextSetFillColorWithColor(ctx, UIColor.cyanColor().CGColor)
        CGContextFillPath(ctx);

        if image != nil {
            let margin: CGFloat = 8
            let imageRect = CGRectMake(circleR/2+margin, circleR+margin,
                                       canvasSize.width-circleR-margin*2,
                                       canvasSize.height-circleR-margin*2)
            UIGraphicsPushContext(ctx!)
            CGContextSetAlpha(ctx, imageAlpha)
            CGContextTranslateCTM(ctx, 0, canvasSize.height)
            CGContextSetBlendMode(ctx, .Multiply)
            // to fix the upside down drawing
            CGContextScaleCTM(ctx, 1.0, -1.0)
            CGContextDrawImage(ctx, imageRect, image!.CGImage)
            UIGraphicsPopContext()
        }

        // Create Image
        let annotationImage = UIGraphicsGetImageFromCurrentImageContext()

        UIGraphicsEndImageContext()

        return annotationImage
    }

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}

extension Int {
    var degreesToRadians : CGFloat {
         return CGFloat(self) * CGFloat(M_PI) / 180.0
    }
}
