package programm_starter;

import game.Field;
import game.MillModel;
import game.MillMove;

import java.util.Random;

public class BugChecker
{

	public static void main(String[] args)
	{
		final MillModel startComperatorModel = new MillModel();
		final MillModel comperatorModel = new MillModel();
		final MillModel model = new MillModel();
		final Random random = new Random(536564);
		final int numberOfGames = 100000;
		System.out.println("Test started");
		for (int gameNumber = 0; gameNumber < numberOfGames; gameNumber++)
		{
			if(gameNumber % 1000 == 0)
				System.out.println(gameNumber);
			do
			{
				model.generatePossibleMoves();
				final MillMove[] possibleMoves = model.getPossibleMoves();
				if (possibleMoves.length == 0)
					break;
				for (MillMove currentMillMove : possibleMoves)
				{
					final MillMove clonedMove = currentMillMove.cloneMoveForOtherBoard(comperatorModel.getBoard());
					if (!comperatorModel.isLegalMove(clonedMove))
					{
						System.out.println("Fehler! Illegal Move!");
					}
					try
					{
						model.executeMove(currentMillMove);
						model.undoMove();
					}
					catch (Exception e)
					{
						System.out.println("Crash!");
						comperatorModel.executeMove(clonedMove);
						comperatorModel.undoMove();
					}

					if (!modelsEquals(comperatorModel, model))
					{
						System.out.println("FEHLER!");
						comperatorModel.executeMove(clonedMove);
						comperatorModel.undoMove();
					}
					
				}
				final MillMove moveDecision = possibleMoves[random.nextInt(possibleMoves.length)];
				model.executeMove(moveDecision);
				comperatorModel.executeMove(moveDecision.cloneMoveForOtherBoard(comperatorModel.getBoard()));
			} while (!model.gameIsOver() && model.getMoveNumber() < 600);
			model.undoMovesToNumber(0);
			comperatorModel.undoMovesToNumber(0);
			if (!modelsEquals(startComperatorModel, model))
			{
				System.out.println("Fehler!");
			}
		}
		System.out.println("Test ended");
	}

	public static boolean modelsEquals(MillModel model1, MillModel model2)
	{
		if (model1.getKeyOfCurrentPosition() != model2.getKeyOfCurrentPosition())
			return false;
		try
		{
			model1.generatePossibleMoves();
			model2.generatePossibleMoves();
		}
		catch (Exception e)
		{
			System.out.println("Crash!");
			model2.generatePossibleMoves();
		}

		if (model1.getNumberOfPossibleMoves() != model2.getNumberOfPossibleMoves())
			return false;
		Field[] board1 = model1.getBoard();
		Field[] board2 = model2.getBoard();
		for (int i = 0; i < board1.length; i++)
		{
			if (!board1[i].equals(board2[i]))
			{
				return false;
			}
		}

		return true;
	}
}
