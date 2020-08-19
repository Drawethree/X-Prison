package me.drawethree.wildprisoncore.gangpoints;


import lombok.Getter;
import me.drawethree.wildprisoncore.WildPrisonCore;
import me.drawethree.wildprisoncore.gangpoints.api.WildPrisonGangPointsAPI;
import me.drawethree.wildprisoncore.gangpoints.api.WildPrisonGangPointsAPIImpl;
import me.drawethree.wildprisoncore.gangpoints.commands.GangPointsCommand;
import me.drawethree.wildprisoncore.gangpoints.managers.GangPointsManager;
import me.lucko.helper.Commands;
import org.bukkit.entity.Player;

public final class WildPrisonGangPoints {

	public static final String TOKENS_ADMIN_PERM = "wildprison.gangpoints.admin";

	@Getter
	private static WildPrisonGangPoints instance;

	@Getter
	private WildPrisonGangPointsAPI api;

	@Getter
	private GangPointsManager gangPointsManager;
	@Getter
	private WildPrisonCore core;

	public WildPrisonGangPoints(WildPrisonCore wildPrisonCore) {
		instance = this;
		this.core = wildPrisonCore;
		this.gangPointsManager = new GangPointsManager(this);
		this.api = new WildPrisonGangPointsAPIImpl(this.gangPointsManager);
	}


	public void enable() {
		this.registerCommands();
	}


	public void disable() {
		this.gangPointsManager.saveGangsDataOnDisable();
		this.gangPointsManager.stopUpdating();
	}

	private void registerCommands() {
		Commands.create()
				.handler(c -> {

					if (c.args().size() == 0 && c.sender() instanceof Player) {
						this.gangPointsManager.sendPointsMessage((Player) c.sender());
						return;
					}

					GangPointsCommand subCommand = GangPointsCommand.getCommand(c.rawArg(0));

					if (subCommand != null) {
						subCommand.execute(c.sender(), c.args().subList(1, c.args().size()));
					}

				}).registerAndBind(core, "gangpoints");

		Commands.create()
				.handler(c -> {
					if (c.args().size() == 0) {
						this.gangPointsManager.sendGangsTop(c.sender());
					}
				}).registerAndBind(core, "gangtop", "gangpointstop");
	}
}
