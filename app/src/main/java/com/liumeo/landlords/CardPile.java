package com.liumeo.landlords;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * 抽象一组可以打出去的牌
 */
public class CardPile
{

    public enum CardType
    {
        DAN,//单牌
        DUI,//对子
        SAN,//3不带
        ZHA_DAN,//炸弹
        SAN_DAI_YI,//3带1
        SAN_DAI_ER,//3带2。
        ZHA_DAN_DAN,//4带2个单，或者一对
        ZHA_DAN_DUI,//4带2对
        SHUN,//顺子
        SHUANG_SHUN,//双顺子
        FEI_JI,//飞机
        FEI_JI_DAN,//飞机带单排.
        FEI_JI_DUI,//飞机带对子.
        HUO_JIAN,//火箭
        c0,//不能出牌
        cnull//空的
    }

    CardType type;//一组牌的类型，取值见上
    private int maxNum;//这组牌卡牌数字重复次数的最大值，取值0,1,2,3,4
    private int maxLevel;//对应上方最大值，重复最多次卡牌取到的最大level值
    private int maxContinue;//这组卡牌顺子最大长度，不考虑顺子重数
    private int maxContinueLevel;//顺子尾端卡牌level值，即顺子最大level值，取值小于12
    private List<Card> list = new ArrayList<>();//卡表
    private int[] cardNum;//长度15，每个level对应的卡片张数
    private int[] continueNum;//长度15，截止到每个level所能对应的顺子长度
    private int[] numNum;//长度5，例如：numNum[3]代表重复数为3的数字的个数

    /**
     * 构造方法
     */
    public CardPile()
    {
        this.type = CardType.cnull;
        this.maxNum = 0;
        this.maxLevel = -1;
        this.maxContinue = 0;
        this.maxContinueLevel = -1;
        this.cardNum = new int[15];
        this.continueNum = new int[15];
        this.numNum = new int[5];
    }

    public int getMaxLevel()
    {
        return this.maxLevel;
    }

    public int getMaxContinue()
    {
        return this.maxContinue;
    }

    public int getMaxContinueLevel()
    {
        return this.maxContinueLevel;
    }

    public List<Card> getList()
    {
        return list;
    }

    public CardType getType()
    {
        switch (this.type)
        {
            case cnull:
                System.out.println("空的");
                break;
            case c0:
                System.out.println("不可发牌");
                break;
            case DAN:
                System.out.println("单牌");
                break;
            case DUI:
                System.out.println("对子");
                break;
            case FEI_JI:
                System.out.println("飞机");
                break;
            case FEI_JI_DAN:
                System.out.println("飞机带单牌");
                break;
            case FEI_JI_DUI:
                System.out.println("飞机带对子");
                break;
            case HUO_JIAN:
                System.out.println("火箭");
                break;
            case SAN:
                System.out.println("三牌");
                break;
            case SAN_DAI_ER:
                System.out.println("三带二");
                break;
            case SAN_DAI_YI:
                System.out.println("三带一");
                break;
            case SHUANG_SHUN:
                System.out.println("双顺子");
                break;
            case SHUN:
                System.out.println("顺子");
                break;
            case ZHA_DAN:
                System.out.println("炸弹");
                break;
            case ZHA_DAN_DAN:
                System.out.println("炸弹带单牌");
                break;
            case ZHA_DAN_DUI:
                System.out.println("炸弹带对子");
                break;
            default:
                break;
        }
        return this.type;
    }

    public void add(Card c)
    {
        this.list.add(c);
        Collections.sort(this.list);
        if (c.getLevel() != 26)
        {	//大王的level值为26
            this.cardNum[c.getLevel()]++;
        }
        else
        {
            this.cardNum[14]++;
        }
    }

    /**
     * 按下标移除卡牌
     *
     * @param index 下标
     */
    public void remove(int index)
    {
        for (Card i : list)
        {
            if (i.getIndex() == index)
            {
                this.list.remove(i);
                if (i.getLevel() != 26)
                {
                    this.cardNum[i.getLevel()]--;
                }
                else
                {
                    this.cardNum[14]--;
                }
                break;
            }
        }
    }

    /**
     * 清空这组牌，用于玩家成功出牌后
     */
    public void clear()
    {
        this.type = CardType.cnull;
        this.maxNum = 0;
        this.maxLevel = -1;
        this.maxContinue = 0;
        this.maxContinueLevel = -1;
        for (int i = 0; i < 15; i++)
        {
            this.continueNum[i] = 0;
            this.cardNum[i] = 0;
        }
        for (int i = 0; i < 5; i++)
        {
            this.numNum[i] = 0;
        }
        this.list.clear();
    }

    /**
     * 刷新这组牌，重置若干参数，用于每次检验这组牌的类别时
     */
    public void refresh()
    {
        this.type = CardType.c0;
        for (int i = 0; i < 15; i++)
        {
            this.continueNum[i] = 0;
        }
        for (int i = 0; i < 5; i++)
        {
            this.numNum[i] = 0;
        }
        this.maxNum = 0;
        this.maxLevel = -1;
        this.maxContinue = 0;
        this.maxContinueLevel = -1;
    }

    /**
     * 检验这组牌的类型，检验完后更新type变量
     */
    public void judgeType()
    {
        this.refresh();
        for (int i = 0; i < 15; i++)
        {
            if (this.cardNum[i] != 0)
            {
                this.numNum[this.cardNum[i]]++;
                if (this.cardNum[i] >= this.maxNum)
                {
                    this.maxNum = this.cardNum[i];
                    this.maxLevel = i;
                }
                if (i < 12)
                {
                    if (i != 0 && this.cardNum[i] == this.cardNum[i - 1])
                    {
                        this.continueNum[i] = this.continueNum[i - 1] + 1;
                    }
                    else
                    {
                        this.continueNum[i] = 1;
                    }
                    if (this.continueNum[i] >= this.maxContinue)
                    {
                        if (this.maxContinueLevel == -1)
                        {
                            this.maxContinueLevel = i;
                        }
                        if (this.cardNum[i] >= this.cardNum[this.maxContinueLevel])
                        {
                            this.maxContinue = this.continueNum[i];
                            this.maxContinueLevel = i;
                        }
                    }
                }
            }
        }
        int len = list.size();
        if (len <= 4)
        {
            switch (len)
            {
                case 1:
                    this.type = CardType.DAN;
                    break;
                case 2:
                    if (this.maxNum == 2)
                    {
                        this.type = CardType.DUI;
                    }
                    else if (list.get(0).getLevel() + list.get(1).getLevel() == 39)
                    {
                        this.type = CardType.HUO_JIAN;
                    }
                    break;
                case 3:
                    if (this.maxNum == 3)
                    {
                        this.type = CardType.SAN;
                    }
                    break;
                case 4:
                    if (this.maxNum == 4)
                    {
                        this.type = CardType.ZHA_DAN;
                    }
                    else if (this.maxNum == 3)
                    {
                        this.type = CardType.SAN_DAI_YI;
                    }
                    break;
                default:
                    this.type = CardType.c0;
            }
        }
        else
        {
            if (this.maxContinueLevel != -1 && this.maxContinue * this.cardNum[this.maxContinueLevel] >= 5)
            {
                if (this.cardNum[this.maxContinueLevel] == 1 && this.maxContinue == len)
                {
                    this.type = CardType.SHUN;
                }
                else if (this.cardNum[this.maxContinueLevel] == 2 && this.maxContinue * 2 == len)
                {
                    this.type = CardType.SHUANG_SHUN;
                }
                else if (this.cardNum[this.maxContinueLevel] == 3)
                {
                    if (this.maxContinue * 3 == len)
                    {
                        this.type = CardType.FEI_JI;
                    }
                    else if (this.numNum[1] == this.numNum[3] && this.maxContinue * 4 == len)
                    {
                        this.type = CardType.FEI_JI_DAN;
                    }
                    else if (this.numNum[2] == this.numNum[3] && this.maxContinue * 5 == len)
                    {
                        this.type = CardType.FEI_JI_DUI;
                    }
                }
            }
            else
            {
                switch (len)
                {
                    case 5:
                        if (this.maxNum == 3 && this.numNum[2] == 1)
                        {
                            this.type = CardType.SAN_DAI_ER;
                        }
                        break;
                    case 6:
                        if (this.maxNum == 4)
                        {
                            this.type = CardType.ZHA_DAN_DAN;
                        }
                        break;
                    case 8:
                        if (this.maxNum == 4 && this.numNum[2] == 2)
                        {
                            this.type = CardType.ZHA_DAN_DUI;
                        }
                        break;
                    default:
                        this.type = CardType.c0;
                }
            }
        }
    }

    /**
     * 检验这组牌是否比场上存在的那组牌大
     *
     * @param courtPile 场上现存的一组牌，可以为空
     * @return 如果这组牌能管上场上的牌，则返回true
     */
    public boolean largerThan(CardPile courtPile)
    {
        if (courtPile.type == CardType.cnull && this.type != CardType.cnull && this.type != CardType.c0)
        {
            return true;
        }
        switch (this.type)
        {
            case DAN:
                return (courtPile.type == CardType.DAN && this.maxLevel > courtPile.maxLevel);
            case DUI:
                return (courtPile.type == CardType.DUI && this.maxLevel > courtPile.maxLevel);
            case FEI_JI:
                return (courtPile.type == CardType.FEI_JI && this.maxLevel > courtPile.maxLevel);
            case FEI_JI_DAN:
                return (courtPile.type == CardType.FEI_JI_DAN && this.maxLevel > courtPile.maxLevel);
            case FEI_JI_DUI:
                return (courtPile.type == CardType.FEI_JI_DUI && this.maxLevel > courtPile.maxLevel);
            case HUO_JIAN:
                return true;
            case SAN:
                return (courtPile.type == CardType.SAN && this.maxLevel > courtPile.maxLevel);
            case SAN_DAI_ER:
                return (courtPile.type == CardType.SAN_DAI_ER && this.maxLevel > courtPile.maxLevel);
            case SAN_DAI_YI:
                return (courtPile.type == CardType.SAN_DAI_YI && this.maxLevel > courtPile.maxLevel);
            case SHUANG_SHUN:
                return (courtPile.type == CardType.SHUANG_SHUN && this.maxLevel > courtPile.maxLevel);
            case SHUN:
                return (courtPile.type == CardType.SHUN && this.maxLevel > courtPile.maxLevel);
            case ZHA_DAN:
                if (courtPile.type == CardType.HUO_JIAN)
                {
                    return false;
                }
                else if (courtPile.type == CardType.ZHA_DAN)
                {
                    if (this.maxLevel < courtPile.maxLevel)
                    {
                        return false;
                    }
                    else
                    {
                        return true;
                    }
                }
                else
                {
                    return true;
                }
            case ZHA_DAN_DAN:
                if (courtPile.type == CardType.HUO_JIAN)
                {
                    return false;
                }
                else if (courtPile.type == CardType.ZHA_DAN_DAN)
                {
                    if (this.maxLevel < courtPile.maxLevel)
                    {
                        return false;
                    }
                    else
                    {
                        return true;
                    }
                }
                else
                {
                    return true;
                }
            case ZHA_DAN_DUI:
                if (courtPile.type == CardType.HUO_JIAN)
                {
                    return false;
                }
                else if (courtPile.type == CardType.ZHA_DAN_DUI)
                {
                    if (this.maxLevel < courtPile.maxLevel)
                    {
                        return false;
                    }
                    else
                    {
                        return true;
                    }
                }
                else
                {
                    return true;
                }
            case c0:
                return false;
            case cnull:
                return false;
            default:
                return false;

        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (Card i : list)
        {
            sb.append(",").append(i.ID);
        }
        return sb.toString();
    }
}
