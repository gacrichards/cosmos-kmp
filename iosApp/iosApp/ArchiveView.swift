import SwiftUI
import ComposeApp

private final class ArchiveStateHolder: ObservableObject {
    @Published var apods: [Apod] = []
    @Published var isLoading = true
    @Published var errorMessage: String?

    private let helper = ArchiveViewModelHelper()

    init() {
        helper.startObserving(
            onLoading: { [weak self] in
                DispatchQueue.main.async {
                    self?.isLoading = true
                    self?.errorMessage = nil
                }
            },
            onSuccess: { [weak self] in
                guard let self else { return }
                // Kotlin Int maps to Int32 in Swift — convert for native indexing
                let count = Int(self.helper.apodCount)
                let loaded = (0..<count).compactMap { self.helper.apodAt(index: Int32($0)) }
                DispatchQueue.main.async {
                    self.isLoading = false
                    self.apods = loaded
                    self.errorMessage = nil
                }
            },
            onError: { [weak self] message in
                DispatchQueue.main.async {
                    self?.isLoading = false
                    self?.errorMessage = message
                }
            }
        )
    }

    func retry() { helper.retry() }

    deinit { helper.dispose() }
}

struct ArchiveView: View {
    @StateObject private var state = ArchiveStateHolder()

    private let columns = [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())]

    var body: some View {
        NavigationStack {
            Group {
                if state.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if !state.apods.isEmpty {
                    ScrollView {
                        LazyVGrid(columns: columns, spacing: 2) {
                            ForEach(state.apods, id: \.date) { apod in
                                NavigationLink(destination: MediaDetailView(date: apod.date)) {
                                    thumbnailCell(apod)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    }
                } else if let error = state.errorMessage {
                    VStack(spacing: 16) {
                        Text("Failed to load").font(.headline)
                        Text(error).font(.caption).foregroundStyle(.secondary).multilineTextAlignment(.center)
                        Button("Retry") { state.retry() }
                    }
                    .padding()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
            }
            .navigationTitle("Archive")
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    @ViewBuilder
    private func thumbnailCell(_ apod: Apod) -> some View {
        let size = (UIScreen.main.bounds.width - 4) / 3
        if let image = apod.mediaType as? MediaType.Image {
            AsyncImage(url: URL(string: image.url)) { phase in
                switch phase {
                case .success(let img):
                    img.resizable().scaledToFill()
                        .frame(width: size, height: size)
                        .clipped()
                case .empty:
                    Rectangle().fill(Color(.systemGray5))
                        .frame(width: size, height: size)
                        .overlay(ProgressView())
                default:
                    Rectangle().fill(Color(.systemGray5))
                        .frame(width: size, height: size)
                        .overlay(Image(systemName: "photo").foregroundStyle(.secondary))
                }
            }
        } else {
            Rectangle().fill(Color(.systemGray5))
                .frame(width: size, height: size)
                .overlay(Image(systemName: "play.circle").foregroundStyle(.secondary))
        }
    }
}
