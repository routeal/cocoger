//
//  InputTableViewCell.swift
//  SokoraNow
//
//  Created by Hiroshi Watanabe on 7/6/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit

class InputTableViewCell: UITableViewCell {

    var label: UILabel!
    var input: UITextField!

    override init(style: UITableViewCellStyle, reuseIdentifier: String!) {
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseIdentifier)

        self.accessoryType = .None
        self.selectionStyle = .None

        label = UILabel()
        //label.font = UIFont.systemFontOfSize(UIFont.labelFontSize())
        label.translatesAutoresizingMaskIntoConstraints = false
        self.contentView.addSubview(label)

        input = UITextField()
        //input.font = UIFont.systemFontOfSize(UIFont.labelFontSize())
        input.clearButtonMode = .WhileEditing
        input.autocapitalizationType = .None
        input.autocorrectionType = .No
        input.translatesAutoresizingMaskIntoConstraints = false
        self.contentView.addSubview(input)

        let views = ["label":label, "input":input]
        let constraints = ["V:|-[label]-|","V:|-[input]-|","H:|-[label(100)]-[input]|"]
        self.contentView.addLayoutConstraints(constraints, views: views)
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    deinit {
        print("deinit: InputTableViewCell")
    }
}
