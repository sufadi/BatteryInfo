### 0.效果
![image](https://raw.githubusercontent.com/sufadi/BatteryInfo/master/BatteryStatus.png)

### 1. 电池广播信息

```
// 电池状态
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
```

我们可以监听电池广播获取

```
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
```


### 2. 电池电压获取
读取文件节点即可


adb shell "cat /sys/class/power_supply/battery/batt_vol"

```
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
```


### 3. 电池电流获取
读取文件节点即可

adb shell "cat /sys/class/power_supply/battery/BatteryAverageCurrent"

```
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
```
### 4. Demo下载
https://github.com/sufadi/BatteryInfo