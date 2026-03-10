import SwiftUI

struct ContentView: View {
    var body: some View {
        TabView {
            TodayView()
                .tabItem {
                    Label("Today", systemImage: "sparkles")
                }
            ArchiveView()
                .tabItem {
                    Label("Archive", systemImage: "square.grid.3x3")
                }
            EarthView()
                .tabItem {
                    Label("Earth", systemImage: "globe.americas")
                }
        }
        .tint(Color(red: 0.541, green: 0.706, blue: 0.973))
    }
}

#Preview {
    ContentView()
}
