package ai.evaluation;

public class EvaluationData
{
	private int[] numberOfUsedImportantFieldsByPlayerId  = new int[2];
	private int[] numberOfStonesInOuterRingByPlayerId = new int[2];
	private int[] numberOfStonesInInnerRingByPlayerId = new int[2];
	private int[] numberOfFreedomesByPlayId = new int[2];
	
	private int[] numberOfClosedAndBlockedOrBlockableMillsByPlayerId = new int[2];
	private int[] numberOfClosedNotBlockableMillsByPlayerId = new int[2];
	private int[] numberOfOpenTwoStonesByPlayerId = new int[2]; //wird im endspiel und in der eröffnung gebraucht
	private int[] numberOfOpenButBlockedMillsByPlayerId = new int[2];
	private int[] numberOfOpenBlockableMillsByPlayerId = new int[2];
	private int[] numberOfOpenNotBlockableMillsByPlayerId = new int[2];
	private int[] numberOfCatchedTokensByPlayerId = new int[2];
			
	private EvaluationData previousMillValues;
	private boolean useMillValuesOfPreviousMillValues;
	
}
