package gui;

import game.GameModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MillPresenter
{
	private GameModel model;
	private MillView view;
	private PlayerOptionsView optionsViewP1;
	private Stage p1Stage;
	private PlayerOptionsView optionsViewP2;
	private Stage p2Stage;
	private boolean p2OptionsOpen = false;
	private Timeline timeLine;
	private boolean winMassageShown;
	private long lastResizeTime;
	private boolean boardNeedsResize = false;
	private final long resizeTimeNeeded = 500;
	private final long minTimeforNextResizeActivation = 50;

	public void initView()
	{
		view.SetPlayerInfoText(model.getPlayerInfo());
		winMassageShown = false;
		view.setAiCheckBox(model.isGlobalAiOn());
		setPlayerInfoAreaText();
	}

	public void setPlayerInfoAreaText()
	{
		view.setP1InfoText(model.getPlayerLabelText(0), model.getPlayerTextAreaInfoText(0));
		view.setP2InfoText(model.getPlayerLabelText(1), model.getPlayerTextAreaInfoText(1));
	}

	public void onNewGame()
	{
		model.newGame();
		refreshView();
		view.SetPlayerInfoText(model.getPlayerInfo());
		winMassageShown = false;
		setPlayerInfoAreaText();
	}

	public void onUndoMove()
	{
		model.undoMove();
		refreshView();
		view.SetPlayerInfoText(model.getPlayerInfo());
		winMassageShown = false;
		setPlayerInfoAreaText();
	}

	public void onSwitchColors()
	{
		model.switchColors();
		view.SetPlayerInfoText(model.getPlayerInfo());
		refreshView();
		setPlayerInfoAreaText();
	}

	public void onGlobalAiChanged(boolean on)
	{
		final boolean anim = model.isAnimationMode();
		model.setGlobalAiOn(on);
		if (model.isAnimationMode() != anim)
			refreshView();
		setPlayerInfoAreaText();
	}

	public void onClose()
	{
		if(p1Stage != null)
			p1Stage.close();
		if(p2Stage != null)
			p2Stage.close();
		model.saveGame();
	}

	public void onPlayer1Options()
	{
		if (optionsViewP1 == null)
			optionsViewP1 = new PlayerOptionsView(this, model.getPlayers(0), 0);
		if (p1Stage == null)
		{
			openNewStage(optionsViewP1, 1);
		}
		p1Stage.show();
	}

	public void onPlayer2Options()
	{
		if (optionsViewP2 == null)
			optionsViewP2 = new PlayerOptionsView(this, model.getPlayers(1), 1);
		if (p2Stage == null)
		{
			openNewStage(optionsViewP2, 2);
		}
		p2Stage.show();
	}

	private void openNewStage(final PlayerOptionsView view, final int playerID)
	{
		view.refreshView();
		Stage stage = new Stage();
		stage.setTitle("Player" + playerID + " Options");
		stage.setScene(new Scene(view));
		stage.setMinHeight(200);
		stage.setMaxHeight(200);
		stage.setMinWidth(300);
		stage.setMaxWidth(300);
		if(playerID == 1)
			p1Stage = stage;
		else 
			p2Stage = stage;
	}

	public void onSizeChange()
	{
		if (System.currentTimeMillis() - lastResizeTime > minTimeforNextResizeActivation)
		{
			boardNeedsResize = true;
			lastResizeTime = System.currentTimeMillis();
		}
		view.fieldRefresh();
		refreshView();
	}

	public void setModel(GameModel model)
	{
		this.model = model;
	}

	public void onMouseClick(final Integer idOfField)
	{
		model.fieldClicked(idOfField);
		setPlayerInfoAreaText();
		refreshView();
		view.SetPlayerInfoText(model.getPlayerInfo());
		checkForWinner();

	}

	public void checkForWinner()
	{
		if (!winMassageShown && (model.isGameIsDraw() || model.getMillModel().gameIsOver()))
		{
			Alert info = new Alert(AlertType.INFORMATION);
			info.setHeaderText(model.getPlayerInfo());
			info.show();
			winMassageShown = true;
		}
	}

	public void onChangeEngine(int engineId, int playerId)
	{
		synchronized (model)
		{
			model.changeToEngine(engineId, playerId);
			if (playerId == 0)
			{
				optionsViewP1.refreshView();
			}
			else
			{
				optionsViewP2.refreshView();
			}
			setPlayerInfoAreaText();
		}
	}

	public void onTimerCall()
	{
		final boolean doRefreshView = model.work();
		if (boardNeedsResize && System.currentTimeMillis() - lastResizeTime > resizeTimeNeeded)
		{
			boardNeedsResize = false;
			lastResizeTime = System.currentTimeMillis();
			if (view.doSquareBoard())
			{
				refreshView();
			}
		}
		else if (doRefreshView || model.isAnimationMode())
		{
			refreshView();
			if (doRefreshView)
			{
				setPlayerInfoAreaText();
				view.SetPlayerInfoText(model.getPlayerInfo());
				checkForWinner();
			}
		}
	}

	private void refreshView()
	{
		view.refresh(model.getTokenAndPositions(view.getPositionOfFields()));
	}

	public void setView(MillView view)
	{
		this.view = view;
	}

	public void startGameFrame()
	{
		timeLine = new Timeline(new KeyFrame(Duration.millis(20), e -> onTimerCall()));
		timeLine.setCycleCount(Timeline.INDEFINITE);
		timeLine.play();
	}
}
