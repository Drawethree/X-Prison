package dev.drawethree.xprison.api.enums;

/**
 * ReceiveCause enum for Gems and Tokens events.
 */
public enum ReceiveCause {
	/**
	 * Player received currency during mining (himself)
	 */
	MINING,
	/**
	 * Player received currency by pay command
	 */
	PAY,
	/**
	 * Player received currency by give command
	 */
	GIVE,
	/**
	 * Player received currency by redeeming items
	 */
	REDEEM,
	/**
	 * Player received currency by breaking lucky blocks
	 */
	LUCKY_BLOCK,
	/**
	 * Player received currency by disenchanting
	 */
	REFUND,
	/**
	 * Player received currency by mining (from other players)
	 */
	MINING_OTHERS
}
