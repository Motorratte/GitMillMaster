package game;

import ai.evaluation.EvaluationModel;
import ai.other.MoveObject;
import other.MoveAndKey;

import java.util.HashMap;
import java.util.Random;

public class MillModel
{
	public static final int NUMBER_OF_FIELDS = 24;

	private final Random random;
	private final Field[] board;
	private final Field[][] playerFieldsAtEnding; //Verbessert die Performance des Zuggenerators (Verwendung in der letzten Spielphase)
	private int currentPlayerId; //0 oder 1
	private final int[] numberOfStonesByPlayerID;
	//private final int[] numberOfFreedomesByPlayerID;
	private final MillRowContainer millRowInformation;
	private int moveNumber;
	private final MoveAndKey[] playedMoves;
	private final MillMove[] possibleMoves;
	private int numberOfPossibleMoves;
	//EvaluationModel eModel = new EvaluationModel(this);
	//int[] evaluationDataBase = new int[22];
	private long keyOfThisPosition;
	private HashMap<Long, Integer> playedMovesDrawCheckMap;
	private int[] lastAttackMoveNumber = new int[16];
	private int lastAttackMoveNumberIndex;
	private int[] playedMovesNumberOfExists;
	private Integer currentPositionNumberOfExists = 1;
	private long keyOfPlayerChange;
	private boolean gameIsOver; //Nur wahr, wenn es einen Sieger gibt, für unentschieden ist mit "gameIsDraw()" zu prüfen (ai verwendet eigene effiziente verfahren um auf unentschieden zu prüfen/vermeiden)
	private int winningPlayerId;
	private MillModel savetyDrawChecker; //späte initallisierung bei verwendung von "gameIsDraw()"

	public MillModel()
	{
		random = new Random(536424354637675L);
		board = new Field[NUMBER_OF_FIELDS];
		playerFieldsAtEnding = new Field[2][3];
		currentPlayerId = 0;
		numberOfStonesByPlayerID = new int[2];
		//numberOfFreedomesByPlayerID = new int[2];
		millRowInformation = new MillRowContainer();
		moveNumber = 0;
		playedMoves = new MoveAndKey[668];
		playedMovesNumberOfExists = new int[playedMoves.length];
		possibleMoves = new MillMove[75];
		keyOfThisPosition = 0;
		gameIsOver = false;
		winningPlayerId = -1;
		savetyDrawChecker = null;
		lastAttackMoveNumber = new int[18];
		lastAttackMoveNumberIndex = 1;
		playedMovesDrawCheckMap = new HashMap<Long, Integer>();
		createPossibleMillRowsAndFields();
		initHashKeys();
		connectFields();
	}

	public MillModel clone()
	{
		final MillModel copy = new MillModel();
		copy.synchronizeThisModellWithOther(this);
		return copy;
	}

	private void createPossibleMillRowsAndFields()
	{
		final MillRow horizontalRow0 = new MillRow(new int[] { 0, 1, 2 }, millRowInformation);
		final MillRow horizontalRow1 = new MillRow(new int[] { 3, 4, 5 }, millRowInformation);
		final MillRow horizontalRow2 = new MillRow(new int[] { 6, 7, 8 }, millRowInformation);
		final MillRow horizontalRow3 = new MillRow(new int[] { 9, 10, 11 }, millRowInformation);
		final MillRow horizontalRow4 = new MillRow(new int[] { 12, 13, 14 }, millRowInformation);
		final MillRow horizontalRow5 = new MillRow(new int[] { 15, 16, 17 }, millRowInformation);
		final MillRow horizontalRow6 = new MillRow(new int[] { 18, 19, 20 }, millRowInformation);
		final MillRow horizontalRow7 = new MillRow(new int[] { 21, 22, 23 }, millRowInformation);

		final MillRow verticalRow0 = new MillRow(new int[] { 0, 9, 21 }, millRowInformation);
		final MillRow verticalRow1 = new MillRow(new int[] { 3, 10, 18 }, millRowInformation);
		final MillRow verticalRow2 = new MillRow(new int[] { 6, 11, 15 }, millRowInformation);
		final MillRow verticalRow3 = new MillRow(new int[] { 1, 4, 7 }, millRowInformation);
		final MillRow verticalRow4 = new MillRow(new int[] { 16, 19, 22 }, millRowInformation);
		final MillRow verticalRow5 = new MillRow(new int[] { 8, 12, 17 }, millRowInformation);
		final MillRow verticalRow6 = new MillRow(new int[] { 5, 13, 20 }, millRowInformation);
		final MillRow verticalRow7 = new MillRow(new int[] { 2, 14, 23 }, millRowInformation);

		board[0] = new Field(horizontalRow0, verticalRow0, 0, false, true, false);
		board[1] = new Field(horizontalRow0, verticalRow3, 1, false, true, false);
		board[2] = new Field(horizontalRow0, verticalRow7, 2, false, true, false);
		board[3] = new Field(horizontalRow1, verticalRow1, 3, false, false, false);
		board[4] = new Field(horizontalRow1, verticalRow3, 4, true, false, false);
		board[5] = new Field(horizontalRow1, verticalRow6, 5, false, false, false);
		board[6] = new Field(horizontalRow2, verticalRow2, 6, false, false, true);
		board[7] = new Field(horizontalRow2, verticalRow3, 7, false, false, true);
		board[8] = new Field(horizontalRow2, verticalRow5, 8, false, false, true);
		board[9] = new Field(horizontalRow3, verticalRow0, 9, false, true, false);
		board[10] = new Field(horizontalRow3, verticalRow1, 10, true, false, false);
		board[11] = new Field(horizontalRow3, verticalRow2, 11, false, false, true);
		board[12] = new Field(horizontalRow4, verticalRow5, 12, false, false, true);
		board[13] = new Field(horizontalRow4, verticalRow6, 13, true, false, false);
		board[14] = new Field(horizontalRow4, verticalRow7, 14, false, true, false);
		board[15] = new Field(horizontalRow5, verticalRow2, 15, false, false, true);
		board[16] = new Field(horizontalRow5, verticalRow4, 16, false, false, true);
		board[17] = new Field(horizontalRow5, verticalRow5, 17, false, false, true);
		board[18] = new Field(horizontalRow6, verticalRow1, 18, false, false, false);
		board[19] = new Field(horizontalRow6, verticalRow4, 19, true, false, false);
		board[20] = new Field(horizontalRow6, verticalRow6, 20, false, false, false);
		board[21] = new Field(horizontalRow7, verticalRow0, 21, false, true, false);
		board[22] = new Field(horizontalRow7, verticalRow4, 22, false, true, false);
		board[23] = new Field(horizontalRow7, verticalRow7, 23, false, true, false);

		horizontalRow0.setFieldsInRow(board);
		horizontalRow1.setFieldsInRow(board);
		horizontalRow2.setFieldsInRow(board);
		horizontalRow3.setFieldsInRow(board);
		horizontalRow4.setFieldsInRow(board);
		horizontalRow5.setFieldsInRow(board);
		horizontalRow6.setFieldsInRow(board);
		horizontalRow7.setFieldsInRow(board);

		verticalRow0.setFieldsInRow(board);
		verticalRow1.setFieldsInRow(board);
		verticalRow2.setFieldsInRow(board);
		verticalRow3.setFieldsInRow(board);
		verticalRow4.setFieldsInRow(board);
		verticalRow5.setFieldsInRow(board);
		verticalRow6.setFieldsInRow(board);
		verticalRow7.setFieldsInRow(board);
	}

	private void connectFields()
	{
		board[0].setNeighbours(board[9], board[1]);
		board[1].setNeighbours(board[4], board[2]);
		board[2].setNeighbours(board[14], null);
		board[3].setNeighbours(board[10], board[4]);
		board[4].setNeighbours(board[7], board[5]);
		board[5].setNeighbours(board[13], null);
		board[6].setNeighbours(board[11], board[7]);
		board[7].setNeighbours(null, board[8]);
		board[8].setNeighbours(board[12], null);
		board[9].setNeighbours(board[21], board[10]);
		board[10].setNeighbours(board[18], board[11]);
		board[11].setNeighbours(board[15], null);
		board[12].setNeighbours(board[17], board[13]);
		board[13].setNeighbours(board[20], board[14]);
		board[14].setNeighbours(board[23], null);
		board[15].setNeighbours(null, board[16]);
		board[16].setNeighbours(board[19], board[17]);
		board[17].setNeighbours(null, null);
		board[18].setNeighbours(null, board[19]);
		board[19].setNeighbours(board[22], board[20]);
		board[20].setNeighbours(null, null);
		board[21].setNeighbours(null, board[22]);
		board[22].setNeighbours(null, board[23]);
		board[23].setNeighbours(null, null);
	}

	private void initHashKeys() //Für die Keys wird "Zobrist hashing" verwendet, es gewährleistet eine gute Gleichverteilung der Keys in einer Hashtabelle!
	{
		final long[] keys = new long[(NUMBER_OF_FIELDS << 1) + 1]; //49 different randome Keys
		long currentKey;
		boolean currentKeyIsLegal; //Es soll sichergestellt werden, dass sich kein Key wiederholt und dass alle Keys ungleich 0 sind.
		for (int a = 0; a < keys.length; a++)
		{
			do
			{
				currentKey = random.nextLong();
				if (!(currentKeyIsLegal = currentKey != 0L))
					continue;
				for (int b = 0; b < a; b++) //Schleife versucht die Annahme zu wiederlegen, dass currentKey kein bereits existierender Key in keys[] darstellt
				{
					if (keys[a] == currentKey)
					{
						currentKeyIsLegal = false;
						break;
					}
				}
			} while (!currentKeyIsLegal);
			keys[a] = currentKey;
		}
		keyOfPlayerChange = keys[0];
		for (int a = 0, keysIndex = 1; a < NUMBER_OF_FIELDS; a++, keysIndex += 2)
		{
			board[a].setPlayerHashKeyOfField(new long[] { keys[keysIndex], keys[keysIndex + 1] });
		}
	}

	public boolean gameIsDrawAi(final int existsNeeded) //darf nur nach einer zugausführung aufgerufen werden
	{
		return moveNumber >= 18 && (currentPositionNumberOfExists >= existsNeeded || getDistanceToLastClosedMill() >= 50);
	}

	public boolean gameIsDraw() //Diese kostspielige Methode wird nur für das Hauptspiel genutzt (die AI nutzt effizientere verfahren um früher ein drohendes unentschieden zu erkennen)
	{
		return !gameIsOver && gameIsDrawAi(3) && (getNumberOfRepititions() >= 2 || getDistanceToLastClosedMill() >= 50);
	}

	private int getNumberOfRepititions()
	{
		if (moveNumber < 22) //bis 3 halbzüge nach der Eröffnungsphase ist eine Stellungswiederholung unmöglich
			return 0;
		int count = 0;
		MoveAndKey current;
		for (int a = moveNumber - 3; a >= 18; a -= 2)
		{
			current = playedMoves[a];
			if (current.getMove().isAttackMove())
				break;
			if (current.getKey() == keyOfThisPosition) //es gibt eine ultra geringe chance, dass die keys nur zufälligerweise gleich sind, daher wird der savetyDrawChecker genutzt. Es wird geprüft ob die stellungen tatsächlich gleich sind. Die AI verzichtet auf diese Sicherheit und nutzt eine HAshMap um über die Keys auf Stellungswiederholung zu prüfen.
			{
				if (savetyDrawChecker == null)
				{
					savetyDrawChecker = new MillModel();
				}
				savetyDrawChecker.executeMovesToNumber(this, a + 1);
				if (savetyDrawChecker.thisBoardIsEqualTo(this.board))
				{
					count++;
				}
				savetyDrawChecker.undoMovesToNumber(0);
			}
		}
		return count;
	}

	public int getDistanceToLastClosedMill()
	{
		return moveNumber - lastAttackMoveNumber[lastAttackMoveNumberIndex - 1];
	}

	public void loadGame(final int[] data)
	{
		for (int i = 0; i < data.length; i += 3)
		{
			executeMove(new MillMove(data[i], data[i + 1], data[i + 2], board));
		}
	}

	public int[] getGameData()
	{
		final int[] data = new int[moveNumber * 3];
		for (int i = 0, move = 0; i < data.length; i += 3, move++)
		{
			final MillMove currentMove = playedMoves[move].getMove();
			data[i] = currentMove.getSourceIndex();
			data[i + 1] = currentMove.getDestinationIndex();
			data[i + 2] = currentMove.getAttackedIndex();
		}
		return data;
	}

	public synchronized void synchronizeThisModellWithOther(final MillModel other)
	{
		synchronizeThisModellWithOther(other, true);
	}

	public synchronized void synchronizeThisModellWithOther(final MillModel other, boolean bidirectionaly)
	{
		if (other.moveNumber < moveNumber)
		{
			if (bidirectionaly)
				undoMovesToNumber(other.moveNumber);
		}
		else
		{
			executeMovesToNumber(other, other.moveNumber);
		}
	}

	public synchronized void synchronizeEvaluationDataBaseWithModel(final EvaluationModel eModel, final int evaluationMoveNumber, final int[] evaluationData)
	{
		final int modelMoveNumber = this.moveNumber;
		undoMovesToNumber(evaluationMoveNumber);
		reExecuteMovesToNumberWhileEvaluatingData(modelMoveNumber, eModel, evaluationData);
	}

	private void reExecuteMovesToNumberWhileEvaluatingData(final int moveNumber, final EvaluationModel eModel, final int[] evaluationData)
	{
		while (this.moveNumber < moveNumber)
		{
			final MillMove currentMove = playedMoves[this.moveNumber].getMove();
			eModel.generateEvaluationDataForMove(evaluationData, evaluationData, currentMove, currentPlayerId);
			executeMove(currentMove);
		}
	}

	public void executeMovesToNumber(final MillModel other, final int number)
	{
		while (moveNumber != number)
		{
			executeMove(other.playedMoves[moveNumber].getMove().cloneMoveForOtherBoard(board));
		}
	}

	public void undoMovesToNumber(final int number)
	{
		while (moveNumber != number)
		{
			undoMove();
		}
	}

	public MillMove getLastMove()
	{
		if (moveNumber == 0)
			return null;
		return playedMoves[moveNumber - 1].getMove();
	}

	public void executeMove(final MillMove move)
	{
		//eModel.generateEvaluationDataForMove(evaluationDataBase, evaluationDataBase, move, currentPlayerId);
		//eModel.printEvaluationDataBase(evaluationDataBase);
		if (moveNumber >= 18) //Erste spielphase vorbei?
		{
			final Field sourceField = move.getSource();
			keyOfThisPosition ^= sourceField.removeToken();
			//numberOfFreedomesByPlayerID[0] += sourceField.getLastEmptyFieldDifferenceResultByPlayerId(0);
			//numberOfFreedomesByPlayerID[1] += sourceField.getLastEmptyFieldDifferenceResultByPlayerId(1);
			if (numberOfStonesByPlayerID[currentPlayerId] == 3)
			{
				final Field[] currentPlayerFields = playerFieldsAtEnding[currentPlayerId];
				if (currentPlayerFields[0] == sourceField)
				{
					currentPlayerFields[0] = move.getDestination();
				}
				else if (currentPlayerFields[1] == sourceField)
				{
					currentPlayerFields[1] = move.getDestination();
				}
				else
				{
					currentPlayerFields[2] = move.getDestination();
				}
			}
		}
		else
		{
			numberOfStonesByPlayerID[currentPlayerId]++;
		}
		final Field destinationField = move.getDestination();
		keyOfThisPosition ^= destinationField.addToken(currentPlayerId) ^ keyOfPlayerChange;
		//numberOfFreedomesByPlayerID[0] += destinationField.getLastEmptyFieldDifferenceResultByPlayerId(0);
		//numberOfFreedomesByPlayerID[1] += destinationField.getLastEmptyFieldDifferenceResultByPlayerId(1);
		currentPlayerId ^= 1; //Spielerwechsel!
		if (move.isAttackMove())
		{
			final Field attackField = move.getAttacked();
			keyOfThisPosition ^= attackField.removeToken();
			//numberOfFreedomesByPlayerID[0] += attackField.getLastEmptyFieldDifferenceResultByPlayerId(0);
			//numberOfFreedomesByPlayerID[1] += attackField.getLastEmptyFieldDifferenceResultByPlayerId(1);
			final int numberOfStones = --numberOfStonesByPlayerID[currentPlayerId];
			if (moveNumber >= 18)
			{
				if (numberOfStones < 3)
				{
					gameIsOver = true;
					winningPlayerId = currentPlayerId ^ 1;
				}
				else if (numberOfStones == 3)
				{
					final Field[] myPlayerFields = playerFieldsAtEnding[currentPlayerId];
					int count = 0;
					for (Field current : board)
					{
						if (!current.isEmpty() && current.getTokenOfPlayerId() == currentPlayerId)
						{
							myPlayerFields[count++] = current;
						}
					}
				}
			}
			else if(moveNumber == 17)
			{
				if(numberOfStonesByPlayerID[1] == 3)
				{
					final Field[] myPlayerFields = playerFieldsAtEnding[1];
					int count = 0;
					for (Field current : board)
					{
						if (!current.isEmpty() && current.getTokenOfPlayerId() == 1)
						{
							myPlayerFields[count++] = current;
						}
					}
				}
			}
			lastAttackMoveNumber[lastAttackMoveNumberIndex++] = moveNumber;
		}
		playedMoves[moveNumber++] = (new MoveAndKey(move, keyOfThisPosition));
		if (moveNumber >= 18)
		{
			currentPositionNumberOfExists = playedMovesDrawCheckMap.get(keyOfThisPosition);
			if (currentPositionNumberOfExists == null)
			{
				currentPositionNumberOfExists = 1;
				playedMovesDrawCheckMap.put(keyOfThisPosition, 1);
			}
			else
			{
				currentPositionNumberOfExists++;
				playedMovesDrawCheckMap.put(keyOfThisPosition, currentPositionNumberOfExists);
			}
			playedMovesNumberOfExists[moveNumber] = currentPositionNumberOfExists;
		}
		else
		{
			currentPositionNumberOfExists = 0;
		}
	}

	public void undoMove()
	{
		if (moveNumber >= 18)
		{
			final int number = playedMovesNumberOfExists[moveNumber] - 1;
			if (number > 0)
				playedMovesDrawCheckMap.put(keyOfThisPosition, number);
			else
				playedMovesDrawCheckMap.remove(keyOfThisPosition);
		}
		final MoveAndKey moveAndKey = playedMoves[--moveNumber];
		final MillMove move = moveAndKey.getMove();
		if (moveNumber == 0)
		{
			keyOfThisPosition = 0;
		}
		else
		{
			keyOfThisPosition = playedMoves[moveNumber - 1].getKey();
		}
		if (move.isAttackMove())
		{
			final Field attackField = move.getAttacked();
			attackField.addTokenWithoutKey(currentPlayerId);
			//numberOfFreedomesByPlayerID[0] += attackField.getLastEmptyFieldDifferenceResultByPlayerId(0);
			//numberOfFreedomesByPlayerID[1] += attackField.getLastEmptyFieldDifferenceResultByPlayerId(1);
			numberOfStonesByPlayerID[currentPlayerId]++;
			lastAttackMoveNumberIndex--;
		}
		gameIsOver = false;
		currentPlayerId ^= 1; //Spielerwechsel!
		final Field destinationField = move.getDestination();
		destinationField.removeTokenWithoutKey();
		//numberOfFreedomesByPlayerID[0] += destinationField.getLastEmptyFieldDifferenceResultByPlayerId(0);
		//numberOfFreedomesByPlayerID[1] += destinationField.getLastEmptyFieldDifferenceResultByPlayerId(1);
		if (moveNumber >= 18) //Erste spielphase vorbei?
		{
			final Field sourceField = move.getSource();
			sourceField.addTokenWithoutKey(currentPlayerId);
			//numberOfFreedomesByPlayerID[0] += sourceField.getLastEmptyFieldDifferenceResultByPlayerId(0);
			//numberOfFreedomesByPlayerID[1] += sourceField.getLastEmptyFieldDifferenceResultByPlayerId(1);
			if (numberOfStonesByPlayerID[currentPlayerId] == 3)
			{
				final Field[] currentPlayerFields = playerFieldsAtEnding[currentPlayerId];
				if (currentPlayerFields[0] == destinationField)
				{
					currentPlayerFields[0] = sourceField;
				}
				else if (currentPlayerFields[1] == destinationField)
				{
					currentPlayerFields[1] = sourceField;
				}
				else
				{
					currentPlayerFields[2] = sourceField;
				}
			}
		}
		else
		{
			numberOfStonesByPlayerID[currentPlayerId]--;
		}
	}

	private boolean thisBoardIsEqualTo(final Field[] otherBoard)
	{
		for (int a = 0; a < NUMBER_OF_FIELDS; a++)
		{
			if (!board[a].equals(otherBoard[a]))
			{
				return false;
			}
		}
		return true;
	}

	public boolean isLegalMove(final MillMove move) //prüft, ob ein Zug legal ist ohne den Zuggenerator zu nutzen (viel performanter als alle Züge neu zu generieren)
	{
		final Field destination = move.getDestination();
		if (!destination.isEmpty())
			return false;
		final boolean shouldBeAttackMove;
		if (moveNumber >= 18)
		{
			final Field source = move.getSource();
			if (source == null || source.isEmpty() || source.getTokenOfPlayerId() != currentPlayerId)
				return false;
			shouldBeAttackMove = destination.becomesMillByPlayerId(currentPlayerId, source.getIdOfField());
		}
		else
		{
			shouldBeAttackMove = destination.becomesMillByPlayerId(currentPlayerId);
		}
		if (move.isAttackMove())
		{
			if (!shouldBeAttackMove)
				return false;
			final Field attackField = move.getAttacked();
			if (attackField.isEmpty() || attackField.getTokenOfPlayerId() == currentPlayerId || (attackField.isPartOfMillByPlayerId(currentPlayerId ^ 1) && playerHasTokensWhichAreNotPartOfMill(currentPlayerId ^ 1)))
				return false;
		}
		else
		{
			return !shouldBeAttackMove;
		}
		return true;
	}

	private boolean playerHasTokensWhichAreNotPartOfMill(final int playerId)
	{
		final int numberOfMills = millRowInformation.getNumberOfMillsByPlayerId(playerId);
		final int numberOfStones = getNumberOfStonesByPlayerID(playerId);
		if (numberOfMills == 0 || numberOfMills * 3 < numberOfStones)
		{
			return true;
		}
		if (numberOfStones == 3 && numberOfMills == 1 || numberOfStones == 5 && numberOfMills == 2)
		{
			return false;
		}
		for (Field currentField : board)
		{
			if (!currentField.isEmpty() && currentField.getTokenOfPlayerId() == playerId && !currentField.isPartOfMillByPlayerId(playerId))
			{
				return true;
			}
		}
		return false;

	}

	public MoveObject[] fillWithPossibleMoves(final MoveObject[] toFill)
	{
		for (int a = 0; a < numberOfPossibleMoves; a++)
		{
			toFill[a].setMove(possibleMoves[a]);
		}
		return toFill;
	}

	public void generatePossibleMoves()
	{
		numberOfPossibleMoves = 0;
		if (moveNumber < 18)
		{
			generatePossibleBeginningMoves();
		}
		else if (numberOfStonesByPlayerID[currentPlayerId] > 3)
		{
			generatePossibleMidGameMoves();
		}
		else
		{
			generatePossibleFinalGameMoves();
		}
		if (numberOfPossibleMoves == 0)
		{
			gameIsOver = true;
			winningPlayerId = currentPlayerId ^ 1;
		}
	}

	private void generatePossibleBeginningMoves()
	{
		for (Field current : board)
		{
			if (current.isEmpty())
			{
				if (current.becomesMillByPlayerId(currentPlayerId))
				{
					generateAttackMoves(null, current);

				}
				else
				{
					possibleMoves[numberOfPossibleMoves++] = new MillMove(current);
				}
			}
		}
	}

	private void generatePossibleMidGameMoves()
	{
		for (Field current : board)
		{
			if (!current.isEmpty() && current.getTokenOfPlayerId() == currentPlayerId)
			{
				generatePossibleMidGameMovesForTopOrBottomNeighbour(current, current.getTopNeighbour());
				generatePossibleMidGameMovesForTopOrBottomNeighbour(current, current.getBottomNeighbour());
				generatePossibleMidGameMovesForLeftOrRightNeighbour(current, current.getLeftNeighbour());
				generatePossibleMidGameMovesForLeftOrRightNeighbour(current, current.getRightNeighbour());
			}
		}
	}

	private void generatePossibleMidGameMovesForTopOrBottomNeighbour(final Field field, final Field neighbour)
	{
		if (neighbour != null && neighbour.isEmpty())
		{
			if (neighbour.becomesHorizontalMillByPlayerId(currentPlayerId)) //Nur vertikale Züge können horizontale Mühlen erzeugen
			{
				generateAttackMoves(field, neighbour);
			}
			else
			{
				possibleMoves[numberOfPossibleMoves++] = new MillMove(field, neighbour);
			}
		}
	}

	private void generatePossibleMidGameMovesForLeftOrRightNeighbour(final Field field, final Field neighbour)
	{
		if (neighbour != null && neighbour.isEmpty())
		{
			if (neighbour.becomesVerticalMillByPlayerId(currentPlayerId)) //Nur horizontale Züge können vertikale Mühlen erzeugen (hätte auch in einer Fallunterscheidung oder in einer Instanz eines implementierten Funktionalen Interface zusammengefasst werden können, was jedoch performance kostet)
			{
				generateAttackMoves(field, neighbour);
			}
			else
			{
				possibleMoves[numberOfPossibleMoves++] = new MillMove(field, neighbour);
			}
		}
	}

	private void generatePossibleFinalGameMoves()
	{
		final Field[] playerFields = playerFieldsAtEnding[currentPlayerId];
		Field attackDestination = null;
		Field attackDestination2 = null;
		boolean attackExists = false;
		final boolean currentPlayerHasMill = millRowInformation.playerHasMills(currentPlayerId);
		final Field currentField0 = playerFields[0];
		final Field currentField1 = playerFields[1];
		final Field currentField2 = playerFields[2];
		for (Field possibleDestination : board)
		{
			if (possibleDestination.isEmpty())
			{
				if (!currentPlayerHasMill && possibleDestination.becomesMillByPlayerId(currentPlayerId))
				{
					if (!attackExists)
					{
						attackExists = true;
						attackDestination = possibleDestination;
					}
					else
					{
						attackDestination2 = possibleDestination;
					}
				}
				else
				{
					possibleMoves[numberOfPossibleMoves++] = new MillMove(currentField0, possibleDestination);
					possibleMoves[numberOfPossibleMoves++] = new MillMove(currentField1, possibleDestination);
					possibleMoves[numberOfPossibleMoves++] = new MillMove(currentField2, possibleDestination);
				}
			}
		}
		if (attackExists)
		{
			do
			{
				Field attackSource;
				if (attackDestination.becomesMillByPlayerId(currentPlayerId, currentField0.getIdOfField()))
				{
					attackSource = currentField0;
					possibleMoves[numberOfPossibleMoves++] = new MillMove(currentField1, attackDestination);
					possibleMoves[numberOfPossibleMoves++] = new MillMove(currentField2, attackDestination);
				}
				else if (attackDestination.becomesMillByPlayerId(currentPlayerId, currentField1.getIdOfField()))
				{
					attackSource = currentField1;
					possibleMoves[numberOfPossibleMoves++] = new MillMove(currentField0, attackDestination);
					possibleMoves[numberOfPossibleMoves++] = new MillMove(currentField2, attackDestination);
				}
				else
				{
					attackSource = currentField2;
					possibleMoves[numberOfPossibleMoves++] = new MillMove(currentField0, attackDestination);
					possibleMoves[numberOfPossibleMoves++] = new MillMove(currentField1, attackDestination);
				}
				generateAttackMoves(attackSource, attackDestination);
				attackDestination = attackDestination != attackDestination2 ? attackDestination2 : null;

			} while (attackDestination != null);
		}
	}

	private void generateAttackMoves(final Field source, final Field destination)
	{
		final int currentEnemiePlayer = currentPlayerId ^ 1;
		final boolean enemiePlayerHasMills = millRowInformation.playerHasMills(currentEnemiePlayer);
		int moveCount = 0;
		for (Field currentEnemieField : board)
		{
			if (!currentEnemieField.isEmpty() && currentEnemieField.getTokenOfPlayerId() == currentEnemiePlayer && (!enemiePlayerHasMills || !currentEnemieField.isPartOfMillByPlayerId(currentEnemiePlayer)))
			{
				possibleMoves[numberOfPossibleMoves++] = new MillMove(source, destination, currentEnemiePlayer, currentEnemieField);
				moveCount++;
			}
		}
		if (moveCount == 0) //seltener Sonderfall (seit 2010), bei dem auch ein durch eine Mühle geschützter Spielstein entfernt werden darf
		{
			for (Field currentEnemieField : board)
			{
				if (!currentEnemieField.isEmpty() && currentEnemieField.getTokenOfPlayerId() == currentEnemiePlayer)
				{
					possibleMoves[numberOfPossibleMoves++] = new MillMove(source, destination, currentEnemiePlayer, currentEnemieField);
				}
			}
		}
	}

	public int getCurrentPlayerId()
	{
		return currentPlayerId;
	}

	public int getWinningPlayerId()
	{
		return winningPlayerId;
	}

	public boolean gameIsOver()
	{
		return gameIsOver;
	}

	public long getKeyOfCurrentPosition()
	{
		return keyOfThisPosition;
	}

	public Field[] getBoard()
	{
		return board;
	}

	public MillMove[] getPossibleMoves()
	{
		final MillMove[] returnValue = new MillMove[numberOfPossibleMoves];
		for (int a = 0; a < numberOfPossibleMoves; a++)
		{
			returnValue[a] = possibleMoves[a];
		}
		return returnValue;
	}

	public int getNumberOfPossibleMoves()
	{
		return numberOfPossibleMoves;
	}

	public MillMove[] getPossibleMovesNotSafe()
	{
		return possibleMoves;
	}

	public int getNumberOfStonesByPlayerID(final int playerId)
	{
		return numberOfStonesByPlayerID[playerId];
	}

	public int getMoveNumber()
	{
		return moveNumber;
	}

	public Field getField(final int fieldId)
	{
		return board[fieldId];
	}
}
