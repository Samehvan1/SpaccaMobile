package com.spacca.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.InputFieldBackground
import com.spacca.app.ui.theme.Red
import com.spacca.app.ui.theme.White

// Text field (app_style_theme_guide.md §5.2)
@Composable
fun DefaultTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true
) {
    val borderColor = when {
        isError -> Red
        else -> DarkBorder
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(InputFieldBackground, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 13.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = singleLine,
            textStyle = TextStyle(color = White, fontSize = 12.sp),
            cursorBrush = SolidColor(AccentGreen),
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color(0xFF8A8A8A),
                            fontSize = 12.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}
