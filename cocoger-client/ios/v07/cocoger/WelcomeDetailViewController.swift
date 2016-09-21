import UIKit
import PKHUD

class WelcomeDetailViewController: UIViewController, UIWebViewDelegate {

    lazy var web: UIWebView = UIWebView()

    var from: CGRect!

    var mUrl: String?

    var url: String? {
        get {
            return  mUrl
        }
        set(url) {
            mUrl = url
            if mUrl != nil {
                web.loadRequest(NSURLRequest(URL: NSURL(string: mUrl!)!))
            }
        }
    }

    required init(coder: NSCoder) {
        fatalError("NSCoding not supported")
    }

    override init(nibName nibNameOrNil: String!, bundle nibBundleOrNil: NSBundle!) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    deinit {
        print("deinit: WelcomeDetailViewController")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationItem.leftBarButtonItem = UIBarButtonItem(title: "Back", style: .Plain, target: self, action: "cancel")
        web.delegate = self
        web.userInteractionEnabled = true
        web.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(web)
        self.view.addLayoutConstraints(["H:|[web]|", "V:|[web]|"], views: ["web":web])
    }

    override func viewDidDisappear(animated: Bool) {
        super.viewDidDisappear(animated)
        PKHUD.sharedHUD.hide(animated: false)
    }

    func webView(webView: UIWebView, didFailLoadWithError error: NSError?) {
        print("Webview fail with error \(error)")
        PKHUD.sharedHUD.hide(animated: false)
    }

    func webViewDidStartLoad(webView: UIWebView) {
        PKHUD.sharedHUD.show()
    }

    func webViewDidFinishLoad(webView: UIWebView) {
        PKHUD.sharedHUD.hide(animated: false)
    }

    func cancel() {
        navigationController!.popViewControllerAnimated(false)
        dismissViewControllerAnimated(true, completion: nil)
    }

}
