package ai.other;

public class DataEvaluatedMove extends EvaluatedMove
{
	private final int[] evaluationData = new int[22];
	private int randomAdd;
	public int[] getEvaluationData()
	{
		return evaluationData;
	}
	public void setRandomAdd(int randomAdd)
	{
		this.randomAdd = randomAdd;
	}
	public int getRandomAdd()
	{
		return randomAdd;
	}
	public void addRandomValueToEvaluation()
	{
		addValueToEvaluation(randomAdd);
	}
}
