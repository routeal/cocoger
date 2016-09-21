//
//  AddressLocation+CoreDataProperties.swift
//  
//
//  Created by Hiroshi Watanabe on 10/30/15.
//
//
//  Choose "Create NSManagedObject Subclass…" from the Core Data editor menu
//  to delete and recreate this implementation file for your updated model.
//

import Foundation
import CoreData

extension AddressLocation {

    @NSManaged var address: String?
    @NSManaged var latitude: NSNumber?
    @NSManaged var longitude: NSNumber?
    @NSManaged var reference: NSNumber?

}
