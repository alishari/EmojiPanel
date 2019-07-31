package com.momt.emojiplayground

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.momt.emojipanel.PanelOpenHelperNew
import com.momt.emojipanel.widgets.EmojiPanel
import kotlinx.android.synthetic.main.activity_main_new.*

class MainActivity : AppCompatActivity() {

    private lateinit var openHelper: PanelOpenHelperNew
    private lateinit var emojiPanel: EmojiPanel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_new)

        emojiPanel = pager.getItemById(R.id.emoji_panel)

        emojiPanel.loadSettings()

        emojiPanel.txtBoundWith = txt
        emojiPanel.setHeadersColor(Color.RED)
        emojiPanel.setAccentColor(Color.BLUE)
        pager.setAccentColor(Color.BLUE)

        openHelper = PanelOpenHelperNew(this, txt, pager, btn_switch, findViewById(R.id.txtContainer), cl)

        openHelper.panelVisibilityChanged += { _, isVisible -> if (isVisible) emojiPanel.updateRecentEmojis() }

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
