package ai.other;

public class DataEvaluatedMove extends EvaluatedMove
{
	private final int[] evaluationData = new int[22];
	private int randomeAdd;
	public int[] getEvaluationData()
	{
		return evaluationData;
	}
	public void setRandomeAdd(int randomeAdd)
	{
		this.randomeAdd = randomeAdd;
	}
	public int getRandomeAdd()
	{
		return randomeAdd;
	}
	public void addRandomeValueToEvaluation()
	{
		addValueToEvaluation(randomeAdd);
	}
}
