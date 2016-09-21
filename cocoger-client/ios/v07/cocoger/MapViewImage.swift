import UIKit

class MapViewImage {

    class func createImage(context: CGContext, frame: CGRect, image: UIImage?=nil, text: String?=nil, bg: UIColor) {
        let borderBackground = User.frameColor.hexColor
        let ballAlpha: CGFloat = 0.6
        let imageAlpha: CGFloat = 0.9
        let lineWitdth: CGFloat = UIScreen.mainScreen().scale

        let ballRect = CGRectInset(frame, 1, 1)
        var imageRect = CGRectInset(ballRect, 5, 5)

        var actualImage: UIImage?

        // unless the text is specified, show the image
        if text == nil {
            if image == nil {
                actualImage = Image.getImage(named: "person.png")
            } else {
                actualImage = image
            }
        }

        CGContextSetAlpha(context, ballAlpha)

        CGContextSetFillColorWithColor(context, bg.CGColor)
        CGContextFillEllipseInRect(context, ballRect)

        CGContextSetLineWidth(context, lineWitdth)
        CGContextSetStrokeColorWithColor(context, borderBackground.CGColor)
        CGContextStrokeEllipseInRect(context, ballRect)

        let translatedY = frame.origin.y + frame.size.height

        imageRect.origin.y -= frame.origin.y

        UIGraphicsPushContext(context)
        CGContextSetAlpha(context, imageAlpha)
        CGContextTranslateCTM(context, 0, translatedY)
        CGContextSetBlendMode(context, .Multiply)
        CGContextScaleCTM(context, 1.0, -1.0)
        if actualImage != nil {
            CGContextDrawImage(context, imageRect, actualImage!.CGImage)
        } else if text != nil {
            let scale: CGFloat = UIScreen.mainScreen().scale
            let font = UIFont(name: "Helvetica-Bold", size: 14*scale)
            let attr:CFDictionaryRef = [NSFontAttributeName:font!,
                                        NSForegroundColorAttributeName:UIColor.blackColor()]
            let cgtext = CFAttributedStringCreate(nil, text, attr)
            let line = CTLineCreateWithAttributedString(cgtext)
            var descent: CGFloat = 0
            let textWidth = CGFloat(CTLineGetTypographicBounds(line, nil, &descent, nil))
            CGContextSetLineWidth(context, 1.5)
            CGContextSetTextDrawingMode(context, .Stroke)
            let x = imageRect.origin.x + (imageRect.size.width - textWidth) / 2
            let y = imageRect.origin.y + descent
            CGContextSetTextPosition(context, x, y)
            CTLineDraw(line, context)
        }
        CGContextScaleCTM(context, 1.0, -1.0)
        CGContextTranslateCTM(context, 0, -translatedY)
        UIGraphicsPopContext()
    }

    class func getImage(image: UIImage?, size: CGSize, backgroundColor: UIColor) -> UIImage? {
        //let size = CGSizeMake(MapViewImage.UIS, MapViewImage.UIS)
        let frame = CGRectMake(0, 0, size.width, size.height)
        UIGraphicsBeginImageContext(size)
        let ctx = UIGraphicsGetCurrentContext()
        MapViewImage.createImage(ctx!, frame: frame, image: image, bg: backgroundColor)
        let icon = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return icon
    }

    class func getImage(text: String, backgroundColor: UIColor) -> UIImage? {
        let size = CGSizeMake(MapViewImage.UIS, MapViewImage.UIS)
        let frame = CGRectMake(0, 0, size.width, size.height)
        UIGraphicsBeginImageContext(size)
        let ctx = UIGraphicsGetCurrentContext()
        MapViewImage.createImage(ctx!, frame: frame, text: text, bg: backgroundColor)
        let icon = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return icon
    }

    class func getImage(icons: [(image: UIImage?, backgroundColor: UIColor)]) -> UIImage? {
        let fi = MapViewImage.friendImages[icons.count]

        let size = CGSizeMake(fi.size.width, fi.size.height)

        UIGraphicsBeginImageContext(size)

        let ctx = UIGraphicsGetCurrentContext()

        for var i = 0; i < icons.count; i++ {
            let icon = icons[i]
            let frame = fi.position[i]
            MapViewImage.createImage(ctx!, frame: frame, image: icon.image, bg: icon.backgroundColor)
        }

        let accumulated = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        return accumulated
    }

    static let UIS: CGFloat = 24 * UIScreen.mainScreen().scale // UserIconSize = 20

    static let FIS: CGFloat = 24 * UIScreen.mainScreen().scale // FriendIconSize = 20

    struct FriendImage {
        var size: CGSize
        var position: [CGRect]
    }

    static let friendImages: [FriendImage] = [
        FriendImage(
            size: CGSizeZero,
            position: []
            ),
        FriendImage(
            size: CGSizeMake(FIS, FIS),
            position: [CGRectMake(0, 0, FIS, FIS)]
            ),
        FriendImage(
            size: CGSizeMake(FIS*2, FIS),
            position: [CGRectMake(0, 0, FIS, FIS), CGRectMake(FIS, 0, FIS, FIS)]
            ),
        FriendImage(
            size: CGSizeMake(FIS*2, FIS*2),
            position: [CGRectMake(FIS/2, 0, FIS, FIS), CGRectMake(0, FIS, FIS, FIS), CGRectMake(FIS, FIS, FIS, FIS)]
            ),
        FriendImage(
            size: CGSizeMake(FIS*2, FIS*2),
            position: [CGRectMake(0, 0, FIS, FIS), CGRectMake(FIS, 0, FIS, FIS),
                       CGRectMake(0, FIS, FIS, FIS), CGRectMake(FIS, FIS, FIS, FIS)]
            ),
        FriendImage(
            size: CGSizeMake(FIS*3, FIS*3),
            position: [CGRectMake(FIS, 0, FIS, FIS), CGRectMake(0, FIS, FIS, FIS),
                       CGRectMake(FIS, FIS, FIS, FIS), CGRectMake(FIS*2, FIS, FIS, FIS),
                       CGRectMake(FIS, FIS*2, FIS, FIS)]
            ),
        FriendImage(
            size: CGSizeMake(FIS*2, FIS*3),
            position: [CGRectMake(0, 0, FIS, FIS), CGRectMake(FIS, 0, FIS, FIS),
                       CGRectMake(0, FIS, FIS, FIS), CGRectMake(FIS, FIS, FIS, FIS),
                       CGRectMake(0, FIS*2, FIS, FIS), CGRectMake(FIS, FIS*2, FIS, FIS)]
            ),
        FriendImage(
            size: CGSizeMake(FIS*3, FIS*3),
            position: [CGRectMake(FIS/2, 0, FIS, FIS), CGRectMake(FIS*3/2, 0, FIS, FIS),
                       CGRectMake(0, FIS, FIS, FIS), CGRectMake(FIS, FIS, FIS, FIS),
                       CGRectMake(FIS*2, FIS, FIS, FIS), CGRectMake(FIS/2, FIS*2, FIS, FIS),
                       CGRectMake(FIS*3/2, FIS*2, FIS, FIS)]
            ),
/*
        FriendImage(
            size: CGSizeMake(FIS*3, FIS*3),
            position: [CGRectMake(0, 0, FIS, FIS), CGRectMake(FIS, 0, FIS, FIS),
                       CGRectMake(FIS*2, 0, FIS, FIS), CGRectMake(0, FIS, FIS, FIS),
                       CGRectMake(FIS, FIS, FIS, FIS), CGRectMake(FIS*2, FIS, FIS, FIS),
                       CGRectMake(0, FIS*2, FIS, FIS), CGRectMake(FIS, FIS*2, FIS, FIS)]
            ),
        FriendImage(
            size: CGSizeMake(FIS*3, FIS*3),
            position: [CGRectMake(0, 0, FIS, FIS), CGRectMake(FIS, 0, FIS, FIS),
                       CGRectMake(FIS*2, 0, FIS, FIS), CGRectMake(0, FIS, FIS, FIS),
                       CGRectMake(FIS, FIS, FIS, FIS), CGRectMake(FIS*2, FIS, FIS, FIS),
                       CGRectMake(0, FIS*2, FIS, FIS), CGRectMake(FIS, FIS*2, FIS, FIS),
                       CGRectMake(FIS*2, FIS*2, FIS, FIS)]
            ),
*/
    ]

}
