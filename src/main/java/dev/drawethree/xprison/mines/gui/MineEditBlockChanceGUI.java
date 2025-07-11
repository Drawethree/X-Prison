package dev.drawethree.xprison.mines.gui;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.mines.model.mine.MineImpl;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MineEditBlockChanceGUI extends Gui {

	private final MineImpl mineImpl;
	private final XMaterial material;

	private double currentChance;

	public MineEditBlockChanceGUI(Player player, MineImpl mineImpl, XMaterial material) {
		super(player, 5, "Editing Block Chance");
		this.mineImpl = mineImpl;
		this.material = material;
		this.currentChance = mineImpl.getBlockPaletteImpl().getPercentage(this.material);
	}

	@Override
	public void redraw() {
		this.setItem(4, ItemStackBuilder.of(this.material.parseItem()).name("&eBlock Chance").lore(" ", "&7The chance of spawning this", String.format("&7block is &b%,.2f%%", this.currentChance)).buildItem().build());

		// +
		this.setItem(10, ItemStackBuilder.of(XMaterial.LIME_STAINED_GLASS_PANE.parseItem()).name("&a+0.1").build(() -> {
			handleChanceAddition(0.1);
		}));
		this.setItem(11, ItemStackBuilder.of(XMaterial.LIME_STAINED_GLASS_PANE.parseItem()).name("&a+0.2").build(() -> {
			handleChanceAddition(0.2);
		}));
		this.setItem(12, ItemStackBuilder.of(XMaterial.LIME_STAINED_GLASS_PANE.parseItem()).name("&a+0.5").build(() -> {
			handleChanceAddition(0.5);
		}));

		this.setItem(19, ItemStackBuilder.of(XMaterial.LIME_STAINED_GLASS_PANE.parseItem()).name("&a+1.0").build(() -> {
			handleChanceAddition(1.0);
		}));
		this.setItem(20, ItemStackBuilder.of(XMaterial.LIME_STAINED_GLASS_PANE.parseItem()).name("&a+2.0").build(() -> {
			handleChanceAddition(2.0);
		}));
		this.setItem(21, ItemStackBuilder.of(XMaterial.LIME_STAINED_GLASS_PANE.parseItem()).name("&a+5.0").build(() -> {
			handleChanceAddition(5.0);
		}));

		this.setItem(28, ItemStackBuilder.of(XMaterial.LIME_STAINED_GLASS_PANE.parseItem()).name("&a+10.0").build(() -> {
			handleChanceAddition(10.0);
		}));
		this.setItem(29, ItemStackBuilder.of(XMaterial.LIME_STAINED_GLASS_PANE.parseItem()).name("&a+20.0").build(() -> {
			handleChanceAddition(20.0);
		}));
		this.setItem(30, ItemStackBuilder.of(XMaterial.LIME_STAINED_GLASS_PANE.parseItem()).name("&a+50.0").build(() -> {
			handleChanceAddition(50.0);
		}));

		// -
		this.setItem(14, ItemStackBuilder.of(XMaterial.RED_STAINED_GLASS_PANE.parseItem()).name("&c-0.1").build(() -> {
			handleChanceAddition(-0.1);
		}));
		this.setItem(15, ItemStackBuilder.of(XMaterial.RED_STAINED_GLASS_PANE.parseItem()).name("&c-0.2").build(() -> {
			handleChanceAddition(-0.2);
		}));
		this.setItem(16, ItemStackBuilder.of(XMaterial.RED_STAINED_GLASS_PANE.parseItem()).name("&c-0.5").build(() -> {
			handleChanceAddition(-0.5);
		}));

		this.setItem(23, ItemStackBuilder.of(XMaterial.RED_STAINED_GLASS_PANE.parseItem()).name("&c-1.0").build(() -> {
			handleChanceAddition(-1.0);
		}));
		this.setItem(24, ItemStackBuilder.of(XMaterial.RED_STAINED_GLASS_PANE.parseItem()).name("&c-2.0").build(() -> {
			handleChanceAddition(-2.0);
		}));
		this.setItem(25, ItemStackBuilder.of(XMaterial.RED_STAINED_GLASS_PANE.parseItem()).name("&c-5.0").build(() -> {
			handleChanceAddition(-5.0);
		}));

		this.setItem(32, ItemStackBuilder.of(XMaterial.RED_STAINED_GLASS_PANE.parseItem()).name("&c-10.0").build(() -> {
			handleChanceAddition(-10.0);
		}));
		this.setItem(33, ItemStackBuilder.of(XMaterial.RED_STAINED_GLASS_PANE.parseItem()).name("&c-20.0").build(() -> {
			handleChanceAddition(-20.0);
		}));
		this.setItem(34, ItemStackBuilder.of(XMaterial.RED_STAINED_GLASS_PANE.parseItem()).name("&c-50.0").build(() -> {
			handleChanceAddition(-50.0);
		}));


		this.setItem(36, ItemStackBuilder.of(Material.ARROW).name("&cBack").lore("&7Click to go back to all blocks.").build(() -> {
			this.close();
			new MineBlocksGUI(this.mineImpl, this.getPlayer()).open();
		}));

		this.setItem(40, ItemStackBuilder.of(XMaterial.GREEN_WOOL.parseItem()).name("&aSave").lore("&7Click to save the current chance.").build(() -> {
			this.close();
			if (this.mineImpl.getBlockPaletteImpl().getTotalPercentage() - this.mineImpl.getBlockPaletteImpl().getPercentage(this.material) + this.currentChance > 100.0) {
				this.currentChance = 100 - (this.mineImpl.getBlockPaletteImpl().getTotalPercentage() - this.mineImpl.getBlockPaletteImpl().getPercentage(this.material));
			}
			this.mineImpl.getBlockPaletteImpl().setPercentage(this.material, this.currentChance);
			new MineBlocksGUI(this.mineImpl, this.getPlayer()).open();
		}));

	}

	private void handleChanceAddition(double addition) {
		if (this.currentChance + addition > 100.0 || this.currentChance + addition < 0.0) {
			return;
		}
		this.currentChance += addition;
		this.redraw();
	}
}
