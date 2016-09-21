//
//  InputTableViewCell.swift
//  SokoraNow
//
//  Created by Hiroshi Watanabe on 7/6/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit

class ColorChooserTableViewCell: UITableViewCell, FrameChooserViewDelegate {

    var myChooser: ColorChooserView!
    var girlChooser: ColorChooserView!
    var boyChooser: ColorChooserView!
    var frameChooser: FrameChooserView!

    var myColor: String {
        set(s) {
            myChooser.value = s
        }
        get {
            return myChooser.value
        }
    }

    var girlColor: String {
        set(s) {
            girlChooser.value = s
        }
        get {
            return girlChooser.value
        }
    }

    var boyColor: String {
        set(s) {
            boyChooser.value = s
        }
        get {
            return boyChooser.value
        }
    }

    var frameColor: String {
        set(s) {
            frameChooser.value = s
            myChooser.frameColor = s
            girlChooser.frameColor = s
            boyChooser.frameColor = s
        }
        get {
            return frameChooser.value
        }
    }

    override init(style: UITableViewCellStyle, reuseIdentifier: String!) {
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseIdentifier)

        self.accessoryType = .None
        self.selectionStyle = .None

        let bg = UIColor(red:   CGFloat(0xf9)/255,
                         green: CGFloat(0xf5)/255,
                         blue:  CGFloat(0xed)/255,
                         alpha: 1.0)

        let label1 = UILabel()
        label1.translatesAutoresizingMaskIntoConstraints = false
        label1.text = NSLocalizedString("You", comment: "tableview label")
        self.contentView.addSubview(label1)

        myChooser = ColorChooserView(frame: CGRectZero)
        myChooser.backgroundColor = bg
        myChooser.translatesAutoresizingMaskIntoConstraints = false
        myChooser.image = Image.getImage(User.photoName, type: User.photoType)
        self.contentView.addSubview(myChooser)

        let label2 = UILabel()
        label2.translatesAutoresizingMaskIntoConstraints = false
        label2.text = NSLocalizedString("Girls", comment: "tableview label")
        self.contentView.addSubview(label2)

        girlChooser = ColorChooserView()
        girlChooser.backgroundColor = bg
        girlChooser.translatesAutoresizingMaskIntoConstraints = false
        girlChooser.image = Image.getImage(named: "girl.png")
        self.contentView.addSubview(girlChooser)

        let label3 = UILabel()
        label3.translatesAutoresizingMaskIntoConstraints = false
        label3.text = NSLocalizedString("Boys", comment: "tableview label")
        self.contentView.addSubview(label3)

        boyChooser = ColorChooserView()
        boyChooser.backgroundColor = bg
        boyChooser.translatesAutoresizingMaskIntoConstraints = false
        boyChooser.image = Image.getImage(named: "boy.png")
        self.contentView.addSubview(boyChooser)

        let label4 = UILabel()
        label4.translatesAutoresizingMaskIntoConstraints = false
        label4.text = NSLocalizedString("Round", comment: "tableview label")
        self.contentView.addSubview(label4)

        frameChooser = FrameChooserView()
        frameChooser.delegate = self
        frameChooser.backgroundColor = bg
        frameChooser.translatesAutoresizingMaskIntoConstraints = false
        frameChooser.image = Image.getImage(named: "frame.png")
        self.contentView.addSubview(frameChooser)

        let views = ["label1":label1, "label2":label2, "label3":label3, "label4":label4,
                     "me":myChooser, "girl":girlChooser, "boy":boyChooser, "frame":frameChooser]

        let constraints = [
            "H:|-[label1(60)][me]|",
            "H:|-[label2(60)][girl]|",
            "H:|-[label3(60)][boy]|",
            "H:|-[label4(60)][frame]|",
            "V:|-[label1(64)]-[label2(64)]-[label3(64)]-[label4(64)]",
            "V:|-[me(64)]-[girl(64)]-[boy(64)]-[frame(64)]",
        ]

        self.contentView.addLayoutConstraints(constraints, views: views)
    }

    func selected(color: String?) {
        if let c = color {
            myChooser.frameColor = c
            girlChooser.frameColor = c
            boyChooser.frameColor = c
        }
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    deinit {
        print("deinit: ColorChooserTableViewCell")
    }
}
