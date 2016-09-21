//
//  LocationRangeData.swift
//  
//
//  Created by Hiroshi Watanabe on 9/30/15.
//
//

import Foundation
import CoreData
import EasyMapping

@objc(LocationRangeData)
class LocationRangeData: EKManagedObjectModel {

    @NSManaged var street: NSNumber
    @NSManaged var town: NSNumber
    @NSManaged var city: NSNumber
    @NSManaged var county: NSNumber
    @NSManaged var state: NSNumber
    @NSManaged var country: NSNumber
    @NSManaged var none: NSNumber

    override class func objectMapping() -> EKManagedObjectMapping {
        let mapping = EKManagedObjectMapping(entityName: "LocationRangeData")

        mapping.mapPropertiesFromDictionary(["street":"street"])
        mapping.mapPropertiesFromDictionary(["town":"town"])
        mapping.mapPropertiesFromDictionary(["city":"city"])
        mapping.mapPropertiesFromDictionary(["county":"county"])
        mapping.mapPropertiesFromDictionary(["state":"state"])
        mapping.mapPropertiesFromDictionary(["country":"country"])
        mapping.mapPropertiesFromDictionary(["none":"none"])

        return mapping
    }
}
