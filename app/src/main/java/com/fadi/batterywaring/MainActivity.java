package com.fadi.batterywaring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.fadi.batterywaring.utils.WeakHandler;
// 电池状态
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static android.os.BatteryManager.EXTRA_STATUS;
// 未知
import static android.os.BatteryManager.BATTERY_STATUS_UNKNOWN;
// 充电中
import static android.os.BatteryManager.BATTERY_STATUS_CHARGING;
// 放电中
import static android.os.BatteryManager.BATTERY_STATUS_DISCHARGING;
// 未充电
import static android.os.BatteryManager.BATTERY_STATUS_NOT_CHARGING;
// 电池满
import static android.os.BatteryManager.BATTERY_STATUS_FULL;
// 电池健康情况
import static android.os.BatteryManager.EXTRA_HEALTH;
// 未知
import static android.os.BatteryManager.BATTERY_HEALTH_UNKNOWN;
// 良好
import static android.os.BatteryManager.BATTERY_HEALTH_GOOD;
// 过热
import static android.os.BatteryManager.BATTERY_HEALTH_OVERHEAT;
// 没电
import static android.os.BatteryManager.BATTERY_HEALTH_DEAD;
// 未知错误
import static android.os.BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE;
// 过电压
import static android.os.BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE;
// 温度过低
import static android.os.BatteryManager.BATTERY_HEALTH_COLD;
// 充电类型
import static android.os.BatteryManager.EXTRA_PLUGGED;
// 充电器
import static android.os.BatteryManager.BATTERY_PLUGGED_AC;
// 其他
import static android.os.BatteryManager.BATTERY_PLUGGED_ANY;
// USB
import static android.os.BatteryManager.BATTERY_PLUGGED_USB;
// 无线充电
import static android.os.BatteryManager.BATTERY_PLUGGED_WIRELESS;
// 当前电量
import static android.os.BatteryManager.EXTRA_LEVEL;
// 当前电池温度
import static android.os.BatteryManager.EXTRA_TEMPERATURE;
import static android.os.BatteryManager.EXTRA_SCALE;
// 当前电池电压
import static android.os.BatteryManager.EXTRA_VOLTAGE;
// 电池技术描述
import static android.os.BatteryManager.EXTRA_TECHNOLOGY;
// 最大充电电压
import static android.os.BatteryManager.EXTRA_MAX_CHARGING_VOLTAGE;
// 最大充电电流
import static android.os.BatteryManager.EXTRA_MAX_CHARGING_CURRENT;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int DEFAULT_CHARGING_VOLTAGE_MICRO_VOLT = 5000000;

    private static final int MSG_UPDATE_UI = 0;


    private String result;

    private TextView tv_show;

    private final MainHandler mHandler = new MainHandler(this);
    private static class MainHandler extends WeakHandler<MainActivity> {

        public MainHandler(MainActivity owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = getOwner();
            if (activity == null)
                return;

            if (msg.what == MSG_UPDATE_UI) {
                activity.updateUI();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startBroadCast();
        
        initView();
    }

    private void initView() {
        tv_show = (TextView) findViewById(R.id.tv_show);
    }

    private void updateUI() {
        tv_show.setText(result);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBroadCast();
    }

    private void startBroadCast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);
    }

    private void stopBroadCast() {
        unregisterReceiver(mBroadcastReceiver);
    }

    private  BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent) {
                return;
            }

            String action = intent.getAction();

            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                int status = intent.getIntExtra(EXTRA_STATUS, BATTERY_STATUS_UNKNOWN);
                int plugged = intent.getIntExtra(EXTRA_PLUGGED, BATTERY_PLUGGED_ANY);
                int level = intent.getIntExtra(EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(EXTRA_SCALE, 0);
                int health = intent.getIntExtra(EXTRA_HEALTH, BATTERY_HEALTH_UNKNOWN);

                int maxChargingMicroAmp = intent.getIntExtra(EXTRA_MAX_CHARGING_CURRENT, -1);
                int maxChargingMicroVolt = intent.getIntExtra(EXTRA_MAX_CHARGING_VOLTAGE, -1);
                int batteryVolt = intent.getIntExtra(EXTRA_VOLTAGE, -1);
                int temperature = intent.getIntExtra(EXTRA_TEMPERATURE, -1);
                String batteryTechnologyDescript = intent.getStringExtra(EXTRA_TECHNOLOGY);

                final int maxChargingMicroWatt;

                if (maxChargingMicroVolt <= 0) {
                    maxChargingMicroVolt = DEFAULT_CHARGING_VOLTAGE_MICRO_VOLT;
                }
                if (maxChargingMicroAmp > 0) {
                    // Calculating muW = muA * muV / (10^6 mu^2 / mu); splitting up the divisor
                    // to maintain precision equally on both factors.
                    maxChargingMicroWatt = (maxChargingMicroAmp / 1000)
                            * (maxChargingMicroVolt / 1000);
                } else {
                    maxChargingMicroWatt = -1;
                }


                String statusStr = getStatus(status);
                String healthStr = getHealth(health);
                String pluggedStr = getPlugged(plugged);
                String levelStr = getLevel(level);
                String scaleStr = getLevel(scale);
                String temperatureStr = getTemperature(temperature);
                String batteryVoltStr = getBatteryVolt(batteryVolt);
                String maxChargingMicroAmpStr = getMaxChargingMicroAmp(maxChargingMicroAmp);
                String maxChargingMicroVoltStr = getMaxChargingMicroVolt(maxChargingMicroVolt);

                int currentChargingCurrent = getCurrentChargingCurrent();
                String currentChargingCurrentStr = getCurrentChargingCurrentStr(currentChargingCurrent);
                int currentChargingVoltage = getCurrentChargingVoltage();
                String currentChargingVoltageStr = getCurrentChargingVoltageStr(currentChargingVoltage);


                result = MainActivity.this.getString(R.string.adb_shell_help)
                        + "\n" + MainActivity.this.getString(R.string.battery_current_level) + levelStr
                        + "\n" + MainActivity.this.getString(R.string.battery_current_temperature) + temperatureStr
                        + "\n" + MainActivity.this.getString(R.string.battery_current_volt) + batteryVoltStr
                        + "\n" + MainActivity.this.getString(R.string.battery_current_charging_current) + currentChargingCurrentStr
                        + "\n" + MainActivity.this.getString(R.string.battery_current_charging_voltage) + currentChargingVoltageStr
                        + "\n" + MainActivity.this.getString(R.string.battery_status_titls) + statusStr
                        + "\n" + MainActivity.this.getString(R.string.battery_plugged_titls) + pluggedStr
                        + "\n" + MainActivity.this.getString(R.string.battery_max_charging_current) + maxChargingMicroAmpStr
                        + "\n" + MainActivity.this.getString(R.string.battery_max_charging_voltage) + maxChargingMicroVoltStr
                        + "\n" + MainActivity.this.getString(R.string.battery_health_titls) + healthStr
                        + "\n" + MainActivity.this.getString(R.string.battery_max_level) + scaleStr
                        + "\n" + MainActivity.this.getString(R.string.battery_technology_describing) + batteryTechnologyDescript
                        +"\n充电速度 = " + maxChargingMicroWatt;
                mHandler.sendEmptyMessage(MSG_UPDATE_UI);

            }
        }


    };

    private String getCurrentChargingVoltageStr(int currentChargingVoltage) {
        return String.format("%.3f V", currentChargingVoltage / 1000000.0);
    }

    private String getCurrentChargingCurrentStr(int currentChargingCurrent) {
        return String.format("%.3f A", currentChargingCurrent / 1000.0);
    }

    /**
     * 当前充电电流 mA
     *
     * adb shell "cat /sys/class/power_supply/battery/BatteryAverageCurrent"
     */
    private int getCurrentChargingCurrent() {
        int result = 0;
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader("/sys/class/power_supply/battery/BatteryAverageCurrent"));
            if ((line = br.readLine()) != null) {
                result = Integer.parseInt(line);
            }

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    /**
     * 当前充电电压 uV
     *
     * adb shell "cat /sys/class/power_supply/battery/batt_vol"
     */
    private int getCurrentChargingVoltage() {
        int result = 0;
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader("/sys/class/power_supply/battery/batt_vol"));
            if ((line = br.readLine()) != null) {
                result = Integer.parseInt(line);
            }

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    private String getMaxChargingMicroVolt(int maxChargingMicroVolt) {
        return String.format("%.1f V", maxChargingMicroVolt / 1000000.0);
    }

    private String getMaxChargingMicroAmp(int maxChargingMicroAmp) {
        return String.format("%.1f A", maxChargingMicroAmp / 1000000.0);
    }

    private String getBatteryVolt(int batteryVolt) {
        return String.format("%.3f V", batteryVolt / 1000.0);
    }


    private String getTemperature(int temperature) {
        return String.format("%.1f ℃", temperature / 10.0);
    }

    private String getLevel(int level) {
        return String.format("%d %%", level);
    }

    private String getPlugged(int plugged) {
        String result = getString(R.string.battery_plugged_any);

        switch (plugged) {
            case BATTERY_PLUGGED_ANY:
                break;
            case BATTERY_PLUGGED_AC:
                result = getString(R.string.battery_plugged_ac);
                break;
            case BATTERY_PLUGGED_USB:
                result = getString(R.string.battery_plugged_usb);
                break;
            case BATTERY_PLUGGED_WIRELESS:
                result = getString(R.string.battery_plugged_wireless);
                break;
        }

        return result;
    }


    private String getHealth(int health) {
        String result = getString(R.string.battery_health_unknow);

        switch (health) {
            case BATTERY_HEALTH_UNKNOWN:
                break;
            case BATTERY_HEALTH_GOOD:
                result = getString(R.string.battery_health_good);
                break;
            case BATTERY_HEALTH_OVERHEAT:
                result = getString(R.string.battery_health_overheat);
                break;
            case BATTERY_HEALTH_DEAD:
                result = getString(R.string.battery_health_dead);
                break;
            case BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                result = getString(R.string.battery_health_unspecified_failure);
                break;
            case BATTERY_HEALTH_OVER_VOLTAGE:
                result = getString(R.string.battery_health_over_voltage);
                break;
            case BATTERY_HEALTH_COLD:
                result = getString(R.string.battery_health_cold);
                break;
        }

        return result;
    }


    private String getStatus(int status) {
        String result = getString(R.string.battery_status_unknown);

        switch (status) {
            case BATTERY_STATUS_FULL:
                result = getString(R.string.battery_status_full);
                break;
            case BATTERY_STATUS_NOT_CHARGING:
                result = getString(R.string.battery_status_not_charging);
                break;
            case BATTERY_STATUS_DISCHARGING:
                result = getString(R.string.battery_status_discharging);
                break;
            case BATTERY_STATUS_CHARGING:
                result = getString(R.string.battery_status_charging);
                break;
            case BATTERY_STATUS_UNKNOWN:
                break;
        }

        return result;
    }
}
