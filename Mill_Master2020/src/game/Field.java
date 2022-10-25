package game;

public class Field
{
	private boolean isEmpty = true;
	private int tokenOfPlayerId;
	private final MillRow horizontalRow;
	private final MillRow verticalRow;
	private final int idOfField;
	private long[] playerHashkeyOfField;
	private int numberOfEmptyNeighbours;
	private int[] numberOfNeighboursWithPlayerId = new int[2];
	private int[] emptyFieldDifferenceResultByPlayerId = new int[2];
	private int numberOfNeighbours;
	private Field[] neighbourFields;
	private Field topNeighbour;
	private Field rightNeighbour;
	private Field leftNeighbour;
	private Field bottomNeighbour;
	private final boolean isVeryImportantField;
	private final boolean isOuterRingField;
	private final boolean isInnerRingField;

	public Field(final MillRow horizontalRow, final MillRow verticalRow, final int idOfField, final boolean isVeryImportantField, final boolean isOuterRingField, final boolean isInnerRingField)
	{
		this.horizontalRow = horizontalRow;
		this.verticalRow = verticalRow;
		this.idOfField = idOfField;
		this.isVeryImportantField = isVeryImportantField;
		this.isOuterRingField = isOuterRingField;
		this.isInnerRingField = isInnerRingField;
	}

	public long addToken(final int playerId)//gibt die differenz des Keys für die änderung der Stellung zurück
	{
		addTokenWithouthKey(playerId);
		return playerHashkeyOfField[playerId];
	}

	public long removeToken()
	{
		removeTokenWithouthKey();
		return playerHashkeyOfField[tokenOfPlayerId];
	}

	public void addTokenWithouthKey(final int playerId)
	{
		isEmpty = false;
		this.tokenOfPlayerId = playerId;
		horizontalRow.addToken(playerId);
		verticalRow.addToken(playerId);
		emptyFieldDifferenceResultByPlayerId[playerId] = numberOfEmptyNeighbours;
		emptyFieldDifferenceResultByPlayerId[playerId ^ 1] = 0;
		for (Field currentField : neighbourFields)
		{
			currentField.numberOfNeighboursWithPlayerId[playerId]++;
			currentField.numberOfEmptyNeighbours--;
			if (!currentField.isEmpty)
			{
				emptyFieldDifferenceResultByPlayerId[currentField.tokenOfPlayerId]--;
			}
		}
	}

	public void removeTokenWithouthKey()
	{
		isEmpty = true;
		horizontalRow.removeToken(tokenOfPlayerId);
		verticalRow.removeToken(tokenOfPlayerId);
		emptyFieldDifferenceResultByPlayerId[tokenOfPlayerId] = -numberOfEmptyNeighbours;
		emptyFieldDifferenceResultByPlayerId[tokenOfPlayerId ^ 1] = 0;
		for (Field currentField : neighbourFields)
		{
			currentField.numberOfNeighboursWithPlayerId[tokenOfPlayerId]--;
			currentField.numberOfEmptyNeighbours++;
			if (!currentField.isEmpty)
			{
				emptyFieldDifferenceResultByPlayerId[currentField.tokenOfPlayerId]++;
			}
		}
	}

	public int getLastEmptyFieldDifferenceResultByPlayerId(final int playerId)
	{
		return emptyFieldDifferenceResultByPlayerId[playerId];
	}

	public Field clone() // erstellt eine flache Kopie von diesem Objekt
	{
		final Field result = new Field(horizontalRow, verticalRow, idOfField, isVeryImportantField, isOuterRingField, isInnerRingField);
		result.bottomNeighbour = bottomNeighbour;
		result.topNeighbour = topNeighbour;
		result.leftNeighbour = leftNeighbour;
		result.rightNeighbour = rightNeighbour;
		result.playerHashkeyOfField = playerHashkeyOfField;
		result.isEmpty = isEmpty;
		result.tokenOfPlayerId = tokenOfPlayerId;
		return result;
	}
	public boolean fieldOnlyHasEmptyNeighbours()
	{
		return numberOfNeighbours == numberOfEmptyNeighbours;
	}
	public boolean fieldHasNeighboursWithPlayerId(int playerId)
	{
		return numberOfNeighboursWithPlayerId[playerId] > 0;
	}
	public boolean fieldHasNeighboursWithPlayerIDWhichAreNotPartOfRow(final int playerId,final MillRow illegalRow)
	{
		for(Field currentField : neighbourFields)
		{
			if(!illegalRow.fieldIsPartOfMillRow(currentField.getIdOfField()) && !currentField.isEmpty() && currentField.tokenOfPlayerId == playerId)
			{
				return true;
			}
		}
		return false;
	}
	public int getNumberOfNeighboursWithPlayerIDWhichAreNotPartOfRow(final int playerId,final MillRow illegalRow)
	{
		int count = 0;
		for(Field currentField : neighbourFields)
		{
			if(!illegalRow.fieldIsPartOfMillRow(currentField.getIdOfField()) && !currentField.isEmpty() && currentField.tokenOfPlayerId == playerId)
			{
				count++;
			}
		}
		return count;
	}
	public void setNeighbours(final Field bottomNeighbour, final Field rightNeighbour)
	{
		if (bottomNeighbour != null)
		{
			this.bottomNeighbour = bottomNeighbour;
			bottomNeighbour.topNeighbour = this;
		}
		if (rightNeighbour != null)
		{
			this.rightNeighbour = rightNeighbour;
			rightNeighbour.leftNeighbour = this;
		}
		calculateNumberOfNeighbours();
		numberOfEmptyNeighbours = numberOfNeighbours;
		initNeighbourFieldsArray();
	}

	private void initNeighbourFieldsArray()
	{
		neighbourFields = new Field[numberOfNeighbours];
		int index = 0;
		if (topNeighbour != null)
			neighbourFields[index++] = topNeighbour;
		if (rightNeighbour != null)
			neighbourFields[index++] = rightNeighbour;
		if (bottomNeighbour != null)
			neighbourFields[index++] = bottomNeighbour;
		if (leftNeighbour != null)
			neighbourFields[index] = leftNeighbour;

	}

	private void calculateNumberOfNeighbours()
	{
		numberOfNeighbours = 0;
		if (topNeighbour != null)
			numberOfNeighbours++;
		if (rightNeighbour != null)
			numberOfNeighbours++;
		if (leftNeighbour != null)
			numberOfNeighbours++;
		if (bottomNeighbour != null)
			numberOfNeighbours++;

	}

	public void setPlayerHashkeyOfField(long[] playerHashkeyOfField)
	{
		this.playerHashkeyOfField = playerHashkeyOfField;
	}

	public long getKeyOfPlayerId(final int playerId)
	{
		return playerHashkeyOfField[playerId];
	}

	public boolean isVeryImportantField()
	{
		return isVeryImportantField;
	}

	public boolean isOuterRingField()
	{
		return isOuterRingField;
	}

	public boolean isInnerRingField()
	{
		return isInnerRingField;
	}

	public int getIdOfField()
	{
		return idOfField;
	}

	public int getTokenOfPlayerId()
	{
		return tokenOfPlayerId;
	}

	public boolean isEmpty()
	{
		return isEmpty;
	}

	public void setTokenOfPlayerId(int tokenOfPlayerId)
	{
		this.tokenOfPlayerId = tokenOfPlayerId;
	}

	public boolean isPartOfMillByPlayerId(final int playerId) //allgemeingültig
	{
		return (verticalRow.getNumberOfTokensByPlayerId(playerId) == 3 || horizontalRow.getNumberOfTokensByPlayerId(playerId) == 3);
	}

	public boolean bekomsMillByPlayerId(final int playerId) //Nutzung bei Steineinsetzphase
	{
		return (verticalRow.getNumberOfTokensByPlayerId(playerId) | horizontalRow.getNumberOfTokensByPlayerId(playerId)) >= 2;
	}

	public boolean bekomsVerticalMillByPlayerId(final int playerId) //Nutzung bei horizontalen Zügen in der mittleren Spielphase
	{
		return verticalRow.getNumberOfTokensByPlayerId(playerId) >= 2;
	}

	public boolean bekomsHorizontalMillByPlayerId(final int playerId) //Nutzung bei vertikalen Zügen in der mittleren Spielphase
	{
		return horizontalRow.getNumberOfTokensByPlayerId(playerId) >= 2;
	}

	public boolean bekomsMillByPlayerId(final int playerId, final int idOfIllegalField) //Nutzung bei Endspielphase
	{
		return !verticalRow.fieldIsPartOfMillRow(idOfIllegalField) && verticalRow.getNumberOfTokensByPlayerId(playerId) >= 2
				|| !horizontalRow.fieldIsPartOfMillRow(idOfIllegalField) && horizontalRow.getNumberOfTokensByPlayerId(playerId) >= 2;
	}
	public int getNumberOfNeighboursWithPlayerId(final int playerId)
	{
		return numberOfNeighboursWithPlayerId[playerId];
	}
	public MillRow getVerticalRow()
	{
		return verticalRow;
	}
	public MillRow getHorizontalRow()
	{
		return horizontalRow;
	}
	public Field getTopNeighbour()
	{
		return topNeighbour;
	}

	public Field getBottomNeighbour()
	{
		return bottomNeighbour;
	}

	public Field getLeftNeighbour()
	{
		return leftNeighbour;
	}

	public Field getRightNeighbour()
	{
		return rightNeighbour;
	}

	@Override
	public boolean equals(final Object obj)
	{
		final Field other = (Field) obj;
		return other.isEmpty == isEmpty || (other.isEmpty == isEmpty && other.tokenOfPlayerId == tokenOfPlayerId);
	}
}
