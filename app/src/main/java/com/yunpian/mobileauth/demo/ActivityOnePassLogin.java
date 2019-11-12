package com.yunpian.mobileauth.demo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.qipeng.yp.onepass.QPOnePass;
import com.qipeng.yp.onepass.callback.QPResultCallback;
import com.qipeng.yp.onepass.callback.SimpleActivityLifecycleCallback;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * OnePiece
 * Created by xukq on 6/6/19.
 */
public class ActivityOnePassLogin extends AppCompatActivity {

    private EditText phoneInputEt;
    private Button checkBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor();
        setContentView(R.layout.activity_one_pass_login);
        initToolbar();

        phoneInputEt = findViewById(R.id.phone_et);
        checkBtn = findViewById(R.id.one_pass_btn);

        phoneInputEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateConfirmBtnState();
            }
        });
        // 监听键盘弹出/隐藏
        final View container = findViewById(R.id.root);
        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                //getWindowVisibleDisplayFrame 获取当前窗口可视区域大小
                getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
                int screenHeight = getWindow().getDecorView().getHeight();
                //键盘弹出时，可视区域大小改变，屏幕高度 - 窗口可视区域高度 = 键盘弹出高度
                int softHeight = screenHeight - rect.bottom;
                /**
                 * 上移的距离 = 键盘的高度 - 按钮距离屏幕底部的高度(如果手机高度很大，上移的距离会是负数，界面将不会上移)
                 * 按钮距离屏幕底部的高度是用屏幕高度 - 按钮底部距离父布局顶部的高度
                 * 注意这里 btn.getBottom() 是按钮底部距离父布局顶部的高度，这里也就是距离最外层布局顶部高度
                 */
                int scrollDistance = softHeight - (screenHeight - checkBtn.getBottom());
                if (scrollDistance > 0) {
                    //具体移动距离可自行调整
                    container.scrollTo(0, scrollDistance + 80);
                } else {
                    //键盘隐藏，页面复位
                    container.scrollTo(0, 0);
                }
            }
        });

        TextView versionTv = findViewById(R.id.version_tv);
        versionTv.setText(versionTv.getText() + " " + Utils.getVersionName(this, getPackageName()));
    }

    private void updateConfirmBtnState() {
        boolean isActiveState = isMobileMum(phoneInputEt.getText().toString());
        if (isActiveState) {
            checkBtn.setEnabled(true);
            checkBtn.setBackgroundResource(R.drawable.qp_confirm_active_bg);
        } else {
            checkBtn.setEnabled(false);
            checkBtn.setBackgroundResource(R.drawable.qp_confirm_in_active_bg);
        }
    }

    public void checkOnePass(View view) {
        String phone = phoneInputEt.getText().toString();
        final AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setView(R.layout.loading)
                .setCancelable(false)
                .show();
        QPOnePass.getInstance().getToken(phone, new QPResultCallback() {
            @Override
            public void onSuccess(String message) {
                checkFromService(loadingDialog, message);
            }

            @Override
            public void onFail(String message) {
                try {
                    JSONObject result = new JSONObject(message);
                    int errorCode = result.optInt("code");
                    if (errorCode == -20206 || errorCode == -20202 || message.toLowerCase().contains("unable to resolve host") || errorCode == -40102 || errorCode == -40202 || errorCode == -40302) {
                        message = "请确保数据流量开关已开启";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Toast.makeText(ActivityOnePassLogin.this, message, Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
                goToSmsActivity();
            }
        }); // 极验
    }

    // 模拟服务器检测用户验证是否有效
    private void checkFromService(final AlertDialog loadingDialog, final String sucMsg) {
        // TODO: 2019-11-12 开发者自行实现
        Intent intent = new Intent(ActivityOnePassLogin.this, ActivityAuthResult.class);
        intent.putExtra("title", "校验成功");
        startActivity(intent);
        loadingDialog.dismiss();
        finish();
    }

    private void goToSmsActivity() {
        // 自定义短信界面
        QPOnePass.getInstance().registerSmsAuthActivityLifecycleCallback(new SimpleActivityLifecycleCallback() {
            @Override
            public void onActivityCreated(final Activity activity, Bundle savedInstanceState) {
                // 调整虚拟导航栏颜色
                Window window = activity.getWindow();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    window.setNavigationBarColor(Color.WHITE);
                }

                LinearLayout customView = activity.findViewById(R.id.custom_login_ll);
                customView.findViewById(R.id.login_wechat_iv).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(ActivityOnePassLogin.this, "微信登录成功", Toast.LENGTH_SHORT).show();
                    }
                });
                customView.findViewById(R.id.login_weibo_iv).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(ActivityOnePassLogin.this, "微博登录成功", Toast.LENGTH_SHORT).show();
                    }
                });
                ImageView loginBtn = customView.findViewById(R.id.login_sms_iv);
                loginBtn.setImageResource(R.drawable.ic_login_onelogin);
                loginBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.finish();
                    }
                });

                // 自动填充电话
                EditText phoneEt = activity.findViewById(R.id.qp_phone_et);
                phoneEt.setText(phoneInputEt.getText());
            }
        });
        QPOnePass.getInstance().requestSmsToken(new QPResultCallback() {
            @Override
            public void onSuccess(String message) {
                try {
                    JSONObject result = new JSONObject(message);
                    if (result.has("cid")) {
                        Toast.makeText(ActivityOnePassLogin.this, "验证码发送成功", Toast.LENGTH_SHORT).show();
                    }
                    if (result.optBoolean("passed")) {
                        Toast.makeText(ActivityOnePassLogin.this, "短信验证成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ActivityOnePassLogin.this, ActivityAuthResult.class);
                        intent.putExtra("title", "短信验证成功");
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(String message) {
                try {
                    JSONObject result = new JSONObject(message);
                    if (result.optInt("status") == 1001) {
//                        Toast.makeText(ActivityOnePassLogin.this, "用户手动取消验证", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String msg = result.optString("msg", "短信验证失败");
                    Toast.makeText(ActivityOnePassLogin.this, msg, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setStatusBar() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        onDispatchTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    public void onDispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (phoneInputEt != null) {
                int[] l = {0, 0};
                phoneInputEt.getLocationInWindow(l);
                int left = l[0], top = l[1], bottom = top + phoneInputEt.getHeight(), right = left
                        + phoneInputEt.getWidth();
                if (event.getX() > left && event.getX() < right
                        && event.getY() > top && event.getY() < bottom) {
                    // 点击EditText的事件，忽略它。
                } else {
                    phoneInputEt.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(phoneInputEt.getWindowToken(), 0); //强制隐藏键盘
                }
            }
        }
    }

    private boolean isMobileMum(@NonNull String mobileNums) {
        if (mobileNums.startsWith("1") && mobileNums.length() == 11) {
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStatusBarColor() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.qp_white_color));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setNavigationBarColor(Color.WHITE);
        }
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
