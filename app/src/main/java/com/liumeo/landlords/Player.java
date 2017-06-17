package com.liumeo.landlords;

import java.util.ArrayList;
import java.util.List;

import java.util.Collections;

/**
 * 实现玩家的动作
 */
public class Player
{

	private String name;//玩家名字
	public List<Card> list = new ArrayList<>();//玩家手牌表
	private CardPile dealPile = new CardPile();//玩家打算打出去的一组牌

	public Player(String name)
	{
		super();
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	public int size()
	{
		return this.list.size();
	}

	/**
	 * 玩家 接收 卡片
	 */
	public void addCard(Card c)
	{
		list.add(c);
		Collections.sort(list);
	}

	/**
	 * 用于自动选牌功能，检索符合条件的牌并选中，若成功检索并选中，则返回true
	 *
	 * @param num   描述玩家手牌分布的数组，长度15
	 * @param begin 数组检索起始点，即要满足检索到的牌比场上的牌大
	 * @param times 检索卡牌的重复数，取值0,1,2,3,4
	 * @param con   检索顺子的长度
	 * @return 返回成功与否
	 */
	public boolean search(int[] num, int begin, int times, int con)
	{
		int continues = 0;        //检索到的顺子长度
		int index = -1;            //检索到的符合条件的起始下标，若未检索到，则保持-1
		int chosenTimes = 0;    //同一数字的卡牌被选择的次数，防止选多
		int conTimes = 0;        //选择到的顺子长度，防止选多
		/*检索是否存在符合要求的一组牌*/
		for (int i = begin; i < 15; i++)
		{
			if (i == 12 & con > 1)
			{
				break;
			}
			if (num[i] >= times)
			{
				continues++;
			} else
			{
				continues = 0;
			}
			if (continues == con)
			{
				index = i - con + 1;
				break;
			}
		}
		/*检索成功，替玩家选中目标卡牌*/
		if (index >= 0)
		{
			for (Card c : list)
			{
				if ((c.getLevel() == index & chosenTimes != times)
						| (c.getLevel() == 26 & index == 14 & chosenTimes != times))
				{
					dealPile.add(c);
					c.setChosen();
					chosenTimes++;
				}
				if (chosenTimes == times)
				{
					chosenTimes = 0;
					num[index] = 0;
					index++;
					conTimes++;
				}
				if (conTimes == con)
				{
					break;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 实现自动选牌功能，目标是从手中选出一组牌，能打出并管上场上存在的卡牌
	 *
	 * @param courtPile 场上存在的一组卡牌
	 */
	public void autoDeal(CardPile courtPile)
	{
		for (Card c : list)
		{
			c.setChosen(false);
		}
		int[] num = new int[15];    //玩家每一level的卡牌的重复数，取值0,1,2,3,4
		int[] numNum = new int[5];    //例如：numNum[3]代表玩家手中重复3张的卡牌的种数
		int[] maxNum = new int[5];    //例如：maxNum[3]代表玩家手中至少重复3张的卡牌的最大level值
    	/*通过玩家手牌初始化以上数组*/
		for (Card i : list)
		{
			if (i.getLevel() != 26)
			{
				num[i.getLevel()]++;
			} else
			{
				num[14]++;
			}
		}
		for (int i = 0; i < 15; i++)
		{
			numNum[num[i]]++;
			maxNum[num[i]] = i;
		}
		if (maxNum[3] < maxNum[4])
		{
			maxNum[3] = maxNum[4];
		}
		if (maxNum[2] < maxNum[3])
		{
			maxNum[2] = maxNum[3];
		}
		if (maxNum[1] < maxNum[2])
		{
			maxNum[1] = maxNum[2];
		}
    	
    	/*根据场上现存卡牌进行选牌*/
		switch (courtPile.type)
		{
			case HUO_JIAN:
				break;
			case ZHA_DAN_DUI:
				if (maxNum[4] > courtPile.getMaxLevel() && numNum[2] + numNum[3] + numNum[4] > 2)
				{
					this.search(num, courtPile.getMaxLevel() + 1, 4, 1);
					this.search(num, 0, 2, 1);
					this.search(num, 0, 2, 1);
				} else if (num[14] == 1 && num[13] == 1)
				{
					dealPile.add(list.get(list.size() - 1));
					list.get(list.size() - 1).setChosen(true);
					dealPile.add(list.get(list.size() - 2));
					list.get(list.size() - 2).setChosen(true);
				}
				break;
			case ZHA_DAN_DAN:
				if (maxNum[4] > courtPile.getMaxLevel())
				{
					if (numNum[1] > 0 & numNum[1] + numNum[2] + numNum[3] + numNum[4] > 2)
					{
						this.search(num, courtPile.getMaxLevel() + 1, 4, 1);
						this.search(num, 0, 1, 1);
						this.search(num, 0, 1, 1);
					} else if (numNum[1] == 0 & numNum[2] + numNum[3] + numNum[4] >= 2)
					{
						this.search(num, courtPile.getMaxLevel() + 1, 4, 1);
						this.search(num, 0, 2, 1);
					}

				} else if (num[14] == 1 && num[13] == 1)
				{
					dealPile.add(list.get(list.size() - 1));
					list.get(list.size() - 1).setChosen(true);
					dealPile.add(list.get(list.size() - 2));
					list.get(list.size() - 2).setChosen(true);
				}
				break;
			case ZHA_DAN:
				if (maxNum[4] > courtPile.getMaxLevel())
				{
					this.search(num, courtPile.getMaxLevel() + 1, 4, 1);
				} else if (num[14] == 1 && num[13] == 1)
				{
					dealPile.add(list.get(list.size() - 1));
					list.get(list.size() - 1).setChosen(true);
					dealPile.add(list.get(list.size() - 2));
					list.get(list.size() - 2).setChosen(true);
				}
				break;
			case FEI_JI_DUI:
				if (maxNum[3] > courtPile.getMaxContinueLevel()
						&& numNum[3] + numNum[4] >= courtPile.getMaxContinue()
						&& numNum[2] + numNum[3] + numNum[4] >= 2 * courtPile.getMaxContinue())
				{
					if (this.search(num, courtPile.getMaxLevel() - courtPile.getMaxContinue() + 2, 3, courtPile.getMaxContinue()))
					{
						for (int i = 0; i < courtPile.getMaxContinue(); i++)
						{
							this.search(num, 0, 2, 1);
						}
					}
				} else if (maxNum[4] > courtPile.getMaxLevel())
				{
					this.search(num, courtPile.getMaxLevel() + 1, 4, 1);
				} else if (num[14] == 1 && num[13] == 1)
				{
					dealPile.add(list.get(list.size() - 1));
					list.get(list.size() - 1).setChosen(true);
					dealPile.add(list.get(list.size() - 2));
					list.get(list.size() - 2).setChosen(true);
				}
				break;
			case FEI_JI_DAN:
				if (maxNum[3] > courtPile.getMaxContinueLevel()
						&& numNum[3] + numNum[4] >= courtPile.getMaxContinue()
						&& numNum[1] + numNum[2] + numNum[3] + numNum[4] >= 2 * courtPile.getMaxContinue())
				{
					if (this.search(num, courtPile.getMaxLevel() - courtPile.getMaxContinue() + 2, 3, courtPile.getMaxContinue()))
					{
						for (int i = 0; i < courtPile.getMaxContinue(); i++)
						{
							this.search(num, 0, 1, 1);
						}
					}
				} else if (maxNum[4] > courtPile.getMaxLevel())
				{
					this.search(num, courtPile.getMaxLevel() + 1, 4, 1);
				} else if (num[14] == 1 && num[13] == 1)
				{
					dealPile.add(list.get(list.size() - 1));
					list.get(list.size() - 1).setChosen(true);
					dealPile.add(list.get(list.size() - 2));
					list.get(list.size() - 2).setChosen(true);
				}
				break;
			case FEI_JI:
				if (maxNum[3] > courtPile.getMaxContinueLevel()
						&& numNum[3] + numNum[4] >= courtPile.getMaxContinue())
				{
					this.search(num, courtPile.getMaxLevel() - courtPile.getMaxContinue() + 2, 3, courtPile.getMaxContinue());
				} else if (maxNum[4] > courtPile.getMaxLevel())
				{
					this.search(num, courtPile.getMaxLevel() + 1, 4, 1);
				} else if (num[14] == 1 && num[13] == 1)
				{
					dealPile.add(list.get(list.size() - 1));
					list.get(list.size() - 1).setChosen(true);
					dealPile.add(list.get(list.size() - 2));
					list.get(list.size() - 2).setChosen(true);
				}
				break;
			case SAN_DAI_ER:
				if (maxNum[3] > courtPile.getMaxContinueLevel() && numNum[2] + numNum[3] + numNum[4] > 1)
				{
					if (this.search(num, courtPile.getMaxLevel() + 1, 3, 1))
					{
						this.search(num, 0, 2, 1);
					}
				} else if (maxNum[4] > courtPile.getMaxLevel())
				{
					this.search(num, courtPile.getMaxLevel() + 1, 4, 1);
				} else if (num[14] == 1 && num[13] == 1)
				{
					dealPile.add(list.get(list.size() - 1));
					list.get(list.size() - 1).setChosen(true);
					dealPile.add(list.get(list.size() - 2));
					list.get(list.size() - 2).setChosen(true);
				}
				break;
			case SAN_DAI_YI:
				if (maxNum[3] > courtPile.getMaxContinueLevel() && numNum[1] + numNum[2] + numNum[3] + numNum[4] > 1)
				{
					if (this.search(num, courtPile.getMaxLevel() + 1, 3, 1))
					{
						this.search(num, 0, 1, 1);
					}
				} else if (maxNum[4] > courtPile.getMaxLevel())
				{
					this.search(num, courtPile.getMaxLevel() + 1, 4, 1);
				} else if (num[14] == 1 && num[13] == 1)
				{
					dealPile.add(list.get(list.size() - 1));
					list.get(list.size() - 1).setChosen(true);
					dealPile.add(list.get(list.size() - 2));
					list.get(list.size() - 2).setChosen(true);
				}
				break;
			case SAN:
				if (maxNum[3] > courtPile.getMaxContinueLevel())
				{
					this.search(num, courtPile.getMaxLevel() + 1, 3, 1);
				} else if (num[14] == 1 && num[13] == 1)
				{
					dealPile.add(list.get(list.size() - 1));
					list.get(list.size() - 1).setChosen(true);
					dealPile.add(list.get(list.size() - 2));
					list.get(list.size() - 2).setChosen(true);
				}
				break;
			case SHUANG_SHUN:
				if (maxNum[2] > courtPile.getMaxContinueLevel()
						&& numNum[2] + numNum[3] + numNum[4] >= courtPile.getMaxContinue())
				{
					this.search(num, courtPile.getMaxLevel() - courtPile.getMaxContinue() + 2, 2, courtPile.getMaxContinue());
				} else if (maxNum[4] > courtPile.getMaxLevel())
				{
					this.search(num, courtPile.getMaxLevel() + 1, 4, 1);
				} else if (num[14] == 1 && num[13] == 1)
				{
					dealPile.add(list.get(list.size() - 1));
					list.get(list.size() - 1).setChosen(true);
					dealPile.add(list.get(list.size() - 2));
					list.get(list.size() - 2).setChosen(true);
				}
				break;
			case DUI:
				if (maxNum[2] > courtPile.getMaxContinueLevel())
				{
					this.search(num, courtPile.getMaxLevel() + 1, 2, 1);
				} else if (num[14] == 1 && num[13] == 1)
				{
					dealPile.add(list.get(list.size() - 1));
					list.get(list.size() - 1).setChosen(true);
					dealPile.add(list.get(list.size() - 2));
					list.get(list.size() - 2).setChosen(true);
				}
				break;
			case SHUN:
				if (this.search(num, courtPile.getMaxLevel() - courtPile.getMaxContinue() + 2, 1, courtPile.getMaxContinue()))
				{
				} else if (maxNum[4] > courtPile.getMaxLevel())
				{
					this.search(num, courtPile.getMaxLevel() + 1, 4, 1);
				} else if (num[14] == 1 && num[13] == 1)
				{
					dealPile.add(list.get(list.size() - 1));
					list.get(list.size() - 1).setChosen(true);
					dealPile.add(list.get(list.size() - 2));
					list.get(list.size() - 2).setChosen(true);
				}
				break;
			case DAN:
				this.search(num, courtPile.getMaxLevel() + 1, 1, 1);
				break;
			case c0:
				break;
			case cnull:
				dealPile.add(list.get(0));
				list.get(0).setChosen(true);
				break;
			default:
				break;
		}
	}

	/**
	 * 实现出牌功能，玩家打出一组牌，返回这组牌，并将它们从玩家手牌列表中移除；可以打出一组空牌
	 *
	 * @param courtPile 场上存在的一组牌
	 */
	public void dealCard(final CardPile courtPile, final GameServer gameServer, final int current, final int refuseNum)//为了回调
	{
		dealPile.clear();
		String s = "4";
		gameServer.server.send(current, s, new CallBack<Void>()
		{
			@Override
			public void error(Exception e)
			{
				System.out.println("\n[Server/Error] " + e.getMessage());
			}

			@Override
			public void success(Void aVoid)
			{
				gameServer.server.receive(current, new CallBack<String>()
				{
					@Override
					public void error(Exception e)
					{
						System.out.println("\n[Server/Error] " + e.getMessage());
					}
					@Override
					public void success(String message)
					{
						try
						{
							String selected[] = message.split(",");//不出牌则抛出异常
							//根据玩家传回的ID构建dealPile
							for (String selectedNum : selected)
							{
								int i = Integer.valueOf(selectedNum);
								for(Card j:list)
								{
									if (j.ID == i)
									{
										dealPile.add(j);
										break;
									}
								}
							}
							dealPile.judgeType();
							if (dealPile.largerThan(courtPile))//合法出牌
							{
								//从玩家手中移除
								for (Card i : dealPile.getList())
								{
									list.remove(i);
								}
								gameServer.updateStatus(0,current,dealPile,refuseNum);//通知所有人最新状态
							} else//非法出牌，重出
							{
								dealCard(courtPile,gameServer,current,refuseNum);
							}
						}
						catch(Exception e)//玩家不出牌
						{
							for (Card c : list)
							{
								c.setChosen(false);
							}
							gameServer.updateStatus(0,current,new CardPile(),refuseNum);
						}
					}
				});
			}
		});
	}
}