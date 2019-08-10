package com.momt.emojiplayground

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.momt.emojipanel.PanelOpenHelper
import com.momt.emojipanel.emoji.EmojiUtils
import com.momt.emojipanel.widgets.EmojiPanel
import kotlinx.android.synthetic.main.activity_main_new.*

class MainActivity : AppCompatActivity() {

    private lateinit var openHelper: PanelOpenHelper
    private lateinit var emojiPanel: EmojiPanel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiUtils.initialize(this)
        setContentView(R.layout.activity_main_new)


        emojiPanel = pager.getItemById(R.id.emoji_panel)

        emojiPanel.loadSettings()

        emojiPanel.boundEditText = txt
        emojiPanel.setHeadersColor(Color.RED)
        emojiPanel.setSelectedColor(Color.BLUE)
        emojiPanel.setDefaultTabColor(Color.MAGENTA)
        emojiPanel.enableSmoothScrollOnTabSelect = false
        pager.setAccentColor(Color.BLUE)
        pager.setDefaultTabColor(Color.MAGENTA)

        openHelper = PanelOpenHelper(this, txt, pager, btn_switch, findViewById(R.id.txtContainer), cl)

        openHelper.panelVisibilityChanged += { _, args -> if (args.isVisible) emojiPanel.updateRecentEmojis() }

        btn_switch.setOnClickListener { openHelper.switchPanel() }
    }


    override fun onBackPressed() {
        if (openHelper.isPanelShowing)
            openHelper.closePanel()
        else
            super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        emojiPanel.saveSettings()
        openHelper.saveSettings()
    }
}
