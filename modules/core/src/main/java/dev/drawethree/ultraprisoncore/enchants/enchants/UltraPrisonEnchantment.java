package dev.drawethree.ultraprisoncore.enchants.enchants;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.enchants.enchants.implementations.*;
import dev.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import dev.drawethree.ultraprisoncore.utils.text.TextUtils;
import lombok.Getter;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Getter
public abstract class UltraPrisonEnchantment implements Refundable {

	private static HashMap<Integer, UltraPrisonEnchantment> allEnchantmentsById = new HashMap<>();
	private static HashMap<String, UltraPrisonEnchantment> allEnchantmentsByName = new HashMap<>();

	protected final UltraPrisonEnchants plugin;

	protected final int id;
	private String rawName;
	private String name;
	private String base64;
	private Material material;
	private List<String> description;
	private boolean enabled;
	private int guiSlot;
	private int maxLevel;
	private long cost;
	private long increaseCost;
	private int requiredPickaxeLevel;
	private boolean messagesEnabled;

	public UltraPrisonEnchantment(UltraPrisonEnchants plugin, int id) {
		this.plugin = plugin;
		this.id = id;
		this.reloadDefaultAttributes();
		this.reload();
	}

	public static Collection<UltraPrisonEnchantment> all() {
		return allEnchantmentsById.values();
	}

	public static UltraPrisonEnchantment getEnchantById(int id) {
		return allEnchantmentsById.get(id);
	}

	public static UltraPrisonEnchantment getEnchantByName(String name) {
		return allEnchantmentsByName.get(name.toLowerCase());
	}

	public static void loadDefaultEnchantments() {
		new EfficiencyEnchant(UltraPrisonEnchants.getInstance()).register();
		new UnbreakingEnchant(UltraPrisonEnchants.getInstance()).register();
		new FortuneEnchant(UltraPrisonEnchants.getInstance()).register();
		new HasteEnchant(UltraPrisonEnchants.getInstance()).register();
		new SpeedEnchant(UltraPrisonEnchants.getInstance()).register();
		new JumpBoostEnchant(UltraPrisonEnchants.getInstance()).register();
		new NightVisionEnchant(UltraPrisonEnchants.getInstance()).register();
		new LuckyBoosterEnchant(UltraPrisonEnchants.getInstance()).register();
		new ExplosiveEnchant(UltraPrisonEnchants.getInstance()).register();
		new LayerEnchant(UltraPrisonEnchants.getInstance()).register();
		new CharityEnchant(UltraPrisonEnchants.getInstance()).register();
		new SalaryEnchant(UltraPrisonEnchants.getInstance()).register();
		new BlessingEnchant(UltraPrisonEnchants.getInstance()).register();
		new TokenatorEnchant(UltraPrisonEnchants.getInstance()).register();
		new KeyFinderEnchant(UltraPrisonEnchants.getInstance()).register();
		new PrestigeFinderEnchant(UltraPrisonEnchants.getInstance()).register();
		new BlockBoosterEnchant(UltraPrisonEnchants.getInstance()).register();
		new KeyallsEnchant(UltraPrisonEnchants.getInstance()).register();

		if (UltraPrisonCore.getInstance().isUltraBackpacksEnabled()) {
			new BackpackAutoSellEnchant(UltraPrisonEnchants.getInstance()).register();
		} else {
			new AutoSellEnchant(UltraPrisonEnchants.getInstance()).register();
		}

		new VoucherFinderEnchant(UltraPrisonEnchants.getInstance()).register();
		new NukeEnchant(UltraPrisonEnchants.getInstance()).register();
		new GemFinderEnchant(UltraPrisonEnchants.getInstance()).register();
		new GangValueFinderEnchant(UltraPrisonEnchants.getInstance()).register();
	}

	public static void reloadAll() {

		allEnchantmentsById.values().forEach(enchant -> {
			enchant.reloadDefaultAttributes();
			enchant.reload();
		});

		UltraPrisonCore.getInstance().getLogger().info(TextUtils.applyColor("&aSuccessfully reloaded all enchants."));
	}

	private void reloadDefaultAttributes() {
		this.rawName = this.plugin.getConfig().get().getString("enchants." + id + ".RawName");
		this.name = TextUtils.applyColor(this.plugin.getConfig().get().getString("enchants." + id + ".Name"));
		this.material = CompMaterial.fromString(this.plugin.getConfig().get().getString("enchants." + id + ".Material")).toMaterial();
		this.description = TextUtils.applyColor(this.plugin.getConfig().get().getStringList("enchants." + id + ".Description"));
		this.enabled = this.plugin.getConfig().get().getBoolean("enchants." + id + ".Enabled");
		this.guiSlot = this.plugin.getConfig().get().getInt("enchants." + id + ".InGuiSlot");
		this.maxLevel = this.plugin.getConfig().get().getInt("enchants." + id + ".Max");
		this.cost = this.plugin.getConfig().get().getLong("enchants." + id + ".Cost");
		this.increaseCost = this.plugin.getConfig().get().getLong("enchants." + id + ".Increase-Cost-by");
		this.requiredPickaxeLevel = this.plugin.getConfig().get().getInt("enchants." + id + ".Pickaxe-Level-Required");
		this.messagesEnabled = this.plugin.getConfig().get().getBoolean("enchants." + id + ".Messages-Enabled", true);
		this.base64 = this.plugin.getConfig().get().getString("enchants." + id + ".Base64", null);
	}

	public abstract String getAuthor();

	public abstract void onEquip(Player p, ItemStack pickAxe, int level);

	public abstract void onUnequip(Player p, ItemStack pickAxe, int level);

	public abstract void onBlockBreak(BlockBreakEvent e, int enchantLevel);

	public abstract void reload();

	public long getCostOfLevel(int level) {
		return (this.cost + (this.increaseCost * (level - 1)));
	}

	@Override
	public boolean isRefundEnabled() {
		return this.plugin.getConfig().get().getBoolean("enchants." + this.id + ".Refund.Enabled");
	}

	@Override
	public int refundGuiSlot() {
		return this.plugin.getConfig().get().getInt("enchants." + this.id + ".Refund.InGuiSlot");
	}

	public void register() {

		if (allEnchantmentsById.containsKey(this.getId()) || allEnchantmentsByName.containsKey(this.getRawName())) {
			UltraPrisonCore.getInstance().getLogger().warning(TextUtils.applyColor("&cUnable to register enchant " + this.getName() + "&c created by " + this.getAuthor() + ". That enchant is already registered."));
			return;
		}

		Validate.notNull(this.getRawName());

		allEnchantmentsById.put(this.getId(), this);
		allEnchantmentsByName.put(this.getRawName().toLowerCase(), this);

		UltraPrisonCore.getInstance().getLogger().info(TextUtils.applyColor("&aSuccessfully registered enchant " + this.getName() + "&a created by " + this.getAuthor()));
	}

	public void unregister() {

		if (!allEnchantmentsById.containsKey(this.getId()) && !allEnchantmentsByName.containsKey(this.getRawName())) {
			UltraPrisonCore.getInstance().getLogger().warning(TextUtils.applyColor("&cUnable to unregister enchant " + this.getName() + "&c created by " + this.getAuthor() + ". That enchant is not registered."));
			return;
		}

		allEnchantmentsById.remove(this.getId());
		allEnchantmentsByName.remove(this.getRawName());

		UltraPrisonCore.getInstance().getLogger().info(TextUtils.applyColor("&aSuccessfully unregistered enchant " + this.getName() + "&a created by " + this.getAuthor()));
	}

	public int getMaxLevel() {
		return this.maxLevel == -1 ? Integer.MAX_VALUE : this.maxLevel;
	}

	public boolean canBeBought(ItemStack pickAxe) {
		if (!this.plugin.getCore().isModuleEnabled("Pickaxe Levels")) {
			return true;
		}
		return this.plugin.getCore().getPickaxeLevels().getPickaxeLevel(pickAxe).getLevel() >= this.requiredPickaxeLevel;
	}
}
