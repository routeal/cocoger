//
//  PhotoSelectView.swift
//  CustomeUIView
//
//  Created by Hiroshi Watanabe on 9/8/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit

class ColorChooserView: UIView, UIScrollViewDelegate, UINavigationControllerDelegate {

    class NamedImageView : UIImageView {
        var name = ""

        init(image: UIImage? = nil, name: String) {
            super.init(image: image)
            self.setValue(name, forKey: "name")
            self.contentMode = .ScaleAspectFit
        }

        required init?(coder aDecoder: NSCoder) {
            super.init(coder: aDecoder)
        }

        deinit {
            print("deinit: NamedImageView")
        }
    }

    private var scrollView: UIScrollView!

    private var currentIndex: Int = 0 // current scrollview position
    private var nixImageCount: Int = 0 // number of the empty images

    private var imageSize: CGFloat! // image square size
    private var iconSize: CGFloat! // icon square size (= imageSize - imagePadding)
    private var viewMargin: CGFloat = 8 // view margin
    private var imageMargin: CGFloat! // inner space
    private var leftArrow: UIImageView!
    private var rightArrow: UIImageView!

    private var colorName: String = "66cc33"
    private var frameColorName: String = "007aff"

    private var centerImage: NamedImageView?

    var value: String {
        get {
            if let image = scrollView.subviews[currentIndex + nixImageCount + 1] as? NamedImageView {
                if let name = image.valueForKey("name") as? String {
                    colorName = name
                }
            }
            return colorName
        }
        set(name) {
            colorName = name
            updateSelection()
        }
    }

    var insideImage: UIImage?

    var image: UIImage? {
        get {
            return insideImage
        }
        set(image) {
            insideImage = image
        }
    }

    var frameColor: String {
        set(s) {
            frameColorName = s
            setNeedsDisplay()
        }
        get {
            return frameColorName
        }
    }

    deinit {
        print("deinit: ColorChooserView")
    }

    init(frame: CGRect, name: String) {
        super.init(frame: frame)
        colorName = name
        setupViews()
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        setupViews()
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    func setupViews() {
        scrollView = UIScrollView(frame: CGRectZero)
        scrollView.delegate = self
        scrollView.showsHorizontalScrollIndicator = true
        addSubview(scrollView)

        leftArrow = UIImageView(image: Image.getImage(named: "leftarrow.png"))
        self.addSubview(leftArrow)
        rightArrow = UIImageView(image: Image.getImage(named: "rightarrow.png"))
        self.addSubview(rightArrow)

        let backgroundColors: [String] = [
            "0",
            "66cc33",
            "66ffff",
            "cc0033",
            "cc3333",
            "cc6633",
            "ccff33",
            "ff3333",
            "ff6633",
            "ff9933",
            "ffcc33",
            "ffff99",
            "33ccff",
            "003366",
            "006633",
            "fefa17",
            "9486a8",
            "9bb7a7",
            "b7b82b",
            "eab8b7",
        ]

        for bg in backgroundColors {
            let imageView = NamedImageView(name: bg)
            imageView.image = createAnnotationImage(bg.hexColor)
            scrollView.addSubview(imageView)
        }
    }

    override func layoutSubviews() {
        scrollView.frame = CGRectMake(viewMargin, 0, frame.size.width - viewMargin * 2, frame.size.height)

        leftArrow.frame = CGRectMake(0, (frame.size.height-viewMargin)/3, viewMargin, viewMargin)
        rightArrow.frame = CGRectMake(frame.size.width - viewMargin, (frame.size.height-viewMargin)/3, viewMargin, viewMargin)

        var imageViewWidth: CGFloat!

        // remove all the none images
        for var index = scrollView.subviews.count - 1; index >= 0; --index {
            if let image = scrollView.subviews[index] as? NamedImageView {
                if let name = image.valueForKey("name") as? String {
                    if name == "front" || name == "back" {
                        image.removeFromSuperview()
                    }
                }
            }
        }

        for var i = 5, j = 1; i < 211; i += 2, j++ {
            if (scrollView.frame.size.width / CGFloat(i) < scrollView.frame.size.height) {
                imageViewWidth = scrollView.frame.size.width / CGFloat(i)
                nixImageCount = j
                break
            }
        }

        if imageViewWidth == nil {
            return
        }

        var imageView: NamedImageView!

        // add the none images to the front
        for var i = 0; i < nixImageCount; i++ {
            imageView = NamedImageView(name: "front")
            scrollView.insertSubview(imageView, atIndex: i)
        }

        imageView = NamedImageView(name: "front")
        scrollView.insertSubview(imageView, atIndex: nixImageCount)

        // add the none images to the back
        for var i = 0; i < (nixImageCount - 1); i++ {
            imageView = NamedImageView(name: "back")
            scrollView.addSubview(imageView)
        }

        imageSize = imageViewWidth
        imageMargin = imageSize / 10
        iconSize = imageSize - imageMargin * 2

        for (index, view) in scrollView.subviews.enumerate() {
            if let image = view as? NamedImageView {
                let x = imageSize * CGFloat(index) + imageMargin
                image.frame = CGRectMake(x, imageMargin, iconSize, iconSize)
            }
        }

        if insideImage != nil && centerImage == nil {
            centerImage = NamedImageView(image: insideImage!, name: "center")
            centerImage!.layer.zPosition = 10
            addSubview(centerImage!)
        }

        if centerImage != nil {
            let insideImageSize = iconSize * 4 / 5
            let insideMargin = (imageSize - insideImageSize) / 2
            let x = imageSize * CGFloat(nixImageCount + 1) + insideImageSize / 2 + insideMargin / 4
            centerImage!.alpha = 0.9
            centerImage!.frame = CGRectMake(x, insideMargin, insideImageSize, insideImageSize)
        }

        scrollView.contentSize = CGSize(width: imageSize * CGFloat(scrollView.subviews.count),
                                        height: imageSize)

        updateSelection()
    }

    func updateSelection() {
        if imageSize == nil {
            return
        }
        var index = -1
        for var i = 0; i < scrollView.subviews.count; i++ {
            if let image = scrollView.subviews[i] as? NamedImageView {
                if let name = image.valueForKey("name") as? String {
                    if name == colorName {
                        index = i
                        break
                    }
                }
            }
        }
        // adjust the index with the predefined images
        index -= (nixImageCount + 1)
        if index >= 0 {
            let x = imageSize * CGFloat(index)
            scrollView.contentOffset = CGPoint(x: x, y: 0)
            currentIndex = Int(floor((scrollView.contentOffset.x - imageSize / 2) / imageSize) + 1)
        }
    }

    override func drawRect(rect: CGRect) {
        let context = UIGraphicsGetCurrentContext()

        // Set the circle outerline-width
        CGContextSetLineWidth(context, 2.0)

        // Set the circle outerline-colour
        frameColorName.hexColor.set()

        // Create Circle
        let x: CGFloat = scrollView.frame.size.width / 2 + viewMargin
        let y: CGFloat = imageSize / 2
        let radius: CGFloat = iconSize / 2
        let startAngle: CGFloat = 0.0
        let endAngle: CGFloat = CGFloat(M_PI * 2.0)
        CGContextAddArc(context, x, y, radius, startAngle, endAngle, 1)

        // Draw
        CGContextClosePath(context)
        CGContextStrokePath(context)
    }

    func scrollViewDidEndDragging(scrollView: UIScrollView, willDecelerate decelerate: Bool) {
        if !decelerate {
            stoppedScrolling()
        }
    }

    func scrollViewDidEndDecelerating(scrollView: UIScrollView) {
        stoppedScrolling()
    }

    func stoppedScrolling() {
        currentIndex = Int(floor((scrollView.contentOffset.x - imageSize / 2) / imageSize) + 1)

        if currentIndex < 0 {
            currentIndex = 0
        } else if currentIndex > scrollView.subviews.count {
            currentIndex = scrollView.subviews.count - 1
        }

        scrollView.contentOffset.x = imageSize * CGFloat(currentIndex)
    }

    func createAnnotationImage(bg: UIColor) -> UIImage? {
        let imageBackground = bg
        let circleAlpha: CGFloat = 0.6
        let imageAlpha: CGFloat = 0.9

        var canvasSize = CGSizeMake(100, 100)
        let scale = UIScreen.mainScreen().scale

        canvasSize.width *= scale
        canvasSize.height *= scale

        var circleRect = CGRectMake(0, 0, canvasSize.width, canvasSize.height)
        circleRect = CGRectInset(circleRect, 5, 5)

        // Create the context
        UIGraphicsBeginImageContext(canvasSize)

        let ctx = UIGraphicsGetCurrentContext()

        // setup drawing attributes
        CGContextSetLineWidth(ctx, 1.0 * scale)
        CGContextSetFillColorWithColor(ctx, imageBackground.CGColor)

        CGContextSetAlpha(ctx, circleAlpha)

        CGContextFillEllipseInRect(ctx, circleRect)

        UIGraphicsPushContext(ctx!)
        CGContextSetAlpha(ctx, imageAlpha)
        CGContextTranslateCTM(ctx, 0, canvasSize.height)
        CGContextSetBlendMode(ctx, .Multiply)
        CGContextScaleCTM(ctx, 1.0, -1.0)
        UIGraphicsPopContext()

        // Create Image
        let annotationImage = UIGraphicsGetImageFromCurrentImageContext()

        UIGraphicsEndImageContext()

        return annotationImage
    }

}
