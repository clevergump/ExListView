package lib.byhook.lv;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import lib.byhook.exlistview.R;

/**
 * Created by byhook on 15-10-31.
 * mail : byhook@163.com
 */
public class ExFooter extends LinearLayout {
    public ExFooter(Context context) {
        super(context);
        initView();
    }

    public ExFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ExFooter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.lv_header, this);

    }

}
