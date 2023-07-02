package kucko.test.endlessbackgroundservice.utils;

import android.content.Context;

import me.leolin.shortcutbadger.ShortcutBadger;

public class IconNotifBadge
{
    private final Context context;

    public IconNotifBadge(Context context )
    {
        this.context = context;
    }

    public void showIconBadge()
    {
        ShortcutBadger.applyCount( context, 1 );
    }

    public void hideIconBadge()
    {
        ShortcutBadger.applyCount( context, 0);
    }
}
