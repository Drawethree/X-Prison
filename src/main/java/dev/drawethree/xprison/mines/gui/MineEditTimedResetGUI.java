package dev.drawethree.xprison.mines.gui;

import dev.drawethree.xprison.mines.model.mine.Mine;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MineEditTimedResetGUI extends Gui {

	private final Mine mine;

	private int currentTime;

	public MineEditTimedResetGUI(Mine mine, Player player) {
		super(player, 5, "Editing Timed Reset");
		this.mine = mine;
		this.currentTime = this.mine.getResetTime();
	}

	@Override
	public void redraw() {
		this.setItem(4, ItemStackBuilder.of(CompMaterial.CLOCK.toItem()).name("&eReset Time (minutes)").lore(" ", "&7Current reset time of", String.format("&7this mine is &b%,d minutes.", this.currentTime)).buildItem().build());

		// +

		this.setItem(19, ItemStackBuilder.of(CompMaterial.GREEN_STAINED_GLASS_PANE.toItem()).name("&a+1.0").build(() -> {
			handleTimeAddition(1);
		}));
		this.setItem(20, ItemStackBuilder.of(CompMaterial.GREEN_STAINED_GLASS_PANE.toItem()).name("&a+2.0").build(() -> {
			handleTimeAddition(2);
		}));
		this.setItem(21, ItemStackBuilder.of(CompMaterial.GREEN_STAINED_GLASS_PANE.toItem()).name("&a+5.0").build(() -> {
			handleTimeAddition(5);
		}));

		// -

		this.setItem(23, ItemStackBuilder.of(CompMaterial.RED_STAINED_GLASS_PANE.toItem()).name("&c-1.0").build(() -> {
			handleTimeAddition(-1);
		}));
		this.setItem(24, ItemStackBuilder.of(CompMaterial.RED_STAINED_GLASS_PANE.toItem()).name("&c-2.0").build(() -> {
			handleTimeAddition(-2);
		}));
		this.setItem(25, ItemStackBuilder.of(CompMaterial.RED_STAINED_GLASS_PANE.toItem()).name("&c-5.0").build(() -> {
			handleTimeAddition(-5);
		}));


		this.setItem(36, ItemStackBuilder.of(Material.ARROW).name("&cBack").lore("&7Click to go back to reset settings.").build(() -> {
			this.close();
			new MineResetOptionsGUI(this.mine, this.getPlayer()).open();
		}));

		this.setItem(40, ItemStackBuilder.of(CompMaterial.GREEN_WOOL.toItem()).name("&aSave").lore("&7Click to save the current reset time.").build(() -> {
			this.close();
			this.mine.setResetTime(this.currentTime);
			new MineResetOptionsGUI(this.mine, this.getPlayer()).open();
		}));

	}

	private void handleTimeAddition(int addition) {
		if (this.currentTime + addition < 1.0) {
			return;
		}
		this.currentTime += addition;
		this.redraw();
	}
}
