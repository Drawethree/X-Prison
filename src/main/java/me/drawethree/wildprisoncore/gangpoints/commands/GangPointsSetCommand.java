package me.drawethree.wildprisoncore.gangpoints.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.wildprisoncore.gangpoints.WildPrisonGangPoints;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import net.brcdev.gangs.GangsPlusApi;
import net.brcdev.gangs.gang.Gang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GangPointsSetCommand extends GangPointsCommand {

	public GangPointsSetCommand(WildPrisonGangPoints plugin) {
		super(plugin);
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {


		if (!sender.isOp()) {
			return false;
		}

		if (args.size() == 2) {
			try {

				Gang gang;

				long amount = Long.parseLong(args.get(0));
				Player target = Players.getNullable(args.get(1));

				if (target == null) {
					gang = GangsPlusApi.getAllGangs().stream().filter(gang1 -> gang1.getRawName().equalsIgnoreCase(args.get(1))).findFirst().get();
				} else {
					gang = GangsPlusApi.getPlayersGang(target);
				}

				if (gang == null) {
					sender.sendMessage(Text.colorize("&cNo gang found."));
					return true;
				}

				plugin.getGangPointsManager().setPoints(gang, amount, sender);
				return true;
			} catch (NumberFormatException e) {
				sender.sendMessage(Text.colorize(String.format("&c%s is not a valid amount!", args.get(0))));
			}
		}
		return false;
	}
}
