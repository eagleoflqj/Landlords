package com.liumeo.landlords;


public interface WaitingCallback<T> {
    void started();
    //开始时执行的函数

    void clientJoined(T t);
    //客户端成功加入时执行的函数

    void finished();
    //结束时执行的函数
}
