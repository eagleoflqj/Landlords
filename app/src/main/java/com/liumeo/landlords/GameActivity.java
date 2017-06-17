package com.liumeo.landlords;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

import java.util.LinkedList;

public class GameActivity extends Activity
{
	String name;
	String code;
	Server server;
	Client client;
	TextView logText;//提示信息
	TextView myName;//我的名字
	TextView leftName;//上家名字
	TextView leftNum;//上家剩余卡牌数
	TextView leftStatus;//上家状态
	TextView rightName;
	TextView rightNum;
	TextView rightStatus;
	Button btn_submit;//出牌按钮
	int bigCardWidth;//手牌宽高
	int bigCardHeight;
	int smallCardWidth;//场上牌宽高
	int smallCardHeight;
	int myIndex;//我在玩家中的编号
	int landlord;//地主编号
	String names[] = new String[3];//玩家名字
	LinkedList<String> selected = new LinkedList<>();//选中的牌
	LinkedList<Button> myCards = new LinkedList<>();//我的手牌
	LinkedList<Button> courtCards = new LinkedList<>();//场上的底牌
	LinkedList<Button> calls = new LinkedList<>();//叫地主按钮
	LinearLayout bottomLayout;//我的手牌所在布局
	LinearLayout centerLayout;//交互按钮所在布局
	LinearLayout topLayout;//场上卡牌所在布局

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		//获取屏幕宽度
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenWidth = dm.widthPixels;
		//设置卡牌宽高
		bigCardWidth = screenWidth / 12;
		bigCardHeight = bigCardWidth * 5 / 3;
		smallCardWidth = screenWidth / 15;
		smallCardHeight = smallCardWidth * 5 / 3;
		//绑定组件
		logText = (TextView) findViewById(R.id.logText);
		myName = (TextView) findViewById(R.id.myName);
		rightName = (TextView) findViewById(R.id.rightName);
		rightNum = (TextView) findViewById(R.id.rightNum);
		rightStatus = (TextView) findViewById(R.id.rightStatus);
		leftName = (TextView) findViewById(R.id.leftName);
		leftNum = (TextView) findViewById(R.id.leftNum);
		leftStatus = (TextView) findViewById(R.id.leftStatus);
		btn_submit = (Button) findViewById(R.id.btn_submit);
		//提交按钮不可见
		btn_submit.setVisibility(View.GONE);
		//绑定布局
		bottomLayout = (LinearLayout) findViewById(R.id.bottomLayout);
		centerLayout = (LinearLayout) findViewById(R.id.centerLayout);
		topLayout = (LinearLayout) findViewById(R.id.topLayout);
		//获取上一个Activity传入的信息
		Intent intent = getIntent();
		Boolean isServer = intent.getBooleanExtra("isServer", false);
		name = intent.getStringExtra("name");
		if (isServer)
		{
			//初始化服务器
			server = new Server(3);
			server.initialize(new CallBack<Void>()
			{
				@Override
				public void error(Exception e)
				{
					logText.append("\n[Error] " + e.getMessage());
				}

				@Override
				public void success(Void aVoid)
				{
					code = server.getCode();
					logText.setText("房间号：" + code);
					server.waitForConnection(new WaitingCallback<Server.ClientInfo>()
					{
						@Override
						public void started()
						{
							//初始化客户端
							client = new Client(name);
							IPHelper.initialize();
							client.keepingConnection(code, new CallBack<Void>()
							{
								@Override
								public void error(Exception e)
								{
									logText.append("\n[Client/Error] " + e.getMessage());
								}

								@Override
								public void success(Void aVoid)
								{
								}
							});
						}

						@Override
						public void clientJoined(Server.ClientInfo clientInfo)
						{
							logText.append("\n" + clientInfo.getName() + "加入");
						}

						@Override
						public void finished()
						{
							//游戏开始
							new GameServer(server).run();
							//server client分离
							mainLoop();
						}
					});
				}
			});
		} else
		{
			//初始化客户端
			code = intent.getStringExtra("code");
			client = new Client(name);
			IPHelper.initialize();
			client.keepingConnection(code, new CallBack<Void>()
			{
				//这里决定客户端连接过程成功或异常的动作
				@Override
				public void error(Exception e)
				{
					logText.append("\n[Client/Error] " + e.getMessage());
				}

				@Override
				public void success(Void aVoid)
				{
					logText.append("成功连接");
					mainLoop();
				}
			});
		}
	}

	//客户端消息循环
	private void mainLoop()
	{
		//接收服务器消息
		client.receiveFromServer(new CallBack<String>()
		{
			@Override
			public void error(Exception e)
			{
				logText.append("\n[Client/Error] " + e.getMessage());
			}

			@Override
			public void success(String message)
			{
				char Status = message.charAt(0);//更新状态
				switch (Status)
				{
					case '0'://开始信息
						initialize(message.substring(1));
						break;
					case '1'://叫地主
						callLandlord();
						break;
					case '2'://叫地主信息
						otherCallLandlord(message.substring(1));
						break;
					case '3'://地主产生
						showExtraCards(message.substring(1));
						break;
					case '4'://出牌
						myTurn();
						break;
					case '5'://状态更新
						update(message.substring(1));
						break;
					case '6'://游戏结束
						gameOver(message.substring(1));
						break;
					default:
						System.out.println(Status);
				}
			}
		});
	}

	private void initialize(String message)
	{
		logText.setText("等待叫地主");
		//拆分信息
		String information[] = message.split(",");
		//获得自己的编号
		myIndex = information[0].charAt(0) - '0';
		//获得所有人的名字
		for (int i = 0; i < 3; ++i)
		{
			names[i] = information[i + 1];
		}
		myName.setText(name);
		leftName.setText(names[(myIndex + 2) % 3]);
		rightName.setText(names[(myIndex + 1) % 3]);
		//清理上次叫地主失败的痕迹
		courtCards.clear();
		myCards.clear();
		bottomLayout.removeAllViews();
		topLayout.removeAllViews();
		leftStatus.setText("");
		rightStatus.setText("");
		//添加底牌
		for (int i = 0; i < 3; ++i)
		{
			addCourtCard(i, "");
		}
		//添加手牌
		for (int i = 0; i < 17; ++i)
		{
			addCard(information[i + 4]);
		}
		mainLoop();
	}

	private void callLandlord()
	{
		logText.setText("");
		//添加叫地主按钮
		for (int i = 0; i < 4; ++i)
		{
			Button btn = new Button(GameActivity.this);
			btn.setText(i == 0 ? "不叫" : (i + "分"));
			btn.setTag(i);
			btn.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					//删除叫地主按钮
					for (int i = 0; i < 4; ++i)
					{
						centerLayout.removeView(calls.get(i));
					}
					calls.clear();
					client.sendToServer(v.getTag().toString(), new CallBack<Void>()
					{
						@Override
						public void error(Exception e)
						{
							logText.append("\n[Client/Error] " + e.getMessage());
						}

						@Override
						public void success(Void aVoid)
						{
							mainLoop();
						}
					});
				}
			});
			calls.add(btn);
			centerLayout.addView(btn);
		}
	}

	private void otherCallLandlord(String message)
	{
		int index = message.charAt(0) - '0';
		int num = message.charAt(1) - '0';
		(index == myIndex ? logText : (index == (myIndex + 1) % 3 ? rightStatus : leftStatus)).setText(num == 0 ? "不叫" : ("叫" + num + "分"));
		mainLoop();
	}

	private void showExtraCards(String message)
	{
		String information[] = message.split(",");
		landlord = information[0].charAt(0) - '0';
		logText.setText(names[landlord] + "成为地主");
		for (int i = 0; i < 3; ++i)
		{
			setImage(courtCards.get(i), information[i + 1]);
		}
		if (landlord == myIndex)//我是地主
		{
			//为保证顺序清理旧牌
			myCards.clear();
			bottomLayout.removeAllViews();
			//刷新手牌
			for (int i = 0; i < 20; ++i)
			{
				addCard(information[i + 4]);
			}
			//添加角色
			myName.setText("地主 " + name);
			leftName.setText("农民 " + names[(myIndex + 2) % 3]);
			rightName.setText("农民 " + names[(myIndex + 1) % 3]);
			//初始化剩余牌数
			leftNum.setText("17");
			rightNum.setText("17");
		} else if (landlord == (myIndex + 2) % 3)//上家地主
		{
			myName.setText("农民 " + name);
			leftName.setText("地主 " + names[(myIndex + 2) % 3]);
			rightName.setText("农民 " + names[(myIndex + 1) % 3]);
			leftNum.setText("20");
			rightNum.setText("17");
		} else//下家地主
		{
			myName.setText("农民 " + name);
			leftName.setText("农民 " + names[(myIndex + 2) % 3]);
			rightName.setText("地主 " + names[(myIndex + 1) % 3]);
			leftNum.setText("17");
			rightNum.setText("20");
		}
		mainLoop();
	}

	private void myTurn()
	{
		//显示提交按钮
		btn_submit.setVisibility(View.VISIBLE);
		logText.setText("");
		//解锁卡牌
		for (Button i : myCards)
		{
			i.setEnabled(true);
		}
	}

	private void update(String message)
	{
		String information[] = message.split(",");
		int index = information[0].charAt(0) - '0';
		if (information.length > 1)//有人出牌
		{
			topLayout.removeAllViews();//清空场上的牌
			//更新场上的牌
			for (int i = 1; i < information.length; ++i)
			{
				addCourtCard(i - 1, information[i]);
			}
			if (index == myIndex)//是我出牌
			{
				//删除我出的牌
				for (int i = 1; i < information.length; ++i)
				{
					for (int j = myCards.size() - 1; j >= 0; --j)
					{
						if (myCards.get(j).getTag().equals(information[i]))
						{
							bottomLayout.removeView(myCards.get(j));
							myCards.remove(j);
						}
					}
				}
				//更新状态信息
				logText.setText("出牌");
				leftStatus.setText("");
				rightStatus.setText("");
			} else if (index == (myIndex + 1) % 3)//下家出牌
			{
				Integer rest = Integer.valueOf(rightNum.getText().toString()) - (information.length - 1);//计算剩余牌数
				rightNum.setText(rest.toString());
				rightStatus.setText("出牌");
				logText.setText("");
				leftStatus.setText("");
			} else//上家出牌
			{
				Integer rest = Integer.valueOf(leftNum.getText().toString()) - (information.length - 1);
				leftNum.setText(rest.toString());
				leftStatus.setText("出牌");
				logText.setText("");
				rightStatus.setText("");
			}
		} else//不出
		{
			(index == myIndex ? logText : (index == (myIndex + 1) % 3 ? rightStatus : leftStatus)).setText("不出");
		}
		mainLoop();
	}

	private void gameOver(String message)
	{
		int information = message.charAt(0) - '0';
		if (information == 0)
		{
			logText.setText("地主" + names[landlord] + "获胜！");
		} else
		{
			logText.setText("农民" + names[(landlord + 1) % 3] + "、" + names[(landlord + 2) % 3] + "获胜！");
		}
	}

	private void addCard(String ID)
	{
		Button btn = new Button(this);
		setImage(btn, ID);//加载扑克图像
		btn.setTag(ID);//设置扑克ID
		btn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
				Object tag = v.getTag();
				if (selected.contains(tag))//被选中了，恢复
				{
					selected.remove(tag);
					layoutParams.bottomMargin -= bigCardWidth / 5;
					layoutParams.topMargin += bigCardWidth / 5;
				} else//没被选中，上移
				{
					selected.add((String) tag);
					layoutParams.bottomMargin += bigCardWidth / 5;
					layoutParams.topMargin -= bigCardWidth / 5;
				}
				v.requestLayout();//更新位置
			}
		});
		//设置位置参数
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(bigCardWidth, bigCardHeight);
		params.leftMargin = -bigCardWidth / 2;//左重叠
		params.topMargin = bigCardWidth / 2;
		params.bottomMargin = bigCardWidth / 2;
		bottomLayout.addView(btn, params);
		myCards.add(btn);
		btn.setEnabled(false);
	}

	private void addCourtCard(int i, String ID)
	{
		Button btn = new Button(this);
		setImage(btn, ID);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(smallCardWidth, smallCardHeight);
		//除第一个外需要缩进
		if (i > 0)
		{
			params.leftMargin = -smallCardWidth / 2;
		}
		params.topMargin = smallCardWidth / 2;
		params.bottomMargin = smallCardWidth / 2;
		topLayout.addView(btn, params);
		courtCards.add(btn);
		btn.setEnabled(false);
	}

	public void submit(View view)
	{
		//提交按钮消失
		btn_submit.setVisibility(View.GONE);
		logText.setText("");
		for (Button i : myCards)
		{
			i.setEnabled(false);//卡牌锁定
			//选中的牌复原
			if (selected.contains(i.getTag()))
			{
				LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) i.getLayoutParams();
				layoutParams.bottomMargin -= bigCardWidth / 5;
				layoutParams.topMargin += bigCardWidth / 5;
				i.requestLayout();
			}
		}
		//构造信息串
		StringBuilder sb = new StringBuilder();
		for (String i : selected)
		{
			sb.append(",").append(i);
		}
		//清空选定的信息
		selected.clear();
		String s = sb.toString();
		if (s.length() > 0)
		{
			s = s.substring(1);//删除第一个,
		}
		client.sendToServer(s, new CallBack<Void>()
		{
			@Override
			public void error(Exception e)
			{
				logText.append("\n[Client/Error] " + e.getMessage());
			}

			@Override
			public void success(Void aVoid)
			{
				mainLoop();
			}
		});
	}

	private void setImage(Button btn, String ID)
	{
		if (ID == "")//卡背
		{
			btn.setBackgroundResource(getResources().getIdentifier("cardback", "drawable", "com.liumeo.landlords"));
		} else
		{
			btn.setBackgroundResource(getResources().getIdentifier("p" + ID, "drawable", "com.liumeo.landlords"));
		}
	}
}
