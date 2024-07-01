import SwiftUI

@main
struct iOSApp: App {
    var body: some Scene {
        let currentSystemScheme = UITraitCollection.current.userInterfaceStyle
        WindowGroup {
            VStack {
                ContentView()
            }.frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(
                    (currentSystemScheme == .dark ? Color.black : Color.white).ignoresSafeArea()
                )
        }
    }
}
