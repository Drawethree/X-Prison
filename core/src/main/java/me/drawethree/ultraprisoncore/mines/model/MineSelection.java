package me.drawethree.ultraprisoncore.mines.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.lucko.helper.serialize.Position;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MineSelection {
	private Position pos1;
	private Position pos2;

	public boolean isValid() {
		return pos1 != null && pos2 != null;
	}
}
