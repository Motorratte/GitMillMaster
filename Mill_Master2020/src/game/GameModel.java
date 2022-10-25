package game;

import ai.*;
import javafx.scene.paint.Color;
import other.Point;
import other.TokenAndPosition;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class GameModel
{
    private final PlayerInfo[] players;
    private final MillModel millModel;
    private int numberOfClicks = 0;
    private Integer[] clicks = new Integer[3];
    final ArrayList<MillMove> selectedMoves = new ArrayList<>();
    private final long timeLineDelayTime = 500;
    private long lastDelay = 0;
    private final long aiMoveClickDelaytime = 1000;
    private long lastAiMoveClickDelay = 0;
    private int aiAttackField = -1;
    private boolean globalAiOn = true;
    private boolean animationMode;
    private boolean attackAnimationMode;
    private boolean gameIsDraw;
    private double attackAnimationLevel;
    private double attackAnimationChangeRate = -0.01;
    private double animationLevel; // animationLevel ist ein Wert zwischen 0 und 1, der den Fortschritt der
    // Animation angibt. Bei 0 befindet sich die Animation am Startpunkt bei 1 ist
    // sie am Ziel angekommen
    private static final String GAME_SAVE_DATA_PATH = "save.txt";

    public GameModel()
    {
        players = new PlayerInfo[2];
        players[0] = new PlayerInfo();
        players[0].setColorOfToken(new Color(0.8, 0.4, 0.2, 1.0));
        players[1] = new PlayerInfo();
        players[1].setColorOfToken(new Color(0.3, 0.5, 0.8, 1.0));
        millModel = new MillModel();
        loadGame();
        millModel.generatePossibleMoves();
        gameIsDraw = false;
        players[1].setEngine(new MillMaster(millModel, 1, false));
    }

    public void switchColors()
    {
        Color color = players[0].getColorOfToken();
        players[0].setColorOfToken(players[1].getColorOfToken());
        players[1].setColorOfToken(color);
    }

    public synchronized void setGlobalAiOn(final boolean on)
    {
        if (on != globalAiOn)
        {
            globalAiOn = on;
            if (on && players[millModel.getCurrentPlayerId()].getEngine() != null)
            {
                animationMode = false;
            }
            refreshAI(0);
            refreshAI(1);
        }

    }

    public boolean isGlobalAiOn()
    {
        return globalAiOn;
    }

    public synchronized TokenAndPosition[] getTokenAndPositions(final Point[] positionOfFields)
    {
        final MillMove lastMove = millModel.getLastMove();
        final Field lastMoveDestination = lastMove == null ? null : lastMove.getDestination();
        final Field[] board = millModel.getBoard();
        final boolean showAttackLost = lastMove != null && lastMove.isAttackMove() && numberOfClicks == 0 && !animationMode;
        final TokenAndPosition[] tokens = new TokenAndPosition[millModel.getNumberOfStonesByPlayerID(0) + millModel.getNumberOfStonesByPlayerID(1) + 1 + (showAttackLost ? 1 : 0)];
        int tokensIndex = 0;
        if (showAttackLost)
        {
            final Color removedColor = new Color(0, 0, 0, 0.4);
            tokens[tokensIndex++] = new TokenAndPosition(removedColor, positionOfFields[lastMove.getAttackedIndex()], -1);
        }
        Field current;
        boolean[] animatedFieldIds = null;
        if (animationMode && attackAnimationMode && animationLevel >= 1.0)
        {
            animatedFieldIds = new boolean[board.length];
            Color attackedColor = players[millModel.getCurrentPlayerId() ^ 1].getColorOfToken();
            attackedColor = new Color(attackedColor.getRed(), attackedColor.getGreen(), attackedColor.getBlue(), attackAnimationLevel);
            for (MillMove currentMove : selectedMoves)
            {
                final int currentId = currentMove.getAttacked().getIdOfField();
                animatedFieldIds[currentId] = true;
                current = board[currentId];
                tokens[tokensIndex++] = new TokenAndPosition(attackedColor, positionOfFields[current.getIdOfField()], currentId);
            }
        }
        Color selectedColor = players[numberOfClicks > 0 && (!animationMode || attackAnimationMode) ? millModel.getCurrentPlayerId() : millModel.getCurrentPlayerId() ^ 1].getColorOfToken();
        selectedColor = new Color(selectedColor.getRed() + 0.1, selectedColor.getGreen() + 0.3, selectedColor.getBlue() + 0.1, 1);
        for (int a = 0; a < board.length; a++)
        {
            current = board[a];
            if (!current.isEmpty() && (!animationMode || (clicks[0] == null || current != board[clicks[0]]) && (clicks[1] == null || current != board[clicks[1]]) && (animatedFieldIds == null || !animatedFieldIds[a])))
            {
                if (clicks[0] != null && current.getIdOfField() == clicks[0] || clicks[0] == null && current == lastMoveDestination)
                {
                    tokens[tokensIndex++] = new TokenAndPosition(selectedColor, positionOfFields[current.getIdOfField()], a);
                }
                else
                {
                    tokens[tokensIndex++] = new TokenAndPosition(players[current.getTokenOfPlayerId()].getColorOfToken(), positionOfFields[current.getIdOfField()], a);
                }
            }
        }

        if (animationMode)
        {
            int playerId;
            if (millModel.getMoveNumber() >= 18)
            {
                if (millModel.getField(clicks[0]).isEmpty())
                {
                    playerId = millModel.getField(clicks[1]).getTokenOfPlayerId();
                }
                else
                {
                    playerId = millModel.getField(clicks[0]).getTokenOfPlayerId();
                }
                tokens[tokensIndex++] = new TokenAndPosition(selectedColor, Point.generatePointBetween(positionOfFields[clicks[0]], positionOfFields[clicks[1]], animationLevel), clicks[1]);
            }
            else
            {
                playerId = millModel.getCurrentPlayerId();
                tokens[tokensIndex++] = new TokenAndPosition(selectedColor, positionOfFields[clicks[0]], playerId);
            }

        }
        if (!attackAnimationMode && animationLevel >= 1)
            animationMode = false;
        return tokens;
    }

    private ArrayList<MillMove> selectMove()
    {
        selectedMoves.clear();
        if (numberOfClicks == 0)
            return selectedMoves;
        Field source = null, destination = null, attacked = null;
        if (millModel.getMoveNumber() < 18)
        {
            destination = millModel.getField(clicks[0]);
            if (numberOfClicks > 1)
                attacked = millModel.getField(clicks[1]);
        }
        else
        {
            source = millModel.getField(clicks[0]);
            if (numberOfClicks > 1)
            {
                destination = millModel.getField(clicks[1]);
                if (numberOfClicks > 2)
                {
                    attacked = millModel.getField(clicks[2]);
                }
            }
        }
        final MillMove[] moves = millModel.getPossibleMoves();
        for (MillMove current : moves)
        {
            if (numberOfClicks == 1)
            {
                if (source != null && current.getSource() == source || current.getDestination() == destination)
                {
                    selectedMoves.add(current);
                }
            }
            else
            {
                if (current.getSource() == source && current.getDestination() == destination)
                {
                    if (attacked == null)
                    {
                        selectedMoves.add(current);
                        if (current.getAttacked() == null)
                            break;
                    }
                    else if (current.getAttacked() == attacked)
                    {
                        selectedMoves.add(current);
                        break;
                    }
                }
            }
        }
        return selectedMoves;

    }

    public void loadGame()
    {
        FileReader reader = null;
        try
        {
            reader = new FileReader(GAME_SAVE_DATA_PATH);
            final int[] data = new int[reader.read() * 3];
            for (int i = 0; i < data.length; i++)
            {
                data[i] = reader.read() - 1;
            }
            reader.close();
            millModel.loadGame(data);
        }
        catch (Exception e)
        {
            System.out.println("No game save found");
        }

    }

    public synchronized void saveGame()
    {
        FileWriter writer = null;
        try
        {
            writer = new FileWriter(GAME_SAVE_DATA_PATH);
            final int[] data = millModel.getGameData();
            writer.write(millModel.getMoveNumber());
            for (int current : data)
            {
                writer.write(current + 1);
            }
            writer.close();
        }
        catch (IOException e)
        {
            System.out.println("File Write Failed!");
        }

    }

    public synchronized void fieldClicked(final int fieldId)
    {
        fieldClicked(fieldId, false, false);
    }

    private synchronized void fieldClicked(final int fieldId, boolean redo, boolean aiClick)
    {
        if (fieldId < 0 || millModel.gameIsOver() || gameIsDraw || !aiClick && globalAiOn && players[millModel.getCurrentPlayerId()].getEngine() != null)
            return;
        animationMode = false;
        clicks[numberOfClicks++] = fieldId;
        ArrayList<MillMove> moves = selectMove();
        if (moves.size() > 0)
        {
            if ((millModel.getMoveNumber() < 18 || numberOfClicks >= 2))
            {
                attackAnimationMode = true;
                if (millModel.getMoveNumber() < 18)
                {
                    attackAnimationMode = moves.get(0).isAttackMove() && numberOfClicks != 2;
                }
                else
                {
                    attackAnimationMode = numberOfClicks <= 2 && moves.get(0).isAttackMove();

                }
                if (!attackAnimationMode && moves.size() == 1)
                {
                    if (numberOfClicks == 2 && millModel.getMoveNumber() >= 18)
                        animationMode = true;
                    else
                        animationMode = false;
                    executeMove(moves.get(0));
                    if (!animationMode)
                        numberOfClicks = 0;
                    animationLevel = 0;
                }
                else
                {
                    animationMode = true;
                    if (millModel.getMoveNumber() < 18)
                        animationLevel = 1;
                    attackAnimationLevel = 1;
                    attackAnimationChangeRate = -0.01;
                }
            }
        }
        else
        {
            numberOfClicks = 0;
            animationMode = false;
            animationLevel = 0;
            clicks[2] = null;
            clicks[1] = null;
            if (!redo)
                fieldClicked(fieldId, true, aiClick);
        }
        for (int a = 2; a >= numberOfClicks; a--)
        {
            clicks[a] = null;
        }
    }

    private void executeMove(final MillMove move)
    {
        millModel.executeMove(move);
        if (!gameIsDraw)
            gameIsDraw = millModel.gameIsDraw();
        if (!gameIsDraw)
            millModel.generatePossibleMoves();
        aiAttackField = -1;
        lookForAiStuff();
        delayTimeline();
    }

    public MillModel getMillModel()
    {
        return millModel;
    }

    public void undoMove()
    {
        aiAttackField = -1;
        if (animationMode)
        {
            animationMode = false;
            refreshAI(millModel.getCurrentPlayerId());
            refreshAI(millModel.getCurrentPlayerId() ^ 1);
            return;
        }
        numberOfClicks = 0;
        clicks[0] = null;
        clicks[1] = null;
        if (millModel.getMoveNumber() == 0)
            return;
        millModel.undoMove();
        gameIsDraw = false;
        millModel.generatePossibleMoves();
        refreshAI(millModel.getCurrentPlayerId());
        refreshAI(millModel.getCurrentPlayerId() ^ 1);
    }

    public synchronized void changeToEngine(int engineId, final int playerId)
    {
        refreshAI(playerId);
        switch (engineId)
        {
            case 0:
                players[playerId].setEngine(null);
                break;
            case 1:
                players[playerId].setEngine(new SimpleBot(millModel, playerId, false));
                break;
            case 2:
                players[playerId].setEngine(new StupidBot(millModel, playerId, false));
                break;
            case 3:
                players[playerId].setEngine(new EcoBot(millModel, playerId, false));
                break;
            case 4:
                players[playerId].setEngine(new MillMaster(millModel, playerId, false));
                break;
        }
    }

    public synchronized void refreshAI(int playerId)
    {
        if (players[playerId].getEngine() != null)
        {
            players[playerId].getEngine().stopCalculation(false);
            players[playerId].getEngine().refreshAI();
        }
    }

    public PlayerInfo getPlayers(final int idOfPlayer)
    {
        return players[idOfPlayer];
    }

    public void newGame()
    {
        animationMode = false;
        millModel.undoMovesToNumber(0);
        millModel.generatePossibleMoves();
        gameIsDraw = false;
        aiAttackField = -1;
        refreshAI(0);
        refreshAI(1);
    }

    public boolean isGameIsDraw()
    {
        return gameIsDraw;
    }

    public String getPlayerInfo()
    {
        final int playerId = millModel.gameIsOver() ? millModel.getWinningPlayerId() : millModel.getCurrentPlayerId();
        PlayerInfo currentPlayer = players[playerId];
        if (gameIsDraw)
        {
            return "draw!";
        }
        else
        {
            return "Player " + (playerId + 1) + " (" + currentPlayer.getColorText() + ") " + (millModel.gameIsOver() ? "wins!" : "'s turn.");
        }
    }

    public void delayTimeline()
    {
        lastDelay = System.currentTimeMillis();
    }

    public synchronized boolean aiMoveExecutioner(final MillMove move)
    {
        if (!globalAiOn)
            return false;
        if (move != null)
        {
            if (millModel.getMoveNumber() < 18)
            {
                fieldClicked(move.getDestinationIndex(), false, true);
            }
            else
            {
                fieldClicked(move.getSourceIndex(), false, true);
                fieldClicked(move.getDestinationIndex(), false, true);
            }
            if (move.isAttackMove())
            {
                lastAiMoveClickDelay = System.currentTimeMillis();
                aiAttackField = move.getAttackedIndex();
            }
        }
        else if (aiAttackField >= 0 && System.currentTimeMillis() - aiMoveClickDelaytime >= lastAiMoveClickDelay)
        {
            fieldClicked(aiAttackField, false, true);
            return true;
        }
        return false;
    }

    public synchronized boolean lookForAiStuff()//ansteuerndes Gegenst�ck zur ai Schnittstelle Engine, gibt true zur�ck, wenn ein zug ausgef�hrt wurde
    {
        if (millModel.gameIsOver() || gameIsDraw || !globalAiOn || System.currentTimeMillis() - lastDelay <= timeLineDelayTime)
            return false;
        PlayerInfo currentPlayer;
        Engine currentEngine;
        boolean moveGotExecuted = false;
        for (int playerId = 0; playerId < players.length; playerId++)
        {
            currentPlayer = players[playerId];
            currentEngine = currentPlayer.getEngine();
            if (currentEngine != null)
            {
                if (playerId == millModel.getCurrentPlayerId() != aiAttackField >= 0)
                {
                    if (!currentEngine.isRunning())
                    {
                        if (currentEngine.getCalculatedMove() != null)
                        {
                            if (aiAttackField >= 0)
                            {
                                delayTimeline();
                                continue;
                            }
                            if (currentEngine.getCalculatedMove() != null && !millModel.isLegalMove(currentEngine.getCalculatedMove()))
                            {
                                System.out.println("AI FATAL ERROR: TRYED TO EXECUTE ILLEGAL MOVE (" + currentEngine.getCalculatedMove() + "), SOMETHINGS WRONG WITH AIMODEL!");
                                continue;
                            }
                            final MillMove calculatedMove = currentEngine.getCalculatedMove();
                            aiMoveExecutioner(calculatedMove);
                            moveGotExecuted = true;
                            currentEngine.calculatedMoveGotPlayed();
                            if (players[0].getEngine() != null)
                            {
                                players[0].getEngine().executeMoveAtAiModel(calculatedMove);
                            }
                            if (players[1].getEngine() != null)
                            {
                                players[1].getEngine().executeMoveAtAiModel(calculatedMove);
                            }
                            lookForAiStuff();
                            break;
                        }
                        else
                        {
                            delayTimeline();
                            if (!currentEngine.setOrderToRun())
                            {
                                continue;
                            }
                            currentPlayer.runPlayerEngine();
                        }
                    }
                    else if (!currentEngine.isStopCalculation())
                    {
                        if (currentEngine.isInPermanentBrainMode())
                        {
                            currentEngine.stopCalculation(true);
                            lookForAiStuff();
                        }
                    }
                }
                else
                {
                    if (!currentEngine.isInPermanentBrainMode() && currentEngine.isActivatePermanentBrainMode())
                    {
                        currentEngine.setOrderToRun();
                        currentPlayer.runPlayerEngine();
                    }
                }
            }
        }
        return moveGotExecuted;
    }

    public synchronized boolean work()
    {
        boolean doRefresh = false;
        doRefresh = lookForAiStuff();
        if (!doRefresh)
            doRefresh = aiMoveExecutioner(null);
        final boolean animationMode = numberOfClicks == 2 || this.animationMode;
        if (animationMode)
        {
            if (animationLevel < 1.0)
            {
                animationLevel += 0.05;
            }
            else if (attackAnimationMode)
            {
                attackAnimationLevel += attackAnimationChangeRate;
                if (attackAnimationLevel <= 0.3)
                    attackAnimationChangeRate = 0.02;
                else if (attackAnimationLevel >= 0.9)
                    attackAnimationChangeRate = -0.015;

            }

        }
        else
        {
            animationLevel = 0;
        }
        return doRefresh;
    }

    public boolean isAnimationMode()
    {
        return animationMode;
    }

    public String getPlayerLabelText(int idOfPlayer)
    {
        final StringBuilder result = new StringBuilder("Player" + (idOfPlayer + 1));
        final PlayerInfo playerInfo = players[idOfPlayer];
        if (!globalAiOn || playerInfo.getEngine() == null)
        {
            result.append(" human(" + playerInfo.getColorText() + ") info:");
        }
        else
        {
            result.append(" " + playerInfo.getEngine().toString() + "(" + playerInfo.getColorText() + ") info:");
        }
        return result.toString();
    }

    public String getPlayerTextAreaInfoText(int idOfPlayer)
    {
        StringBuilder result = new StringBuilder();
        PlayerInfo playerInfo = players[idOfPlayer];
        final int tokensToPlaceLft = millModel.getMoveNumber() < 18 ? (9 - ((millModel.getMoveNumber() + 1 - idOfPlayer) >> 1)) : 0;
        result.append("Tokens to place left: " + tokensToPlaceLft + "\n");
        result.append("Tokens at field: " + millModel.getNumberOfStonesByPlayerID(idOfPlayer) + "\n");
        result.append("Tokens lost: " + (9 - (millModel.getNumberOfStonesByPlayerID(idOfPlayer) + tokensToPlaceLft)) + "\n");
        final Engine currentEngine = playerInfo.getEngine();
        if (globalAiOn && currentEngine != null)
        {
            result.append("AI Evaluation: " + currentEngine.getCurrentEvaluation() + "\n");
            result.append("AI Number of evaluations: " + currentEngine.getNumberOfEvaluations() + "\n");
            result.append("AI Reached Depth: " + currentEngine.getReachedDepth() + "\n");
            result.append("AI Time needed: " + currentEngine.getTimeNeeded() + "ms" + "\n");
            result.append("AI Difficulty: " + currentEngine.getDifficulty() + "\n");
        }
        return result.toString();
    }
}
