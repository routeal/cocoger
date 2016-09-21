import MagicalRecord

let LocationRange = LocationRangeImpl()

class LocationRangeImpl {
    static let MAX = 32

    let Street  : Int = 3
    let Town    : Int = 6
    let City    : Int = 9
    let County  : Int = 12
    let State   : Int = 15
    let Country : Int = 18
    let None    : Int = 21

    var label: [String]!

    var count: Int {
        return self.label.count
    }

    init() {
        if let obj = LocationRangeData.MR_findAll() {
            if obj.count > 0 {
                setup(obj[0] as! LocationRangeData)
            }
        }
    }

    func setup(data: Dictionary<String, AnyObject>) {
        LocationRangeData.MR_truncateAll()
        let range = LocationRangeData.objectWithProperties(
                        data, inContext: NSManagedObjectContext.MR_defaultContext())
        range.managedObjectContext!.MR_saveToPersistentStoreAndWait()
        self.setup(range)
    }

    func setup(range: LocationRangeData) {
        label = [String]()

        if Int(range.street) <  LocationRangeImpl.MAX {
            label.append(NSLocalizedString("Street", comment: "location range"))
        }
        if Int(range.town) < LocationRangeImpl.MAX {
            label.append(NSLocalizedString("Town", comment: "location range"))
        }
        if Int(range.city) < LocationRangeImpl.MAX {
            label.append(NSLocalizedString("City", comment: "location range"))
        }
        if Int(range.county) < LocationRangeImpl.MAX {
            label.append(NSLocalizedString("County", comment: "location range"))
        }
        if Int(range.state) < LocationRangeImpl.MAX {
            label.append(NSLocalizedString("State", comment: "location range"))
        }
        if Int(range.country) < LocationRangeImpl.MAX {
            label.append(NSLocalizedString("Country", comment: "location range"))
        }
        if Int(range.none) < LocationRangeImpl.MAX {
            label.append(NSLocalizedString("None", comment: "location range"))
        }
    }

    func position(range: Int) -> Int? {
        if range == Street {
            return label.indexOf(NSLocalizedString("Street", comment: "location range"))
        }
        else if range == Town {
            return label.indexOf(NSLocalizedString("Town", comment: "location range"))
        }
        else if range == City {
            return label.indexOf(NSLocalizedString("City", comment: "location range"))
        }
        else if range == County {
            return label.indexOf(NSLocalizedString("County", comment: "location range"))
        }
        else if range == State {
            return label.indexOf(NSLocalizedString("State", comment: "location range"))
        }
        else if range == Country {
            return label.indexOf(NSLocalizedString("Country", comment: "location range"))
        }
        else if range == None {
            return label.indexOf(NSLocalizedString("None", comment: "location range"))
        }
        return nil
    }

    func value(position: Int) -> Int? {
        if position < 0 || position >= self.label.count {
            return nil
        }
        let v = self.label[position]
        if v == NSLocalizedString("Street", comment: "location range") {
            return Street
        }
        if v == NSLocalizedString("Town", comment: "location range") {
            return Town
        }
        if v == NSLocalizedString("City", comment: "location range") {
            return City
        }
        if v == NSLocalizedString("County", comment: "location range") {
            return County
        }
        if v == NSLocalizedString("State", comment: "location range") {
            return State
        }
        if v == NSLocalizedString("Country", comment: "location range") {
            return Country
        }
        if v == NSLocalizedString("None", comment: "location range") {
            return None
        }
        return nil
    }

}
