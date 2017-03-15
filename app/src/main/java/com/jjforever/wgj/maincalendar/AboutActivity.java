package com.jjforever.wgj.maincalendar;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.jjforever.wgj.maincalendar.monthui.ThemeStyle;
import com.jjforever.wgj.maincalendar.toolbar.ToolBarActivity;
import com.jjforever.wgj.maincalendar.util.DateUtil;
import com.jjforever.wgj.maincalendar.util.Helper;

public class AboutActivity extends ToolBarActivity {

    /**
     * 作者易信号
     */
    private static final String mAuthorName = "main_h";
    /**
     * 作者易信推广链接
     */
    private static final String mExtLink = "http://yxs.im/8a62w2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        showOkBtn(false);
        setTitle(getResources().getString(R.string.action_about));

        TextView rightView = (TextView) findViewById(R.id.about_right);
        if (rightView != null) {
            String tmpStr = String.format(getString(R.string.about_right),
                    DateUtil.getYear(), Helper.getAppVersionName(this));
            rightView.setText(getClickableSpan(tmpStr));
            rightView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    /**
     * 获取超链接字段
     * @return 超链接字符串
     */
    private SpannableString getClickableSpan(String aStr) {
        int startIndex = aStr.indexOf(mAuthorName);
        int endIndex = startIndex + mAuthorName.length();
        SpannableString spannableString = new SpannableString(aStr);
        //设置下划线文字
//        spannableString.setSpan(new UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //设置文字的单击事件
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Uri uri = Uri.parse(mExtLink);
                Context context = widget.getContext();
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
                context.startActivity(intent);
            }
        }, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //设置文字的前景色
        spannableString.setSpan(new ForegroundColorSpan(ThemeStyle.Accent), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }
}
