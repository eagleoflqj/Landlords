package com.liumeo.landlords;


import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public class ClientInfo {
        //client信息，辅助记录连接的服务器信息
        private final String name;
        private Socket socket;

        ClientInfo(String name, Socket socket) {
            this.name = name;
            this.socket = socket;
        }

        public String getName() {
            return name;
        }

        Socket getSocket() {
            return socket;
        }
    }

    private static final int EXECUTOR_COUNT = 3;
    //线程数量
    private final int count;
    //连接数上限
    private int port;//端口
    private String code;//房间号
    private ArrayList<ClientInfo> clients;//连接的客户端信息
    private ServerSocket server;//服务器ServerSocket
    private ExecutorService executorService;
    //线程池，因为需要和多个客户端连接

    public Server(int count) {
        //初始化，输入为连接数上限
        this.count = count;
        clients = new ArrayList<>();
        executorService = Executors.newFixedThreadPool(EXECUTOR_COUNT);
    }

    public int getPort() {
        return port;
    }

    public String getCode() {
        return code;
    }

    public ClientInfo clientAt(int idx) {
        //得到clients中第idx+1的客户端信息
        return clients.get(idx);
    }

    public <T extends Serializable> AsyncTask receive(int idx, CallBack<T> callBack) {
        //从id为idx的客户端接收信息
        AsyncTask<ClientInfo, Object, Void> asyncTask = new SReceiveTask<>(callBack);
        //生成服务器端的接收数据线程类
        asyncTask.executeOnExecutor(executorService, clients.get(idx));
        //加入线程池并执行从特定客户端接收消息
        return asyncTask;
    }

    public <T extends Serializable> AsyncTask receiveFromAll(CallBack<T> callBack) {
        //从所有客户端接收信息
        AsyncTask<ClientInfo, Object, Void> asyncTask = new SReceiveTask<>(callBack);
        //生成服务器端的接收数据线程类
        asyncTask.executeOnExecutor(executorService, (ClientInfo[]) clients.toArray());
        //加入线程池并执行从所有客户端接收消息
        return asyncTask;
    }

    public <T extends Serializable> AsyncTask sendToAll(T data, CallBack<Void> callBack) {
        //向所有客户端发送信息
        AsyncTask<ClientInfo, Object, Void> asyncTask = new SSendTask<>(data, callBack);
        //生成服务器端的发送数据线程类
        asyncTask.executeOnExecutor(executorService, (ClientInfo[]) clients.toArray());
        //加入线程池并执行向所有客户端发送消息

        return asyncTask;
    }

    public <T extends Serializable> AsyncTask send(int idx, T data, CallBack<Void> callBack) {
        //向特定客户端发送信息
        AsyncTask<ClientInfo, Object, Void> asyncTask = new SSendTask<>(data, callBack);

        //生成服务器端的发送数据线程类
        asyncTask.executeOnExecutor(executorService, clients.get(idx));

        //加入线程池并执行向特定客户端发送消息
        return asyncTask;
    }

    public AsyncTask waitForConnection(final WaitingCallback<ClientInfo> callback) {
        //等待客户端连接
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                int current_count = 0;
                Socket client;
                while (current_count < count) {
                    //当连接数小于上限时持续连接
                    try {
                        client = server.accept();
                        //接受客户端连接并得到Socket
                        client.setSoTimeout(MessageWrapper.TIMEOUT);
                        //设定超时时间
                        String name = (String) MessageWrapper.read(client.getInputStream());
                        //读取请求信息
                        MessageWrapper.write(client.getOutputStream(), name == null ? "F" : "S");
                        //返回请求信息，若没有成功得到请求信息name，返回F
                        if (name != null) {
                            ClientInfo info = new ClientInfo(name, client);
                            ++current_count;
                            //若成功，记录新的client信息
                            clients.add(info);
                            publishProgress(info);
                            //显示信息
                        }
                    } catch (IOException e) {
                        Log.e("Server/waitForClients", e.getMessage());
                    }
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                //开始时执行
                super.onPreExecute();
                callback.started();
            }

            @Override
            protected void onPostExecute(Object a) {
                //结束时执行
                super.onPostExecute(a);
                callback.finished();
            }

            @Override
            protected void onProgressUpdate(Object[] values) {
                //过程中执行
                super.onProgressUpdate(values);
                callback.clientJoined((ClientInfo) values[0]);

            }
        };
        asyncTask.executeOnExecutor(executorService);
        return asyncTask;
    }

    public void initialize(final CallBack<Void> callBack) {
        //初始化服务器端
        AsyncTask asyncTask = new AsyncTask<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object... params) {
                //初始化  IPHelper，得到房间号
                IPHelper.initialize();
                code = IPHelper.getCode(port);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                //表示成功
                super.onPostExecute(aVoid);
                callBack.success(null);
            }
        };
        server = null;
        for (int i = 0; i < IPHelper.PORT_SPAN; ++i) {
            try {
                //寻找空闲端口
                server = new ServerSocket(i + IPHelper.PORT_START);
                port = server.getLocalPort();
                break;
            } catch (IOException ignored) {
            }
        }
        if (server == null)
            callBack.error(new IOException("no free port"));
        else {
            port = server.getLocalPort();
            asyncTask.executeOnExecutor(executorService);
            //加入线程池中并执行
        }
    }

    public void release() {
        //释放所有连接
        for (ClientInfo client : clients)
            try {
                client.socket.close();
            } catch (IOException e) {
                Log.e("Server/release", e.getMessage());
            }
        clients.clear();
    }
}

class SSendTask<T extends Serializable> extends AsyncTask<Server.ClientInfo, Object, Void> {
    private T data;//传输的数据
    private CallBack<Void> callBack;

    SSendTask(T data, CallBack<Void> callBack) {
        this.data = data;
        this.callBack = callBack;
    }

    @Override
    protected Void doInBackground(Server.ClientInfo... clientInfos) {
        for (Server.ClientInfo clientInfo : clientInfos) {
            try {
                MessageWrapper.write(clientInfo.getSocket().getOutputStream(), data);
                //服务器端将date通过OutputStream传出
                publishProgress(0);
                //表示成功
            } catch (IOException e) {
                publishProgress(e);
                //输出异常
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        //用于根据数据传输过程是否发生异常选择回调函数
        if (values[0] instanceof Exception)
            callBack.error((IOException) values[0]);
        else
            callBack.success(null);
    }
}

class SReceiveTask<T extends Serializable> extends AsyncTask<Server.ClientInfo, Object, Void> {
    private CallBack<T> callBack;

    SReceiveTask(CallBack<T> callBack) {
        this.callBack = callBack;
    }

    @Override
    protected Void doInBackground(Server.ClientInfo... clients) {
        for (Server.ClientInfo client : clients) {
            try {
                publishProgress(MessageWrapper.read(client.getSocket().getInputStream()));
                //通过从InputStream读取数据并发布
            } catch (IOException e) {
                publishProgress(e);
                //输出异常
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Object... values) {

        //用于根据数据传输过程是否发生异常选择回调函数
        super.onProgressUpdate(values);
        if (values[0] instanceof IOException)
            callBack.error((IOException) values[0]);
        else
            callBack.success((T) values[0]);
    }
}
