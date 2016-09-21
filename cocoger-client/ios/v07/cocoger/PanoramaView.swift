//
//  PanoramaView.swift
//  cocoger
//
//  Created by Hiroshi Watanabe on 11/25/15.
//  Copyright © 2015 routeal. All rights reserved.
//

import UIKit
import GoogleMaps

class PanoramaView: UIView {

    var panoView: GMSPanoramaView!

    var position: CLLocationCoordinate2D?

    var panoramaID: String?

    var navigationController: UINavigationController?

    var address: String?

    override init(frame: CGRect) {
        super.init(frame: frame)

        self.userInteractionEnabled = true

        panoView = GMSPanoramaView(frame: frame)
        panoView.userInteractionEnabled = false
        panoView.orientationGestures = false
        panoView.navigationGestures = false
        panoView.navigationLinksHidden = true
        addSubview(panoView)

        let tapGestureRecognizer = UITapGestureRecognizer(target: self, action: "handleTapGesture:")
        self.addGestureRecognizer(tapGestureRecognizer)

        let panGestureRecognizer = UIPanGestureRecognizer(target: self, action: "handlePanGesture:")
        self.addGestureRecognizer(panGestureRecognizer)
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    override func willMoveToWindow(newWindow: UIWindow?) {
        if self.panoramaID != nil {
            panoView.moveToPanoramaID(self.panoramaID!)
        } else if self.position != nil {
            panoView.moveNearCoordinate(self.position!)
        }
    }

    func handlePanGesture(sender: UIPanGestureRecognizer) {
        let translation = sender.translationInView(self)
        sender.view!.center = CGPoint(x: sender.view!.center.x + translation.x, y: sender.view!.center.y + translation.y)
        sender.setTranslation(CGPointZero, inView: self)
    }

    func handleTapGesture(sender: UITapGestureRecognizer) {
        print("handleTapGesture")

        let controller = PanoramaViewController()
        if self.panoramaID != nil {
            controller.panoramaID = self.panoramaID!
        } else if self.position != nil {
            controller.position = self.position!
        }
        controller.title = self.address

        if navigationController != nil {
            navigationController!.pushViewController(controller, animated: true)
        }
    }
}



class PanoramaViewController: UIViewController, GMSMapViewDelegate {

    var position: CLLocationCoordinate2D?

    var panoramaID: String?

    override func loadView() {
        if (self.title ?? "").isEmpty {
            let location = CLLocation(latitude: position!.latitude, longitude: position!.longitude)
            CLGeocoder().reverseGeocodeLocation(
                location, completionHandler:
                {(placemarks: [CLPlacemark]?, error: NSError?) in
                    if let error = error {
                        print(error.localizedDescription)
                    } else {
                        if let placemarks = placemarks {
                            print(placemarks.last!)
                            let placemark = placemarks.last!

                            if UIDevice.country == "JP" {
                                var street: String = ""
                                if let locality = placemark.locality {
                                    street += locality
                                }
                                if let thoroughfare = placemark.thoroughfare {
                                    street += thoroughfare
                                }
                                if let subThoroughfare = placemark.subThoroughfare {
                                    street += subThoroughfare
                                }
                                self.title = street
                            } else {
                                if let list = placemark.addressDictionary!["FormattedAddressLines"] as? [String] {
                                    self.title =  list.joinWithSeparator(", ")
                                }
                            }
                        }
                    }
                })
        }

        self.navigationController?.setNavigationBarHidden(false, animated: false)
        self.navigationController?.setToolbarHidden(true, animated: false)

        let panoView = GMSPanoramaView(frame: CGRectZero)

        self.view = panoView

        if panoramaID != nil {
            panoView.moveToPanoramaID(panoramaID!)
        } else if position != nil {
            panoView.moveNearCoordinate(position!)
        }
    }
}
