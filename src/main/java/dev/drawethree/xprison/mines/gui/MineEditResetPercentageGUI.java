package dev.drawethree.xprison.mines.gui;

import dev.drawethree.xprison.mines.model.mine.Mine;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MineEditResetPercentageGUI extends Gui {


	private final Mine mine;

	private double currentChance;

	public MineEditResetPercentageGUI(Mine mine, Player player) {
		super(player, 5, "Editing Reset Percentage");
		this.mine = mine;
		this.currentChance = this.mine.getResetPercentage();
	}

	@Override
	public void redraw() {
		this.setItem(4, ItemStackBuilder.of(CompMaterial.CLOCK.toItem()).name("&eReset Percentage").lore(" ", "&7Current reset percentage of", String.format("&7this mine is &b%,.2f%%", this.currentChance)).buildItem().build());

		// +
		this.setItem(10, ItemStackBuilder.of(CompMaterial.GREEN_STAINED_GLASS_PANE.toItem()).name("&a+0.1").build(() -> {
			handleChanceAddition(0.1);
		}));
		this.setItem(11, ItemStackBuilder.of(CompMaterial.GREEN_STAINED_GLASS_PANE.toItem()).name("&a+0.2").build(() -> {
			handleChanceAddition(0.2);
		}));
		this.setItem(12, ItemStackBuilder.of(CompMaterial.GREEN_STAINED_GLASS_PANE.toItem()).name("&a+0.5").build(() -> {
			handleChanceAddition(0.5);
		}));

		this.setItem(19, ItemStackBuilder.of(CompMaterial.GREEN_STAINED_GLASS_PANE.toItem()).name("&a+1.0").build(() -> {
			handleChanceAddition(1.0);
		}));
		this.setItem(20, ItemStackBuilder.of(CompMaterial.GREEN_STAINED_GLASS_PANE.toItem()).name("&a+2.0").build(() -> {
			handleChanceAddition(2.0);
		}));
		this.setItem(21, ItemStackBuilder.of(CompMaterial.GREEN_STAINED_GLASS_PANE.toItem()).name("&a+5.0").build(() -> {
			handleChanceAddition(5.0);
		}));

		this.setItem(28, ItemStackBuilder.of(CompMaterial.GREEN_STAINED_GLASS_PANE.toItem()).name("&a+10.0").build(() -> {
			handleChanceAddition(10.0);
		}));
		this.setItem(29, ItemStackBuilder.of(CompMaterial.GREEN_STAINED_GLASS_PANE.toItem()).name("&a+20.0").build(() -> {
			handleChanceAddition(20.0);
		}));
		this.setItem(30, ItemStackBuilder.of(CompMaterial.GREEN_STAINED_GLASS_PANE.toItem()).name("&a+50.0").build(() -> {
			handleChanceAddition(50.0);
		}));

		// -
		this.setItem(14, ItemStackBuilder.of(CompMaterial.RED_STAINED_GLASS_PANE.toItem()).name("&c-0.1").build(() -> {
			handleChanceAddition(-0.1);
		}));
		this.setItem(15, ItemStackBuilder.of(CompMaterial.RED_STAINED_GLASS_PANE.toItem()).name("&c-0.2").build(() -> {
			handleChanceAddition(-0.2);
		}));
		this.setItem(16, ItemStackBuilder.of(CompMaterial.RED_STAINED_GLASS_PANE.toItem()).name("&c-0.5").build(() -> {
			handleChanceAddition(-0.5);
		}));

		this.setItem(23, ItemStackBuilder.of(CompMaterial.RED_STAINED_GLASS_PANE.toItem()).name("&c-1.0").build(() -> {
			handleChanceAddition(-1.0);
		}));
		this.setItem(24, ItemStackBuilder.of(CompMaterial.RED_STAINED_GLASS_PANE.toItem()).name("&c-2.0").build(() -> {
			handleChanceAddition(-2.0);
		}));
		this.setItem(25, ItemStackBuilder.of(CompMaterial.RED_STAINED_GLASS_PANE.toItem()).name("&c-5.0").build(() -> {
			handleChanceAddition(-5.0);
		}));

		this.setItem(32, ItemStackBuilder.of(CompMaterial.RED_STAINED_GLASS_PANE.toItem()).name("&c-10.0").build(() -> {
			handleChanceAddition(-10.0);
		}));
		this.setItem(33, ItemStackBuilder.of(CompMaterial.RED_STAINED_GLASS_PANE.toItem()).name("&c-20.0").build(() -> {
			handleChanceAddition(-20.0);
		}));
		this.setItem(34, ItemStackBuilder.of(CompMaterial.RED_STAINED_GLASS_PANE.toItem()).name("&c-50.0").build(() -> {
			handleChanceAddition(-50.0);
		}));


		this.setItem(36, ItemStackBuilder.of(Material.ARROW).name("&cBack").lore("&7Click to go back to reset settings.").build(() -> {
			this.close();
			new MineResetOptionsGUI(this.mine, this.getPlayer()).open();
		}));

		this.setItem(40, ItemStackBuilder.of(CompMaterial.GREEN_WOOL.toItem()).name("&aSave").lore("&7Click to save the current reset percentage.").build(() -> {
			this.close();
			this.mine.setResetPercentage(this.currentChance);
			new MineResetOptionsGUI(this.mine, this.getPlayer()).open();
		}));

	}

	private void handleChanceAddition(double addition) {
		if (this.currentChance + addition > 95.0 || this.currentChance + addition < 5.0) {
			return;
		}
		this.currentChance += addition;
		this.redraw();
	}
}
