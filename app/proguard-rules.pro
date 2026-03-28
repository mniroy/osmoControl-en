# Keep release shrinking conservative until the MVP surface is verified on device.
# The app relies on direct Kotlin/Compose references, so the default optimized
# Android rule set should remove most unused code safely.

# Preserve stack-trace readability for coroutines during field testing.
-keepattributes SourceFile,LineNumberTable

