package dev.drawethree.ultraprisoncore.enchants.command;

import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.enchants.model.UltraPrisonEnchantment;
import dev.drawethree.ultraprisoncore.enchants.repo.EnchantsRepository;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import me.lucko.helper.Commands;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class GivePickaxeCommand {

	private final UltraPrisonEnchants plugin;

	public GivePickaxeCommand(UltraPrisonEnchants plugin) {
		this.plugin = plugin;
	}

	public void register() {
		Commands.create()
				.assertOp()
				.handler(c -> {

					if (c.args().size() == 0) {
						PlayerUtils.sendMessage(c.sender(), "&c/givepickaxe <player> <[enchant1]=[level1],[enchant2]=[level2],...[enchantX]=[levelX]> <pickaxe_name>");
						return;
					}

					String input = null, name = null;
					Player target = null;

					if (c.args().size() == 1) {
						input = c.rawArg(0);
					} else if (c.args().size() == 2) {
						target = c.arg(0).parseOrFail(Player.class);
						input = c.rawArg(1);
					} else if (c.args().size() >= 3) {
						target = c.arg(0).parseOrFail(Player.class);
						input = c.rawArg(1);
						name = StringUtils.join(c.args().subList(2, c.args().size()), " ");
					}

					Map<UltraPrisonEnchantment, Integer> enchants = parseEnchantsFromInput(input);

					this.plugin.getEnchantsManager().givePickaxe(target, enchants, name, c.sender());
				}).registerAndBind(this.plugin.getCore(), "givepickaxe");
	}


	private Map<UltraPrisonEnchantment, Integer> parseEnchantsFromInput(String input) {
		Map<UltraPrisonEnchantment, Integer> enchants = new HashMap<>();

		String[] split = input.split(",");
		for (String s : split) {
			String[] enchantData = s.split("=");

			try {
				UltraPrisonEnchantment enchantment = getEnchantsRepository().getEnchantByName(enchantData[0]);
				if (enchantment == null) {
					enchantment = getEnchantsRepository().getEnchantById(Integer.parseInt(enchantData[0]));
				}

				if (enchantment == null) {
					continue;
				}

				int enchantLevel = Integer.parseInt(enchantData[1]);
				enchants.put(enchantment, enchantLevel);
			} catch (Exception ignored) {

			}
		}
		return enchants;
	}

	private EnchantsRepository getEnchantsRepository() {
		return this.plugin.getEnchantsRepository();
	}
}
