package other;

import javafx.scene.paint.Color;

public class TokenAndPosition
{
    private final Color color;
    private final Point position;
    private final int idOfToken;
    public TokenAndPosition(final Color color, final Point position, final int idOfToken)
    {
        this.color = color;
        this.position = position;
        this.idOfToken = idOfToken;
    }
    public Color getColor()
    {
        return color;
    }
    public Point getPosition()
    {
        return position;
    }
    public int getIdOfToken()
    {
        return idOfToken;
    }
}
