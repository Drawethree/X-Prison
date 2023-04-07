package dev.drawethree.xprison.support.exception;

import dev.drawethree.xprison.XPrisonModule;

public class ModuleNotEnabledException extends Exception {

    public ModuleNotEnabledException(XPrisonModule module) {
        super("Module " + module.getName() + " is not enabled");
    }
}
