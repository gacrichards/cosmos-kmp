import SwiftUI
import ComposeApp

@main
struct CosmosApp: App {
    init() {
        IosModuleKt.startKoin(nasaApiKey: nasaApiKey)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
