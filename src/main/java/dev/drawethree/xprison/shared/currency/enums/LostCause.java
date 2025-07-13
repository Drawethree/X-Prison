package dev.drawethree.xprison.shared.currency.enums;

/**
 * Enum representing various reasons why a player might lose currency.
 */
public enum LostCause {

	/**
	 * Player lost currency by using the pay command.
	 */
	PAY,

	/**
	 * Player lost currency by enchanting items.
	 */
	ENCHANT,

	/**
	 * Player lost currency by prestiging.
	 */
	PRESTIGE,

	/**
	 * Player lost currency by ranking up.
	 */
	RANKUP,

	/**
	 * Player lost currency by an admin running a command affecting currency.
	 */
	ADMIN,

	/**
	 * Player lost currency by withdrawing it to physical form.
	 */
	WITHDRAW,

}
