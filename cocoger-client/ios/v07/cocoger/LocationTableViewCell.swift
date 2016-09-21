//
//  InputTableViewCell.swift
//  SokoraNow
//
//  Created by Hiroshi Watanabe on 7/6/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit

class LocationTableViewCell: UITableViewCell {

    override init(style: UITableViewCellStyle, reuseIdentifier: String!) {
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseIdentifier)

        self.accessoryType = .None
        self.selectionStyle = .None

        let label = UILabel()
        //label.font = UIFont.systemFontOfSize(UIFont.labelFontSize())
        label.translatesAutoresizingMaskIntoConstraints = false
        label.text = NSLocalizedString("Location", comment: "tableview label")
        self.contentView.addSubview(label)

        self.textLabel!.lineBreakMode = NSLineBreakMode.ByWordWrapping
        self.textLabel!.numberOfLines = 0
        self.textLabel!.translatesAutoresizingMaskIntoConstraints = false

        //self.textLabel.font = UIFont.systemFontOfSize(UIFont.labelFontSize())

        let views = ["label":label, "text":self.textLabel!]
        let constraints = [
            "H:|-[label(90)]-[text]-|",
            "V:|-[label]-|",
            "V:|[text]|",
        ]
        self.contentView.addLayoutConstraints(constraints, views: views)
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    deinit {
        print("deinit: LocationTableViewCell")
    }

    override func layoutSubviews() {
        super.layoutSubviews()

        self.contentView.setNeedsLayout()
        self.contentView.layoutIfNeeded()

        self.textLabel!.preferredMaxLayoutWidth = CGRectGetWidth(self.textLabel!.frame)
    }

}
