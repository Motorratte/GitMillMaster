package other;

import game.MillMove;

public class MoveAndKey
{
    private int evaluation;
    private final MillMove move;
    private final long key;
    public MoveAndKey(final MillMove move, final long key)
    {
        this.move = move;
        this.key = key;
    }
    public MillMove getMove()
    {
        return move;
    }
    public long getKey()
    {
        return key;
    }
    public void setEvaluation(int evaluation)
    {
        this.evaluation = evaluation;
    }
    public int getEvaluation()
    {
        return evaluation;
    }
}
