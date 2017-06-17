package com.liumeo.landlords;

/**
 * 卡牌的属性
 */
public class Card implements Comparable<Card>
{

    private int flowerColor;//花色，取值0,1,2,3
    private int level;//卡牌的level，取值0~13,26.其中13为小王，26为大王
    private boolean chosen = false;//卡牌是否被玩家选中
    public int ID;//卡牌ID

    /**
     * 只为了进行卡牌排序，并不用作比较卡牌大小
     */
    public int compareTo(Card c)
    {
        return this.getIndex() - c.getIndex();
    }

    public int getIndex()
    {
        return this.ID;
    }

    public void setChosen()
    {
        if (this.chosen == true)
        {
            this.chosen = false;
        }
        else
        {
            this.chosen = true;
        }
    }

    public void setChosen(boolean newChosen)
    {
        this.chosen = newChosen;
    }

    public int getLevel()
    {
        return level;
    }


    public Card(int flowerColor, int level)
    {
        this.flowerColor = flowerColor;
        this.level = level;
        this.ID = (this.level * 4 + this.flowerColor) % 54;
    }

}
