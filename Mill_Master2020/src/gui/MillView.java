package gui;

import game.MillModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Lighting;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Window;
import other.Point;
import other.TokenAndPosition;

public class MillView extends HBox
{
	private final MillPresenter presenter;
	private final TextArea player1Info;
	private final TextArea player2Info;
	private final Pane boardView;
	private final Pane tokenView;
	private final Label playerTurnInfoLabel;
	private final Label player1InfoLabel;
	private final Label player2InfoLabel;
	private final Point[] positionOfFields;
	private final CheckBox aiOnCheckBox;
	private double circleRadius;
	private double strokeWhidth;
	private Line arrow;
	private TokenAndPosition[] lastTokens;

	public MillView(final MillPresenter presenter)
	{
		this.presenter = presenter;
		final VBox menuePanel = new VBox();
		menuePanel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		menuePanel.setMinWidth(240);
		menuePanel.setMaxWidth(240);
		menuePanel.setPadding(new Insets(5));
		final Button newGameButton = new Button("New Game");
		newGameButton.setOnAction(e -> presenter.onNewGame());
		final Button undoMoveButton = new Button("Undo");
		undoMoveButton.setOnAction(e -> presenter.onUndoMove());
		final Button switchColorsButton = new Button("Switch Colors");
		switchColorsButton.setOnAction(e -> presenter.onSwitchColors());
		final Button player1OptionsButton = new Button("Player 1 Options");
		player1OptionsButton.setOnAction(e -> presenter.onPlayer1Options());
		final Button player2OptionsButton = new Button("Player 2 Options");
		player2OptionsButton.setOnAction(e -> presenter.onPlayer2Options());
		aiOnCheckBox = new CheckBox("use ai");
		aiOnCheckBox.setSelected(true);
		aiOnCheckBox.setOnAction(e -> presenter.onGlobalAiChanged(aiOnCheckBox.isSelected()));
		playerTurnInfoLabel = new Label("Player Move");
		playerTurnInfoLabel.setFont(new Font(14));
		player1InfoLabel = new Label("Player1 info:");
		player1InfoLabel.setFont(Font.font(16));
		player2InfoLabel = new Label("Player2 info:");
		player2InfoLabel.setFont(Font.font(16));
		player1Info = new TextArea();
		player1Info.setEditable(false);
		player1Info.setFont(Font.font(14));
		player2Info = new TextArea();
		player2Info.setEditable(false);
		player2Info.setFont(Font.font(14));
		final VBox buttonVbox = new VBox(newGameButton, undoMoveButton, switchColorsButton, player1OptionsButton, player2OptionsButton, playerTurnInfoLabel);
		final int buttonSpacing = 5;
		final int buttonWidth = ((int) menuePanel.getMinWidth()) - (buttonSpacing << 2);
		buttonVbox.getChildren().forEach(e -> ((Control) e).setMinWidth(buttonWidth));
		buttonVbox.setPadding(new Insets(buttonSpacing));
		menuePanel.getChildren().addAll(buttonVbox, aiOnCheckBox, player1InfoLabel, player1Info, player2InfoLabel, player2Info);
		boardView = new Pane();
		boardView.setStyle("-fx-background-color: #FFFFC0;");
		boardView.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
		tokenView = new Pane();
		tokenView.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
		tokenView.setOnMouseClicked(this::onMouseClick);
		this.getChildren().addAll(menuePanel, boardView);

		positionOfFields = new Point[MillModel.NUMBER_OF_FIELDS];
		ChangeListener<Number> listener = new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				presenter.onSizeChange();
			}
		};
		this.widthProperty().addListener(listener);
		this.heightProperty().addListener(listener);
		arrow = new Line();
		arrow.setEffect(new DropShadow());
		arrow.setVisible(false);
	}

	public boolean doSquareBoard()
	{
		if (Math.abs(boardView.getWidth() - boardView.getHeight()) < 0.0001)
		{
			return false;
		}
		double width = this.getScene().getWidth() - ((VBox) this.getChildren().get(0)).getWidth();
		double height = this.getScene().getHeight();
		double newSize;
		if (width < height)
		{
			newSize = width;
		}
		else
		{
			newSize = height;
		}
		if (newSize < 200)
			newSize = 200;
		final double widthDifference = newSize - width;
		final double heightDifference = newSize - height;
		final Window window = boardView.getScene().getWindow();
		window.setHeight(window.getHeight() + heightDifference);
		window.setWidth(window.getWidth() + widthDifference);
		boardView.setPrefSize(newSize, newSize);
		boardView.autosize();
		boardView.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
		return true;
	}

	public void onMouseClick(MouseEvent mouse)
	{
		if (mouse.getButton() != MouseButton.PRIMARY)
		{
			presenter.onMouseClick(-1);
			return;
		}
		final double x = mouse.getX();
		final double y = mouse.getY();
		final double minDistance = circleRadius * 2;
		double bestFoundDistance = Double.MAX_VALUE;
		int idOfBestField = -1;
		double currentDistance;
		Point positionOfCurrentField;
		for (int a = 0; a < positionOfFields.length; a++)
		{
			positionOfCurrentField = positionOfFields[a];
			currentDistance = getDistance(x, positionOfCurrentField.getX(), y, positionOfCurrentField.getY());
			if (currentDistance <= minDistance && currentDistance < bestFoundDistance)
			{
				bestFoundDistance = currentDistance;
				idOfBestField = a;
			}
		}
		if (idOfBestField >= 0)
			presenter.onMouseClick(idOfBestField);
	}

	public void setAiCheckBox(boolean selected)
	{
		aiOnCheckBox.setSelected(selected);
	}

	public void SetPlayerInfoText(String text)
	{
		playerTurnInfoLabel.setText(text);
	}

	public double getDistance(final double x1, final double x2, final double y1, final double y2)
	{
		final double xDif = x2 - x1;
		final double yDif = y2 - y1;
		return Math.sqrt(xDif * xDif + yDif * yDif);
	}

	public synchronized void fieldRefresh() //Aktuallisiert das Spielfeld
	{
		final double topLeftCorner = 0.05; //Anteilmäßige Position auf x- und y-Achse des äußeren Eckspielfeldes im Verhältniss zur Spielfeldgröße
		final double diagonallyDistance = 0.142; //Anteilmäßige diagonale Verschiebung der Ringe

		final double bottomRightCorner = 1 - topLeftCorner;
		final double middleLeftCorner = topLeftCorner + diagonallyDistance;
		final double middleRightCorner = 1 - middleLeftCorner;
		final double innerLeftCorner = middleLeftCorner + diagonallyDistance;
		final double innerRightCorner = 1 - innerLeftCorner;
		final double whidthOfField = boardView.getWidth();
		final double heightOfField = boardView.getHeight();

		positionOfFields[0] = new Point(topLeftCorner * whidthOfField, topLeftCorner * heightOfField);
		positionOfFields[1] = new Point(whidthOfField * 0.5, topLeftCorner * heightOfField);
		positionOfFields[2] = new Point(bottomRightCorner * whidthOfField, topLeftCorner * heightOfField);
		positionOfFields[3] = new Point(middleLeftCorner * whidthOfField, middleLeftCorner * heightOfField);
		positionOfFields[4] = new Point(whidthOfField * 0.5, middleLeftCorner * heightOfField);
		positionOfFields[5] = new Point(middleRightCorner * whidthOfField, middleLeftCorner * heightOfField);
		positionOfFields[6] = new Point(innerLeftCorner * whidthOfField, innerLeftCorner * heightOfField);
		positionOfFields[7] = new Point(whidthOfField * 0.5, innerLeftCorner * heightOfField);
		positionOfFields[8] = new Point(innerRightCorner * whidthOfField, innerLeftCorner * heightOfField);
		positionOfFields[9] = new Point(topLeftCorner * whidthOfField, heightOfField * 0.5);
		positionOfFields[10] = new Point(middleLeftCorner * whidthOfField, heightOfField * 0.5);
		positionOfFields[11] = new Point(innerLeftCorner * whidthOfField, heightOfField * 0.5);
		positionOfFields[12] = new Point(innerRightCorner * whidthOfField, heightOfField * 0.5);
		positionOfFields[13] = new Point(middleRightCorner * whidthOfField, heightOfField * 0.5);
		positionOfFields[14] = new Point(bottomRightCorner * whidthOfField, heightOfField * 0.5);
		positionOfFields[15] = new Point(innerLeftCorner * whidthOfField, innerRightCorner * heightOfField);
		positionOfFields[16] = new Point(whidthOfField * 0.5, innerRightCorner * heightOfField);
		positionOfFields[17] = new Point(innerRightCorner * whidthOfField, innerRightCorner * heightOfField);
		positionOfFields[18] = new Point(middleLeftCorner * whidthOfField, middleRightCorner * heightOfField);
		positionOfFields[19] = new Point(whidthOfField * 0.5, middleRightCorner * heightOfField);
		positionOfFields[20] = new Point(middleRightCorner * whidthOfField, middleRightCorner * heightOfField);
		positionOfFields[21] = new Point(topLeftCorner * whidthOfField, bottomRightCorner * heightOfField);
		positionOfFields[22] = new Point(whidthOfField * 0.5, bottomRightCorner * heightOfField);
		positionOfFields[23] = new Point(bottomRightCorner * whidthOfField, bottomRightCorner * heightOfField);

		strokeWhidth = (heightOfField + whidthOfField) * 0.005;
		Point start, end;
		Line[] lines = new Line[16];

		start = positionOfFields[0];
		end = positionOfFields[2];
		lines[0] = new Line(start.getX(), start.getY(), end.getX(), end.getY());
		start = positionOfFields[3];
		end = positionOfFields[5];
		lines[1] = new Line(start.getX(), start.getY(), end.getX(), end.getY());
		start = positionOfFields[6];
		end = positionOfFields[8];
		lines[2] = new Line(start.getX(), start.getY(), end.getX(), end.getY());
		start = positionOfFields[15];
		end = positionOfFields[17];
		lines[3] = new Line(start.getX(), start.getY(), end.getX(), end.getY());
		start = positionOfFields[18];
		end = positionOfFields[20];
		lines[4] = new Line(start.getX(), start.getY(), end.getX(), end.getY());
		start = positionOfFields[21];
		end = positionOfFields[23];
		lines[5] = new Line(start.getX(), start.getY(), end.getX(), end.getY());
		start = positionOfFields[0];
		end = positionOfFields[21];
		lines[6] = new Line(start.getX(), start.getY(), end.getX(), end.getY());
		start = positionOfFields[3];
		end = positionOfFields[18];
		lines[7] = new Line(start.getX(), start.getY(), end.getX(), end.getY());
		start = positionOfFields[6];
		end = positionOfFields[15];
		lines[8] = new Line(start.getX(), start.getY(), end.getX(), end.getY());
		start = positionOfFields[1];
		end = positionOfFields[7];
		lines[9] = new Line(start.getX(), start.getY(), end.getX(), end.getY());
		start = positionOfFields[16];
		end = positionOfFields[22];
		lines[10] = new Line(start.getX(), start.getY(), end.getX(), end.getY());
		start = positionOfFields[8];
		end = positionOfFields[17];
		lines[11] = new Line(start.getX(), start.getY(), end.getX(), end.getY());
		start = positionOfFields[5];
		end = positionOfFields[20];
		lines[12] = new Line(start.getX(), start.getY(), end.getX(), end.getY());
		start = positionOfFields[2];
		end = positionOfFields[23];
		lines[13] = new Line(start.getX(), start.getY(), end.getX(), end.getY());
		start = positionOfFields[9];
		end = positionOfFields[11];
		lines[14] = new Line(start.getX(), start.getY(), end.getX(), end.getY());
		start = positionOfFields[12];
		end = positionOfFields[14];
		lines[15] = new Line(start.getX(), start.getY(), end.getX(), end.getY());

		for (Line current : lines)
		{
			current.setStrokeWidth(strokeWhidth);
		}

		boardView.getChildren().clear();
		boardView.getChildren().addAll(lines);
		boardView.getChildren().addAll(arrow, tokenView);
		arrow.setStrokeWidth(strokeWhidth);
		circleRadius = strokeWhidth * 3;
	}

	public synchronized void refresh(final TokenAndPosition[] tokens) //Aktuallisiert Spielsteine
	{
		if (tokens == null)
			return;
		tokenView.getChildren().clear();
		double x, y;
		Color color;
		Circle circle1, circle2;
		final Lighting currentLight = new Lighting();
		currentLight.setSpecularConstant(1);
		currentLight.setSurfaceScale(4);
		currentLight.setDiffuseConstant(2);
		for (TokenAndPosition current : tokens)
		{
			if (current == null)
				break;
			color = current.getColor();
			x = current.getPosition().getX();
			y = current.getPosition().getY();
			circle1 = new Circle(x, y, circleRadius, color);
			circle1.setEffect(new DropShadow());
			if (current.getIdOfToken() >= 0)
			{
				circle2 = new Circle(x, y, circleRadius, color);
				circle2.setEffect(currentLight);
				tokenView.getChildren().addAll(circle1, circle2);
			}
			else
			{
				tokenView.getChildren().add(circle1);
			}

		}
		lastTokens = tokens;
	}

	public void setP1InfoText(String labelText, String text)
	{
		player1InfoLabel.setText(labelText);
		player1Info.setText(text);
	}

	public void setP2InfoText(String labelText, String text)
	{
		player2InfoLabel.setText(labelText);
		player2Info.setText(text);
	}

	public void setArrow(int startIndex, int endIndex, Color color)
	{
		arrow.setStroke(color);
		Point start = positionOfFields[startIndex];
		Point end = positionOfFields[endIndex];
		arrow.setStartX(start.getX());
		arrow.setStartY(start.getY());
		arrow.setEndX(end.getX());
		arrow.setEndY(end.getY());
		arrow.setVisible(true);
	}

	public void removeArrow()
	{
		arrow.setVisible(false);
	}

	public Point[] getPositionOfFields()
	{
		return positionOfFields;
	}
}
