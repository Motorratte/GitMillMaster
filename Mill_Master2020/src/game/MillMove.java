package game;

public class MillMove
{
	private final Field source; //bei einem Zug der ersten Spielphase ist source immer null
	private final Field destination;
	private final int removedToken;
	private final Field attacked;
	private final boolean isAttackMove;

	public MillMove(final Field destination)
	{
		this.destination = destination;
		source = null;
		removedToken = 0;
		attacked = null;
		isAttackMove = false;
	}

	public MillMove(final Field source, final Field destination) //Konstruktor verwendet keinen this(...) aufruf, da die attribute ansonsten nicht final seien dürften
	{
		this.source = source;
		this.destination = destination;
		removedToken = 0;
		attacked = null;
		isAttackMove = false;
	}

	public MillMove(final Field destination, final int removedToken, final Field attacked)
	{
		this.destination = destination;
		this.removedToken = removedToken;
		this.attacked = attacked;
		isAttackMove = true;
		source = null;
	}

	public MillMove(final Field source, final Field destination, final int removedToken, final Field attacked) //Konstruktor verwendet keinen this(...) aufruf, da die attribute ansonsten nicht final seien dürften
	{
		this.source = source;
		this.destination = destination;
		this.removedToken = removedToken;
		this.attacked = attacked;
		isAttackMove = true;
	}

	public MillMove(final Field source, final Field destination, final int removedToken, final Field attacked, final boolean isAttackMove) //Konstruktor verwendet keinen this(...) aufruf, da die attribute ansonsten nicht final seien dürften
	{
		this.source = source;
		this.destination = destination;
		this.removedToken = removedToken;
		this.attacked = attacked;
		this.isAttackMove = isAttackMove;
	}

	public MillMove(final int sourceIndex, final int destinationIndex, final int attackedIndex, Field[] board)
	{
		source = sourceIndex >= 0 ? board[sourceIndex] : null;
		destination = destinationIndex >= 0 ? board[destinationIndex] : null;
		if (attackedIndex >= 0)
		{
			attacked = board[attackedIndex];
			isAttackMove = true;
			removedToken = attacked.getTokenOfPlayerId();
		}
		else
		{
			attacked = null;
			isAttackMove = false;
			removedToken = 0;
		}
	}

	public MillMove cloneMoveForOtherBoard(final Field[] otherBoard)
	{
		final Field newSource = source == null ? null : otherBoard[source.getIdOfField()];
		final Field newDestination = destination == null ? null : otherBoard[destination.getIdOfField()];
		final Field newAttacked = attacked == null ? null : otherBoard[attacked.getIdOfField()];
		return new MillMove(newSource, newDestination, removedToken, newAttacked, isAttackMove);
	}

	public boolean isAttackMove()
	{
		return isAttackMove;
	}

	public Field getSource()
	{
		return source;
	}

	public int getSourceIndex()
	{
		return source == null ? -1 : source.getIdOfField();
	}

	public Field getDestination()
	{
		return destination;
	}

	public int getDestinationIndex()
	{
		return destination == null ? -1 : destination.getIdOfField();
	}

	public Field getAttacked()
	{
		return attacked;
	}

	public int getAttackedIndex()
	{
		return attacked == null ? -1 : attacked.getIdOfField();
	}

	public int getRemovedToken()
	{
		return removedToken;
	}

	@Override
	public boolean equals(Object obj)
	{
		MillMove otherMove = (MillMove) obj;
		return otherMove.source == source && otherMove.destination == destination && otherMove.attacked == attacked;
	}
}
