package com.spacca.app.ui.screens.customization

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spacca.app.data.CatalogRepository
import com.spacca.app.data.model.DrinkSelection
import com.spacca.app.data.model.DrinkSlot
import com.spacca.app.data.model.DrinkSlotOption
import com.spacca.app.data.model.DrinkSlotTypeOption
import com.spacca.app.data.model.DrinkSlotVolume
import com.spacca.app.ui.components.CupSimulator
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.ButtonVariant
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.DefaultTopBar
import com.spacca.app.ui.components.IngredientSummary
import com.spacca.app.ui.components.SpaccaImage
import com.spacca.app.ui.components.buildCupLayers
import com.spacca.app.ui.components.clickableNoRipple
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.BackgroundSecondary
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.Grey
import com.spacca.app.ui.theme.LightGrey
import com.spacca.app.ui.theme.MediumGrey
import com.spacca.app.ui.theme.White
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun CustomizationScreen(
    drinkId: Int,
    name: String,
    price: Double,
    initialQty: Int,
    onBack: () -> Unit,
    onAdd: (quantity: Int, unitPrice: Double, selections: List<DrinkSelection>, customizationSummary: String?) -> Unit
) {
    val catalog = koinInject<CatalogRepository>()
    val scope = rememberCoroutineScope()

    var slots by remember { mutableStateOf<List<DrinkSlot>>(emptyList()) }
    var cupSizeMl by remember { mutableStateOf<Int?>(null) }
    var productImage by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf(initialQty.coerceAtLeast(1)) }

    // slotId -> current selection
    val selections = remember { mutableStateMapOf<Int, DrinkSelection>() }

    if (loading) {
        loading = false
        scope.launch {
            try {
                val detail = catalog.drinkDetail(drinkId)
                val recipe = detail.slots
                slots = recipe
                cupSizeMl = detail.drink?.cupSizeMl
                productImage = detail.drink?.imageUrl
                // Initialize defaults for each slot
                recipe.forEach { slot ->
                    val def = defaultSelection(slot)
                    if (def != null) selections[slot.slotId ?: -1] = def
                }
            } catch (_: Exception) {
                loadError = true
            }
        }
    }

    // Slots with customerSortOrder == 0 are hidden from the customer (barista-only
    // ingredients). Their cost is already included in the drink's standard price,
    // so they are excluded from the extra-cost sum to avoid double-counting.
    val visibleSlots = slots.filter { (it.customerSortOrder ?: 1) > 0 }

    val unitPrice = price + visibleSlots.sumOf { slot ->
        val sid = slot.slotId
        if (sid != null) extraCost(slot, selections[sid]) else 0.0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Customize", onBack = onBack)

        if (loadError) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                DefaultText(
                    text = "Couldn't load customization options",
                    fontSize = 15,
                    fontColor = LightGrey
                )
                Spacer(modifier = Modifier.height(16.dp))
                DefaultButton(
                    text = "Try Again",
                    onClick = {
                        loadError = false
                        loading = true
                    }
                )
            }
            return@Column
        }

        // Fixed header: drink name + price + CupSimulatorSection
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
        ) {
            DefaultText(
                text = name,
                fontSize = 20,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            DefaultText(
                text = "Base EGP ${"%.2f".format(price)}",
                fontSize = 13,
                fontColor = MediumGrey
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (cupSizeMl != null) {
                // It's a drink (has a cup size) -> show the cup simulator.
                if (visibleSlots.isNotEmpty()) {
                    CupSimulatorSection(
                        slots = visibleSlots,
                        selections = selections,
                        cupSizeMl = cupSizeMl
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            } else {
                // No cup size -> it's dessert/bakery/etc., not a drink.
                // Show the product image instead of the cup simulator.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BackgroundSecondary)
                        .border(
                            width = 1.dp,
                            color = DarkBorder,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    SpaccaImage(
                        imageUrl = productImage,
                        contentDescription = name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(160.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Scrollable area: slot sections + quantity
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            if (visibleSlots.isEmpty()) {
                DefaultText(
                    text = "No customization options available for this drink.",
                    fontSize = 14,
                    fontColor = LightGrey
                )
            } else {
                visibleSlots.forEach { slot ->
                    val sid = slot.slotId ?: -1
                    SlotSection(
                        slot = slot,
                        selection = selections[sid],
                        onSelect = { sel -> selections[sid] = sel }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            QuantitySection(
                quantity = quantity,
                onDecrease = { if (quantity > 1) quantity-- },
                onIncrease = { quantity++ }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Fixed bottom action bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .background(BackgroundPrimary)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DefaultButton(
                    text = "Cancel",
                    onClick = onBack,
                    variant = ButtonVariant.SECONDARY,
                    modifier = Modifier.weight(0.8f)
                )
                DefaultButton(
                    text = "Add to Cart  ·  EGP ${"%.2f".format(unitPrice * quantity)}",
                    onClick = {
                        val selList = selections.values.toList()
                        onAdd(quantity, unitPrice, selList, buildSummary(visibleSlots, selList))
                    },
                    modifier = Modifier.weight(1.2f),
                    fontSize = 14
                )
            }
        }
    }
}

@Composable
private fun QuantitySection(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        DefaultText(
            text = "Quantity",
            fontSize = 14,
            fontWeight = FontWeight.Medium,
            fontColor = Grey
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (quantity > 1) DarkBorder else DarkBorder.copy(alpha = 0.4f))
                    .clickableNoRipple(enabled = quantity > 1) { onDecrease() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Decrease",
                    tint = if (quantity > 1) White else MediumGrey,
                    modifier = Modifier.size(20.dp)
                )
            }
            DefaultText(
                text = quantity.toString(),
                fontSize = 16,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBorder)
                    .clickableNoRipple { onIncrease() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Increase",
                    tint = White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SlotSection(
    slot: DrinkSlot,
    selection: DrinkSelection?,
    onSelect: (DrinkSelection) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundSecondary)
            .border(
                width = 1.dp,
                color = DarkBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultText(
                text = slot.slotLabel ?: "Option",
                fontSize = 15,
                fontWeight = FontWeight.SemiBold,
                fontColor = White
            )
            if (slot.isRequired == true) {
                DefaultText(
                    text = "Required",
                    fontSize = 11,
                    fontColor = MediumGrey
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val isTyped = slot.slotStyle == "typed" && slot.typeOptions.isNotEmpty()

        if (isTyped) {
            // Type option chips (FlowRow so they wrap naturally)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                slot.typeOptions.forEach { typeOpt ->
                    val selectedType = selection?.ingredientTypeId == typeOpt.ingredientTypeId
                    ChoiceChip(
                        text = typeOpt.typeName ?: "Option",
                        selected = selectedType,
                        onClick = {
                            // Select the type option. If it has volumes, also pick the
                            // default volume. If it has NO volumes, select the type alone
                            // (no typeVolumeId) so the option is still selectable.
                            val vol = defaultVolume(typeOpt)
                            onSelect(
                                DrinkSelection(
                                    slotId = slot.slotId,
                                    typeVolumeId = vol?.typeVolumeId,
                                    ingredientTypeId = typeOpt.ingredientTypeId
                                )
                            )
                        }
                    )
                }
            }

            // Volumes for the selected type (only if it has volumes)
            val selectedType = slot.typeOptions.firstOrNull { typeOpt ->
                selection?.ingredientTypeId == typeOpt.ingredientTypeId
            } ?: slot.typeOptions.firstOrNull { it.isDefault == true } ?: slot.typeOptions.firstOrNull()

            if (selectedType != null && selectedType.volumes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedType.volumes.forEach { vol ->
                        ChoiceChip(
                            text = volumeLabel(vol),
                            selected = selection?.typeVolumeId == vol.typeVolumeId,
                            onClick = {
                                if (vol.typeVolumeId != null) {
                                    onSelect(
                                        DrinkSelection(
                                            slotId = slot.slotId,
                                            typeVolumeId = vol.typeVolumeId,
                                            ingredientTypeId = selectedType.ingredientTypeId
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
            }
        } else {
            // Legacy option chips (FlowRow so they wrap naturally)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                slot.options.forEach { opt ->
                    ChoiceChip(
                        text = opt.label ?: "Option",
                        selected = selection?.optionId == opt.optionId,
                        onClick = {
                            if (opt.optionId != null) {
                                onSelect(
                                    DrinkSelection(
                                        slotId = slot.slotId,
                                        optionId = opt.optionId,
                                        ingredientId = slot.ingredientId
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    scale.animateTo(
                        targetValue = 0.93f,
                        animationSpec = tween(80, easing = FastOutSlowInEasing)
                    )
                }
                is PressInteraction.Release, is PressInteraction.Cancel -> {
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) AccentGreen else BackgroundPrimary)
            .border(
                width = 1.dp,
                color = if (selected) AccentGreen else DarkBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        DefaultText(
            text = text,
            fontSize = 13,
            fontColor = if (selected) BackgroundPrimary else White,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ---- Helpers ----

private fun defaultSelection(slot: DrinkSlot): DrinkSelection? {
    if (slot.slotStyle == "typed" && slot.typeOptions.isNotEmpty()) {
        val typeOpt = slot.typeOptions.firstOrNull { it.isDefault == true }
            ?: slot.typeOptions.firstOrNull()
            ?: return null
        val vol = defaultVolume(typeOpt)
        return DrinkSelection(
            slotId = slot.slotId,
            typeVolumeId = vol?.typeVolumeId,
            ingredientTypeId = typeOpt.ingredientTypeId
        )
    }
    if (slot.options.isNotEmpty()) {
        val opt = slot.options.firstOrNull { it.isDefault == true }
            ?: slot.options.firstOrNull()
            ?: return null
        return DrinkSelection(
            slotId = slot.slotId,
            optionId = opt.optionId,
            ingredientId = slot.ingredientId
        )
    }
    return null
}

// Build a human-readable summary of the selected customizations, e.g.
// "Coffee: Ethiobian · Triple" joined by ", ".
private fun buildSummary(slots: List<DrinkSlot>, selections: List<DrinkSelection>): String? {
    val parts = slots.mapNotNull { slot ->
        val sel = selections.firstOrNull { it.slotId == slot.slotId } ?: return@mapNotNull null
        val label = slot.slotLabel ?: return@mapNotNull null
        if (slot.slotStyle == "typed") {
            val typeOpt = slot.typeOptions.firstOrNull { to -> to.ingredientTypeId == sel.ingredientTypeId }
                ?: return@mapNotNull null
            val typeName = typeOpt.typeName ?: ""
            val tvId = sel.typeVolumeId
            val vol = typeOpt.volumes.firstOrNull { it.typeVolumeId == tvId }
            val volName = vol?.volumeName ?: ""
            if (volName.isNotEmpty()) "$label: $typeName · $volName" else "$label: $typeName"
        } else {
            val opt = slot.options.firstOrNull { it.optionId == sel.optionId }
            val optName = opt?.label ?: return@mapNotNull null
            "$label: $optName"
        }
    }
    return if (parts.isEmpty()) null else parts.joinToString(", ")
}

private fun defaultVolume(typeOpt: DrinkSlotTypeOption): DrinkSlotVolume? {
    return typeOpt.volumes.firstOrNull { it.isDefault == true }
        ?: typeOpt.volumes.firstOrNull()
}

private fun extraCost(slot: DrinkSlot, selection: DrinkSelection?): Double {
    if (selection == null) return 0.0
    if (slot.slotStyle == "typed") {
        val typeOpt = slot.typeOptions.firstOrNull { to -> to.ingredientTypeId == selection.ingredientTypeId }
            ?: return 0.0
        // Type-level extra cost applies regardless of volume
        var cost = typeOpt.extraCost ?: 0.0
        // Volume-level extra cost only if a volume is selected
        val tvId = selection.typeVolumeId
        if (tvId != null) {
            val vol = typeOpt.volumes.firstOrNull { it.typeVolumeId == tvId }
            cost += vol?.extraCost ?: 0.0
        }
        return cost
    }
    val opt = slot.options.firstOrNull { it.optionId == selection.optionId } ?: return 0.0
    return opt.extraCost ?: 0.0
}

private fun volumeLabel(vol: DrinkSlotVolume): String {
    val cost = vol.extraCost ?: 0.0
    return if (cost > 0) "${vol.volumeName ?: ""} +${"%.0f".format(cost)}" else (vol.volumeName ?: "")
}

// Two-column section: ingredient summary (left) + 3D cup simulator (right).
@Composable
private fun CupSimulatorSection(
    slots: List<DrinkSlot>,
    selections: Map<Int, DrinkSelection>,
    cupSizeMl: Int? = null
) {
    val layers = buildCupLayers(slots, selections, cupSizeMl)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundSecondary)
            .border(
                width = 1.dp,
                color = DarkBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IngredientSummary(
                slots = slots,
                selections = selections,
                modifier = Modifier.weight(0.35f)
            )
            CupSimulator(
                layers = layers,
                cupSizeMl = cupSizeMl,
                modifier = Modifier
                    .weight(0.65f)
                    .height(200.dp)
            )
        }
    }
}
