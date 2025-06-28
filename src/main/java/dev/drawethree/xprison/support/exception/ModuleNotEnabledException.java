package dev.drawethree.xprison.support.exception;

import dev.drawethree.xprison.XPrisonModuleAbstract;

public class ModuleNotEnabledException extends Exception {

    public ModuleNotEnabledException(XPrisonModuleAbstract module) {
        super("Module " + module.getName() + " is not enabled");
    }
}
