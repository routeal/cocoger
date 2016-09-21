//
//  MapViewController.swift
//  SokoraNow
//
//  Created by Hiroshi Watanabe on 7/7/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit
import PKHUD
import MapKit
import MagicalRecord
import MessageUI
import Social
import FBSDKShareKit

class HomeAnnotation: NSObject, MKAnnotation {
    dynamic var coordinate: CLLocationCoordinate2D
    var location: LocationLight = LocationLight()
    var title: String?
    var subtitle: String?

    init(coordinate: CLLocationCoordinate2D) {
        self.coordinate = coordinate
        super.init()
    }

    func address() -> String {
        return location.address(LocationRange.Street)
    }

    func address(street: String, town: String, city: String, county: String, state: String, country: String, zip: String) {
        location.address(street, town: town, city: city, county: county, state: state, country: country, zip: zip)
        subtitle = location.title(LocationRange.Street)
    }
}

class FriendAnnotation: NSObject, MKAnnotation {
    dynamic var coordinate: CLLocationCoordinate2D
    var location: LocationLight
    var id: String
    var title: String?
    var name: String
    var range: Int
    var photoType: UInt
    var photoName: String
    var subtitle: String?
    var gender: Int

    init(coordinate: CLLocationCoordinate2D, friend: Friend, location: LocationLight) {
        self.coordinate = coordinate
        self.location = location
        self.id = friend.user
        self.range = Int(friend.range)
        self.title = friend.name
        self.name = friend.name
        self.subtitle = location.title(range)
        self.photoType = UInt(friend.photoType)
        self.photoName = friend.photoName
        self.gender = Int(friend.gender)
        super.init()
    }
}

class MapIcon: UIImageView {
    static let SIZE: CGFloat = 42
    static let SPACE: CGFloat = 4
    init(index: Int, image: String, target: AnyObject?, action: Selector) {
        let y = (MapIcon.SIZE + MapIcon.SPACE) * CGFloat(index)
        super.init(frame: CGRectMake(0, y, MapIcon.SIZE, MapIcon.SIZE))
        self.backgroundColor = UIColor.lightGrayColor()
        self.layer.borderWidth = 1
        self.layer.borderColor = UIColor.whiteColor().CGColor
        self.layer.cornerRadius = 0.6
        self.alpha = 0.7
        self.image = Image.getImage(named:image)

        let tap = UITapGestureRecognizer(target: target, action: action)
        tap.numberOfTapsRequired = 1
        self.userInteractionEnabled = true
        self.addGestureRecognizer(tap)
    }
    init(image: String, target: AnyObject?, action: Selector) {
        super.init(frame: CGRectZero)
        self.translatesAutoresizingMaskIntoConstraints = false
        self.backgroundColor = UIColor.lightGrayColor()
        //self.layer.borderWidth = 1
        //self.layer.borderColor = UIColor.whiteColor().CGColor
        //self.layer.cornerRadius = 0.6
        self.alpha = 0.7
        self.image = Image.getImage(named: image)

        let tap = UITapGestureRecognizer(target: target, action: action)
        tap.numberOfTapsRequired = 1
        self.userInteractionEnabled = true
        self.addGestureRecognizer(tap)
    }
    required init(coder: NSCoder) {
        fatalError("NSCoding not supported")
    }
}

class MapViewController: UIViewController {

    let LABEL_HEIGHT: CGFloat = 18

    var mapView: MKMapView!

    var homeAnnotation: HomeAnnotation!

    // first time, zoom the map
    var firstUserLocation = true

    // friend annotations on the same coordinate
    var friendLocations: [CLLocationCoordinate2D: [FriendAnnotation]] = [:]

#if false
    // current tap point to detect the touch move
    var beganTouch: UITouch?
#endif

    // cache the callout right button
    var calloutRightButton: UIButton!

    // coordinate for the current callout collection view which is only available during the callout popup
    var currentCalloutCoordinate: CLLocationCoordinate2D?

    var selectedAnnotationView: Bool = false

    var searchContainer: UIView!

    var searchBar: UISearchBar!

    var loadOperations: [CLLocationCoordinate2D: NSBlockOperation] = [:]

#if false
    var circle: MKCircle?

    var tappedPolyline: MKPolyline?
#endif

    var closedTime: NSDate?

    required init(coder: NSCoder) {
        fatalError("NSCoding not supported")
    }

    override init(nibName nibNameOrNil: String!, bundle nibBundleOrNil: NSBundle!) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    deinit {
        NSNotificationCenter.defaultCenter().removeObserver(
            self, name: NSManagedObjectContextObjectsDidChangeNotification, object: nil)

        print("deinit: MapViewController")
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        createUI()
        locationTracker.delegate = self
        locationTracker.checkLocationServicesEnabled()
    }

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        // disable the navigation bar
        self.navigationController?.setNavigationBarHidden(true, animated: false)
        self.navigationController?.setToolbarHidden(true, animated: false)
        mapView.delegate = self
        if closedTime != nil {
            let delta = NSDate().timeIntervalSinceDate(closedTime!)
            if delta > 60 * 30 {
                refresh()
            }
        }
    }

    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        mapView.delegate = nil
        closedTime = NSDate()
    }

}

///////////////////////////////////////////////////////////////////////////////
// UI creation
///////////////////////////////////////////////////////////////////////////////

extension MapViewController {

    func createUI() {
        let image = Image.getImage(named: "rightarrow.png")
        // set the original color, otherwise set to tint color
        let origImage = image!.imageWithRenderingMode(.AlwaysOriginal)
        calloutRightButton = UIButton(type: .DetailDisclosure)
        calloutRightButton.setImage(origImage, forState: UIControlState.Normal)

        // mapview in full screen
        mapView = MKMapView()
        mapView.delegate = self
        mapView.frame = CGRectZero
        mapView.mapType = MKMapType.Standard
        mapView.zoomEnabled = true
        mapView.scrollEnabled = true
        mapView.showsUserLocation = false
        mapView.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(mapView)
        self.view.addLayoutConstraints(["H:|[map]|","V:|[map]|"], views: ["map": mapView])

        searchContainer = UIView()
        searchContainer.alpha = 0.8
        searchContainer.layer.cornerRadius = 5.0
        searchContainer.backgroundColor = UIColor.clearColor()
        //searchContainer.hidden = true
        searchContainer.translatesAutoresizingMaskIntoConstraints = false

        searchBar = UISearchBar()
        searchBar.translatesAutoresizingMaskIntoConstraints = false
        searchBar.placeholder = NSLocalizedString("Search Friends", comment: "search bar")
        searchBar.sizeToFit()
        searchBar.showsScopeBar = true
        searchBar.delegate = self
        searchBar.translucent = true
        searchContainer.addSubview(searchBar)

        let settings = MapIcon(image: "ic_menu_black_48dp.png", target: self, action: "settingsTapDetected:")
        searchContainer.addSubview(settings)
        let friends = MapIcon(image: "ic_add_black_48dp.png", target: self, action: "friendsTapDetected:")
        searchContainer.addSubview(friends)
        searchContainer.addLayoutConstraints(["H:|[menu(\(MapIcon.SIZE))]-[search]-[friends(\(MapIcon.SIZE))]|",
                                        "V:|-[search]-|", "V:|-[menu(\(MapIcon.SIZE))]-|", "V:|-[friends(\(MapIcon.SIZE))]-|"],
                                       views: ["menu":settings, "search":searchBar, "friends":friends])

        self.view.addSubview(searchContainer)
        self.view.addLayoutConstraints(["H:|-[search]-|", "V:|-18-[search]"], views: ["search":searchContainer])

        var i: Int!
        var height: CGFloat!

        /*
        i = 0
        let zoom = UIView()
        zoom.translatesAutoresizingMaskIntoConstraints = false
        let refresh = MapIcon(index: i, image: "ic_refresh_black_48dp.png", target: self, action: "refreshTapDetected:")
        zoom.addSubview(refresh)
        i = i + 2
        let zoomIn = MapIcon(index: i, image: "ic_add_black_48dp.png", target: self, action: "zoomInTapDetected:")
        zoom.addSubview(zoomIn)
        i = i + 1
        let zoomOut = MapIcon(index: i, image: "ic_remove_black_48dp.png", target: self, action: "zoomOutTapDetected:")
        zoom.addSubview(zoomOut)
        i = i + 1
        self.view.addSubview(zoom)
        height = (MapIcon.SIZE + MapIcon.SPACE) * CGFloat(i)
        self.view.addLayoutConstraints(["H:[zoom(\(MapIcon.SIZE))]-12-|", "V:[zoom(\(height))]-44-|"], views: ["zoom":zoom])
        */

        i = 0
        let mapType = UIView()
        mapType.translatesAutoresizingMaskIntoConstraints = false
        /*
        let normal = MapIcon(index: i, image: "ic_map_black_48dp.png", target: self, action: "normalTapDetected")
        mapType.addSubview(normal)
        i = i + 1
        let hibrid = MapIcon(index: i, image: "ic_satellite_black_48dp.png", target: self, action: "hybridTapDetected")
        mapType.addSubview(hibrid)
        */
        /*
        i = i + 1
        let terrain = MapIcon(index: i, image: "ic_terrain_black_48dp.png", target: self, action: "terrainTapDetected:")
        mapType.addSubview(terrain)
        i = i + 2
        */
        i = 3
        let myloc = MapIcon(index: i, image: "position2.png", target: self, action: "myLocTapDetected:")
        mapType.addSubview(myloc)
        i = i + 1
        self.view.addSubview(mapType)
        height = (MapIcon.SIZE + MapIcon.SPACE) * CGFloat(i)
        self.view.addLayoutConstraints(["H:|-16-[type(\(MapIcon.SIZE))]", "V:[type(\(height))]-44-|"], views: ["type":mapType])
    }

}

///////////////////////////////////////////////////////////////////////////////
// UI Event Handling
///////////////////////////////////////////////////////////////////////////////

extension MapViewController {

    func settingsTapDetected(sender: UITapGestureRecognizer) {
        self.slideMenuController()?.toggleLeft()
    }

    func friendsTapDetected(sender: UITapGestureRecognizer) {
        //self.slideMenuController()?.toggleRight()
        self.navigationController!.pushViewController(FriendTableViewController(), animated: true)
    }

    func myLocTapDetected(sender: UITapGestureRecognizer) {
        if homeAnnotation != nil {
            mapView.centerCoordinate = homeAnnotation.coordinate
        }
    }

    func zoomInTapDetected(sender: UITapGestureRecognizer) {
        let span = MKCoordinateSpan(latitudeDelta: mapView.region.span.latitudeDelta/2, longitudeDelta: mapView.region.span.longitudeDelta/2)
        let region = MKCoordinateRegion(center: mapView.region.center, span: span)

        mapView.setRegion(region, animated: true)
    }

    func zoomOutTapDetected(sender: UITapGestureRecognizer) {
        let span = MKCoordinateSpan(latitudeDelta: mapView.region.span.latitudeDelta*2, longitudeDelta: mapView.region.span.longitudeDelta*2)
        let region = MKCoordinateRegion(center: mapView.region.center, span: span)

        mapView.setRegion(region, animated: true)
    }

    func normalTapDetected() {
        mapView.mapType = .Standard
    }

    func hybridTapDetected() {
        mapView.mapType = .Hybrid
    }

    func terrainTapDetected(sender: UITapGestureRecognizer) {
    }

    func refreshTapDetected(sender: UITapGestureRecognizer) {
        refresh()
    }

    func setCurrentPosition(sender: UIBarButtonItem) {
    }

    func refresh() {
        for (_, operation) in loadOperations {
            operation.cancel()
        }

        loadOperations = [:]

        mapView.removeAnnotations(mapView.annotations)

        if homeAnnotation != nil {
            mapView.addAnnotation(homeAnnotation)
        }

        self.friendLocations = [:]

        if let friends = Friend.MR_findAll() as? [Friend] {
            for friend in friends {
                addFriendAnnotation(friend)
            }
        }
    }

}

///////////////////////////////////////////////////////////////////////////////
// MapView Delegate
///////////////////////////////////////////////////////////////////////////////

extension MapViewController: MKMapViewDelegate {

    // called when the annotation is added by addAnnotation()
    func mapView(mapView: MKMapView, viewForAnnotation annotation: MKAnnotation) -> MKAnnotationView? {
        print("viewForAnnotation")

        let id = "key"

        var annotationView = mapView.dequeueReusableAnnotationViewWithIdentifier(id)

        if annotationView == nil {
            let view = MKAnnotationView(annotation: annotation, reuseIdentifier: id)
            view.canShowCallout = true
            annotationView = view
        }

        if let _ = annotation as? HomeAnnotation {
            updateHomeAnnotation(annotationView!)
        } else if let friendAnnotation = annotation as? FriendAnnotation {
            self.setAnnotationImage(annotationView!, annotation: friendAnnotation)
        }

        return annotationView
    }

    func mapView(mapView: MKMapView, didSelectAnnotationView view: MKAnnotationView) {
        print("didSelectAnnotationView")
        searchContainer.hidden = true
    }

    func mapView(mapView: MKMapView, didDeselectAnnotationView view: MKAnnotationView) {
        print("didDeselectAnnotationView")
        
        searchContainer.hidden = false

        currentCalloutCoordinate = nil

        if view.rightCalloutAccessoryView  is UICollectionView {
            view.leftCalloutAccessoryView = nil
            view.rightCalloutAccessoryView = nil
        }
    }

    func mapView(mapView: MKMapView, regionDidChangeAnimated animated: Bool) {
        searchBar.resignFirstResponder()
    }

    func mapView(mapView: MKMapView, annotationView view: MKAnnotationView, calloutAccessoryControlTapped control: UIControl) {
        if view.annotation is HomeAnnotation && User.available {
            let controller = ProfileTableViewController(hasLocation: true, address: homeAnnotation.address())
            navigationController!.pushViewController(controller, animated: true)
        }
        // find a friend in the coordinate array
        else if let friendAnnotation = view.annotation as? FriendAnnotation {
            var id: String?
            if let annotations = self.friendLocations[friendAnnotation.coordinate] {
                for var i = 0; i < annotations.count; i++ {
                    if annotations[i].name == friendAnnotation.title {
                        id = annotations[i].id
                        break
                    }
                }
            }
            if id == nil {
                print("error: unable to find an id of the selected annotation ")
                return
            }
            let friend = Friend.MR_findFirstByAttribute("user", withValue: id)
            if friend != nil {
                tapFriendCallout(friend, annotation: friendAnnotation)
            }
        }
    }

#if false
/*
    func mapView(mapView: MKMapView, rendererForOverlay overlay: MKOverlay) -> MKOverlayRenderer {
        let renderer = MKPolylineRenderer(polyline: overlay as! MKPolyline)
        renderer.strokeColor = UIColor.blueColor()
        renderer.lineWidth = 3.0
        renderer.alpha = 0.7
        return renderer
    }

    func mapView(mapView: MKMapView, rendererForOverlay overlay: MKOverlay) -> MKOverlayRenderer {
            let circle = MKCircleRenderer(overlay: overlay)
            circle.strokeColor = UIColor.redColor()
            circle.fillColor = User.myColor.hexColor
            circle.alpha = 0.5
            circle.lineWidth = 1
            return circle
    }
*/
#endif

}

///////////////////////////////////////////////////////////////////////////////
// Core Data Change Detection
///////////////////////////////////////////////////////////////////////////////

extension MapViewController {

    // NOTE: called twice for one operation
    func handleDataModelChange(notification: NSNotification) {
        if let tmp = notification.userInfo {
            if let updated: AnyObject = tmp[NSUpdatedObjectsKey] {
                let asSet = updated as! NSSet
                if let friends = asSet.allObjects as? [Friend] {
                    for friend in friends {
                        print("Friend Updated: \(friend.name)")
                        updateFriendAnnotation(friend)
                    }
                }
                else if let users = asSet.allObjects as? [User] {
                    for _ in users {
                        if homeAnnotation != nil {
                            if let view = mapView.viewForAnnotation(homeAnnotation) {
                                print("Home Updated")
                                updateHomeAnnotation(view)
                            }
                        }
                    }
                }
            }
            else if let inserted: AnyObject = tmp[NSInsertedObjectsKey] {
                let asSet = inserted as! NSSet
                if let friends = asSet.allObjects as? [Friend] {
                    for friend in friends {
                        if let _ = self.friendAnnotation(friend) {
                            print("Friend already exists")
                        } else {
                            print("Friend Inserted")
                            addFriendAnnotation(friend)
                        }
                    }
                }
            }
            else if let deleted: AnyObject = tmp[NSDeletedObjectsKey] {
                let asSet = deleted as! NSSet
                if let friends = asSet.allObjects as? [Friend] {
                    for friend in friends {
                        if let friendAnnotation = self.friendAnnotation(friend) {
                            print("Friend Deleted")
                            removeAnnotation(friendAnnotation)
                        } else {
                            print("Friend already deleted")
                        }
                    }
                }
            }
        }
    }

}

///////////////////////////////////////////////////////////////////////////////
// Touch Event Handling
///////////////////////////////////////////////////////////////////////////////

extension MapViewController {

    override func touchesBegan(touches: Set<UITouch>, withEvent event: UIEvent?) {
        if let touch = touches.first {
            let currentPoint = touch.locationInView(self.view)

            for tmp in mapView.annotations {
                if let annotation = tmp as? FriendAnnotation {
                    if let view = mapView.viewForAnnotation(annotation) {
                        let frame = self.view.convertRect(view.frame, fromView: view.superview)
                        if CGRectContainsPoint(frame, currentPoint) {

                            let imagePoint = self.view.convertPoint(currentPoint, toView: view)
                            tapFriendAnnotation(view, annotation: annotation, point: imagePoint)

                            // event is consumed
                            //beganTouch = nil
                            return
                        }
                    }
                } else if tmp is HomeAnnotation {
                    if let view = mapView.viewForAnnotation(tmp) {
                        let frame = self.view.convertRect(view.frame, fromView: view.superview)
                        if CGRectContainsPoint(frame, currentPoint) {
                            // event is consumed
                            //beganTouch = nil
                            print("tap home annotation")
                            return
                        }
                    }
                }
            }

            //beganTouch = touch

#if false
            let tapCoord = mapView.convertPoint(currentPoint, toCoordinateFromView:self.mapView)
            let mapPoint = MKMapPointForCoordinate(tapCoord)
            for polyline in self.mapView.overlays as! [MKPolyline] {

                let view = mapView.rendererForOverlay(polyline)

                if view is MKPolylineRenderer {

                    let polyView = view as! MKPolylineRenderer
                    polyView.invalidatePath()

                    let polygonViewPoint = polyView.pointForMapPoint(mapPoint)

                    //let mapCoordinateIsInPolygon = CGPathContainsPoint(polyView.path, nil, polygonViewPoint, false)

                    let mapCoordinateIsInPolygon = CGRectContainsPoint(CGPathGetPathBoundingBox(polyView.path), polygonViewPoint);

                    if (mapCoordinateIsInPolygon) {
                        tappedPolyline = polyline
                    }
                }
            }
#endif
        }
    }

    override func touchesMoved(touches: Set<UITouch>, withEvent event: UIEvent?) {
#if false
        tappedPolyline = nil
#endif
    }

    override func touchesEnded(touches: Set<UITouch>, withEvent event: UIEvent?) {
#if false
        if tappedPolyline != nil {
            let okLabel = NSLocalizedString("OK", comment: "alert label")
            let cancelLabel = NSLocalizedString("Cancel", comment: "alert label")
            let message = NSLocalizedString("Remove the route?", comment: "alert message")
            UIAlertController.simpleAlert(
                message, ok: okLabel, cancel: cancelLabel,
                handler: {(index: Int) -> Void in
                    switch index {
                    case 1:
                        self.mapView.removeOverlay(self.tappedPolyline!)
                    default: break
                    }
                })
        }
#endif

        /*
        if let touch = touches.first {
            let currentPoint = touch.locationInView(self.view)
            if beganTouch != nil {
                let beganPoint = beganTouch!.locationInView(self.view)
                let distance = distanceBetween(currentPoint, p2: beganPoint)
                if distance > CGFloat(15) {
                }
            }
        }
        */
    }

#if false
    func distanceBetween(p1 : CGPoint, p2 : CGPoint) -> CGFloat {
        let dx : CGFloat = p1.x - p2.x
        let dy : CGFloat = p1.y - p2.y
        return sqrt(dx * dx + dy * dy)
    }
#endif

}

///////////////////////////////////////////////////////////////////////////////
// Home Annotation
///////////////////////////////////////////////////////////////////////////////

extension MapViewController {

    func updateHomeAnnotation(view: MKAnnotationView) {
        if User.available {
            homeAnnotation.title = User.name
        } else {
            homeAnnotation.title = NSLocalizedString("Future of You", comment: "user annotation for non member")
        }
        Image.getImage(User.photoName, type: User.photoType, completion: {(image: UIImage?) in
                let size = CGSizeMake(MapViewImage.UIS+4, MapViewImage.UIS+4)

                view.image = MapViewImage.getImage(image, size: size, backgroundColor: User.myColor.hexColor)

                let imageview = UIImageView(image: image)
                imageview.frame = CGRectMake(0, 0, MapViewImage.FIS/2, MapViewImage.FIS/2)

                view.leftCalloutAccessoryView = imageview
                view.rightCalloutAccessoryView = self.calloutRightButton
        })
    }

}

///////////////////////////////////////////////////////////////////////////////
// Friend Annotation
///////////////////////////////////////////////////////////////////////////////

extension MapViewController {

    // find an annotation from the friend list
    func friendAnnotation(friend: Friend) -> FriendAnnotation? {
        for (_, annotations) in self.friendLocations {
            for annotation in annotations {
                if annotation.id == friend.user {
                    return annotation
                }
            }
        }
        return nil
    }

    func viewForAnnotation(annotation: FriendAnnotation) -> MKAnnotationView? {
        if let annotations = self.friendLocations[annotation.coordinate] {
            for annotation in annotations {
                if let view = self.mapView.viewForAnnotation(annotation) {
                    return view
                }
            }
        }
        return nil
    }

    // find an annotation actually displayed, only the one of the
    // annotations in the same coordinate is used for the actual
    // annotation whose view is the annotation view
    func friendPopupAnnotation(annotation: FriendAnnotation) -> FriendAnnotation? {
        for tmp in mapView.annotations {
            if let friendAnnotation = tmp as? FriendAnnotation {
                if friendAnnotation.coordinate == annotation.coordinate {
                    return friendAnnotation
                }
            }
        }
        return nil
    }

    func addAnnotation(annotation: FriendAnnotation) {
        // initialize the array for the new coordinate
        if let annotations = self.friendLocations[annotation.coordinate] {
            for existing in annotations {
                if existing.id == annotation.id {
                    print("addAnnotation: already there")
                    return
                }
            }
        } else {
            self.friendLocations[annotation.coordinate] = [FriendAnnotation]()
        }

        // add the new coordinate
        self.friendLocations[annotation.coordinate]!.append(annotation)

        print("one coordinate with \(self.friendLocations[annotation.coordinate]!.count)")

        if self.friendLocations[annotation.coordinate]!.count == 1 {
            print("addAnnotation: added")
            if self.mapView.viewForAnnotation(annotation) == nil {
                self.mapView.addAnnotation(annotation)
            }
            return
        }

        if let annotations = self.friendLocations[annotation.coordinate] {
            for na in annotations {
                if let view = self.mapView.viewForAnnotation(na) {
                    view.image = getAnnotationImage(annotations)
                }
            }
        }
    }


    func removeAnnotation(annotation: FriendAnnotation) {
        print("removeAnnotation:")
        if let annotations = self.friendLocations[annotation.coordinate] {
            var index: Int?
            for (i, existing) in annotations.enumerate() {
                if existing.id == annotation.id {
                    index = i
                    print("removeAnnotation: \(i)")
                    break
                }
            }
            if let i = index {
                var newannotations = annotations
                newannotations.removeAtIndex(i)
                print("removeAnnotation: removed")
                if newannotations.count == 0 {
                    self.friendLocations[annotation.coordinate] = nil
                    print("removeAnnotation: size 0")
                    self.mapView.removeAnnotation(annotation)
                } else {
                    self.friendLocations[annotation.coordinate] = newannotations
                    print("removeAnnotation: size \(newannotations.count) - group annotation not removed")
                    for na in newannotations {
                        if let view = self.mapView.viewForAnnotation(na) {
                            print("removeAnnotation: view resued")
                            // this annotation belongs to one of the same position annotations for the view
                            view.image = getAnnotationImage(newannotations)
                            return
                        }
                    }
                    // this is the one for the view, so remove it and add another one
                    print("removeAnnotation: new annotation added")
                    self.mapView.removeAnnotation(annotation)

                    // add one of the annotations to the mapview so that the rest will be added
                    self.mapView.addAnnotation(newannotations[0])
                }
            } else {
                print("removeAnnotation: not found")
            }
        }
    }

    func tapFriendAnnotation(view: MKAnnotationView, annotation: FriendAnnotation, point: CGPoint) {
        if let annotations = self.friendLocations[annotation.coordinate] {
            // the annotation must be the one which shows the number
            // of the friends inside, create the callout and show the
            // list of the friends in the callout
            if annotations.count >= MapViewImage.friendImages.count {
                let space:CGFloat = 8

                let layout = UICollectionViewFlowLayout()
                layout.itemSize = CGSizeMake((MapViewImage.FIS+space), (MapViewImage.FIS+LABEL_HEIGHT))
                layout.sectionInset = UIEdgeInsetsMake(2, 2, 2, 2)

                let frame = CGRectMake(0, 0, (MapViewImage.FIS+space*2)*2, (MapViewImage.FIS+LABEL_HEIGHT+space)*3)
                let collectionView = UICollectionView(frame: frame, collectionViewLayout: layout)
                collectionView.registerClass(UICollectionViewCell.self, forCellWithReuseIdentifier: "id")
                collectionView.delegate = self
                collectionView.dataSource = self
                collectionView.backgroundColor = UIColor.clearColor()

                currentCalloutCoordinate = annotation.coordinate

                view.leftCalloutAccessoryView = nil
                view.rightCalloutAccessoryView = collectionView

                let fmt = NSLocalizedString("%d friends", comment: "annotation callout")
                annotation.title = String(format: fmt, annotations.count)

                // trim the subtitle
                if let index = annotation.subtitle!.rangeOfString(",") {
                    annotation.subtitle = annotation.subtitle!.substringToIndex(index.startIndex)
                }

                // workaround to force change the image of the icon
                //view.image = getAnnotationImage(annotations)
            } else {
                //println("num annotations: \(annotations.count)")
                var fi = MapViewImage.friendImages[annotations.count]
                for var i = 0; i < annotations.count; i++ {
                    let rect = fi.position[i]
                    let frame = CGRectMake(rect.origin.x, rect.origin.y, rect.size.width, rect.size.height)
                    //println("contains: point=\(point), rect=\(frame)")
                    if CGRectContainsPoint(frame, point) {
                        //println("found: \(annotations[i].name)")
                        let tapped = annotations[i]
                        mapView.deselectAnnotation(annotation, animated: false)
                        annotation.title = tapped.name
                        view.canShowCallout = true
                        let image = UIImageView(image: Image.getImage(tapped.photoName, type: tapped.photoType))
                        //image.backgroundColor = genderColor(tapped)
                        image.frame = CGRectMake(0, 0, MapViewImage.FIS/2, MapViewImage.FIS/2)
                        view.leftCalloutAccessoryView = image
                        view.rightCalloutAccessoryView = calloutRightButton
                    }
                }
            }
        }
    }

    func getAnnotationImage(annotations: [FriendAnnotation]) -> UIImage? {
        print("images with \(annotations.count)")

        if annotations.count == 0 {
            return nil
        }
        // 10 or more users in the same location, show the badge icon
        // which has the number of the users inside and display the
        // list of the users in the callout
        else if annotations.count >= MapViewImage.friendImages.count {
            return MapViewImage.getImage("\(annotations.count)", backgroundColor: "208695".hexColor)
        }
        // 1 to 9 users in the same position, show each icon at the
        // location where is specified by the predefined location
        else {
            var items: [(image: UIImage?, backgroundColor: UIColor)] = []
            for var i = 0; i < annotations.count; i++ {
                let annotation = annotations[i]
                let image = Image.getImage(annotation.photoName, type: annotation.photoType)
                let color = genderColor(annotation)

                items += [(image: image, backgroundColor: color)]
            }
            return MapViewImage.getImage(items)
        }
    }

    func setAnnotationImageImpl(view: MKAnnotationView, annotation: FriendAnnotation) {
        let operation = NSBlockOperation()
        operation.addExecutionBlock { () -> Void in
            let time = dispatch_time(DISPATCH_TIME_NOW, Int64(0.1*Double(NSEC_PER_SEC)))
            dispatch_after(time, dispatch_get_main_queue(), { () -> Void in

                if let friendAnnotations = self.friendLocations[annotation.coordinate] {
                    var imageLoaded = 0

                    func done(image: UIImage?) {
                        if operation.cancelled {
                            return
                        }
                        if ++imageLoaded == friendAnnotations.count {
                            //print("imageLoaded=\(imageLoaded)")
                            view.image = self.getAnnotationImage(friendAnnotations)
                        }
                    }

                    func empty(image: UIImage?) {
                        // do nothing
                    }

                    for annotation in friendAnnotations {
                        if operation.cancelled {
                            return
                        }
                        if friendAnnotations.count >= MapViewImage.friendImages.count {
                            Image.getImage(annotation.photoName, type: annotation.photoType, completion: empty)
                        } else {
                            Image.getImage(annotation.photoName, type: annotation.photoType, completion: done)
                        }
                    }

                    if operation.cancelled {
                        return
                    }

                    // shows the one icon which has the number of the friends inside
                    if friendAnnotations.count >= MapViewImage.friendImages.count {
                        view.image = self.getAnnotationImage(friendAnnotations)
                    }
                }
            })
        }
        loadOperations[annotation.coordinate] = operation
        operation.start()
    }

    func setAnnotationImage(view: MKAnnotationView, annotation: FriendAnnotation) {
        if let operation = loadOperations[annotation.coordinate] {
            operation.cancel()
            loadOperations[annotation.coordinate] = nil
        }
        setAnnotationImageImpl(view, annotation: annotation)
    }

}

///////////////////////////////////////////////////////////////////////////////
// Friend Annotation
///////////////////////////////////////////////////////////////////////////////

extension MapViewController {

    func addFriendAnnotation(friend: Friend) {
        if let _ = self.friendAnnotation(friend) {
            print("\(friend.name) annotation already exists")
            return
        }
        if friend.range == LocationRange.None {
            return
        }
        Friend.coordinate(friend, completion: {(status: Bool, location: LocationLight?) in
            if !status {
                print("addFriendAnnotation: Can't get the friend latest coordinate.")
                return
            }
            location!.rangeCoordinate(Int(friend.range),
                                      completion: {[weak self] (coordinate: CLLocationCoordinate2D?) in
                dispatch_async(dispatch_get_main_queue()) {
                    if coordinate != nil {
                        let annotation = FriendAnnotation(coordinate: coordinate!, friend: friend, location: location!)
                        self!.addAnnotation(annotation)
                    } else {
                        print("range coordinate not available - annotation won't be added")
                    }
                }
            })
        })
    }

    func updateFriendAnnotation(friend: Friend) {
        if let annotation = self.friendAnnotation(friend) {
            print("updateFriendAnnotation: \(annotation.title) \(annotation.range)")

            if annotation.title != friend.name {
                print("updateFriendAnnotation: friend title updated")
                annotation.title = friend.name
            }
            if annotation.range != Int(friend.range) {
                print("updateFriendAnnotation: range updated to \(friend.range)")
                annotation.range = Int(friend.range)
                annotation.subtitle = annotation.location.title(Int(friend.range))
                annotation.location.rangeCoordinate(
                    Int(friend.range),
                    completion: {(coordinate: CLLocationCoordinate2D?) in
                        if coordinate != nil {
                            print("new coordinate: \(annotation.range)")
                            self.removeAnnotation(annotation)
                            annotation.coordinate = coordinate!
                            self.addAnnotation(annotation)
                        } else {
                            print("range coordinate not available - annotation won't be added")
                        }
                    })
            }
            if annotation.photoName != friend.photoName {
                print("updateFriendAnnotation: \(annotation.photoName) \(friend.photoName)")
                annotation.photoType = UInt(friend.photoType)
                annotation.photoName = friend.photoName
                if let view = self.viewForAnnotation(annotation) {
                    if let annotations = self.friendLocations[annotation.coordinate] {
                        view.image = getAnnotationImage(annotations)
                    }
                }
            }
            if friend.hasMoved {
                print("updateFriendAnnotation: moved")
                friend.hasMoved = false
                friend.managedObjectContext!.MR_saveToPersistentStoreAndWait()
                self.updateFriendLocation(friend)
            }
            if friend.hasSignedOut {
                print("updateFriendAnnotation: signedout")
                friend.hasSignedOut = false
                friend.managedObjectContext!.MR_saveToPersistentStoreAndWait()
                let annotation = self.friendAnnotation(friend)
                if annotation != nil {
                    self.removeAnnotation(annotation!)
                }
            }
        } else {
            print("updateFriendAnnotation not found")
        }
    }

    // called from Notification when the friend is moved
    func updateFriendLocation(friend: Friend) {
        let annotation = self.friendAnnotation(friend)
        if annotation == nil {
            print("\(friend.name) annotation dose not exist")
            return
        }
        Friend.coordinate(friend, completion: {(status: Bool, location: LocationLight?) in
            if !status {
                print("updateFriendLocation: Can't get the friend latest coordinate.")
                return
            }
            location!.rangeCoordinate(Int(friend.range), completion: {(coordinate: CLLocationCoordinate2D?) in
                dispatch_async(dispatch_get_main_queue()) {
                    if coordinate != nil {
                        /*
                        // if the annotation is only the one for the view, simply update the location
                        if let annotations = self.friendLocations[annotation!.coordinate] {
                            if annotations.count == 1 {
                                annotation!.coordinate = coordinate!
                                return
                            }
                        }
                        */
                        self.removeAnnotation(annotation!)
                        annotation!.subtitle = annotation!.location.title(Int(friend.range))
                        annotation!.coordinate = coordinate!
                        self.addAnnotation(annotation!)
                    } else {
                        print("range coordinate not available - annotation won't be added")
                    }
                }
            })
        })
    }

}


///////////////////////////////////////////////////////////////////////////////
// Friend Annotation Callout
///////////////////////////////////////////////////////////////////////////////

extension MapViewController: UICollectionViewDelegate, UICollectionViewDataSource {

    func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        var annotation: FriendAnnotation?

        if let coordinate = currentCalloutCoordinate {
            if let annotations = self.friendLocations[coordinate] {
                annotation = annotations[indexPath.row]
            }
        }

        if annotation != nil {
            if let friend = Friend.MR_findFirstByAttribute("user", withValue: annotation!.id) {
                tapFriendCallout(friend, annotation: annotation!)
            }
        }
    }

    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        if let coordinate = currentCalloutCoordinate {
            if let annotations = self.friendLocations[coordinate] {
                return annotations.count
            }
        }
        return 0
    }

    func genderColor(annotation: FriendAnnotation) -> UIColor {
        return (annotation.gender == 0) ? User.boyColor.hexColor : User.girlColor.hexColor
    }

    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let cell : UICollectionViewCell =
            collectionView.dequeueReusableCellWithReuseIdentifier(
                "id", forIndexPath: indexPath)

        var annotation: FriendAnnotation?

        if let coordinate = currentCalloutCoordinate {
            if let annotations = self.friendLocations[coordinate] {
                annotation = annotations[indexPath.row]
            }
        }

        if annotation == nil {
            return cell
        }

        // Note: remove the previous ones for avoiding the repaint problem
        for view in cell.contentView.subviews {
            view.removeFromSuperview()
        }

        // create the friend image
        let image = Image.getImage(annotation!.photoName, type: annotation!.photoType)
        let size = CGSizeMake(MapViewImage.UIS-2, MapViewImage.UIS-2)
        let annotationImage = MapViewImage.getImage(image, size: size, backgroundColor: genderColor(annotation!))

        // add the image
        let imageView = UIImageView(image: annotationImage)
        imageView.frame = CGRectMake(0, 0, MapViewImage.FIS, MapViewImage.FIS)
        cell.contentView.addSubview(imageView)

        // add the label
        let labelFrame = CGRectMake(0, MapViewImage.FIS, MapViewImage.FIS, LABEL_HEIGHT)
        let label = UILabel(frame: labelFrame)
        label.text = annotation!.name
        //label.backgroundColor = UIColor.whiteColor()
        label.textAlignment = NSTextAlignment.Center
        label.font = UIFont.systemFontOfSize(UIFont.smallSystemFontSize())
        cell.contentView.addSubview(label)

        return cell
    }

    func tapFriendCallout(friend: Friend, annotation: FriendAnnotation) {
        if let deselect = friendPopupAnnotation(annotation) {
            mapView.deselectAnnotation(deselect, animated: false)
        }

        var action: UIAlertAction!
        var str: String!

        let fmt = NSLocalizedString("To %@", comment: "action label")
        str = String(format: fmt, friend.name)
        let controller = UIAlertController(title: str, message: nil, preferredStyle: .Alert)

        str = NSLocalizedString("Cancel", comment: "action label")
        action = UIAlertAction(title: str, style: .Cancel, handler: nil)
        controller.addAction(action)

        controller.addTextFieldWithConfigurationHandler { (textField) in
            textField.placeholder = NSLocalizedString("Not for Facebook", comment: "action label")
            textField.keyboardType = .Default
        }

        // Line is availabe only for Japan
        if UIDevice.country.caseInsensitiveCompare("JP") == .OrderedSame {
            str = NSLocalizedString("Send with Line", comment: "action label")
            action = UIAlertAction(title: str, style: .Default) { action -> Void in
                let textField = controller.textFields![0] as UITextField
                let allowedSet =  NSCharacterSet(charactersInString:"!*'();:@&=+$,/?%#[] ").invertedSet
                if let text = textField.text {
                    let encstring = text.stringByAddingPercentEncodingWithAllowedCharacters(allowedSet)
                    let urlstr = "line://msg/text/\(encstring!)"
                    let url = NSURL(string: urlstr)
                    UIApplication.sharedApplication().openURL(url!)
                }
            }
            controller.addAction(action)
        }

        if MFMessageComposeViewController.canSendText() {
            str = NSLocalizedString("Send SMS", comment: "action label")
            action = UIAlertAction(title: str, style: .Default) { action -> Void in
                let textField = controller.textFields![0] as UITextField
                let controller = MFMessageComposeViewController()
                controller.body = textField.text
                controller.messageComposeDelegate = self
                self.navigationController!.presentViewController(controller, animated: true, completion: nil)
            }
            controller.addAction(action)
        }

        if MFMailComposeViewController.canSendMail() {
            str = NSLocalizedString("Send Email", comment: "action label")
            action = UIAlertAction(title: str, style: .Default) { action -> Void in
                let textField = controller.textFields![0] as UITextField
                let controller = MFMailComposeViewController()
                if let subtitle = annotation.subtitle {
                    controller.setSubject(subtitle)
                }
                controller.setMessageBody(textField.text!, isHTML: false)
                controller.mailComposeDelegate = self
                self.navigationController!.presentViewController(controller, animated: true, completion: nil)
            }
            controller.addAction(action)
        }

        if SLComposeViewController.isAvailableForServiceType(SLServiceTypeFacebook) {
            str = NSLocalizedString("Post on Facebook", comment: "action label")
            action = UIAlertAction(title: str, style: .Default) { action -> Void in
                let content = FBSDKShareLinkContent()
                str = Router.WebURLString
                content.contentURL = NSURL(string: str)
                str = NSLocalizedString("Icon-40.png", comment: "fb share image url")
                let url = "\(Router.WebURLString)/images/\(str)"
                content.imageURL  = NSURL(string: url)
                str = NSLocalizedString("cocoger", comment: "fb share content title")
                content.contentTitle = str
                str = NSLocalizedString("share location with comfort", comment: "fb share content description")
                content.contentDescription = str

                let shareDialog = FBSDKShareDialog()
                shareDialog.shareContent = content
                shareDialog.show()
            }
            controller.addAction(action)
        }

        if SLComposeViewController.isAvailableForServiceType(SLServiceTypeTwitter) {
            str = NSLocalizedString("Post on Twitter", comment: "action label")
            action = UIAlertAction(title: str, style: .Default) { action -> Void in
                let messageTextField = controller.textFields![0] as UITextField
                let controller = SLComposeViewController(forServiceType:SLServiceTypeTwitter)
                controller.setInitialText(messageTextField.text)
                self.presentViewController(controller, animated:true, completion:nil)
            }
            controller.addAction(action)
        }

        /*
        let timeElapsed = fabs(friend.statusChecked.timeIntervalSinceNow)
        //print("check status: \(timeElapsed)")
        if timeElapsed == 0 || timeElapsed > (60 * 60) {
            str = NSLocalizedString("Poke", comment: "action label")
            action = UIAlertAction(title: str, style: .Default) { action -> Void in
                friend.statusChecked = NSDate()
                friend.managedObjectContext!.MR_saveToPersistentStoreAndWait()
                Friend.ping(friend.user, completion: nil)
            }
            controller.addAction(action)
        }
        */

#if false
        if annotation.range == LocationRange.Street {
            str = NSLocalizedString("Show Routes", comment: "action label")
            action = UIAlertAction(title: str, style: .Default) { action -> Void in
                self.showRoute(annotation)
            }
            controller.addAction(action)
        }
#endif

        str = NSLocalizedString("Profile", comment: "action label")
        action = UIAlertAction(title: str, style: .Default) { action -> Void in
            self.navigationController!.pushViewController(
                UpdateFriendTableViewController(
                    friend: friend, hasLocation: true,
                    address: annotation.location.address(annotation.range)), animated: true)
        }
        controller.addAction(action)

        if let popover = controller.popoverPresentationController {
            popover.sourceView = self.view
        }

        self.presentViewController(controller, animated: true, completion: nil)
    }

}

extension MapViewController: UISearchBarDelegate {

    func centerizeFriend(friend: String) {
        let text = friend.lowercaseString
        for coordinate in self.friendLocations.keys {
            if let annotations = self.friendLocations[coordinate] {
                for annotation in annotations {
                    if annotation.name.lowercaseString.rangeOfString(text) != nil {
                        mapView.centerCoordinate = annotation.coordinate
                        mapView.zoomLevel = 14
                        return
                    }
                }
            }
        }
    }

    func searchBar(searchBar: UISearchBar, textDidChange searchText: String) {
        centerizeFriend(searchText)
    }

    func searchBarSearchButtonClicked(searchBar: UISearchBar) {
        searchBar.resignFirstResponder()
    }

}

extension MapViewController: MFMailComposeViewControllerDelegate {

    func mailComposeController(controller: MFMailComposeViewController,
                               didFinishWithResult result: MFMailComposeResult, error: NSError?) {
        controller.dismissViewControllerAnimated(true, completion: nil)
    }

}

extension MapViewController: MFMessageComposeViewControllerDelegate {

    func messageComposeViewController(controller: MFMessageComposeViewController,
                                      didFinishWithResult result: MessageComposeResult) {
        self.dismissViewControllerAnimated(true, completion: nil)
    }

}

extension MapViewController: LocationTrackerDelegate {

    func locationAuthorized(status: Bool) {
        if status {
            locationTracker.start(.Precise)

            // observe the core data changes
            NSNotificationCenter.defaultCenter().addObserver(
                self, selector: Selector("handleDataModelChange:"),
                name: NSManagedObjectContextObjectsDidChangeNotification,
                object: NSManagedObjectContext.MR_defaultContext())

            // add the friends annotation
            if User.available {
                if let friends = Friend.MR_findAll() as? [Friend] {
                    for friend in friends {
                        addFriendAnnotation(friend)
                    }
                }
            }
        } else {
            // show the alert, bring the user to the location setting
            let title = NSLocalizedString("Location Access Denied",
                                          comment: "error title for location service not being authorized")
            let message = NSLocalizedString(
                    "Cocoger requires Location Access with Always.  Please update the Settings.",
                    comment: "error message for location service not being authorized")
            UIAlertController.simpleAlert(
                title, message: message,
                completion: {(alert: UIAlertController) -> Void in
                    let settingsUrl = NSURL(string: UIApplicationOpenSettingsURLString)
                    UIApplication.sharedApplication().openURL(settingsUrl!)
                })
        }
    }

    func locationUpdated(location: CLLocation) {
        print("location updated")

        if !isViewLoaded() {
            return
        }

        if firstUserLocation {
            firstUserLocation = false
            mapView.centerCoordinate = location.coordinate
            mapView.zoomLevel = 12
        }

        if homeAnnotation == nil {
            homeAnnotation = HomeAnnotation(coordinate: location.coordinate)
            self.mapView.addAnnotation(homeAnnotation)
        }

        homeAnnotation.coordinate = location.coordinate

#if false
        if circle != nil {
            self.mapView.removeOverlay(circle!)
        }
        circle = MKCircle(centerCoordinate: location.coordinate, radius: 50 as CLLocationDistance)
        self.mapView.addOverlay(circle!)
#endif

        if Location.shouldSave(location) {
            Location.save(location,
                          completion: {[weak self]
                                       (status: Bool, street: String, town: String, city: String,
                                        county: String, state: String, country: String, zip: String) in
                if status {
                    self!.homeAnnotation.address(street, town: town, city: city, county: county,
                                                 state: state, country: country, zip: zip)
                }
            })
        } else if homeAnnotation.subtitle == nil {
            Location.placemark(location,
                               completion: {[weak self]
                                            (status: Bool, street: String, town: String, city: String,
                                             county: String, state: String, country: String, zip: String) in
                if status {
                    self!.homeAnnotation.address(street, town: town, city: city, county: county,
                                                     state: state, country: country, zip: zip)
                }
            })
        }
    }
}

#if false
extension MapViewController {

    func showRoute(friend: FriendAnnotation) {
        let fromPlacemark = MKPlacemark(coordinate: homeAnnotation.coordinate, addressDictionary:nil)
        let toPlacemark   = MKPlacemark(coordinate: friend.coordinate, addressDictionary: nil)

        let fromItem = MKMapItem(placemark: fromPlacemark)
        let toItem   = MKMapItem(placemark: toPlacemark)

        let request = MKDirectionsRequest()
        request.source = fromItem
        request.destination = toItem
        request.requestsAlternateRoutes = true
        request.transportType = MKDirectionsTransportType.Any

        let directions = MKDirections(request:request)

        PKHUD.sharedHUD.show()

        directions.calculateDirectionsWithCompletionHandler({[unowned self] response, error in
            dispatch_async(dispatch_get_main_queue()) {
            PKHUD.sharedHUD.hide(animated: false)
            guard let unwrappedResponse = response else {
                UIAlertController.simpleAlert("Sorry, any route not found")
                return
            }
	    for route in unwrappedResponse.routes {
		self.mapView.addOverlay(route.polyline)
		self.mapView.setVisibleMapRect(route.polyline.boundingMapRect, animated: true)
            }
            }
        })
   }

}
#endif
