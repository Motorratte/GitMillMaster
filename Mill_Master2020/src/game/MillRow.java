package game;


public class MillRow
{
	private final int[] numberOfTokensByPlayerId;
	private final boolean[] idIsPartOfThisMillRow;
	private final Field[] fieldsInThisMillRow;
	private final MillRowContainer millRowInformation;

	public MillRow(final int[] fieldIds, final MillRowContainer millRowInformation)
	{
		this.millRowInformation = millRowInformation;
		numberOfTokensByPlayerId = new int[2];
		fieldsInThisMillRow = new Field[3];
		idIsPartOfThisMillRow = new boolean[MillModel.NUMBER_OF_FIELDS];
		for (int current : fieldIds)
		{
			idIsPartOfThisMillRow[current] = true;
		}
	}

	public int getNumberOfTokensByPlayerId(final int playerId)
	{
		return numberOfTokensByPlayerId[playerId];
	}

	public void addToken(final int playerId)
	{
		if (++numberOfTokensByPlayerId[playerId] == 3)
			millRowInformation.addNumberOfMills(playerId);
	}

	public void removeToken(final int playerId)
	{
		if (numberOfTokensByPlayerId[playerId]-- == 3)
			millRowInformation.reduceNumberOfMills(playerId);
	}

	public boolean fieldIsPartOfMillRow(final int fieldId)
	{
		return idIsPartOfThisMillRow[fieldId];
	}

	public Field[] getFieldsOfRow()
	{
		return fieldsInThisMillRow;
	}

	void setFieldsInRow(final Field[] board)
	{
		for (int i = 0, indexCount = 0; i < idIsPartOfThisMillRow.length; i++)
		{
			if (idIsPartOfThisMillRow[i])
			{
				fieldsInThisMillRow[indexCount++] = board[i];
			}
		}
	}
}
