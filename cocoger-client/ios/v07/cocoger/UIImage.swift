import UIKit

extension UIImage {

    func resizeToWidth(width:CGFloat)-> UIImage {
        let imageView = UIImageView(frame: CGRectMake(0, 0, width, CGFloat(ceil(width/size.width * size.height))))
        imageView.contentMode = UIViewContentMode.ScaleAspectFit
        imageView.image = self
        UIGraphicsBeginImageContext(imageView.bounds.size)
        imageView.layer.renderInContext(UIGraphicsGetCurrentContext()!)
        let result = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return result
    }


/*
+ (UIImage *)resizedImage:(UIImage *)image width:(CGFloat)width height:(CGFloat)height
{
    if (UIGraphicsBeginImageContextWithOptions != NULL) {
        UIGraphicsBeginImageContextWithOptions(CGSizeMake(width, height), NO, [[UIScreen mainScreen] scale])
    } else {
        UIGraphicsBeginImageContext(CGSizeMake(width, height));
    }

    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextSetInterpolationQuality(context, kCGInterpolationHigh);

    [image drawInRect:CGRectMake(0.0, 0.0, width, height)];

    UIImage *resizedImage = UIGraphicsGetImageFromCurrentImageContext();

    UIGraphicsEndImageContext();

    return resizedImage;
}
*/

}




extension UIImage {

    func cropCircularImage(size: CGSize) -> UIImage {
        // This function returns a newImage, based on image, that has been:
        // - scaled to fit in (CGRect) rect
        // - and cropped within a circle of radius: rectWidth/2

        //Create the bitmap graphics context
        UIGraphicsBeginImageContextWithOptions(CGSizeMake(size.width, size.height), false, 0.0)
        let context = UIGraphicsGetCurrentContext()

        //Get the width and heights
        let imageWidth = self.size.width
        let imageHeight = self.size.height
        let rectWidth = size.width
        let rectHeight = size.height

        //Calculate the scale factor
        let scaleFactorX = rectWidth / imageWidth
        let scaleFactorY = rectHeight / imageHeight

        //Calculate the centre of the circle
        let imageCentreX = rectWidth / 2
        let imageCentreY = rectHeight / 2

        // Create and CLIP to a CIRCULAR Path
        // (This could be replaced with any closed path if you want a different shaped clip)
        let radius = rectWidth / 2
        let startAngle: CGFloat = 0.0
        let endAngle: CGFloat = CGFloat(M_PI * 2.0)
        CGContextBeginPath(context)
        CGContextAddArc(context, imageCentreX, imageCentreY, radius, startAngle, endAngle, 0)
        CGContextClosePath(context)
        CGContextClip(context)

        //Set the SCALE factor for the graphics context
        //All future draw calls will be scaled by this factor
        CGContextScaleCTM(context, scaleFactorX, scaleFactorY)

        // Draw the IMAGE
        self.drawInRect(CGRectMake(0, 0, imageWidth, imageHeight))

        let newImage = UIGraphicsGetImageFromCurrentImageContext()

        UIGraphicsEndImageContext()

        return newImage
    }

}
