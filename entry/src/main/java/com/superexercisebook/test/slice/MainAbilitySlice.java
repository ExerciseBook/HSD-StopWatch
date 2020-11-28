package com.superexercisebook.test.slice;

import com.superexercisebook.test.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.Text;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainAbilitySlice extends AbilitySlice {
    private int status = 0;
    private Date startTime = null;
    private int targetTime = 60 * 1000;
    private Date pauseTime;
    private Button startButton;
    private Button pauseOrEnd;
    private Text text;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
    }

    @Override
    public void onActive() {
        super.onActive();
        
        // 获取屏幕中央的倒计时读数文本对象
        text = (Text)this.findComponentById(ResourceTable.Id_text_screen);

        // 设置预设时间的按钮
        {
            Button t = (Button) this.findComponentById(ResourceTable.Id_button_1min);
            t.setClickedListener(component -> {
                if (status != 0) return;
                targetTime = 60 * 1000;
                refresh();
            });
        }

        {
            Button t = (Button) this.findComponentById(ResourceTable.Id_button_3min);
            t.setClickedListener(component -> {
                if (status != 0) return;
                targetTime = 60 * 3 * 1000;
                refresh();
            });
        }

        {
            Button t = (Button) this.findComponentById(ResourceTable.Id_button_5min);
            t.setClickedListener(component -> {
                if (status != 0) return;
                targetTime = 60 * 5 * 1000;
                refresh();
            });
        }

        // 设置控制按钮
        // 如果未开始或者已结束，那么按钮分别是 开始 和 结束
        // 如果正在计时，那么按钮分别是 开始 和 暂停
        // 如果正在暂停，那么按钮分别是 继续 和 结束
        pauseOrEnd = (Button)this.findComponentById(ResourceTable.Id_button_pauseOrEnd);
        startButton = (Button)this.findComponentById(ResourceTable.Id_button_start);

        pauseOrEnd.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                if (status == 0) {
                    return;
                }
                else
                if (status == 1) {
                    // 运行状态
                    pauseTime = new Date();
                    pauseOrEnd.setText("结束");
                    startButton.setText("继续");
                    status = 2;
                }
                else
                if (status == 2) {
                    // 暂停状态
                    startTime = null;
                    status = 0;
                    pauseOrEnd.setText("结束");
                    startButton.setText("开启");
                }
                refresh();
            }
        });


        startButton.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                if (status == 0 || status == 3) {
                    status = 1;
                    startTime = new Date();
                    pauseOrEnd.setText("暂停");
                }
                else
                if (status == 2) {
                    // 暂停状态
                    // 暂停状态转为继续状态时需要把暂停的时间给补上
                    long nowInt = (new Date()).getTime();
                    long pauseInt = pauseTime.getTime();
                    long pauseInterval = nowInt - pauseInt;
                    targetTime += pauseInterval;
                    pauseOrEnd.setText("暂停");
                    status = 1;
                }
                refresh();
            }
        });

        
        // 设置屏幕中央读数的更新规则
        Timer timer = new Timer();
        TimerTask task = new TimerTask(){

            @Override
            public void run() {
                //refresh();
                Runnable tmpRun = new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                    }
                };
                // 把更新读数的任务丢给界面线程进行
                getUITaskDispatcher().asyncDispatch(tmpRun);
            }
        };
        timer.schedule(task, 0L, 30L);

    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    /**
     * 秒表已运行时间 = 现在时间 - 开始时间
     * 读数 = 目标倒计时时间长度 - 秒表已运行时间
     */
    private void refresh() {
        
        long now = (new Date()).getTime();
        long startInt = 0;
        long tmpTargetTime = targetTime;

        if (status == 0) {
            // 未开始
            startInt = now;
        }
        if (status == 1) {
            // 正在计时
            startInt = startTime.getTime();
        }
        if (status == 2) {
            // 暂停计时
            return;
        }
        if (status == 3) {
            // 计时结束
            return;
        }
        long interval = tmpTargetTime - (now - startInt);

        if (status == 1 && interval <= 0) {
            status = 3;
            text.setText("时间到");
            targetTime = 60 * 1000;
            startButton.setText("开启");
            pauseOrEnd.setText("结束");
        }

        interval /= 1000;
        interval = interval > 0 ? interval : 0;

        long min = interval / 60;
        long sec = interval % 60;

        text.setText( (min < 10 ? "0" : "") + min  +
                " : " +
                (sec < 10 ? "0" : "") + sec);


    }
}
