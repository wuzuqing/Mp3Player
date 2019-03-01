package com.wuzuqing.android.mp3player.audioplayer;

/**
 * 作者：士元
 * 时间：2019/3/1 11:36
 * 邮箱：wuzuqing@linghit.com
 * 说明：
 */
public enum AudioError {
    UN_SUPER_TYPE("不支持的文件格式"),IO_ERROE("文件操作失败"),NET_ERROE("网络异常");
    private String msg;

    AudioError(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
