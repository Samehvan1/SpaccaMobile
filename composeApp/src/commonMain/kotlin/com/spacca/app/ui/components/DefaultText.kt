package com.spacca.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.spacca.app.ui.theme.White

// Shared text primitive (app_style_theme_guide.md §3.3)
@Composable
fun DefaultText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 14,
    lineHeight: Int = 16,
    fontColor: Color = White,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Start,
    style: TextStyle? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        modifier = modifier,
        color = fontColor,
        fontSize = fontSize.sp,
        lineHeight = lineHeight.sp,
        fontWeight = fontWeight,
        textAlign = textAlign,
        style = style ?: TextStyle.Default,
        maxLines = maxLines,
        overflow = overflow
    )
}
