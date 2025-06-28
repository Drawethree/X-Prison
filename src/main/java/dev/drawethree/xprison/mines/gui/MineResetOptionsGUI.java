package dev.drawethree.xprison.mines.gui;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.mines.model.mine.MineImpl;
import dev.drawethree.xprison.mines.model.mine.reset.ResetType;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MineResetOptionsGUI extends Gui {

	private final MineImpl mineImpl;

	public MineResetOptionsGUI(MineImpl mineImpl, Player player) {
		super(player, 5, "Reset Options");
		this.mineImpl = mineImpl;
	}

	@Override
	public void redraw() {

		for (int i = 0; i < 5 * 9; i++) {
			this.setItem(i, ItemStackBuilder.of(XMaterial.BLACK_STAINED_GLASS_PANE.parseItem()).name("&a").buildItem().build());
		}

		this.setItem(11, ItemStackBuilder.of(XMaterial.STONE_BUTTON.parseItem()).name("&eReset NOW").lore(" ", "&7Resets the mine now.").build(() -> {
			this.close();
			this.mineImpl.getManager().resetMine(this.mineImpl);
		}));

		this.setItem(13, ItemStackBuilder.of(XMaterial.COMPARATOR.parseItem()).name("&eReset Type: " + this.mineImpl.getResetType().getName()).lore(" ", "&7Instant: Will use more CPU power", "&7but the mine will reset instantly.", " ", "&7Gradual: Will use less CPU power", "&7but mine reset may take more time.", " ", "&aClick &7to change.").build(() -> {
			if (this.mineImpl.getResetType() == ResetType.GRADUAL) {
				this.mineImpl.setResetType(ResetType.INSTANT);
			} else {
				this.mineImpl.setResetType(ResetType.GRADUAL);
			}
			this.redraw();
		}));

		this.setItem(15, ItemStackBuilder.of(XMaterial.CLOCK.parseItem()).name("&eEdit Reset Percentage").lore(" ", "&7Click to edit mine's reset percentage").build(() -> {
			this.close();
			new MineEditResetPercentageGUI(this.mineImpl, this.getPlayer()).open();
		}));

		this.setItem(29, ItemStackBuilder.of(XMaterial.CLOCK.parseItem()).name("&eEdit Timed Reset").lore(" ", "&7Click to edit timed reset").build(() -> {
			this.close();
			new MineEditTimedResetGUI(this.mineImpl, this.getPlayer()).open();
		}));

		this.setItem(31, ItemStackBuilder.of(XMaterial.PAPER.parseItem()).name("&eBroadcast Reset: " + this.mineImpl.isBroadcastReset()).lore(" ", "&aTrue &7- All players will get message", "&7on mine's reset.", "&cFalse &7- No broadcast message.").build(() -> {
			this.mineImpl.setBroadcastReset(!this.mineImpl.isBroadcastReset());
			this.redraw();
		}));

		this.setItem(36, ItemStackBuilder.of(Material.ARROW).name("&cBack").lore("&7Click to go back to panel").build(() -> {
			this.close();
			new MinePanelGUI(this.mineImpl, this.getPlayer()).open();
		}));


	}
}
