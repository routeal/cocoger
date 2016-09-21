//
//  InputTableViewCell.swift
//  SokoraNow
//
//  Created by Hiroshi Watanabe on 7/6/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit

class FriendTableViewCell: UITableViewCell {

    var nimage: UIImageView!
    var name: UILabel!
    var email: UILabel!
    var range: LocationRangeView!

    override init(style: UITableViewCellStyle, reuseIdentifier: String!) {
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseIdentifier)

        self.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
        self.selectionStyle = .Default

        nimage = UIImageView()
        nimage.translatesAutoresizingMaskIntoConstraints = false
        self.contentView.addSubview(nimage)

        name = UILabel(frame: CGRectZero)
        name.textAlignment = .Left
        name.translatesAutoresizingMaskIntoConstraints = false
        self.contentView.addSubview(name)

        range = LocationRangeView(frame: CGRectZero)
        //range.backgroundColor = UIColor.redColor()
        range.barMargin = 0
        range.size = 0
        //range.noneHidden = false
        range.labelHidden = true
        range.selectable = false
        range.translatesAutoresizingMaskIntoConstraints = false
        self.contentView.addSubview(range)

        let views = ["name":name, "range":range, "image": nimage]

        let constraints = ["H:|-[image(24)]-[name(>=120)]-[range(80)]|",
                           "V:|-10-[image(24)]",
                           "V:|-[name]-|",
                           "V:|-[range]-|"]

        self.contentView.addLayoutConstraints(constraints, views: views)
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    deinit {
        print("deinit: FriendTableViewCell")
    }
}
