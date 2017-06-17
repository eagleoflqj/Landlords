package com.liumeo.landlords;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GameServer extends Thread
{
	int landlord = -1;//地主编号
	final List<Card> list = new ArrayList<>();//一组扑克
	CardPile courtPile = new CardPile();//场上现存的牌，可以为空
	Server server;
	Player[] players;

	GameServer(Server server)
	{
		this.server = server;
	}

	@Override
	public void run()//线程起点
	{
		//生成一副扑克
		for (int i = 0; i < 13; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				list.add(new Card(j,  i));
			}
		}
		list.add(new Card(0, 13));//小王
		list.add(new Card(3, 26));//大王

		//生成玩家列表
		players = new Player[3];
		for (int i = 0; i < 3; ++i)
		{
			players[i] = new Player(server.clientAt(i).getName());
		}
		sendStartMessage(0, getIndexList());//发送开始信息
	}

	private void sendStartMessage(final int i, final int[][] indexList)
	{
		StringBuilder sb = new StringBuilder("0" + i);//需要告诉玩家自己的编号
		for (int j = 0; j < 3; ++j)
		{
			sb.append(",").append(players[j].getName());//所有玩家的姓名
		}
		for (int k : indexList[i])
		{
			sb.append(",").append(list.get(k).ID);//玩家卡牌
		}
		server.send(i, sb.toString(), new CallBack<Void>()
		{
			//这里决定服务器端发送信息过程成功或异常的动作
			@Override
			public void error(Exception e)
			{
				System.out.println("\n[Server/Error] " + e.getMessage());
			}

			@Override
			public void success(Void aVoid)
			{
				if (i < 2)
				{
					sendStartMessage(i + 1, indexList);
				} else
				{
					callLandlord(indexList, 0, 0, -1);//抢地主
				}
			}
		});
	}

	private void callLandlord(final int[][] indexList, final int i, final int max, final int index)
	{
		server.send(i, "1", new CallBack<Void>()
		{
			//这里决定服务器端发送信息过程成功或异常的动作
			@Override
			public void error(Exception e)
			{
				System.out.println("\n[Server/Error] " + e.getMessage());
			}

			@Override
			public void success(Void aVoid)
			{
				server.receive(i, new CallBack<String>()
				{
					@Override
					public void error(Exception e)
					{
						System.out.println("\n[Server/Error] " + e.getMessage());
					}

					@Override
					public void success(String message)
					{
						int num = Integer.valueOf(message);
						int newMax = max;
						int newIndex = index;
						if (num > max)
						{
							newMax = num;
							newIndex = i;
						}
						tellAllCallLandlord(0, indexList, i, num, newMax, newIndex);//通知所有人该玩家叫了几分
					}
				});
			}
		});
	}

	private void tellAllCallLandlord(final int j, final int[][] indexList, final int i, final int num, final int max, final int index)
	{
		String s = "2" + i + "" + num;//玩家和他叫的分
		server.send(j, s, new CallBack<Void>()
		{
			@Override
			public void error(Exception e)
			{
				System.out.println("\n[Server/Error] " + e.getMessage());
			}

			@Override
			public void success(Void aVoid)
			{
				if (j < 2)
				{
					tellAllCallLandlord(j + 1, indexList, i, num, max, index);
				} else
				{
					if (i < 2)//让下一个人叫地主
					{
						callLandlord(indexList, i + 1, max, index);
					} else if (index == -1)//没人叫地主
					{
						sendStartMessage(0, getIndexList());//重来
					} else//有人叫地主
					{
						landlord = index;
						//发牌
						for (int j = 0; j < 3; j++)
						{
							for (int k = 0; k < 17; k++)
							{
								players[j].addCard(list.get(indexList[j][k]));
							}
						}
						//地主加底牌
						players[landlord].addCard(list.get(indexList[3][0]));
						players[landlord].addCard(list.get(indexList[3][1]));
						players[landlord].addCard(list.get(indexList[3][2]));
						tellAllLandlordSet(0, indexList, landlord);//通知所有人地主产生
					}
				}
			}
		});
	}

	private void tellAllLandlordSet(final int i, final int[][] indexList, final int landlord)
	{
		StringBuilder sb = new StringBuilder("3" + landlord);//地主编号
		//亮底牌
		for (int j = 0; j < 3; ++j)
		{
			sb.append(",").append(list.get(indexList[3][j]).ID);
		}
		//更新地主手牌
		if (i == landlord)
		{
			for(int j=0;j<20;++j)
			{
				sb.append(",").append(players[landlord].list.get(j).ID);
			}
		}
		server.send(i, sb.toString(), new CallBack<Void>()
		{
			@Override
			public void error(Exception e)
			{
				System.out.println("\n[Server/Error] " + e.getMessage());
			}

			@Override
			public void success(Void aVoid)
			{
				if (i < 2)
				{
					tellAllLandlordSet(i + 1, indexList, landlord);
				} else
				{
					mainLoop(landlord, 0);//进入游戏主循环
				}
			}
		});
	}

	public void mainLoop(int current, int refuseNum)
	{
		//选择不出牌的玩家数目，为2时清空场上的牌
		if (refuseNum == 2)
		{
			courtPile = new CardPile();
			refuseNum = 0;
		}
		players[current].dealCard(courtPile, this, current, refuseNum);//玩家出牌
	}

	public void updateStatus(final int i, final int current, final CardPile newPile, final int refuseNum)
	{
		StringBuilder sb = new StringBuilder("5");
		sb.append(current).append(newPile);//当前玩家和他出的牌
		server.send(i, sb.toString(), new CallBack<Void>()
		{
			@Override
			public void error(Exception e)
			{
				System.out.println("\n[Server/Error] " + e.getMessage());
			}

			@Override
			public void success(Void aVoid)
			{
				if (i < 2)
				{
					updateStatus(i + 1, current, newPile, refuseNum);
				} else
				{
					checkResult(current, newPile, refuseNum);//检查游戏结果
				}
			}
		});
	}
	public void checkResult(int current, CardPile newPile, int refuseNum)
	{
		if (players[current].size() == 0)//出光了牌
		{
			if (current == landlord)//地主获胜
			{
				gameOver(0,0);
			} else//农民获胜
			{
				gameOver(0,1);
			}
			return;
		}
		if (newPile.getType() != CardPile.CardType.cnull)//压住场上的牌
		{
			courtPile = newPile;
			refuseNum = 0;
		} else//不出
		{
			refuseNum++;
		}
		mainLoop((current + 1) % 3, refuseNum);//下一人
	}
	private void gameOver(final int i, final int winner)
	{
		String message="6"+winner;//赢者
		server.send(i, message, new CallBack<Void>()
		{
			@Override
			public void error(Exception e)
			{
				System.out.println("\n[Server/Error] " + e.getMessage());
			}

			@Override
			public void success(Void aVoid)
			{
				if (i < 2)
				{
					gameOver(i + 1, winner);
				}
				//终止
			}
		});
	}
	//洗牌
	public int[][] getIndexList()
	{
		int no = 54;
		int[] indexList = new int[no];
		for (int i = 0; i < no; i++)
		{
			indexList[i] = i;
		}
		Random random = new Random();
		for (int i = 0; i < no; i++)
		{
			int p = random.nextInt(no);
			int tmp = indexList[i];
			indexList[i] = indexList[p];
			indexList[p] = tmp;
		}
		int[][] indexList0 = new int[4][17];
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 17; j++)
			{
				indexList0[i][j] = indexList[17 * i + j];
			}
			Arrays.sort(indexList0[i]);
		}
		indexList0[3][0] = indexList[51];
		indexList0[3][1] = indexList[52];
		indexList0[3][2] = indexList[53];
		return indexList0;
	}
}
