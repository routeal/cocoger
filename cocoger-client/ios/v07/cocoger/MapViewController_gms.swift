//
//  MapViewController.swift
//  SokoraNow
//
//  Created by Hiroshi Watanabe on 7/7/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit
import MagicalRecord
import MessageUI
import Social
import FBSDKShareKit
import GoogleMaps
import SlideMenuControllerSwift

class HomeMarker: GMSMarker {
    weak var mapViewController: MapViewController?

    var location: LocationLight?
    var photoType: UInt = 0
    var photoName: String = ""

    init(mapViewController: MapViewController?) {
        self.mapViewController = mapViewController
        super.init()
    }

    func address(range: Int) -> String {
        if let location = self.location {
            return location.address(range)
        }
        return ""
    }

    func update() {
        if self.title != User.name {
           self.title = User.name
        }
        if self.photoName != User.photoName || self.photoType != User.photoType {
            self.photoName = User.photoName
            self.photoType = User.photoType
            setIcon()
        }
    }

    func setIcon() {
        Image.getImage(self.photoName, type: self.photoType, completion: {[unowned self](image: UIImage?) in
                self.icon = MapViewImage.getImage(image, backgroundColor: User.myColor.hexColor)
            }
        )
    }
}

class FriendMarker: HomeMarker {
    var user: String
    var range: Int = 0
    var gender: Int = 0
    weak var friend: Friend?

    static var friends: [FriendMarker] = []
    static var friendLocations: [CLLocationCoordinate2D: [FriendMarker]] = [:]

    init(mapViewController: MapViewController?, friend: Friend?) {
        // FriendMarker
        self.friend = friend
        self.user = friend!.user
        self.gender = Int(friend!.gender)
        self.range = Int(friend!.range)

        // GMSMarker
        super.init(mapViewController: mapViewController)

        self.title = friend!.name
        self.photoType = UInt(friend!.photoType)
        self.photoName = friend!.photoName

        FriendMarker.friends.append(self)
    }

    func update(friend: Friend) {
        if self.title != friend.name {
            print("update: friend title updated")
            self.title = friend.name
        }

        if self.gender != Int(friend.gender) {
            self.gender = Int(friend.gender)
        }

        if self.range != Int(friend.range) {
            print("update: range updated to \(friend.range)")
            self.range = Int(friend.range)

            if let loc = self.location {
                loc.rangeCoordinate(range, completion: {[weak self](coordinate: CLLocationCoordinate2D?) in
                    if let coordinate = coordinate {
                        print("new coordinate for new range change")
                        if self!.remove() {
                            print("added to the original FriendMarker")
                            if self!.append(coordinate) {
                                self!.map = self!.mapViewController!.mapView
                            }
                        } else {
                            print("added a brand new FriendMarker")
                            let m = FriendMarker(mapViewController: self!.mapViewController, friend: friend)
                            m.append(coordinate)
                            m.map = self!.mapViewController!.mapView
                        }
                    }
                })
            }
        }

        if self.photoName != friend.photoName {
            print("update: \(self.photoName) \(friend.photoName)")
            self.photoType = UInt(friend.photoType)
            self.photoName = friend.photoName
            self.setIcon()
        }
    }

    func getPosition(range: Int, completion: (position: CLLocationCoordinate2D?) -> Void) {
        Friend.position(self.user, completion: {[weak self](status: Bool, location: LocationLight?) in
            if !status || location == nil {
                print("getPosition: Can't get the friend latest coordinate.")
                completion(position: nil)
                return
            }

            self!.location = location
            self!.location!.rangeCoordinate(range, completion: {(coordinate: CLLocationCoordinate2D?) in
                if coordinate != nil {
                    print("new coordinate")
                    completion(position: coordinate)
                } else {
                    print("range coordinate not available - marker won't be added")
                    completion(position: nil)
                }
            })
        })
    }

    class func isValid(marker: FriendMarker) -> Bool {
        if let _ = FriendMarker.friendLocations[marker.position] {
            return true
        }
        return false
    }

    class func find(friend: Friend) -> FriendMarker? {
        for marker in FriendMarker.friends {
            if marker.user == friend.user {
                return marker
            }
        }
        return nil
    }

    class func removeAll() {
        FriendMarker.friendLocations = [:]
        FriendMarker.friends = []
    }

    func append(position: CLLocationCoordinate2D) -> Bool {
        print("append FriendMarker")

        if !FriendMarker.friends.contains(self) {
            FriendMarker.friends.append(self)
        }

        if FriendMarker.friendLocations[position] == nil {
            print("append first FriendMarker")
            FriendMarker.friendLocations[position] = [FriendMarker]()
            FriendMarker.friendLocations[position]!.append(self)

            if self.map == nil {
                print("NEED NEW FriendMarker")
            }

            self.position = position
            self.setIcon()
            // map needs to be assigned

            // add the new coordinate
            return true
        } else {
            print("append \(FriendMarker.friendLocations[position]!.count) FriendMarker")
            FriendMarker.friendLocations[position]!.append(self)
            self.position = position
            var current: FriendMarker?
            for marker in FriendMarker.friendLocations[position]! {
                if marker.map != nil {
                    current = marker
                    break
                }
            }
            if current == nil {
                self.setIcon()
                return true
            } else {
                current!.setIcon()
                return false
            }
        }
    }

    func remove() -> Bool {
        var removed: Bool = false
        if let markers = FriendMarker.friendLocations[self.position] {
            var index: Int?
            for (i, existing) in markers.enumerate() {
                if existing.user == self.user {
                    index = i
                    print("removeMarker: \(i)")
                }
            }
            if let i = index {
                var newmarkers = markers
                newmarkers.removeAtIndex(i)
                print("removeMarker: removed")
                if newmarkers.count == 0 {
                    FriendMarker.friendLocations[self.position] = nil
                    print("removeMarker: size 0")
                    self.icon = nil
                    self.map = nil
                    removed = true
                } else {
                    FriendMarker.friendLocations[self.position] = newmarkers
                    print("removeMarker: size \(newmarkers.count) - group marker not removed")
                    var current: FriendMarker?
                    for marker in newmarkers {
                        if marker.map != nil {
                            current = marker
                        }
                    }
                    if current == nil {
                        print("removeMarker: no current")
                        current = newmarkers[0]
                        current!.icon = getMarkerImage(newmarkers)
                        current!.position = self.position
                        current!.map = self.map
                        self.icon = nil
                        self.map = nil
                        removed = true
                    } else {
                        print("removeMarker: map there")
                        current!.icon = getMarkerImage(newmarkers)
                    }
                }
            } else {
                print("removeMarker: not found")
            }
        } else {
                print("removeMarker: no location")
        }
        if removed {
            if let index = FriendMarker.friends.indexOf(self) {
                FriendMarker.friends.removeAtIndex(index)
            }
        }
        return removed
    }

    override func setIcon() {
        if let friendMarkers = FriendMarker.friendLocations[self.position] {

            var imageLoaded = 0

            func done(image: UIImage?) {
                if ++imageLoaded == friendMarkers.count {
                    //print("imageLoaded=\(imageLoaded)")
                    self.icon = getMarkerImage(friendMarkers)
                }
            }

            for marker in friendMarkers {
                Image.getImage(marker.photoName, type: marker.photoType, completion: done)
            }
        }
    }

    func getMarkerImage(markers: [FriendMarker]) -> UIImage? {
        print("images with \(markers.count)")

        if markers.count == 0 {
            return nil
        }
        // 10 or more users in the same location, show the badge icon
        // which has the number of the users inside and display the
        // list of the users in the callout
        else if markers.count >= MapViewImage.friendImages.count {
            return MapViewImage.getImage("\(markers.count)", backgroundColor: "e6005b".hexColor)
        }
        // 1 to 9 users in the same position, show each icon at the
        // location where is specified by the predefined location
        else {
            var items: [(image: UIImage?, backgroundColor: UIColor)] = []
            for var i = 0; i < markers.count; i++ {
                let marker = markers[i]
                let image = Image.getImage(marker.photoName, type: marker.photoType)
                let color = (marker.gender == 0) ? User.boyColor.hexColor : User.girlColor.hexColor
                items += [(image: image, backgroundColor: color)]
            }
            return MapViewImage.getImage(items)
        }
    }

    class func search(searchText: String) -> CLLocationCoordinate2D? {
        let text = searchText.lowercaseString
        for coordinate in FriendMarker.friendLocations.keys {
            if let markers = FriendMarker.friendLocations[coordinate] {
                for marker in markers {
                    if marker.title.lowercaseString.rangeOfString(text) != nil {
                        return marker.position
                    }
                }
            }
        }
        return nil
    }
}

class HomeInfoView: UIView {
    var panoView: PanoramaView!
    var marker: HomeMarker!

    init(marker: HomeMarker) {
        super.init(frame: CGRectMake(0, 0, 300, 60))

        self.layer.borderWidth = 0.5
        self.layer.cornerRadius = 10.0

        self.marker = marker
        self.backgroundColor = UIColor.whiteColor()

        let placeholder = UILabel(frame: CGRectMake(0, 0, 80, 60))
        placeholder.font = UIFont.systemFontOfSize(UIFont.smallSystemFontSize())
        placeholder.textAlignment = .Center
        //placeholder.backgroundColor = UIColor.blueColor()
        placeholder.text = "no picture"
        placeholder.layer.borderWidth = 0.5
        placeholder.layer.cornerRadius = 10.0
        self.addSubview(placeholder)

        let image = UIImageView(frame: CGRectMake(84, 4, 32, 32))
        image.image = Image.getImage(User.photoName, type: User.photoType)
        self.addSubview(image)

        let name = UILabel(frame: CGRectMake(80+40, 0, 300-80-40-10, 36))
        name.text = User.name
        //name.backgroundColor = UIColor.yellowColor()
        self.addSubview(name)

        let address = UILabel(frame: CGRectMake(80, 36, 300-80-10-4, 24))
        address.font = UIFont.systemFontOfSize(UIFont.smallSystemFontSize())
        address.textAlignment = .Center
        //address.backgroundColor = UIColor.blueColor()
        let text = marker.address(LocationRange.Street)
        let replaced = text.stringByReplacingOccurrencesOfString("\n", withString: " ")
        address.text = replaced
        self.addSubview(address)

        let arrow = UIImageView(frame: CGRectMake(286, 15, 10, 10))
        arrow.image = UIImage(named: "rightarrow")
        self.addSubview(arrow)

        let tapGestureRecognizer = UITapGestureRecognizer(target: self, action: "handleTapGesture:")
        self.addGestureRecognizer(tapGestureRecognizer)

        GMSPanoramaService().requestPanoramaNearCoordinate(
            marker.position, callback: {(panorama: GMSPanorama?, error: NSError?) in
                if let error = error {
                    print(error.localizedDescription)
                } else {
                    self.panoView = PanoramaView(frame: CGRectMake(0, 0, 80, 60))
                    self.panoView.address = replaced
                    self.panoView.position = marker.position
                    self.panoView.navigationController = self.marker.mapViewController!.navigationController!
                    self.addSubview(self.panoView)
                }
            })
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    func handleTapGesture(sender: UITapGestureRecognizer) {
        let address = marker.address(LocationRange.Street)
        let controller = ProfileTableViewController(hasLocation: true, address: address)
        self.marker.mapViewController!.navigationController!.pushViewController(controller, animated: true)
    }
}

class FriendInfoView: UIView {
    var panoView: PanoramaView!
    var marker: FriendMarker!

    init?(marker: FriendMarker) {
        let markers = FriendMarker.friendLocations[marker.position]

        var height: CGFloat = 40
        if markers!.count == 1 {
            super.init(frame: CGRectMake(0, 0, 300, 60))
        } else {
            height = height * CGFloat(markers!.count > 4 ? 4 : markers!.count)
            super.init(frame: CGRectMake(0, 0, 300, height+24))
        }

        self.layer.borderWidth = 0.5
        self.layer.cornerRadius = 10.0

        self.marker = marker
        self.backgroundColor = UIColor.whiteColor()

        let placeholder = UILabel(frame: CGRectMake(0, 0, 80, 60))
        placeholder.font = UIFont.systemFontOfSize(UIFont.smallSystemFontSize())
        placeholder.textAlignment = .Center
        //placeholder.backgroundColor = UIColor.blueColor()
        placeholder.text = "no picture"
        placeholder.layer.borderWidth = 0.5
        placeholder.layer.cornerRadius = 10.0
        self.addSubview(placeholder)

        let layout = UICollectionViewFlowLayout()
        layout.itemSize = CGSizeMake(300-80, 40)
        layout.minimumInteritemSpacing = 0
        layout.minimumLineSpacing = 0.5

        let frame = CGRectMake(80, 0, 300-80, height)
        let collectionView = UICollectionView(frame: frame, collectionViewLayout: layout)
        collectionView.registerClass(UICollectionViewCell.self, forCellWithReuseIdentifier: "id")
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.backgroundColor = UIColor.clearColor()
        self.addSubview(collectionView)

        var addressText: String?

        if markers!.count > 1 {
            collectionView.backgroundColor = UIColor.blackColor()

            let friends = UILabel(frame: CGRectMake(0, 60, 80, height-60))
            friends.font = UIFont.systemFontOfSize(UIFont.smallSystemFontSize())
            friends.textAlignment = .Center
            let fmt = NSLocalizedString("%d friends", comment: "marker callout")
            friends.text = String(format: fmt, markers!.count)
            self.addSubview(friends)

            let address = UILabel(frame: CGRectMake(0, height, 300, 24))
            address.font = UIFont.systemFontOfSize(UIFont.smallSystemFontSize())
            address.textAlignment = .Center
            //address.layer.borderWidth = 0.5
            //address.layer.cornerRadius = 10.0
            //address.backgroundColor = UIColor.blueColor()
            let text = marker.address(marker.range)
            addressText = text.stringByReplacingOccurrencesOfString("\n", withString: " ")
            address.text = addressText!
            self.addSubview(address)
        } else {
            let address = UILabel(frame: CGRectMake(80, 36, 220, 24))
            address.font = UIFont.systemFontOfSize(UIFont.smallSystemFontSize())
            address.textAlignment = .Center
            let text = marker.address(marker.range)
            addressText = text.stringByReplacingOccurrencesOfString("\n", withString: " ")
            address.text = addressText!
            self.addSubview(address)
        }

        GMSPanoramaService().requestPanoramaNearCoordinate(
            marker.position, callback: {(panorama: GMSPanorama?, error: NSError?) in
                if let error = error {
                    print(error.localizedDescription)
                } else {
                    self.panoView = PanoramaView(frame: CGRectMake(0, 0, 80, 60))
                    if addressText != nil {
                        self.panoView.address = addressText
                    }
                    self.panoView.position = marker.position
                    self.panoView.navigationController = self.marker.mapViewController!.navigationController!
                    self.addSubview(self.panoView)
                }
            })
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

}

extension FriendInfoView: UICollectionViewDelegate {

    func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        print("collectionView tapped")

        var selected: FriendMarker?
        if let markers = FriendMarker.friendLocations[marker!.position] {
            selected = markers[indexPath.row]
        }

        if selected != nil {
            if let friend = Friend.MR_findFirstByAttribute("user", withValue: selected!.user) {
                tapFriendCallout(friend, marker: selected!)
            }
        }
    }

    func tapFriendCallout(friend: Friend, marker: FriendMarker) {
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
        if UIApplication.sharedApplication().canOpenURL(NSURL(string: "line://")!) {
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
                self.marker.mapViewController!.navigationController!.presentViewController(controller, animated: true, completion: nil)
            }
            controller.addAction(action)
        }

        if MFMailComposeViewController.canSendMail() {
            str = NSLocalizedString("Send Email", comment: "action label")
            action = UIAlertAction(title: str, style: .Default) { action -> Void in
                let textField = controller.textFields![0] as UITextField
                let controller = MFMailComposeViewController()
                if let subtitle = marker.title {
                    controller.setSubject(subtitle)
                }
                controller.setMessageBody(textField.text!, isHTML: false)
                controller.mailComposeDelegate = self
                self.marker.mapViewController!.navigationController!.presentViewController(controller, animated: true, completion: nil)
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
                self.marker.mapViewController!.navigationController!.presentViewController(controller, animated:true, completion:nil)
            }
            controller.addAction(action)
        }

        /* FIXME: not really working
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

        str = NSLocalizedString("Profile", comment: "action label")
        action = UIAlertAction(title: str, style: .Default) { action -> Void in
            self.marker.mapViewController!.navigationController!.pushViewController(
                UpdateFriendTableViewController(
                    friend: friend, hasLocation: true,
                    address: marker.address(marker.range)), animated: true)
        }
        controller.addAction(action)

        if let popover = controller.popoverPresentationController {
            popover.sourceView = self.marker.mapViewController!.navigationController!.topViewController!.view
        }

        self.marker.mapViewController!.navigationController!.presentViewController(controller, animated: true, completion: nil)
    }

}

extension FriendInfoView: UICollectionViewDataSource {

    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        if let markers = FriendMarker.friendLocations[marker.position] {
            return markers.count
        }
        return 0
    }

    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let cell : UICollectionViewCell = collectionView.dequeueReusableCellWithReuseIdentifier("id", forIndexPath: indexPath)

        var selected: FriendMarker?
        if let markers = FriendMarker.friendLocations[marker!.position] {
            selected = markers[indexPath.row]
        }
        if selected == nil {
            return cell
        }

        cell.contentView.backgroundColor = UIColor.whiteColor() //genderColor(selected!)

        // Note: remove the previous ones for avoiding the repaint problem
        for view in cell.contentView.subviews {
            view.removeFromSuperview()
        }

        // add the image
        let image = Image.getImage(selected!.photoName, type: selected!.photoType)
        let imageView = UIImageView(image: image)
        imageView.frame = CGRectMake(4, 4, 32, 32)
        cell.contentView.addSubview(imageView)

        // add the label
        let labelFrame = CGRectMake(40, 0, 166, 40)
        let label = UILabel(frame: labelFrame)
        label.text = selected!.title
        //label.backgroundColor = UIColor.whiteColor()
        //label.textAlignment = NSTextAlignment.Center
        //label.font = UIFont.systemFontOfSize(UIFont.smallSystemFontSize())
        cell.contentView.addSubview(label)

        let arrow = UIImageView(frame: CGRectMake(206, 15, 10, 10))
        arrow.image = UIImage(named: "rightarrow")
        cell.contentView.addSubview(arrow)

        return cell
    }

}

extension FriendInfoView: MFMailComposeViewControllerDelegate {

    func mailComposeController(controller: MFMailComposeViewController, didFinishWithResult result: MFMailComposeResult, error: NSError?) {
        controller.dismissViewControllerAnimated(true, completion: nil)
    }

}

extension FriendInfoView: MFMessageComposeViewControllerDelegate {

    func messageComposeViewController(controller: MFMessageComposeViewController, didFinishWithResult result: MessageComposeResult) {
        self.marker.mapViewController!.navigationController!.dismissViewControllerAnimated(true, completion: nil)
    }

}

class MapIcon: UIImageView {
    static let SIZE: CGFloat = 42
    static let SPACE: CGFloat = 4
    init(index: Int, image: UIImage?, target: AnyObject?, action: Selector) {
        let y = (MapIcon.SIZE + MapIcon.SPACE) * CGFloat(index)
        super.init(frame: CGRectMake(0, y, MapIcon.SIZE, MapIcon.SIZE))
        self.backgroundColor = UIColor.lightGrayColor()
        self.layer.borderWidth = 1
        self.layer.borderColor = UIColor.whiteColor().CGColor
        self.layer.cornerRadius = 0.6
        self.alpha = 0.7
        self.image = image

        let tap = UITapGestureRecognizer(target: target, action: action)
        tap.numberOfTapsRequired = 1
        self.userInteractionEnabled = true
        self.addGestureRecognizer(tap)
    }
    init(image: UIImage?, target: AnyObject?, action: Selector) {
        super.init(frame: CGRectZero)
        self.translatesAutoresizingMaskIntoConstraints = false
        self.backgroundColor = UIColor.lightGrayColor()
        //self.layer.borderWidth = 1
        //self.layer.borderColor = UIColor.whiteColor().CGColor
        //self.layer.cornerRadius = 0.6
        self.alpha = 0.7
        self.image = image

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

    var mapView: GMSMapView!

    var homeMarker: HomeMarker!

    var searchBar: UIView!
    var search: UISearchBar!

    var didTapMarker: Bool = false

    var infoView: UIView?

    var panoView: PanoramaView?
    var panoViewIsLoading: Bool = false
    var panoViewPosition: CGPoint?

    ////////////////////////////////////////////////////////////////////////////
    // Init and deinit
    ////////////////////////////////////////////////////////////////////////////

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

    ////////////////////////////////////////////////////////////////////////////
    // View Handling
    ////////////////////////////////////////////////////////////////////////////

    override func viewDidLoad() {
        super.viewDidLoad()
        createUI()
        // start the location tracking as soon as the location manager is authorized
        locationTracker.delegate = self
        locationTracker.checkLocationServicesEnabled()
    }

    func createUI() {
        let camera = GMSCameraPosition.cameraWithLatitude(0, longitude: 0, zoom: 12)
        mapView = GMSMapView.mapWithFrame(CGRectZero, camera: camera)
        mapView.translatesAutoresizingMaskIntoConstraints = false
        mapView.delegate = self
        self.view.addSubview(mapView)
        self.view.addLayoutConstraints(["H:|[map]|","V:|[map]|"], views: ["map": mapView])

        searchBar = UIView()
        searchBar.alpha = 0.8
        searchBar.layer.cornerRadius = 5.0
        searchBar.backgroundColor = UIColor.clearColor()
        //searchBar.hidden = true
        searchBar.translatesAutoresizingMaskIntoConstraints = false

        search = UISearchBar()
        search.translatesAutoresizingMaskIntoConstraints = false
        search.placeholder = NSLocalizedString("Search Friends", comment: "search bar")
        search.sizeToFit()
        search.showsScopeBar = true
        search.delegate = self
        search.translucent = true
        searchBar.addSubview(search)

        let settings = MapIcon(image: UIImage(named: "ic_menu_black_48dp"), target: self, action: "settingsTapDetected:")
        searchBar.addSubview(settings)
        let friends = MapIcon(image: UIImage(named: "ic_add_black_48dp"), target: self, action: "friendsTapDetected:")
        searchBar.addSubview(friends)
        searchBar.addLayoutConstraints(["H:|[menu(\(MapIcon.SIZE))]-[search]-[friends(\(MapIcon.SIZE))]|",
                                        "V:|-[search]-|", "V:|-[menu(\(MapIcon.SIZE))]-|", "V:|-[friends(\(MapIcon.SIZE))]-|"],
                                       views: ["menu":settings, "search":search, "friends":friends])

        self.view.addSubview(searchBar)
        self.view.addLayoutConstraints(["H:|-[search]-|", "V:|-18-[search]"], views: ["search":searchBar])

#if false
        // buttons in the toolbar
        let img = UIImage(named: "position.png")
        let btn0 = UIBarButtonItem(image: img, style: .Plain,
                                   target: self, action: "setCurrentPosition:")
        let btn1 = UIBarButtonItem(barButtonSystemItem: .Action,
                                   target: self, action: "startActionMenu:")
        let reload = UIBarButtonItem(barButtonSystemItem: .Refresh,
                                      target: self, action: "refreshMarkers:")
        let space = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.FlexibleSpace,
                                    target: nil, action: nil)
        let items = [btn0, space, reload, space, btn1]
        self.setToolbarItems(items as [UIBarButtonItem], animated: false)
#endif

        var i: Int!
        var height: CGFloat!

        i = 0
        let zoom = UIView()
        zoom.translatesAutoresizingMaskIntoConstraints = false
        let myloc = MapIcon(index: i, image: UIImage(named: "ic_my_location_black_48dp"), target: self, action: "myLocTapDetected:")
        zoom.addSubview(myloc)
        i = i + 2
        let zoomIn = MapIcon(index: i, image: UIImage(named: "ic_add_black_48dp"), target: self, action: "zoomInTapDetected:")
        zoom.addSubview(zoomIn)
        i = i + 1
        let zoomOut = MapIcon(index: i, image: UIImage(named: "ic_remove_black_48dp"), target: self, action: "zoomOutTapDetected:")
        zoom.addSubview(zoomOut)
        i = i + 1
        self.view.addSubview(zoom)
        height = (MapIcon.SIZE + MapIcon.SPACE) * CGFloat(i)
        self.view.addLayoutConstraints(["H:[zoom(\(MapIcon.SIZE))]-12-|", "V:[zoom(\(height))]-44-|"], views: ["zoom":zoom])

        i = 0
        let mapType = UIView()
        mapType.translatesAutoresizingMaskIntoConstraints = false
        let normal = MapIcon(index: i, image: UIImage(named: "ic_traffic_black_48dp"), target: self, action: "normalTapDetected:")
        mapType.addSubview(normal)
        i = i + 1
        let hibrid = MapIcon(index: i, image: UIImage(named: "ic_satellite_black_48dp"), target: self, action: "hybridTapDetected:")
        mapType.addSubview(hibrid)
        i = i + 1
        let terrain = MapIcon(index: i, image: UIImage(named: "ic_terrain_black_48dp"), target: self, action: "terrainTapDetected:")
        mapType.addSubview(terrain)
        i = i + 1
        let refresh = MapIcon(index: i, image: UIImage(named: "ic_refresh_black_48dp"), target: self, action: "refreshTapDetected:")
        mapType.addSubview(refresh)
        i = i + 1
        self.view.addSubview(mapType)
        height = (MapIcon.SIZE + MapIcon.SPACE) * CGFloat(i)
        self.view.addLayoutConstraints(["H:|-12-[type(\(MapIcon.SIZE))]", "V:[type(\(height))]-44-|"], views: ["type":mapType])
    }

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        self.navigationController?.setNavigationBarHidden(true, animated: false)
        self.navigationController?.setToolbarHidden(true, animated: false)
        //self.searchBar.hidden = true
        mapView.delegate = self
    }

    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        popdownPanoramaPreview()
        removeInfoWindow()
        mapView.delegate = nil
    }

}

////////////////////////////////////////////////////////////////////////////
// GMSMapView Delegate
////////////////////////////////////////////////////////////////////////////
extension MapViewController: GMSMapViewDelegate {


    func mapView(mapView: GMSMapView!, willMove gesture: Bool) {
        //print("willMove")
        //searchBar.resignFirstResponder()
    }

    func mapView(mapView: GMSMapView!, didChangeCameraPosition position: GMSCameraPosition?) {
        //print("didChangeCameraPosition")
        popdownPanoramaPreview()
        if !didTapMarker {
            removeInfoWindow()
        }
    }

    func mapView(mapview: GMSMapView!, idleAtCameraPosition position: GMSCameraPosition?) {
        //print("idleAtCameraPosition")
        didTapMarker = false
    }

    func mapView(mapView: GMSMapView!, didTapAtCoordinate coordinate: CLLocationCoordinate2D) {
        //print("You tapped at \(coordinate.latitude), \(coordinate.longitude)")

        popdownPanoramaPreview()

        if removeInfoWindow() {
            // tap to pop down the current info window
            return
        }

        //let hidden = self.navigationController?.toolbarHidden
        /* FIXME: should be replaced
        self.navigationController?.setNavigationBarHidden(!hidden!, animated: false)
        */
//        self.navigationController?.setToolbarHidden(!hidden!, animated: true)
        //searchBar.hidden = !hidden!
        search.resignFirstResponder()

        popupPanoramaPreview(coordinate)
    }

    func mapView(mapView: GMSMapView!, didLongPressAtCoordinate coordinate: CLLocationCoordinate2D) {
        //print("didLongPressAtCoordinate")
    }

    func mapView(mapView: GMSMapView!, didTapMarker marker: GMSMarker!) -> Bool {
        //print("didTapMarker")

        didTapMarker = true

        popdownPanoramaPreview()

        addInfoWindow(marker)

        self.mapView.animateToLocation(marker.position)

        return true
    }

    func mapView(mapView: GMSMapView!, didTapInfoWindowOfMarker marker: GMSMarker!) {
        print("didTapInfoWindowOfMarker")
    }

    func mapView(mapView: GMSMapView!, didTapOverlay overlay: GMSOverlay!) {
        print("didTapOverlay")
    }

    func mapView(mapView: GMSMapView!, markerInfoWindow marker: GMSMarker!) -> UIView? {
        return nil
    }

    func mapView(mapView: GMSMapView!, markerInfoContents marker: GMSMarker!) -> UIView? {
        return nil
    }

    func mapView(mapView: GMSMapView!, didBeginDraggingMarker marker: GMSMarker!) {
        //print("didBeginDraggingMarker")
    }

    func mapView(mapView: GMSMapView!, didEndDraggingMarker marker: GMSMarker!) {
        //print("didEndDraggingMarker")
    }

    func mapView(mapView: GMSMapView!, didDragMarker marker: GMSMarker!) {
        //print("didDragMarker")
    }

    func didTapMyLocationButtonForMapView(mapView: GMSMapView!) -> Bool {
        //print("didTapMyLocationButtonForMapView")
        return false
    }

}

////////////////////////////////////////////////////////////////////////////
// Mapview Controls
////////////////////////////////////////////////////////////////////////////
extension MapViewController {

    func myLocTapDetected(sender: UITapGestureRecognizer) {
        if homeMarker != nil {
            mapView.animateToLocation(homeMarker.position)
        }
    }

    func refreshTapDetected(sender: UITapGestureRecognizer) {
        refresh()
    }

    func refresh() {
        mapView.clear()

        homeMarker.update()
        homeMarker.map = mapView

        FriendMarker.removeAll()

        if let friends = Friend.MR_findAll() as? [Friend] {
            for friend in friends {
                print("refreshMarkers: addFriendMarker")
                addFriendMarker(friend)
            }
        }
    }

    func normalTapDetected(sender: UITapGestureRecognizer) {
        if mapView.mapType != kGMSTypeNormal {
            mapView.mapType = kGMSTypeNormal
            for view in self.view.subviews {
                for v in view.subviews {
                    if let view = v as? MapIcon {
                        view.backgroundColor = UIColor.lightGrayColor()
                        view.layer.borderColor = UIColor.whiteColor().CGColor
                    }
                }
            }
        } else {
            if let view = sender.view as? UIImageView {
                if mapView.trafficEnabled {
                    mapView.trafficEnabled = false
                    view.image = UIImage(named: "ic_traffic_black_48dp")
                } else {
                    mapView.trafficEnabled = true
                    view.image = UIImage(named: "ic_directions_black_48dp")
                }
            }
        }
    }

    func satelliteTapDetected(sender: UITapGestureRecognizer) {
        mapView.mapType = kGMSTypeSatellite
        for view in self.view.subviews {
            for v in view.subviews {
                if let view = v as? MapIcon {
                    view.backgroundColor = UIColor.whiteColor()
                    view.layer.borderColor = UIColor.lightGrayColor().CGColor
                }
            }
        }
    }

    func hybridTapDetected(sender: UITapGestureRecognizer) {
        mapView.mapType = kGMSTypeHybrid
        for view in self.view.subviews {
            for v in view.subviews {
                if let view = v as? MapIcon {
                    view.backgroundColor = UIColor.whiteColor()
                    view.layer.borderColor = UIColor.lightGrayColor().CGColor
                }
            }
        }
    }

    func terrainTapDetected(sender: UITapGestureRecognizer) {
        mapView.mapType = kGMSTypeTerrain
        for view in self.view.subviews {
            for v in view.subviews {
                if let view = v as? MapIcon {
                    view.backgroundColor = UIColor.lightGrayColor()
                    view.layer.borderColor = UIColor.whiteColor().CGColor
                }
            }
        }
    }

    func zoomInTapDetected(sender: UITapGestureRecognizer) {
        let zoomCamera = GMSCameraUpdate.zoomIn()
        mapView.animateWithCameraUpdate(zoomCamera)
    }

    func zoomOutTapDetected(sender: UITapGestureRecognizer) {
        let zoomCamera = GMSCameraUpdate.zoomOut()
        mapView.animateWithCameraUpdate(zoomCamera)
    }

    func settingsTapDetected(sender: UITapGestureRecognizer) {
        self.slideMenuController()?.toggleLeft()
    }

    func friendsTapDetected(sender: UITapGestureRecognizer) {
        //self.slideMenuController()?.toggleRight()
        self.navigationController!.pushViewController(FriendTableViewController(), animated: true)
    }

}

////////////////////////////////////////////////////////////////////////////
// Custom Info Window
////////////////////////////////////////////////////////////////////////////
extension MapViewController {

    func addInfoWindow(marker: GMSMarker) {

        removeInfoWindow()

        if marker is FriendMarker {
            let marker = marker as! FriendMarker
            if FriendMarker.isValid(marker) {
                self.infoView = FriendInfoView(marker: marker)
            } else {
                // marker might be out of sync
                return
            }
        } else {
            self.infoView = HomeInfoView(marker: homeMarker)
        }

        let pos: CGPoint = self.mapView.projection.pointForCoordinate(marker.position)

        self.view.addSubview(infoView!)
        self.infoView!.center = CGPoint(x: pos.x, y: pos.y - infoView!.bounds.height / 2 - 60)

        UIView.animateWithDuration(
            0.5, animations: {
                let height = self.infoView!.bounds.height / 2
                let x = self.mapView.center.x
                var y = self.mapView.center.y - height - 60
                if (y - height) < 0 {
                    y = height
                }
                self.infoView!.center = CGPoint(x: x, y: y)
            }, completion: { (finished: Bool) -> Void in
            }
        )
    }

    func removeInfoWindow() -> Bool {
        if infoView != nil {
            infoView!.hidden = true
            infoView!.removeFromSuperview()
            infoView = nil
            return true
        }
        return false
    }

}

////////////////////////////////////////////////////////////////////////////
// Panorama Street View popup
////////////////////////////////////////////////////////////////////////////
extension MapViewController {

    func popdownPanoramaPreview() {
        if self.panoView != nil {
            self.panoViewPosition = self.panoView!.center
            self.panoView!.hidden = true
            self.panoView!.removeFromSuperview()
            self.panoView = nil
        }
    }

    func popupPanoramaPreview(coordinate: CLLocationCoordinate2D) {
        if panoViewIsLoading {
            return
        }
        panoViewIsLoading = true
        if self.panoView != nil {
            if self.panoView!.position == coordinate {
                return
            }
            popdownPanoramaPreview()
        }

        GMSPanoramaService().requestPanoramaNearCoordinate(
            coordinate, callback: {(panorama: GMSPanorama?, error: NSError?) in
                if let error = error {
                    print(error.localizedDescription)
                } else {
                    let screen = UIScreen.mainScreen().bounds
                    let size = (screen.size.width < screen.size.height) ? screen.size.width : screen.size.height
                    let width = size / 3
                    let height = width * 3 / 4
                    self.panoView = PanoramaView(frame: CGRectMake(0, 0, width, height))
                    self.panoView!.layer.borderWidth = 2
                    self.panoView!.layer.borderColor = UIColor.whiteColor().CGColor
                    self.panoView!.layer.cornerRadius = 2.0
                    self.panoView!.navigationController = self.navigationController
                    self.panoView!.position = coordinate
                    if self.panoViewPosition == nil {
                        let x: CGFloat = width / 2 + 12 + MapIcon.SIZE + 4
                        let y: CGFloat = screen.size.height - height / 2 - 44 - MapIcon.SPACE
                        self.panoView!.center = CGPointMake(x, y)
                    } else {
                        if self.panoViewPosition!.y > screen.size.height {
                            let x: CGFloat = self.panoViewPosition!.x
                            let y: CGFloat = screen.size.height - height / 2 - 44 - MapIcon.SPACE
                            self.panoView!.center = CGPointMake(x, y)
                        } else {
                            self.panoView!.center = self.panoViewPosition!
                        }
                    }
                    self.view.addSubview(self.panoView!)
                }
                self.panoViewIsLoading = false
            })
    }

}

////////////////////////////////////////////////////////////////////////////
// LocationTracker Delegate
////////////////////////////////////////////////////////////////////////////
extension MapViewController: LocationTrackerDelegate {

    func locationAuthorized(status: Bool) {
        if status {
            // might be called twice
            if homeMarker != nil {
                return
            }

            homeMarker = HomeMarker(mapViewController: self)
            homeMarker.update()
            homeMarker.map = mapView

            locationTracker.start(.Precise)

            // observe the core data changes
            NSNotificationCenter.defaultCenter().addObserver(
                self, selector: Selector("handleDataModelChange:"),
                name: NSManagedObjectContextObjectsDidChangeNotification,
                object: NSManagedObjectContext.MR_defaultContext())

            // add the friends marker
            if User.available {
                if let friends = Friend.MR_findAll() as? [Friend] {
                    for friend in friends {
                        print("locationAuthorized: addFriendMarker")
                        addFriendMarker(friend)
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
        if !isViewLoaded() {
            return
        }

        if !CLLocationCoordinate2DIsValid(homeMarker.position) {
            let camera = GMSCameraPosition.cameraWithTarget(location.coordinate, zoom: 10)
            mapView.animateToCameraPosition(camera)
        }

        homeMarker.position = location.coordinate

        func setup(coordinate: CLLocationCoordinate2D, street: String, town: String, city: String,
                   county: String, state: String, country: String, zip: String) {
            self.homeMarker.location = LocationLight()
            self.homeMarker.location!.address(street, town: town, city: city, county: county,
                                                  state: state, country: country, zip: zip)
            self.homeMarker.location!.latitude = coordinate.latitude
            self.homeMarker.location!.longitude = coordinate.longitude
            self.homeMarker.update()
        }

        if Location.shouldSave(location) {
            Location.save(location,
                          completion: {(status: Bool, street: String, town: String, city: String,
                                        county: String, state: String, country: String, zip: String) in
                if status {
                    setup(location.coordinate, street: street, town: town, city: city,
                          county: county, state: state, country: country, zip: zip)
                }
            })
        } else if homeMarker.location == nil {
            Location.placemark(location,
                               completion: {(status: Bool, street: String, town: String, city: String,
                                             county: String, state: String, country: String, zip: String) in
                if status {
                    setup(location.coordinate, street: street, town: town, city: city,
                          county: county, state: state, country: country, zip: zip)
                }
            })
        }

    }

}

////////////////////////////////////////////////////////////////////////////
// Core Data Update
////////////////////////////////////////////////////////////////////////////
extension MapViewController {

    // NOTE: called twice for one operation
    func handleDataModelChange(notification: NSNotification) {
        if let tmp = notification.userInfo {
            if let updated: AnyObject = tmp[NSUpdatedObjectsKey] {
                let asSet = updated as! NSSet
                if let friends = asSet.allObjects as? [Friend] {
                    for friend in friends {
                        print("Friend Updated: \(friend.name)")
                        updateFriendMarker(friend)
                    }
                }
                else if let users = asSet.allObjects as? [User] {
                    for _ in users {
                        homeMarker.update()
                    }
                }
            }
            else if let inserted: AnyObject = tmp[NSInsertedObjectsKey] {
                let asSet = inserted as! NSSet
                if let friends = asSet.allObjects as? [Friend] {
                    for friend in friends {
                        if let _ = FriendMarker.find(friend) {
                            print("Friend already exists")
                        } else {
                            print("Friend Inserted")
                            addFriendMarker(friend)
                        }
                    }
                }
            }
            else if let deleted: AnyObject = tmp[NSDeletedObjectsKey] {
                let asSet = deleted as! NSSet
                if let friends = asSet.allObjects as? [Friend] {
                    for friend in friends {
                        let friendMarker = FriendMarker.find(friend)
                        if friendMarker != nil {
                            print("Friend Deleted")
                            friendMarker!.remove()
                        } else {
                            print("Friend already deleted")
                        }
                    }
                }
            }
        }
    }

}

////////////////////////////////////////////////////////////////////////////
// Marker Handling
////////////////////////////////////////////////////////////////////////////
extension MapViewController {

    func addFriendMarker(friend: Friend) {
        print("addFriendMarker")

        if let _ = FriendMarker.find(friend) {
            print("\(friend.name) marker already exists")
            return
        }

        if friend.range == LocationRange.None {
            return
        }

        let marker = FriendMarker(mapViewController: self, friend: friend)

        marker.getPosition(Int(friend.range), completion: {[weak self](coordinate: CLLocationCoordinate2D?) in
            dispatch_async(dispatch_get_main_queue()) {
                if let coordinate = coordinate {
                    if marker.append(coordinate) {
                        //marker.setIcon()
                        marker.map = self!.mapView
                    }
                }
            }
        })
    }

    func updateFriendMarker(friend: Friend) {
        if let marker = FriendMarker.find(friend) {
            if friend.hasMoved {
                print("updateFriendMarker: moved")
                friend.hasMoved = false
                friend.managedObjectContext!.MR_saveToPersistentStoreAndWait()

                marker.getPosition(Int(friend.range), completion: {[weak self](coordinate: CLLocationCoordinate2D?) in
                    dispatch_async(dispatch_get_main_queue()) {
                        if let coordinate = coordinate {
                            if marker.remove() {
                                if marker.append(coordinate) {
                                    marker.map = self!.mapView
                                }
                            } else {
                                let m = FriendMarker(mapViewController: self!, friend: friend)
                                m.append(coordinate)
                                m.map = self!.mapView
                            }
                        }
                    }
                })
            } else if friend.hasSignedOut {
                print("updateFriendMarker: signedout")
                friend.hasSignedOut = false
                friend.managedObjectContext!.MR_saveToPersistentStoreAndWait()
                dispatch_async(dispatch_get_main_queue()) {
                    marker.remove()
                }
            } else {
                marker.update(friend)
                //marker.map = self.mapView
            }
        } else {
            print("updateFriendMarker not found")
        }
    }

}

////////////////////////////////////////////////////////////////////////////
// User search
////////////////////////////////////////////////////////////////////////////
extension MapViewController: UISearchBarDelegate {

    func searchBar(searchBar: UISearchBar, textDidChange searchText: String) {
        if let position = FriendMarker.search(searchText) {
            mapView.animateToLocation(position)
        }
    }

}

////////////////////////////////////////////////////////////////////////////
// Toolbar commands
////////////////////////////////////////////////////////////////////////////
#if false
extension MapViewController {

    func setCurrentPosition(sender: UIBarButtonItem) {
        if homeMarker != nil {
            mapView.animateToLocation(homeMarker.position)
        }
    }

    func refreshMarkers(sender: UIBarButtonItem) {
        mapView.clear()

        homeMarker.map = mapView

        FriendMarker.removeAll()

        if let friends = Friend.MR_findAll() as? [Friend] {
            for friend in friends {
                print("refreshMarkers: addFriendMarker")
                addFriendMarker(friend)
            }
        }
    }

    func startActionMenu(sender: UIBarButtonItem) {
        self.navigationController?.setNavigationBarHidden(true, animated: false)
//        self.navigationController?.setToolbarHidden(true, animated: false)
        searchBar.hidden = true
        search.resignFirstResponder()

        // NOTE: iOS bug: memory leak
        let controller = UIAlertController(title: nil, message: nil, preferredStyle: .ActionSheet)

        let cancelStr = NSLocalizedString("Cancel", comment: "action label")
        var action = UIAlertAction(title: cancelStr, style: .Cancel, handler: nil)
        controller.addAction(action)

        let nav = self.navigationController!

        if User.available {
            let settingStr = NSLocalizedString("Settings", comment: "action label")
            action = UIAlertAction(title: settingStr, style: .Default) { action -> Void in
                nav.pushViewController(SettingsTableViewController(), animated: true)
            }
            controller.addAction(action)
            let historyStr = NSLocalizedString("Location History", comment: "action label")
            action = UIAlertAction(title: historyStr, style: .Default) { action -> Void in
                nav.pushViewController(LocationHistoryViewController(), animated: true)
            }
            controller.addAction(action)
            let friendStr = NSLocalizedString("Friends", comment: "action label")
            action = UIAlertAction(title: friendStr, style: .Default) { action -> Void in
                nav.pushViewController(FriendTableViewController(), animated: true)
            }
            controller.addAction(action)
            let inviteStr = NSLocalizedString("Invite", comment: "action label")
            action = UIAlertAction(title: inviteStr, style: .Default) { action -> Void in
                nav.pushViewController(InviteTableViewController(), animated: true)
            }
            controller.addAction(action)
        } else {
            let aboutStr = NSLocalizedString("About", comment: "action label")
            action = UIAlertAction(title: aboutStr, style: .Default) { action -> Void in
                let controller = WebViewController()
                controller.title = aboutStr
                controller.url = "\(Router.WebURLString)/m/about.html?lang=\(UIDevice.lang)"
                self.navigationController!.pushViewController(controller, animated: true)
            }
            controller.addAction(action)
            let faqStr = NSLocalizedString("FAQ", comment: "action label")
            action = UIAlertAction(title: faqStr, style: .Default) { action -> Void in
                let controller = WebViewController()
                controller.title = faqStr
                controller.url = "\(Router.WebURLString)/m/faq.html?lang=\(UIDevice.lang)"
                self.navigationController!.pushViewController(controller, animated: true)
            }
            controller.addAction(action)
            let signInUpStr = NSLocalizedString("Sign In or Up", comment: "action label")
            action = UIAlertAction(title: signInUpStr, style: .Destructive) { action -> Void in
                nav.pushViewController(SignInTableViewController(), animated: true)
            }
            controller.addAction(action)
        }

        // FIXME: misplaced in the ipad
        if let popover = controller.popoverPresentationController {
            popover.barButtonItem = sender
            popover.permittedArrowDirections = UIPopoverArrowDirection.Up
        }

        self.presentViewController(controller, animated: true, completion: nil)
    }

}
#endif
