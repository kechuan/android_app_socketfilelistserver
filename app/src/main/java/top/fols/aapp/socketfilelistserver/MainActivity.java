package top.fols.aapp.socketfilelistserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
//import androidx.appcompat.widget.SwitchCompat;


import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import top.fols.aapp.utils.XUIHandler;
import top.fols.aapp.view.alertdialog.SingleSelectAlertDialog;
import top.fols.aapp.view.alertdialog.SingleSelectAlertDialogCallback;
import top.fols.box.application.httpserver.XHttpServer;
import top.fols.box.application.socketfilelistserver.XHttpFileListDataPacketHander;
import top.fols.box.io.XStream;
import top.fols.box.io.os.XFile;
import top.fols.box.util.XExceptionTool;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.OnPermissionInterceptor;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

/**
 * @author lenovo
 */

//MessageDeal ?? extends Activity
public class MainActivity extends AppCompatActivity implements XUIHandler.MessageDeal {

	private ImageView main_cloud_status_icon = null;
	private TextView main_cloud_status_text = null;
	private Switch main_cloud_status_switch = null;
	private TextView main_baseDir= null;

	private TextView main_link_count = null;
	private TextView main_ipv4_addres = null;
	private TextView main_ipv6_addres = null;
	private TextView main_upload_speed = null;
	private TextView main_download_speed = null;
	private TextView main_log_size = null;

	private void updateUiCloudStatus(boolean status) {
		if (main_cloud_status_icon == null) {
            main_cloud_status_icon = findViewById(R.id.main_cloud_icon);
        }

		// widget -> AppCompat
		main_cloud_status_icon.setImageDrawable(AppCompatResources.getDrawable(context,status ? R.drawable.ic_cloud_status_on : R.drawable.ic_cloud_status_off));

		if (main_cloud_status_text == null) {
            main_cloud_status_text = findViewById(R.id.main_cloud_status);
        }
		main_cloud_status_text.setText(getText(status ? R.string.on : R.string.off));

		if (main_cloud_status_switch == null) {
            main_cloud_status_switch = findViewById(R.id.main_cloud_switch);
        }

		main_cloud_status_switch.setChecked(status);
	}
	private void updateUiBaseDir(String dir) {
		if (main_baseDir == null) {
            main_baseDir = findViewById(R.id.main_basedir);
        }
		main_baseDir.setText(dir);
	}
	private void updateUiThreadCount(long count) {
		if (main_link_count == null) {
            main_link_count = findViewById(R.id.main_link_count);
        }
		main_link_count.setText(String.format("%d", count));
	}
	private void updateUiIPV4Address(String address) {
		if (main_ipv4_addres == null) {
            main_ipv4_addres = findViewById(R.id.main_ip_v4);
        }
		main_ipv4_addres.setText(address);
	}
	private void updateUiIPV6Address(String address) {
		if (main_ipv6_addres == null) {
            main_ipv6_addres = (TextView)findViewById(R.id.main_ip_v6);
        }
		main_ipv6_addres.setText(address);
	}
	private void updateUiUploadSpeed(String speed) {
		if (main_upload_speed == null) {
            main_upload_speed = (TextView)findViewById(R.id.main_speed_upload);
        }
		main_upload_speed.setText(speed + "");
	}
	private void updateUiDownloadSpeed(String speed) {
		if (main_download_speed == null) {
            main_download_speed = (TextView)findViewById(R.id.main_speed_download);
        }
		main_download_speed.setText(speed + "");
	}
	private void updateUiLogSize(String size) {
		if (main_log_size == null) {
            main_log_size = (TextView)findViewById(R.id.main_log_size);
        }
		main_log_size.setText(size + "");
	}


	private void openServer(boolean isSwitch) {
		try {
			
			if (isSwitch) {
				server.stop(); server.start();
				server.log(getString(R.string.server_start));
			}

			else {
				server.stop();
				server.log(getString(R.string.server_end));
			}
		}

		catch (BindException e) {
			toast(getText(R.string.server_exception) + ": " + e.getMessage());
			server.log(getText(R.string.server_exception) + ": " + e.getMessage());
		}

		catch (Exception e) {
			toast(getText(R.string.server_exception) + ": \n" + XExceptionTool.StackTraceToString(e));
			System.out.println(getText(R.string.server_exception) + ": \n" + XExceptionTool.StackTraceToString(e));

			server.log(XExceptionTool.StackTraceToString(e));
		}
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		//调用inflate()方法创建菜单
        getMenuInflater().inflate(R.menu.main_menu, menu);
        //如果返回false，创建的菜单无法显示
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			if(item.getItemId() == R.id.settingMenu){
				Intent intent = new Intent();
				intent.setClass(getContext(), SettingActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("name", "This is from MainActivity!");
				intent.putExtras(bundle);
				startActivity(intent);
			}

			else if(item.getItemId() == R.id.clearResMenu){
				File zipres = new File(getFilesDir(), "res.zip");
				zipres.delete();
				writerResZip();
				if (fileListDataHander != null) {
					fileListDataHander.clearResCache();
					fileListDataHander.setResZipPath(zipres);
					toast(getText(R.string.write_successful) + "");//写入成功
				}
			}

			else if(item.getItemId() == R.id.copyV4linkMenu){
				Utils.setClip(MainActivity.this, getV4Url());
				toast(getText(R.string.copy_successful) + "");//"复制成功."
			}

			else if(item.getItemId() == R.id.shareV4linkMenu){
				shareMsg(getString(R.string.app_name) + Config.getNowVersion(), /* 分享连接*/getText(R.string.share_link) + "", getV4Url());
			}

			else if(item.getItemId() == R.id.thisPhoneV4LocalAddressMenu){
				SingleSelectAlertDialog dialog;
				dialog = new SingleSelectAlertDialog();
				dialog.setContext(this);
				final List<String> allHost = Utils.getIPV4LANList();
				for (int i = 0;i < allHost.size();i++) {
					allHost.set(i, formatHttpUrl(allHost.get(i), getPort()));
				}
				String[] str = new String[allHost.size()];
				dialog.setItems(allHost.toArray(str));
				dialog.setCancelable(true);
				dialog.setDirectSelectMode(true);
				dialog.setCallback(new SingleSelectAlertDialogCallback(){
					@Override
					public void selectComplete(SingleSelectAlertDialog obj, String key, int index, boolean isSelected, boolean isNegativeButton) {
						// TODO: Implement this method
						if (key != null && isSelected) {
							showQrCode(allHost.get(index));
						}
					}
				});
				dialog.show();
			}

			else if(item.getItemId() == R.id.copyV6linkMenu){
				Utils.setClip(MainActivity.this, getV6Url());
				toast(getText(R.string.copy_successful) + "");//"复制成功."
			}

			else if(item.getItemId() == R.id.shareV6linkMenu){
				shareMsg(getString(R.string.app_name) + Config.getNowVersion(),  /* 分享连接*/getText(R.string.share_link) + "", getV6Url());
			}

			else if(item.getItemId() == R.id.thisPhoneV6LocalAddressMenu){
				SingleSelectAlertDialog dialog;
				dialog = new SingleSelectAlertDialog();
				dialog.setContext(this);
				final List<String> allHost = Utils.getIPV6LANList();

                allHost.replaceAll(host -> formatHttpUrl(host, getPort()));

				String[] str = new String[allHost.size()];
				dialog.setItems(allHost.toArray(str));
				dialog.setCancelable(true);
				dialog.setDirectSelectMode(true);
				dialog.setCallback(new SingleSelectAlertDialogCallback(){
					@Override
					public void selectComplete(SingleSelectAlertDialog obj, String key, int index, boolean isSelected, boolean isNegativeButton) {
						// TODO: Implement this method
						if (key != null && isSelected) {
							showQrCode(allHost.get(index));
						}
					}
				});
				dialog.show();

			}

			else if(item.getItemId() == R.id.clearLogMenu){
				logStream.clear();
			}

			else if(item.getItemId() == R.id.addAppToBaseDir){
				String path = getApplicationContext().getPackageResourcePath();

				File apk = new File(path);
				File f = new File(Config.getBaseDir(), getString(R.string.app_name) + Config.getNowVersion() + ".apk");
				if (f.exists()) {
					toast(getText(R.string.file_already_exist) + ": " + f.getAbsolutePath());
				}
				if (!new File(Config.getBaseDir()).canWrite()) {
					toast(getText(R.string.no_write_permission) + ": " + f.getAbsolutePath());
				}
				XStream.copy(new FileInputStream(apk), new FileOutputStream(f));
				toast("OK");
			}

			else if(item.getItemId() == R.id.helpMenu){
				Utils.openLink(MainActivity.this, "https://github.com/xiaoxinwangluo/android_app_socketfilelistserver/blob/master/help.md");
			}



			//通过调用item.getItemId()来判断菜单项

		}

		catch (Exception e) {
			System.out.println(e);

			toast(e);
		}
        return true;
    }

	@Override
	public Object dealMessages(Object[] value) {
		if (value.length > 0) {
			int Type = (int) value[0];
			switch (Type) {
				case TYPE_SET_TITLE:
					setTitle(value[1].toString());
					break;
				case TYPE_UPDATE:
					try {
						updateUiBaseDir(fileListDataHander.getBaseDir().getAbsolutePath());
						updateUiCloudStatus(!server.isStop());
						updateUiThreadCount(server.getNowThreadPoolSize());
						updateUiIPV4Address(getV4Host() + ":" + getPort());
						updateUiIPV6Address(getV6Host() + ":" + getPort());
						updateUiUploadSpeed(XFile.fileUnitFormat(fileListDataHander.getUpload2UserSpeedLimit().getAverageSpeed()));
						updateUiDownloadSpeed(XFile.fileUnitFormat(fileListDataHander.getDownloadUserSpeedLimit().getAverageSpeed()));
						updateUiLogSize(XFile.fileUnitFormat(logStream.getLogFileLength()));
					} catch (Exception e) {
						break;
					}

				default: break;
			}
		}
		return null;
	}

	@Override
	public void onBackPressed() { 
        new AlertDialog.Builder(this).setTitle(String.format("%s？", getText(R.string.exit))) 
			.setCancelable(true)
            .setPositiveButton(getText(R.string.confirm), new DialogInterface.OnClickListener() { 
                @Override 
                public void onClick(DialogInterface dialog, int which) { 
					// 点击“确认”后的操作 
                    server.stop();
					android.os.Process.killProcess(android.os.Process.myPid());
                } 
            }) 
            .show(); 
	} 

	public File writerResZip() throws IOException {
		File zipFile = new File(getFilesDir(), "res.zip");
		if (!zipFile.exists()) {
			InputStream is = getAssets().open("res.zip");
			OutputStream os = Files.newOutputStream(zipFile.toPath());
			XStream.copy(is, os);
			is.close();
			os.close();
		} 
		return zipFile;
	}

	@Override
	protected void onResume() {
		// TODO: Implement this method
		super.onResume();
		context = getApplicationContext();
		handlerHeartbeat();
	}
	
	public static void handlerHeartbeat() {
		if (handler == null) {
            handler = XUIHandler.create();
        }
	}
	
	private static final int TYPE_SET_TITLE = 1;
	private static final int TYPE_UPDATE = 2;
	private static final int TYPE_IMAGE_REQUEST_CODE = 4;
	private static final int TYPE_REQUEST_CODE_CAMERA = 8;
	
	private static XUIHandler handler;
	//这是什么? 就是Handler 用于Thread 与 UIThread的通信 这里指更新UI信息

	private Thread notifyThread = null;
	private Switch serverStatusSwitch = null;
	public static LogFileOutStream logStream;

	private static XHttpServer server;
	private static XHttpFileListDataPacketHander fileListDataHander;

	private void storageHandler(){

		XXPermissions.with(this)
			// 申请单个权限
			.permission( Permission.MANAGE_EXTERNAL_STORAGE)
			.interceptor(new OnPermissionInterceptor(){

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

			})
			.request(new OnPermissionCallback() {

				@Override
				public void onGranted(@NonNull List<String> permissions, boolean all) {
					if (!all) {
						toast("获取部分权限成功，但部分权限未正常授予");
						return;
					}
					toast("获取外部存储权限成功");

                    try {

						Config.init();

						if (logStream == null) {
							logStream = new LogFileOutStream(Config.getLogFile(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(System.currentTimeMillis()) + ".log"), true);
						}

						if(serverStatusSwitch==null){
							configLoad();

						}

                    }

					catch (IOException e) {
						toast("未知异常 IOException");
						throw new RuntimeException(e);
                    }


				}

				@Override
				public void onDenied(@NonNull List<String> permissions, boolean never) {
					if (never) {
						toast("被永久拒绝授权，请手动授予外部存储权限");
						// 如果是被永久拒绝就跳转到应用权限系统设置页面
						XXPermissions.startPermissionActivity(context, permissions);
					} else {
						toast("获取外部存储权限失败");
					}
				}
			});
	}


	public class NotifyThread extends Thread {
		@Override
		public void run() {
			handler.sendMessages(new Object[]{TYPE_UPDATE}, MainActivity.this);
			while (true) {
				try {
					Thread.sleep(1500);
					handler.sendMessages(new Object[]{TYPE_UPDATE}, MainActivity.this);
				}

				catch (Exception e) {
					System.out.println(e);
					toast(e);

				}

			}
		}
	}

	protected static void heatSet() {


		if (fileListDataHander == null) {
			fileListDataHander = new XHttpFileListDataPacketHander();
		}

		if (server != null) {
			server.setDataHandler(fileListDataHander);
		}


		fileListDataHander.setBaseDir(new File(Config.getBaseDir()));
		fileListDataHander.setSupportRangeDownload(Config.getMultiThreadDownload());
		fileListDataHander.setSupportFileUpload(Config.getUploadFile());

		long speed;
		speed = Config.getDownloadUserUploadSpeedLimit();
		fileListDataHander.getDownloadUserSpeedLimit().setCycleAccessMax(speed == -1 ?8192 * 1024: speed);
		fileListDataHander.getDownloadUserSpeedLimit().limit(speed != -1);

		speed = Config.getUploadDataToSpeedLimit();
		fileListDataHander.getUpload2UserSpeedLimit().setCycleAccessMax(speed == -1 ?8192 * 1024: speed);
		fileListDataHander.getUpload2UserSpeedLimit().limit(speed != -1);

		fileListDataHander.setSupportKeepAlive(false);

		fileListDataHander.setFileListLatticeMode(Config.getFileListLatticeMode());
		fileListDataHander.setSupportDownloadApp(Config.getSupportDownloadApp());
		if (server != null) {
			server.setMaxThreadPoolSize(Config.getMaxMonitorThread());

			server.log("Loading Info...");
			Map m = Config.getConfig();
			for (Object Key:m.keySet()) {
                server.log(Key + "=" + m.get(Key));
            }
		}
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		context = getApplicationContext();



		handlerHeartbeat(); // XUIHandler 初始化
        storageHandler();

		if (notifyThread == null) {
			notifyThread = new NotifyThread();
			notifyThread.start();
		}


    }

	private void configLoad() throws IOException {
		try {
			fileListDataHander = new XHttpFileListDataPacketHander();

			File zipRes = writerResZip();
			//如果是第一次启动的话 大概率会卡在这。 然后直接触发error 因为没有授权

			fileListDataHander.setResZipPath(zipRes);

			boolean openAppOpenWebServer = Config.getOpenAppOpenWebServer();
			//由onStart配置 是否直接开启服务器

			if (server == null) {server = new XHttpServer();}
			server.setPort(Config.getWebPort());
			server.setLogSteam(logStream);


			heatSet(); //fileListDataHander配置

			openServer(openAppOpenWebServer);
			updateUiCloudStatus(openAppOpenWebServer);

			listenerLoad();

		}

		catch (IOException e){
			toast(e);

		}
	}

	private void listenerLoad(){
		serverStatusSwitch = findViewById(R.id.main_cloud_switch);
		serverStatusSwitch.setOnCheckedChangeListener((serverSwitch, switchStatus) -> {

			if (!serverSwitch.isPressed()) {
				return;
			}
			openServer(switchStatus);
		});

		main_cloud_status_text.setOnClickListener(p1 -> {
			// TODO: Implement this method
			openServer(!serverStatusSwitch.isChecked());
		});

		findViewById(R.id.main_ip_v4).setOnClickListener(
				p1 -> Utils.openLink(MainActivity.this, getV4Url())
		);

		findViewById(R.id.main_ip_v6).setOnClickListener(
				p1 -> Utils.openLink(MainActivity.this, getV6Url())
		);

		findViewById(R.id.main_ip_v4_icon).setOnClickListener(p1 -> {
			final String ipv4Url = getV4Url();
			showQrCode(ipv4Url);
		});

		findViewById(R.id.main_ip_v6_icon).setOnClickListener(p1 -> {
			final String ipv6Url = getV6Url();
			showQrCode(ipv6Url);
		});

		findViewById(R.id.main_log_size).setOnClickListener(p1 -> {

			ScrollView logScrollView = new ScrollView(MainActivity.this);

			logScrollView.setLayoutParams(
					new ScrollView.LayoutParams(
							ScrollView.LayoutParams.WRAP_CONTENT,
							ScrollView.LayoutParams.WRAP_CONTENT
					)
			);

			TextView logTextView = new TextView(MainActivity.this);

			logTextView.setLayoutParams(
					new ScrollView.LayoutParams(
							ScrollView.LayoutParams.WRAP_CONTENT,
							ScrollView.LayoutParams.WRAP_CONTENT
					)
			);
			logTextView.setTextIsSelectable(true);

			StringBuilder logStringBuilder = new StringBuilder();

			long logLength = logStream.getLogFileLength();

			if (logLength <= R.integer.MEGA_BYTE) {
				logStringBuilder.append(new XFile(logStream.getLogFile()).toString());
			} else {
				try {
					logStringBuilder.append(new String(XFile.readFile(logStream.getLogFile(), 0, (int) (1L * 1024L * 1024L))));
				} catch (Throwable e) {
					logStringBuilder.append("load log file error." + "\n")
							.append(XExceptionTool.StackTraceToString(e));
				}
			}

			logTextView.setText(logStringBuilder);

			logScrollView.addView(logTextView);

			new AlertDialog.Builder(MainActivity.this)
					.setView(logScrollView)
					.setCancelable(true)
					.create()
					.show();
		});


		final TextView baseDirView = findViewById(R.id.main_basedir);
		baseDirView.setOnClickListener(p1 ->
				Config.setBaseDir(
						MainActivity.this,
						getString(R.string.config_basedir),
						baseDirView::setText
				)
		);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			//这里的requestCode是我自己设置的，就是确定返回到那个Activity的标志
			case TYPE_IMAGE_REQUEST_CODE:
				//resultcode是setResult里面设置的code值
				if (resultCode == RESULT_OK) {
					try {
						Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
						String[] filePathColumn = {MediaStore.Images.Media.DATA};
						Cursor cursor = getContentResolver().query(selectedImage,
																   filePathColumn, null, null, null);//从系统表中查询指定Uri对应的照片
                        if (cursor != null) {
                            cursor.moveToFirst();
                        }
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
						String path = cursor.getString(columnIndex); //获取照片路径
						cursor.close();

						String URL = Utils.decodeQrcode(path).toString();
						Utils.openLink(this, URL);
					} catch (Exception e) {
						// TODO Auto-generatedcatch block
						toast(e);
					}
				}
				break;

			case TYPE_REQUEST_CODE_CAMERA://这里的requestCode是我自己设置的，就是确定返回到那个Activity的标志
				if (requestCode ==  TYPE_REQUEST_CODE_CAMERA && resultCode == RESULT_OK) {
					try {
						Bundle bundle = data.getExtras();
						// 获取相机返回的数据，并转换为Bitmap图片格式，这是缩略图
						Bitmap bitmap = (Bitmap) bundle.get("data");
						String URL = Utils.decodeQrcode(bitmap).toString();

						Utils.openLink(this, URL);
					} catch (Exception e) {
						// TODO Auto-generatedcatch block
						toast(e);
					}

				}
				break;

		}
	}


	public void showQrCode(final String ul) {
		final RelativeLayout relativeLayout = new RelativeLayout(MainActivity.this);
		relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

		final Bitmap bmp = Utils. encodeQrcode(ul, 720, 720);

		final ImageView qrCodeImage = new ImageView(MainActivity.this);
		qrCodeImage.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
		qrCodeImage.setImageBitmap(bmp);
		qrCodeImage.setOnLongClickListener(p1 -> {
            Utils.openLink(MainActivity.this, ul);
            return true;
        });
		
		qrCodeImage.setId(R.id.settingMenu);

		TextView textView = new TextView(MainActivity.this);
		RelativeLayout.LayoutParams layoutparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutparams.addRule(RelativeLayout.BELOW, qrCodeImage.getId());
		textView.setGravity(Gravity.CENTER_HORIZONTAL);
		textView.setLayoutParams(layoutparams);

		textView.setText(Html.fromHtml(String.format("<a href='%s'>%s</a>", ul, ul)));
		textView.setOnClickListener(p1 -> {
            // TODO: Implement this method
            Utils.openLink(MainActivity.this, ul);
        });
		relativeLayout.addView(qrCodeImage);
		relativeLayout.addView(textView);

		AlertDialog.Builder qrDialogBuilder = new AlertDialog.Builder(MainActivity.this)
			.setTitle("Qrcode") 
			.setView(relativeLayout)  
			.setPositiveButton(getText(R.string.shooting) + "", (p1, p2) -> {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, TYPE_REQUEST_CODE_CAMERA);
            })
			.setNegativeButton(getText(R.string.select_image) + "", (p1, p2) -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                //intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, TYPE_IMAGE_REQUEST_CODE);
            }).setNeutralButton(getText(R.string.save) + "", (p1, p2) -> {
                try {
                    File file = new File(Config.SDPath, System.currentTimeMillis() + ".png");
                    byte[] byteArray = Utils.Bitmap2Bytes(bmp);
                    new XFile(file).append(byteArray);
                    toast(getText(R.string.write_successful) + ": " + file.getAbsolutePath());
                    Utils.updateDCIM(getApplicationContext(), file.getAbsolutePath());
                } catch (IOException e) {
                    toast(XExceptionTool.StackTraceToString(e));
                }
            });
		qrDialogBuilder.show();

	}


	private static Context context = null;
	public static void toast(Object obj) {
		Toast.makeText(getContext(), obj == null ?"": obj.toString(), Toast.LENGTH_LONG).show();
	}
	public static void toast(Exception obj) {
		toast(XExceptionTool.StackTraceToString(obj));
	}
	public static Context getContext() {return context;}

	public String getV4Host() {
		return Utils.getIPV4LAN();
	}
	public String getV6Host() {
		return Utils.getIPV6LAN();
	}
	public int getPort() {
		return server.getBindPort();
	}
	public String getV4Url() {
		return formatHttpUrl(getV4Host(), String.valueOf(getPort()));
	}
	public String getV6Url() {
		return formatHttpUrl(getV6Host(), String.valueOf(getPort()));
	}
	public static String formatHttpUrl(Object host, Object port) {
		return String.format("http://%s:%s", host, port);
	}

	public void shareMsg(String activityTitle, String msgTitle, String msgText) {  
        shareMsg(activityTitle, msgTitle, msgText, null);
    }
	public void shareMsg(String activityTitle, String msgTitle, String msgText,  
						 String imgPath) {  
        Intent intent = new Intent(Intent.ACTION_SEND);  
        if (imgPath == null || imgPath.isEmpty()) {
            intent.setType("logTextView/plain"); // 纯文本  
        } else {  
            File f = new File(imgPath);  
            if (f.exists() && f.isFile()) {
                intent.setType("image/jpg");  
				Uri u = Uri.fromFile(f);  
                intent.putExtra(Intent.EXTRA_STREAM, u);  
            }  
        }  
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);  
        intent.putExtra(Intent.EXTRA_TEXT, msgText);  
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
        startActivity(Intent.createChooser(intent, activityTitle));  
    } 





}
