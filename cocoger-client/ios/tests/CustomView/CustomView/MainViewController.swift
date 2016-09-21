//
//  ViewController.swift
//  CustomView
//
//  Created by Hiroshi Watanabe on 10/11/15.
//  Copyright (c) 2015 test. All rights reserved.
//

import UIKit

enum PhotoType: UInt {
    case Asset = 1
    case Photo = 2
    case Facebook = 4
    case Template = 8
}

class MainViewController: UIViewController {

    required init(coder: NSCoder) {
        fatalError("NSCoding not supported")
    }

    override init(nibName nibNameOrNil: String!, bundle nibBundleOrNil: NSBundle!) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    deinit {
        print("deinit: MainViewController")
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.title = "Custom View Tests"

        self.navigationController!.navigationBar.translucent = false

        self.navigationItem.rightBarButtonItem =
            UIBarButtonItem(title: "Next", style: .Plain, target: self, action: "next")

        let locationRange = LocationRangeView(frame: CGRectZero)
        locationRange.backgroundColor = UIColor.lightGrayColor()
        locationRange.value = LocationRange.State
        locationRange.lowerValue = false
        locationRange.selectable = true
        locationRange.noneHidden = false
        locationRange.alertIndicator = true
        locationRange.size = 1
        locationRange.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(locationRange)

        let photoSelect = PhotoSelectView(frame: CGRectZero)
        //photoSelect.photoName = "alien"
        photoSelect.supportCamera = true
        photoSelect.backgroundColor = UIColor.lightGrayColor()
        photoSelect.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(photoSelect)

        let colorChooser = ColorChooserView(frame: CGRectZero)
        //photoSelect.photoName = "alien"
        colorChooser.value = "ccff33"
        colorChooser.backgroundColor = UIColor.lightGrayColor()
        colorChooser.image = UIImage(named: "person")
        colorChooser.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(colorChooser)

        let frameChooser = FrameChooserView(frame: CGRectZero)
        //photoSelect.photoName = "alien"
        frameChooser.value = "ccff33"
        frameChooser.backgroundColor = UIColor.lightGrayColor()
        frameChooser.image = UIImage(named: "person")
        frameChooser.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(frameChooser)

        let views = ["loc":locationRange, "photo":photoSelect, "color":colorChooser, "frame":frameChooser]
        let constraints = ["H:|-[loc]-|",
                           "H:|-[photo]-|",
                           "H:|-[color]-|",
                           "H:|-[frame]-|",
                           "V:|-16-[loc(44)]-16-[photo(44)]-16-[color(44)]-[frame(44)]"]
        self.view.addLayoutConstraints(constraints, views: views)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    func next() {
        self.navigationController!.pushViewController(IconViewController(), animated: true)
    }

}

let LocationRange = LocationRangeImpl()

class LocationRangeImpl {

    let Street  : Int = 3
    let Town    : Int = 6
    let City    : Int = 9
    let County  : Int = 12
    let State   : Int = 15
    let Country : Int = 18
    let None    : Int = 21

    var label: [String] = [
        "Street",
        "Town",
        "City",
        "State",
        "Country",
    ]

    var count: Int {
        return 5
    }

    func position(range: Int) -> Int? {
        if range == Street {
            return label.indexOf("Street")
        }
        else if range == Town {
            return label.indexOf("Town")
        }
        else if range == City {
            return label.indexOf("City")
        }
        else if range == County {
            return label.indexOf("County")
        }
        else if range == State {
            return label.indexOf("State")
        }
        else if range == Country {
            return label.indexOf("Country")
        }
        return nil
    }

    func value(position: Int) -> Int? {
        if position < 0 || position >= self.label.count {
            return nil
        }
        let v = self.label[position]
        if v == "Street" {
            return Street
        }
        if v == "Town" {
            return Town
        }
        if v == "City" {
            return City
        }
        if v == "County" {
            return County
        }
        if v == "State" {
            return State
        }
        if v == "Country" {
            return Country
        }
        return nil
    }
}

class Image {

    var name: String!
    var type: UInt = 1
    var url: String!
    var data: NSData!

    init(name: String, url: String) {
        self.name = name
        self.url = url
    }

    class func getAll() -> [Image] {
        let images: [Image] = [
            Image(name: "baby", url: "http://cocoger.com/images/baby.png"),
            Image(name: "cattle", url: "http://cocoger.com/images/cattle.png"),
            Image(name: "gandhi", url: "http://cocoger.com/images/gandhi.png"),
            Image(name: "indian", url: "http://cocoger.com/images/indian.png"),
            Image(name: "jack", url: "http://cocoger.com/images/jack.png"),
            Image(name: "panda", url: "http://cocoger.com/images/panda.png"),
            Image(name: "alien", url: "http://cocoger.com/images/alien.png"),
            Image(name: "pyramid", url: "http://cocoger.com/images/pyramid.png"),
            Image(name: "uncle", url: "http://cocoger.com/images/uncle.png"),
            Image(name: "cat", url: "http://cocoger.com/images/cat.png"),
            Image(name: "frog", url: "http://cocoger.com/images/frog.png"),
            Image(name: "hippie", url: "http://cocoger.com/images/hippie.png"),
            Image(name: "lacoon", url: "http://cocoger.com/images/lacoon.png"),
            Image(name: "glasses", url: "http://cocoger.com/images/glasses.png"),
            Image(name: "mummy", url: "http://cocoger.com/images/mummy.png"),
            Image(name: "ladybug", url: "http://cocoger.com/images/ladybug.png"),
            Image(name: "boy", url: "http://cocoger.com/images/boy.png"),
            Image(name: "boy2", url: "http://cocoger.com/images/boy2.png"),
            Image(name: "boy3", url: "http://cocoger.com/images/boy3.png"),
            Image(name: "demon", url: "http://cocoger.com/images/demon.png"),
            // Image(name: "", url: "http://bitsbees.com/assets/.png"),
        ]
        return images
    }

    func getImage(completion: (image: UIImage?) -> Void) {
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), {
                let url = NSURL(string: self.url)
                var data: NSData?
                if url != nil {
                    data = NSData(contentsOfURL: url!)
                }
                dispatch_async(dispatch_get_main_queue(), {
                    if data == nil {
                        completion(image: nil)
                    } else {
                        self.data = data
                        completion(image: UIImage(data: self.data))
                    }
               })
            })
    }

    class func getImage(name: String, type: UInt, completion: (image: UIImage?) -> Void) {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), {
            let str = "http://cocoger.com/images/\(name).png"
            let url = NSURL(string: str)
            var data: NSData?
            if url != nil {
                data = NSData(contentsOfURL: url!)
            }
            dispatch_async(dispatch_get_main_queue(), {
                if data == nil {
                    completion(image: nil)
                } else {
                    completion(image: UIImage(data: data!))
                }
            })
        })
    }

    class func deleteImage(name: String, completion: ((status: Bool) -> Void)? = nil) {
        if completion != nil {
            completion!(status: true)
        }
    }

}
