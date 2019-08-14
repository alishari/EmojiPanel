/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package com.momt.emojipanel.emoji;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Build;
import android.text.Spannable;
import android.text.Spanned;
import android.util.Pair;
import com.momt.emojipanel.AndroidUtilities;
import com.momt.emojipanel.ApplicationLoader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;

public class EmojiUtils {

    static final HashMap<CharSequence, EmojiDrawableInfo> rects = new HashMap<>();

    private static final int splitCount = 4;
    static final Bitmap[][] emojiBmp = new Bitmap[8][splitCount];
    static final boolean[][] loadingEmoji = new boolean[8][splitCount];
    public static float emojiSpanSizeRatio = 1f;

    public static void initialize(Context context) {
        ApplicationLoader.setContext(context);
        AndroidUtilities.density = context.getResources().getDisplayMetrics().density;
        loadAllEmojis();
        initMaps();
    }

    private static final int[][] cols = {
            {16, 16, 16, 16},
            {6, 6, 6, 6},
            {5, 5, 5, 5},
            {7, 7, 7, 7},
            {5, 5, 5, 5},
            {7, 7, 7, 7},
            {8, 8, 8, 8},
            {8, 8, 8, 8},
    };

    private static void initMaps() {
        int emojiFullSize;
        int add = 2;
        if (AndroidUtilities.density <= 1.0f) {
            emojiFullSize = 33;
            add = 1;
        } else if (AndroidUtilities.density <= 1.5f) {
            emojiFullSize = 66;
        } else if (AndroidUtilities.density <= 2.0f) {
            emojiFullSize = 66;
        } else {
            emojiFullSize = 66;
        }

        for (int j = 0; j < EmojiData.data.length; j++) {
            int count2 = (int) Math.ceil(EmojiData.data[j].length / (float) splitCount);
            int position;
            for (int i = 0; i < EmojiData.data[j].length; i++) {
                int page = i / count2;
                position = i - page * count2;
                int row = position % cols[j][page];
                int col = position / cols[j][page];
                Rect rect = new Rect(row * emojiFullSize + row * add, col * emojiFullSize + col * add, (row + 1) * emojiFullSize + row * add, (col + 1) * emojiFullSize + col * add);
                rects.put(EmojiData.data[j][i], new EmojiDrawableInfo(rect, (byte) j, (byte) page, i));
            }
        }
    }

    public static void loadEmoji(final int page, final int page2) {
        try {
            float scale;
            int imageResize = 1;
            if (AndroidUtilities.density <= 1.0f) {
                scale = 2.0f;
                imageResize = 2;
            } else if (AndroidUtilities.density <= 1.5f) {
                //scale = 3.0f;
                //imageResize = 2;
                scale = 2.0f;
            } else if (AndroidUtilities.density <= 2.0f) {
                scale = 2.0f;
            } else {
                scale = 2.0f;
            }

            Bitmap bitmap = null;
            try {
                String fileName = "emoji/" + String.format(Locale.US, "v14_emoji%.01fx_%d_%d.png", scale, page, page2);
                InputStream is = ApplicationLoader.applicationContext.getAssets().open(fileName);
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = false;
                opts.inSampleSize = imageResize;
                if (Build.VERSION.SDK_INT >= 26) {
                    opts.inPreferredConfig = Bitmap.Config.HARDWARE;
                }
                bitmap = BitmapFactory.decodeStream(is, null, opts);
                is.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }

            final Bitmap finalBitmap = bitmap;
            emojiBmp[page][page2] = finalBitmap;
        } catch (Throwable x) {
            x.printStackTrace();
        }
    }

    public static CharSequence replaceEmoji(CharSequence cs, boolean createNew) {
        return replaceEmoji(cs, createNew, null);
    }

    public static CharSequence replaceEmoji(CharSequence cs, boolean createNew, int[] emojiOnly) {
        if (cs == null || cs.length() == 0) {
            return cs;
        }
        Spannable s;
        if (!createNew && cs instanceof Spannable) {
            s = (Spannable) cs;
        } else {
            s = Spannable.Factory.getInstance().newSpannable(cs.toString());
        }
        long buf = 0;
        int emojiCount = 0;
        char c;
        int startIndex = -1;
        int startLength = 0;
        int previousGoodIndex = 0;
        StringBuilder emojiCode = new StringBuilder(16);
        int length = cs.length();
        boolean doneEmoji = false;

        try {
            for (int i = 0; i < length; i++) {
                c = cs.charAt(i);
                if (c >= 0xD83C && c <= 0xD83E || (buf != 0 && (buf & 0xFFFFFFFF00000000L) == 0 && (buf & 0xFFFF) == 0xD83C && (c >= 0xDDE6 && c <= 0xDDFF))) {
                    if (startIndex == -1) {
                        startIndex = i;
                    }
                    emojiCode.append(c);
                    startLength++;
                    buf <<= 16;
                    buf |= c;
                } else if (emojiCode.length() > 0 && (c == 0x2640 || c == 0x2642 || c == 0x2695)) {
                    emojiCode.append(c);
                    startLength++;
                    buf = 0;
                    doneEmoji = true;
                } else if (buf > 0 && (c & 0xF000) == 0xD000) {
                    emojiCode.append(c);
                    startLength++;
                    buf = 0;
                    doneEmoji = true;
                } else if (c == 0x20E3) {
                    if (i > 0) {
                        char c2 = cs.charAt(previousGoodIndex);
                        if ((c2 >= '0' && c2 <= '9') || c2 == '#' || c2 == '*') {
                            startIndex = previousGoodIndex;
                            startLength = i - previousGoodIndex + 1;
                            emojiCode.append(c2);
                            emojiCode.append(c);
                            doneEmoji = true;
                        }
                    }
                } else if ((c == 0x00A9 || c == 0x00AE || c >= 0x203C && c <= 0x3299) && EmojiData.dataCharsMap.contains(c)) {
                    if (startIndex == -1) {
                        startIndex = i;
                    }
                    startLength++;
                    emojiCode.append(c);
                    doneEmoji = true;
                } else if (startIndex != -1) {
                    emojiCode.setLength(0);
                    startIndex = -1;
                    startLength = 0;
                    doneEmoji = false;
                } else if (c != 0xfe0f) {
                    if (emojiOnly != null) {
                        emojiOnly[0] = 0;
                        emojiOnly = null;
                    }
                }
                if (doneEmoji && i + 2 < length) {
                    char next = cs.charAt(i + 1);
                    if (next == 0xD83C) {
                        next = cs.charAt(i + 2);
                        if (next >= 0xDFFB && next <= 0xDFFF) {
                            emojiCode.append(cs.subSequence(i + 1, i + 3));
                            startLength += 2;
                            i += 2;
                        }
                    } else if (emojiCode.length() >= 2 && emojiCode.charAt(0) == 0xD83C && emojiCode.charAt(1) == 0xDFF4 && next == 0xDB40) {
                        i++;
                        while (true) {
                            emojiCode.append(cs.subSequence(i, i + 2));
                            startLength += 2;
                            i += 2;
                            if (i >= cs.length() || cs.charAt(i) != 0xDB40) {
                                i--;
                                break;
                            }
                        }

                    }
                }
                previousGoodIndex = i;
                char prevCh = c;
                for (int a = 0; a < 3; a++) {
                    if (i + 1 < length) {
                        c = cs.charAt(i + 1);
                        if (a == 1) {
                            if (c == 0x200D && emojiCode.length() > 0) {
                                emojiCode.append(c);
                                i++;
                                startLength++;
                                doneEmoji = false;
                            }
                        } else if (startIndex != -1 || prevCh == '*' || prevCh >= '1' && prevCh <= '9') {
                            if (c >= 0xFE00 && c <= 0xFE0F) {
                                i++;
                                startLength++;
                            }
                        }
                    }
                }
                if (doneEmoji && i + 2 < length && cs.charAt(i + 1) == 0xD83C) {
                    char next = cs.charAt(i + 2);
                    if (next >= 0xDFFB && next <= 0xDFFF) {
                        emojiCode.append(cs.subSequence(i + 1, i + 3));
                        startLength += 2;
                        i += 2;
                    }
                }
                if (doneEmoji) {
                    if (emojiOnly != null) {
                        emojiOnly[0]++;
                    }
                    CharSequence code = emojiCode.subSequence(0, emojiCode.length());
                    try {
                        EmojiSpanNew span = new EmojiSpanNew(code, emojiSpanSizeRatio);
                        s.setSpan(span, startIndex, startIndex + startLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        emojiCount++;
                    } catch (Exception ignored) {
                    }
                    startLength = 0;
                    startIndex = -1;
                    emojiCode.setLength(0);
                    doneEmoji = false;
                }
                if (Build.VERSION.SDK_INT < 23 && emojiCount >= 50) {
                    break;
                }
            }
        } catch (Exception e) {
            return cs;
        }
        return s;
    }

    public static final String[] skinColors = {"", "\uD83C\uDFFB", "\uD83C\uDFFC", "\uD83C\uDFFD", "\uD83C\uDFFE", "\uD83C\uDFFF"};

    public static String setColorToCode(String code, int color) {
        return setColorToCode(code, skinColors[color]);
    }

    public static String setColorToCode(String code, String color) {
        //This part is for sex or other devices which come after the skin tone
        String end = null;
        int length = code.length();
        if (length > 2 && code.charAt(code.length() - 2) == '\u200D') {
            end = code.substring(code.length() - 2);
            code = code.substring(0, code.length() - 2);
        } else if (length > 3 && code.charAt(code.length() - 3) == '\u200D') {
            end = code.substring(code.length() - 3);
            code = code.substring(0, code.length() - 3);
        }

        code += color;
        if (end != null) {
            code += end;
        }
        return code;
    }

    /**
     * The inverse function of {@link EmojiUtils#setColorToCode}
     *
     * @param code The code with color
     * @return A pair with first component is the emoji without the skin color and the second component is the skin color
     */
    public static Pair<String, String> extractRawAndColorFromCode(String code) {
        String extractedRawEmoji = code;
        for (int i = 1; i < skinColors.length; i++) {
            String color = skinColors[i];
            if ((extractedRawEmoji = code.replace(color, "")).length() != code.length())
                return new Pair<>(extractedRawEmoji, color);
        }
        return new Pair<>(extractedRawEmoji, "");
    }

    public static void loadAllEmojis() {
        for (int p = 0; p < 8; p++)
            for (int p2 = 0; p2 < 4; p2++)
                if (emojiBmp[p][p2] == null)
                    loadEmoji(p, p2);
    }


}
