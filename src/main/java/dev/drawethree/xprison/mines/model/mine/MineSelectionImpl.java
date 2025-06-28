package dev.drawethree.xprison.mines.model.mine;

import dev.drawethree.xprison.api.mines.model.MineSelection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.lucko.helper.serialize.Position;
import me.lucko.helper.serialize.Region;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MineSelectionImpl implements MineSelection {
	private Position position1;
	private Position position2;

	public boolean isValid() {
		return position1 != null && position2 != null;
	}

	public Region toRegion() {
		return position1.regionWith(position2);
	}
}
