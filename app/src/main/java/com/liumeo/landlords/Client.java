package com.liumeo.landlords;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
    private Socket client;
    //使用client为客户端对应的Socket
    private String name;

    public Client(String name) {
        this.name = name;
    }


    public AsyncTask keepingConnection(String code, final CallBack<Void> callBack) {
        final InetSocketAddress address = IPHelper.getAddressFromCode(code);
        //通过code得到Socket地址
        AsyncTask asyncTask = new AsyncTask() {
          //使用内部异步任务类实现连接功能
            private Exception err;
            //用于记录异常类型

            @Override
            protected Object doInBackground(Object[] params) {
                String result = null;
                try {
                    client = new Socket();
                    client.setSoTimeout(MessageWrapper.TIMEOUT);
                    client.connect(address, MessageWrapper.TIMEOUT);
                    //尝试连接目标地址，上限时间为MessageWrapper.TIMEOUT
                    MessageWrapper.write(client.getOutputStream(), name);
                    //向服务器端写入name信息以确认连接
                    result = (String) MessageWrapper.read(client.getInputStream());
                    //得到返回的确认信息
                } catch (IOException e) {
                    client = null;
                    err = e;
                    //IO异常，重置client
                }
                if (result == null || !result.equals("S")) {
                    err = new IOException("Connect refuse");
                    //连接异常，重置client
                    client = null;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if (client == null)
                    callBack.error(err);
                else
                    callBack.success(null);
                    //根据client是否为空决定回调函数
            }

        };
        asyncTask.execute(0);
        return asyncTask;
    }

    public Object recevieFromServerSync()
    {
        Object o=null;
        try
        {
            o=MessageWrapper.read(client.getInputStream());
        }
        catch(Exception e)
        {
        }
        return o;
    }
    public <T extends Serializable> AsyncTask receiveFromServer(CallBack<T> callBack) {
        AsyncTask<Socket, Object, Void> asyncTask = new CReceiveTask<>(callBack);
        //生成客户端的接受任务线程类
        asyncTask.execute(client);//执行线程
        return asyncTask;
    }

    public <T extends Serializable> AsyncTask sendToServer(T data, CallBack<Void> callBack) {
        AsyncTask<Socket, Object, Void> asyncTask = new CSendTask<>(data, callBack);
        //生成客户端的发送任务线程类
        asyncTask.execute(client);
        return asyncTask;
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            Log.e("Client/close", e.getMessage());
            //记录关闭客户端的异常信息
        }
    }
}

class CSendTask<T extends Serializable> extends AsyncTask<Socket, Object, Void> {
    private T data;//传输的数据
    private CallBack<Void> callBack;

    CSendTask(T data, CallBack<Void> callBack) {
        this.data = data;
        this.callBack = callBack;
    }

    @Override
    protected Void doInBackground(Socket... clients) {
        for (Socket client : clients) {
            try {
                MessageWrapper.write(client.getOutputStream(), data);
                //客户端将date通过OutputStream传出
                publishProgress(0);
                //表示成功
            } catch (IOException e) {
                publishProgress(e);
                //输出
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
      //用于根据数据传输过程是否发生异常选择回调函数
        super.onProgressUpdate(values);
        if (values[0] instanceof Exception)
            callBack.error((IOException) values[0]);
        else
            callBack.success(null);
    }
}


class CReceiveTask<T extends Serializable> extends AsyncTask<Socket, Object, Void> {
    private CallBack<T> callBack;

    CReceiveTask(CallBack<T> callBack) {
        this.callBack = callBack;
    }

    @Override
    protected Void doInBackground(Socket... clients) {
        for (Socket client : clients) {
            try {
                publishProgress(MessageWrapper.read(client.getInputStream()));
                //通过从InputStream读取数据并发布
            } catch (IOException e) {
                publishProgress(e);
                //输出
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
