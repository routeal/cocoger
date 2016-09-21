//
//  ViewController.swift
//  SnsTest
//
//  Created by Hiroshi Watanabe on 10/27/15.
//  Copyright © 2015 routeal. All rights reserved.
//

import UIKit
import Social

class MainViewController: UIViewController {

    required init(coder: NSCoder) {
        fatalError("NSCoding not supported")
    }

    override init(nibName nibNameOrNil: String!, bundle nibBundleOrNil: NSBundle!) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    deinit {
        print("deinit: ViewController")
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        self.navigationController?.setNavigationBarHidden(true, animated: false)
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.

        let fb  = UIButton(type: UIButtonType.System) as UIButton
        fb.translatesAutoresizingMaskIntoConstraints = false
        fb.setTitle("FaceBook Button", forState: UIControlState.Normal)
        fb.addTarget(self, action: "fbAction:", forControlEvents: UIControlEvents.TouchUpInside)
        self.view.addSubview(fb)

        let tw  = UIButton(type: UIButtonType.System) as UIButton
        tw.translatesAutoresizingMaskIntoConstraints = false
        tw.setTitle("Twitter Button", forState: UIControlState.Normal)
        tw.addTarget(self, action: "twAction:", forControlEvents: UIControlEvents.TouchUpInside)
        self.view.addSubview(tw)

        let line  = UIButton(type: UIButtonType.System) as UIButton
        line.translatesAutoresizingMaskIntoConstraints = false
        line.setTitle("Line Button", forState: UIControlState.Normal)
        line.addTarget(self, action: "lineAction:", forControlEvents: UIControlEvents.TouchUpInside)
        self.view.addSubview(line)

        self.view.addLayoutConstraints(["H:|-100-[fb]",
                                        "H:|-100-[tw]",
                                        "H:|-100-[line]",
                                        "V:|-32-[fb]-[tw]-[line]"], views: ["fb":fb, "tw":tw, "line":line])
    }

    func fbAction(sender:UIButton!) {
        if SLComposeViewController.isAvailableForServiceType(SLServiceTypeFacebook) {
            let controller = SLComposeViewController(forServiceType: SLServiceTypeFacebook)
            controller.setInitialText("Testing Posting to Facebook")
            self.presentViewController(controller, animated:true, completion:nil)
        } else {
            print("no Facebook account found on device")
        }
    }

    func twAction(sender:UIButton!) {
        if SLComposeViewController.isAvailableForServiceType(SLServiceTypeTwitter) {
            let controller = SLComposeViewController(forServiceType:SLServiceTypeTwitter)
            controller.setInitialText("Testing Posting to Twitter")
            //[tweetSheet addImage:postImage];
            //[tweetSheet addURL:[NSURL URLWithString:@"http://qiita.com/WizowozY"]];
            self.presentViewController(controller, animated:true, completion:nil)
        } else {
            print("no Twitter account found on device")
        }
    }

    func lineAction(sender:UIButton!) {
        //let contentType = "text"
        let plainString = "content"
        let customAllowedSet =  NSCharacterSet(charactersInString:"!*'();:@&=+$,/?%#[] ").invertedSet
        let urlstring = plainString.stringByAddingPercentEncodingWithAllowedCharacters(customAllowedSet)

        let str = "line://msg/text/\(urlstring!)"

        let url = NSURL(string: str)

        UIApplication.sharedApplication().openURL(url!)
    }

}

class Image {
    
    var name: String!
    var type: Int = 0
    var url: String!
    var data: NSData!
    
    init(name: String, url: String) {
        self.name = name
        self.url = url
    }
    
    class func getAll() -> [Image] {
        let images: [Image] = [
            Image(name: "baby", url: "http://bitsbees.com/assets/baby.png"),
            Image(name: "cattle", url: "http://bitsbees.com/assets/cattle.png"),
            Image(name: "gandhi", url: "http://bitsbees.com/assets/gandhi.png"),
            Image(name: "indian", url: "http://bitsbees.com/assets/indian.png"),
            Image(name: "jack", url: "http://bitsbees.com/assets/jack.png"),
            Image(name: "panda", url: "http://bitsbees.com/assets/panda.png"),
            Image(name: "alien", url: "http://bitsbees.com/assets/alien.png"),
            Image(name: "pyramid", url: "http://bitsbees.com/assets/pyramid.png"),
            Image(name: "uncle", url: "http://bitsbees.com/assets/uncle.png"),
            Image(name: "cat", url: "http://bitsbees.com/assets/cat.png"),
            Image(name: "frog", url: "http://bitsbees.com/assets/frog.png"),
            Image(name: "hippie", url: "http://bitsbees.com/assets/hippie.png"),
            Image(name: "lacoon", url: "http://bitsbees.com/assets/lacoon.png"),
            Image(name: "glasses", url: "http://bitsbees.com/assets/glasses.png"),
            Image(name: "mummy", url: "http://bitsbees.com/assets/mummy.png"),
            Image(name: "ladybug", url: "http://bitsbees.com/assets/ladybug.png"),
            Image(name: "boy", url: "http://bitsbees.com/assets/boy.png"),
            Image(name: "boy2", url: "http://bitsbees.com/assets/boy2.png"),
            Image(name: "boy3", url: "http://bitsbees.com/assets/boy3.png"),
            Image(name: "demon", url: "http://bitsbees.com/assets/demon.png"),
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
    
    class func getImage(name: String, type: Int, completion: (image: UIImage?) -> Void) {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), {
            let str = "http://bitsbees.com/images/\(name).png"
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


