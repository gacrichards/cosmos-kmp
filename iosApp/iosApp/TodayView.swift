import SwiftUI
import ComposeApp

private final class TodayStateHolder: ObservableObject {
    @Published var isLoading = true
    @Published var apod: Apod?
    @Published var errorMessage: String?

    private let helper = TodayViewModelHelper()

    init() {
        helper.startObserving(
            onLoading: { [weak self] in
                DispatchQueue.main.async {
                    self?.isLoading = true
                    self?.apod = nil
                    self?.errorMessage = nil
                }
            },
            onSuccess: { [weak self] apod in
                DispatchQueue.main.async {
                    self?.isLoading = false
                    self?.apod = apod
                    self?.errorMessage = nil
                }
            },
            onError: { [weak self] message in
                DispatchQueue.main.async {
                    self?.isLoading = false
                    self?.apod = nil
                    self?.errorMessage = message
                }
            }
        )
    }

    func retry() { helper.retry() }

    deinit { helper.dispose() }
}

struct TodayView: View {
    @StateObject private var state = TodayStateHolder()

    var body: some View {
        NavigationStack {
            Group {
                if state.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let apod = state.apod {
                    apodContent(apod)
                } else if let error = state.errorMessage {
                    errorView(error)
                }
            }
            .navigationTitle("Today")
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    @ViewBuilder
    private func apodContent(_ apod: Apod) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                if let image = apod.mediaType as? MediaType.Image {
                    AsyncImage(url: URL(string: apod.hdUrl ?? image.url)) { phase in
                        switch phase {
                        case .success(let img):
                            img.resizable().scaledToFit()
                        case .failure:
                            AsyncImage(url: URL(string: image.url)) { img in
                                img.resizable().scaledToFit()
                            } placeholder: { imagePlaceholder }
                        default:
                            imagePlaceholder
                        }
                    }
                } else if let video = apod.mediaType as? MediaType.Video {
                    VideoPlayerView(url: video.url)
                        .aspectRatio(16 / 9, contentMode: .fit)
                }

                VStack(alignment: .leading, spacing: 8) {
                    Text(apod.title).font(.headline)
                    if let copyright = apod.copyright {
                        Text("© \(copyright.trimmingCharacters(in: .whitespacesAndNewlines))")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                    Spacer().frame(height: 4)
                    Text(apod.explanation).font(.body)
                }
                .padding()
            }
        }
    }

    private var imagePlaceholder: some View {
        Rectangle()
            .fill(Color(.systemGray6))
            .frame(height: 240)
            .overlay(ProgressView())
    }

    private func errorView(_ message: String) -> some View {
        VStack(spacing: 16) {
            Text("Failed to load").font(.headline)
            Text(message).font(.caption).foregroundStyle(.secondary).multilineTextAlignment(.center)
            Button("Retry") { state.retry() }
        }
        .padding()
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
