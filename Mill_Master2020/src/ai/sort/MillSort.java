package ai.sort;

import ai.other.EvaluatedMove;
import ai.other.MoveObject;

import java.util.Random;

public class MillSort
{
	final Random random = new Random();
	final private EvaluatedMove[] merge = new EvaluatedMove[128];

	public void mergeSort(final EvaluatedMove[] moves)
	{
		if (moves.length <= 1)
			return;
		mergeSortR(moves, 0, moves.length - 1);
		//sortControll(moves);
	}

	public void mergeSort(final EvaluatedMove[] moves, final int startIndexInclusiv, final int endIndexExclusiv)
	{
		if (endIndexExclusiv - startIndexInclusiv <= 1)
			return;
		mergeSortR(moves, startIndexInclusiv, endIndexExclusiv - 1);
		//sortControll(moves);
	}

	private void mergeSortR(final EvaluatedMove[] moves, final int left, final int right)
	{
		final int middle = (left + right) >> 1;
		if (left < middle)
			mergeSortR(moves, left, middle);
		if (middle + 1 < right)
			mergeSortR(moves, middle + 1, right);
		int a = left, b = middle + 1;
		EvaluatedMove currentA, currentB;
		currentA = moves[a];
		currentB = moves[b];
		int mergeIndex = left;
		do
		{
			if (currentA.getEvaluation() >= currentB.getEvaluation())
			{
				merge[mergeIndex++] = currentA;
				a++;
				if (a <= middle) //a <= middle muss hier für die anpassung von currentA ohnehin geprüft werden daher while(true)
				{
					currentA = moves[a];
				}
				else
				{
					for (; b <= right; b++)
						merge[mergeIndex++] = moves[b];
					break;
				}
			}
			else
			{
				merge[mergeIndex++] = currentB;
				b++;
				if (b <= right)
				{
					currentB = moves[b];
				}
				else
				{
					for (; a <= middle; a++)
						merge[mergeIndex++] = moves[a];
					break;
				}
			}
		} while (true);
		for (int i = left; i <= right; i++)
			moves[i] = merge[i];
	}

	@SuppressWarnings("unused")
	private void sortControll(EvaluatedMove[] moves)
	{
		for (int a = 0; a < moves.length - 1; a++)
		{
			if (moves[a].getEvaluation() < moves[a + 1].getEvaluation())
				System.out.println("nicht sortiert!");
		}
	}

	public void randomSort(final MoveObject[] moves) //bringt die elemente im array in eine zufällige reihenfolge
	{
		randomSort(moves, 0, moves.length);
	}

	public void randomSort(final MoveObject[] moves, final int startIndexInclusiv, final int endIndexExclusiv)
	{
		final int range = endIndexExclusiv - startIndexInclusiv;
		MoveObject memorize;
		int choosenIndex;
		for (int a = startIndexInclusiv; a < endIndexExclusiv; a++)
		{
			choosenIndex = random.nextInt(range) + startIndexInclusiv;
			memorize = moves[a];
			moves[a] = moves[choosenIndex];
			moves[choosenIndex] = memorize;
		}
	}
	public void randomSort(final int[] moves, final int startIndexInclusiv, final int endIndexExclusiv)
	{
		final int range = endIndexExclusiv - startIndexInclusiv;
		int memorize;
		int choosenIndex;
		for (int a = startIndexInclusiv; a < endIndexExclusiv; a++)
		{
			choosenIndex = random.nextInt(range) + startIndexInclusiv;
			memorize = moves[a];
			moves[a] = moves[choosenIndex];
			moves[choosenIndex] = memorize;
		}
	}
}
