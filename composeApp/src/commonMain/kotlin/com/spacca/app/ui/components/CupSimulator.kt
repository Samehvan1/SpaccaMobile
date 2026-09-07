package com.spacca.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacca.app.data.model.DrinkSelection
import com.spacca.app.data.model.DrinkSlot
import com.spacca.app.data.model.DrinkSlotTypeOption
import com.spacca.app.ui.theme.MediumGrey
import com.spacca.app.ui.theme.White
import kotlin.math.PI
import kotlin.math.sin

// ============================================================
// Data
// ============================================================

data class CupLayer(
    val id: String,
    val label: String,
    val volumeMl: Float,
    val color: Color,
    val category: String,
    val isDynamic: Boolean = false
)

// ============================================================
// Color Resolver (ported from POS cup-simulator.tsx)
// ============================================================

object CupColorResolver {
    private val categoryColors = mapOf(
        "coffee" to Color(0xFF4B2C20),
        "espresso" to Color(0xFF1A0F0A),
        "milk" to Color(0xFFFDF5E6),
        "syrup" to Color(0xFFD4A373),
        "sauce" to Color(0xFF7F4F24),
        "sweetener" to Color(0xFFFFFFFF),
        "topping" to Color(0xFFFB8500),
        "base" to Color(0xFFD1D5DB),
        "water" to Color(0xFF60A5FA),
        "other" to Color(0xFF9CA3AF),
        "ice" to Color(0xFFE0F2FE),
        "cream" to Color(0xFFF5F5DC),
        "foam" to Color(0xFFF3E9D2),
        "chocolate" to Color(0xFF5D4037)
    )

    fun resolve(label: String, category: String): Color {
        val l = label.lowercase()
        val c = category.lowercase()
        return when {
            l.contains("milk") || c.contains("milk") ->
                categoryColors["milk"]!!
            l.contains("coffee") || c.contains("coffee") || c.contains("espresso") ->
                categoryColors["coffee"]!!
            l.contains("ice") || c.contains("ice") || c.contains("water") ->
                categoryColors["ice"]!!
            l.contains("cream") || c.contains("cream") ->
                categoryColors["cream"]!!
            l.contains("foam") || c.contains("foam") ->
                categoryColors["foam"]!!
            l.contains("chocolate") || c.contains("chocolate") ->
                categoryColors["chocolate"]!!
            l.contains("syrup") || c.contains("syrup") ->
                categoryColors["syrup"]!!
            l.contains("topping") || c.contains("topping") ->
                categoryColors["topping"]!!
            else -> categoryColors[c] ?: categoryColors["other"]!!
        }
    }
}

// ============================================================
// Layer Builder
// ============================================================

fun buildCupLayers(
    slots: List<DrinkSlot>,
    selections: Map<Int, DrinkSelection>,
    cupSizeMl: Int? = null
): List<CupLayer> {
    val layers = mutableListOf<CupLayer>()
    val dynamicCategories = setOf("milk", "water", "cream")

    for (slot in slots) {
        if ((slot.customerSortOrder ?: 1) <= 0) continue
        val selection = selections[slot.slotId ?: -1] ?: continue
        val slotLabel = slot.slotLabel ?: "Unknown"
        val cat = slotLabel.lowercase()

        if (slot.slotStyle == "typed") {
            val typeOpt = slot.typeOptions.firstOrNull {
                it.ingredientTypeId == selection.ingredientTypeId
            } ?: continue
            val typeName = typeOpt.typeName ?: "Unknown"
            val volumeMl = resolveVolumeMl(selection.typeVolumeId, typeOpt, slotLabel)

            // A "dynamic" ingredient fills remaining space: no fixed processedQty
            // and its category is a liquid filler (milk, water, cream).
            val isDynamic = (typeOpt.processedQty == null || typeOpt.processedQty <= 0f) &&
                    typeName.lowercase() != "none" &&
                    dynamicCategories.any { cat.contains(it) }

            layers.add(
                CupLayer(
                    id = "${slot.slotId}-${selection.ingredientTypeId}-${selection.typeVolumeId}",
                    label = typeName,
                    volumeMl = volumeMl,
                    color = CupColorResolver.resolve(typeName, cat),
                    category = cat,
                    isDynamic = isDynamic
                )
            )
        } else {
            val opt = slot.options.firstOrNull { it.optionId == selection.optionId }
                ?: continue
            val optLabel = opt.label ?: "Unknown"
            val volumeMl = opt.processedQty?.takeIf { it > 0f } ?: 50f

            layers.add(
                CupLayer(
                    id = "${slot.slotId}-${selection.optionId}",
                    label = optLabel,
                    volumeMl = volumeMl,
                    color = CupColorResolver.resolve(optLabel, cat),
                    category = cat
                )
            )
        }
    }

    // --- Dynamic fill adjustment ---
    if (cupSizeMl != null && cupSizeMl > 0 && layers.isNotEmpty()) {
        val dynamicLayers = layers.filter { it.isDynamic }
        val totalVol = layers.sumOf { it.volumeMl.toDouble() }.toFloat()

        if (dynamicLayers.isNotEmpty()) {
            // Scale the primary dynamic filler to fill remaining space
            val filler = dynamicLayers.maxByOrNull { it.volumeMl }!!
            val otherVol = totalVol - filler.volumeMl
            val remaining = (cupSizeMl - otherVol).coerceAtLeast(0f)
            val idx = layers.indexOf(filler)
            if (idx >= 0) {
                layers[idx] = filler.copy(volumeMl = remaining)
            }
        } else if (totalVol < cupSizeMl * 0.5f) {
            // No dynamic filler — ensure at least 50% fill
            val target = cupSizeMl * 0.5f
            val scale = if (totalVol > 0f) target / totalVol else 1f
            for (i in layers.indices) {
                layers[i] = layers[i].copy(volumeMl = layers[i].volumeMl * scale)
            }
        }
    }

    return layers
}

// Volume processedQty overrides the type option's processedQty; fall back to
// name-based estimation when neither is set.
private fun resolveVolumeMl(typeVolumeId: Int?, typeOpt: DrinkSlotTypeOption, category: String = ""): Float {
    if (typeVolumeId != null) {
        val vol = typeOpt.volumes.firstOrNull { it.typeVolumeId == typeVolumeId }
        val volQty = vol?.processedQty
        if (volQty != null && volQty > 0f) return volQty
    }
    val optQty = typeOpt.processedQty
    if (optQty != null && optQty > 0f) return optQty
    return estimateVolume(typeVolumeId, typeOpt, typeOpt.typeName ?: "", category)
}

private fun estimateVolume(
    typeVolumeId: Int?,
    typeOpt: DrinkSlotTypeOption,
    typeName: String,
    category: String = ""
): Float {
    if (typeVolumeId == null) {
        // No volume options — infer a sensible default from the ingredient
        val n = typeName.lowercase()
        if (n == "none" || n == "no" || n == "without") return 0f
        val cat = category.lowercase()
        return when {
            n.contains("foam") || cat.contains("foam") -> 25f
            n.contains("milk") || cat.contains("milk") -> 100f
            n.contains("sauce") || n.contains("syrup") || cat.contains("syrup") -> 30f
            n.contains("water") || cat.contains("water") -> 150f
            else -> 50f
        }
    }
    val volume = typeOpt.volumes.firstOrNull { it.typeVolumeId == typeVolumeId }
    val name = volume?.volumeName?.lowercase() ?: return 100f
    return when {
        name.contains("single") || name.contains("small") -> 30f
        name.contains("double") || name.contains("medium") -> 60f
        name.contains("triple") || name.contains("large") -> 90f
        name.contains("150") -> 150f
        name.contains("200") -> 200f
        name.contains("250") -> 250f
        name.contains("300") -> 300f
        name.contains("less") -> 170f
        name.contains("standard") -> 190f
        name.contains("more") -> 210f
        name.contains("1 pump") || name.contains("1 pack") -> 10f
        name.contains("2 pump") || name.contains("2 pack") -> 20f
        name.contains("3 pump") || name.contains("3 pack") -> 30f
        else -> 100f
    }
}

// ============================================================
// Bubbles
// ============================================================

private data class BubbleData(
    val x: Float,
    val initialY: Float,
    val sizeDp: Float,
    val speed: Float
)

private fun generateBubbles(count: Int, seed: Int = 0): List<BubbleData> {
    val rng = kotlin.random.Random(seed)
    return List(count) {
        BubbleData(
            x = rng.nextFloat(),
            initialY = rng.nextFloat(),
            sizeDp = 1.5f + rng.nextFloat() * 2f,
            speed = 0.6f + rng.nextFloat() * 0.8f
        )
    }
}

// ============================================================
// Cup Simulator Composable
// ============================================================

@Composable
fun CupSimulator(
    layers: List<CupLayer>,
    modifier: Modifier = Modifier,
    cupSizeMl: Int? = null,
    showSmoke: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition()
    val textMeasurer = rememberTextMeasurer()

    // Time for bubble/steam animation (0..1 repeating over 5 s)
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Shimmer sweep position (-0.3 .. 1.3)
    val shimmerX by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Pre-generate bubbles per layer (stable across recompositions)
    val layersKey = layers.joinToString(",") { it.id }
    val bubblesMap = remember(layersKey) {
        layers.mapIndexed { index, layer ->
            val count = when {
                layer.category.contains("coffee") ||
                        layer.category.contains("espresso") -> 8
                layer.category.contains("milk") -> 5
                else -> 0
            }
            generateBubbles(count, seed = index * 1000)
        }
    }

    // Ice cube layout (stable across recompositions)
    val hasIce = remember(layersKey) {
        layers.any { it.category.contains("ice") || it.category.contains("water") }
    }
    val iceCubes = remember(layersKey) {
        if (hasIce) {
            val rng = kotlin.random.Random(7)
            List(4) {
                IceCubeData(
                    x = 0.18f + rng.nextFloat() * 0.64f,
                    sizeDp = 7f + rng.nextFloat() * 5f,
                    tilt = (rng.nextFloat() - 0.5f) * 0.5f,
                    bobPhase = rng.nextFloat() * PI.toFloat() * 2f
                )
            }
        } else emptyList()
    }

    // Steam wisps (stable across recompositions)
    val isHot = remember(layersKey) {
        layers.any {
            it.category.contains("coffee") || it.category.contains("espresso") ||
                    it.category.contains("milk") || it.category.contains("foam") ||
                    it.category.contains("chocolate")
        }
    }
    val steamWisps = remember(layersKey) {
        if (isHot) {
            listOf(
                SteamWisp(x = 0.38f, amp = 0.05f, speed = 0.9f, phase = 0f),
                SteamWisp(x = 0.55f, amp = 0.07f, speed = 1.1f, phase = 1.7f),
                SteamWisp(x = 0.70f, amp = 0.045f, speed = 0.8f, phase = 3.1f)
            )
        } else emptyList()
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val d = density

        // Cup dimensions
        val cupPaddingX = w * 0.18f
        val cupTopY = h * 0.06f
        val cupBottomY = h * 0.94f
        val cupHeight = cupBottomY - cupTopY

        val topHalfW = (w - cupPaddingX * 2) / 2f
        val bottomHalfW = topHalfW * 0.62f
        val cx = w * 0.58f

        val topLeftX = cx - topHalfW
        val topRightX = cx + topHalfW
        val bottomLeftX = cx - bottomHalfW
        val bottomRightX = cx + bottomHalfW

        val cornerR = 8f * d

        // Cup outline path (for clipping liquids)
        val cupPath = Path().apply {
            moveTo(topLeftX, cupTopY)
            lineTo(bottomLeftX, cupBottomY - cornerR)
            quadraticTo(
                bottomLeftX, cupBottomY,
                bottomLeftX + cornerR, cupBottomY
            )
            lineTo(bottomRightX - cornerR, cupBottomY)
            quadraticTo(
                bottomRightX, cupBottomY,
                bottomRightX, cupBottomY - cornerR
            )
            lineTo(topRightX, cupTopY)
            close()
        }

        // Shadow
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Black.copy(alpha = 0.28f),
                    Color.Transparent
                ),
                center = Offset(cx, cupBottomY + 12f * d),
                radius = bottomHalfW * 1.4f
            ),
            topLeft = Offset(cx - bottomHalfW * 1.4f, cupBottomY + 4f * d),
            size = Size(bottomHalfW * 2.8f, 28f * d)
        )

        // Glass glow (behind the clipped liquids)
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                center = Offset(cx, cupTopY + cupHeight * 0.4f),
                radius = bottomHalfW * 1.6f
            ),
            topLeft = Offset(cx - bottomHalfW * 1.6f, cupTopY - 10f * d),
            size = Size(bottomHalfW * 3.2f, cupHeight * 1.2f)
        )

        // Liquid layers (clipped to cup)
        drawContext.canvas.save()
        drawContext.canvas.clipPath(cupPath)

        val totalVol = layers.sumOf { it.volumeMl.toDouble() }.toFloat()
        // Absolute fill level when cup size is known; relative otherwise.
        // Shared by the liquid layers, ice cubes and steam wisps.
        val fillFraction = if (totalVol > 0f) {
            if (cupSizeMl != null && cupSizeMl > 0) {
                (totalVol / cupSizeMl).coerceIn(0.06f, 1f)
            } else {
                0.88f
            }
        } else 0f

        if (totalVol > 0f) {
            val liquidTopY = cupBottomY - cupHeight * fillFraction

            // Interior tint for the empty part of the cup
            if (liquidTopY > cupTopY + 2f * d) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF3A3530).copy(alpha = 0.18f),
                            Color(0xFF3A3530).copy(alpha = 0.08f)
                        ),
                        startY = cupTopY,
                        endY = liquidTopY
                    ),
                    topLeft = Offset(topLeftX, cupTopY),
                    size = Size(topRightX - topLeftX, liquidTopY - cupTopY)
                )
            }

            var curY = cupBottomY

            layers.forEachIndexed { idx, layer ->
                val rawH = (layer.volumeMl / totalVol) * (cupBottomY - liquidTopY)
                val lh = rawH.coerceAtLeast(4f * d)

                // Interpolate cup half-widths at layer edges
                val progBot = (cupBottomY - curY) / cupHeight
                val progTop = (cupBottomY - (curY - lh)) / cupHeight
                val rBot = bottomHalfW + (topHalfW - bottomHalfW) * progBot
                val rTop = bottomHalfW + (topHalfW - bottomHalfW) * progTop

                // Gradient fill for depth
                val grad = Brush.verticalGradient(
                    colors = listOf(
                        layer.color.copy(alpha = 0.90f),
                        layer.color,
                        layer.color.copy(alpha = 0.95f)
                    ),
                    startY = curY - lh,
                    endY = curY
                )

                val lPath = Path().apply {
                    moveTo(cx - rTop, curY - lh)
                    lineTo(cx - rBot, curY)
                    lineTo(cx + rBot, curY)
                    lineTo(cx + rTop, curY - lh)
                    close()
                }
                drawPath(lPath, grad)

                // Bubbles
                val bubbles = bubblesMap.getOrElse(idx) { emptyList() }
                bubbles.forEach { b ->
                    val bx = cx - rBot + (rBot * 2f) * b.x
                    val rawBY = curY - lh * b.initialY
                    val animatedY = rawBY - (time * b.speed * lh * 0.55f)
                    val finalY = if (animatedY < curY - lh) animatedY + lh else animatedY
                    val wobble = sin(time * 2f * PI.toFloat() * b.speed * 3f + b.x * 12f) * 1.5f * d

                    drawCircle(
                        color = Color.White.copy(alpha = 0.22f),
                        radius = b.sizeDp * d,
                        center = Offset(bx + wobble, finalY)
                    )
                }

                // Surface shimmer (top layer only)
                if (idx == layers.lastIndex) {
                    val surfY = curY - lh
                    val shimmerRadius = rTop * 0.5f
                    val shimmerCX = cx + (shimmerX - 0.5f) * rTop * 0.7f

                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.28f),
                                Color.Transparent
                            ),
                            center = Offset(shimmerCX, surfY),
                            radius = shimmerRadius
                        ),
                        topLeft = Offset(shimmerCX - shimmerRadius, surfY - 3f * d),
                        size = Size(shimmerRadius * 2f, 6f * d)
                    )
                }

                curY -= lh
            }

            // Ice cubes floating at the liquid surface
            if (iceCubes.isNotEmpty()) {
                val surfY = liquidTopY
                val surfR = bottomHalfW + (topHalfW - bottomHalfW) * fillFraction
                iceCubes.forEach { cube ->
                    val bob = sin(time * 2f * PI.toFloat() + cube.bobPhase) * 1.2f * d
                    val cubeX = cx - surfR + (surfR * 2f) * cube.x
                    val cubeSize = cube.sizeDp * d
                    val half = cubeSize / 2f
                    val cubeTopY = surfY + bob - half * 0.6f

                    // Rounded, slightly tilted translucent ice cube
                    val pivotX = cubeX
                    val pivotY = surfY + bob
                    drawContext.canvas.save()
                    drawContext.canvas.translate(pivotX, pivotY)
                    drawContext.canvas.rotate(cube.tilt * 15f)
                    drawContext.canvas.translate(-pivotX, -pivotY)
                    drawRoundRect(
                        color = Color(0xFFE8F4FD).copy(alpha = 0.55f),
                        topLeft = Offset(cubeX - half, cubeTopY),
                        size = Size(cubeSize, cubeSize * 0.6f),
                        cornerRadius = CornerRadius(3f * d, 3f * d)
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.4f),
                        topLeft = Offset(cubeX - half, cubeTopY),
                        size = Size(cubeSize, cubeSize * 0.6f),
                        cornerRadius = CornerRadius(3f * d, 3f * d),
                        style = Stroke(1f * d)
                    )
                    // Subtle inner highlight
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.3f),
                        topLeft = Offset(cubeX - half * 0.55f, cubeTopY + half * 0.15f),
                        size = Size(cubeSize * 0.55f, cubeSize * 0.22f),
                        cornerRadius = CornerRadius(2f * d, 2f * d)
                    )
                    drawContext.canvas.restore()
                }
            }
        } else {
            // Empty cup - subtle dashed outline + hint text
            drawPath(
                cupPath,
                color = Color.White.copy(alpha = 0.15f),
                style = Stroke(
                    width = 2f * d,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f * d, 8f * d))
                )
            )
            val hintLayout = textMeasurer.measure(
                text = "Select ingredients",
                style = TextStyle(
                    color = White.copy(alpha = 0.35f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            drawText(
                textLayoutResult = hintLayout,
                topLeft = Offset(
                    x = cx - hintLayout.size.width / 2f,
                    y = cupTopY + cupHeight * 0.42f
                )
            )
        }

        drawContext.canvas.restore()

        // Steam wisps rising above the liquid surface (hot drinks)
        if (showSmoke && steamWisps.isNotEmpty() && totalVol > 0f) {
            val surfY = cupBottomY - cupHeight * fillFraction
            val surfR = bottomHalfW + (topHalfW - bottomHalfW) * fillFraction

            steamWisps.forEach { wisp ->
                val baseX = cx - surfR + (surfR * 2f) * wisp.x
                val maxRise = cupHeight * 0.22f
                val rise = (sin(time * wisp.speed * PI.toFloat() + wisp.phase) + 1f) * 0.5f * maxRise
                val alpha = (1f - rise / maxRise) * 0.12f
                val wispPath = Path().apply {
                    val startY = surfY - 2f * d - rise
                    moveTo(baseX, startY)
                    quadraticTo(
                        baseX + wisp.amp * surfR * sin(time * 2f * PI.toFloat() + wisp.phase),
                        startY - cupHeight * 0.06f,
                        baseX + wisp.amp * surfR * 0.5f * sin(time * 2f * PI.toFloat() + wisp.phase + 1f),
                        startY - cupHeight * 0.12f
                    )
                }
                drawPath(
                    wispPath,
                    color = Color.White.copy(alpha = alpha),
                    style = Stroke(
                        width = 5f * d,
                        cap = StrokeCap.Round
                    )
                )
                // Diffusion pass for a softer cloud
                drawPath(
                    wispPath,
                    color = Color.White.copy(alpha = 0.06f),
                    style = Stroke(
                        width = 5f * d * 1.5f,
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        // Glass outline
        val edgeColor = Color.White.copy(alpha = 0.22f)

        // Left edge
        drawLine(
            edgeColor,
            Offset(topLeftX, cupTopY),
            Offset(bottomLeftX, cupBottomY),
            1.5f * d,
            cap = StrokeCap.Round
        )
        // Right edge
        drawLine(
            edgeColor.copy(alpha = 0.16f),
            Offset(topRightX, cupTopY),
            Offset(bottomRightX, cupBottomY),
            1.5f * d,
            cap = StrokeCap.Round
        )
        // Bottom curve
        drawPath(
            Path().apply {
                moveTo(bottomLeftX, cupBottomY)
                quadraticTo(cx, cupBottomY + 8f * d, bottomRightX, cupBottomY)
            },
            color = edgeColor,
            style = Stroke(
                1.5f * d,
                cap = StrokeCap.Round
            )
        )
        // Top rim (ellipse opening for 3D depth)
        drawOval(
            color = Color.White.copy(alpha = 0.32f),
            topLeft = Offset(topLeftX, cupTopY - 2f * d),
            size = Size(topRightX - topLeftX, 5f * d),
            style = Stroke(2f * d)
        )
        drawOval(
            color = Color.White.copy(alpha = 0.10f),
            topLeft = Offset(topLeftX + 2f * d, cupTopY - 1f * d),
            size = Size(topRightX - topLeftX - 4f * d, 3f * d),
            style = Stroke(1f * d)
        )

        // Glass highlight streaks
        val hlLeft = Path().apply {
            moveTo(topLeftX + topHalfW * 0.18f, cupTopY + cupHeight * 0.04f)
            lineTo(bottomLeftX + bottomHalfW * 0.12f, cupBottomY - cupHeight * 0.06f)
            lineTo(bottomLeftX + bottomHalfW * 0.20f, cupBottomY - cupHeight * 0.06f)
            lineTo(topLeftX + topHalfW * 0.26f, cupTopY + cupHeight * 0.04f)
            close()
        }
        drawPath(hlLeft, Color.White.copy(alpha = 0.13f))

        val hlRight = Path().apply {
            moveTo(topRightX - topHalfW * 0.22f, cupTopY + cupHeight * 0.08f)
            lineTo(bottomRightX - bottomHalfW * 0.18f, cupBottomY - cupHeight * 0.10f)
            lineTo(bottomRightX - bottomHalfW * 0.12f, cupBottomY - cupHeight * 0.10f)
            lineTo(topRightX - topHalfW * 0.16f, cupTopY + cupHeight * 0.08f)
            close()
        }
        drawPath(hlRight, Color.White.copy(alpha = 0.07f))

        // Top rim highlight
        drawLine(
            Color.White.copy(alpha = 0.15f),
            Offset(topLeftX + 4f * d, cupTopY - 1f * d),
            Offset(topRightX - 4f * d, cupTopY - 1f * d),
            1f * d,
            cap = StrokeCap.Round
        )
    }
}

private data class IceCubeData(
    val x: Float,
    val sizeDp: Float,
    val tilt: Float,
    val bobPhase: Float
)

private data class SteamWisp(
    val x: Float,
    val amp: Float,
    val speed: Float,
    val phase: Float
)

// ============================================================
// Ingredient Summary (left column) — one line per ingredient
// ============================================================

@Composable
fun IngredientSummary(
    slots: List<DrinkSlot>,
    selections: Map<Int, DrinkSelection>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.heightIn(max = 180.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        slots.filter { (it.customerSortOrder ?: 1) > 0 }.forEach { slot ->
            val sid = slot.slotId ?: -1
            val selection = selections[sid]
            val slotLabel = slot.slotLabel ?: "Option"

            val selectedName: String
            val layerColor: Color

            if (slot.slotStyle == "typed" && selection != null) {
                val typeOpt = slot.typeOptions.firstOrNull {
                    it.ingredientTypeId == selection.ingredientTypeId
                }
                val typeName = typeOpt?.typeName ?: ""
                val vol = typeOpt?.volumes?.firstOrNull {
                    it.typeVolumeId == selection.typeVolumeId
                }
                val volName = vol?.volumeName
                selectedName = if (!volName.isNullOrEmpty()) "$typeName · $volName" else typeName
                layerColor = CupColorResolver.resolve(typeName, slotLabel.lowercase())
            } else if (selection != null) {
                val opt = slot.options.firstOrNull { it.optionId == selection.optionId }
                selectedName = opt?.label ?: ""
                layerColor = CupColorResolver.resolve(selectedName, slotLabel.lowercase())
            } else {
                selectedName = ""
                layerColor = CupColorResolver.resolve("", slotLabel.lowercase())
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(layerColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                DefaultText(
                    text = if (selectedName.isEmpty()) "$slotLabel: —" else "$slotLabel: $selectedName",
                    fontSize = 11,
                    fontColor = White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}