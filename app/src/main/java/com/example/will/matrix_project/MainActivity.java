package com.example.will.matrix_project;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * 矩阵的位移和缩放
 * <p>
 * bug:
 * 1,旋转没有实现.
 * 2,移动的时候第三根手指的运动轨迹并未屏蔽.
 */
public class MainActivity extends AppCompatActivity {

    private ImageView imageView;

    //图片的放大缩小
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageview);
        Chinese_and_English_translation();

    }
    //把图片移动到屏幕的中间

    public static float ScreenW;//屏幕的宽
    public static float ScreenH;//屏幕的高

    private void Chinese_and_English_translation() {
        //获取屏幕高宽度
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        ScreenW = displayMetrics.widthPixels;
        ScreenH = displayMetrics.heightPixels;
        //把图片转变为bitmap 然后获取图片的高宽度
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        /**
         * float withs=bitmap.getWidth()
         * float withs=bitmap.getHeight()
         * */
        //1这里是先声明一个矩阵来容纳图片的矩阵
        Matrix MatrixImags = imageView.getImageMatrix();
        //2把矩阵移动 （ postTranslate）         这里就用到了我们获取的屏幕高宽
        //屏幕高，宽度 /2- 图片高，宽/2
        MatrixImags.postTranslate(((ScreenW / 2) - (bitmap.getWidth() / 2)), ((ScreenH / 2) - (bitmap.getHeight() / 2)));
        //在把变化后的矩阵给设置进去
        imageView.setImageMatrix(MatrixImags);
        //调用图片触摸事件
        imageView.setOnTouchListener(new Touch_picture());

    }

    /*得到 点（两）与 点之间的直线距离*/
    public float getDistance(MotionEvent event) {
        float disX = event.getX(1) - event.getX(0);
        float disy = event.getY(1) - event.getY(0);
        double sqrt = Math.sqrt((disX * disX) + (disy * disy));
        return (float) sqrt;
    }

    /*得到点（两）与 点的中点坐标*/
    public PointF getDistanceMid(MotionEvent event) {
        float midX = (event.getX(1) + event.getX(0)) / 2;
        float midy = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(midX, midy);
    }

    //声名图片放大缩小需要的六大步骤：

    //1声明：当前矩阵；
    private Matrix startMx = new Matrix();
    //2声明: 变化后矩阵：
    private Matrix changegMx = new Matrix();
    //3声明：双指按下时的点：
    private PointF startPF = new PointF();
    //4声明 ：双指按下时的 两指间的中点距离，也是缩放地1中心
    private PointF miPF = new PointF();
    //5声明：双指按下（开始）的的距离
    private float startDistance;
    //6声明 用来记录图片（矩阵）变化的标志量  来判断实现调用什么方法！
    /**
     * 当前模式
     * 0 初始状态
     * 1 移动状态
     * 2 缩放状态
     **/
    private int sign = 0;

    /**
     * 一个重要的思想   就是  移动是移动  缩放是缩放  区分开来
     */

    //声明一个内部类 实现 触摸事件方法：
    /*
     * 实现图片放大缩小的具体操作：如下
     * 注意点：1拦截 false 改为 true 才可以拦截到触摸屏幕
     *         2做switch判断 其实功能与用if判断一样可以实现
     * */
    class Touch_picture implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //判断 获取的 触摸动作 与 触摸点动作（标志）是 那个动作或者动作（标志）：
            /*
             *动作判断事件分别为：
             * 1： MotionEvent.ACTION_DOWN  触摸动作（单指）down
             * 2： MotionEvent.ACTION_MOVE  触摸动作（单指）move
             * 3： MotionEvent.ACTION_UP    触摸（单指）up
             * 4： MotionEvent.ACTION_POINTER_UP    触摸点的动作（标志）（双指）up
             * 5： MotionEvent.ACTION_POINTER_DOWN    触摸点的动作（标志）（双指）down
             * */
            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    /*
                     * 标志量给1 将图片的矩阵赋给startMx  把开始的点的x，y值赋给startPF
                     * */
                    sign = 1;
                    startMx.set(imageView.getImageMatrix());
                    startPF.set(event.getX(), event.getY());

                    break;
                case MotionEvent.ACTION_UP:
                    //标志量给0
                    sign = 0;

                case MotionEvent.ACTION_POINTER_UP:
                    //标志量给0
                    sign = 0;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    //标志量给2  把开始的矩阵设置给变化后的矩阵  changegMx.set(startMx);
                    // 获取点距给startDistance
                    //获取开始的点与点 中点距给 miPF
                    sign = 2;
                    changegMx.set(startMx);
                    startDistance = getDistance(event);
                    miPF = getDistanceMid(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    //这里才是重点：
                    //判断Sion 应该做什么
                    if (sign == 1) {
                        //移动
                        //把开始的矩阵设置给变化后的矩阵
                        //获取当前 变化的x ，y 值设置到移动矩阵的方法中 changegMx.postTranslate(offx, offy);
                        //移动变化后的矩阵
                        float offx = event.getX() - startPF.x;
                        float offy = event.getY() - startPF.y;
                        Log.i("FFF", "onTouch: offx_" + offx + "__offy_" + offy);
                        changegMx.set(startMx);
                        changegMx.postTranslate(offx, offy);
                    } else if (sign == 2) {
                        //缩放
                        //把开始的矩阵设置给变化后的矩阵
                        //获取当前的移动距离 给现在的dismove
                        //计算缩放倍数  现在的距离/以前的距离=倍数=scale
                        //按倍数 （x，y）的倍数与 x点，y点缩放 changegMx.postScale(scale, scale, miPF.x, miPF.y);
                        float dismove = getDistance(event);
                        float scale = dismove / startDistance;
                        changegMx.set(startMx);
                        changegMx.postScale(scale, scale, miPF.x, miPF.y);
                        Log.i("FFF", "onTouch: scale" + scale);
                    }
                    break;


            }
            //设置图片的矩阵为变化后的矩阵

            imageView.setImageMatrix(changegMx);

            return true;
        }
    }


}
