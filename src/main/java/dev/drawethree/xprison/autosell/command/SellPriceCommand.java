package dev.drawethree.xprison.autosell.command;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.autosell.gui.SellPriceEditorGui;
import dev.drawethree.xprison.autosell.utils.AutoSellContants;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Commands;
import me.lucko.helper.command.CommandInterruptException;
import me.lucko.helper.command.context.CommandContext;
import org.bukkit.entity.Player;

public class SellPriceCommand {

    private static final String COMMAND_NAME = "sellprice";
    private final XPrisonAutoSell plugin;

    public SellPriceCommand(XPrisonAutoSell plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Commands.create()
                .assertPlayer()
                .assertPermission(AutoSellContants.ADMIN_PERMISSION)
                .handler(c -> {

                    if (!this.validateContext(c)) {
                        this.sendInvalidUsage(c.sender());
                        return;
                    }

                    if (isEditorCommand(c)) {
                        this.openEditorGui(c.sender());
                        return;
                    }

                    XMaterial type = this.parseMaterialFromCommandContext(c);
                    double price = this.parsePriceFromCommandContext(c);

                    if (!validateMaterial(type)) {
                        PlayerUtils.sendMessage(c.sender(), "&cInvalid item in hand / specified item!");
                        return;
                    }

                    if (!validatePrice(price)) {
                        PlayerUtils.sendMessage(c.sender(), "&cSell price needs to be higher than 0!");
                        return;
                    }

                    this.plugin.getManager().addSellPrice(type, price);

                    PlayerUtils.sendMessage(c.sender(), String.format("&aSuccessfuly set sell price of &e%s &ato &e$%.2f", type.name(), price));

                }).registerAndBind(this.plugin, COMMAND_NAME);
    }

    private void openEditorGui(Player sender) {
        new SellPriceEditorGui(this.plugin.getManager(),sender).open();
    }

    private boolean isEditorCommand(CommandContext<Player> c) {
        return "editor".equalsIgnoreCase(c.rawArg(0));
    }

    private boolean validatePrice(double price) {
        return price > 0.0;
    }

    private boolean validateMaterial(XMaterial type) {
        return type != null;
    }

    private void sendInvalidUsage(Player player) {
        PlayerUtils.sendMessage(player, "&cInvalid usage!");
        PlayerUtils.sendMessage(player, "&c/sellprice editor - Opens Editor GUI for sell prices");
        PlayerUtils.sendMessage(player, "&c/sellprice <material> <price> - Sets the sell price of specified material.");
        PlayerUtils.sendMessage(player, "&c/sellprice <price> - Sets the sell price of item material you have in your hand.");
    }

    private boolean validateContext(CommandContext<Player> context) {
        return context.args().size() == 1 || context.args().size() == 2 || context.args().size() == 3;
    }

    private XMaterial parseMaterialFromCommandContext(CommandContext<Player> c) {
        XMaterial material = null;
        if (c.args().size() == 1) {
            if (c.sender().getItemInHand() == null) {
                PlayerUtils.sendMessage(c.sender(), "&cPlease hold some item!");
            } else {
                material = XMaterial.matchXMaterial(c.sender().getItemInHand());
            }
        } else if (c.args().size() == 2) {
            material = XMaterial.matchXMaterial(c.rawArg(0)).get();
        }
        return material;
    }

    private double parsePriceFromCommandContext(CommandContext<Player> c) throws CommandInterruptException {
        double price = 0.0;
        if (c.args().size() == 1) {
            price = c.arg(0).parseOrFail(Double.class);
        } else if (c.args().size() == 2) {
            price = c.arg(1).parseOrFail(Double.class);
        }
        return price;
    }
}
