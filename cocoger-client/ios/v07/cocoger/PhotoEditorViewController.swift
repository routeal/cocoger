//
//  PhotoEditorViewController.swift
//  CustomView
//
//  Created by Hiroshi Watanabe on 10/11/15.
//  Copyright (c) 2015 test. All rights reserved.
//

import UIKit
import ACEDrawingView

protocol PhotoEditorControllerDelegate: class {
    func save(image: UIImage?) -> Void
}

class PhotoEditorViewController: UIViewController, PhotoSelectViewDelegate, ACEDrawingViewDelegate {

    required init(coder: NSCoder) {
        fatalError("NSCoding not supported")
    }

    override init(nibName nibNameOrNil: String!, bundle nibBundleOrNil: NSBundle!) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    deinit {
        print("deinit: PhotoEditorViewController")
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    var image: UIImage? {
        return preview.image
    }

    var editor: PhotoEditorView!
    var undo: UIBarButtonItem!
    var redo: UIBarButtonItem!
    var preview: UIImageView!
    var lineWidthSlider: UISlider!
    var lineAlphaSlider: UISlider!
    var photoSelect: PhotoSelectView!

    weak var delegate: PhotoEditorControllerDelegate?

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        self.navigationController?.setNavigationBarHidden(false, animated: false)
        self.navigationController?.setToolbarHidden(true, animated: false)
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        let cancelStr = NSLocalizedString("Cancel", comment: "barbutton title")
        self.navigationItem.leftBarButtonItem = UIBarButtonItem(title: cancelStr, style: .Plain, target: self, action: "cancel")
        let saveStr = NSLocalizedString("Save", comment: "barbutton title")
        self.navigationItem.rightBarButtonItem = UIBarButtonItem(title: saveStr, style: .Plain, target: self, action: "save")
        self.title = NSLocalizedString("Photo Editor", comment: "viewcontroller title")

        let upper = UIToolbar(frame: CGRectZero)
        upper.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(upper)

        photoSelect = PhotoSelectView(frame: CGRectZero)
        photoSelect.delegate = self
        photoSelect.translatesAutoresizingMaskIntoConstraints = false
        photoSelect.supportCamera = false
        photoSelect.backgroundColor = UIColor.lightGrayColor()
        photoSelect.filterType = PhotoType.Template.rawValue | PhotoType.Photo.rawValue
        self.view.addSubview(photoSelect)

        let bg = UIColor(red:   CGFloat(0xf9)/255,
                         green: CGFloat(0xf5)/255,
                         blue:  CGFloat(0xed)/255,
                         alpha: 1.0)

        editor = PhotoEditorView(frame: CGRectZero)
        editor.aced.delegate = self
        editor.translatesAutoresizingMaskIntoConstraints = false
        editor.backgroundColor = bg
        self.view.addSubview(editor)

        lineWidthSlider = UISlider(frame: CGRectZero)
        lineWidthSlider.hidden = true
        lineWidthSlider.minimumValue = 0
        lineWidthSlider.maximumValue = 20
        lineWidthSlider.continuous = true
        lineWidthSlider.value = Float(editor.aced.lineWidth)
        lineWidthSlider.addTarget(self, action: "widthChange:", forControlEvents: .ValueChanged)
        self.view.addSubview(lineWidthSlider)

        lineAlphaSlider = UISlider(frame: CGRectZero)
        lineAlphaSlider.hidden = true
        lineAlphaSlider.minimumValue = 0.1
        lineAlphaSlider.maximumValue = 1
        lineAlphaSlider.continuous = true
        lineAlphaSlider.value = Float(editor.aced.lineAlpha)
        lineAlphaSlider.addTarget(self, action: "alphaChange:", forControlEvents: .ValueChanged)
        self.view.addSubview(lineAlphaSlider)

        let views = ["upper":upper, "selector":photoSelect, "editor":editor]
        let constraints = ["H:|[upper]|",
                           "H:|[selector]|",
                           "H:|[editor]|",
                           "V:|[selector(48)]-[upper]-[editor]-|"]
        self.view.addLayoutConstraints(constraints, views: views)

        preview = UIImageView(frame: CGRectMake(6, 6, 32, 32))

        let image = UIBarButtonItem(customView: preview)
        let space = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.FlexibleSpace, target: nil, action: nil)
        undo = UIBarButtonItem(barButtonSystemItem: .Undo, target: self, action: "undo:")
        redo = UIBarButtonItem(barButtonSystemItem: .Redo, target: self, action: "redo:")
        let trash = UIBarButtonItem(barButtonSystemItem: .Trash, target: self, action: "trash:")

        let eraiser = UIBarButtonItem(image: Image.getImage(named: "eraiser.png"), style: .Plain, target: self, action: "eraiser:")
        let palette = UIBarButtonItem(image: Image.getImage(named: "palette.png"), style: .Plain, target: self, action: "palette:")
        let pen = UIBarButtonItem(image: Image.getImage(named: "pen.png"), style: .Plain, target: self, action: "pen:")
        let ruler = UIBarButtonItem(image: Image.getImage(named: "ruler.png"), style: .Plain, target: self, action: "ruler:")
        let flask = UIBarButtonItem(image: Image.getImage(named: "flask.png"), style: .Plain, target: self, action: "flask:")

        let upperItems = [undo, redo, space, image, space, trash]
        let lowerItems = [palette, space, pen, space, eraiser, space, ruler, space, flask]

        upper.setItems((upperItems as! [UIBarButtonItem]), animated: false)
        self.setToolbarItems(lowerItems as [UIBarButtonItem], animated: false)

        updateButtonStatus()
    }

    func updateButtonStatus() {
        undo.enabled = editor.aced.canUndo()
        redo.enabled = editor.aced.canRedo()
    }

    func selected(image: UIImage?, type: PhotoType) {
        /*
        editor.aced.clear()
        preview.image = nil
        updateButtonStatus()
        */
        editor.setImage(image, type: type)
    }

    func undo(sender: UIBarButtonItem) {
        editor.aced.undoLatestStep()
        updateButtonStatus()
        preview.image = editor.aced.image
    }

    func redo(sender: UIBarButtonItem) {
        editor.aced.redoLatestStep()
        updateButtonStatus()
        preview.image = editor.aced.image
    }

    func trash(sender: UIBarButtonItem) {
        editor.aced.clear()
        updateButtonStatus()
        preview.image = nil
    }

    func eraiser(sender: UIBarButtonItem) {
        self.editor.aced.drawTool = ACEDrawingToolTypeEraser
    }

    func palette(sender: UIBarButtonItem) {
        let controller = UIAlertController(title: nil, message: nil, preferredStyle: .ActionSheet)

        let cancelStr = NSLocalizedString("Cancel", comment: "action label")
        var action = UIAlertAction(title: cancelStr, style: .Cancel, handler: nil)
        controller.addAction(action)

        let blackStr = NSLocalizedString("Black", comment: "action label")
        action = UIAlertAction(title: blackStr, style: .Default) { action -> Void in
            self.editor.aced.lineColor = UIColor.blackColor()
        }
        controller.addAction(action)
        let redStr = NSLocalizedString("Red", comment: "action label")
        action = UIAlertAction(title: redStr, style: .Default) { action -> Void in
            self.editor.aced.lineColor = UIColor.redColor()
        }
        controller.addAction(action)
        let pinkStr = NSLocalizedString("Pink", comment: "action label")
        action = UIAlertAction(title: pinkStr, style: .Default) { action -> Void in
            self.editor.aced.lineColor = "ffb6c1".hexColor
        }
        controller.addAction(action)
        let greenStr = NSLocalizedString("Green", comment: "action label")
        action = UIAlertAction(title: greenStr, style: .Default) { action -> Void in
            self.editor.aced.lineColor = UIColor.greenColor()
        }
        controller.addAction(action)
        let tealStr = NSLocalizedString("Teal", comment: "action label")
        action = UIAlertAction(title: tealStr, style: .Default) { action -> Void in
            self.editor.aced.lineColor = "20b2aa".hexColor
        }
        controller.addAction(action)
        let blueStr = NSLocalizedString("Blue", comment: "action label")
        action = UIAlertAction(title: blueStr, style: .Default) { action -> Void in
            self.editor.aced.lineColor = UIColor.blueColor()
        }
        controller.addAction(action)
        let yellowStr = NSLocalizedString("Yellow", comment: "action label")
        action = UIAlertAction(title: yellowStr, style: .Default) { action -> Void in
            self.editor.aced.lineColor = "ffff66".hexColor
        }
        controller.addAction(action)

        if let popover = controller.popoverPresentationController {
            popover.barButtonItem = sender
        }

        self.presentViewController(controller, animated: true, completion: nil)
    }

    func pen(sender: UIBarButtonItem) {
        let controller = UIAlertController(title: nil, message: nil, preferredStyle: .ActionSheet)

        let cancelStr = NSLocalizedString("Cancel", comment: "action label")
        var action = UIAlertAction(title: cancelStr, style: .Cancel, handler: nil)
        controller.addAction(action)

        let penStr = NSLocalizedString("Pen", comment: "action label")
        action = UIAlertAction(title: penStr, style: .Default) { action -> Void in
            self.editor.aced.drawTool = ACEDrawingToolTypePen
        }
        controller.addAction(action)
        let lineStr = NSLocalizedString("Line", comment: "action label")
        action = UIAlertAction(title: lineStr, style: .Default) { action -> Void in
            self.editor.aced.drawTool = ACEDrawingToolTypeLine
        }
        controller.addAction(action)
        let rectagleStrokStr = NSLocalizedString("Rectangle", comment: "action label")
        action = UIAlertAction(title: rectagleStrokStr, style: .Default) { action -> Void in
            self.editor.aced.drawTool = ACEDrawingToolTypeRectagleStroke
        }
        controller.addAction(action)
        let ellipseStrokStr = NSLocalizedString("Ellipse", comment: "action label")
        action = UIAlertAction(title: ellipseStrokStr, style: .Default) { action -> Void in
            self.editor.aced.drawTool = ACEDrawingToolTypeEllipseStroke
        }
        controller.addAction(action)
        let textStr = NSLocalizedString("Text", comment: "action label")
        action = UIAlertAction(title: textStr, style: .Default) { action -> Void in
            self.editor.aced.drawTool = ACEDrawingToolTypeText
        }
        controller.addAction(action)

        if let popover = controller.popoverPresentationController {
            popover.barButtonItem = sender
        }

        self.presentViewController(controller, animated: true, completion: nil)
    }

    func ruler(sender: UIBarButtonItem) {
        let scale: CGFloat = UIScreen.mainScreen().scale

        let margin = 8 * scale * 2
        let width = self.view.frame.size.width - margin * 2
        let height = self.view.frame.size.height - margin * 3

        lineWidthSlider.frame = CGRectMake(margin, height, width, margin)
        lineWidthSlider.hidden = !lineWidthSlider.hidden
        lineAlphaSlider.hidden = true
    }

    func flask(sender: UIBarButtonItem) {
        let scale: CGFloat = UIScreen.mainScreen().scale

        let margin = 8 * scale * 2
        let width = self.view.frame.size.width - margin * 2
        let height = self.view.frame.size.height - margin * 3

        lineAlphaSlider.frame = CGRectMake(margin, height, width, margin)
        lineAlphaSlider.hidden = !lineAlphaSlider.hidden
        lineWidthSlider.hidden = true
    }

    func widthChange(sender: UISlider) {
        editor.aced.lineWidth = CGFloat(sender.value)
    }

    func alphaChange(sender: UISlider) {
        editor.aced.lineAlpha = CGFloat(sender.value)
    }

    func drawingView(view: ACEDrawingView, didEndDrawUsingTool tool: ACEDrawingTool) {
        updateButtonStatus()
        lineAlphaSlider.hidden = true
        lineWidthSlider.hidden = true
        preview.image = editor.aced.image
    }

    func cancel() {
        if let nav = self.navigationController {
            nav.popViewControllerAnimated(false)
            if let vc = nav.presentingViewController {
                vc.dismissViewControllerAnimated(true, completion: nil)
            }
        }
    }

    func save() {
        if let d = delegate {
            if let image = preview.image {
                d.save(image)
            }
        }
        cancel()
    }

    class PhotoEditorView: UIView {
        var aced: ACEDrawingView!
        var reference: CALayer!

        deinit {
            print("deinit: PhotoEditorView")
        }

        override init(frame: CGRect) {
            super.init(frame: frame)

            aced = ACEDrawingView(frame: CGRectZero)
            aced.layer.borderColor = UIColor(red:0, green:0, blue:0, alpha: 0.3).CGColor
            aced.layer.borderWidth = 1.0
            addSubview(aced)
        }

        required init?(coder aDecoder: NSCoder) {
            super.init(coder: aDecoder)
        }

        func setImage(image: UIImage?, type: PhotoType) {
            if let img = image {
                UIGraphicsBeginImageContext(CGSizeMake(iconSize, iconSize))
                img.drawInRect(CGRectMake(0, 0, iconSize, iconSize))
                let scaled = UIGraphicsGetImageFromCurrentImageContext()
                UIGraphicsEndImageContext()

                if reference != nil {
                    reference.removeFromSuperlayer()
                    reference = nil
                }

                if type == PhotoType.Photo {
                    aced.loadImage(scaled)
                } else {
                    let newlayer = CALayer()
                    newlayer.opacity = 0.4
                    newlayer.contents = scaled.CGImage
                    newlayer.frame = aced.bounds
                    aced.layer.addSublayer(newlayer)
                    reference = newlayer
                }
            }
        }

        var iconSize: CGFloat!

        override func layoutSubviews() {
            let scale: CGFloat = UIScreen.mainScreen().scale
            let margin = 2 * scale
            var size = (frame.size.width > frame.size.height) ? frame.size.height : frame.size.width
            size -= margin * 2
            aced.frame = CGRectMake((frame.size.width-size)/2, margin, size, size)
            iconSize = size
        }

    }
}
