package ai;

import ai.evaluation.EvaluationModel;
import ai.other.EvaluatedMove;
import game.MillModel;
import game.MillMove;

public class SimpleBot extends Engine
{
	final EvaluationModel eModel;

	public SimpleBot(MillModel millModel, int representsPlayerId, boolean puppetMode)
	{
		super(millModel, representsPlayerId, puppetMode);
		eModel = new EvaluationModel(model);
	}

	@Override
	protected void calculate()
	{
		final int[] evaluationRoot = eModel.generateNewEvaluationRoot();
		final int[] currentEvaluationData = new int[evaluationRoot.length];
		model.generatePossibleMoves();
		final MillMove[] possibleMoves = model.getPossibleMoves();
		final EvaluatedMove[] results = new EvaluatedMove[possibleMoves.length];
		for (int i = 0; i < possibleMoves.length; i++)
		{
			eModel.generateEvaluationDataForMove(currentEvaluationData, evaluationRoot, possibleMoves[i], model.getCurrentPlayerId());
			results[i] = new EvaluatedMove(possibleMoves[i], eModel.evaluatePositionStil1(currentEvaluationData, model.getCurrentPlayerId(), model.getCurrentPlayerId() ^ 1));
			numberOfEvaluations++;
		}
		chooseMove(results, 0);
		MillMove choosed = getCalculatedMove(true);
		eModel.generateEvaluationDataForMove(currentEvaluationData, evaluationRoot, choosed, model.getCurrentPlayerId());
		eModel.evaluatePositionStil1(currentEvaluationData, model.getCurrentPlayerId(), model.getCurrentPlayerId() ^ 1);

		eModel.printEvaluationDataBase(currentEvaluationData);
	}
	@Override
	public String toString()
	{
		return "Simple Bot";
	}
}
