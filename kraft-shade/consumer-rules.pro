# Keep all KraftShader subclasses for reflection access
-keep class * extends com.cardinalblue.kraftshade.shader.KraftShader { }

# Keep constructors for reflection instantiation
-keepclassmembers class * extends com.cardinalblue.kraftshade.shader.KraftShader {
    <init>(...);
}

# Keep pipeline serialization classes
-keepclassmembers class com.cardinalblue.kraftshade.pipeline.serialization.PipelineShaderNode {
    <fields>;
    <methods>;
}