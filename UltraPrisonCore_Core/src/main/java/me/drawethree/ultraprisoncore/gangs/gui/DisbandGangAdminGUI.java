package me.drawethree.ultraprisoncore.gangs.gui;

import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import me.drawethree.ultraprisoncore.gangs.models.Gang;
import me.drawethree.ultraprisoncore.utils.gui.ConfirmationGui;
import me.lucko.helper.Schedulers;
import org.bukkit.entity.Player;

public class DisbandGangAdminGUI extends ConfirmationGui {

    private final UltraPrisonGangs plugin;
    private final Gang gang;

    public DisbandGangAdminGUI(UltraPrisonGangs plugin, Player player, Gang gang) {
        super(player, "Disband " + gang.getName() + " gang ?");
        this.plugin = plugin;
        this.gang = gang;
    }

    @Override
    public void confirm(boolean confirm) {
        if (confirm) {
            Schedulers.async().run(() -> this.plugin.getGangsManager().disbandGang(this.getPlayer(), this.gang));
        }
        this.close();
    }
}
