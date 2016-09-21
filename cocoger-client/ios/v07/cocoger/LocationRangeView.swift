//
//  RangeView.swift
//  CustomeUIView
//
//  Created by Hiroshi Watanabe on 9/6/15.
//  Copyright (c) 2015 BitsBees. All rights reserved.
//

import UIKit

class LocationRangeView: UIView {

    deinit {
        print("deinit: LocationRangeView")
    }

    var barHeight: CGFloat = 6 // 8
    var trackRadius: CGFloat = 3
    var barSpacing: CGFloat = 6 // 8
    var labelSpace: CGFloat = 6
    var indicatorRadius: CGFloat = 16 // 18

    var barMargin: CGFloat = 8
    let indicatorRange: CGFloat = 60

    var mBarColor: UIColor!
    var mTrackColor: UIColor!
    var mIndicatorColor: UIColor!
    var mTextColor: UIColor!
    var mAlertColor: UIColor!
    var mCurrentColor: UIColor!
    var mTextFontSize: CGFloat = 9
    var mSelectable: Bool = true

    var labelHidden: Bool = false

    var barColor: UIColor {
        get {
            return mBarColor
        }
        set(color) {
            mBarColor = color
        }
    }

    var trackColor: UIColor {
        get {
            return mTrackColor
        }
        set(color) {
            mTrackColor = color
        }
    }

    var indicatorColor: UIColor {
        get {
            return mIndicatorColor
        }
        set(color) {
            mIndicatorColor = color
        }
    }

    var textColor: UIColor {
        get {
            return mTextColor
        }
        set(color) {
            mTextColor = color
        }
    }

    var alertColor: UIColor {
        get {
            return mAlertColor
        }
        set(color) {
            mAlertColor = color
        }
    }

    var currentColor: UIColor {
        get {
            return mCurrentColor
        }
        set(color) {
            mCurrentColor = color
        }
    }

    var textFontSize: CGFloat {
        get {
            return mTextFontSize
        }
        set(size) {
            mTextFontSize = size
        }
    }

    private var currentPosition: Int = LocationRange.position(LocationRange.City)!

    var initialPosition: Int = 0

    var value: Int {
        get {
            return LocationRange.value(currentPosition)!
        }
        set(v) {
            if let t = LocationRange.position(v) {
                if t < LocationRange.count {
                    currentPosition = t
                    initialPosition = t
                    if (self.trackPosition.count > 0) {
                        updateButtonSelectionStates()
                    }
                }
            }
        }
    }

    var lowerValue: Bool = true

    var mSize: Int = 1

    var size: Int {
        get {
            return mSize
        }
        set(v) {
            if v == 0 {
                mSize = 0
                barHeight = 4 // 8
                trackRadius = 2
                barSpacing = 4 // 8
                labelSpace = 6
                indicatorRadius = 12 // 18
            } else if v == 1 {
                mSize = 1
                barHeight = 6 // 8
                trackRadius = 3
                barSpacing = 6 // 8
                labelSpace = 7
                indicatorRadius = 16 // 18
            } else if v == 2 {
                mSize = 2
                barHeight = 8
                trackRadius = 3
                barSpacing = 8
                labelSpace = 8
                indicatorRadius = 18
            }
        }
    }

    // disable the user interaction
    var selectable: Bool {
        get {
            return mSelectable
        }
        set(flag) {
            mSelectable = flag
            if mSelectable {
                mIndicatorColor = UIColor(red: 52/255, green: 73/255, blue: 94/255, alpha: 1)
            } else {
                mIndicatorColor = mBarColor
                self.userInteractionEnabled = false
            }
        }
    }

    // should be deprecated
    var noneHidden: Bool = false

    var alertIndicator: Bool = false

    var indicator: UIView!
    var bar: UIView!
    var tracks: [UIView] = []
    var labels: [UILabel] = []
    var trackPosition: [CGPoint] = []
    var trackTapRegion: [CGRect] = []
    var snappedRegion: [CGRect] = []
    var panGestureRecognizer: UIPanGestureRecognizer!
    var beingDragged: Bool = false

    override init(frame: CGRect) {
        super.init(frame: frame)

        mBarColor = tintColor
        mIndicatorColor = tintColor
        mTrackColor = UIColor.whiteColor()
        mTextColor = UIColor.blackColor()
        mAlertColor = UIColor.redColor()
        mCurrentColor = UIColor.blackColor()

        bar = UIView()
        bar.backgroundColor = mBarColor
        bar.layer.zPosition = 0
        bar.layer.cornerRadius = barHeight / 2
        bar.layer.masksToBounds = true
        addSubview(bar)

        indicator = UIView()
        indicator.backgroundColor = mIndicatorColor
        indicator.layer.cornerRadius = indicatorRadius / 2
        indicator.layer.zPosition = 1
        addSubview(indicator)

        for var i = 0; i < LocationRange.count; i++ {
            let circle = UIView()
            circle.backgroundColor = mTrackColor
            circle.layer.cornerRadius = trackRadius
            circle.layer.zPosition = 2
            addSubview(circle)
            tracks.append(circle)

            let name = UILabel()
            name.font = UIFont.boldSystemFontOfSize(mTextFontSize)
            name.textColor = mTextColor
            name.textAlignment = .Center
            addSubview(name)
            labels.append(name)
        }

        createGestureRecognizer()
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    override func layoutSubviews() {

        trackPosition = []
        trackTapRegion = []
        snappedRegion = []

        let x = barMargin
        let y = labelHidden ? ((frame.size.height - barHeight) / 2) : (frame.size.height * 2 / 5 - barHeight / 2)
        let w = frame.size.width - barMargin * 2
        let barSize: CGRect = CGRectMake(x, y, w, barHeight)

        bar.frame = barSize

        let trackcount = noneHidden ? (tracks.count - 1) : tracks.count

        // distance between tracks
        let trackDistance = (barSize.size.width - barSpacing * 2) / CGFloat(trackcount - 1)

        for var i = 0; i < trackcount; i++ {
            let track = tracks[i]

            if alertIndicator {
                if i < currentPosition {
                    track.backgroundColor = mAlertColor
                } else if i == currentPosition {
                    track.backgroundColor = mCurrentColor
                }
            }

            let x = barSize.origin.x + barSpacing + trackDistance * CGFloat(i) - trackRadius
            let y = barSize.origin.y + (barHeight - trackRadius*2) / 2
            track.frame = CGRectMake(x, y, trackRadius * 2, trackRadius * 2)

            let centerX = x + trackRadius
            let centerY = y + trackRadius
            // center position of each track
            trackPosition.append(CGPoint(x: centerX, y: centerY))
            // regions where the tracks are tapped
            trackTapRegion.append(CGRectMake(centerX - indicatorRange / 2, centerY - indicatorRange / 2, indicatorRange, indicatorRange))
            // regions where the indicator is snapped to which track
            snappedRegion.append(CGRectMake(centerX - trackDistance / 2, centerY - trackDistance / 2, trackDistance, trackDistance))

            if !labelHidden {
                let l = labels[i]
                l.text = LocationRange.label[i]
                l.frame = CGRectMake(centerX - trackDistance / 2, y + barHeight + labelSpace,
                                     trackDistance, (frame.size.height - barHeight) / 2)
            }
        }

        updateButtonSelectionStates()
    }

    func updateButtonSelectionStates() {
        let pos = trackPosition[currentPosition]
        let x = pos.x - indicatorRadius / 2
        let y = pos.y - indicatorRadius / 2
        indicator.frame = CGRectMake(x, y, indicatorRadius, indicatorRadius)
    }

    func createGestureRecognizer() {
        let tapGestureRecognizer = UITapGestureRecognizer(target: self, action: "handleTap:")
        addGestureRecognizer(tapGestureRecognizer)

        panGestureRecognizer = UIPanGestureRecognizer(target: self, action: "handlePanGestures:")
        panGestureRecognizer.minimumNumberOfTouches = 1
        panGestureRecognizer.maximumNumberOfTouches = 1
        self.addGestureRecognizer(panGestureRecognizer)
    }

    func trackAroundTapPoint(point: CGPoint) -> Int {
        for var i = 0; i < trackTapRegion.count; i++ {
            if CGRectContainsPoint(trackTapRegion[i], point) {
                return i
            }
        }
        return -1
    }

    func handleTap(tap: UITapGestureRecognizer) {
        if !selectable {
            return
        }

        let tapPoint = tap.locationInView(self)

        let targetPoint = trackAroundTapPoint(tapPoint)
        if targetPoint < 0 || targetPoint >= LocationRange.count || targetPoint == currentPosition {
            return
        }

        if !lowerValue {
            if targetPoint < initialPosition {
                return
            }
        }

        currentPosition = targetPoint
        updateButtonSelectionStates()
    }

    func snap(point: CGPoint) {
        for var i = 0; i < snappedRegion.count; i++ {
            // compare only the x points
            if (snappedRegion[i].origin.x <= point.x) && ((snappedRegion[i].origin.x + snappedRegion[i].size.width) >= point.x) {
                if !lowerValue {
                    if i < initialPosition {
                        updateButtonSelectionStates()
                        return
                    }
                }
                currentPosition = i
                updateButtonSelectionStates()
            }
        }
    }

    func handlePanGestures(sender: UIPanGestureRecognizer) {
        if !selectable {
            return
        }
        let location = sender.locationInView(sender.view!)
        if sender.state == .Began {
            let tappedTrack = trackAroundTapPoint(location)
            if tappedTrack == currentPosition {
                beingDragged = true
            } else {
                beingDragged = false
            }
        } else if sender.state == .Changed {
            if beingDragged {
                let s = trackPosition.first!
                let e = trackPosition.last!
                if location.x >= s.x && location.x <= e.x {
                    indicator.center = CGPoint(x: location.x, y: s.y)
                }
            }
        } else if sender.state == .Ended || sender.state == .Failed || sender.state == .Cancelled {
            if beingDragged {
                snap(location)
                beingDragged = false
            } else {
                updateButtonSelectionStates()
            }
        }
    }

}
