package ai;

import ai.other.DataEvaluatedMove;
import game.MillModel;
import game.MillMove;

public class MillMaster extends EcoBot
{
	private int evaluationDifferenceToWin = 210;
	private int softDepthSplitWide;
	private int[] currentEvaluationAtDepth = new int[128];
	private DataEvaluatedMove currentAnalysedRootMove;

	public MillMaster(MillModel millModel, int representsPlayerId, boolean puppetMode)
	{
		super(millModel, representsPlayerId, puppetMode);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doSomethingBeforeCalculation()
	{
		super.doSomethingBeforeCalculation();
		softDepthSplitWide = 3;
		if (isPlayToWin())
		{
			drawValue = -120;
		}
		else
		{
			drawValue = -10;
		}
		final int numberOfPossibleMoves = model.getNumberOfPossibleMoves();
		DataEvaluatedMove[] movesContainer = possibleEvaluatedMovesDepth[0];
		for (int a = 0; a < numberOfPossibleMoves; a++)
		{
			final int randomeAdd = random.nextInt(5) - 2;
			movesContainer[a].setRandomAdd(randomeAdd);
		}
	}

	@Override
	protected void calculate()
	{
		final long force = 800000000L;
		if (model.getNumberOfPossibleMoves() == 0)
			return;
		chooseMove(masterForceTree(rootEvaluationDataBase, 0, force, false, Integer.MIN_VALUE, Integer.MAX_VALUE, false));
	}
	private boolean checkForWinCutOffAlpha(final int value,final int depth)
	{
		final int valueBefore = currentEvaluationAtDepth[depth - 2];
		return value > valueBefore && valueBefore > currentEvaluationAtDepth[depth - 4];
	}
	private boolean checkForWinCutOffBeta(final int value,final int depth)
	{
		final int valueBefore = currentEvaluationAtDepth[depth - 2];
		return value < valueBefore && valueBefore < currentEvaluationAtDepth[depth - 4];
	}
	private DataEvaluatedMove masterForceTree(final int[] lastMoveEvaluationData, int currentDepth, long forceLeft, boolean softDepth, int alpha, int beta, final boolean invertedPerspective)
	{
		final int numberOfPossibleMoves = model.getNumberOfPossibleMoves();
		final int numberOfMovesToAnalyse;
		if (softDepth)
		{
			numberOfMovesToAnalyse = numberOfPossibleMoves >= softDepthSplitWide ? softDepthSplitWide : numberOfPossibleMoves;
		}
		else
		{
			numberOfMovesToAnalyse = numberOfPossibleMoves;
		}
		long nextForce = forceLeft / numberOfMovesToAnalyse;
		boolean nextSoftDepth = model.getMoveNumber() > 17 && model.getNumberOfStonesByPlayerID(model.getCurrentPlayerId() ^ 1) == 3;
		final boolean reachedHorizont;
		if (nextForce == 0)
		{
			reachedHorizont = true;
			adjustRechedDepth(currentDepth + 1);
		}
		else
		{
			reachedHorizont = false;
		}
		final DataEvaluatedMove[] moves;
		boolean newBest = false;
		DataEvaluatedMove currentBestResult = null;
		int bestFoundValue = invertedPerspective ? beta : alpha;
		if (!reachedHorizont)
		{
			final boolean isRoot = currentDepth == 0;
			if (isRoot)
			{
				moves = ecoCalculation(4);
				for (int i = 0; i < numberOfPossibleMoves; i++)
				{
					moves[i].addRandomValueToEvaluation();
				}
			}
			else
			{
				moves = possibleEvaluatedMovesDepth[currentDepth];
				model.fillWithPossibleMoves(moves);
				eModel.evaluateMoves(moves, numberOfPossibleMoves, model.getCurrentPlayerId(), model.getCurrentPlayerId() ^ 1, lastMoveEvaluationData);
				numberOfEvaluations += numberOfPossibleMoves;
			}
			sort.randomSort(moves, 0, numberOfPossibleMoves);
			sort.mergeSort(moves, 0, numberOfPossibleMoves);
			if (invertedPerspective)
			{
				final int value = -moves[0].getEvaluation();
				if (currentDepth > 3 && checkForWinCutOffBeta(value, currentDepth) || -moves[0].getEvaluation() + evaluationDifferenceToWin <= rootEvaluation)
				{
					final DataEvaluatedMove result = moves[0];
					result.setEvaluation(-winValue + currentDepth);
					adjustRechedDepth(currentDepth + 1);
					return result;
				}
				currentEvaluationAtDepth[currentDepth] = value;
			}
			else
			{
				final int value = moves[0].getEvaluation();
				if (currentDepth > 3 && checkForWinCutOffAlpha(value, currentDepth) || !isRoot && value - evaluationDifferenceToWin >= rootEvaluation || isRoot && value >= winValue)
				{
					final DataEvaluatedMove result = moves[0];
					result.setEvaluation(winValue - currentDepth);
					adjustRechedDepth(currentDepth + 1);
					return result;
				}
				currentEvaluationAtDepth[currentDepth] = value;
			}
			int currentEvaluation;
			for (int i = 0; i < numberOfMovesToAnalyse; i++)
			{
				final DataEvaluatedMove currentMove = moves[i];
				if (isRoot)
				{
					currentAnalysedRootMove = currentMove;
				}
				model.executeMove(currentMove.getMove());
				if (model.gameIsDrawAi(3) || model.getNumberOfStonesByPlayerID(model.getCurrentPlayerId()) == 3 && model.getMoveNumber() > 17 && model.getDistanceToLastClosedMill() > 4)
				{
					currentEvaluation = drawValue;
					adjustRechedDepth(currentDepth + 1);
				}
				else
				{
					model.generatePossibleMoves();
					/*
					 * if(model.gameIsOver()) { model.undoMove();
					 * eModel.printEvaluationDataBase(currentMove.
					 * getEvaluationData());
					 * eModel.evaluatePositionStil1(currentMove.
					 * getEvaluationData(), model.getCurrentPlayerId(),
					 * model.getCurrentPlayerId() ^ 1);
					 * System.out.println("Fehler!");
					 * model.executeMove(currentMove.getMove()); }
					 */
					currentEvaluation = masterForceTree(currentMove.getEvaluationData(), currentDepth + 1, nextForce, nextSoftDepth, alpha, beta, !invertedPerspective).getEvaluation();
				}
				model.undoMove();
				if (invertedPerspective)
				{
					newBest = currentEvaluation < beta;
					if (newBest)
					{
						beta = currentEvaluation;
						bestFoundValue = beta;
					}
				}
				else
				{
					newBest = currentEvaluation > alpha;
					if (newBest)
					{
						alpha = currentEvaluation;
						bestFoundValue = alpha;
					}
				}
				if (newBest)
				{
					currentBestResult = currentMove;
					currentBestResult.setEvaluation(bestFoundValue);
					if (beta <= alpha)
					{
						return currentBestResult;
					}
				}
			}
		}
		else
		{
			moves = possibleEvaluatedMovesDepth[currentDepth];
			final MillMove[] possibleMoves = model.getPossibleMovesNotSafe();
			for (int i = 0; i < numberOfPossibleMoves; i++)
			{
				final MillMove currentMove = possibleMoves[i];
				final DataEvaluatedMove currentResult = moves[i];
				eModel.generateEvaluationDataForMove(currentResult.getEvaluationData(), lastMoveEvaluationData, currentMove, model.getCurrentPlayerId());
				int currentEvaluation = eModel.evaluatePositionStil1(currentResult.getEvaluationData(), model.getCurrentPlayerId(), model.getCurrentPlayerId() ^ 1);
				numberOfEvaluations++;
				if (invertedPerspective)
				{
					currentEvaluation = -currentEvaluation;
					currentEvaluation += currentAnalysedRootMove.getRandomAdd();
					newBest = currentEvaluation < beta;
					if (newBest)
					{
						beta = currentEvaluation;
						bestFoundValue = beta;
						if (currentEvaluation + evaluationDifferenceToWin <= rootEvaluation)
						{
							currentEvaluation = -winValue + currentDepth;
							currentBestResult = currentResult;
							currentBestResult.setEvaluation(bestFoundValue);
							return currentBestResult;
						}
					}
				}
				else
				{
					currentEvaluation += currentAnalysedRootMove.getRandomAdd();
					newBest = currentEvaluation > alpha;
					if (newBest)
					{
						alpha = currentEvaluation;
						bestFoundValue = alpha;
						if (currentEvaluation - evaluationDifferenceToWin >= rootEvaluation)
						{
							currentEvaluation = winValue - currentDepth;
							currentBestResult = currentResult;
							currentBestResult.setEvaluation(bestFoundValue);
							return currentBestResult;
						}
					}
				}
				if (newBest)
				{
					currentBestResult = currentResult;
					currentBestResult.setEvaluation(bestFoundValue);
					if (beta <= alpha)
					{
						return currentBestResult;
					}
				}
			}
		}
		if (currentBestResult == null)
		{
			currentBestResult = moves[0];
			currentBestResult.setEvaluation(bestFoundValue);
			currentBestResult.setMove(null);
		}
		return currentBestResult;
	}

	private void adjustRechedDepth(final int depth)
	{
		if (getReachedDepth() < depth)
			setReachedDepth(depth);
	}

	@Override
	public boolean supportsDifficulty()
	{
		return true;
	}

	@Override
	public int getMaxDifficulty()
	{
		return 1;
	}

	@Override
	public boolean supportsPlayToWin()
	{
		return true;
	}

	@Override
	public String toString()
	{
		return "Mill Master(V0.3)";
	}
}
