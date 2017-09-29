package com.sph.robotabc;

import android.text.TextUtils;

import java.util.ArrayList;

/**
 * 机器人聊天消息数据bean
 *  
 *
 * @author ShiPengHao
 * @date 2017/9/26
 */

class RobotMessageEntity {
    /**
     * 数据集
     */
    RobotMessageType RT_LIST;

    /**
     * 机器人应答的消息
     */
    final static int TYPE_ANSWER = 0;
    /**
     * 用户发送的消息
     */
    final static int TYPE_ASK = 1;

    /**
     * 消息类型，默认为机器人应答消息
     */
    int type = TYPE_ANSWER;

    /**
     * 聊天数据类型有关封装
     */
    static class RobotMessageType{
        /**
         * 01：知识列表
         */
        static final String TYPE_LIST = "01";
        /**
         * 02：知识内容
         */
        static final String TYPE_ITEM = "02";
        /**
         * 匹配结果类型
             01：知识列表
             02：知识内容
         */
        String RES_TYPE = TYPE_LIST;
        /**
         * 知识列表
         */
        ArrayList<RobotMessage> RES_LIST;
        /**
         * 知识内容
         */
        String RES_DETAIL;
    }

    /**
     * 聊天数据
     */
    static class RobotMessage {

        String ANS_TITLE;
        String ANS_CONTENT;
        String ANS_URL;

        RobotMessage(){}

        RobotMessage(String title){
            ANS_TITLE = title;
        }


        public String toString() {
            String text;
            if (TextUtils.isEmpty(ANS_TITLE)) {
                text = "您好，我是电力问答机器人，有什么可以帮您的么？";
            } else {
                text = ANS_TITLE;
            }
            if (!TextUtils.isEmpty(ANS_CONTENT)) {
                text = text + "\n\t•\t" + ANS_CONTENT;
            }
//            if (!TextUtils.isEmpty(ANS_URL)) {
//                text = text + "\t(可浏览网页)";
//            }
            return text;
        }
    }

}
