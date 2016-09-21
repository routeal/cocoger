import Foundation
import Alamofire

extension NSURLRequest {
    static func allowsAnyHTTPSCertificateForHost(host: String) -> Bool {
        return true
    }
}

class Router: URLRequestConvertible {
    static let RestURLString = "http://cocoger.com:3000"
    static let WebURLString = "http://cocoger.com"

    let path: String
    let method: Alamofire.Method
    let parameters: [String: AnyObject]?
    let token: String?

    deinit {
        //print("deinit: Router")
    }

    required init(api: String, method: String, parameters: [String: AnyObject]? = nil, token: String? = nil) {
        path = api
        switch (method) {
        case "GET":
            self.method = Alamofire.Method.GET
        case "POST":
            self.method = Alamofire.Method.POST
        case "PUT":
            self.method = Alamofire.Method.PUT
        case "DELETE":
            self.method = Alamofire.Method.DELETE
        default:
            self.method = Alamofire.Method.GET
        }
        self.parameters = parameters
        self.token = token
    }

    var URLRequest: NSMutableURLRequest {
        let URL = NSURL(string: Router.RestURLString)!
        let mutableURLRequest = NSMutableURLRequest(URL: URL.URLByAppendingPathComponent(path))
        mutableURLRequest.HTTPMethod = method.rawValue

        if token != nil {
            let jwt = "JWT " + token!
            mutableURLRequest.setValue(jwt, forHTTPHeaderField: "Authorization")
        }

        if let p = parameters {
            if method == .GET {
                return Alamofire.ParameterEncoding.URL.encode(mutableURLRequest, parameters: p).0
            } else {
                return Alamofire.ParameterEncoding.JSON.encode(mutableURLRequest, parameters: p).0
            }
        } else {
            return mutableURLRequest
        }
    }
}

typealias RestResponseCallback = (status: Bool, response: AnyObject?) -> Void

let Rest = RestClient()

class RestClient {

    static private let TIMEOUT: NSTimeInterval = 10 // seconds

    private let manager: Alamofire.Manager

    init() {
        let configuration = NSURLSessionConfiguration.defaultSessionConfiguration()
        configuration.timeoutIntervalForResource = RestClient.TIMEOUT // seconds
        manager = Alamofire.Manager(configuration: configuration)
    }

    deinit {
        print("deinit: RestClient")
    }

    func request(request: URLRequestConvertible, completion: (RestResponseCallback)? = nil) {
        manager.request(request)
            .validate()
            .responseJSON { response in

            switch response.result {
            case .Success:
                return completion!(status: true, response: response.result.value)

            case .Failure(let error):
                if let data = response.data {
                    if let str = String(data: data, encoding: NSUTF8StringEncoding) {
                        if let dictionary = self.stringToDictionary(str) {
                            return completion!(status: false, response: dictionary)
                        }
                    }
                }
                return completion!(status: false, response: ["message": error.localizedDescription])
            }
        }
    }

    func stringToDictionary(text: String) -> [String:AnyObject]? {
        if let data = text.dataUsingEncoding(NSUTF8StringEncoding) {
            do {
                let json = try NSJSONSerialization.JSONObjectWithData(data, options: .MutableContainers) as? [String:AnyObject]
                return json
            } catch {
                print("NSJSONSerialization failed")
            }
        }
        return nil
    }

}
