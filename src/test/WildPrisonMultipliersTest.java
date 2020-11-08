import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WildPrisonMultipliersTest {

	@Test
	public void getPrestigeMultiplierTest() {

		assertEquals(1999, getPrestigeMultiplier(4_999_999));
		assertEquals(2000, getPrestigeMultiplier(5_000_000));
		assertEquals(2000 + 300, getPrestigeMultiplier(6_500_000));


	}


	private long getPrestigeMultiplier(int prestige) {

		long totalMulti = 0;

		long baseIncrement = 5000000;
		long baseMultiIncrement = 2500;

		for (long i = 0, m = 2500; i < 100000000; i += baseIncrement, m += baseMultiIncrement) {

			if (prestige > i && prestige <= i + baseIncrement) {
				totalMulti += (prestige - i) / m;
				break;
			} else if (prestige > (i + baseIncrement)) {
				totalMulti += (baseIncrement / baseMultiIncrement);
				continue;
			}
		}

		return totalMulti;
		//return (playerPrestige / this.perPrestigeMultiplier) * this.prestigeMultiplier;
	}
}