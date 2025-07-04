package dev.drawethree.xprison;

import dev.drawethree.xprison.api.XPrisonModule;
import lombok.Getter;
import me.lucko.helper.Schedulers;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.composite.CompositeTerminable;
import me.lucko.helper.terminable.module.TerminableModule;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public abstract class XPrisonModuleBase implements XPrisonModule, TerminableConsumer {

    @Getter
    protected final XPrison core;
    private final CompositeTerminable terminableRegistry;
    @Getter
    protected boolean enabled;

    public XPrisonModuleBase(XPrison core) {
        this.core = core;
        this.terminableRegistry = CompositeTerminable.create();
    }


    public void enable() {
        Schedulers.builder()
                .async()
                .after(10, TimeUnit.SECONDS)
                .every(30, TimeUnit.SECONDS)
                .run(this.terminableRegistry::cleanup)
                .bindWith(this.terminableRegistry);
    }

    public void disable() {
        this.terminableRegistry.closeAndReportException();
    }

    public void reload() {

    }

    @Nonnull
    public <T extends AutoCloseable> T bind(@Nonnull T terminable) {
        return this.terminableRegistry.bind(terminable);
    }

    @Nonnull
    public <T extends TerminableModule> T bindModule(@Nonnull T module) {
        return this.terminableRegistry.bindModule(module);
    }
}