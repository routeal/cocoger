//
//  MapViewController.swift
//  SokoraNow
//
//  Created by Hiroshi Watanabe on 7/7/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit
import MapKit
import EasyMapping
import PKHUD

class DateAnnotation: NSObject, MKAnnotation {
    let coordinate: CLLocationCoordinate2D
    let title: String?
    init(coordinate: CLLocationCoordinate2D, title: String) {
        self.coordinate = coordinate
        self.title = title
        super.init()
    }
}

class LocationHistoryViewController: UIViewController, UISearchBarDelegate, MKMapViewDelegate {

    var mapView: MKMapView!
    var hiddenTextField: UITextField!
    var datePicker: UIDatePicker!
    var toolBar: UIToolbar!
    var locations: [LocationLight] = []
    var currentDate: NSDate? // for not loading the same date

    required init(coder: NSCoder) {
        fatalError("NSCoding not supported")
    }

    private override init(nibName nibNameOrNil: String!, bundle nibBundleOrNil: NSBundle!) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    deinit {
        print("deinit: LocationHistoryViewController")
    }

    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        PKHUD.sharedHUD.show()
        loadLocationData(NSDate(), completion: { (status: Bool, reason: String?) in
            PKHUD.sharedHUD.hide(animated: false)
            if let message = reason {
                UIAlertController.simpleAlert(nil, message: message)
            }
        })
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.title = NSLocalizedString("Location History", comment: "viewcontroller title")
        self.navigationController!.setNavigationBarHidden(false, animated: false)
        let dateStr = NSLocalizedString("Date", comment: "barbutton title")
        self.navigationItem.rightBarButtonItem =
            UIBarButtonItem(title: dateStr, style: .Plain, target: self, action: "startDatePicker")

        mapView = MKMapView()
        //mapView.hidden = true
        mapView.delegate = self
        mapView.mapType = MKMapType.Standard
        mapView.zoomEnabled = true
        mapView.scrollEnabled = true
        mapView.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(mapView)
        self.view.addLayoutConstraints(["H:|[map]|", "V:|[map]|"], views: ["map":mapView])

        hiddenTextField = UITextField(frame: CGRectZero)
        hiddenTextField.hidden = true
        self.view.addSubview(hiddenTextField)

        datePicker = UIDatePicker()
        datePicker.maximumDate = NSDate()
        datePicker.datePickerMode = UIDatePickerMode.Date
        hiddenTextField.inputView = datePicker

        let space = UIBarButtonItem(
                barButtonSystemItem: UIBarButtonSystemItem.FlexibleSpace, target: nil, action: nil)
        let cancelStr = NSLocalizedString("Cancel", comment: "barbutton title")
        let btn0 = UIBarButtonItem(
                title: cancelStr, style: .Plain, target: self, action: "cancelDatePicker")
        let todayStr = NSLocalizedString("Today", comment: "barbutton title")
        let btn1 = UIBarButtonItem(
                title: todayStr, style: .Plain, target: self, action: "setToday")
        let selectStr = NSLocalizedString("Select", comment: "barbutton title")
        let btn2 = UIBarButtonItem(
                title: selectStr, style: .Plain, target: self, action: "selectDate")

        // note: size needs to be specified for some reason
        toolBar = UIToolbar(frame: CGRectMake(0, 0, 480, 40))
        toolBar.barStyle = .Default
        toolBar.items = [btn0, space, btn1, space, btn2]

        hiddenTextField.inputAccessoryView = toolBar
    }

    override func touchesBegan(touches: Set<UITouch>, withEvent event: UIEvent?) {
        hiddenTextField.resignFirstResponder()
    }

    override func touchesMoved(touches: Set<UITouch>, withEvent event: UIEvent?) {
    }

    override func touchesEnded(touches: Set<UITouch>, withEvent event: UIEvent?) {
    }

    func loadLocationData(date: NSDate, completion: (status: Bool, reason: String?) -> Void) {

        // check the date not to load twice
        if let c = currentDate {
            if c.isSameDate(date) {
                completion(status: false, reason: nil)
                return
            }
      }

        let parameters: [String: AnyObject] = [
            "timestamp": date.timeIntervalSince1970,
            "timezone": NSTimeZone.localTimeZone().name
        ]

        Rest.request(
            Router(api: "/m/locations", method: "GET", parameters: parameters, token: User.authToken),
            completion: { [weak self] (status: Bool, response: AnyObject?) -> Void in

            if !status {
                dispatch_async(dispatch_get_main_queue()) {
                    if let m = response!["message"] as? String {
                        completion(status: false, reason: m)
                    }
                }
                return
            }


            if let responseArray = response as? Array<Dictionary<String,AnyObject>> {
                self!.locations = []
                for json in responseArray {
                    let location = LocationLight(properties: json)
                    self!.locations.append(location)
                }
            }

            dispatch_async(dispatch_get_main_queue()) {
                if self!.locations.count == 0 {
                    let fmt = NSLocalizedString("No location available for %@", comment: "location history")
                    let msg = String(format: fmt, date.dateString)
                    completion(status: false, reason: msg)
                    return
                }

                self!.locations.sortInPlace({ $0.created.timeIntervalSinceDate($1.created) > 0 })

                self!.mapView.removeAnnotations(self!.mapView.annotations)

                self!.mapView.removeOverlays(self!.mapView.overlays)

                var points: [CLLocationCoordinate2D] = []
                var annotations: [DateAnnotation] = []

                var lastLocation: CLLocation?

                for location in self!.locations {

                    let point = CLLocationCoordinate2DMake(location.latitude as CLLocationDegrees,
                                                           location.longitude as CLLocationDegrees)

                    let cclocation = CLLocation(latitude: location.latitude as CLLocationDegrees,
                                                longitude: location.longitude as CLLocationDegrees)

                    var dist: Double = 0
                    if (lastLocation != nil) {
                        dist = cclocation.distanceFromLocation(lastLocation!)
                    }

                    /*
                    let formatter = NSDateFormatter()
                    formatter.timeStyle = .MediumStyle
                    let tstr = formatter.stringFromDate(location.created)
                    */
                    print("speed: \(location.speed) time=\(location.created.timeString) distance=\(dist)")

                    lastLocation = cclocation

                    let annotation = DateAnnotation(coordinate: point,
                                                    title: location.created.timeString)

                    points.append(point)
                    annotations.append(annotation)
                }

                self!.mapView.addAnnotations(annotations)

                let geodesic = MKPolyline(coordinates: &points, count: points.count)
                self!.mapView.addOverlay(geodesic)

                let mid = points.count > 1 ? points.count / 2 : 0

                self!.mapView.centerCoordinate = points[mid]
                self!.mapView.zoomLevel = 13

                self!.title = date.dateString
                self!.currentDate = date

                completion(status: true, reason: nil)
            }
        })

    }

    func mapView(mapView: MKMapView, rendererForOverlay overlay: MKOverlay) -> MKOverlayRenderer {
        //if overlay is MKPolyline {
            let polylineRenderer = MKPolylineRenderer(overlay: overlay)
            polylineRenderer.strokeColor = UIColor.blueColor()
            polylineRenderer.lineWidth = 2
            return polylineRenderer
    //}
                   //return nil
    }

    func mapView(mapView: MKMapView, viewForAnnotation annotation: MKAnnotation) -> MKAnnotationView? {

        let key = "pin"

        var view = mapView.dequeueReusableAnnotationViewWithIdentifier(key) as? MKPinAnnotationView

        if view == nil {
            view = MKPinAnnotationView(annotation: annotation, reuseIdentifier: key)
            view!.canShowCallout = true

            let image = Image.getImage(named: "clock.png")
            let image2 = image!.imageWithRenderingMode(.AlwaysOriginal)
            let button = UIButton(type: .DetailDisclosure)
            button.setImage(image2, forState: UIControlState.Normal)
            view!.leftCalloutAccessoryView = button

            /*
            view!.pinTintColor = (User.gender == 1) ? MKPinAnnotationView.purplePinColor() : MKPinAnnotationView.greenPinColor()
*/
        }

        return view
    }

    func startDatePicker() {
        hiddenTextField.becomeFirstResponder()
    }

    func selectDate() {
        hiddenTextField.resignFirstResponder()
        PKHUD.sharedHUD.show()
        loadLocationData(datePicker.date, completion: {(status: Bool, reason: String?) in
            PKHUD.sharedHUD.hide(animated: false)
            if let message = reason {
                UIAlertController.simpleAlert(message)
            }
        })
    }

    func setToday() {
        datePicker.date = NSDate()
    }

    func cancelDatePicker() {
        hiddenTextField.resignFirstResponder()
    }
}
