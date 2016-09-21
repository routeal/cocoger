//
//  ViewController.swift
//  GeoPlace
//
//  Created by Hiroshi Watanabe on 11/27/15.
//  Copyright © 2015 routeal. All rights reserved.
//

import UIKit
import CoreLocation

class ViewController: UIViewController {

    var input: UITextField!

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.

        let label = UILabel(frame: CGRectMake(8, 40, 70, 40))
        label.text = "Address:"
        self.view.addSubview(label)

        input = UITextField(frame: CGRectMake(80, 40, 220, 40))
        input.layer.borderWidth = 1.0
        self.view.addSubview(input)

        let myButton = UIButton(frame: CGRectMake(80, 100, 160, 40))
        myButton.setTitle("Get Geo Location", forState: .Normal)
        myButton.setTitleColor(UIColor.blueColor(), forState: .Normal)
        myButton.addTarget(self, action: "pressedAction:", forControlEvents: .TouchUpInside)
        self.view.addSubview(myButton)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    func pressedAction(sender: UIButton!) {
        if let text = input.text {
            CLGeocoder().geocodeAddressString(
                text, completionHandler: {(placemarks: [CLPlacemark]?, error: NSError?) in
                    if let error = error {
                        print(error.localizedDescription)
                        return
                    }
                    if let pms = placemarks {
                        for place in pms {
                            if let list = place.addressDictionary!["FormattedAddressLines"] as? [String] {
                                print(list.joinWithSeparator(", "))
                            }
                            if place.location == nil {
                                print("no location available")
                            } else {
                                print(place.location)
                            }
                        }
                    }
                })
        }
    }
}

