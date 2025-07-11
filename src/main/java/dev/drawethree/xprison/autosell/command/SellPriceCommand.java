package dev.drawethree.xprison.autosell.command;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.autosell.gui.AllSellRegionsGui;
import dev.drawethree.xprison.autosell.model.SellRegionImpl;
import dev.drawethree.xprison.autosell.utils.AutoSellContants;
import dev.drawethree.xprison.utils.misc.RegionUtils;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Commands;
import me.lucko.helper.command.CommandInterruptException;
import me.lucko.helper.command.context.CommandContext;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

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

                    IWrappedRegion wrappedRegion = RegionUtils.getFirstRegionAtLocation(c.sender().getLocation());

                    if (!validateRegion(wrappedRegion)) {
                        PlayerUtils.sendMessage(c.sender(), "&cYou must be standing in a region / specify a valid region!");
                        return;
                    }

                    SellRegionImpl sellRegionImpl = this.getSellRegionFromWrappedRegion(wrappedRegion);

                    if (sellRegionImpl == null) {
                        sellRegionImpl = new SellRegionImpl(wrappedRegion, c.sender().getWorld());
                    }

                    sellRegionImpl.addSellPrice(type, price);

                    this.plugin.getManager().updateSellRegion(sellRegionImpl);
                    this.plugin.getAutoSellConfig().saveSellRegion(sellRegionImpl);

                    PlayerUtils.sendMessage(c.sender(), String.format("&aSuccessfuly set sell price of &e%s &ato &e$%.2f &ain region &e%s", type.name(), price, wrappedRegion.getId()));

                }).registerAndBind(this.plugin.getCore(), COMMAND_NAME);
    }

    private void openEditorGui(Player sender) {
        AllSellRegionsGui.createAndOpenTo(sender);
    }

    private boolean isEditorCommand(CommandContext<Player> c) {
        return "editor".equalsIgnoreCase(c.rawArg(0));
    }

    private SellRegionImpl getSellRegionByName(String name) {
        return this.plugin.getManager().getSellRegionByName(name);
    }

    private SellRegionImpl getSellRegionFromWrappedRegion(IWrappedRegion region) {
        return this.plugin.getManager().getSellRegionFromWrappedRegion(region);
    }

    private boolean validateRegion(IWrappedRegion region) {
        return region != null;
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
