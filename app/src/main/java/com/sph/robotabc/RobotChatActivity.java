package com.sph.robotabc;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * 机器人聊天界面，用户输入关键字，机器人应答
 *  
 *
 * @author ShiPengHao
 * @date 2017/9/26
 */

public class RobotChatActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView listView;
    private EditText et_key;
    private TextView tv_send;
    /**
     * 聊天消息数据bean集合
     */
    private ArrayList<RobotMessageEntity> mEntities = new ArrayList<>();
    /**
     * 聊天消息列表数据适配器
     */
    private RobotMsgAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_chat);
        initView();
        setListener();
        robotSayHi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        listView = (ListView) findViewById(R.id.lv);
        tv_send = (TextView) findViewById(R.id.tv_send);
        et_key = (EditText) findViewById(R.id.et_key);
    }

    private void setListener() {
        tv_send.setOnClickListener(this);
        mAdapter = new RobotMsgAdapter(mEntities);
        listView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_send:
                checkInput();
                break;
        }
    }

    /**
     * 检查用户输入，无误则发起网络请求
     */
    private void checkInput() {
        String key = et_key.getText().toString().trim();
        if (TextUtils.isEmpty(key)) {
            Toast.makeText(this, "您到底想问什么？", Toast.LENGTH_SHORT).show();
        } else {
            et_key.setText("");
            requestAsk(key);
        }
    }

    /**
     * 机器人首先发出欢迎信息
     */
    private void robotSayHi() {
        RobotMessageEntity entity = new RobotMessageEntity();
        entity.RT_LIST = new RobotMessageEntity.RobotMessageType();
        entity.RT_LIST.RES_TYPE = RobotMessageEntity.RobotMessageType.TYPE_LIST;
        entity.RT_LIST.RES_LIST = new ArrayList<>();
        RobotMessageEntity.RobotMessage message = new RobotMessageEntity.RobotMessage();
        message.ANS_TITLE = "hi，我是机器人小莉";
        message.ANS_CONTENT = "想和我聊聊么？";
        message.ANS_URL = "hi";
        entity.RT_LIST.RES_LIST.add(message);
        message = new RobotMessageEntity.RobotMessage();
        message.ANS_TITLE = "hi";
        entity.RT_LIST.RES_LIST.add(message);
        checkMsgToDisplay(entity);
    }

    /**
     * 根据用户输入，生成一条类型为发送的消息
     *
     * @param keyWords 关键字
     */
    private void generateAskData(String keyWords) {
        RobotMessageEntity entity = new RobotMessageEntity();
        entity.type = RobotMessageEntity.TYPE_ASK;
        entity.RT_LIST = new RobotMessageEntity.RobotMessageType();
        entity.RT_LIST.RES_TYPE = RobotMessageEntity.RobotMessageType.TYPE_LIST;
        entity.RT_LIST.RES_LIST = new ArrayList<>();
        RobotMessageEntity.RobotMessage message = new RobotMessageEntity.RobotMessage(keyWords);
        entity.RT_LIST.RES_LIST.add(message);
        checkMsgToDisplay(entity);
    }

    /**
     * 发起网络请求
     *
     * @param keyWords 关键字
     */
    private void requestAsk(final String keyWords) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                generateAskData(keyWords);
            }

            @Override
            protected Void doInBackground(Void... params) {
                SystemClock.sleep(2000);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                robotSayHi();
            }
        }.execute();
    }

    /**
     * 检查消息数据，无误则添加到消息列表
     *
     * @param entity 消息
     */
    private void checkMsgToDisplay(RobotMessageEntity entity) {
        if (null == entity || null == entity.RT_LIST) {
            return;
        }
        switch (entity.RT_LIST.RES_TYPE) {
            case RobotMessageEntity.RobotMessageType.TYPE_ITEM:
                if (null == entity.RT_LIST.RES_DETAIL) {
                    return;
                }
                // 将解析后的数据，类型为内容的，转化为列表，简化消息列表adapter和布局
                // adapter在绑定数据时，只判断是发送的消息还是接受的消息，把消息内容全当列表处理
                entity.RT_LIST.RES_TYPE = RobotMessageEntity.RobotMessageType.TYPE_LIST;
                entity.RT_LIST.RES_LIST = new ArrayList<>();
                entity.RT_LIST.RES_LIST.add(new RobotMessageEntity.RobotMessage(entity.RT_LIST.RES_DETAIL));
                break;
            case RobotMessageEntity.RobotMessageType.TYPE_LIST:
                if (null == entity.RT_LIST.RES_LIST || 0 == entity.RT_LIST.RES_LIST.size()) {
                    return;
                }
                break;
        }
        mEntities.add(entity);
        mAdapter.notifyDataSetChanged();
        listView.setSelection(mEntities.size() - 1);
    }

    /**
     * 消息列表适配器使用的ViewHolder
     */
    private class MessageEntityHolder {
        private View view;
        private TextView tv;
        private ListView lv;

        /**
         * 构造holder
         *
         * @param viewType holder类型
         */
        private MessageEntityHolder(int viewType) {
            if (viewType == RobotMessageEntity.TYPE_ANSWER) {
                view = View.inflate(RobotChatActivity.this, R.layout.item_robot_msg_in, null);
                lv = (ListView) view.findViewById(R.id.lv);
                view.setTag(this);
            } else if (viewType == RobotMessageEntity.TYPE_ASK) {
                view = View.inflate(RobotChatActivity.this, R.layout.item_robot_msg_out, null);
                tv = (TextView) view.findViewById(R.id.tv);
                view.setTag(this);
            }
        }

        /**
         * 获取列表条目视图
         *
         * @return 列表条目视图
         */
        private View getView() {
            return view;
        }

        /**
         * 绑定数据到视图
         *
         * @param entity 消息
         */
        private void bindData(final RobotMessageEntity entity) {
            if (entity.type == RobotMessageEntity.TYPE_ANSWER) {
                lv.setAdapter(new ArrayAdapter<>(RobotChatActivity.this, android.R.layout.simple_list_item_1, entity.RT_LIST.RES_LIST));
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        RobotMessageEntity.RobotMessage message = entity.RT_LIST.RES_LIST.get(position);
                        if (!TextUtils.isEmpty(message.ANS_URL)) {
                            startActivity(new Intent(RobotChatActivity.this, WebViewActivity.class));
                        }
                    }
                });
            } else {
                tv.setText(entity.RT_LIST.RES_LIST.get(0).ANS_TITLE);
            }
        }
    }

    /**
     * 聊天机器人消息数据适配器
     */
    private class RobotMsgAdapter extends BaseAdapter {
        private ArrayList<RobotMessageEntity> mEntities;

        private RobotMsgAdapter(ArrayList<RobotMessageEntity> entities) {
            mEntities = entities;
        }

        @Override
        public int getCount() {
            return mEntities.size();
        }

        @Override
        public RobotMessageEntity getItem(int position) {
            return mEntities.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).type;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MessageEntityHolder messageEntityHolder;
            if (convertView == null) {
                messageEntityHolder = new MessageEntityHolder(getItemViewType(position));
                convertView = messageEntityHolder.getView();
            } else {
                messageEntityHolder = (MessageEntityHolder) convertView.getTag();
            }
            messageEntityHolder.bindData(mEntities.get(position));
            return convertView;
        }
    }
}
