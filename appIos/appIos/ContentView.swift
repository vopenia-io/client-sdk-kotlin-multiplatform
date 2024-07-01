import UIKit
import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        ComposeView().ignoresSafeArea(.all, edges: .all) // Compose has own keyboard handler
    }
}
