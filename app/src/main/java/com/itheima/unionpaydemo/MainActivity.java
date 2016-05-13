package com.itheima.unionpaydemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.unionpay.UPPayAssistEx;
import com.unionpay.uppay.PayActivity;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements Response.Listener<String>, Response.ErrorListener {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    /**
     * 银联支付
     * @param v 按键
     */
    public void unionpay(View v){


        /**
         * 1.post相关信息到服务器：商品信息（名称、数量、价格）、支付信息（哪种支付方式）、用户信息（id）
         2.处理服务器返回结果，获取支付串码（调用第三方支付平台需要的核心参数）
         3.调用第三方支付平台
         4.处理支付结果
         */

        //银联测试接口
        String url="http://101.231.204.84:8091/sim/getacptn";
        StringRequest request = new StringRequest(url,MainActivity.this,MainActivity.this){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return super.getParams();
            }
        };

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

        queue.add(request);

    }

    @Override
    public void onResponse(String result) {
        //成功
        //Toast.makeText(MainActivity.this, "result==>: "+result, Toast.LENGTH_SHORT).show();

        pay(result);

    }

    private void pay(String result) {

        //测试环境 01, 正式环境00
        String serverMode = "01";

        String tnSum = result;
        UPPayAssistEx.startPayByJAR(MainActivity.this, PayActivity.class,null,null,tnSum,serverMode);



    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        //失败

        Toast.makeText(MainActivity.this, "volleyError", Toast.LENGTH_SHORT).show();

    }


    /**
     * sign —— 签名后做Base64的数据
     data —— 用于签名的原始数据
     data中原始数据结构：
     pay_result —— 支付结果success，fail，cancel
     tn          —— 订单号

     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult( int requestCode,
                                     int resultCode,
                                     Intent data)
    {
        if( data == null ){
            return;
        }

        String str =  data.getExtras().getString("pay_result");
        String mes = "";
        if( str.equalsIgnoreCase("success") ){
            // 支付成功后，extra中如果存在result_data，取出校验
            // result_data结构见c）result_data参数说明
            if(data.hasExtra("result_data")) {
                String sign =  data.getExtras().getString("result_data");
                // 验签证书同后台验签证书
                // 此处的verify，商户需送去商户后台做验签

                Log.d(TAG, "sign: "+sign);
                    //验证通过后，显示支付结果
                    showResultDialog(sign);
                mes="支付成功";
                }else{

                Toast.makeText(MainActivity.this, "验证失败", Toast.LENGTH_SHORT).show();

            }

        }else if(str.equalsIgnoreCase("fail") ){
            showResultDialog(" 支付失败！ ");
            mes="支付失败";
        }else if( str.equalsIgnoreCase("cancel") ){
            showResultDialog(" 你已取消了本次订单的支付！ ");

            mes="你已取消了本次订单的支付";
        }

        Toast.makeText(MainActivity.this, mes, Toast.LENGTH_SHORT).show();
    }

    /**
     * 对话框显示结果
     * @param result
     */
    private void showResultDialog(String result) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);


        builder.setTitle("支付结果");
        builder.setMessage(result);

        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();


    }


}
