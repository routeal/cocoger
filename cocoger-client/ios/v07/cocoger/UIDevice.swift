import UIKit
import DeviceGuru

extension UIDevice {

    class var simulator: Bool {
        return DeviceGuru.hardware() == .SIMULATOR
    }

    class var lang: String {
        return NSLocale.preferredLanguages().first!
    }

    class var country: String {
        let defaults = NSUserDefaults.standardUserDefaults()
        if let code = defaults.stringForKey("ISOcountryCode") {
            return code
        }
        return NSLocale.currentLocale().objectForKey(NSLocaleCountryCode) as! String
    }

    class var hardwareString: String {
        return DeviceGuru.hardwareString()
    }
}
