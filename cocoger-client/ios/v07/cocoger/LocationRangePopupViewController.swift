import MJPopupViewController

class LocationRangePopupViewController: UIViewController {

    required init(coder: NSCoder) {
        fatalError("NSCoding not supported")
    }

    override init(nibName nibNameOrNil: String!, bundle nibBundleOrNil: NSBundle!) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    deinit {
        NSNotificationCenter.defaultCenter().removeObserver(
            self, name: UIDeviceOrientationDidChangeNotification, object: nil)

        print("deinit: LocationRangePopupViewController")
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    var nameLabel: UILabel = UILabel()
    var textView: UITextView = UITextView()
    var locationRange: LocationRangeView = LocationRangeView(frame: CGRectZero)

    var name: String {
        set(v) {
            nameLabel.text = v
        }
        get {
            return nameLabel.text!
        }
    }

    var message: String {
        set(v) {
            textView.text = v
        }
        get {
            return textView.text
        }
    }

    var range: Int {
        set(v) {
            locationRange.value = v
        }
        get {
            return locationRange.value
        }
    }

    var ok = UIButton(type: .System)

    var okLabel: String {
        set(v) {
            ok.setTitle(v, forState: UIControlState.Normal)
        }
        get {
            if ok.titleLabel == nil || ok.titleLabel!.text == nil {
                return ""
            }
            return ok.titleLabel!.text!
        }
    }

    var cancel = UIButton(type: .System)

    var cancelLabel: String {
        set(v) {
            cancel.setTitle(v, forState: UIControlState.Normal)
        }
        get {
            if cancel.titleLabel == nil || cancel.titleLabel!.text == nil {
                return ""
            }
            return cancel.titleLabel!.text!
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        NSNotificationCenter.defaultCenter().addObserver(
            self, selector: "resized", name: UIDeviceOrientationDidChangeNotification, object: nil)

        resized()

        let pane = UIView(frame: CGRectZero)
        //pane.alpha = 0.9
        pane.layer.cornerRadius = 5
        pane.layer.masksToBounds = true
        // FIXME: white is a default color???
        pane.backgroundColor = UIColor.whiteColor()
        pane.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(pane)
        self.view.addLayoutConstraints(["H:|-[pane]-|","V:|-[pane]-|"], views: ["pane": pane])

        let label = UILabel()
        label.text = self.title
        label.textAlignment = .Center
        label.translatesAutoresizingMaskIntoConstraints = false
        label.font = UIFont.boldSystemFontOfSize(UIFont.labelFontSize())
        self.view.addSubview(label)

        nameLabel.textAlignment = .Center
        nameLabel.translatesAutoresizingMaskIntoConstraints = false
        nameLabel.font = UIFont.boldSystemFontOfSize(UIFont.labelFontSize())
        self.view.addSubview(nameLabel)

        textView.alpha = 0.9
        textView.selectable = false
        textView.editable = false
        textView.font = UIFont.systemFontOfSize(UIFont.labelFontSize())
        textView.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(textView)

        //locationRange.noneHidden = false
        locationRange.lowerValue = false
        locationRange.alertIndicator = true
        locationRange.selectable = true
        locationRange.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(locationRange)

        let sep = UILabel()
        sep.alpha = 0.9
        sep.translatesAutoresizingMaskIntoConstraints = false
        sep.backgroundColor = UIColor.lightGrayColor()
        self.view.addSubview(sep)

        //cancel.backgroundColor = self.view.tintColor
        if cancel.titleLabel == nil || cancel.titleLabel!.text == nil || cancel.titleLabel!.text!.isEmpty {
            let cancelStr = NSLocalizedString("Cancel", comment: "label title")
            cancel.setTitle(cancelStr, forState: UIControlState.Normal)
        }
        cancel.addTarget(self, action: "cancelPressed:", forControlEvents: UIControlEvents.TouchUpInside)
        cancel.titleLabel!.font = UIFont.systemFontOfSize(UIFont.labelFontSize())
        cancel.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(cancel)

        //ok.backgroundColor = self.view.tintColor
        if ok.titleLabel == nil || ok.titleLabel!.text == nil || ok.titleLabel!.text!.isEmpty {
            let okStr = NSLocalizedString("OK", comment: "label title")
            ok.setTitle(okStr, forState: UIControlState.Normal)
        }
        ok.addTarget(self, action: "okPressed:", forControlEvents: UIControlEvents.TouchUpInside)
        ok.titleLabel!.font = UIFont.systemFontOfSize(UIFont.labelFontSize())
        ok.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(ok)

        self.view.addLayoutConstraints(["H:|-[label]-|",
                                        "H:|-[name]-|",
                                        "H:|-32-[text]-32-|",
                                        "H:|-[range]-|",
                                        "H:|-[sep]-|",
                                        "H:|-[cancel]-[ok(==cancel)]-|",
                                        "V:|-16-[label][name]-[text]-16-[range(38)]-[sep(1)]-[cancel]-16-|",
                                        "V:|-16-[label][name]-[text]-16-[range(38)]-[sep(1)]-[ok]-16-|"],
                                       views: ["label":label, "name":nameLabel, "range":locationRange,
                                               "text":textView, "ok":ok, "cancel":cancel, "sep":sep])
    }

    func cancelPressed(sender: UIButton!) {
        if let view = LocationRangePopupViewController.Static.startView {
            view.dismissPopupViewControllerWithanimationType(MJPopupViewAnimationFade)
            LocationRangePopupViewController.Static.startView = nil
        }
        if done != nil {
            done!(status: false, range: locationRange.value)
        }

    }

    func okPressed(sender: UIButton!) {
        if let view = LocationRangePopupViewController.Static.startView {
            view.dismissPopupViewControllerWithanimationType(MJPopupViewAnimationFade)
            LocationRangePopupViewController.Static.startView = nil
        }
        if done != nil {
            done!(status: true, range: locationRange.value)
        }
    }

    func resized() {
        let screen = UIScreen.mainScreen().bounds
        let width = screen.size.width * 9 / 10
        let height = screen.size.height * 2 / 3
        let x = (screen.size.width - width) / 2
        let y = (screen.size.height - height) / 2
        self.view.frame = CGRectMake(x, y, width, height)
    }

    typealias ResponseCallback = (status: Bool, range: Int) -> Void

    var done: ResponseCallback?

    struct Static {
        static var startView: UIViewController?
    }

    func popup(completion: ((status: Bool, range: Int) -> Void)) {
        if LocationRangePopupViewController.Static.startView != nil {
            return
        }
        if let view = UIViewController.viewController() {
            self.done = completion
            LocationRangePopupViewController.Static.startView = view
            view.presentModalPopupViewController(self, animationType:MJPopupViewAnimationSlideBottomBottom)
        }
    }
}
