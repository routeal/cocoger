//
//  Friend.swift
//  
//
//  Created by Hiroshi Watanabe on 9/21/15.
//
//

import Foundation
import CoreData
import EasyMapping

@objc(Friend)
class Friend: EKManagedObjectModel {

    @NSManaged var range: NSNumber
    @NSManaged var name: String
    @NSManaged var gender: NSNumber
    @NSManaged var photoName: String
    @NSManaged var photoType: NSNumber
    @NSManaged var created: NSDate
    @NSManaged var user: String
    @NSManaged var provider: String
    @NSManaged var providerID: String
    @NSManaged var hasMoved: Bool
    @NSManaged var hasSignedOut: Bool
    @NSManaged var statusChecked: NSDate

}
