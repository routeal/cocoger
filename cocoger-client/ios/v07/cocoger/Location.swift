//
//  Location.swift
//  
//
//  Created by Hiroshi Watanabe on 8/27/15.
//
//

import Foundation
import CoreData
import EasyMapping

@objc(Location)
class Location: EKManagedObjectModel {
    @NSManaged var accuracy: NSNumber
    @NSManaged var altitude: NSNumber
    @NSManaged var city: String
    @NSManaged var country: String
    @NSManaged var county: String
    @NSManaged var created: NSDate
    @NSManaged var latitude: NSNumber
    @NSManaged var longitude: NSNumber
    @NSManaged var speed: NSNumber
    @NSManaged var state: String
    @NSManaged var street: String
    @NSManaged var town: String
    @NSManaged var timezone: String
    @NSManaged var user: String
    @NSManaged var device: String
    @NSManaged var zip: String
}
