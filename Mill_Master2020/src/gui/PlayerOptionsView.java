package gui;

import ai.Engine;
import game.PlayerInfo;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PlayerOptionsView extends VBox
{
	final int playerId;
	final MillPresenter presenter;
	final PlayerInfo playerInfo;
	final ComboBox<String> engineBox;
	final Spinner<Integer> difficultySpinner;
	final CheckBox playToWinCheckBox;
	final String[] engineStrings = new String[] { "Human", "Simple Bot", "Stupid Bot", "Eco Bot", "Mill Master" };

	public PlayerOptionsView(final MillPresenter presenter, final PlayerInfo playerInfo, final int playerId)
	{
		this.presenter = presenter;
		this.playerInfo = playerInfo;
		this.playerId = playerId;
		engineBox = new ComboBox<String>();
		engineBox.getItems().addAll(engineStrings);
		engineBox.setOnAction(e -> presenter.onChangeEngine(engineBox.getSelectionModel().getSelectedIndex(), playerId));
		difficultySpinner = new Spinner<Integer>();
		difficultySpinner.valueProperty().addListener(new ChangeListener<Integer>()
		{
			@Override
			public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue)
			{
				onDifficultyChange();
			}

		});
		HBox difficultyBox = new HBox();
		playToWinCheckBox = new CheckBox("dislike draw");
		playToWinCheckBox.setOnAction(e -> onPlayToWinChange());
		difficultyBox.getChildren().addAll(difficultySpinner, new Label("difficulty"));
		this.getChildren().addAll(engineBox, difficultyBox, playToWinCheckBox);
		refreshView();
	}

	public void onDifficultyChange()
	{
		if (difficultySpinner.getValue() > playerInfo.getEngine().getMaxDifficulty())
			difficultySpinner.getValueFactory().setValue(playerInfo.getEngine().getMaxDifficulty());
		playerInfo.getEngine().setDifficulty(difficultySpinner.getValue());
	}

	public void onPlayToWinChange()
	{
		playerInfo.getEngine().setPlayToWin(playToWinCheckBox.isSelected());
	}

	public void refreshView()
	{
		final Engine currentEngine = playerInfo.getEngine();
		this.getChildren().clear();
		this.getChildren().add(engineBox);
		if (currentEngine == null)
		{
			engineBox.getSelectionModel().select(0);
			return;
		}
		if (currentEngine.supportsDifficulty())
		{
			this.getChildren().add(difficultySpinner);
			difficultySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, currentEngine.getMaxDifficulty(), currentEngine.getDifficulty()));
		}

		if (currentEngine.supportsPlayToWin())
		{
			this.getChildren().add(playToWinCheckBox);
			playToWinCheckBox.setSelected(currentEngine.isPlayToWin());
		}
	}
}
