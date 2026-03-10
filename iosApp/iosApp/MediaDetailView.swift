import SwiftUI
import ComposeApp

private final class MediaDetailStateHolder: ObservableObject {
    @Published var isLoading = true
    @Published var apod: Apod?
    @Published var errorMessage: String?

    private let helper: MediaDetailViewModelHelper

    init(date: String) {
        helper = MediaDetailViewModelHelper(date: date)
        helper.startObserving(
            onLoading: { [weak self] in
                DispatchQueue.main.async {
                    self?.isLoading = true
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
                    self?.errorMessage = message
                }
            }
        )
    }

    deinit { helper.dispose() }
}

struct MediaDetailView: View {
    @StateObject private var state: MediaDetailStateHolder

    init(date: String) {
        _state = StateObject(wrappedValue: MediaDetailStateHolder(date: date))
    }

    var body: some View {
        Group {
            if state.isLoading {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let apod = state.apod {
                apodContent(apod)
            } else if let error = state.errorMessage {
                VStack(spacing: 16) {
                    Text("Failed to load").font(.headline)
                    Text(error).font(.caption).foregroundStyle(.secondary).multilineTextAlignment(.center)
                }
                .padding()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .navigationTitle(state.apod?.title ?? "")
        .navigationBarTitleDisplayMode(.inline)
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
}
