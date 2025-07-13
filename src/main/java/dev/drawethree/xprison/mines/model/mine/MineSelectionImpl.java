package dev.drawethree.xprison.mines.model.mine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.lucko.helper.serialize.Position;
import me.lucko.helper.serialize.Region;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MineSelectionImpl{
	private Position position1;
	private Position position2;

	public boolean isValid() {
		return position1 != null && position2 != null && Objects.equals(position1.getWorld(),position2.getWorld());
	}

	public Region toRegion() {
		return position1.regionWith(position2);
	}
}
