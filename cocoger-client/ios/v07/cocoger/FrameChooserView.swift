//
//  PhotoSelectView.swift
//  CustomeUIView
//
//  Created by Hiroshi Watanabe on 9/8/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit

protocol FrameChooserViewDelegate: class {
    func selected(color: String?) -> Void
}

class FrameChooserView: UIView, UIScrollViewDelegate, UINavigationControllerDelegate {

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

    weak var delegate: FrameChooserViewDelegate?

    private var colorName: String = "007aff"

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

    deinit {
        print("deinit: FrameChooserView")
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
            "007aff",
            "66ffff",
            "cc0033",
            "ccff33",
            "ff3333",
            "ff6633",
            "ff9933",
            "ffcc33",
            "ffff99",
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
            let x = imageSize * CGFloat(nixImageCount + 1) + insideMargin / 2 + insideImageSize / 2
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

        if let d = delegate {
            if let image = scrollView.subviews[currentIndex + nixImageCount + 1] as? NamedImageView {
                if let name = image.valueForKey("name") as? String {
                    d.selected(name)
                }
            }
        }
    }

    func createAnnotationImage(bg: UIColor) -> UIImage? {
        let imageBackground = bg

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
        CGContextSetLineWidth(ctx, 5.0 * scale)

        CGContextSetStrokeColorWithColor(ctx, imageBackground.CGColor)
        CGContextStrokeEllipseInRect(ctx, circleRect)


        // Create Image
        let annotationImage = UIGraphicsGetImageFromCurrentImageContext()

        UIGraphicsEndImageContext()

        return annotationImage
    }

}
