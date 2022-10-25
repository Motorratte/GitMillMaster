package ai.evaluation;

public class EvaluationData
{
	private final int[] numberOfUsedImportantFieldsByPlayerId  = new int[2];
	private final int[] numberOfStonesInOuterRingByPlayerId = new int[2];
	private final int[] numberOfStonesInInnerRingByPlayerId = new int[2];
	private final int[] numberOfFreedomesByPlayId = new int[2];
	
	private final int[] numberOfClosedAndBlockedOrBlockableMillsByPlayerId = new int[2];
	private final int[] numberOfClosedNotBlockableMillsByPlayerId = new int[2];
	private final int[] numberOfOpenTwoStonesByPlayerId = new int[2]; //wird im endspiel und in der eröffnung gebraucht
	private final int[] numberOfOpenButBlockedMillsByPlayerId = new int[2];
	private final int[] numberOfOpenBlockableMillsByPlayerId = new int[2];
	private final int[] numberOfOpenNotBlockableMillsByPlayerId = new int[2];
	private final int[] numberOfCatchedTokensByPlayerId = new int[2];
			
	private EvaluationData previousMillValues;
	private boolean useMillValuesOfPreviousMillValues;
	
}
