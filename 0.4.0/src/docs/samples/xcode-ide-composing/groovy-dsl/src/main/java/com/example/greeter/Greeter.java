
package com.example.greeter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;

public class Greeter {

    static {
        NativeLoader.loadLibrary(Greeter.class.getClassLoader(), "xcode-ide-composing");
    }

    public native String sayHello(String name);
}
