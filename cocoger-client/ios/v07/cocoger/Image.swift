//
//  Image.swift
//  
//
//  Created by Hiroshi Watanabe on 9/13/15.
//
//

import Foundation
import CoreData
import EasyMapping

@objc(Image)
class Image: EKManagedObjectModel {

    @NSManaged var user: String
    @NSManaged var type: NSNumber
    @NSManaged var name: String
    @NSManaged var url: String
    @NSManaged var data: NSData
    @NSManaged var created: NSDate

}
