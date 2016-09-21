import UIKit

extension String {

    var hexColor: UIColor {
        var cString:String = self.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet()).uppercaseString

        if (cString.hasPrefix("#")) {
            cString = (cString as NSString).substringFromIndex(1)
        }

        if (cString.characters.count != 6) {
            return UIColor.clearColor()
        }

        let rString = (cString as NSString).substringToIndex(2)
        let gString = ((cString as NSString).substringFromIndex(2) as NSString).substringToIndex(2)
        let bString = ((cString as NSString).substringFromIndex(4) as NSString).substringToIndex(2)

        var r:CUnsignedInt = 0, g:CUnsignedInt = 0, b:CUnsignedInt = 0
        NSScanner(string: rString).scanHexInt(&r)
        NSScanner(string: gString).scanHexInt(&g)
        NSScanner(string: bString).scanHexInt(&b)

        return UIColor(red: CGFloat(r) / 255.0, green: CGFloat(g) / 255.0, blue: CGFloat(b) / 255.0, alpha: CGFloat(1))
    }

}


extension String {

    // Handling the Japanese specific naming scheme for "-ku", "-shi", "-ken"
    var stripped: String {
        return self
/*
          .stringByReplacingOccurrencesOfString("-ku", withString: "",
              options: NSStringCompareOptions.CaseInsensitiveSearch, range: nil)
*/
          .stringByReplacingOccurrencesOfString("-shi", withString: "",
              options: NSStringCompareOptions.CaseInsensitiveSearch, range: nil)
          .stringByReplacingOccurrencesOfString("-ken", withString: "",
              options: NSStringCompareOptions.CaseInsensitiveSearch, range: nil)
    }

    var wordList:[String] {
        return componentsSeparatedByCharactersInSet(.punctuationCharacterSet())
          .joinWithSeparator("")
          .componentsSeparatedByString(" ")
    }

}


extension String {

    var isEmail: Bool {
        do {
            let regex = try NSRegularExpression(pattern: "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$", options: .CaseInsensitive)
            return regex.firstMatchInString(self, options: NSMatchingOptions(rawValue: 0), range: NSMakeRange(0, self.characters.count)) != nil
        } catch {
            return false
        }
    }

}
