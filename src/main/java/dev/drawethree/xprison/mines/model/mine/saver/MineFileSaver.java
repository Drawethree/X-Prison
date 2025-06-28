package dev.drawethree.xprison.mines.model.mine.saver;

import dev.drawethree.xprison.mines.managers.MineManager;
import dev.drawethree.xprison.mines.model.mine.MineImpl;
import me.lucko.helper.gson.GsonProvider;

import java.io.FileWriter;
import java.io.IOException;

import static dev.drawethree.xprison.utils.log.XPrisonLogger.error;

public class MineFileSaver implements MineSaver {

	private final MineManager manager;

	public MineFileSaver(MineManager manager) {
		this.manager = manager;
	}

	@Override
	public void save(MineImpl mineImpl) {
		try (FileWriter writer = new FileWriter(mineImpl.getFile())) {
			GsonProvider.writeObjectPretty(writer, mineImpl.serialize().getAsJsonObject());
		} catch (IOException e) {
			error("Exception happened during saving mine as JSON!");
			e.printStackTrace();
		}
	}
}
