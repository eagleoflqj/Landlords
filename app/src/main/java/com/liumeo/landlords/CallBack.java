package com.liumeo.landlords;

public interface CallBack<T> {
    void error(Exception e);
    //异常接口

    void success(T t);
    //成功执行接口
}
