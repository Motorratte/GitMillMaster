package other;

public class Point
{
    private final double x, y;

    public Point(final double x, final double y)
    {
        this.x = x;
        this.y = y;
    }
    public static Point generatePointBetween(final Point p1, final Point p2, final double factor)
    {
        final double xDifference = p2.x - p1.x;
        final double yDifference = p2.y - p1.y;
        return new Point(p1.x + xDifference * factor, p1.y + yDifference * factor);
    }
    public double getX()
    {
        return x;
    }
    public double getY()
    {
        return y;
    }
}
