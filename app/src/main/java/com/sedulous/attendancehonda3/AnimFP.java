package com.sedulous.attendancehonda3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

import attendancehonda.R;

public class AnimFP extends View {
    Paint paint, paint_w;

    float v=2.0f;
    int time=0, cheight=0, cwidth=0;
    RectF rorigin;
    boolean isActive=true;
    int y=0;

    AnimListener animListener;

    public AnimFP(Context context, AnimListener animListener) {
        super(context);
        this.animListener = animListener;
        paint=new Paint();
        paint_w=new Paint();
        //ColorFilter filter = new PorterDuffColorFilter(getColor(context, android.R.color.white), PorterDuff.Mode.SRC_IN);
        //paint.setColorFilter(filter);
        paint_w.setColor(Color.WHITE);

    }
    @Override
    public void onDraw(Canvas canvas){
        try {
            if (time == 0) {
                cheight = getHeight();
                cwidth = getWidth();

            }
            time+=5;

            if(time% (cheight*2) < cheight) y=time%cheight;
            else y=cheight-time% cheight;
            if(y> cheight) y= cheight;
            if(y<0) y=0;
            paint.setShader(new LinearGradient(0, y, 0, cheight, getResources().getColor(R.color.semi_transparent),
                    getResources().getColor(R.color.transparent), Shader.TileMode.MIRROR));
            canvas.drawRect(new Rect(0,y,cwidth,cheight),paint);
            canvas.drawLine(0,y,cwidth,y, paint_w);

            postInvalidate();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static interface AnimListener{
        public void onAnimViewCompleated();
    }
}

