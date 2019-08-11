/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package com.momt.emojipanel.emoji;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import com.momt.emojipanel.AndroidUtilities;
import com.momt.emojipanel.ApplicationLoader;
import com.momt.emojipanel.R;

public class EmojiDrawable extends Drawable {

    private static int drawImgSize;
    private static int bigImgSize;

    static {
        drawImgSize = AndroidUtilities.dp(20);
        bigImgSize = ApplicationLoader.applicationContext.getResources().getDimensionPixelOffset(R.dimen.emoji_big_size);
    }

    public static EmojiDrawable getEmojiDrawable(CharSequence code) {
        EmojiDrawableInfo info = EmojiUtils.rects.get(code);
        if (info == null) {
            CharSequence newCode = EmojiData.emojiAliasMap.get(code);
            if (newCode != null) {
                info = EmojiUtils.rects.get(newCode);
            }
        }
        if (info == null) {
            return null;
        }
        EmojiDrawable ed = new EmojiDrawable(info);
        ed.setBounds(0, 0, drawImgSize, drawImgSize);
        return ed;
    }

    public static Drawable getEmojiBigDrawable(String code) {
        EmojiDrawable ed = getEmojiDrawable(code);
        if (ed == null) {
            CharSequence newCode = EmojiData.emojiAliasMap.get(code);
            if (newCode != null) {
                ed = getEmojiDrawable(newCode);
            }
        }
        if (ed == null) {
            return null;
        }
        ed.setBounds(0, 0, bigImgSize, bigImgSize);
        ed.fullSize = true;
        return ed;
    }


    private EmojiDrawableInfo info;
    private boolean fullSize = false;
    private static Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private static Rect rect = new Rect();

    public EmojiDrawable(EmojiDrawableInfo i) {
        info = i;
    }

    public EmojiDrawableInfo getDrawableInfo() {
        return info;
    }

    public Rect getDrawRect() {
        Rect original = getBounds();
        int cX = original.centerX(), cY = original.centerY();
        rect.left = cX - (fullSize ? bigImgSize : drawImgSize) / 2;
        rect.right = cX + (fullSize ? bigImgSize : drawImgSize) / 2;
        rect.top = cY - (fullSize ? bigImgSize : drawImgSize) / 2;
        rect.bottom = cY + (fullSize ? bigImgSize : drawImgSize) / 2;
        return rect;
    }

    @Override
    public void draw(Canvas canvas) {
        if (EmojiUtils.emojiBmp[info.page][info.page2] == null) {
            if (EmojiUtils.loadingEmoji[info.page][info.page2]) {
                return;
            }
            EmojiUtils.loadingEmoji[info.page][info.page2] = true;
            EmojiUtils.loadEmoji(info.page, info.page2);
            EmojiUtils.loadingEmoji[info.page][info.page2] = false;
        }

        Rect b;
        if (fullSize) {
            b = getDrawRect();
        } else {
            b = getBounds();
        }

        canvas.drawBitmap(EmojiUtils.emojiBmp[info.page][info.page2], info.rect, b, paint);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }
}
