//
//  AgePickerViewController.swift
//  sokora
//
//  Created by Hiroshi Watanabe on 9/8/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit

class AgePickerViewController: UIViewController, UIPickerViewDataSource, UIPickerViewDelegate {

    let StartYear = 1920

    var picker: UIPickerView?

    weak var target: UITextField?

    var myear: Int = 1980

    var year: Int {
        get {
            if picker == nil {
                return myear
            } else {
                return picker!.selectedRowInComponent(0) + StartYear
            }
        }
        set(year) {
            if picker == nil {
                if year >= StartYear && year < (StartYear + 100) {
                    myear = year
                }
            } else {
                picker!.selectRow(year - StartYear, inComponent: 0, animated: false)
            }
        }
    }

    required init(coder: NSCoder) {
        fatalError("NSCoding not supported")
    }

    override init(nibName nibNameOrNil: String!, bundle nibBundleOrNil: NSBundle!) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    deinit {
        print("deinit: AgePickerViewController")
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.navigationController!.setNavigationBarHidden(false, animated: false)

        self.title = NSLocalizedString("Birth Year", comment: "viewcontroller title")

        picker = UIPickerView()
        picker!.dataSource = self
        picker!.delegate = self
        picker!.translatesAutoresizingMaskIntoConstraints = false
        picker!.selectRow(myear - StartYear, inComponent: 0, animated: false)
        view.addSubview(picker!)

        let views = ["picker":picker!]
        let constraints = ["H:|-[picker]-|", "V:|[picker]|"]
        view.addLayoutConstraints(constraints, views: views)
    }

    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        // set the year to the textfield of the calling view controller before dismissing
        if target != nil {
            target!.text = "\(self.year)"
        }
    }

    func numberOfComponentsInPickerView(pickerView: UIPickerView) -> Int {
        return 1
    }

    func pickerView(pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return 100
    }

    func pickerView(pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String?{
        return "\(StartYear + row)"
    }
}
