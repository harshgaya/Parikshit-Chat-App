package com.parikshit.parikshitchat;

import android.content.Context;
import android.view.View;

public class InternalURLSpan extends android.text.style.ClickableSpan {
    public String text;
    private Context context;

    @Override
    public void onClick(View widget) {
        handleLinkClicked(text);
    }

    public void handleLinkClicked(String value) {
        if (value.startsWith("http")) {
            // handle http links
//            Intent intent=new Intent(context,WebActivity.class);
//            context.startActivity(intent);

        } else if (value.startsWith("@")) {
            // handle @links
        } else if (value.startsWith("#")) {
            // handle #links
        }
    }

}
