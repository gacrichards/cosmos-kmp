import SwiftUI
import ComposeApp

private final class EarthStateHolder: ObservableObject {
    @Published var isLoading = true
    @Published var currentDate: String = ""
    @Published var canGoPrevious = false
    @Published var canGoNext = false
    @Published var images: [EpicImage] = []
    @Published var errorMessage: String?

    private let helper = EpicViewModelHelper()

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
                let count = Int(self.helper.imageCount)
                let loaded = (0..<count).compactMap { self.helper.imageAt(index: Int32($0)) }
                DispatchQueue.main.async {
                    self.isLoading = false
                    self.currentDate = self.helper.currentDate
                    self.canGoPrevious = self.helper.canGoPrevious
                    self.canGoNext = self.helper.canGoNext
                    self.images = loaded
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

    func nextDate() { helper.nextDate() }
    func previousDate() { helper.previousDate() }
    func retry() { helper.retry() }

    deinit { helper.dispose() }
}

struct EarthView: View {
    @StateObject private var state = EarthStateHolder()

    private let columns = [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())]

    var body: some View {
        NavigationStack {
            Group {
                if state.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if !state.images.isEmpty {
                    VStack(spacing: 0) {
                        dateNavBar
                        ScrollView {
                            LazyVGrid(columns: columns, spacing: 2) {
                                ForEach(state.images, id: \.identifier) { image in
                                    thumbnailCell(image)
                                }
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
            .navigationTitle("Earth")
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    private var dateNavBar: some View {
        HStack {
            Button(action: { state.previousDate() }) {
                Image(systemName: "chevron.left")
                    .font(.body.weight(.semibold))
            }
            .disabled(!state.canGoPrevious)
            Spacer()
            Text(state.currentDate)
                .font(.subheadline)
            Spacer()
            Button(action: { state.nextDate() }) {
                Image(systemName: "chevron.right")
                    .font(.body.weight(.semibold))
            }
            .disabled(!state.canGoNext)
        }
        .padding(.horizontal)
        .padding(.vertical, 8)
        .background(Color(.systemBackground))
    }

    @ViewBuilder
    private func thumbnailCell(_ image: EpicImage) -> some View {
        let size = (UIScreen.main.bounds.width - 4) / 3
        AsyncImage(url: URL(string: image.thumbnailUrl)) { phase in
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
    }
}
