package org.zeith.comms.c18telebeacons.utils;

@FunctionalInterface
public interface IRepeatableAction<A>
{
	Do accept(A thing);

	enum Do
	{
		RETURN(false),
		CONTINUE(true);

		boolean mayContinue;

		Do(boolean mayContinue)
		{
			this.mayContinue = mayContinue;
		}

		public boolean canContinue()
		{
			return mayContinue;
		}
	}
}