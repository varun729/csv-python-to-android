### csvpythontoandroid

A minimal Android app demonstrating how to bundle and call Python code from Kotlin using Chaquopy. The app includes a sample Python script (`app/src/main/python/process_csv.py`) and bundles scientific packages via `pip`.

App is built with Junie (by JetBrains) in about 1.5 hours with zero coding. Code needs proper review before using in production.

#### Key point: Python version and executable configuration (must match and be supported)

Chaquopy compiles your Python sources to bytecode during the Android build. For this to work, the Python used at build time MUST match the Android runtime’s Python minor version, and it MUST be one of Chaquopy’s supported versions. Using any other minor version (newer or older) will fail.

- Required minor version: `3.11`
- Acceptable builds: any Chaquopy-supported `3.11.x`
- Not acceptable: `3.10.x`, `3.12.x`, or any other minor version. Newer or older WILL NOT work for bytecode compilation.

Reference: https://chaquo.com/chaquopy/doc/current/android.html#android-bytecode

#### How to configure it

This project cannot use a managed Python. You MUST point Chaquopy to a local Python 3.11 executable, and you MUST set the Android runtime to the same minor version (`3.11`). Edit `app/build.gradle` and update the `chaquopy` block as follows:

```gradle
// app/build.gradle
chaquopy {
    defaultConfig {
        // REQUIRED: Path to your local Python 3.11 executable
        // Examples (adjust to your system):
        //   macOS (Homebrew ARM):   /opt/homebrew/bin/python3.11
        //   macOS (Intel/Homebrew): /usr/local/bin/python3.11
        //   pyenv:                  ~/.pyenv/versions/3.11.x/bin/python
        //   Windows:                C:/Users/<you>/AppData/Local/Programs/Python/Python311/python.exe
        buildPython '/opt/homebrew/bin/python3.11'  // must be 3.11.x

        // Android embedded runtime must match the same minor version
        version '3.11'

        pip {
            install 'numpy'
            install 'pandas'
            install 'matplotlib'
            install 'pillow'
        }
    }
}
```

Tips:
- Verify your interpreter is 3.11: run `/path/to/python3.11 -V` → should print `Python 3.11.x`.
- If you use environments (pyenv/venv/conda), point to the environment’s Python binary which is 3.11.x.
- Keep `version '3.11'` unchanged. Older versions may work, but 3.12+ are not supported.

If the minor versions do not match, or the interpreter is not a supported 3.11.x, Gradle will fail with an error similar to:

```
Failed to compile to .pyc format: buildPython version 3.11.13 is incompatible
```

#### Requirements

- Android Studio (Electric Eel or newer recommended)
- Gradle with internet access (for Chaquopy to download wheels for your packages)
- Min SDK 24, Target SDK 36 (as configured)

#### Build and run

1) Open the project in Android Studio.
2) Ensure Gradle is NOT in Offline mode (File → Settings → Build, Execution, Deployment → Gradle → uncheck "Offline work").
3) Sync Gradle. The build will use your local Python 3.11 and download any required wheels.
4) Build and run on an emulator or device (ABIs: `arm64-v8a`, `x86_64`).

#### Verifying Python integration

- Inspect `app/src/main/python/process_csv.py` for the sample Python logic.
- Run the app and trigger any UI flow that exercises Python (e.g., CSV processing or transforms).

#### Troubleshooting

- If builds fail with a `.pyc` error, confirm your `buildPython` path points to Python 3.11.x and that `version '3.11'` is set.
- Ensure the path is accessible to Gradle (no interactive prompts, no spaces requiring escaping in shell contexts). Use absolute paths.
- On Windows, use forward slashes or escaped backslashes in Gradle strings, e.g., `C:/Python311/python.exe`.
- Any mismatch of Python minor versions (e.g., `3.10` or `3.12`) will fail. Keep both `buildPython` (3.11.x) and `version '3.11'`.

#### License

MIT © 2025 Varun Agrawal. See `LICENSE` for details.
