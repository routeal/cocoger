import UIKit

class DetailViewController: UIViewController {

    var from: CGRect!

    override func viewDidLoad() {
        super.viewDidLoad()
        //tableView.tableFooterView = UIView(frame: CGRectZero)
        //self.view.backgroundColor = UIColor.redColor()

        let tapGestureRecognizer = UITapGestureRecognizer(target: self, action: "handleTap:")
        view.addGestureRecognizer(tapGestureRecognizer)
    }

    func handleTap(tap: UITapGestureRecognizer) {
        dismissViewControllerAnimated(true, completion: nil)
    }

    @IBAction func dismissView() {
        dismissViewControllerAnimated(true, completion: nil)
    }

    override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return .LightContent
    }
}
