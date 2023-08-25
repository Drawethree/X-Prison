package dev.drawethree.xprison.mines.model.mine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.lucko.helper.serialize.Position;
import me.lucko.helper.serialize.Region;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MineSelection {
	private Position pos1;
	private Position pos2;

	public boolean isValid() {
		return pos1 != null && pos2 != null;
	}

	public Region toRegion() {
		return pos1.regionWith(pos2);
	}
}
