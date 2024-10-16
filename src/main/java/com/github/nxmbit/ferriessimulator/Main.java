package com.github.nxmbit.ferriessimulator;

/*
   Starter class for the application that doesn't inherit
   from javafx.application.Application,
   so that the built jar can be run without errors.
   See: https://github.com/javafxports/openjdk-jfx/issues/236
*/
public class Main {
    public static void main(String[] args) {
        FerriesSimulator.main(args);
    }
}