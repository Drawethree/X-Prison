package dev.drawethree.xprison.utils.misc;

import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.ProfileInputType;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import dev.drawethree.xprison.utils.compat.MinecraftVersion;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class SkullUtils {

	public static final ItemStack HELP_SKULL = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2YzY2E0ZjdjOTJkZGUzYTc3ZWM1MTBhNzRiYThjMmU4ZDBlYzdiODBmMGUzNDhjYzZkZGRkNmI0NThiZCJ9fX0=");
	public static final ItemStack DIAMOND_R_SKULL = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzkzZWQ4MDdkYmYxNDdjNWVmOWI4ZWM0NmQzZmE2ZTJkN2IyZGJkMzQzMWEyMzQxN2MxMzU0YmI4NjNjNCJ9fX0=");
	public static final ItemStack DIAMOND_P_SKULL = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWIxZTQyNzY3MDkwODI4OTcwYTliNDMyNzYyMDYyZmY2ZGY0Y2JjMjMxMWRlMjNhMWJiNDI1M2VjYjE2OTJjIn19fQ==");
	public static final ItemStack MONEY_SKULL = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTM2ZTk0ZjZjMzRhMzU0NjVmY2U0YTkwZjJlMjU5NzYzODllYjk3MDlhMTIyNzM1NzRmZjcwZmQ0ZGFhNjg1MiJ9fX0=");
	public static final ItemStack COIN_SKULL = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjBhN2I5NGM0ZTU4MWI2OTkxNTlkNDg4NDZlYzA5MTM5MjUwNjIzN2M4OWE5N2M5MzI0OGEwZDhhYmM5MTZkNSJ9fX0=");
	public static final ItemStack GANG_SKULL = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTM3ZjlkYjZlZWNlNDliMmMxZDZkOWVmOTRmNmMxMTQ4OTA0MTIwMjkxMzY1YTE3ZDI3MGY5OGY2MmFlZGUifX19");
	public static final ItemStack INFO_SKULL = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTY0MzlkMmUzMDZiMjI1NTE2YWE5YTZkMDA3YTdlNzVlZGQyZDUwMTVkMTEzYjQyZjQ0YmU2MmE1MTdlNTc0ZiJ9fX0");
	public static final ItemStack COMMAND_BLOCK_SKULL = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWNiYTcyNzdmYzg5NWJmM2I2NzM2OTQxNTk4NjRiODMzNTFhNGQxNDcxN2U0NzZlYmRhMWMzYmYzOGZjZjM3In19fQ==");
	public static final ItemStack CHECK_SKULL = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2UyYTUzMGY0MjcyNmZhN2EzMWVmYWI4ZTQzZGFkZWUxODg5MzdjZjgyNGFmODhlYThlNGM5M2E0OWM1NzI5NCJ9fX0=");
	public static final ItemStack CROSS_SKULL = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTljZGI5YWYzOGNmNDFkYWE1M2JjOGNkYTc2NjVjNTA5NjMyZDE0ZTY3OGYwZjE5ZjI2M2Y0NmU1NDFkOGEzMCJ9fX0=");
	public static final ItemStack DANGER_SKULL = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTRlMWRhODgyZTQzNDgyOWI5NmVjOGVmMjQyYTM4NGE1M2Q4OTAxOGZhNjVmZWU1YjM3ZGViMDRlY2NiZjEwZSJ9fX0");


	public static void init() {

	}

	public static ItemStack getCustomTextureHead(String value) {
		return XSkull.createItem().profile(Profileable.of(ProfileInputType.BASE64, value)).apply();
	}


	public static ItemStack createPlayerHead(OfflinePlayer player, String displayName, List<String> lore) {
		ItemStack baseItem = XSkull.createItem().profile(Profileable.of(player)).apply();
		SkullMeta meta = (SkullMeta) baseItem.getItemMeta();

		if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_13)) {
			meta.setOwningPlayer(player);
		} else {
			meta.setOwner(player.getName());
		}
		baseItem.setItemMeta(meta);
		return ItemStackBuilder.of(baseItem).name(displayName).lore(lore).build();
	}

	private SkullUtils() {
		throw new UnsupportedOperationException("Cannot instantiate");
	}
}
