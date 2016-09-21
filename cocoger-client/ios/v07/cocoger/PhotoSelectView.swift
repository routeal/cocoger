//
//  PhotoSelectView.swift
//  CustomeUIView
//
//  Created by Hiroshi Watanabe on 9/8/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit
import MobileCoreServices
import Photos
import AssetsLibrary
import AVFoundation
import AVKit

protocol PhotoSelectViewDelegate: class {
    func selected(image: UIImage?, type: PhotoType) -> Void
}

class PhotoSelectView: UIView, UIScrollViewDelegate, PhotoEditorControllerDelegate,
                       UIImagePickerControllerDelegate, UINavigationControllerDelegate {

    class NamedImageView : UIImageView {
        var name = ""
        var type: PhotoType = .Asset

        init(image: UIImage? = nil, name: String, type: PhotoType = .Asset) {
            super.init(image: image)
            self.setValue(name, forKey: "name")
            self.contentMode = .ScaleAspectFit
            self.type = type
        }

        required init?(coder aDecoder: NSCoder) {
            super.init(coder: aDecoder)
        }

        deinit {
            // print("deinit: NamedImageView")
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

    weak var delegate: PhotoSelectViewDelegate?

    private var initPhotoName: String = "uncle"

    var filterType: UInt = PhotoType.Asset.rawValue | PhotoType.Photo.rawValue

    var supportCamera: Bool = true

    var name: String {
        get {
            if let image = scrollView.subviews[currentIndex + nixImageCount + 1] as? NamedImageView {
                if let v = image.valueForKey("name") as? String {
                    return v
                }
            }
            return initPhotoName
        }
        set(v) {
            initPhotoName = v
        }
    }

    var type: UInt {
        if let image = scrollView.subviews[currentIndex + nixImageCount + 1] as? NamedImageView {
            return image.type.rawValue
        }
        return 0
    }

    var image: UIImage? {
        if let image = scrollView.subviews[currentIndex + nixImageCount + 1] as? NamedImageView {
            return image.image
        }
        return nil
    }

    deinit {
        print("deinit: PhotoSelectView")
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
    }

    override func willMoveToSuperview(newSuperview: UIView?) {
        if scrollView.subviews.count == 0 {
            load()
        }
    }

    func load() {
        let images = Image.getAll()
        for image in images {
            if image.type == PhotoType.Facebook.rawValue {
                continue
            }

            let type = PhotoType(rawValue: UInt(image.type))!

            if filterType & type.rawValue == 0 {
                continue
            }

            let imageView = NamedImageView(name: image.name, type: type)
            if imageView.type == PhotoType.Photo {
                imageView.userInteractionEnabled = true
                imageView.addGestureRecognizer(UITapGestureRecognizer(target: self, action: "startDeleteMenu:"))
            }

            Image.getImage(image.name, type: type.rawValue, completion: { (image: UIImage?) in
                if let newimage = image {
                    imageView.image = newimage
                } else {
                    imageView.image = Image.getImage(named: "person.png")
                }
            })

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
                    if name == "front" || name == "camera" || name == "back" {
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

        let imageView = NamedImageView(name: "front")
        scrollView.insertSubview(imageView, atIndex: 0)

        // add the none images to the front
        for var i = 1; i <= nixImageCount; i++ {
            let imageView = NamedImageView(name: "front")
            scrollView.insertSubview(imageView, atIndex: i)
        }

        scrollView.subviews.last!.removeFromSuperview()

        // add the none images to the back
        for var i = 0; i < nixImageCount; i++ {
            let imageView = NamedImageView(name: "back")
            scrollView.addSubview(imageView)
        }

        if supportCamera {
            let imageView = NamedImageView(image: Image.getImage(named: "camera.png"), name: "camera")
            imageView.userInteractionEnabled = true
            imageView.addGestureRecognizer(UITapGestureRecognizer(target: self, action: "startActionMenu:"))
            scrollView.addSubview(imageView)
        } else {
            let imageView = NamedImageView(name: "back")
            scrollView.addSubview(imageView)
        }

        imageSize = imageViewWidth
        imageMargin = imageSize / 5
        iconSize = imageSize - imageMargin * 2

        for (index, view) in scrollView.subviews.enumerate() {
            if let image = view as? NamedImageView {
                let x = imageSize * CGFloat(index) + imageMargin
                image.frame = CGRectMake(x, imageMargin, iconSize, iconSize)
            }
        }

        scrollView.contentSize = CGSize(width: imageSize * CGFloat(scrollView.subviews.count),
                                        height: imageSize)

        updateSelection()
    }

    func updateSelection() {
        var index = -1
        for var i = 0; i < scrollView.subviews.count; i++ {
            if let image = scrollView.subviews[i] as? NamedImageView {
                if let name = image.valueForKey("name") as? String {
                    if name == initPhotoName {
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
        tintColor.set()

        // Create Circle
        let x: CGFloat = scrollView.frame.size.width / 2 + viewMargin
        let y: CGFloat = imageSize / 2
        let radius: CGFloat = iconSize / 2 + 4
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

        if let d = delegate {
            if let image = scrollView.subviews[currentIndex + nixImageCount + 1] as? NamedImageView {
                d.selected(image.image, type: image.type)
            }
        }
    }

    func startDeleteMenu(gesture: UIGestureRecognizer) {
        //Create the AlertController
        let controller = UIAlertController(title: nil, message: nil, preferredStyle: .ActionSheet)

        //Create and add the Cancel action
        let cancelStr = NSLocalizedString("Cancel", comment: "action label")
        var action = UIAlertAction(title: cancelStr, style: .Cancel) { action -> Void in
            //Just dismiss the action sheet
        }
        controller.addAction(action)

        let deletePhotoStr = NSLocalizedString("Delete Photo", comment: "action label")
        action = UIAlertAction(title: deletePhotoStr, style: .Default) { action -> Void in
            if let imageView = gesture.view as? NamedImageView {
                self.deletePhoto(imageView)
            }
        }
        controller.addAction(action)

        if let popover = controller.popoverPresentationController {
            if let view = gesture.view {
                popover.sourceView  = view
                popover.sourceRect = view.bounds
            }
        }

        controller.show()
    }

    func startActionMenu(gesture: UIGestureRecognizer) {
        //Create the AlertController
        let controller = UIAlertController(title: nil, message: nil, preferredStyle: .ActionSheet)

        //Create and add the Cancel action
        let cancelStr = NSLocalizedString("Cancel", comment: "action label")
        var action = UIAlertAction(title: cancelStr, style: .Cancel) { action -> Void in
            //Just dismiss the action sheet
        }
        controller.addAction(action)

        if isCameraAvailable() && doesCameraSupportTakingPhotos() {
            let takePhotoStr = NSLocalizedString("Take Photo", comment: "action label")
            action = UIAlertAction(title: takePhotoStr, style: .Default) { action -> Void in
                let picker: UIImagePickerController = UIImagePickerController()
                picker.sourceType = .Camera
                picker.mediaTypes = UIImagePickerController.availableMediaTypesForSourceType(.Camera)!
                picker.delegate = self
                picker.allowsEditing = false

                if (UIDevice.currentDevice().userInterfaceIdiom == .Pad) {
                    if let view = gesture.view {
                        let popper = picker.popoverPresentationController
                        popper?.sourceView = view
                        popper?.sourceRect = view.bounds
                    }
                    picker.show()
                } else {
                    picker.show()
                }

            }
            controller.addAction(action)
        }

        let choosePhotoStr = NSLocalizedString("Choose Photo", comment: "action label")
        action = UIAlertAction(title: choosePhotoStr, style: .Default) { action -> Void in
            let picker : UIImagePickerController = UIImagePickerController()
            picker.sourceType = .PhotoLibrary
            picker.mediaTypes = UIImagePickerController.availableMediaTypesForSourceType(.PhotoLibrary)!
            picker.delegate = self
            picker.allowsEditing = false
            if (UIDevice.currentDevice().userInterfaceIdiom == .Pad) {
                if let view = gesture.view {
                    let popper = picker.popoverPresentationController
                    popper?.sourceView = view
                    popper?.sourceRect = view.bounds
                }
            }
            picker.show()
        }
        controller.addAction(action)

        let editPhotoStr = NSLocalizedString("Edit Photo", comment: "action label")
        action = UIAlertAction(title: editPhotoStr, style: .Default) { action -> Void in
            let editor = PhotoEditorViewController()
            editor.delegate = self
            let navigationController = UINavigationController(custom: true, rootViewController: editor)
            navigationController.show()
        }
        controller.addAction(action)

        if let popover = controller.popoverPresentationController {
            if let view = gesture.view {
                popover.sourceView  = view
                popover.sourceRect = view.bounds
            }
        }

        controller.show()
    }

    func isCameraAvailable() -> Bool{
        return UIImagePickerController.isSourceTypeAvailable(.Camera)
    }

    func cameraSupportsMedia(mediaType: String, sourceType: UIImagePickerControllerSourceType) -> Bool{

        let availableMediaTypes =
            UIImagePickerController.availableMediaTypesForSourceType(sourceType) 

        if let types = availableMediaTypes {
            for type in types {
                if type == mediaType {
                    return true
                }
            }
        }

        return false
    }

    func doesCameraSupportTakingPhotos() -> Bool {
        return cameraSupportsMedia(kUTTypeImage as String, sourceType: .Camera)
    }

    func save(image: UIImage?) {
        let photoType = PhotoType.Photo
        let name = NSDate().timeIntervalSince1970.description
        dispatch_async(dispatch_get_main_queue(), {
            self.insertPhoto(photoType, image: image!, name: name)
        })
    }

    func imagePickerController(picker: UIImagePickerController,
                               didFinishPickingMediaWithInfo info: [String : AnyObject]) {
        var photoImage: UIImage?
        var photoType: PhotoType?
        var name: String?

        if let type = info[UIImagePickerControllerMediaType] as? NSString {
            if type == kUTTypeImage {
                if let image = info[UIImagePickerControllerOriginalImage] as? UIImage {
                    photoImage = image
                }
                photoType = .Photo
                name = NSDate().timeIntervalSince1970.description
            }
        }

        picker.dismissViewControllerAnimated(true, completion: { _ in
             if photoType != nil && photoImage != nil && name != nil {
                 dispatch_async(dispatch_get_global_queue(QOS_CLASS_BACKGROUND, 0), {
                     photoImage = photoImage!.cropCircularImage(CGSizeMake(32, 32))
                     dispatch_async(dispatch_get_main_queue(), {
                         self.insertPhoto(photoType!, image: photoImage!, name: name!)
                     })
                 })
             }
        })
    }

    func insertPhoto(type: PhotoType, image: UIImage, name: String) {
        // extends the content to fit the additional image
        let width = scrollView.contentSize.width + imageSize * CGFloat(1)
        scrollView.contentSize = CGSize(width: width, height: imageSize)

        for var index = (scrollView.subviews.count-nixImageCount-1); index < scrollView.subviews.count; index++ {
            if let view = scrollView.subviews[index] as? NamedImageView {
                let frame: CGRect = view.frame
                let newX = frame.origin.x + imageSize
                view.setX(x: newX)
            }
        }

        // insert the new photo image
        let imageView = NamedImageView(image: image, name: name, type: type)
        let x = imageSize * CGFloat(scrollView.subviews.count-nixImageCount-1) + imageMargin
        imageView.frame = CGRectMake(x, imageMargin, iconSize, iconSize)
        scrollView.insertSubview(imageView, atIndex: scrollView.subviews.count-nixImageCount-1)

        let xx = imageSize * CGFloat(scrollView.subviews.count-(nixImageCount+1)*2-1)
        scrollView.contentOffset = CGPoint(x: xx, y: 0)

        currentIndex = Int(floor((scrollView.contentOffset.x - imageSize / 2) / imageSize) + 1)
    }

    func deletePhoto(imageView: NamedImageView) {
        Image.deleteImage(imageView.name, completion: {[weak self](status: Bool) in
            if status {
                dispatch_async(dispatch_get_main_queue()) {
                    self!.deletePhotoFromView(imageView)
                }
            }
        })
    }

    func deletePhotoFromView(imageView: NamedImageView) {
        // shrink the content to fit the additional image
        let width = scrollView.contentSize.width - imageSize * CGFloat(1)
        scrollView.contentSize = CGSize(width: width, height: imageSize)

        var position: Int?

        // find the index
        for var index = nixImageCount + 1; index < scrollView.subviews.count; index++ {
            if let view = scrollView.subviews[index] as? NamedImageView {
                if view.name == imageView.name {
                    position = index
                }
            }
        }

        // make space for the new photo image
        if let start = position {
            for var index = start+1; index < scrollView.subviews.count; index++ {
                if let view = scrollView.subviews[index] as? NamedImageView {
                    let frame: CGRect = view.frame
                    let newX = frame.origin.x - imageSize
                    view.setX(x: newX)
                }
            }
        }

        imageView.removeFromSuperview()

        currentIndex = Int(floor((scrollView.contentOffset.x - imageSize / 2) / imageSize) + 1)
    }

    func cropThumbnailImage(image :UIImage, w:Int, h:Int) ->UIImage {
        let origRef    = image.CGImage
        let origWidth  = Int(CGImageGetWidth(origRef))
        let origHeight = Int(CGImageGetHeight(origRef))
        var resizeWidth:Int = 0, resizeHeight:Int = 0

        if (origWidth < origHeight) {
            resizeWidth = w
            resizeHeight = origHeight * resizeWidth / origWidth
        } else {
            resizeHeight = h
            resizeWidth = origWidth * resizeHeight / origHeight
        }

        let resizeSize = CGSizeMake(CGFloat(resizeWidth), CGFloat(resizeHeight))
        UIGraphicsBeginImageContextWithOptions(resizeSize, false, CGFloat(0))

        image.drawInRect(CGRectMake(0, 0, CGFloat(resizeWidth), CGFloat(resizeHeight)))

        let resizeImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        let cropRect  = CGRectMake(
                CGFloat((resizeWidth - w) / 2),
                CGFloat((resizeHeight - h) / 2),
                CGFloat(w), CGFloat(h))
        let cropRef   = CGImageCreateWithImageInRect(resizeImage.CGImage, cropRect)
        let cropImage = UIImage(CGImage: cropRef!)

        return cropImage
    }
}
