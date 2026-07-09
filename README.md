# Easeatten

> [!NOTE]
>
> This application is independent, there's no form of affiliation with any institution, educational
> or otherwise.

## Building Instructions

### Dependencies

- Android SDK (latest versions recommended)
    - Android Platform Tools
    - Android Build Tools
    - Android NDK
- JDK (version 21 or higher)
- Cargo, along with the following binaries:
    - [`cargo-ndk`](https://github.com/bbqsrc/cargo-ndk)
      (follow its installation guide, you will also need `rustup` for all target architectures)
    - [`uniffi-bindgen`](https://github.com/mozilla/uniffi-rs) (with the `cli` feature enabled)

### Steps

Build with the Gradle wrapper included with this repository:

```sh
./gradlew build
```

The resulting `.apk` builds are located in `app/build/outputs/apk`.

## License

See [LICENSE](./LICENSE).

<!-- vim: set tw=100 -->
