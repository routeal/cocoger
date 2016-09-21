import UIKit

extension NSDate {

    var dateString: String {
        let formatter = NSDateFormatter()
        formatter.dateStyle = NSDateFormatterStyle.LongStyle
        return formatter.stringFromDate(self)
    }

    var timeString: String {
        let formatter = NSDateFormatter()
        formatter.timeStyle = NSDateFormatterStyle.ShortStyle
        return formatter.stringFromDate(self)
    }

    func isSameDate(date: NSDate) -> Bool {
        let calendar = NSCalendar.currentCalendar()
        let timeZone = NSTimeZone.localTimeZone()
        calendar.timeZone = timeZone
        let firstDate = calendar.components([.Year, .Month, .Day], fromDate: self)
        let secondDate = calendar.components([.Year, .Month, .Day], fromDate: date)
        return firstDate.day == secondDate.day
    }

}

extension NSDate {

    class func dateMonthFromNow(month: Int) -> NSDate {
        let dc:NSDateComponents = NSDateComponents()
        dc.second = 0
        dc.minute = 0
        dc.hour = 0
        dc.day = 0
        dc.weekOfYear = 0
        dc.month = month
        dc.year = 0
        return NSCalendar.currentCalendar().dateByAddingComponents(dc, toDate: NSDate(), options: NSCalendarOptions(rawValue: 0))!
    }

}
