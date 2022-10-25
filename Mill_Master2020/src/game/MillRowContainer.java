package game;

public class MillRowContainer
{
    int[] numberOfMillsByPlayerId = new int[2];
    public void addNumberOfMills(final int playerId)
    {
        numberOfMillsByPlayerId[playerId]++;
    }
    public void reduceNumberOfMills(final int playerId)
    {
        numberOfMillsByPlayerId[playerId]--;
    }
    public boolean playerHasMills(final int playerId)
    {
        return numberOfMillsByPlayerId[playerId] > 0;
    }
    public int getNumberOfMillsByPlayerId(final int playerId)
	{
		return numberOfMillsByPlayerId[playerId];
	}
}
