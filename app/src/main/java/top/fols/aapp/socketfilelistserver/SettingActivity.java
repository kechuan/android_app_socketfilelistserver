package top.fols.aapp.socketfilelistserver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;



import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.OnPermissionInterceptor;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import top.fols.aapp.simpleListView.Entry;
import top.fols.aapp.simpleListView.EntryAdapter;

import top.fols.aapp.simpleListView.EntryFactory;

import top.fols.box.util.XExceptionTool;
import top.fols.box.io.os.XFile;

public class SettingActivity extends Activity {
    private ListView listView;
    private EntryAdapter adapter;

    private SettingActivity getActivity(){
        return this;
    }

    public static void toast(Object o) {
        if (o instanceof Exception) {
            MainActivity.toast((Exception) o);
        } else {
            MainActivity.toast(o);
        }
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        try {
            setContentView(R.layout.setting);
            setTitle(R.string.setting_name);

            if (listView == null) {

                List<Entry> fruitList = new ArrayList<Entry>();

                adapter = new EntryAdapter(SettingActivity.this, fruitList);

                listView = findViewById(R.id.settingListView1);

                //entryAdapter 用于展示listView数据 可以理解为。。 abstract抽象 的 各种数据展示的回调处理
                listView.setAdapter(adapter);

                final Entry entry5 = new Entry();
                final Entry entry6 = new Entry();
                final Entry entry7 = new Entry();
                final Entry entry8 = new Entry();
                final Entry entry9 = new Entry();
                final Entry entry10 = new Entry();
                final Entry entry11 = new Entry();
                final Entry entry12 = new Entry();
                final Entry entry13 = new Entry();
                final Entry entry14 = new Entry();
                final Entry entry15 = new Entry();
                final Entry entry16 = new Entry();


                final Entry permissionHandler = EntryFactory.templateEntry(
                    "申请存储权限",
                     (String) (Config.getOpenAppOpenWebServer() ? getText(R.string.on): getText(R.string.off)),
                    true,

                    view -> XXPermissions.with(getActivity())
                        .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                        .interceptor(
                            new OnPermissionInterceptor() {
                                @Override
                                public void launchPermissionRequest(@NonNull Activity activity, @NonNull List<String> allPermissions, @Nullable OnPermissionCallback callback) {
                                    PermissionDialog permissionDialog = new PermissionDialog(
                                        "存储权限获取",
                                        "授权以正常获取外部存储目录",
                                        activity,
                                        allPermissions,
                                        this,
                                        callback
                                    );

                                    permissionDialog.showDialog();
                                }
                            }
                        )

                        .request(new OnPermissionCallback() {

                            @Override
                            public void onGranted(@NonNull List<String> permissions, boolean all) {
                                if (!all) {
                                    toast("获取部分权限成功，但部分权限未正常授予");
                                    return;
                                }
                                toast("获取外部存储权限成功 请重启以正常运行APP");


                            }

                            @Override
                            public void onDenied(@NonNull List<String> permissions, boolean never) {
                                if (never) {
                                    toast("被永久拒绝授权，请手动授予外部存储");
                                    // 如果是被永久拒绝就跳转到应用权限系统设置页面
                                    XXPermissions.startPermissionActivity(getActivity().getBaseContext(), permissions);
                                }
                                else {
                                    toast("获取外部存储失败");
                                }
                            }
                        })

                );


				fruitList.add(permissionHandler);


                final Entry onStartEntry = EntryFactory.templateEntry(
                    "启动应用时启动服务器",
                    "" + (Config.getOpenAppOpenWebServer() ? getText(R.string.on) : getText(R.string.off)),
                    true,
                    true

                );

                onStartEntry.onChange = (button, status) -> {
                    // TODO: Implement this method
                    Config.setOpenAppOpenWebServer(status);
                    //因为需要 onStartEntry 实体更改 所以只能分离出来赋值了

                    onStartEntry.checkBox = status;
                    onStartEntry.subTitle = "" + (Config.getOpenAppOpenWebServer() ? getText(R.string.on) : getText(R.string.off));

                    adapter.notifyDataSetChanged();
                    MainActivity.heatSet();
                };


                fruitList.add(onStartEntry);


                final Entry basicDirEntry = EntryFactory.templateEntry(
                    "基础目录",
                    Config.getBaseDir(),
                    true
                );

                basicDirEntry.onClick = view -> {
                    // TODO: Implement this method
                    Config.setBaseDir(SettingActivity.this,
                        basicDirEntry.title,
                        newDir -> {
                            // TODO: Implement this method
                            basicDirEntry.subTitle = newDir;
                            adapter.notifyDataSetChanged();
                            MainActivity.heatSet();
                        });
                };

                fruitList.add(basicDirEntry);


                final Entry serverPortEntry = EntryFactory.templateEntry(
                    "服务器端口",
                    String.format("%d",Config.getWebPort()),
                    true

                );

                serverPortEntry.onClick = view -> {
                    // TODO: Implement this method
                    final String config = String.valueOf(Config.getWebPort());
                    final EditText et = new EditText(SettingActivity.this);
                    et.setInputType(InputType.TYPE_CLASS_NUMBER);
                    et.setText(config);
                    new AlertDialog.Builder(SettingActivity.this)
                            .setTitle(serverPortEntry.title)
                            .setView(et)
                            .setPositiveButton("确定", (dialog, which) -> {
                                String input = et.getText().toString();
                                int i = Config.toInt(input);
                                if (input.isEmpty()) {
                                    toast("端口不能为空");
                                }

                                else if (i < getResources().getInteger(R.integer.MIN_PORT)  || i > getResources().getInteger(R.integer.MAX_PORT)) {
                                    toast("端口异常");
                                }

                                else {
                                    Config.setWebPort(i);

                                    serverPortEntry.subTitle = (String.valueOf(Config.getWebPort()));
                                    adapter.notifyDataSetChanged();
                                    toast("修改完毕,请重启应用");
                                    MainActivity.heatSet();
                                }

                            })
                            .setNeutralButton("默认", (dialog, which) -> {
                                Config.setWebPort(Config.defWebPort);
                                serverPortEntry.subTitle = (String.valueOf(Config.getWebPort()));
                                adapter.notifyDataSetChanged();
                                toast("修改完毕,请重启应用");
                                MainActivity.heatSet();
                            })
                            .setNegativeButton("取消", null)
                            .show();
                };

                fruitList.add(serverPortEntry);


                final Entry gridViewEntry = EntryFactory.templateEntry(
                    "列表默认宫格模式",
                    "" + (Config.getFileListLatticeMode() ? getText(R.string.on) : getText(R.string.off)),
                    true,
                    Config.getFileListLatticeMode()
                );

                gridViewEntry.onChange = (button, status) -> {
                    // TODO: Implement this method
                    Config.setFileListLatticeMode(status);
                    gridViewEntry.checkBox = status;
                    gridViewEntry.subTitle = "" + (Config.getFileListLatticeMode() ? getText(R.string.on) : getText(R.string.off));
                    adapter.notifyDataSetChanged();
                    MainActivity.heatSet();
                };


                fruitList.add(gridViewEntry);

//                adapter.notifyDataSetChanged(); ???


                entry5.title = "多线程文件下载";
                entry5.subTitle = "" + (Config.getMultiThreadDownload() ? getText(R.string.on) : getText(R.string.off));
                entry5.subTitleShow = true;
                entry5.checkBox = Config.getMultiThreadDownload();
                entry5.checkBoxShow = true;
                entry5.onChange = new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton p1, boolean p2) {
                        // TODO: Implement this method
                        Config.setMultiThreadDownload(p2);
                        entry5.checkBox = p2;
                        entry5.subTitle = "" + (Config.getMultiThreadDownload() ? getText(R.string.on) : getText(R.string.off));
                        adapter.notifyDataSetChanged();
                        MainActivity.heatSet();
                    }
                };
                fruitList.add(entry5);


                entry6.title = "允许下载App";
                entry6.subTitle = "" + (Config.getSupportDownloadApp() ? getText(R.string.on) : getText(R.string.off));
                entry6.subTitleShow = true;
                entry6.checkBox = Config.getSupportDownloadApp();
                entry6.checkBoxShow = true;
                entry6.onChange = new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton p1, boolean p2) {
                        // TODO: Implement this method
                        Config.setSupportDownloadApp(p2);
                        entry6.checkBox = p2;
                        entry6.subTitle = "" + (Config.getSupportDownloadApp() ? getText(R.string.on) : getText(R.string.off));
                        adapter.notifyDataSetChanged();
                        MainActivity.heatSet();
                    }
                };
                fruitList.add(entry6);


                entry7.title = "最大监听线程";
                entry7.subTitleShow = true;
                entry7.subTitle = "" + Config.getMaxMonitorThread();
                entry7.checkBox = false;
                entry7.checkBoxShow = false;
                entry7.onClick = new View.OnClickListener() {
                    @Override
                    public void onClick(View p1) {
                        // TODO: Implement this method
                        final String config = String.valueOf(Config.getMaxMonitorThread());
                        final EditText et = new EditText(SettingActivity.this);
                        et.setInputType(InputType.TYPE_CLASS_NUMBER);
                        et.setText(config);
                        new AlertDialog.Builder(SettingActivity.this)
                                .setTitle(entry7.title)
                                .setView(et)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String input = et.getText().toString();
                                        int i = Config.toInt(input);

                                        if ("".equals(input) || null == input) {
                                            toast("不能为空");
                                        } else if (i < 1) {
                                            toast("输入异常");
                                        } else {
                                            Config.setMaxMonitorThread(i);

                                            entry7.subTitle = (String.valueOf(Config.getMaxMonitorThread()));
                                            adapter.notifyDataSetChanged();
                                            MainActivity.heatSet();
                                        }
                                    }
                                })
                                .setNeutralButton("默认", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Config.setMaxMonitorThread(Config.defMaxThread);

                                        entry7.subTitle = (String.valueOf(Config.getMaxMonitorThread()));
                                        adapter.notifyDataSetChanged();
                                        MainActivity.heatSet();
                                    }
                                })
                                .setNegativeButton("取消", null)
                                .show();
                    }
                };
                fruitList.add(entry7);


                //entry = new Entry();
                entry8.title = "上传数据给用户速度限制";
                entry8.subTitleShow = true;
                String text = null;
                if (Config.isUploadDataToSpeedLimit()) {
                    text = String.valueOf(Config.getUploadDataToSpeedLimit() + "(" + XFile.fileUnitFormat(Config.getUploadDataToSpeedLimit()) + "/s)");
                }
                else {
                    text = String.valueOf("无限制");
                }
                entry8.subTitle = text;
                entry8.checkBox = false;
                entry8.checkBoxShow = false;
                entry8.onClick = new View.OnClickListener() {
                    @Override
                    public void onClick(View p1) {
                        // TODO: Implement this method
                        final RelativeLayout relativeLayout = new RelativeLayout(SettingActivity.this);
                        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                        final TextView TextView = new TextView(SettingActivity.this);
                        final SeekBar et = new SeekBar(SettingActivity.this);
                        et.setMax(5000);
                        et.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                        et.setId(R.id.main_speed_upload);

                        RelativeLayout.LayoutParams layoutparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layoutparams.addRule(RelativeLayout.BELOW, et.getId());
                        TextView.setGravity(Gravity.CENTER_HORIZONTAL);
                        TextView.setLayoutParams(layoutparams);
                        TextView.setText("0B/s");

                        et.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar p1, int p2, boolean p3) {
                                if (et.getProgress() == et.getMax()) {
                                    TextView.setText("没有限制");
                                } else {
                                    long speed = formatSeekToSpeed(et.getProgress());
                                    TextView.setText(XFile.fileUnitFormat(speed) + "/s");
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar p1) {
                                // TODO: Implement this method
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar p1) {
                                // TODO: Implement this method
                            }
                        });
                        int index = formatSpeedToSeek(Config.getUploadDataToSpeedLimit());
                        if (index < 0) {
                            index = et.getMax();
                        }
                        et.setProgress(index);
                        if (et.getProgress() == et.getMax()) {
                            TextView.setText("没有限制");
                        } else {
                            long speed = formatSeekToSpeed(et.getProgress());
                            TextView.setText(XFile.fileUnitFormat(speed) + "/s");
                        }

                        relativeLayout.addView(et);
                        relativeLayout.addView(TextView);

                        new AlertDialog.Builder(SettingActivity.this)
                                .setTitle(entry8.title)
                                .setView(relativeLayout)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        long i = formatSeekToSpeed(et.getProgress());
                                        if (et.getProgress() == et.getMax()) {
                                            i = -1;
                                        }
                                        Config.setUploadDataToSpeedLimit(i);
                                        String text;
                                        if (Config.isUploadDataToSpeedLimit()) {
                                            text = String.valueOf(Config.getUploadDataToSpeedLimit() + "(" + XFile.fileUnitFormat(Config.getUploadDataToSpeedLimit()) + "/s)");
                                        } else {
                                            text = String.valueOf("无限制");
                                        }
                                        entry8.subTitle = (text);
                                        adapter.notifyDataSetChanged();
                                        MainActivity.heatSet();

                                    }

                                })
                                .setNeutralButton("默认", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Config.setUploadDataToSpeedLimit(Config.defUploadDataToSpeedLimit);
                                        String text;
                                        if (Config.isUploadDataToSpeedLimit()) {
                                            text = String.valueOf(Config.getUploadDataToSpeedLimit() + "(" + XFile.fileUnitFormat(Config.getUploadDataToSpeedLimit()) + "/s)");
                                        } else {
                                            text = String.valueOf("无限制");
                                        }
                                        entry8.subTitle = (text);
                                        adapter.notifyDataSetChanged();
                                        MainActivity.heatSet();

                                    }
                                })
                                .setNegativeButton("取消", null)
                                .show();
                    }
                };
                fruitList.add(entry8);


                entry9.title = "下载用户上传的数据速度限制";
                entry9.subTitleShow = true;
                text = null;
                if (Config.isDownloadUserUploadSpeedLimit()) {
                    text = String.valueOf(Config.getDownloadUserUploadSpeedLimit() + "(" + XFile.fileUnitFormat(Config.getDownloadUserUploadSpeedLimit()) + "/s)");
                } else {
                    text = String.valueOf("无限制");
                }
                entry9.subTitle = text;
                entry9.checkBox = false;
                entry9.checkBoxShow = false;
                entry9.onClick = new View.OnClickListener() {
                    @Override
                    public void onClick(View p1) {
                        // TODO: Implement this method
                        final RelativeLayout relativeLayout = new RelativeLayout(SettingActivity.this);
                        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                        final TextView TextView = new TextView(SettingActivity.this);
                        final SeekBar et = new SeekBar(SettingActivity.this);
                        et.setMax(5000);
                        et.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                        et.setId(R.id.main_speed_download);

                        RelativeLayout.LayoutParams layoutparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layoutparams.addRule(RelativeLayout.BELOW, et.getId());
                        TextView.setGravity(Gravity.CENTER_HORIZONTAL);
                        TextView.setLayoutParams(layoutparams);
                        TextView.setText("0B/s");

                        et.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar p1, int p2, boolean p3) {
                                if (et.getProgress() == et.getMax()) {
                                    TextView.setText("没有限制");
                                } else {
                                    long speed = formatSeekToSpeed(et.getProgress());
                                    TextView.setText(XFile.fileUnitFormat(speed) + "/s");
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar p1) {
                                // TODO: Implement this method
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar p1) {
                                // TODO: Implement this method
                            }
                        });
                        int index = formatSpeedToSeek(Config.getDownloadUserUploadSpeedLimit());
                        if (index < 0) {
                            index = et.getMax();
                        }
                        et.setProgress(index);
                        if (et.getProgress() == et.getMax()) {
                            TextView.setText("没有限制");
                        } else {
                            long speed = formatSeekToSpeed(et.getProgress());
                            TextView.setText(XFile.fileUnitFormat(speed) + "/s");
                        }

                        relativeLayout.addView(et);
                        relativeLayout.addView(TextView);

                        new AlertDialog.Builder(SettingActivity.this)
                                .setTitle(entry9.title)
                                .setView(relativeLayout)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        long i = formatSeekToSpeed(et.getProgress());
                                        if (et.getProgress() == et.getMax()) {
                                            i = -1;
                                        }
                                        Config.setDownloadUserUploadSpeedLimit(i);
                                        String text;
                                        if (Config.isDownloadUserUploadSpeedLimit()) {
                                            text = String.valueOf(Config.getDownloadUserUploadSpeedLimit() + "(" + XFile.fileUnitFormat(Config.getDownloadUserUploadSpeedLimit()) + "/s)");
                                        } else {
                                            text = String.valueOf("无限制");
                                        }
                                        entry9.subTitle = (text);
                                        adapter.notifyDataSetChanged();
                                        MainActivity.heatSet();
                                    }
                                })
                                .setNeutralButton("默认", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Config.setDownloadUserUploadSpeedLimit(Config.defDownloadUserUploadSpeedLimit);
                                        String text;
                                        if (Config.isUploadDataToSpeedLimit()) {
                                            text = String.valueOf(Config.getDownloadUserUploadSpeedLimit() + "(" + XFile.fileUnitFormat(Config.getDownloadUserUploadSpeedLimit()) + "/s)");
                                        } else {
                                            text = String.valueOf("无限制");
                                        }
                                        entry9.subTitle = (text);
                                        adapter.notifyDataSetChanged();
                                        MainActivity.heatSet();

                                    }
                                })
                                .setNegativeButton("取消", null)
                                .show();
                    }
                };
                fruitList.add(entry9);

                //entry = new Entry();
                entry10.subTitle = "其它";
                entry10.subTitleShow = true;
                fruitList.add(entry10);

                //entry = new Entry();
                long dirLength = 0;
                final File[] fs = Config.getLogDir().listFiles();
                for (int i = 0; fs != null && i < fs.length; i++) {
                    dirLength += fs[i].length();
                }
                entry11.title = "日志";
                entry11.subTitleShow = true;
                entry11.subTitle = XFile.fileUnitFormat(dirLength) + "(" + (fs == null ? 0 : fs.length) + ")";
                entry11.checkBoxShow = false;
                entry11.onClick = new View.OnClickListener() {
                    @Override
                    public void onClick(View p1) {
                        // TODO: Implement this method
                        new AlertDialog.Builder(SettingActivity.this)
                                .setTitle(getText(R.string.delete) + "?")
                                .setCancelable(true)
                                .setPositiveButton(getText(R.string.confirm), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface p1, int p2) {
                                        // TODO: Implement this method
                                        for (int i = 0; fs != null && i < fs.length; i++) {
                                            fs[i].delete();
                                        }
                                        long dirLength = 0;
                                        final File[] fs = Config.getLogDir().listFiles();
                                        for (int i = 0; fs != null && i < fs.length; i++) {
                                            dirLength += fs[i].length();
                                        }
                                        entry11.subTitle = XFile.fileUnitFormat(dirLength) + "(" + (fs == null ? 0 : fs.length) + ")";
                                        adapter.notifyDataSetChanged();
                                    }
                                })
                                .show();
                    }
                };
                fruitList.add(entry11);



                //entry = new Entry();
                entry13.title = "版本";
                entry13.subTitleShow = true;
                entry13.subTitle = Config.getNowVersion();
                entry13.checkBoxShow = false;
                entry13.onClick = new View.OnClickListener() {
                    @Override
                    public void onClick(View p1) {
                        // TODO: Implement this method
                        Utils.openLink(SettingActivity.this, "https://www.coolapk.com/apk/top.fols.aapp.socketfilelistserver");
                    }
                };
                fruitList.add(entry13);


                //添加 关于页面
//                entry14.title = "赞助";
//                entry14.subTitleShow = true;
//                entry14.subTitle = "" + "alipay(支付宝) 784920843@qq.com";
//                entry14.checkBoxShow = false;
//                entry14.onClick = new View.OnClickListener() {
//                    @Override
//                    public void onClick(View p1) {
//                        // TODO: Implement this method
//                        Utils.openLink(SettingActivity.this, "https://mobilecodec.alipay.com/client_download.htm?qrcode=a6x02342sxg3u49xdxf47b6");
//                    }
//                };
//                fruitList.add(entry14);

                //entry = new Entry();
                entry16.title = "Github";
                entry16.subTitleShow = true;
                entry16.subTitle = "Open Github";
                entry16.checkBoxShow = false;
                entry16.onClick = p1 -> {
                    // TODO: Implement this method
                    Utils.openLink(SettingActivity.this, "https://github.com/xiaoxinwangluo/android_app_socketfilelistserver");
                };

                fruitList.add(entry16);


                adapter.notifyDataSetChanged();
                //notifyDataSetChanged


            }

        }

        catch (Throwable e) {
            MainActivity.toast(XExceptionTool.StackTraceToString(e));
        }
    }

    public long formatSeekToSpeed(int seek) {
        return ((long) seek + 1L) * 8192L * 4L;
    }

    public int formatSpeedToSeek(long speed) {
        if (speed < 0) {
            return -1;
        } else if (speed == 0) {
            return 0;
        }
        return (int) (speed / 4L / 8192L - 1L);
    }
}
