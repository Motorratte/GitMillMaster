package ai;

import ai.other.EvaluatedMove;
import ai.sort.MillSort;
import game.Field;
import game.MillModel;
import game.MillMove;

import java.util.Random;

public abstract class Engine implements Runnable
{
	protected final Random random = new Random();
	protected MillModel model;
	protected Field[] board;
	protected long numberOfEvaluations;
	protected long rootKey;
	protected final MillSort sort = new MillSort();

	private MillModel originalModel;
	private int difficulty;
	private int reachedDepth;
	private int levelOfPugnacity;
	private boolean playToWin;
	private boolean inPermanentBrainMode; //befindet sich zurzeit tatsächlich in diesem Modus?
	private boolean activatePermanentBrainMode; //informartion für die äußere Schnittstelle, ob diese die ki starten soll, wenn der gegner am Zug ist.
	private int numberOfThreads;
	private int representsPlayerId;
	private int currentEvaluation;
	private MillMove calculatedMove;
	private boolean isRunning;
	private boolean stopCalculation;
	private boolean showTrueEvaluation;

	private boolean calculationStarted;
	private long startTime;
	private long timeNeeded;

	public Engine(final MillModel millModel, final int representsPlayerId, final boolean puppetMode)
	{
		levelOfPugnacity = 1;
		difficulty = 1;
		numberOfThreads = 1;
		showTrueEvaluation = true;
		if (!puppetMode)
		{
			originalModel = millModel;
			model = millModel.clone();
			this.representsPlayerId = representsPlayerId;
		}
		else
		{
			originalModel = null;
			model = millModel;
			this.representsPlayerId = representsPlayerId;
			board = model.getBoard();
			rootKey = model.getKeyOfCurrentPosition();
			refreshExtansion();
		}
	}

	public void calculatedMoveGotPlayed()
	{
		calculatedMove = null;
	}

	public synchronized boolean setOrderToRun() //muss immer vor dem aufruf von run ausgeführt werden
	{
		if (!model.gameIsOver())
		{
			calculatedMove = null;
			numberOfEvaluations = 0;
			reachedDepth = 1;
			isRunning = true;
			stopCalculation = false;
			System.out.println("Engine got order to run! PlayerId: " + representsPlayerId);
			return true;
		}
		else
		{
			return false;
		}
	}

	public synchronized void run()
	{
		model.synchronizeThisModellWithOther(originalModel, false); //spielzustand aktuallisieren
		if (!aiRunControll())
		{
			isRunning = false;
			return;
		}
		board = model.getBoard();
		rootKey = model.getKeyOfCurrentPosition();
		startTime = System.currentTimeMillis();
		calculationStarted = true;
		doSomethingBeforCalculation();
		calculate(); //Die eigentliche Berechnung der jeweiligen ai
		timeNeeded = System.currentTimeMillis() - startTime;
		calculationStarted = false;
		aiBugControll();
		isRunning = false;
		if (stopCalculation || isInPermanentBrainMode())
			calculatedMoveGotPlayed();
		printCalculationInformations();
	}

	private void printCalculationInformations()
	{
		System.out.println("AI Reached Depth: " + reachedDepth);
		System.out.println("AI Evaluation: " + currentEvaluation);
		System.out.println("AI Number of evaluations: " + numberOfEvaluations);
		System.out.println("AI Time needed: " + timeNeeded + "ms");
		System.out.println("AI move decision: " + calculatedMove);
		if (stopCalculation)
		{
			System.out.println("AI GOT ORDER TO BREAK CALCULATION!");
		}
		System.out.println("Engine stopt running!");
		System.out.println();
		System.out.println();
	}

	protected void doSomethingBeforCalculation()
	{
		//leere überschreibbare methode
	}

	protected abstract void calculate(); //Die eigentliche Berechnung der jeweiligen ai

	private boolean aiRunControll()
	{
		if (!isRunning)
		{
			System.out.println("AI RUN CONTROLL: didnt got order to run! PlayerId: " + representsPlayerId);
			return false;
		}
		if (model.gameIsOver())
		{
			System.out.println("AI RUN CONTROLL: game is over.");
			return false;
		}
		if (model.getCurrentPlayerId() != representsPlayerId)
		{
			if (supportsPermanentBrain() && activatePermanentBrainMode)
			{
				inPermanentBrainMode = true;
			}
			else
			{
				if (!supportsPermanentBrain())
				{
					System.out.println("AI RUN CONTROLL: this ai does not support permanent Brain!");
				}
				else
				{
					System.out.println("AI RUN CONTROLL: permanent brain is not set!");
				}
				return false;
			}
		}
		else
		{
			inPermanentBrainMode = false;
		}
		System.out.println("AI RUN CONTROLL: Engine is running! PlayerId: " + representsPlayerId);
		return true;
	}

	private void aiBugControll()//überprüft das spiel auf Fehler in der ai/model simulation. Fehler werden mit hoher Wahrscheinlichkeit gefunden
	{
		if (!isInPermanentBrainMode() && !stopCalculation) //Funktioniert nur, wenn die ai auch im originalen Model am Zug ist, weil sich das Original für die kontrolle nicht verändern darf. Daher nur wenn nicht im permanentBrainMode
		{
			if (calculatedMove == null)
			{
				System.out.println("AI BUG CONTROLL: no move calculated! MOVE GOT SET TO -1");
				calculatedMove = null;
			}
			else if (calculatedMove != null && !model.isLegalMove(calculatedMove))
			{
				System.out.println("AI BUG CONTROLL: illegal move (" + calculatedMove + ") calculated! MOVE GOT SET TO -1");
				calculatedMove = null;
			}
			if (model.getKeyOfCurrentPosition() != rootKey)
			{
				System.out.println("AI BUG CONTROLL: Key not equals to root!");
			}
			//todo prüfen ob die modell zustände gleich sind
		}
	}

	public synchronized void executeMoveAtAiModel(MillMove move)
	{
		model.executeMove(move.cloneMoveForOtherBoard(model.getBoard()));
	}

	protected final void chooseMove(final EvaluatedMove move)
	{
		chooseMove(new EvaluatedMove[] { move }, 0);
	}

	protected final void chooseMove(final EvaluatedMove[] moves, final int evaluationTollerance)//Wählt einen zufälligen der übergebenen Züge, welcher sich in der Bewertungstolleranzgrenze befindet
	{
		int bestEvaluation = Integer.MIN_VALUE;
		int currentEvaluation;
		for (int i = 0; i < moves.length; i++)
		{
			currentEvaluation = moves[i].getEvaluation();
			if (currentEvaluation > bestEvaluation)
				bestEvaluation = currentEvaluation;
		}
		int choosableValue = bestEvaluation - evaluationTollerance;
		int numberOfChoosable = 0;
		final int[] choosableIndexes = new int[moves.length];
		for (int i = 0; i < moves.length; i++)
		{
			currentEvaluation = moves[i].getEvaluation();
			if (currentEvaluation >= choosableValue)
				choosableIndexes[numberOfChoosable++] = i;
		}
		final EvaluatedMove toPlay = moves[choosableIndexes[random.nextInt(numberOfChoosable)]];
		calculatedMove = toPlay.getMove();
		setEvaluation(toPlay.getEvaluation());
	}

	public final void refreshAI() //sollte bei newGame oder undoMove aufgerufen werden! ein refresh ist nich möglich solange die ai arbeitet (vor dem aufruf am besten stopCalculation() aufrufen)
	{
		final boolean wasRunning = isRunning;
		if (isRunning)
		{
			System.out.println();
			System.out.println("Warte darauf, dass die Engine ihre ausführung beendet... PlayerId: " + representsPlayerId);
		}
		synchronized (this)
		{
			model.synchronizeThisModellWithOther(originalModel);
			calculatedMove = null;
			inPermanentBrainMode = false;
			refreshExtansion();
		}
		if (wasRunning)
		{
			System.out.println("Engine läuft nicht mehr, refresh erfolgreich!");
			System.out.println();
		}
	}

	protected void refreshExtansion() //zusätzliches toDo beim überschreiben
	{

	}

	protected final void setEvaluation(final int evaluation)
	{
		if (!showTrueEvaluation) //Normt den Wertebereich auf einen Wert zwischen 4 und -4
		{
			final int evaluationRange = getMaxEvaluation() - getMinEvaluation();
			final int distanceToMax = getMaxEvaluation() - evaluation;
			final int maxResult = 4;
			final int minResult = -4;
			final int numberOfPossibleEvaluationlevels = maxResult - minResult + 1;
			final int rangeOfOneEvaluationLevel = (int) (evaluationRange / (double) numberOfPossibleEvaluationlevels);
			if (distanceToMax < 0)
				this.currentEvaluation = maxResult;
			else if (distanceToMax >= evaluationRange)
				this.currentEvaluation = minResult;
			else
			{
				this.currentEvaluation = maxResult - distanceToMax / rangeOfOneEvaluationLevel + 1;
			}
		}
		else //Zeigt die tatsächliche Bewertung der Engine
		{
			this.currentEvaluation = evaluation;
		}
	}

	public void setActivatePermanentBrainMode(final boolean activatePermanentBrainMode)
	{
		if (supportsPermanentBrain())
		{
			this.activatePermanentBrainMode = activatePermanentBrainMode;
		}
	}

	public void stopCalculation(final boolean hardStop)
	{
		stopCalculation = true;
		if (hardStop)
		{
			int iterationNumber = 0;
			while (isRunning)
			{
				try
				{
					Thread.sleep(10);
				}
				catch (InterruptedException e)
				{
				}
				if (isRunning && iterationNumber % 200 == 0)
				{
					System.out.println("ENGINE STOP ERROR: Die Engine stopt nicht!");
					if (inPermanentBrainMode)
					{
						System.out.println("ENGINE STOP ERROR: Die Engine befindet sich im permanent brain Modus!");
					}
					System.out.println("ENGINE STOP ERROR: Der Hauptanwendungsthread blockiert und wartet auf die Engine!");
				}
				iterationNumber++;
			}
		}
	}

	public void setDifficulty(final int difficulty)
	{
		this.difficulty = difficulty;
	}

	public void setLevelOfPugnacity(final int levelOfPugnacity)
	{
		this.levelOfPugnacity = levelOfPugnacity;
	}

	public void setNumberOfThreads(final int numberOfThreads)
	{
		this.numberOfThreads = numberOfThreads;
	}

	public boolean isStopCalculation()
	{
		return stopCalculation;
	}

	public void setPlayToWin(boolean playToWin)
	{
		this.playToWin = playToWin;
	}

	public boolean isPlayToWin()
	{
		return playToWin;
	}

	public boolean isActivatePermanentBrainMode()
	{
		return activatePermanentBrainMode;
	}

	public int getDifficulty()
	{
		return difficulty;
	}

	public int getLevelOfPugnacity()
	{
		return levelOfPugnacity;
	}

	public int getMaxEvaluation() //der evaluationsbereich, mit dem die eigene ai arbeitet
	{
		return 10;
	}

	public int getMinEvaluation()//der evaluationsbereich, mit dem die eigene ai arbeitet
	{
		return -10;
	}

	public synchronized MillMove getCalculatedMove()
	{
		return getCalculatedMove(false);
	}

	public synchronized MillMove getCalculatedMove(final boolean original)
	{
		if (original)
			return calculatedMove;
		return calculatedMove == null ? null : calculatedMove.cloneMoveForOtherBoard(originalModel.getBoard());
	}

	public int getCurrentEvaluation()
	{
		return currentEvaluation;
	}

	public void setShowTrueEvaluation(boolean showTrueEvaluation)
	{
		this.showTrueEvaluation = showTrueEvaluation;
	}

	public void setReachedDepth(int reachedDepth)
	{
		this.reachedDepth = reachedDepth;
	}

	public int getReachedDepth()
	{
		return reachedDepth;
	}

	public long getTimeNeeded()
	{
		if (calculationStarted)
		{
			return System.currentTimeMillis() - startTime;
		}
		else
		{
			return timeNeeded;
		}
	}

	public long getNumberOfEvaluations()
	{
		return numberOfEvaluations;
	}

	public boolean isInPermanentBrainMode()
	{
		return inPermanentBrainMode;
	}

	public boolean isRunning()
	{
		return isRunning;
	}

	public int getNumberOfThreads()
	{
		return numberOfThreads;
	}

	public int getRepresentedPlayerId()
	{
		return representsPlayerId;
	}

	public boolean supportsDifficulty()
	{
		return false;
	}

	public boolean supportsLevelOfPugnacity()
	{
		return false;
	}

	public boolean supportsPermanentBrain()
	{
		return false;
	}

	public boolean supportsPlayToWin()
	{
		return false;
	}

	public boolean supportsMultithreading()
	{
		return false;
	}

	public int getMaxNumberOfThreads()
	{
		return 1;
	}

	public int getMaxDifficulty()
	{
		return 1;
	}

	public int getMaxLevelOfPugnacity()
	{
		return 1;
	}

	@Override
	public String toString()
	{
		return "unknown engine";
	}
}
