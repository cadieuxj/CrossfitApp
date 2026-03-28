package com.apexai.crossfit.feature.nutrition.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexai.crossfit.core.domain.model.MacroEntry
import com.apexai.crossfit.core.domain.model.MacroTargets
import com.apexai.crossfit.core.domain.model.MealType
import com.apexai.crossfit.core.ui.components.ApexCard
import com.apexai.crossfit.core.ui.components.ApexTextField
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.components.ShimmerBox
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BlazeOrange
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.CornerMedium
import com.apexai.crossfit.core.ui.theme.CornerSmall
import com.apexai.crossfit.core.ui.theme.CornerXLarge
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.SurfaceElevated
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroLogScreen(
    viewModel: MacroLogViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { /* toast/snackbar could go here */ }
    }

    Scaffold(
        containerColor = BackgroundDeepBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Today's Nutrition", style = ApexTypography.headlineMedium, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundDeepBlack)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(MacroLogEvent.AddEntry) },
                containerColor = ElectricBlue,
                contentColor = BackgroundDeepBlack
            ) {
                Icon(Icons.Outlined.Add, "Add food")
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerBox(Modifier.fillMaxWidth().height(120.dp))
                ShimmerBox(Modifier.fillMaxWidth().height(200.dp))
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Daily summary ring card
                item {
                    DailySummaryCard(
                        summary = uiState.summary,
                        targets = uiState.targets
                    )
                    Spacer(Modifier.height(24.dp))
                }

                // Meals grouped by type
                val grouped = uiState.summary?.entries?.groupBy { it.mealType } ?: emptyMap()
                if (grouped.isEmpty()) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.LocalFireDepartment, null,
                                    tint = TextSecondary, modifier = Modifier.size(40.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No meals logged today", style = ApexTypography.bodyMedium, color = TextSecondary)
                                Text("Tap + to add your first meal", style = ApexTypography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }

                MealType.values().forEach { mealType ->
                    val entries = grouped[mealType] ?: return@forEach
                    item {
                        Text(
                            mealType.name.replace("_", " "),
                            style = ApexTypography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(entries) { entry ->
                        FoodEntryRow(
                            entry = entry,
                            onDelete = { viewModel.onEvent(MacroLogEvent.DeleteEntry(entry.id)) }
                        )
                        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                    }
                }
            }
        }
    }

    if (uiState.showEntrySheet) {
        FoodEntrySheet(
            state = uiState,
            onEvent = viewModel::onEvent
        )
    }
}

@Composable
private fun DailySummaryCard(summary: com.apexai.crossfit.core.domain.model.DailyMacroSummary?, targets: MacroTargets?) {
    ApexCard {
        Column(Modifier.fillMaxWidth()) {
            Text("TODAY'S MACROS", style = ApexTypography.labelSmall, color = TextSecondary)
            Spacer(Modifier.height(12.dp))

            // Calorie bar
            val calTarget = targets?.caloriesKcal ?: 2500
            val calActual = summary?.totalCalories ?: 0
            val calProgress = (calActual.toFloat() / calTarget.toFloat()).coerceIn(0f, 1f)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$calActual kcal", style = ApexTypography.headlineSmall, color = TextPrimary)
                Text("/ $calTarget", style = ApexTypography.bodySmall, color = TextSecondary)
            }
            Spacer(Modifier.height(6.dp))
            MacroProgressBar(progress = calProgress, color = BlazeOrange)

            Spacer(Modifier.height(16.dp))

            // Macro pills row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MacroPill(
                    label   = "Protein",
                    actual  = summary?.totalProteinG?.roundToInt() ?: 0,
                    target  = targets?.proteinG ?: 175,
                    unit    = "g",
                    color   = ElectricBlue
                )
                MacroPill(
                    label   = "Carbs",
                    actual  = summary?.totalCarbsG?.roundToInt() ?: 0,
                    target  = targets?.carbsG ?: 250,
                    unit    = "g",
                    color   = NeonGreen
                )
                MacroPill(
                    label   = "Fat",
                    actual  = summary?.totalFatG?.roundToInt() ?: 0,
                    target  = targets?.fatG ?: 80,
                    unit    = "g",
                    color   = BlazeOrange
                )
            }
        }
    }
}

@Composable
private fun MacroProgressBar(progress: Float, color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CornerSmall)
            .background(SurfaceDark)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(6.dp)
                .clip(CornerSmall)
                .background(color)
        )
    }
}

@Composable
private fun MacroPill(label: String, actual: Int, target: Int, unit: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$actual$unit", style = ApexTypography.titleMedium, color = color)
        Text("/ $target$unit", style = ApexTypography.bodySmall, color = TextSecondary)
        Text(label, style = ApexTypography.labelSmall, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        val progress = (actual.toFloat() / target.toFloat()).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(4.dp)
                .clip(CornerSmall)
                .background(SurfaceDark)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(4.dp)
                    .clip(CornerSmall)
                    .background(color)
            )
        }
    }
}

@Composable
private fun FoodEntryRow(entry: MacroEntry, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.foodName, style = ApexTypography.titleMedium, color = TextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${entry.calories} kcal", style = ApexTypography.bodySmall, color = BlazeOrange)
                Text("P: ${entry.proteinG.roundToInt()}g", style = ApexTypography.bodySmall, color = ElectricBlue)
                Text("C: ${entry.carbsG.roundToInt()}g", style = ApexTypography.bodySmall, color = NeonGreen)
                Text("F: ${entry.fatG.roundToInt()}g", style = ApexTypography.bodySmall, color = TextSecondary)
            }
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Outlined.Delete, "Delete", tint = ColorError, modifier = Modifier.size(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodEntrySheet(
    state: MacroLogUiState,
    onEvent: (MacroLogEvent) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onEvent(MacroLogEvent.DismissEntrySheet) },
        sheetState = sheetState,
        containerColor = SurfaceElevated,
        shape = CornerXLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Add Food", style = ApexTypography.headlineMedium, color = TextPrimary)

            // Food name + autofill suggestions
            ApexTextField(
                value = state.entryFoodName,
                onValueChange = { onEvent(MacroLogEvent.FoodNameChanged(it)) },
                label = "Food name",
                modifier = Modifier.fillMaxWidth()
            )

            if (state.foodSuggestions.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CornerMedium)
                        .background(SurfaceDark)
                ) {
                    state.foodSuggestions.forEach { food ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEvent(MacroLogEvent.FoodSuggestionSelected(food)) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(food.name, style = ApexTypography.bodyMedium, color = TextPrimary)
                                Text(
                                    "P:${food.proteinG.roundToInt()}g  C:${food.carbsG.roundToInt()}g  F:${food.fatG.roundToInt()}g",
                                    style = ApexTypography.bodySmall, color = TextSecondary
                                )
                            }
                            Text("${food.calories} kcal", style = ApexTypography.bodySmall, color = BlazeOrange)
                        }
                        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                    }
                }
            }

            // Macro inputs
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ApexTextField(
                    value = state.entryCalories,
                    onValueChange = { onEvent(MacroLogEvent.CaloriesChanged(it)) },
                    label = "Calories",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                ApexTextField(
                    value = state.entryProtein,
                    onValueChange = { onEvent(MacroLogEvent.ProteinChanged(it)) },
                    label = "Protein (g)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ApexTextField(
                    value = state.entryCarbs,
                    onValueChange = { onEvent(MacroLogEvent.CarbsChanged(it)) },
                    label = "Carbs (g)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                ApexTextField(
                    value = state.entryFat,
                    onValueChange = { onEvent(MacroLogEvent.FatChanged(it)) },
                    label = "Fat (g)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            // Meal type selector
            MealTypeSelector(
                selected = state.entryMealType,
                onSelect = { onEvent(MacroLogEvent.MealTypeChanged(it)) }
            )

            PrimaryButton(
                text = "Add",
                onClick = { onEvent(MacroLogEvent.SubmitEntry) },
                enabled = state.entryFoodName.isNotBlank() && !state.isSubmitting,
                isLoading = state.isSubmitting,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MealTypeSelector(selected: MealType, onSelect: (MealType) -> Unit) {
    val shortLabels = mapOf(
        MealType.BREAKFAST    to "Breakfast",
        MealType.LUNCH        to "Lunch",
        MealType.DINNER       to "Dinner",
        MealType.SNACK        to "Snack",
        MealType.PRE_WORKOUT  to "Pre-WO",
        MealType.POST_WORKOUT to "Post-WO"
    )
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MealType.values().forEach { type ->
            val isSelected = selected == type
            Box(
                modifier = Modifier
                    .clip(CornerMedium)
                    .background(if (isSelected) ElectricBlue.copy(0.2f) else BackgroundDeepBlack)
                    .border(1.dp, if (isSelected) ElectricBlue else BorderSubtle, CornerMedium)
                    .clickable { onSelect(type) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    shortLabels[type] ?: type.name,
                    style = ApexTypography.labelLarge,
                    color = if (isSelected) ElectricBlue else TextSecondary
                )
            }
        }
    }
}
