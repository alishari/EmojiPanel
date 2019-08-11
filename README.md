# EmojiPanel
A set of components and classes that make it possible to have emoticons in your app.
(Image assets and some sources are grabbed from [Telegram open source project](https://github.com/DrKLO/Telegram)

## Components and Widgets:
* `EmojiPanel`: A panel with tabs and list which will be bound to an EditText and insert emoticons in it.
* `EmojiEditText` and `EmojiTextView`: Provide automatic conversion of emoji characters to supported `EmojiSpan`s
* `EmojiImageView`: An ImageView which has a `emojiCode` property used to set a displaying emoji. Also has `showSkinColor` which causes a circle to be drawn at bottom right corner of it.
* `EmojiSpan`: A `ReplacementSpan` used to display our emojis in texts.
* `PagerPanel`: A panel with a ViewPager and tabs to support a panel with multy views shown as pages.

## How to use:
* Simply initialize it with one line (before using any part of the library):
```kotlin
EmojiUtils.initialize(context)
```
And now you can use the components.

### How to handle keyboard and swithes with my panel?
A class called `PanelOpenHelper` is made for this purpose. But the area you want to have your panel in it must has the following structure:
```xml
<FrameLayout>
  <Everything that you want to show.../>      nonEmojiContent
  <Your Panel/>                               panel
</FrameLayout>
```
To get the helper class working correctly, it is important to consider that the parent must be a `FrameLayout` and your panel must be a direct child of it. Also your activity's `windowSoftInputMode` must be `adjustResize`.
Then you can use it like this:
```kotlin
openHelper = PanelOpenHelper(context, theTextToGetFocus, panel, btn_switch, nonEmojiContent, frameParent)
openHelper.panelVisibilityChanged += { _, args -> if (args.isVisible) emojiPanel.updateRecentEmojis() }
btn_switch.setOnClickListener { openHelper.switchPanel() }

//If you want to close the panel when back button is pressed
override fun onBackPressed() {
  if (openHelper.isPanelShowing)
    openHelper.closePanel()
  else
    super.onBackPressed()
}
```
