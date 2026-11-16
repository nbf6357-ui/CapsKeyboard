package com.example.capskeyboard

import android.inputmethodservice.InputMethodService
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import androidx.emoji2.emojipicker.EmojiPickerView

class CapsKeyboardService : InputMethodService() {

    private var letterCount = 0

    private val rows = listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")

    private lateinit var lettersView: View
    private lateinit var emojiView: View

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        letterCount = 0
    }

    override fun onCreateInputView(): View {
        lettersView = buildLettersView()
        emojiView = buildEmojiView()
        return lettersView
    }

    private fun buildLettersView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF263238.toInt())
            setPadding(dp(4), dp(6), dp(4), dp(6))
        }
        for (row in rows) {
            val line = newRow()
            for (ch in row) {
                line.addView(key(ch.toString(), 1f) { typeLetter(ch) })
            }
            if (row == rows.last()) {
                line.addView(key("⌫", 1.5f) { backspace() })
            }
            root.addView(line)
        }
        val bottom = newRow()
        bottom.addView(key("😀", 1.3f) { setInputView(emojiView) })
        bottom.addView(key(",", 1f) { typeText(",") })
        bottom.addView(key("'", 1f) { typeText("'") })
        bottom.addView(key("space", 4f) { typeText(" ") })
        bottom.addView(key(".", 1f) { typeText(".") })
        bottom.addView(key("⏎", 1.5f) { enter() })
        root.addView(bottom)
        return root
    }

    private fun buildEmojiView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF263238.toInt())
            setPadding(dp(4), dp(6), dp(4), dp(6))
        }
        val themed = ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault)
        val picker = EmojiPickerView(themed).apply {
            emojiGridColumns = 9
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(280)
            )
            setOnEmojiPickedListener { item -> typeText(item.emoji) }
        }
        root.addView(picker)
        val bar = newRow()
        bar.addView(key("ABC", 1.5f) { setInputView(lettersView) })
        bar.addView(key("space", 4f) { typeText(" ") })
        bar.addView(key("⌫", 1.5f) { backspace() })
        root.addView(bar)
        return root
    }

    private fun typeLetter(c: Char) {
        letterCount++
        val out = if (letterCount % 3 == 0) c.uppercaseChar() else c.lowercaseChar()
        currentInputConnection?.commitText(out.toString(), 1)
    }

    private fun typeText(s: String) {
        currentInputConnection?.commitText(s, 1)
    }

    private fun backspace() {
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(1, 0)
        if (!before.isNullOrEmpty() && before[0].isLetter() && letterCount > 0) {
            letterCount--
        }
        ic.deleteSurroundingText(1, 0)
    }

    private fun enter() {
        val ic = currentInputConnection ?: return
        val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
        if (action != null && action != EditorInfo.IME_ACTION_NONE &&
            action != EditorInfo.IME_ACTION_UNSPECIFIED
        ) {
            ic.performEditorAction(action)
        } else {
            ic.commitText("\n", 1)
        }
    }

    private fun newRow() = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun key(label: String, weight: Float, onClick: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 18f
        setTextColor(0xFFFFFFFF.toInt())
        setBackgroundColor(0xFF37474F.toInt())
        minHeight = 0
        minimumHeight = 0
        setPadding(0, dp(12), 0, dp(12))
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
            .apply { setMargins(dp(2), dp(2), dp(2), dp(2)) }
        setOnClickListener { onClick() }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
