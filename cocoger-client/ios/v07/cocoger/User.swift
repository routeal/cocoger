//
//  User.swift
//
//
//  Created by Hiroshi Watanabe on 9/2/15.
//
//

import Foundation
import CoreData
import EasyMapping

@objc(User)
class User: EKManagedObjectModel {

    deinit {
        //print("deinit: User")
    }

    @NSManaged var _authToken: String
    @NSManaged var _bod: NSNumber
    @NSManaged var _created: NSDate
    @NSManaged var _email: String
    @NSManaged var _gender: NSNumber
    @NSManaged var _id: String
    @NSManaged var _name: String
    @NSManaged var _photoName: String
    @NSManaged var _photoType: NSNumber
    @NSManaged var _providerID: String
    @NSManaged var _providerToken: String
    @NSManaged var _revised: NSDate

    @NSManaged var _latitude: NSNumber
    @NSManaged var _longitude: NSNumber

    @NSManaged var _device: String

    @NSManaged var _myColor: String
    @NSManaged var _boyColor: String
    @NSManaged var _girlColor: String
    @NSManaged var _frameColor: String

    @NSManaged var _provider: String

}
