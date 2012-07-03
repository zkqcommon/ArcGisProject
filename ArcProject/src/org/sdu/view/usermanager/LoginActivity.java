package org.sdu.view.usermanager;

import org.sdu.dbaction.Action;
import org.sdu.gis.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private Button login, cancel;
	private EditText username, password;
	private Action userAction;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		init();
	}

	/**
	 * 该方法用来对login组件进行监听、进行登录验证或取消登录
	 */
	public void LoginAction(View view) {
		Intent intent;
		switch (view.getId()) {
		case R.id.login:
			// 首先获取登录信息
			String name = username.getText().toString();
			String pwd = password.getText().toString();
			// 跳转到相应页面
			if(name.equals("")||pwd.equals("")){
				Toast.makeText(this, "用户名或密码不能为空", Toast.LENGTH_LONG).show();
			}else{
				if(userAction.login(name, pwd)){
					//跳转到相关页面
					 intent=new Intent();
					 intent.setClass(LoginActivity.this, AddUserActivity.class);
					 startActivity(intent);
				}else{
					//提示错误并清空密码
					password.setText("");
					Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_LONG).show();
				}
			}
			break;
		case R.id.cancelLogin:
			 //清空密码
			password.setText("");
			break;
		default:
			break;
		}

	}
	public void init(){
		// 获取组件
		login = (Button) this.findViewById(R.id.login);
		cancel = (Button) this.findViewById(R.id.cancelLogin);
		username = (EditText) this.findViewById(R.id.username);
		password = (EditText) this.findViewById(R.id.password);
		userAction=new Action(this);
	}
}
