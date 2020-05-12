
package com.example.cocoa;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;

public class NSSavePanel {

    static {
        NativeLoader.loadLibrary(NSSavePanel.class.getClassLoader(), "jni-library-with-framework-dependencies");
    }

    public native String saveDialog(String title, String extension);
}
