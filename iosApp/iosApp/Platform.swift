import Foundation

enum Platform {
    static var isiOS: Bool {
        #if os(iOS)
        return true
        #else
        return false
        #endif
    }
}
