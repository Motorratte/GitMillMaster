package ai;

import ai.evaluation.EvaluationModel;
import ai.other.DataEvaluatedMove;
import ai.other.EvaluatedMove;
import game.MillModel;
import game.MillMove;

public class EcoBot extends Engine
{
	final protected EvaluationModel eModel;
	protected int[] rootEvaluationDataBase;
	protected DataEvaluatedMove[][] possibleEvaluatedMovesDepth;
	private int evaluationDataBaseMoveNumber;
	protected int drawValue = -10;
	protected int winValue = 100000;
	private int evaluationDifferenceToWin = 210;
	protected int rootEvaluation;

	public EcoBot(MillModel millModel, int representsPlayerId, boolean puppetMode)
	{
		super(millModel, representsPlayerId, puppetMode);
		eModel = new EvaluationModel(model);
		possibleEvaluatedMovesDepth = new DataEvaluatedMove[128][75];
		for (int i = 0; i < possibleEvaluatedMovesDepth.length; i++)
		{
			final EvaluatedMove[] currentEvaluatedMoves = possibleEvaluatedMovesDepth[i];
			for (int j = 0; j < currentEvaluatedMoves.length; j++)
			{
				currentEvaluatedMoves[j] = new DataEvaluatedMove();
			}
		}
		initRootInformations();
	}

	private void initRootInformations()
	{
		rootEvaluationDataBase = eModel.generateNewEvaluationRoot();
		evaluationDataBaseMoveNumber = model.getMoveNumber();
	}

	@Override
	protected void doSomethingBeforCalculation()
	{
		super.doSomethingBeforCalculation();
		evaluationDataBaseMoveNumber = eModel.synchronizeModelWithEvaluationDataBase(rootEvaluationDataBase, evaluationDataBaseMoveNumber);
		rootEvaluation = -eModel.evaluatePositionStil1(rootEvaluationDataBase, getRepresentedPlayerId() ^ 1, getRepresentedPlayerId());
		model.generatePossibleMoves();
	}

	@Override
	protected void refreshExtansion()
	{
		initRootInformations();
	}

	@Override
	protected void calculate()
	{
		final int extraDepth = random.nextInt(2) + 2;
		setReachedDepth(extraDepth + 1);
		final int numberOfMoves = model.getNumberOfPossibleMoves();
		DataEvaluatedMove[] possibleMoves = ecoCalculation(extraDepth);
		sort.mergeSort(possibleMoves, 0, numberOfMoves);
		chooseMove(possibleMoves[0]);
	}

	protected DataEvaluatedMove[] ecoCalculation(final int extraDepth)
	{
		final DataEvaluatedMove[] possibleMoves = possibleEvaluatedMovesDepth[0];
		model.fillWithPossibleMoves(possibleMoves);
		final int numberOfMoves = model.getNumberOfPossibleMoves();
		eModel.evaluateMoves(possibleMoves, numberOfMoves, model.getCurrentPlayerId(), model.getCurrentPlayerId() ^ 1, rootEvaluationDataBase);
		numberOfEvaluations += numberOfMoves;
		sort.randomSort(possibleMoves, 0, numberOfMoves);
		sort.mergeSort(possibleMoves, 0, numberOfMoves);

		final int numberOfMovesToAnalyse = numberOfMoves;
		for (int moveIndex = 0; moveIndex < numberOfMovesToAnalyse; moveIndex++)
		{
			final DataEvaluatedMove toExecute = possibleMoves[moveIndex];
			model.executeMove(toExecute.getMove());
			if (model.gameIsDrawAi(3))
			{
				toExecute.setEvaluation(drawValue);
			}
			else if (toExecute.getEvaluation() - evaluationDifferenceToWin >= rootEvaluation)
			{
				toExecute.setEvaluation(winValue + toExecute.getEvaluation());
			}
			else
			{
				toExecute.setEvaluation(randomBestMoveForwardSimulation(toExecute.getEvaluationData(), toExecute.getEvaluation(), 1, extraDepth, true, true));
			}
			model.undoMove();
		}
		sort.randomSort(possibleMoves, 0, numberOfMovesToAnalyse);
		return possibleMoves;
	}

	protected int randomBestMoveForwardSimulation(final int[] lastMoveEvaluationData, final int lastMoveEvaluation, final int currentDepth, final int depthLeft, final boolean checkDraw, final boolean invertedEvaluationPerspective)
	{
		model.generatePossibleMoves();
		int numberOfFoundBestMoves = 0;
		int bestFoundEvaluation = Integer.MIN_VALUE;
		final MillMove[] possibleMoves = model.getPossibleMovesNotSafe();
		final int numberOfPossibleMoves = model.getNumberOfPossibleMoves();
		final DataEvaluatedMove[] currentEvaluatedMoves = possibleEvaluatedMovesDepth[currentDepth];
		for (int i = 0; i < numberOfPossibleMoves; i++)
		{
			final MillMove currentMove = possibleMoves[i];
			eModel.generateEvaluationDataForMove(currentEvaluatedMoves[numberOfFoundBestMoves].getEvaluationData(), lastMoveEvaluationData, currentMove, model.getCurrentPlayerId());
			final int currentEvaluation = eModel.evaluatePositionStil1(currentEvaluatedMoves[numberOfFoundBestMoves].getEvaluationData(), model.getCurrentPlayerId(), model.getCurrentPlayerId() ^ 1);
			numberOfEvaluations++;
			if (currentEvaluation > bestFoundEvaluation)
			{
				final DataEvaluatedMove merk = currentEvaluatedMoves[numberOfFoundBestMoves];
				currentEvaluatedMoves[numberOfFoundBestMoves] = currentEvaluatedMoves[0];
				currentEvaluatedMoves[0] = merk;
				merk.setMove(currentMove);
				numberOfFoundBestMoves = 1;
				bestFoundEvaluation = currentEvaluation;
			}
			else if (currentEvaluation == bestFoundEvaluation)
			{
				currentEvaluatedMoves[numberOfFoundBestMoves++].setMove(currentMove);
			}
		}
		if (invertedEvaluationPerspective)
		{
			bestFoundEvaluation = (~bestFoundEvaluation) + 1;
			if (bestFoundEvaluation + evaluationDifferenceToWin <= rootEvaluation)
			{
				return -winValue + currentDepth;
			}
		}
		else
		{
			if (bestFoundEvaluation - evaluationDifferenceToWin >= rootEvaluation)
			{
				return winValue - currentDepth;
			}
		}
		if (depthLeft == 1)
		{
			return bestFoundEvaluation;
		}
		else
		{
			final DataEvaluatedMove toExecute = currentEvaluatedMoves[random.nextInt(numberOfFoundBestMoves)];
			model.executeMove(toExecute.getMove());
			if (checkDraw && model.gameIsDrawAi(3))
			{
				model.undoMove();
				return drawValue;
			}
			final int result = randomBestMoveForwardSimulation(toExecute.getEvaluationData(), bestFoundEvaluation, currentDepth + 1, depthLeft - 1, false, !invertedEvaluationPerspective);
			model.undoMove();
			return result;
		}

	}

	@Override
	public String toString()
	{
		return "Eco Bot";
	}
}
