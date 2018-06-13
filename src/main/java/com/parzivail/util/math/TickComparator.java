package com.parzivail.util.math;

public class TickComparator
{
	/**
	 * Set this every tick to whatever you're checking
	 **/
	public boolean is = false;
	private boolean was = false;

	/**
	 * Checks if there was a change from true to false
	 *
	 * @return Returns true if there was a change from true to false this tick
	 */
	public boolean changeFalse()
	{
		return !this.is && this.was;
	}

	/**
	 * Checks if there was a change from false to true
	 *
	 * @return Returns true if there was a change from false to true this tick
	 */
	public boolean changeTrue()
	{
		return this.is && !this.was;
	}

	/**
	 * Sets was to is, so we can store last tick's boolean
	 */
	public void swap()
	{
		this.was = this.is;
	}
}
