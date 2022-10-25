package ai;

import ai.other.EvaluatedMove;
import game.MillModel;
import game.MillMove;

public class StupidBot extends Engine
{

	public StupidBot(MillModel millModel, int representsPlayerId, boolean puppetMode)
	{
		super(millModel, representsPlayerId, puppetMode);
	}

	@Override
	protected void calculate()
	{
		model.generatePossibleMoves();
		final MillMove[] possibleMoves = model.getPossibleMoves();
		chooseMove(new EvaluatedMove(possibleMoves[random.nextInt(possibleMoves.length)]));
	}
	@Override
	public String toString()
	{
		return "Stupid Bot";
	}
}
