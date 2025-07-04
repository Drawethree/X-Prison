package dev.drawethree.xprison.bombs;


import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.XPrisonModuleBase;
import dev.drawethree.xprison.api.bombs.XPrisonBombsAPI;
import dev.drawethree.xprison.bombs.api.XPrisonBombsAPIImpl;
import dev.drawethree.xprison.bombs.commands.BombsCommand;
import dev.drawethree.xprison.bombs.config.BombsConfig;
import dev.drawethree.xprison.bombs.listener.BombsListener;
import dev.drawethree.xprison.bombs.repo.BombsRepository;
import dev.drawethree.xprison.bombs.service.BombCooldownService;
import dev.drawethree.xprison.bombs.service.BombsService;
import lombok.Getter;

public final class XPrisonBombs extends XPrisonModuleBase {

	public static final String MODULE_NAME = "Bombs";

	@Getter
	private static XPrisonBombs instance;

	@Getter
	private XPrisonBombsAPI api;

	@Getter
	private BombsConfig config;

	@Getter
	private BombsRepository bombsRepository;

	@Getter
	private BombsService bombsService;

	@Getter
	private BombCooldownService bombCooldownService;

	public XPrisonBombs(XPrison prisonCore) {
		super(prisonCore);
		instance = this;
	}

	@Override
	public void enable() {
		super.enable();
		this.config = new BombsConfig(this);
		this.config.load();

		this.bombsRepository = new BombsRepository(this.config);
		this.bombsRepository.load();

		this.bombsService = new BombsService(this);
		this.bombsService.enable();

		this.bombCooldownService = new BombCooldownService(this);

		BombsCommand command = new BombsCommand(this);
		command.register();

		BombsListener bombsListener = new BombsListener(this);
		bombsListener.register();

		this.api = new XPrisonBombsAPIImpl(this);

		this.enabled = true;

	}

	@Override
	public void disable() {
		super.disable();
		this.bombsService.disable();
		this.enabled = false;
	}

	@Override
	public void reload() {
		super.reload();
		this.config.reload();
		this.bombsRepository.reload();
		this.bombsService.reload();
		this.bombCooldownService.reload();
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public boolean isHistoryEnabled() {
		return false;
	}
}
