//
//  RawLocation+CoreDataProperties.swift
//  
//
//  Created by Hiroshi Watanabe on 10/29/15.
//
//
//  Choose "Create NSManagedObject Subclass…" from the Core Data editor menu
//  to delete and recreate this implementation file for your updated model.
//

import Foundation
import CoreData

extension RawLocation {

    @NSManaged var altitude: NSNumber?
    @NSManaged var latitude: NSNumber?
    @NSManaged var longitude: NSNumber?
    @NSManaged var speed: NSNumber?
    @NSManaged var timestamp: NSDate?
    @NSManaged var hAccuracy: NSNumber?
    @NSManaged var vAccuracy: NSNumber?
    @NSManaged var course: NSNumber?

}
