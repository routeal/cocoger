//
//  FaqViewController.swift
//  sokora
//
//  Created by Hiroshi Watanabe on 8/30/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit
import PKHUD

class WebViewController: UIViewController {

    lazy var web: UIWebView = UIWebView()

    var url: String? {
        get {
            return nil
        }
        set(url) {
            web.loadRequest(NSURLRequest(URL: NSURL(string: url!)!))
        }
    }

    required init(coder: NSCoder) {
        fatalError("NSCoding not supported")
    }

    override init(nibName nibNameOrNil: String!, bundle nibBundleOrNil: NSBundle!) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    deinit {
        print("deinit: WebViewController")
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        web.delegate = self
        web.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(web)
        self.view.addLayoutConstraints(["H:|[web]|", "V:|[web]|"], views: ["web":web])
    }

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        // disable the navigation bar
        self.navigationController?.setNavigationBarHidden(false, animated: false)
        self.navigationController?.setToolbarHidden(true, animated: false)
    }

    override func viewDidDisappear(animated: Bool) {
        super.viewDidDisappear(animated)
    }

}

extension WebViewController: UIWebViewDelegate {

    func webView(webView: UIWebView, didFailLoadWithError error: NSError?) {
        print("Webview fail with error \(error)")
        PKHUD.sharedHUD.hide(animated: false)
        navigationController?.popViewControllerAnimated(true)
        UIAlertController.simpleAlert("\(error)")
    }

    func webViewDidStartLoad(webView: UIWebView) {
        print("Webview started Loading")
        PKHUD.sharedHUD.show()
    }

    func webViewDidFinishLoad(webView: UIWebView) {
        print("Webview did finish load")
        PKHUD.sharedHUD.hide(animated: false)
    }

}
