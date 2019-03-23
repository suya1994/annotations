package com.example.annotation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.annotation.bindView.GetViewTo;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {

    @GetViewTo(value = R.id.text_view)
    private TextView mTextView;

    @GetViewTo(R.id.button)
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getAnnotations();

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"text  on textview is :" + mTextView.getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAnnotations() {
        try {
            //获取MainActivity的成员变量
            Field[] fields = getClass().getDeclaredFields();
            for (Field field : fields) {
                if (null != field.getAnnotations()) {
                    //确定注解类型
                    if (field.isAnnotationPresent(GetViewTo.class)) {
                        //允许修改反射属性
                        field.setAccessible(true);
                        GetViewTo getViewTo = field.getAnnotation(GetViewTo.class);
                        //findViewById将注解的id，找到View注入成员变量中
                        field.set(this, findViewById(getViewTo.value()));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
