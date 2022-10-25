package ai.other;

import game.MillMove;

public class EvaluatedMove implements MoveObject
{
	private MillMove move;
	private int evaluation;

	public EvaluatedMove()
	{

	}

	public EvaluatedMove(final MillMove move)
	{
		this.move = move;
	}

	public EvaluatedMove(final MillMove move, final int evaluation)
	{
		this.move = move;
		this.evaluation = evaluation;
	}

	public int getEvaluation()
	{
		return evaluation;
	}

	public MillMove getMove()
	{
		return move;
	}

	public void setEvaluation(int evaluation)
	{
		this.evaluation = evaluation;
	}

	public void setMove(final MillMove move)
	{
		this.move = move;
	}

	public void addValueToEvaluation(final int value)
	{
		evaluation += value;
	}
}
