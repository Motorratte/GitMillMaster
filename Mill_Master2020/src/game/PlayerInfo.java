package game;

import ai.Engine;
import javafx.scene.paint.Color;

public class PlayerInfo
{
	private Engine engine;
	private Color colorOfToken;
	private String colorText;

	public Color getColorOfToken()
	{
		return colorOfToken;
	}

	public void setColorOfToken(final Color colorOfToken)
	{
		this.colorOfToken = colorOfToken;
		double r, g, b;
		r = colorOfToken.getRed();
		g = colorOfToken.getGreen();
		b = colorOfToken.getBlue();
		if (r == g && g == b)
		{
			if (r > 0.5)
			{
				colorText = "white";
			}
			else
			{
				colorText = "black";
			}
		}
		else
		{
			if (r > g)
			{
				if (r > b)
				{
					colorText = "red";
				}
				else
				{
					colorText = "blue";
				}
			}
			else
			{
				if (g > b)
				{
					colorText = "green";
				}
				else
				{
					colorText = "blue";
				}
			}
		}
	}

	public void setEngine(final Engine engine)
	{
		this.engine = engine;
	}

	public Engine getEngine()
	{
		return engine;
	}
	public void runPlayerEngine()
	{
		new Thread(engine).start();
	}
	public void setDifficulty(int difficulty)
	{
		engine.setDifficulty(difficulty);
	}

	public void setLevelOfPugnacity(int levelOfPugnacity)
	{
		engine.setLevelOfPugnacity(levelOfPugnacity);
	}

	public void setNumberOfThreads(int numberOfThreads)
	{
		engine.setNumberOfThreads(numberOfThreads);
	}

	public String getColorText()
	{
		return colorText;
	}
}
