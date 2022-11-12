package ru.nsu.gemuev.net4.util;

import com.google.inject.Guice;
import com.google.inject.Injector;

public final class DIContainer {
    private static final Injector injector;

    static {
        injector = Guice.createInjector(new DIModule());
    }

    public static Injector getInjector() {
        return injector;
    }

    private DIContainer() {
    }
}