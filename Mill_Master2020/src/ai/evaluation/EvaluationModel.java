package ai.evaluation;

import ai.other.DataEvaluatedMove;
import game.Field;
import game.MillModel;
import game.MillMove;
import game.MillRow;

public class EvaluationModel
{
	private final MillModel model;
	//private final int[] generatedDifference;

	private final int NUMBER_OF_USED_IMPORTANT_FIELDS_INDEX = 0; //immer der index + playerId
	private final int NUMBER_OF_STONES_IN_OUTER_RING_INDEX = 2;
	private final int NUMBER_OF_STONES_IN_INNER_RING_INDEX = 4;
	private final int NUMBER_OF_FREEDOMES_INDEX = 6;
	private final int NUMBER_OF_CLOSED_AND_BLOCKED_OR_BLOCKABLE_MILLS_INDEX = 8;
	private final int NUMBER_OF_CLOSED_NOT_BLOCKABLE_MILLS_INDEX = 10;
	private final int NUMBER_OF_OPEN_TWO_STONES_INDEX = 12;
	private final int NUMBER_OF_OPEN_BUT_BLOCKED_MILLS_INDEX = 14;
	private final int NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX = 16;
	private final int NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX = 18;
	private final int NUMBER_OF_CATCHED_TOKENS_INDEX = 20;

	public EvaluationModel(final MillModel model)
	{
		this.model = model;
		//generatedDifference = new int[22];
	}

	public int evaluatePositionStil1(final int[] evaluationData, final int playerId, final int enemiePlayerId)
	{
		int currentEvaluation = 0;
		int currentDataPlayerId = evaluationData[NUMBER_OF_USED_IMPORTANT_FIELDS_INDEX | playerId];
		int currentDataEnemiePlayerId = evaluationData[NUMBER_OF_USED_IMPORTANT_FIELDS_INDEX | enemiePlayerId];
		if (currentDataPlayerId >= 3)
		{
			currentEvaluation += 3 - currentDataEnemiePlayerId;
		}
		else if (currentDataEnemiePlayerId >= 3)
		{
			currentEvaluation += currentDataPlayerId - 3;
		}
		else
		{
			currentEvaluation += currentDataPlayerId - currentDataEnemiePlayerId;
		}

		if (evaluationData[NUMBER_OF_STONES_IN_OUTER_RING_INDEX | playerId] > 0)
			currentEvaluation++;
		if (evaluationData[NUMBER_OF_STONES_IN_OUTER_RING_INDEX | enemiePlayerId] > 0)
			currentEvaluation--;
		if (evaluationData[NUMBER_OF_STONES_IN_INNER_RING_INDEX | playerId] > 0)
			currentEvaluation++;
		if (evaluationData[NUMBER_OF_STONES_IN_INNER_RING_INDEX | enemiePlayerId] > 0)
			currentEvaluation--;

		final boolean playerInFinalStage = model.getNumberOfStonesByPlayerID(playerId) == 3;
		final int numberOfCatchedTokensByMe = evaluationData[NUMBER_OF_CATCHED_TOKENS_INDEX | playerId];
		final boolean enemiePlayerInFinalStage = numberOfCatchedTokensByMe >= 6;
		
		currentDataPlayerId = evaluationData[NUMBER_OF_FREEDOMES_INDEX | playerId];
		currentDataEnemiePlayerId = evaluationData[NUMBER_OF_FREEDOMES_INDEX | enemiePlayerId];
		if (currentDataPlayerId < 3)
		{
			if (currentDataPlayerId == 2)
			{
				currentEvaluation -= 12;
			}
			else
			{
				currentEvaluation -= 30;
			}
		}
		if (currentDataEnemiePlayerId < 3 && model.getMoveNumber() > 0)
		{
			if (currentDataEnemiePlayerId == 2)
			{
				currentEvaluation += 12;
			}
			else if(currentDataEnemiePlayerId == 1 ||  enemiePlayerInFinalStage)
			{
				currentEvaluation += 30;
			}
			else if(model.getMoveNumber() > 16)
			{
				currentEvaluation += 800;
			}
		}
		currentEvaluation += (currentDataPlayerId << 3) - (currentDataEnemiePlayerId << 3);

		currentDataPlayerId = evaluationData[NUMBER_OF_OPEN_TWO_STONES_INDEX | playerId];
		if (currentDataPlayerId > 1 && (model.getMoveNumber() < 16 || playerInFinalStage))
		{
			currentEvaluation += 10;
		}
		else
		{
			currentDataPlayerId += currentDataPlayerId << 1;
		}
		currentDataEnemiePlayerId = evaluationData[NUMBER_OF_OPEN_TWO_STONES_INDEX | enemiePlayerId];
		if (currentDataEnemiePlayerId > 0 && (model.getMoveNumber() < 17 || enemiePlayerInFinalStage))
		{
			currentEvaluation -= 20;
		}
		else
		{
			currentDataPlayerId -= currentDataEnemiePlayerId << 1;
		}

		if (model.getMoveNumber() >= 17)
		{
			currentEvaluation += (evaluationData[NUMBER_OF_CLOSED_AND_BLOCKED_OR_BLOCKABLE_MILLS_INDEX | playerId] << 2) - (evaluationData[NUMBER_OF_CLOSED_AND_BLOCKED_OR_BLOCKABLE_MILLS_INDEX | enemiePlayerId] << 2);
			final int numberOfClosedUsableMillsPlayer = evaluationData[NUMBER_OF_CLOSED_NOT_BLOCKABLE_MILLS_INDEX | playerId];
			currentEvaluation += (numberOfClosedUsableMillsPlayer << 5) - (evaluationData[NUMBER_OF_CLOSED_NOT_BLOCKABLE_MILLS_INDEX | enemiePlayerId] << 5);
			final int numberOfOpenUsableMillsEnemie = enemiePlayerInFinalStage ? currentDataEnemiePlayerId : (evaluationData[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | enemiePlayerId] + evaluationData[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | enemiePlayerId]);
			if (numberOfOpenUsableMillsEnemie > 0)
			{
				if (playerInFinalStage)
				{
					currentEvaluation -= 400;
				}
				else
				{
					currentEvaluation -= 64 + (numberOfOpenUsableMillsEnemie << 2);
				}
			}
			final int numberOfOpenBlockablesPlayer = evaluationData[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | playerId];
			final int numberOfOpenNotBlockablesPlayer = evaluationData[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | playerId];
			currentEvaluation += ((evaluationData[NUMBER_OF_OPEN_BUT_BLOCKED_MILLS_INDEX | playerId] + numberOfOpenBlockablesPlayer + numberOfOpenNotBlockablesPlayer
					- evaluationData[NUMBER_OF_OPEN_BUT_BLOCKED_MILLS_INDEX | enemiePlayerId]) << 1);
			if (numberOfOpenUsableMillsEnemie == 0 || numberOfClosedUsableMillsPlayer > 0)
			{
				currentEvaluation += (numberOfOpenBlockablesPlayer + numberOfOpenNotBlockablesPlayer) << 2;
				if (numberOfOpenNotBlockablesPlayer > 0 && !playerInFinalStage)
					currentEvaluation += 64;
			}
		}
		currentEvaluation += (numberOfCatchedTokensByMe - evaluationData[NUMBER_OF_CATCHED_TOKENS_INDEX | enemiePlayerId]) * 64;
		if(evaluationData[NUMBER_OF_CATCHED_TOKENS_INDEX | playerId] >= 7)
		{
			currentEvaluation += 800;
		}
		
		return currentEvaluation;
	}

	public void printEvaluationDataBase(final int[] currentEvaluationDataBase)
	{
		System.out.println();
		System.out.println("NUMBER_OF_USED_IMPORTANT_FIELDS: " + currentEvaluationDataBase[NUMBER_OF_USED_IMPORTANT_FIELDS_INDEX] + " | " + currentEvaluationDataBase[NUMBER_OF_USED_IMPORTANT_FIELDS_INDEX | 1]);
		System.out.println("NUMBER_OF_STONES_IN_OUTER_RING: " + currentEvaluationDataBase[NUMBER_OF_STONES_IN_OUTER_RING_INDEX] + " | " + currentEvaluationDataBase[NUMBER_OF_STONES_IN_OUTER_RING_INDEX | 1]);
		System.out.println("NUMBER_OF_STONES_IN_INNER_RING: " + currentEvaluationDataBase[NUMBER_OF_STONES_IN_INNER_RING_INDEX] + " | " + currentEvaluationDataBase[NUMBER_OF_STONES_IN_INNER_RING_INDEX | 1]);
		System.out.println("NUMBER_OF_FREEDOMES: " + currentEvaluationDataBase[NUMBER_OF_FREEDOMES_INDEX] + " | " + currentEvaluationDataBase[NUMBER_OF_FREEDOMES_INDEX | 1]);
		System.out.println("NUMBER_OF_CLOSED_AND_BLOCKED_OR_BLOCKABLE_MILLS: " + currentEvaluationDataBase[NUMBER_OF_CLOSED_AND_BLOCKED_OR_BLOCKABLE_MILLS_INDEX] + " | "
				+ currentEvaluationDataBase[NUMBER_OF_CLOSED_AND_BLOCKED_OR_BLOCKABLE_MILLS_INDEX | 1]);
		System.out.println("NUMBER_OF_CLOSED_NOT_BLOCKABLE_MILLS: " + currentEvaluationDataBase[NUMBER_OF_CLOSED_NOT_BLOCKABLE_MILLS_INDEX] + " | " + currentEvaluationDataBase[NUMBER_OF_CLOSED_NOT_BLOCKABLE_MILLS_INDEX | 1]);
		System.out.println("NUMBER_OF_OPEN_TWO_STONES: " + currentEvaluationDataBase[NUMBER_OF_OPEN_TWO_STONES_INDEX] + " | " + currentEvaluationDataBase[NUMBER_OF_OPEN_TWO_STONES_INDEX | 1]);
		System.out.println("NUMBER_OF_OPEN_BUT_BLOCKED_MILLS: " + currentEvaluationDataBase[NUMBER_OF_OPEN_BUT_BLOCKED_MILLS_INDEX] + " | " + currentEvaluationDataBase[NUMBER_OF_OPEN_BUT_BLOCKED_MILLS_INDEX | 1]);
		System.out.println("NUMBER_OF_OPEN_BLOCKABLE_MILLS: " + currentEvaluationDataBase[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX] + " | " + currentEvaluationDataBase[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | 1]);
		System.out.println("NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS: " + currentEvaluationDataBase[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX] + " | " + currentEvaluationDataBase[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | 1]);
		System.out.println("NUMBER_OF_CATCHED_TOKENS: " + currentEvaluationDataBase[NUMBER_OF_CATCHED_TOKENS_INDEX] + " | " + currentEvaluationDataBase[NUMBER_OF_CATCHED_TOKENS_INDEX | 1]);
		System.out.println();
	}

	public int[] generateNewEvaluationRoot()
	{
		final int[] evaluationDataBase = new int[22];
		model.synchronizeEvaluationDataBaseWithModel(this, 0, evaluationDataBase);
		return evaluationDataBase;
	}
	public int synchronizeModelWithEvaluationDataBase(final int[] evaluationDataBase, final int evaluaionDataBaseMoveNumber)
	{
		model.synchronizeEvaluationDataBaseWithModel(this, evaluaionDataBaseMoveNumber, evaluationDataBase);
		return model.getMoveNumber();
	}
	public void evaluateMoves(final DataEvaluatedMove[] toFill, final int numberOfMoves,final int playerId, final int enemiePlayerId,final int[] evaluationDataBase)
	{
		DataEvaluatedMove current;
		for(int moveIndex = 0;moveIndex < numberOfMoves;moveIndex++)
		{
			current = toFill[moveIndex];
			generateEvaluationDataForMove(current.getEvaluationData(), evaluationDataBase, current.getMove(), playerId);
			current.setEvaluation(evaluatePositionStil1(current.getEvaluationData(), playerId, enemiePlayerId));
		}
	}
	public void generateEvaluationDataForMove(final int[] toFill, final int[] currentEvaluationDataBase, final MillMove move, final int playerId)
	{
		final Field sourceField = move.getSource();
		final Field destinationField = move.getDestination();
		final Field attackField = move.getAttacked();
		final boolean hasSource = sourceField != null;
		final boolean isAttackMove = move.isAttackMove();
		final int enemiePlayerId = playerId ^ 1;

		//auf important fields prüfen..
		int currentOwnIndex = NUMBER_OF_USED_IMPORTANT_FIELDS_INDEX | playerId;
		int currentEnemieIndex = NUMBER_OF_USED_IMPORTANT_FIELDS_INDEX | enemiePlayerId;
		int currentValueOwn = currentEvaluationDataBase[currentOwnIndex];
		int currentValueEnemie = currentEvaluationDataBase[currentEnemieIndex];
		if (hasSource && sourceField.isVeryImportantField())
			currentValueOwn--;
		if (destinationField.isVeryImportantField())
			currentValueOwn++;
		if (isAttackMove && attackField.isVeryImportantField())
			currentValueEnemie--;
		toFill[currentOwnIndex] = currentValueOwn;
		toFill[currentEnemieIndex] = currentValueEnemie;

		//auf number of stones in outer ring prüfen..
		currentOwnIndex = NUMBER_OF_STONES_IN_OUTER_RING_INDEX | playerId;
		currentEnemieIndex = NUMBER_OF_STONES_IN_OUTER_RING_INDEX | enemiePlayerId;
		currentValueOwn = currentEvaluationDataBase[currentOwnIndex];
		currentValueEnemie = currentEvaluationDataBase[currentEnemieIndex];
		if (hasSource && sourceField.isOuterRingField())
			currentValueOwn--;
		if (destinationField.isOuterRingField())
			currentValueOwn++;
		if (isAttackMove && attackField.isOuterRingField())
			currentValueEnemie--;
		toFill[currentOwnIndex] = currentValueOwn;
		toFill[currentEnemieIndex] = currentValueEnemie;

		//auf numberOfStones in inner ring prüfen..
		currentOwnIndex = NUMBER_OF_STONES_IN_INNER_RING_INDEX | playerId;
		currentEnemieIndex = NUMBER_OF_STONES_IN_INNER_RING_INDEX | enemiePlayerId;
		currentValueOwn = currentEvaluationDataBase[currentOwnIndex];
		currentValueEnemie = currentEvaluationDataBase[currentEnemieIndex];
		if (hasSource && sourceField.isInnerRingField())
			currentValueOwn--;
		if (destinationField.isInnerRingField())
			currentValueOwn++;
		if (isAttackMove && attackField.isInnerRingField())
			currentValueEnemie--;
		toFill[currentOwnIndex] = currentValueOwn;
		toFill[currentEnemieIndex] = currentValueEnemie;

		//zug außerhalb des modells ausführen...
		// und auf number of freedomes sowie nach änderungen bei anzahl geschlossener mühlen prüfen
		for (int i = NUMBER_OF_CLOSED_AND_BLOCKED_OR_BLOCKABLE_MILLS_INDEX; i < toFill.length; i++)
			toFill[i] = currentEvaluationDataBase[i];
		currentOwnIndex = NUMBER_OF_FREEDOMES_INDEX | playerId;
		currentEnemieIndex = NUMBER_OF_FREEDOMES_INDEX | enemiePlayerId;
		currentValueOwn = currentEvaluationDataBase[currentOwnIndex];
		currentValueEnemie = currentEvaluationDataBase[currentEnemieIndex];
		boolean sourceHorizontalMillIsOpenNow = false;
		boolean attackHorizontalMillIsOpenNow = false;
		boolean horizontalMillIsClosedNow = false;
		boolean sourceVerticalMillIsOpenNow = false;
		boolean attackVerticalMillIsOpenNow = false;
		boolean verticalMillIsClosedNow = false;
		if (hasSource)
		{
			sourceField.removeToken();
			sourceHorizontalMillIsOpenNow = sourceField.becomesHorizontalMillByPlayerId(playerId);
			sourceVerticalMillIsOpenNow = sourceField.becomesVerticalMillByPlayerId(playerId);
			currentValueOwn += sourceField.getLastEmptyFieldDifferenceResultByPlayerId(playerId);
			currentValueEnemie += sourceField.getLastEmptyFieldDifferenceResultByPlayerId(enemiePlayerId);
		}

		horizontalMillIsClosedNow = destinationField.becomesHorizontalMillByPlayerId(playerId);
		verticalMillIsClosedNow = destinationField.becomesVerticalMillByPlayerId(playerId);
		destinationField.addToken(playerId);
		currentValueOwn += destinationField.getLastEmptyFieldDifferenceResultByPlayerId(playerId);
		currentValueEnemie += destinationField.getLastEmptyFieldDifferenceResultByPlayerId(enemiePlayerId);
		if (isAttackMove)
		{
			attackField.removeToken();
			attackHorizontalMillIsOpenNow = attackField.becomesHorizontalMillByPlayerId(enemiePlayerId);
			attackVerticalMillIsOpenNow = attackField.becomesVerticalMillByPlayerId(enemiePlayerId);
			currentValueOwn += attackField.getLastEmptyFieldDifferenceResultByPlayerId(playerId);
			currentValueEnemie += attackField.getLastEmptyFieldDifferenceResultByPlayerId(enemiePlayerId);
			toFill[NUMBER_OF_CATCHED_TOKENS_INDEX | playerId]++;
			attackField.addToken(enemiePlayerId);
			checkForDirectChangesOnMoveInverted(toFill, attackField.getVerticalRow(), enemiePlayerId, playerId, attackVerticalMillIsOpenNow, attackField);
			checkForDirectChangesOnMoveInverted(toFill, attackField.getHorizontalRow(), enemiePlayerId, playerId, attackHorizontalMillIsOpenNow, attackField);
			checkForInDirectChangesOnMoveInverted(toFill, attackField, enemiePlayerId, playerId);
		}
		toFill[currentOwnIndex] = currentValueOwn;
		toFill[currentEnemieIndex] = currentValueEnemie;

		//auf mühlen prüfen
		/*
		 * if(!millIsClosedNow && !sourceMillIsOpenNow && !attackMillIsOpenNow)
		 * //anzahl geschlossener mühlen unverändert? { for(int i =
		 * NUMBER_OF_CLOSED_AND_BLOCKED_OR_BLOCKABLE_MILLS_INDEX; i <
		 * NUMBER_OF_OPEN_TWO_STONES_INDEX;i++) { toFill[i] =
		 * currentEvaluationDataBase[i]; } } else {
		 * 
		 * }
		 */

		checkForDirectChangesOnMove(toFill, destinationField.getVerticalRow(), playerId, enemiePlayerId, verticalMillIsClosedNow, destinationField);
		checkForDirectChangesOnMove(toFill, destinationField.getHorizontalRow(), playerId, enemiePlayerId, horizontalMillIsClosedNow, destinationField);
		checkForInDirectChangesOnMove(toFill, destinationField, playerId, enemiePlayerId);

		//zug außerhalb des modells zurücknehmen...
		destinationField.removeToken();
		if (hasSource)
		{
			sourceField.addToken(playerId);
			checkForDirectChangesOnMoveInverted(toFill, sourceField.getVerticalRow(), playerId, enemiePlayerId, sourceVerticalMillIsOpenNow, sourceField);
			checkForDirectChangesOnMoveInverted(toFill, sourceField.getHorizontalRow(), playerId, enemiePlayerId, sourceHorizontalMillIsOpenNow, sourceField);
			checkForInDirectChangesOnMoveInverted(toFill, sourceField, playerId, enemiePlayerId);
		}

	}

	private void checkForInDirectChangesOnMove(final int[] toFill, final Field currentField, final int playerId, final int enemiePlayerId)
	{
		Field currentNeighbourField = currentField.getTopNeighbour();
		if (currentNeighbourField != null)
		{
			if (currentNeighbourField.becomesHorizontalMillByPlayerId(playerId))
			{
				if (currentNeighbourField.isEmpty())
				{
					if (currentNeighbourField.fieldHasNeighboursWithPlayerId(enemiePlayerId))
					{
						toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | playerId]++;
					}
					else
					{
						toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | playerId]++;
					}
				}
				else if (currentNeighbourField.getTokenOfPlayerId() == enemiePlayerId)
				{
					toFill[NUMBER_OF_OPEN_BUT_BLOCKED_MILLS_INDEX | playerId]++;
				}
			}
			else if (currentNeighbourField.isEmpty() && currentNeighbourField.becomesHorizontalMillByPlayerId(enemiePlayerId))
			{
				final Field neighbourOfNeighbour = currentNeighbourField.getTopNeighbour();
				if (neighbourOfNeighbour != null && !neighbourOfNeighbour.isEmpty() && neighbourOfNeighbour.getTokenOfPlayerId() == enemiePlayerId)
				{
					toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | enemiePlayerId]++;
					toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | enemiePlayerId]--;
				}
			}
			else if (!currentNeighbourField.isEmpty() && currentNeighbourField.getHorizontalRow().getNumberOfTokensByPlayerId(enemiePlayerId) == 3)
			{
				final Field neighbourOfNeighbour = currentNeighbourField.getTopNeighbour();
				if ((neighbourOfNeighbour == null || neighbourOfNeighbour.isEmpty() || neighbourOfNeighbour.getTokenOfPlayerId() != playerId) && millIsBlockableByPlayerID(playerId, currentNeighbourField.getHorizontalRow()))
				{
					toFill[NUMBER_OF_CLOSED_NOT_BLOCKABLE_MILLS_INDEX | enemiePlayerId]--;
					toFill[NUMBER_OF_CLOSED_AND_BLOCKED_OR_BLOCKABLE_MILLS_INDEX | enemiePlayerId]++;
				}
			}
		}
		currentNeighbourField = currentField.getBottomNeighbour();
		if (currentNeighbourField != null)
		{
			if (currentNeighbourField.becomesHorizontalMillByPlayerId(playerId))
			{
				if (currentNeighbourField.isEmpty())
				{
					if (currentNeighbourField.fieldHasNeighboursWithPlayerId(enemiePlayerId))
					{
						toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | playerId]++;
					}
					else
					{
						toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | playerId]++;
					}
				}
				else if (currentNeighbourField.getTokenOfPlayerId() == enemiePlayerId)
				{
					toFill[NUMBER_OF_OPEN_BUT_BLOCKED_MILLS_INDEX | playerId]++;
				}
			}
			else if (currentNeighbourField.isEmpty() && currentNeighbourField.becomesHorizontalMillByPlayerId(enemiePlayerId))
			{
				final Field neighbourOfNeighbour = currentNeighbourField.getBottomNeighbour();
				if (neighbourOfNeighbour != null && !neighbourOfNeighbour.isEmpty() && neighbourOfNeighbour.getTokenOfPlayerId() == enemiePlayerId)
				{
					toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | enemiePlayerId]++;
					toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | enemiePlayerId]--;
				}
			}
			else if (!currentNeighbourField.isEmpty() && currentNeighbourField.getHorizontalRow().getNumberOfTokensByPlayerId(enemiePlayerId) == 3)
			{
				final Field neighbourOfNeighbour = currentNeighbourField.getBottomNeighbour();
				if ((neighbourOfNeighbour == null || neighbourOfNeighbour.isEmpty() || neighbourOfNeighbour.getTokenOfPlayerId() != playerId) && millIsBlockableByPlayerID(playerId, currentNeighbourField.getHorizontalRow()))
				{
					toFill[NUMBER_OF_CLOSED_NOT_BLOCKABLE_MILLS_INDEX | enemiePlayerId]--;
					toFill[NUMBER_OF_CLOSED_AND_BLOCKED_OR_BLOCKABLE_MILLS_INDEX | enemiePlayerId]++;
				}
			}
		}
		currentNeighbourField = currentField.getRightNeighbour();
		if (currentNeighbourField != null)
		{
			if (currentNeighbourField.becomesVerticalMillByPlayerId(playerId))
			{
				if (currentNeighbourField.isEmpty())
				{
					if (currentNeighbourField.fieldHasNeighboursWithPlayerId(enemiePlayerId))
					{
						toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | playerId]++;
					}
					else
					{
						toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | playerId]++;
					}
				}
				else if (currentNeighbourField.getTokenOfPlayerId() == enemiePlayerId)
				{
					toFill[NUMBER_OF_OPEN_BUT_BLOCKED_MILLS_INDEX | playerId]++;
				}
			}
			else if (currentNeighbourField.isEmpty() && currentNeighbourField.becomesVerticalMillByPlayerId(enemiePlayerId))
			{
				final Field neighbourOfNeighbour = currentNeighbourField.getRightNeighbour();
				if (neighbourOfNeighbour != null && !neighbourOfNeighbour.isEmpty() && neighbourOfNeighbour.getTokenOfPlayerId() == enemiePlayerId)
				{
					toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | enemiePlayerId]++;
					toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | enemiePlayerId]--;
				}
			}
			else if (!currentNeighbourField.isEmpty() && currentNeighbourField.getVerticalRow().getNumberOfTokensByPlayerId(enemiePlayerId) == 3)
			{
				final Field neighbourOfNeighbour = currentNeighbourField.getRightNeighbour();
				if ((neighbourOfNeighbour == null || neighbourOfNeighbour.isEmpty() || neighbourOfNeighbour.getTokenOfPlayerId() != playerId) && millIsBlockableByPlayerID(playerId, currentNeighbourField.getVerticalRow()))
				{
					toFill[NUMBER_OF_CLOSED_NOT_BLOCKABLE_MILLS_INDEX | enemiePlayerId]--;
					toFill[NUMBER_OF_CLOSED_AND_BLOCKED_OR_BLOCKABLE_MILLS_INDEX | enemiePlayerId]++;
				}
			}
		}
		currentNeighbourField = currentField.getLeftNeighbour();
		if (currentNeighbourField != null)
		{
			if (currentNeighbourField.becomesVerticalMillByPlayerId(playerId))
			{
				if (currentNeighbourField.isEmpty())
				{
					if (currentNeighbourField.fieldHasNeighboursWithPlayerId(enemiePlayerId))
					{
						toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | playerId]++;
					}
					else
					{
						toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | playerId]++;
					}
				}
				else if (currentNeighbourField.getTokenOfPlayerId() == enemiePlayerId)
				{
					toFill[NUMBER_OF_OPEN_BUT_BLOCKED_MILLS_INDEX | playerId]++;
				}
			}
			else if (currentNeighbourField.isEmpty() && currentNeighbourField.becomesVerticalMillByPlayerId(enemiePlayerId))
			{
				final Field neighbourOfNeighbour = currentNeighbourField.getLeftNeighbour();
				if (neighbourOfNeighbour != null && !neighbourOfNeighbour.isEmpty() && neighbourOfNeighbour.getTokenOfPlayerId() == enemiePlayerId)
				{
					toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | enemiePlayerId]++;
					toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | enemiePlayerId]--;
				}
			}
			else if (!currentNeighbourField.isEmpty() && currentNeighbourField.getVerticalRow().getNumberOfTokensByPlayerId(enemiePlayerId) == 3)
			{
				final Field neighbourOfNeighbour = currentNeighbourField.getLeftNeighbour();
				if ((neighbourOfNeighbour == null || neighbourOfNeighbour.isEmpty() || neighbourOfNeighbour.getTokenOfPlayerId() != playerId) && millIsBlockableByPlayerID(playerId, currentNeighbourField.getVerticalRow()))
				{
					toFill[NUMBER_OF_CLOSED_NOT_BLOCKABLE_MILLS_INDEX | enemiePlayerId]--;
					toFill[NUMBER_OF_CLOSED_AND_BLOCKED_OR_BLOCKABLE_MILLS_INDEX | enemiePlayerId]++;
				}
			}
		}
	}

	private void checkForInDirectChangesOnMoveInverted(final int[] toFill, final Field currentField, final int playerId, final int enemiePlayerId)
	{
		Field currentNeighbourField = currentField.getTopNeighbour();
		if (currentNeighbourField != null)
		{
			if (currentNeighbourField.becomesHorizontalMillByPlayerId(playerId))
			{
				if (currentNeighbourField.isEmpty())
				{
					if (currentNeighbourField.fieldHasNeighboursWithPlayerId(enemiePlayerId))
					{
						toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | playerId]--;
					}
					else
					{
						toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | playerId]--;
					}
				}
				else if (currentNeighbourField.getTokenOfPlayerId() == enemiePlayerId)
				{
					toFill[NUMBER_OF_OPEN_BUT_BLOCKED_MILLS_INDEX | playerId]--;
				}
			}
			else if (currentNeighbourField.isEmpty() && currentNeighbourField.becomesHorizontalMillByPlayerId(enemiePlayerId))
			{
				final Field neighbourOfNeighbour = currentNeighbourField.getTopNeighbour();
				if (neighbourOfNeighbour != null && !neighbourOfNeighbour.isEmpty() && neighbourOfNeighbour.getTokenOfPlayerId() == enemiePlayerId)
				{
					toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | enemiePlayerId]--;
					toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | enemiePlayerId]++;
				}
			}
			else if (!currentNeighbourField.isEmpty() && currentNeighbourField.getHorizontalRow().getNumberOfTokensByPlayerId(enemiePlayerId) == 3)
			{
				final Field neighbourOfNeighbour = currentNeighbourField.getTopNeighbour();
				if ((neighbourOfNeighbour == null || neighbourOfNeighbour.isEmpty() || neighbourOfNeighbour.getTokenOfPlayerId() != playerId) && millIsBlockableByPlayerID(playerId, currentNeighbourField.getHorizontalRow()))
				{
					toFill[NUMBER_OF_CLOSED_NOT_BLOCKABLE_MILLS_INDEX | enemiePlayerId]++;
					toFill[NUMBER_OF_CLOSED_AND_BLOCKED_OR_BLOCKABLE_MILLS_INDEX | enemiePlayerId]--;
				}
			}
		}
		currentNeighbourField = currentField.getBottomNeighbour();
		if (currentNeighbourField != null)
		{
			if (currentNeighbourField.becomesHorizontalMillByPlayerId(playerId))
			{
				if (currentNeighbourField.isEmpty())
				{
					if (currentNeighbourField.fieldHasNeighboursWithPlayerId(enemiePlayerId))
					{
						toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | playerId]--;
					}
					else
					{
						toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | playerId]--;
					}
				}
				else if (currentNeighbourField.getTokenOfPlayerId() == enemiePlayerId)
				{
					toFill[NUMBER_OF_OPEN_BUT_BLOCKED_MILLS_INDEX | playerId]--;
				}
			}
			else if (currentNeighbourField.isEmpty() && currentNeighbourField.becomesHorizontalMillByPlayerId(enemiePlayerId))
			{
				final Field neighbourOfNeighbour = currentNeighbourField.getBottomNeighbour();
				if (neighbourOfNeighbour != null && !neighbourOfNeighbour.isEmpty() && neighbourOfNeighbour.getTokenOfPlayerId() == enemiePlayerId)
				{
					toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | enemiePlayerId]--;
					toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | enemiePlayerId]++;
				}
			}
			else if (!currentNeighbourField.isEmpty() && currentNeighbourField.getHorizontalRow().getNumberOfTokensByPlayerId(enemiePlayerId) == 3)
			{
				final Field neighbourOfNeighbour = currentNeighbourField.getBottomNeighbour();
				if ((neighbourOfNeighbour == null || neighbourOfNeighbour.isEmpty() || neighbourOfNeighbour.getTokenOfPlayerId() != playerId) && millIsBlockableByPlayerID(playerId, currentNeighbourField.getHorizontalRow()))
				{
					toFill[NUMBER_OF_CLOSED_NOT_BLOCKABLE_MILLS_INDEX | enemiePlayerId]++;
					toFill[NUMBER_OF_CLOSED_AND_BLOCKED_OR_BLOCKABLE_MILLS_INDEX | enemiePlayerId]--;
				}
			}
		}
		currentNeighbourField = currentField.getRightNeighbour();
		if (currentNeighbourField != null)
		{
			if (currentNeighbourField.becomesVerticalMillByPlayerId(playerId))
			{
				if (currentNeighbourField.isEmpty())
				{
					if (currentNeighbourField.fieldHasNeighboursWithPlayerId(enemiePlayerId))
					{
						toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | playerId]--;
					}
					else
					{
						toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | playerId]--;
					}
				}
				else if (currentNeighbourField.getTokenOfPlayerId() == enemiePlayerId)
				{
					toFill[NUMBER_OF_OPEN_BUT_BLOCKED_MILLS_INDEX | playerId]--;
				}
			}
			else if (currentNeighbourField.isEmpty() && currentNeighbourField.becomesVerticalMillByPlayerId(enemiePlayerId))
			{
				final Field neighbourOfNeighbour = currentNeighbourField.getRightNeighbour();
				if (neighbourOfNeighbour != null && !neighbourOfNeighbour.isEmpty() && neighbourOfNeighbour.getTokenOfPlayerId() == enemiePlayerId)
				{
					toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | enemiePlayerId]--;
					toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | enemiePlayerId]++;
				}
			}
			else if (!currentNeighbourField.isEmpty() && currentNeighbourField.getVerticalRow().getNumberOfTokensByPlayerId(enemiePlayerId) == 3)
			{
				final Field neighbourOfNeighbour = currentNeighbourField.getRightNeighbour();
				if ((neighbourOfNeighbour == null || neighbourOfNeighbour.isEmpty() || neighbourOfNeighbour.getTokenOfPlayerId() != playerId) && millIsBlockableByPlayerID(playerId, currentNeighbourField.getVerticalRow()))
				{
					toFill[NUMBER_OF_CLOSED_NOT_BLOCKABLE_MILLS_INDEX | enemiePlayerId]++;
					toFill[NUMBER_OF_CLOSED_AND_BLOCKED_OR_BLOCKABLE_MILLS_INDEX | enemiePlayerId]--;
				}
			}
		}
		currentNeighbourField = currentField.getLeftNeighbour();
		if (currentNeighbourField != null)
		{
			if (currentNeighbourField.becomesVerticalMillByPlayerId(playerId))
			{
				if (currentNeighbourField.isEmpty())
				{
					if (currentNeighbourField.fieldHasNeighboursWithPlayerId(enemiePlayerId))
					{
						toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | playerId]--;
					}
					else
					{
						toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | playerId]--;
					}
				}
				else if (currentNeighbourField.getTokenOfPlayerId() == enemiePlayerId)
				{
					toFill[NUMBER_OF_OPEN_BUT_BLOCKED_MILLS_INDEX | playerId]--;
				}
			}
			else if (currentNeighbourField.isEmpty() && currentNeighbourField.becomesVerticalMillByPlayerId(enemiePlayerId))
			{
				final Field neighbourOfNeighbour = currentNeighbourField.getLeftNeighbour();
				if (neighbourOfNeighbour != null && !neighbourOfNeighbour.isEmpty() && neighbourOfNeighbour.getTokenOfPlayerId() == enemiePlayerId)
				{
					toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | enemiePlayerId]--;
					toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | enemiePlayerId]++;
				}
			}
			else if (!currentNeighbourField.isEmpty() && currentNeighbourField.getVerticalRow().getNumberOfTokensByPlayerId(enemiePlayerId) == 3)
			{
				final Field neighbourOfNeighbour = currentNeighbourField.getLeftNeighbour();
				if ((neighbourOfNeighbour == null || neighbourOfNeighbour.isEmpty() || neighbourOfNeighbour.getTokenOfPlayerId() != playerId) && millIsBlockableByPlayerID(playerId, currentNeighbourField.getVerticalRow()))
				{
					toFill[NUMBER_OF_CLOSED_NOT_BLOCKABLE_MILLS_INDEX | enemiePlayerId]++;
					toFill[NUMBER_OF_CLOSED_AND_BLOCKED_OR_BLOCKABLE_MILLS_INDEX | enemiePlayerId]--;
				}
			}
		}
	}

	private void checkForDirectChangesOnMoveInverted(final int[] toFill, final MillRow currentRow, final int playerId, final int enemiePlayerId, final boolean currentMillIsClosed, final Field fieldOfChange)
	{
		if (currentMillIsClosed)
		{
			if (millIsBlockableByPlayerID(enemiePlayerId, currentRow))
				toFill[NUMBER_OF_CLOSED_AND_BLOCKED_OR_BLOCKABLE_MILLS_INDEX | playerId]--;
			else
				toFill[NUMBER_OF_CLOSED_NOT_BLOCKABLE_MILLS_INDEX | playerId]--;
			toFill[NUMBER_OF_OPEN_TWO_STONES_INDEX | playerId]++;
			final int count;
			if ((count = fieldOfChange.getNumberOfNeighboursWithPlayerIDWhichAreNotPartOfRow(playerId, currentRow)) > 0) //ANZAHL PRÜFEN statt nur HAS!!!!
			{
				if (fieldOfChange.fieldHasNeighboursWithPlayerId(enemiePlayerId))
				{
					toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | playerId]++;
				}
				else
				{
					toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | playerId] += count;
				}
			}
		}
		else
		{
			if (currentRow.getNumberOfTokensByPlayerId(enemiePlayerId) == 2)
			{
				toFill[NUMBER_OF_OPEN_TWO_STONES_INDEX | enemiePlayerId]++;
				final int count = fieldOfChange.getNumberOfNeighboursWithPlayerIDWhichAreNotPartOfRow(enemiePlayerId, currentRow);
				if (count > 0)
				{
					if (fieldOfChange.fieldHasNeighboursWithPlayerId(playerId))
					{
						toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | enemiePlayerId]++;
					}
					else
					{
						toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | enemiePlayerId] += count;
					}
					toFill[NUMBER_OF_OPEN_BUT_BLOCKED_MILLS_INDEX | enemiePlayerId] -= count;
				}
			}
			if (currentRow.getNumberOfTokensByPlayerId(playerId) == 2)
			{
				if (millIsBlockedByPlayerId(currentRow, enemiePlayerId))
				{
					toFill[NUMBER_OF_OPEN_BUT_BLOCKED_MILLS_INDEX | playerId] -= blockedMillIsClosableByPlayerID(playerId, enemiePlayerId, currentRow);
				}
				else
				{
					final int kindOfMill = openMillIsClosableByPlayerID(playerId, enemiePlayerId, currentRow);
					toFill[NUMBER_OF_OPEN_TWO_STONES_INDEX | playerId]--;
					if (kindOfMill < 3)
					{
						toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | playerId] -= kindOfMill;
					}
					else
					{
						toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | playerId]--;
					}
				}
			}
		}
	}

	private void checkForDirectChangesOnMove(final int[] toFill, final MillRow currentRow, final int playerId, final int enemiePlayerId, final boolean currentMillIsClosed, final Field fieldOfChange)
	{
		if (currentMillIsClosed)
		{
			if (millIsBlockableByPlayerID(enemiePlayerId, currentRow))
				toFill[NUMBER_OF_CLOSED_AND_BLOCKED_OR_BLOCKABLE_MILLS_INDEX | playerId]++;
			else
				toFill[NUMBER_OF_CLOSED_NOT_BLOCKABLE_MILLS_INDEX | playerId]++;
			toFill[NUMBER_OF_OPEN_TWO_STONES_INDEX | playerId]--;
			final int count;
			if ((count = fieldOfChange.getNumberOfNeighboursWithPlayerIDWhichAreNotPartOfRow(playerId, currentRow)) > 0) //ANZAHL PRÜFEN statt nur HAS!!!!
			{
				if (fieldOfChange.fieldHasNeighboursWithPlayerId(enemiePlayerId))
				{
					toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | playerId]--;
				}
				else
				{
					toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | playerId] -= count;
				}
			}
		}
		else
		{
			if (currentRow.getNumberOfTokensByPlayerId(enemiePlayerId) == 2)
			{
				toFill[NUMBER_OF_OPEN_TWO_STONES_INDEX | enemiePlayerId]--;
				final int count = fieldOfChange.getNumberOfNeighboursWithPlayerIDWhichAreNotPartOfRow(enemiePlayerId, currentRow);
				if (count > 0)
				{
					if (fieldOfChange.fieldHasNeighboursWithPlayerId(playerId))
					{
						toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | enemiePlayerId]--;
					}
					else
					{
						toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | enemiePlayerId] -= count;
					}
					toFill[NUMBER_OF_OPEN_BUT_BLOCKED_MILLS_INDEX | enemiePlayerId] += count;
				}
			}
			if (currentRow.getNumberOfTokensByPlayerId(playerId) == 2)
			{
				if (millIsBlockedByPlayerId(currentRow, enemiePlayerId))
				{
					toFill[NUMBER_OF_OPEN_BUT_BLOCKED_MILLS_INDEX | playerId] += blockedMillIsClosableByPlayerID(playerId, enemiePlayerId, currentRow);
				}
				else
				{
					final int kindOfMill = openMillIsClosableByPlayerID(playerId, enemiePlayerId, currentRow);
					toFill[NUMBER_OF_OPEN_TWO_STONES_INDEX | playerId]++;
					if (kindOfMill < 3)
					{
						toFill[NUMBER_OF_OPEN_NOT_BLOCKABLE_MILLS_INDEX | playerId] += kindOfMill;
					}
					else
					{
						toFill[NUMBER_OF_OPEN_BLOCKABLE_MILLS_INDEX | playerId]++;
					}
				}
			}
		}
	}

	private int openMillIsClosableByPlayerID(final int playerId, final int enemiePlayerId, final MillRow millRow)
	{
		final Field[] fieldsOfRow = millRow.getFieldsOfRow();
		for (Field currentField : fieldsOfRow)
		{
			if (currentField.isEmpty())
			{
				final int count = currentField.getNumberOfNeighboursWithPlayerIDWhichAreNotPartOfRow(playerId, millRow);
				if (count == 0)
				{
					return 0; // keine schließbare mühle
				}
				else if (currentField.fieldHasNeighboursWithPlayerId(enemiePlayerId))
				{
					return 3; //von beiden parteien schließbar
				}
				else
				{
					return count;//nur von playerId schließbar
				}
			}
		}
		System.out.println("funktion openMillIsClosableByPlayerID fehler!");
		return -1;
	}

	private int blockedMillIsClosableByPlayerID(final int playerId, final int enemiePlayerId, final MillRow millRow)
	{
		final Field[] fieldsOfRow = millRow.getFieldsOfRow();
		for (Field currentField : fieldsOfRow)
		{
			if (currentField.getTokenOfPlayerId() == enemiePlayerId)
			{
				return currentField.getNumberOfNeighboursWithPlayerIDWhichAreNotPartOfRow(playerId, millRow);
			}
		}
		System.out.println("funktion blockedMillIsClosableByPlayerID fehler!");
		return 0;
	}

	private boolean millIsBlockableByPlayerID(final int playerID, final MillRow millRow)
	{
		final Field[] fieldsOfRow = millRow.getFieldsOfRow();
		for (Field currentField : fieldsOfRow)
		{
			if (!currentField.fieldHasNeighboursWithPlayerId(playerID))
				return false;
		}
		return true;
	}

	private boolean millIsBlockedByPlayerId(final MillRow millRow, final int playerId)
	{
		return millRow.getNumberOfTokensByPlayerId(playerId) > 0;
	}
}
