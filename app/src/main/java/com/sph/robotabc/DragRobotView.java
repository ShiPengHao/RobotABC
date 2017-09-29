package com.sph.robotabc;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 可以设定图片和文字显示的可拖拽布局自定义组合控件，必须配合指定的布局使用。使用时应当在xml文件中指明该View相关属性。待优化。
 *  <li>禁用子View的触摸反应</li>
 *  <li>重新onTouchEvent方法，分发触摸事件，实现手势判断是拖拽，还是图片单击，或是图片双击</li>
 *  <li>图片单击事件为显示或隐藏文字部分做，并做360度旋转动画</li>
 *  <li>图片双击事件以监听器形式暴露出接口和设置方法</li>
 *  <li>文字内容点击事件同图片双击事件</li>
 *
 * @author ShiPengHao
 * @date 2017/9/25
 */
public class DragRobotView extends LinearLayout {
    /**
     * 图片部分
     */
    private ImageView iv_robot;
    /**
     * 文字部分
     */
    private LinearLayout ll_txt;
    /**
     * 文字部分关闭按钮
     */
    private ImageView iv_close;
    /**
     * 文字部分文字内容
     */
    private TextView tv_content;
    /**
     * Handler，主要用于延时显示或隐藏文字部分
     */
    private Handler mHandler;
    /**
     * 用于获取屏幕宽高
     */
    private DisplayMetrics mDisplayMetrics;
    /**
     * what：显示文字
     */
    private final int WHAT_SHOW = 101;
    /**
     * what：隐藏文字
     */
    private final int WHAT_DISMISS = 102;
    /**
     * what：图片做动画
     */
    private final int WHAT_ANIMATION = 103;
    /**
     * 图片旋转动画
     */
    private RotateAnimation mImgAnimation;

    //**************************************以下手势相关变量****************************//
    /**
     * 图片宽度
     */
    private int mImgWidth;
    /**
     * 图片高度
     */
    private int mImgHeight;
    /**
     * 手势按下时的时间
     */
    private long mDownTime = 0L;
    /**
     * 上次图片被点击的时间
     */
    private long mLastImgClickedTime;
    /**
     * 手势按下时的x
     */
    private int mDownX;
    /**
     * 手势按下时的y
     */
    private int mDownY;
    /**
     * 滑动标志
     */
    private boolean mHasNotMoved;
    /**
     * 图片被点击标志
     */
    private boolean mIsImageClicked;

    //**************************************以上手势相关变量****************************//

    // 禁用new的方式创建实例，必须使用xml布局文件
    @SuppressWarnings("unused")
    private DragRobotView(Context context) {
        super(context);
    }

    public DragRobotView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragRobotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        bindViewByAttrs(context, attrs);
        setListener();
        mDisplayMetrics = context.getResources().getDisplayMetrics();
    }

    /**
     * 初始化视图
     *
     * @param context context
     */
    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_drag_robot, this, true);
        iv_close = (ImageView) findViewById(R.id.iv_close);
        tv_content = (TextView) findViewById(R.id.tv_content);
        iv_robot = (ImageView) findViewById(R.id.iv_robot);
        ll_txt = (LinearLayout) findViewById(R.id.ll_txt);
        // 禁用子View触摸事件
        iv_robot.setEnabled(false);
//        tv_content.setEnabled(false);
        // 隐藏文字
        ll_txt.setVisibility(GONE);
    }

    /**
     * 根据布局文件设置的属性，设置视图
     *
     * @param context context
     * @param attrs   属性集
     */
    private void bindViewByAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DragRobotView);
        // 图片
        Drawable drawable = a.getDrawable(R.styleable.DragRobotView_img);
        iv_robot.setImageDrawable(drawable);
        // 文字
        String content = a.getString(R.styleable.DragRobotView_txt);
        tv_content.setText(content);
        // 回收
        a.recycle();
    }

    /**
     * 设置有关监听
     */
    private void setListener() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what != WHAT_ANIMATION) {
                    mHandler.removeCallbacksAndMessages(null);
                }
                switch (msg.what) {
                    case WHAT_SHOW:
                        showTxt();
                        break;
                    case WHAT_DISMISS:
                        dismissTxt();
                        break;
                    case WHAT_ANIMATION:
                        startImgAnimation(false);
                        break;
                }
                return true;
            }
        });
        // 关闭文字部分
        iv_close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessageDelayed(WHAT_DISMISS, 500);
                mHandler.sendEmptyMessageDelayed(WHAT_ANIMATION, 200);
            }
        });
        tv_content.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onGestureDoubleClick();
            }
        });
        // 获取图片宽高，以便于在onTouchEvent中判断手势的作用点是否在图片上，进行判断手势，比如是单击还是双击
        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (mImgWidth == 0) {
                    mImgWidth = iv_robot.getMeasuredWidth();
                    mImgHeight = iv_robot.getMeasuredHeight();
                }
            }
        });
    }

    public boolean onTouchEvent(MotionEvent event) {
        int[] location = new int[2];
        //获取到手指处的横坐标和纵坐标
        int x = (int) event.getX();
        int y = (int) event.getY();
        int rawX = (int) event.getRawX();
        int rawY = (int) event.getRawY();
        switch (event.getAction()) {
            // 新的手势开始
            case MotionEvent.ACTION_DOWN:
                Log.i("drag", "down");
                // 判断手势落点，是否在图片上
                iv_robot.getLocationInWindow(location);
                mIsImageClicked = location[0] < rawX && location[1] < rawY && location[0] + mImgWidth > rawX && location[1] + mImgHeight > rawY;
                // 重置相关变量
                mHasNotMoved = true;
                mDownTime = System.currentTimeMillis();
                mDownX = x;
                mDownY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                //计算相对于手势落点，手势移动的距离
                int offX = x - mDownX;
                int offY = y - mDownY;
                // 如果移动的距离大，则认为是在滑动，则使视图跟随手势进行拖拽
                if ((mHasNotMoved && Math.abs(offX) > 5 || Math.abs(offY) > 5) || !mIsImageClicked) {
                    mIsImageClicked = false;
                    mHasNotMoved = false;
                    onGestureDrag(offX, offY);
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.i("drag", "up");
                long now = System.currentTimeMillis();
                // 如果焦点一直在图片上，并且没有滑动，且本次手势历程较短，则认为触发ImageView点击事件
                if (mIsImageClicked && now - mDownTime < 200) {
                    onGestureClick(now);
                    // 更新单击事件时间
                    mLastImgClickedTime = now;
                } else {
                    // 本次手势不是单击事件，则重置时间
                    mLastImgClickedTime = -1L;
                }
                break;
        }
        // 接收图片和文字内容的事件
        return true;
    }

    /**
     * 判断手势为图片点击
     *
     * @param now 当前时间毫秒值
     */
    private void onGestureClick(long now) {
        // 如果离上次单击事件事件相差不大，则认为是双击事件，调用相应回调
        if (now - mLastImgClickedTime < 300 && mImgDoubleClickListener != null) {
            onGestureDoubleClick();
        } else {
            onGestureSingleClick();
        }
    }

    /**
     * 判断手势为图片单击事件，延迟显示或隐藏文字部分
     */
    private void onGestureSingleClick() {
        if (ll_txt.getVisibility() == VISIBLE) {
            mHandler.sendEmptyMessageDelayed(WHAT_DISMISS, 500);
        } else {
            mHandler.sendEmptyMessageDelayed(WHAT_SHOW, 500);
        }
        mHandler.sendEmptyMessageDelayed(WHAT_ANIMATION, 200);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
        }
        super.onDetachedFromWindow();
    }

    /**
     * 开始图片动画
     *
     * @param self 旋转动画中心是否是控件中心
     */
    public void startImgAnimation(boolean self) {
        if (mImgAnimation == null) {
            initImgAnimation();
        }
        if (self) {
            RotateAnimation animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(400);
            animation.setFillEnabled(true);
            animation.setFillBefore(true);
            iv_robot.startAnimation(animation);
        } else {
            // 图片做旋转动画
            iv_robot.startAnimation(mImgAnimation);
        }
    }

    /**
     * 初始化图片动画
     */
    private void initImgAnimation() {
//        mImgAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mImgAnimation = new RotateAnimation(0, 360);
        mImgAnimation.setDuration(400);
        mImgAnimation.setFillEnabled(true);
        mImgAnimation.setFillBefore(true);
    }

    /**
     * 判断手势为图片被双击
     */
    private void onGestureDoubleClick() {
        if (mImgAnimation != null) {
            mImgAnimation.cancel();
        }
        mHandler.removeMessages(WHAT_ANIMATION);
        // 立即隐藏文字部分
        mHandler.sendEmptyMessage(WHAT_DISMISS);
        // 调用图片双击监听逻辑
        if (null != mImgDoubleClickListener) {
            mImgDoubleClickListener.onImgDoubleClick();
        }
    }

    /**
     * 判断手势为拖拽，则执行拖拽
     *
     * @param offX x距离
     * @param offY y距离
     */
    private void onGestureDrag(int offX, int offY) {
        // 限制拖拽范围，不能超出屏幕
        int l = getLeft() + offX;
        int r = getRight() + offX;
        if (l < 0) {
            l = 0;
            r = getMeasuredWidth();
        } else if (r > mDisplayMetrics.widthPixels) {
            r = mDisplayMetrics.widthPixels;
            l = mDisplayMetrics.widthPixels - getMeasuredWidth();
        }
        int t = getTop() + offY;
        int b = getBottom() + offY;
        if (t < 0) {
            t = 0;
            b = getMeasuredHeight();
        } else if (b > mDisplayMetrics.heightPixels) {
            int offset = 0;
            b = mDisplayMetrics.heightPixels - offset;
            t = mDisplayMetrics.heightPixels - getMeasuredHeight() - offset;
        }
        //调用layout方法来重新放置它的位置
        layout(l, t, r, b);
    }

    /**
     * 显示文字部分
     */
    private void showTxt() {
        ll_txt.setVisibility(VISIBLE);
    }

    /**
     * 隐藏文字部分
     */
    private void dismissTxt() {
        ll_txt.setVisibility(GONE);
    }

    // 禁用点击事件
    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(null);
    }

    /**
     * 暴露一个图片双击的接口
     */
    public interface OnImgDoubleClickListener {
        /**
         * 图片被双击
         */
        void onImgDoubleClick();
    }

    /**
     * 图片双击监听
     */
    private OnImgDoubleClickListener mImgDoubleClickListener;

    /**
     * 设置图片双击监听
     *
     * @param onImgDoubleClickListener 图片双击监听
     */
    public void setOnImgDoubleClickListener(OnImgDoubleClickListener onImgDoubleClickListener) {
        mImgDoubleClickListener = onImgDoubleClickListener;
    }
}

