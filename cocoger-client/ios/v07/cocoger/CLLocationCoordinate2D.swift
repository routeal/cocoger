import CoreLocation

extension CLLocationCoordinate2D: Hashable {
    public var hashValue: Int {
        let hash =  Int((latitude >= longitude) ?
                   (latitude * latitude + latitude + longitude) * 1000.0 :
                   (latitude + longitude * longitude) * 1000.0)
        return hash
    }
}

public func ==(lhs: CLLocationCoordinate2D, rhs: CLLocationCoordinate2D) -> Bool {
    return lhs.latitude == rhs.latitude && lhs.longitude == rhs.longitude
}

