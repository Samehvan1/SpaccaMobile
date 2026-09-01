package com.spacca.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import com.spacca.app.data.resolveImageUrl
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.jetbrains.compose.resources.painterResource
import spaccamobile.composeapp.generated.resources.Res
import spaccamobile.composeapp.generated.resources.ic_coffee_cup

/**
 * Reusable image that loads a backend image URL (relative or absolute) via Kamel,
 * falling back to the local coffee-cup placeholder while loading / on failure.
 *
 * Pass `imageUrl` exactly as returned by the backend (e.g. "/uploads/foo.png");
 * it is resolved against [com.spacca.app.data.ApiConfig.BASE_URL] internally.
 */
@Composable
fun SpaccaImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val resolved = resolveImageUrl(imageUrl)
    val placeholder: Painter = painterResource(Res.drawable.ic_coffee_cup)

    if (resolved == null) {
        // No image URL — show the placeholder directly.
        Image(
            painter = placeholder,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
        return
    }

    KamelImage(
        resource = {
            asyncPainterResource(
                data = resolved,
                onLoadingPainter = { Result.success(placeholder) },
                onFailurePainter = { Result.success(placeholder) }
            )
        },
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    )
}
