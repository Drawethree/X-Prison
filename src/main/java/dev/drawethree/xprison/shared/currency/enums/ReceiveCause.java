package dev.drawethree.xprison.shared.currency.enums;

/**
 * Enum representing various causes for a player receiving Gems or Tokens.
 */
public enum ReceiveCause {

	/**
	 * Player received currency by mining (their own mining).
	 */
	MINING,

	/**
	 * Player received currency by using the pay command.
	 */
	PAY,

	/**
	 * Player received currency by the give command.
	 */
	GIVE,

	/**
	 * Player received currency by redeeming items.
	 */
	REDEEM,

	/**
	 * Player received currency by breaking lucky blocks.
	 */
	LUCKY_BLOCK,

	/**
	 * Player received currency by disenchanting items (refund).
	 */
	REFUND,

	/**
	 * Player received currency by mining from other players.
	 */
	MINING_OTHERS

}
