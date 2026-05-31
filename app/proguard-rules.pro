# Add project specific ProGuard rules here.
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keep,includedescriptorclasses class id.pina.bacakomik.**$$serializer { *; }
-keepclassmembers class id.pina.bacakomik.** {
    *** Companion;
    *** INSTANCE;
}
